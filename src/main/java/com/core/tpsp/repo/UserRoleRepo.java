package com.core.tpsp.repo;

import com.core.tpsp.entity.UserRole;
import com.core.tpsp.entity.UserRoleCompositeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepo extends JpaRepository<UserRole, UserRoleCompositeKey> {

    List<UserRole> findByUserRoleKeyRoleId(String roleId);

}
