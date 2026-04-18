package com.testgenieai.backend.domain;

import com.testgenieai.backend.domain.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "visual_comparisons")
public class VisualComparison {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id")
    private TestRun testRun;

    @Column(name = "page_key", nullable = false)
    private String pageKey;

    @Column(name = "baseline_hash")
    private String baselineHash;

    @Column(name = "baseline_image_path")
    private String baselineImagePath;

    @Column(name = "current_hash", nullable = false)
    private String currentHash;

    @Column(name = "current_image_path", nullable = false)
    private String currentImagePath;

    @Column(name = "diff_image_path")
    private String diffImagePath;

    @Column(name = "diff_percent", nullable = false)
    private double diffPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisualDiffStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
