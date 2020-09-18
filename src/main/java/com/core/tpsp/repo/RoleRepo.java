package com.core.tpsp.repo;

import com.core.tpsp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Role, String> {

    Optional<Role> findByName(String name);

}
