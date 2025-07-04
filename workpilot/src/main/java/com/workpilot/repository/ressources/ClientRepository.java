package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{
        @Query("SELECT c FROM Client c LEFT JOIN FETCH c.projects")
        List<Client> findAllWithProjects ();
        public Optional<Client> findById(Long id);
}

