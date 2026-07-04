package com.junmo.certificatesystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junmo.certificatesystem.common.enums.RoleType;
import com.junmo.certificatesystem.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    List<User> findByRoleOrderByUserIdAsc(RoleType role);
}
