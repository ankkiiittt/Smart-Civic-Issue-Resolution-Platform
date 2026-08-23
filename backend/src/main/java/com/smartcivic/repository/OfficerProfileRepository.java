package com.smartcivic.repository;

import com.smartcivic.entity.OfficerProfile;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OfficerProfileRepository extends JpaRepository<OfficerProfile, Long> {
    
    List<OfficerProfile> findByDepartmentIdAndIsAvailable(Long departmentId, boolean isAvailable);

    @Query(value = "SELECT * FROM officer_profiles WHERE is_available = true AND department_id = :departmentId AND ST_Contains(service_zone_geom, :point)", nativeQuery = true)
    List<OfficerProfile> findAvailableOfficersByDepartmentAndPoint(@Param("departmentId") Long departmentId, @Param("point") Point point);
    
    // Fallback: search across all departments for an officer whose service zone contains the point
    @Query(value = "SELECT * FROM officer_profiles WHERE is_available = true AND ST_Contains(service_zone_geom, :point)", nativeQuery = true)
    List<OfficerProfile> findAvailableOfficersContainingPoint(@Param("point") Point point);
}
