package com.core.tpsp.repo;

import com.core.tpsp.entity.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ClazzRepo extends JpaRepository<Clazz, Integer> {

    List<Clazz> findByIdIn(Set<Integer> ids);
}
