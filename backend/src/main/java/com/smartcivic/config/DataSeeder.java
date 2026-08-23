package com.smartcivic.config;

import com.smartcivic.entity.*;
import com.smartcivic.repository.CategoryRepository;
import com.smartcivic.repository.DepartmentRepository;
import com.smartcivic.repository.OfficerProfileRepository;
import com.smartcivic.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public DataSeeder(UserRepository userRepository,
                      DepartmentRepository departmentRepository,
                      CategoryRepository categoryRepository,
                      OfficerProfileRepository officerProfileRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping seeder.");
            return;
        }

        log.info("Seeding database with default civic data...");

        // 1. Create Departments
        Department roads = Department.builder().name("Roads & Transport").description("Maintenance of public roads, traffic lights, and pavements.").build();
        Department electricity = Department.builder().name("Electricity & Power").description("Maintenance of streetlights, cables, and power substations.").build();
        Department sanitation = Department.builder().name("Sanitation").description("Waste collection, illegal dumping removal, and street cleaning.").build();
        Department water = Department.builder().name("Water & Sewage").description("Burst main water pipes, leaks, and sewer blockages.").build();

        roads = departmentRepository.save(roads);
        electricity = departmentRepository.save(electricity);
        sanitation = departmentRepository.save(sanitation);
        water = departmentRepository.save(water);

        // 2. Create Categories
        Category pothole = Category.builder().name("Pothole Repair").description("A hole in a road surface.").defaultSlaHours(48).department(roads).build();
        Category streetlight = Category.builder().name("Streetlight Outage").description("Public streetlight not working.").defaultSlaHours(24).department(electricity).build();
        Category garbage = Category.builder().name("Garbage Overflow").description("Public garbage bin overflowing with waste.").defaultSlaHours(12).department(sanitation).build();
        Category pipeBurst = Category.builder().name("Burst Water Pipe").description("Water gushing onto pavement or street.").defaultSlaHours(8).department(water).build();
        Category illegalDumping = Category.builder().name("Illegal Dumping").description("Unauthorised waste dumping in public space.").defaultSlaHours(72).department(sanitation).build();

        categoryRepository.save(pothole);
        categoryRepository.save(streetlight);
        categoryRepository.save(garbage);
        categoryRepository.save(pipeBurst);
        categoryRepository.save(illegalDumping);

        // 3. Create Users
        User adminUser = User.builder()
                .username("admin")
                .email("admin@smartcivic.gov")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .firstName("System")
                .lastName("Administrator")
                .phoneNumber("0000000000")
                .build();

        User officerSanitation = User.builder()
                .username("officer_sanitation")
                .email("officer.sanitation@smartcivic.gov")
                .passwordHash(passwordEncoder.encode("officer123"))
                .role(UserRole.OFFICER)
                .firstName("John")
                .lastName("Sanitation")
                .phoneNumber("1111111111")
                .build();

        User officerWater = User.builder()
                .username("officer_water")
                .email("officer.water@smartcivic.gov")
                .passwordHash(passwordEncoder.encode("officer123"))
                .role(UserRole.OFFICER)
                .firstName("Sarah")
                .lastName("Hydrant")
                .phoneNumber("2222222222")
                .build();

        User citizenUser = User.builder()
                .username("citizen_test")
                .email("citizen@example.com")
                .passwordHash(passwordEncoder.encode("citizen123"))
                .role(UserRole.CITIZEN)
                .firstName("Jane")
                .lastName("Doe")
                .phoneNumber("3333333333")
                .build();

        userRepository.save(adminUser);
        userRepository.save(officerSanitation);
        userRepository.save(officerWater);
        userRepository.save(citizenUser);

        // 4. Create Officer Profiles with service zone polygon coordinates (covering Manhattan and adjacent regions for testing)
        // Closed loop ring (first and last coordinate are identical)
        Coordinate[] zoneCoords1 = new Coordinate[] {
            new Coordinate(-74.05, 40.70),
            new Coordinate(-74.05, 40.85),
            new Coordinate(-73.90, 40.85),
            new Coordinate(-73.90, 40.70),
            new Coordinate(-74.05, 40.70)
        };
        Polygon zone1 = geometryFactory.createPolygon(zoneCoords1);
        zone1.setSRID(4326);

        OfficerProfile profileSanitation = OfficerProfile.builder()
                .user(officerSanitation)
                .department(sanitation)
                .serviceZoneGeom(zone1)
                .isAvailable(true)
                .build();
        officerProfileRepository.save(profileSanitation);

        Coordinate[] zoneCoords2 = new Coordinate[] {
            new Coordinate(-74.10, 40.65),
            new Coordinate(-74.10, 40.80),
            new Coordinate(-73.85, 40.80),
            new Coordinate(-73.85, 40.65),
            new Coordinate(-74.10, 40.65)
        };
        Polygon zone2 = geometryFactory.createPolygon(zoneCoords2);
        zone2.setSRID(4326);

        OfficerProfile profileWater = OfficerProfile.builder()
                .user(officerWater)
                .department(water)
                .serviceZoneGeom(zone2)
                .isAvailable(true)
                .build();
        officerProfileRepository.save(profileWater);

        log.info("Database seeding completed successfully.");
    }
}
