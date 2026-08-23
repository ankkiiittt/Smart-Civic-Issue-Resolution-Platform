package com.smartcivic.config;

import com.smartcivic.entity.Category;
import com.smartcivic.entity.Department;
import com.smartcivic.entity.OfficerProfile;
import com.smartcivic.entity.User;
import com.smartcivic.repository.CategoryRepository;
import com.smartcivic.repository.DepartmentRepository;
import com.smartcivic.repository.OfficerProfileRepository;
import com.smartcivic.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OfficerProfileRepository officerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    void run_DatabaseAlreadySeeded_SkipsSeeding() {
        when(userRepository.count()).thenReturn(5L);

        dataSeeder.run();

        verify(departmentRepository, never()).save(any(Department.class));
        verify(categoryRepository, never()).save(any(Category.class));
        verify(userRepository, never()).save(any(User.class));
        verify(officerProfileRepository, never()).save(any(OfficerProfile.class));
    }

    @Test
    void run_DatabaseEmpty_PerformsSeeding() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        
        // Mock save calls to return the object passed
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArguments()[0]);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(officerProfileRepository.save(any(OfficerProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        dataSeeder.run();

        // 4 departments, 5 categories, 4 users, 2 officer profiles
        verify(departmentRepository, times(4)).save(any(Department.class));
        verify(categoryRepository, times(5)).save(any(Category.class));
        verify(userRepository, times(4)).save(any(User.class));
        verify(officerProfileRepository, times(2)).save(any(OfficerProfile.class));
    }
}
