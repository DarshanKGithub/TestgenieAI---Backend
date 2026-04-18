package com.testgenieai.backend.domain;

import com.testgenieai.backend.domain.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "visual_baselines")
public class VisualBaseline {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(name = "page_key", nullable = false)
    private String pageKey;

    @Column(name = "baseline_hash", nullable = false)
    private String baselineHash;

    @Column(name = "baseline_image_path")
    private String baselineImagePath;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
