package com.smartcivic.repository;

import com.smartcivic.entity.Issue;
import com.smartcivic.entity.IssueStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    Page<Issue> findByStatus(IssueStatus status, Pageable pageable);

    Page<Issue> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Issue> findByReporterId(Long reporterId, Pageable pageable);

    Page<Issue> findByAssignedOfficerId(Long officerId, Pageable pageable);

    @Query(value = "SELECT * FROM issues i WHERE ST_DWithin(i.location_geom::geography, :point::geography, :radiusInMeters) ORDER BY i.created_at DESC", nativeQuery = true)
    List<Issue> findNearbyIssues(@Param("point") Point point, @Param("radiusInMeters") double radiusInMeters);

    @Query("SELECT i FROM Issue i WHERE i.status != 'RESOLVED' AND i.slaDeadline < :now AND i.isEscalated = false")
    List<Issue> findUnresolvedOverdueIssues(@Param("now") LocalDateTime now);

    @Query("SELECT i.category.name, COUNT(i) FROM Issue i GROUP BY i.category.name")
    List<Object[]> countIssuesByCategory();

    @Query("SELECT i.status, COUNT(i) FROM Issue i GROUP BY i.status")
    List<Object[]> countIssuesByStatus();

    // Average resolution time in hours per category
    @Query(value = "SELECT c.name, AVG(EXTRACT(EPOCH FROM (i.resolved_at - i.created_at)) / 3600) " +
                   "FROM issues i JOIN categories c ON i.category_id = c.id " +
                   "WHERE i.status = 'RESOLVED' AND i.resolved_at IS NOT NULL " +
                   "GROUP BY c.name", nativeQuery = true)
    List<Object[]> getAverageResolutionTimeByCategory();

    // SLA breach rate: count breached (resolved after deadline or still unresolved and past deadline) vs total
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN (i.status = 'RESOLVED' AND i.resolved_at > i.sla_deadline) OR (i.status != 'RESOLVED' AND i.sla_deadline < NOW()) THEN 1 ELSE 0 END) * 100.0 / COUNT(i) " +
                   "FROM issues i", nativeQuery = true)
    Double getSlaBreachRate();

    // SLA Breach counts: total breached vs total resolved
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN (i.status = 'RESOLVED' AND i.resolved_at > i.sla_deadline) OR (i.status != 'RESOLVED' AND i.sla_deadline < NOW()) THEN 1 ELSE 0 END) as breached, " +
                   "COUNT(i) as total " +
                   "FROM issues i", nativeQuery = true)
    List<Object[]> getSlaComplianceStats();

    // Weekly volume trends: week (start date) vs issue count
    @Query(value = "SELECT DATE_TRUNC('week', i.created_at) as week_date, COUNT(i) " +
                   "FROM issues i " +
                   "GROUP BY week_date ORDER BY week_date ASC", nativeQuery = true)
    List<Object[]> getWeeklyIssueVolumeTrends();
}
