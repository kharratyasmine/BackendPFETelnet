package com.workpilot.service.GestionRessources.teamMember;

import com.workpilot.dto.GestionRessources.*;
import com.workpilot.entity.ressources.WorkEntry;

import java.security.Principal;
import java.util.List;

public interface TeamMemberService {

    // Récupérer tous les membres d'équipe
    List<TeamMemberDTO> getAllTeamMembers();

    // Récupérer un membre par son ID
    TeamMemberDTO getTeamMemberById(Long id);

    // Créer un nouveau membre
    TeamMemberDTO createTeamMember(TeamMemberDTO teamMemberDTO);

    // Mettre à jour un membre existant
    TeamMemberDTO updateTeamMember(Long id, TeamMemberDTO teamMemberDTO);

    // Supprimer un membre
    void deleteTeamMember(Long id);

    // Déplacer un membre d'une équipe à une autre
    TeamMemberDTO moveTeamMember(Long teamMemberId, Long newTeamId);

    List<TeamMemberDTO> getMembersByProjectId(Long projectId);

    List<TeamMemberDTO> getTeamMembersByTeamId(Long teamId);

    // Récupérer un membre par son email
    TeamMemberDTO getTeamMemberByEmail(String email);

    // Récupérer les allocations d'un membre
    List<TeamMemberAllocationDTO> getMemberAllocations(Long memberId);

    // Récupérer les projets d'un membre
    List<ProjectDTO> getMemberProjects(Long memberId);

    // Récupérer l'équipe d'un membre
    TeamDTO getMemberTeam(Long memberId);

    // Récupérer les collègues d'un membre
    List<TeamMemberDTO> getMemberColleagues(Long memberId);

    // Récupérer les tâches d'un membre
    List<ProjectTaskDTO> getMemberTasks(Long memberId);

    // Récupérer les entrées de travail d'un membre
    List<WorkEntry> getMemberWorkEntries(Long memberId);

    // Ajouter une entrée de travail
      WorkEntry addWorkEntry(WorkEntry workEntry, Principal connectedUser);

    // Mettre à jour une entrée de travail
    WorkEntry updateWorkEntry(Long memberId, Long workEntryId, WorkEntry workEntry);

    // Générer les initiales selon le nouveau format (première lettre du prénom + deux premières lettres du nom)
    String generateInitials(String firstname, String lastname);

}