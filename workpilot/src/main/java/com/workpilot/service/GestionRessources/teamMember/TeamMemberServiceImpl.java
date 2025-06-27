package com.workpilot.service.GestionRessources.teamMember;

import com.workpilot.dto.GestionRessources.*;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.ressources.TeamMemberAllocationRepository;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.repository.ressources.TeamRepository;
import com.workpilot.service.GestionRessources.TeamMemberHistory.TeamMemberHistoryService;
import com.workpilot.service.GestionRessources.WorkEntry.WorkEntryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberAllocationRepository teamMemberAllocationRepository;

    @Autowired
    private TeamMemberHistoryService historyService;

    @Autowired
    private WorkEntryService workEntryService;

    @Override
    public List<TeamMemberDTO> getAllTeamMembers() {
        List<TeamMember> allMembers = teamMemberRepository.findAll();

        // ✅ Définir les noms standards des membres "fake"
        List<String> standardFakeNames = new ArrayList<>();
        for (Seniority role : Seniority.values()) {
            standardFakeNames.add("Inconnu (" + role.name() + ")");
        }

        // ✅ Filtrer pour ne garder que les vrais membres et les fakes standards
        List<TeamMember> filteredMembers = allMembers.stream()
                .filter(member -> !member.isFake() || standardFakeNames.contains(member.getName()))
                .collect(Collectors.toList());

        return filteredMembers.stream()
                .map(this::convertToDTO)
                .sorted(Comparator.comparing(TeamMemberDTO::getName))
                .collect(Collectors.toList());
    }

    @Override
    public TeamMemberDTO getTeamMemberById(Long id) {
        return teamMemberRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public TeamMemberDTO createTeamMember(TeamMemberDTO teamMemberDTO) {
        try {
            TeamMember teamMember = convertToEntity(teamMemberDTO);
            // ✅ VALIDATIONS
            if (teamMember.getName() == null || teamMember.getName().trim().isEmpty() || teamMember.getName().equalsIgnoreCase("Inconnu")) {
                throw new IllegalArgumentException("Le nom du membre est invalide.");
            }

            if (teamMember.getInitial() == null || teamMember.getInitial().trim().isEmpty()) {
                throw new IllegalArgumentException("Les initiales sont obligatoires.");
            }

            if (teamMember.getStartDate() == null) {
                throw new IllegalArgumentException("La date de début est obligatoire.");
            }

            // 🔥 Suggérer un rôle automatiquement si aucun n'est précisé
            if (teamMember.getStartDate() != null && teamMemberDTO.getRole() == null) {
                double exp = getYearsFromStartDate(teamMember.getStartDate());
                teamMember.setRole(suggestRole(exp));
            }
            teamMember.setStatus(calculateStatus(teamMember.getEndDate()));

            teamMember = teamMemberRepository.save(teamMember);
            return convertToDTO(teamMember);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du membre : " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public TeamMemberDTO updateTeamMember(Long id, TeamMemberDTO teamMemberDTO) {
        return teamMemberRepository.findById(id)
                .map(existingMember -> {
                    TeamMember updatedMember = convertToEntity(teamMemberDTO);
                    updatedMember.setId(existingMember.getId());

                    // Sauvegarder l'historique pour chaque champ modifié
                    if (!existingMember.getName().equals(updatedMember.getName())) {
                        historyService.saveHistoryForField(id, "name", existingMember.getName(), updatedMember.getName(), "system");
                    }
                    if (!existingMember.getInitial().equals(updatedMember.getInitial())) {
                        historyService.saveHistoryForField(id, "initial", existingMember.getInitial(), updatedMember.getInitial(), "system");
                    }
                    if (!existingMember.getJobTitle().equals(updatedMember.getJobTitle())) {
                        historyService.saveHistoryForField(id, "jobTitle", existingMember.getJobTitle(), updatedMember.getJobTitle(), "system");
                    }
                    if (!existingMember.getRole().equals(updatedMember.getRole())) {
                        historyService.saveHistoryForField(id, "role", existingMember.getRole().toString(), updatedMember.getRole().toString(), "system");
                    }
                    if (!Objects.equals(existingMember.getCost(), updatedMember.getCost())) {
                        historyService.saveHistoryForField(id, "cost",
                                String.valueOf(existingMember.getCost()),
                                String.valueOf(updatedMember.getCost()),
                                "system");
                    }
                    if (!Objects.equals(existingMember.getStartDate(), updatedMember.getStartDate())) {
                        historyService.saveHistoryForField(id, "startDate",
                                existingMember.getStartDate() != null ? existingMember.getStartDate().toString() : "",
                                updatedMember.getStartDate() != null ? updatedMember.getStartDate().toString() : "",
                                "system");
                    }
                    if (!Objects.equals(existingMember.getEndDate(), updatedMember.getEndDate())) {
                        historyService.saveHistoryForField(id, "endDate",
                                existingMember.getEndDate() != null ? existingMember.getEndDate().toString() : "",
                                updatedMember.getEndDate() != null ? updatedMember.getEndDate().toString() : "",
                                "system");
                    }
                    if (!Objects.equals(existingMember.getNote(), updatedMember.getNote())) {
                        historyService.saveHistoryForField(id, "note",
                                existingMember.getNote() != null ? existingMember.getNote() : "",
                                updatedMember.getNote() != null ? updatedMember.getNote() : "",
                                "system");
                    }

                    // 🔥 Suggérer un rôle si pas fourni et startDate connue
                    if (updatedMember.getStartDate() != null && teamMemberDTO.getRole() == null) {
                        double exp = getYearsFromStartDate(updatedMember.getStartDate());
                        Seniority suggestedRole = suggestRole(exp);
                        if (!suggestedRole.equals(existingMember.getRole())) {
                            historyService.saveHistoryForField(
                                    id,
                                    "role",
                                    existingMember.getRole().toString(),
                                    suggestedRole.toString(),
                                    "system"
                            );
                        }
                        updatedMember.setRole(suggestedRole);
                    }

                    // Mettre à jour le statut
                    String newStatus = calculateStatus(updatedMember.getEndDate());
                    if (!newStatus.equals(existingMember.getStatus())) {
                        historyService.saveHistoryForField(
                                id,
                                "status",
                                existingMember.getStatus() != null ? existingMember.getStatus() : "",
                                newStatus,
                                "system"
                        );
                    }
                    updatedMember.setStatus(newStatus);

                    // Mettre à jour l'expérience
                    String newExperienceRange = getExperienceRange(updatedMember.getStartDate());
                    if (!newExperienceRange.equals(existingMember.getExperienceRange())) {
                        historyService.saveHistoryForField(
                                id,
                                "experienceRange",
                                existingMember.getExperienceRange() != null ? existingMember.getExperienceRange() : "",
                                newExperienceRange,
                                "system"
                        );
                    }
                    updatedMember.setExperienceRange(newExperienceRange);

                    // Sauvegarder les modifications
                    updatedMember = teamMemberRepository.save(updatedMember);
                    return convertToDTO(updatedMember);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public void deleteTeamMember(Long id) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found with id: " + id));

        // ✅ Supprimer l'historique du membre
        historyService.deleteHistoryByTeamMemberId(id);

        // ✅ Supprimer les allocations du membre
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findByTeamMemberId(id);
        teamMemberAllocationRepository.deleteAll(allocations);

        // ✅ Supprimer le membre des équipes
        member.getTeams().clear();
        teamMemberRepository.save(member);

        // ✅ Supprimer le membre
        teamMemberRepository.deleteById(id);
    }

    @Override
    public TeamMemberDTO moveTeamMember(Long teamMemberId, Long newTeamId) {
        TeamMember member = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        Team newTeam = teamRepository.findById(newTeamId)
                .orElseThrow(() -> new RuntimeException("Nouvelle équipe non trouvée"));

        // Vérifier si le membre a des allocations dans des projets de l'ancienne équipe
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findByTeamMemberId(member.getId());
        if (!allocations.isEmpty()) {
            // Option 1 : Supprimer les allocations
            teamMemberAllocationRepository.deleteAll(allocations);

            // Option 2 : Transférer les allocations vers la nouvelle équipe (si les projets sont communs)
            for (TeamMemberAllocation allocation : allocations) {
                if (newTeam.getProjects().contains(allocation.getProject())) {
                    allocation.setTeamMember(member); // Pas besoin de changer le projet
                    teamMemberAllocationRepository.save(allocation);
                } else {
                    teamMemberAllocationRepository.delete(allocation);
                }
            }
        }

        teamMemberRepository.save(member);

        return convertToDTO(member);
    }

    private double getYearsFromStartDate(LocalDate startDate) {
        if (startDate == null) return 0;
        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        return Math.round((days / 365.25) * 10.0) / 10.0;
    }


    private TeamMemberDTO convertToDTO(TeamMember teamMember) {
        TeamMemberDTO dto = new TeamMemberDTO();
        dto.setId(teamMember.getId());
        dto.setName(teamMember.getName());
        dto.setInitial(teamMember.getInitial());
        dto.setJobTitle(teamMember.getJobTitle());
        dto.setHoliday(teamMember.getHoliday());
        dto.setRole(teamMember.getRole());
        dto.setCost(teamMember.getCost());
        dto.setNote(teamMember.getNote());
        dto.setImage(teamMember.getImage());
        dto.setExperienceRange(getExperienceRange(teamMember.getStartDate()));
        dto.setStartDate(teamMember.getStartDate());
        dto.setEndDate(teamMember.getEndDate());
        dto.setEmail(teamMember.getEmail());
        dto.setTeams(
                teamMember.getTeams().stream()
                        .map(Team::getId)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private TeamMember convertToEntity(TeamMemberDTO dto) {
        TeamMember teamMember = new TeamMember();
        teamMember.setId(dto.getId());
        teamMember.setName(dto.getName());
        teamMember.setInitial(dto.getInitial());
        teamMember.setJobTitle(dto.getJobTitle());
        teamMember.setHoliday(dto.getHoliday());
        teamMember.setRole(dto.getRole());
        teamMember.setCost(dto.getCost());
        teamMember.setNote(dto.getNote());
        teamMember.setImage(dto.getImage());
        teamMember.setStartDate(dto.getStartDate());
        teamMember.setEndDate(dto.getEndDate());
        teamMember.setStatus(calculateStatus(dto.getEndDate()));
        teamMember.setExperienceRange(getExperienceRange(teamMember.getStartDate()));
        teamMember.setEmail(dto.getEmail());
        // ✅ Gérer le champ fake
        boolean isFake = dto.isFake(); // assure-toi que ton DTO a bien un boolean getFake()
        teamMember.setFake(isFake);
        if (isFake) {
            // ⚠️ Un seul membre fake autorisé par rôle
            teamMemberRepository.findByRoleAndFake(String.valueOf(dto.getRole()), true).ifPresent(existing -> {
                if (dto.getId() == null || !existing.getId().equals(dto.getId())) {
                    throw new RuntimeException("Un membre fake existe déjà pour le rôle " + dto.getRole());
                }
            });
        }

        // ✅ Associer aux équipes si besoin
        if (dto.getTeams() != null) {
            Set<Team> teams = dto.getTeams().stream()
                    .map(id -> teamRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Équipe non trouvée")))
                    .collect(Collectors.toSet());
            teamMember.setTeams(teams);
        }

        return teamMember;
    }


    private String calculateStatus(LocalDate endDate) {
        if (endDate == null || endDate.isAfter(LocalDate.now())) {
            return "En poste"; // ✅ Encore en activité
        } else {
            return "Inactif"; // ❌ Fin de contrat dépassée
        }
    }

    @Override
    public List<TeamMemberDTO> getTeamMembersByTeamId(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamMemberDTO> getMembersByProjectId(Long projectId) {
        return teamMemberRepository.findMembersByProjectId(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String getExperienceRange(LocalDate startDate) {
        if (startDate == null) return "Inconnu";

        LocalDate now = LocalDate.now();
        long years = ChronoUnit.YEARS.between(startDate, now);

        if (years == 0) return "0 - 1 Year";
        return years + " - " + (years + 1) + " Years";
    }

    private Seniority suggestRole(double experience) {
        if (experience <= 2) {
            return Seniority.JUNIOR_ENGINEER;
        } else if (experience <= 6) {
            return Seniority.INTERMEDIATE_ENGINEER;
        } else if (experience <= 12) {
            return Seniority.SENIOR_ENGINEER;
        } else {
            return Seniority.SENIOR_MANAGER;
        }
    }

    @Override
    public TeamMemberDTO getTeamMemberByEmail(String email) {
        return teamMemberRepository.findByEmail(email)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found with email: " + email));
    }

    @Override
    public List<TeamMemberAllocationDTO> getMemberAllocations(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));
        
        return member.getAllocations().stream()
                .map(allocation -> {
                    TeamMemberAllocationDTO dto = new TeamMemberAllocationDTO();
                    dto.setId(allocation.getId());
                    dto.setMemberId(memberId);
                    dto.setProjectId(allocation.getProject().getId());
                    dto.setTeamId(allocation.getTeam().getId());
                    dto.setAllocation(allocation.getAllocation());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getMemberProjects(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));

        return member.getAllocations().stream()
                .map(allocation -> {
                    Project project = allocation.getProject();
                    ProjectDTO dto = new ProjectDTO();
                    dto.setId(project.getId());
                    dto.setName(project.getName());
                    dto.setProjectType(project.getProjectType());
                    dto.setDescription(project.getDescription());
                    dto.setStartDate(project.getStartDate());
                    dto.setEndDate(project.getEndDate());
                    dto.setActivity(project.getActivity());
                    dto.setTechnologie(project.getTechnologie());
                    dto.setStatus(project.getStatus());

                    // Associations
                    if (project.getClient() != null) {
                        dto.setClientId(project.getClient().getId());
                    }

                    if (project.getUser() != null) {
                        dto.setUserId(project.getUser().getId());
                        dto.setUserName(project.getUser().getFirstname() + " " + project.getUser().getLastname());
                    }

                    return dto;
                })
                .distinct()
                .collect(Collectors.toList());
    }


    @Override
    public TeamDTO getMemberTeam(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));
        
        if (member.getTeams().isEmpty()) {
            throw new EntityNotFoundException("TeamMember has no team assigned");
        }
        
        Team team = member.getTeams().iterator().next();
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setProjectIds(team.getProjects().stream()
                .map(Project::getId)
                .collect(Collectors.toSet()));
        return dto;
    }

    @Override
    public List<TeamMemberDTO> getMemberColleagues(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));
        
        if (member.getTeams().isEmpty()) {
            return new ArrayList<>();
        }
        
        Team team = member.getTeams().iterator().next();
        return team.getMembers().stream()
                .filter(colleague -> !colleague.getId().equals(memberId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectTaskDTO> getMemberTasks(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));

        return member.getAllocations().stream()
                .flatMap(allocation -> allocation.getProject().getTasks().stream())
                .filter(task -> task.getAssignments().stream()
                        .anyMatch(assignment -> assignment.getTeamMember().getId().equals(memberId)))
                .map(task -> {
                    ProjectTaskDTO dto = new ProjectTaskDTO();
                    dto.setId(task.getId());
                    dto.setName(task.getName());
                    dto.setDescription(task.getDescription());
                    dto.setDateDebut(task.getDateDebut());
                    dto.setDateFin(task.getDateFin());
                    dto.setStatus(task.getStatus());
                    dto.setProjectId(task.getProject().getId());
                    dto.setProjectName(task.getProject().getName());
                    dto.setAssignments(
                            task.getAssignments().stream()
                            .filter(assignment -> assignment.getTeamMember().getId().equals(memberId))
                                    .map(assignment -> {
                                        TaskAssignmentDTO assignmentDTO = new TaskAssignmentDTO();
                                        assignmentDTO.setId(assignment.getId());
                                        assignmentDTO.setTeamMemberId(assignment.getTeamMember().getId());
                                        assignmentDTO.setTeamMemberName(assignment.getTeamMember().getName());
                                        assignmentDTO.setProgress(assignment.getProgress());
                                        assignmentDTO.setWorkedMD(assignment.getWorkedMD());
                                        assignmentDTO.setEstimatedMD(assignment.getEstimatedMD());
                                        assignmentDTO.setRemainingMD(assignment.getRemainingMD());
                                        assignmentDTO.setEstimatedStartDate(assignment.getEstimatedStartDate());
                                        assignmentDTO.setEstimatedEndDate(assignment.getEstimatedEndDate());
                                // Calculer les dates effectives à partir des WorkEntry du membre sur cette tâche
                                List<WorkEntry> entries = workEntryService.getWorkEntriesByMemberAndTask(memberId, task.getId());
                                if (!entries.isEmpty()) {
                                    LocalDate min = entries.stream().map(WorkEntry::getDate).min(LocalDate::compareTo).orElse(null);
                                    LocalDate max = entries.stream().map(WorkEntry::getDate).max(LocalDate::compareTo).orElse(null);
                                    assignmentDTO.setEffectiveStartDate(min);
                                    assignmentDTO.setEffectiveEndDate(max);
                                } else {
                                    assignmentDTO.setEffectiveStartDate(null);
                                    assignmentDTO.setEffectiveEndDate(null);
                                }
                                        return assignmentDTO;
                                    })
                                    .collect(Collectors.toList())
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkEntry> getMemberWorkEntries(Long memberId) {
        return workEntryService.getWorkEntriesByMember(memberId);
    }

    @Override
    public WorkEntry addWorkEntry(WorkEntry workEntry, Principal connectedUser) {
        // 1. Récupérer l'email de l'utilisateur connecté
        String email = connectedUser.getName();

        // 2. Retrouver le TeamMember par email
        TeamMember member = teamMemberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Si aucun TeamMember trouvé, créer un nouveau basé sur l'utilisateur connecté
                    try {
                        // Récupérer les informations de l'utilisateur depuis le Principal
                        var user = (com.workpilot.entity.auth.User) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
                        
                        TeamMember newMember = new TeamMember();
                        newMember.setName(user.getFirstname() + " " + user.getLastname());
                        newMember.setInitial(generateInitials(user.getFirstname(), user.getLastname()));
                        newMember.setEmail(user.getEmail());
                        newMember.setJobTitle("Ingénieur"); // Valeur par défaut
                        newMember.setRole(Seniority.INTERMEDIATE_ENGINEER); // Valeur par défaut
                        newMember.setFake(false);
                        newMember.setCost(350.0); // Coût par défaut pour INTERMEDIATE_ENGINEER
                        newMember.setStartDate(java.time.LocalDate.now());
                        newMember.setStatus("En poste");
                        newMember.setExperienceRange("0-2 ans");
                        newMember.setImage(user.getPhotoUrl() != null ? user.getPhotoUrl() : "assets/img/profiles/default-avatar.jpg");
                        newMember.setHoliday(new ArrayList<>());
                        newMember.setTeams(new HashSet<>());
                        newMember.setAllocations(new HashSet<>());
                        
                        return teamMemberRepository.save(newMember);
                    } catch (Exception e) {
                        throw new RuntimeException("Impossible de créer un TeamMember pour l'utilisateur : " + email, e);
                    }
                });

        // 3. Injecter le bon memberId dans l'objet WorkEntry
        workEntry.setMemberId(member.getId());

        // 4. Appel au service d'enregistrement
        return workEntryService.createWorkEntry(workEntry);
    }




    @Override
    public WorkEntry updateWorkEntry(Long memberId, Long workEntryId, WorkEntry workEntry) {
        // Vérifier que le membre existe
        teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));

        // Vérifier que l'entrée existe et appartient au membre
        WorkEntry existingEntry = workEntryService.getWorkEntryById(workEntryId);
        if (!existingEntry.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("WorkEntry does not belong to the specified member");
        }

        // Mettre à jour l'entrée
        workEntry.setId(workEntryId);
        workEntry.setMemberId(memberId);
        
        return workEntryService.updateWorkEntry(workEntryId, workEntry);
    }

    /**
     * Génère les initiales selon le format : première lettre du prénom + deux premières lettres du nom
     * Exemples : 
     * - "Jean Dupont" → "JDU"
     * - "Marie Martin" → "MMA"
     * - "Pierre" → "P" (si nom vide)
     * - "Dupont" → "DU" (si prénom vide)
     */
    public String generateInitials(String firstname, String lastname) {
        if (firstname == null || firstname.trim().isEmpty() || lastname == null || lastname.trim().isEmpty()) {
            return "";
        }
        String firstInitial = firstname.trim().substring(0, 1).toUpperCase();
        String lastInitials = lastname.trim().substring(0, Math.min(2, lastname.trim().length())).toUpperCase();
        return firstInitial + lastInitials;
    }

}


