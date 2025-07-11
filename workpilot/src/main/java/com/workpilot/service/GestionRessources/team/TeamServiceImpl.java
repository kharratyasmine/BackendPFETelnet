package com.workpilot.service.GestionRessources.team;

import com.workpilot.dto.GestionRessources.TeamDTO;
import com.workpilot.dto.GestionRessources.TeamMemberDTO;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.ressources.*;
import com.workpilot.service.GestionRessources.PlannedWorkloadMember.PlannedWorkloadMemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamMemberAllocationRepository teamMemberAllocationRepository;

    @Autowired
    private PlannedWorkloadMemberService plannedWorkloadMemberService;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Override
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(team -> convertToDTO(team, null))
                .collect(Collectors.toList());
    }

    private Team convertToEntity(TeamDTO dto) {
        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        return team;
    }

    @Override
    public TeamDTO getTeamById(Long id) {
        return teamRepository.findById(id)
                .map(team -> convertToDTO(team, null))
                .orElse(null);
    }

    @Override
    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = convertToEntity(teamDTO);
        team = teamRepository.save(team);
        return convertToDTO(team, null);
    }

    @Override
    public TeamDTO updateTeam(Long id, TeamDTO teamDTO) {
        return teamRepository.findById(id)
                .map(existingTeam -> {
                    existingTeam.setName(teamDTO.getName());
                    if (teamDTO.getProjectIds() != null) {
                        Set<Project> projects = teamDTO.getProjectIds().stream()
                                .map(projectId -> projectRepository.findById(projectId)
                                        .orElseThrow(() -> new EntityNotFoundException("Projet introuvable : " + projectId)))
                                .collect(Collectors.toSet());
                        existingTeam.setProjects(projects);
                    }
                    Team updatedTeam = teamRepository.save(existingTeam);
                    return convertToDTO(updatedTeam, null);
                })
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée : " + id));
    }

    @Override
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée avec l'ID : " + id));

        if (team.getMembers() != null) {
            for (TeamMember member : team.getMembers()) {
                List<TeamMemberAllocation> allocations = teamMemberAllocationRepository
                        .findAllByTeamMemberIdAndTeamId(member.getId(), team.getId());

                for (TeamMemberAllocation allocation : allocations) {
                    // Supprimer aussi les workloads liés à cette allocation
                    plannedWorkloadMemberService.deleteWorkloadsByProjectAndMember(
                            allocation.getProject().getId(),
                            allocation.getTeamMember().getId()
                    );
                    teamMemberAllocationRepository.delete(allocation);
                }

                member.getTeams().remove(team);
                teamMemberRepository.save(member);
            }
        }

        if (team.getProjects() != null) {
            for (Project project : team.getProjects()) {
                project.getTeams().remove(team);
                projectRepository.save(project);
            }
        }

        teamRepository.delete(team);
    }

    @Override
    public TeamDTO addMemberToTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);

        if (team != null && member != null) {
            member.getTeams().add(team);
            team.getMembers().add(member);
            teamMemberRepository.save(member);
            teamRepository.save(team);
            return convertToDTO(team, null);
        }
        return null;
    }

    @Override
    @Transactional
    public void removeMemberFromTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        // 1. Suppression des tâches assignées
        for (Project project : team.getProjects()) {
            List<TaskAssignment> taskAssignments = taskAssignmentRepository.findByTeamMemberId(memberId);
            for (TaskAssignment taskAssignment : taskAssignments) {
                if (taskAssignment.getTask().getProject().equals(project)) {
                    taskAssignmentRepository.delete(taskAssignment);
                }
            }
        }

        // 2. Suppression des allocations et des planned workloads
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository
                .findAllByTeamMemberIdAndTeamId(memberId, teamId);

        for (TeamMemberAllocation allocation : allocations) {
            // Supprimer les planned workloads pour ce projet
            plannedWorkloadMemberService.deleteWorkloadsByProjectAndMember(
                    allocation.getProject().getId(),
                    memberId
            );
        }
        teamMemberAllocationRepository.deleteAll(allocations);

        // 3. Mise à jour des relations
        team.getMembers().remove(member);
        member.getTeams().remove(team);

        teamRepository.save(team);
        teamMemberRepository.save(member);
    }
    @Override
    public List<TeamMemberDTO> getMembersOfTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Équipe introuvable avec l'id : " + teamId));

        // ✅ Ne récupérer que les membres réels de l'équipe, sans ajouter les fakes des demandes
        return team.getMembers().stream()
                .map(member -> convertToMemberDTO(member, null, team))
                .collect(Collectors.toList());
    }

    private double estimateCostByRole(Seniority role) {
        return switch (role) {
            case JUNIOR_ENGINEER -> 200;
            case INTERMEDIATE_ENGINEER -> 350;
            case SENIOR_ENGINEER -> 500;
            case SENIOR_MANAGER -> 800;
        };
    }

    @Override
    public TeamDTO addProjectToTeam(Long teamId, Long projectId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        Project project = projectRepository.findById(projectId).orElse(null);

        if (team != null && project != null) {
            team.getProjects().add(project);
            project.getTeams().add(team);
            projectRepository.save(project);
            teamRepository.save(team);
            return convertToDTO(team, projectId);
        }
        return null;
    }

    private TeamDTO convertToDTO(Team team, Long projectId) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());

        if (team.getProjects() != null) {
            dto.setProjectIds(
                    team.getProjects().stream()
                            .map(Project::getId)
                            .collect(Collectors.toSet())
            );
        }

        if (team.getMembers() != null) {
            dto.setMembers(
                    team.getMembers().stream()
                            .map(member -> convertToMemberDTO(member, projectId, team))
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private TeamMemberDTO convertToMemberDTO(TeamMember member, Long projectId, Team team) {
        Double allocation = teamMemberAllocationRepository
                .findAllByTeamMemberIdAndProjectIdAndTeamId(member.getId(), projectId, team.getId())
                .stream()
                .mapToDouble(TeamMemberAllocation::getAllocation)
                .sum();

        return new TeamMemberDTO(
                member.getId(),
                member.getName(),
                member.getInitial(),
                member.getJobTitle(),
                member.getHoliday(),
                member.getRole(),
                member.getCost(),
                member.getNote(),
                member.getImage(),
                member.getTeams().stream().map(Team::getId).collect(Collectors.toList()),
                member.getExperienceRange(),
                allocation,
                member.getEmail()
        );
    }

    @Override
    public List<TeamMemberDTO> getAvailableMembers(Long teamId) {
        try {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found"));

            List<TeamMember> allMembers = teamMemberRepository.findAll();
            List<TeamMember> assignedMembers = new ArrayList<>(team.getMembers());

            return allMembers.stream()
                    .filter(member -> !assignedMembers.contains(member))
                    .map(member -> convertToMemberDTO(member, null, team))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur dans getAvailableMembers : " + e.getMessage());
        }
    }

}
