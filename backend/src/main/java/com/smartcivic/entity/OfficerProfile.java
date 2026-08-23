package com.smartcivic.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "officer_profiles")
public class OfficerProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "service_zone_geom", columnDefinition = "geometry(Geometry, 4326)")
    private Geometry serviceZoneGeom;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    public OfficerProfile() {}

    public OfficerProfile(Long userId, User user, Department department, Geometry serviceZoneGeom, boolean isAvailable) {
        this.userId = userId;
        this.user = user;
        this.department = department;
        this.serviceZoneGeom = serviceZoneGeom;
        this.isAvailable = isAvailable;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Geometry getServiceZoneGeom() { return serviceZoneGeom; }
    public void setServiceZoneGeom(Geometry serviceZoneGeom) { this.serviceZoneGeom = serviceZoneGeom; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public static OfficerProfileBuilder builder() {
        return new OfficerProfileBuilder();
    }

    public static class OfficerProfileBuilder {
        private Long userId;
        private User user;
        private Department department;
        private Geometry serviceZoneGeom;
        private boolean isAvailable;

        OfficerProfileBuilder() {}

        public OfficerProfileBuilder userId(Long userId) { this.userId = userId; return this; }
        public OfficerProfileBuilder user(User user) { this.user = user; return this; }
        public OfficerProfileBuilder department(Department department) { this.department = department; return this; }
        public OfficerProfileBuilder serviceZoneGeom(Geometry serviceZoneGeom) { this.serviceZoneGeom = serviceZoneGeom; return this; }
        public OfficerProfileBuilder isAvailable(boolean isAvailable) { this.isAvailable = isAvailable; return this; }

        public OfficerProfile build() {
            return new OfficerProfile(userId, user, department, serviceZoneGeom, isAvailable);
        }
    }
}
