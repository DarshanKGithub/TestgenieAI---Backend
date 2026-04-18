package com.testgenieai.backend.repository;

import com.testgenieai.backend.domain.TestCaseResult;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseResultRepository extends JpaRepository<TestCaseResult, UUID> {

    List<TestCaseResult> findByTestRunId(UUID testRunId);
}
