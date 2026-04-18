package com.testgenieai.backend.repository;

import com.testgenieai.backend.domain.TestRun;
import com.testgenieai.backend.domain.ExecutionStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRunRepository extends JpaRepository<TestRun, UUID> {

    Page<TestRun> findByOwnerIdOrderByStartedAtDesc(UUID ownerId, Pageable pageable);

    Page<TestRun> findByOwnerIdAndStatusOrderByStartedAtDesc(UUID ownerId, ExecutionStatus status, Pageable pageable);

    Page<TestRun> findByOwnerIdAndSuiteNameContainingIgnoreCaseOrderByStartedAtDesc(
            UUID ownerId,
            String suiteName,
            Pageable pageable
    );

    Page<TestRun> findByOwnerIdAndStatusAndSuiteNameContainingIgnoreCaseOrderByStartedAtDesc(
            UUID ownerId,
            ExecutionStatus status,
            String suiteName,
            Pageable pageable
    );
}
