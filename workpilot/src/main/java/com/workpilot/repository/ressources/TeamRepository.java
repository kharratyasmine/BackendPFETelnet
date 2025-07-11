package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    }