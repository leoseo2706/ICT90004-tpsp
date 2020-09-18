package com.core.tpsp.repo;

import com.core.tpsp.entity.TutorPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TutorPreferenceRepo extends JpaRepository<TutorPreference, Integer> {

    List<TutorPreference> findByConvenorIdIn(Set<String> convenorIds);

}
