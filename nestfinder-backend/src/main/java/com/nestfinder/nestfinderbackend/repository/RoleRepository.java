package com.nestfinder.nestfinderbackend.repository;

import com.nestfinder.nestfinderbackend.model.ERole;
import com.nestfinder.nestfinderbackend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
