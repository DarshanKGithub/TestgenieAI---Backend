package com.testgenieai.backend.domain;

import com.testgenieai.backend.domain.user.AppUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "test_runs")
public class TestRun {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(name = "suite_name", nullable = false)
    private String suiteName;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "total_tests", nullable = false)
    private int totalTests;

    @Column(name = "passed_tests", nullable = false)
    private int passedTests;

    @Column(name = "failed_tests", nullable = false)
    private int failedTests;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "triggered_by", nullable = false)
    @Builder.Default
    private String triggeredBy = "MANUAL";

    @Column(name = "ci_project")
    private String ciProject;

    @Column(name = "ci_branch")
    private String ciBranch;

    @Column(name = "ci_commit_sha")
    private String ciCommitSha;

    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TestCaseResult> testCases = new ArrayList<>();
}
