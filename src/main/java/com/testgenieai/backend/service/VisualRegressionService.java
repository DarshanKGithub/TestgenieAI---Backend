package com.testgenieai.backend.service;

import com.testgenieai.backend.domain.TestRun;
import com.testgenieai.backend.domain.VisualBaseline;
import com.testgenieai.backend.domain.VisualComparison;
import com.testgenieai.backend.domain.VisualDiffStatus;
import com.testgenieai.backend.domain.user.AppUser;
import com.testgenieai.backend.dto.VisualBaselineResponse;
import com.testgenieai.backend.dto.VisualBaselineUpsertRequest;
import com.testgenieai.backend.dto.VisualCompareRequest;
import com.testgenieai.backend.dto.VisualComparisonResponse;
import com.testgenieai.backend.repository.AppUserRepository;
import com.testgenieai.backend.repository.TestRunRepository;
import com.testgenieai.backend.repository.VisualBaselineRepository;
import com.testgenieai.backend.repository.VisualComparisonRepository;
import com.testgenieai.backend.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VisualRegressionService {

    private final VisualBaselineRepository visualBaselineRepository;
    private final VisualComparisonRepository visualComparisonRepository;
    private final AppUserRepository appUserRepository;
    private final TestRunRepository testRunRepository;

    @Value("${visual.regression.storage-dir:./visual-regression-artifacts}")
    private String storageDir;

    @Transactional
    public VisualBaselineResponse upsertBaseline(VisualBaselineUpsertRequest request) {
        AppUser owner = appUserRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        BufferedImage baselineImage = decodeImage(request.baselineImageBase64());
        String imageHash = fingerprint(baselineImage);
        String imagePath = writeImageArtifact(owner.getId(), request.pageKey(), "baseline", baselineImage);

        VisualBaseline baseline = visualBaselineRepository
                .findByOwnerIdAndPageKey(owner.getId(), request.pageKey())
                .orElseGet(() -> VisualBaseline.builder()
                        .id(UUID.randomUUID())
                        .owner(owner)
                        .pageKey(request.pageKey())
                        .build());

        baseline.setBaselineHash(imageHash);
        baseline.setBaselineImagePath(imagePath);
        baseline.setUpdatedAt(LocalDateTime.now());

        VisualBaseline saved = visualBaselineRepository.save(baseline);
        return new VisualBaselineResponse(saved.getPageKey(), saved.getBaselineImagePath(), saved.getUpdatedAt());
    }

    @Transactional
    public VisualComparisonResponse compare(VisualCompareRequest request) {
        AppUser owner = appUserRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        VisualBaseline baseline = visualBaselineRepository
                .findByOwnerIdAndPageKey(owner.getId(), request.pageKey())
                .orElse(null);

        BufferedImage currentImage = decodeImage(request.currentImageBase64());
        String currentHash = fingerprint(currentImage);
        String currentImagePath = writeImageArtifact(owner.getId(), request.pageKey(), "current", currentImage);
        double threshold = request.thresholdPercent() == null ? 0.0 : Math.max(request.thresholdPercent(), 0.0);

        VisualDiffResult diffResult = baseline == null || baseline.getBaselineImagePath() == null
                ? new VisualDiffResult(VisualDiffStatus.NO_BASELINE, 100.0, null)
                : compareAgainstBaseline(baseline.getBaselineImagePath(), currentImage, request.pageKey(), owner.getId(), currentImagePath, threshold);

        TestRun testRun = null;
        if (request.runId() != null) {
            testRun = testRunRepository.findById(request.runId()).orElse(null);
        }

        VisualComparison saved = visualComparisonRepository.save(VisualComparison.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .testRun(testRun)
                .pageKey(request.pageKey())
                .baselineHash(baseline == null ? null : baseline.getBaselineHash())
                .baselineImagePath(baseline == null ? null : baseline.getBaselineImagePath())
                .currentHash(currentHash)
                .currentImagePath(currentImagePath)
                .diffImagePath(diffResult.diffImagePath)
                .diffPercent(diffResult.diffPercent)
                .status(diffResult.status)
                .createdAt(LocalDateTime.now())
                .build());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VisualComparisonResponse> history(String pageKey) {
        UUID ownerId = SecurityUtils.currentUserId();
        List<VisualComparison> items;
        if (pageKey == null || pageKey.isBlank()) {
            items = visualComparisonRepository.findTop20ByOwnerIdOrderByCreatedAtDesc(ownerId);
        } else {
            items = visualComparisonRepository.findTop20ByOwnerIdAndPageKeyOrderByCreatedAtDesc(ownerId, pageKey);
        }
        return items.stream().map(this::toResponse).toList();
    }

    private VisualComparisonResponse toResponse(VisualComparison item) {
        return new VisualComparisonResponse(
                item.getId(),
                item.getPageKey(),
                item.getBaselineImagePath(),
                item.getCurrentImagePath(),
                item.getDiffImagePath(),
                item.getDiffPercent(),
                item.getStatus(),
                item.getCreatedAt()
        );
    }

    private VisualDiffResult compareAgainstBaseline(String baselineImagePath, BufferedImage currentImage, String pageKey, UUID ownerId, String currentImagePath, double threshold) {
        try {
            BufferedImage baselineImage = ImageIO.read(Path.of(baselineImagePath).toFile());
            if (baselineImage == null) {
                return new VisualDiffResult(VisualDiffStatus.NO_BASELINE, 100.0, null);
            }

            DiffComputation computation = computePixelDiff(baselineImage, currentImage);
            String diffImagePath = writeDiffArtifact(ownerId, pageKey, computation.diffImage());
            VisualDiffStatus status = computation.diffPercent() <= threshold ? VisualDiffStatus.MATCH : VisualDiffStatus.DIFF;
            return new VisualDiffResult(status, computation.diffPercent(), diffImagePath);
        } catch (IOException ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Unable to compare visual baseline", ex);
        }
    }

    private DiffComputation computePixelDiff(BufferedImage baselineImage, BufferedImage currentImage) {
        int width = Math.max(baselineImage.getWidth(), currentImage.getWidth());
        int height = Math.max(baselineImage.getHeight(), currentImage.getHeight());
        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int differingPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int baselineRgb = samplePixel(baselineImage, x, y);
                int currentRgb = samplePixel(currentImage, x, y);
                if (baselineRgb != currentRgb) {
                    differingPixels++;
                    diffImage.setRGB(x, y, new Color(255, 59, 48, 220).getRGB());
                } else {
                    diffImage.setRGB(x, y, currentRgb);
                }
            }
        }

        double totalPixels = (double) width * height;
        double diffPercent = totalPixels == 0 ? 0.0 : (differingPixels * 100.0) / totalPixels;
        return new DiffComputation(diffPercent, diffImage);
    }

    private int samplePixel(BufferedImage image, int x, int y) {
        if (x >= image.getWidth() || y >= image.getHeight()) {
            return 0;
        }
        return image.getRGB(x, y);
    }

    private BufferedImage decodeImage(String data) {
        try {
            byte[] bytes = decodeBase64(data);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid PNG image payload");
            }
            return image;
        } catch (IOException ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Unable to decode screenshot image", ex);
        }
    }

    private byte[] decodeBase64(String data) {
        String normalized = data;
        if (normalized.contains(",")) {
            normalized = normalized.substring(normalized.indexOf(',') + 1);
        }
        return Base64.getDecoder().decode(normalized);
    }

    private String fingerprint(BufferedImage image) {
        long checksum = 17L;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                checksum = 31L * checksum + image.getRGB(x, y);
            }
        }
        return Long.toHexString(checksum);
    }

    private String writeImageArtifact(UUID ownerId, String pageKey, String prefix, BufferedImage image) {
        try {
            Path directory = Paths.get(storageDir, ownerId.toString(), sanitize(pageKey));
            Files.createDirectories(directory);
            Path file = directory.resolve(prefix + "-" + System.currentTimeMillis() + ".png");
            ImageIO.write(image, "png", file.toFile());
            return file.toAbsolutePath().toString();
        } catch (IOException ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store screenshot artifact", ex);
        }
    }

    private String writeDiffArtifact(UUID ownerId, String pageKey, BufferedImage image) {
        return writeImageArtifact(ownerId, pageKey, "diff", image);
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private record DiffComputation(double diffPercent, BufferedImage diffImage) {
    }

    private record VisualDiffResult(VisualDiffStatus status, double diffPercent, String diffImagePath) {
    }
}
