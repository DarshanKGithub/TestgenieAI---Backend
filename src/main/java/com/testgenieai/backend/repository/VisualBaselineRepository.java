package com.testgenieai.backend.repository;

import com.testgenieai.backend.domain.VisualBaseline;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisualBaselineRepository extends JpaRepository<VisualBaseline, UUID> {

    Optional<VisualBaseline> findByOwnerIdAndPageKey(UUID ownerId, String pageKey);
}
