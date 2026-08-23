package com.smartcivic.repository;

import com.smartcivic.entity.IssueUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IssueUpvoteRepository extends JpaRepository<IssueUpvote, Long> {
    Optional<IssueUpvote> findByIssueIdAndUserId(Long issueId, Long userId);
    boolean existsByIssueIdAndUserId(Long issueId, Long userId);
    long countByIssueId(Long issueId);
    void deleteByIssueId(Long issueId);
}
