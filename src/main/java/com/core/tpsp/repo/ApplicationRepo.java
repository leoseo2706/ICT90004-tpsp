package com.core.tpsp.repo;

import com.core.tpsp.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepo extends JpaRepository<Application, Integer> {

    List<Application> findByApproved(Boolean approved);

    List<Application> findAllByOrderByApplicationIdDesc();

}
