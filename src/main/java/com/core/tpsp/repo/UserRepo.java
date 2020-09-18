package com.core.tpsp.repo;

import com.core.tpsp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

    List<User> findByIdIn(Set<String> ids);

}
