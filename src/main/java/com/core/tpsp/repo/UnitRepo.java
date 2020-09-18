package com.core.tpsp.repo;

import com.core.tpsp.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UnitRepo extends JpaRepository<Unit, Integer> {

    List<Unit> findByIdIn(Set<Integer> ids);
}
