package com.testgenieai.backend.repository;

import com.testgenieai.backend.domain.VisualComparison;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisualComparisonRepository extends JpaRepository<VisualComparison, UUID> {

    List<VisualComparison> findTop20ByOwnerIdAndPageKeyOrderByCreatedAtDesc(UUID ownerId, String pageKey);

    List<VisualComparison> findTop20ByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
