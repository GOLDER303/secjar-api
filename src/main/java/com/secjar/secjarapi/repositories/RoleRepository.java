package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<UserRole, Long> {
}
