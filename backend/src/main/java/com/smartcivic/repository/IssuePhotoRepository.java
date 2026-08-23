package com.smartcivic.repository;

import com.smartcivic.entity.IssuePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssuePhotoRepository extends JpaRepository<IssuePhoto, Long> {
    List<IssuePhoto> findByIssueId(Long issueId);
}
