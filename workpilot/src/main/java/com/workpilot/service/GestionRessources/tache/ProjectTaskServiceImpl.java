package com.workpilot.service.GestionRessources.tache;

import com.workpilot.dto.GestionRessources.ProjectTaskDTO;
import com.workpilot.dto.GestionRessources.TaskAssignmentDTO;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.Psr.TaskTrackerRepository;
import com.workpilot.repository.ressources.*;
import com.workpilot.service.PublicHolidayService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// ... imports ...

@Service
@RequiredArgsConstructor
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private final ProjectTaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskAssignmentRepository assignmentRepository;
    private final PublicHolidayService publicHolidayService;
    private final WorkEntryRepository workEntryRepository;
    private final TaskTrackerRepository taskTrackerRepository;


    @Override
    public ProjectTaskDTO createTache(ProjectTaskDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Projet introuvable"));

        ProjectTask task = new ProjectTask();
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setDateDebut(dto.getDateDebut());
        task.setDateFin(dto.getDateFin());
        task.setStatus(dto.getStatus());
        task.setProject(project);

        ProjectTask savedTask = taskRepository.save(task);

        if (dto.getAssignments() != null) {
            for (TaskAssignmentDTO assignDTO : dto.getAssignments()) {
                TeamMember member = teamMemberRepository.findById(assignDTO.getTeamMemberId())
                        .orElseThrow(() -> new EntityNotFoundException("Membre introuvable"));

                TaskAssignment assignment = new TaskAssignment();
                assignment.setTask(savedTask);
                assignment.setTeamMember(member);
                assignment.setProgress((int) Math.min(assignDTO.getProgress(), 100.0));
                assignment.setWorkedMD(assignDTO.getWorkedMD());

                // Dates estimées
                assignment.setEstimatedStartDate(assignDTO.getEstimatedStartDate());
                assignment.setEstimatedEndDate(assignDTO.getEstimatedEndDate());

                // Dates effectives si fournies
                assignment.setEffectiveStartDate(assignDTO.getEffectiveStartDate());
                assignment.setEffectiveEndDate(assignDTO.getEffectiveEndDate());

                // Jours fériés publics
                Set<LocalDate> publicHolidays = publicHolidayService.getAllCombinedHolidaysBetween(
                        assignDTO.getEstimatedStartDate(), assignDTO.getEstimatedEndDate()
                );

                // Estimation automatique des MD
                double estimatedMD = calculateWorkedDays(
                        assignDTO.getEstimatedStartDate(),
                        assignDTO.getEstimatedEndDate(),
                        publicHolidays
                );
                assignment.setEstimatedMD(estimatedMD);
                double remaining = estimatedMD - assignDTO.getWorkedMD();
                assignment.setRemainingMD(remaining);

                // 🔍 Logique optionnelle : jours personnels non travaillés
                if (assignDTO.getWorkedDaysList() != null) {
                    LocalDate start = assignDTO.getEffectiveStartDate() != null ? assignDTO.getEffectiveStartDate() : assignDTO.getEstimatedStartDate();
                    LocalDate end = assignDTO.getEffectiveEndDate() != null ? assignDTO.getEffectiveEndDate() : assignDTO.getEstimatedEndDate();

                    List<String> personalHolidays = new ArrayList<>();

                    if (start != null && end != null && !start.isAfter(end)) {
                        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                            DayOfWeek day = date.getDayOfWeek();
                            boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                            boolean isHoliday = publicHolidays.contains(date);
                            boolean workedThisDay = assignDTO.getWorkedDaysList().contains(date);

                            if (!isWeekend && !isHoliday && !workedThisDay) {
                                personalHolidays.add(date.toString());
                            }
                        }

                        // Ajout aux jours personnels du membre
                        if (!personalHolidays.isEmpty()) {
                            if (member.getHoliday() == null) member.setHoliday(new ArrayList<>());
                            Set<String> existing = new HashSet<>(member.getHoliday());
                            existing.addAll(personalHolidays);
                            member.setHoliday(new ArrayList<>(existing));
                        }
                    }
                }

                // 🔁 Éviter doublons d'assignation
                Optional<TaskAssignment> existingTask = assignmentRepository
                        .findByTaskIdAndTeamMemberId(savedTask.getId(), member.getId());

                if (existingTask.isEmpty()) {
                    assignmentRepository.save(assignment);
                } else {
                    System.out.println("⚠️ Assignation déjà existante pour ce membre.");
                }
            }
        }

        return getTacheById(savedTask.getId());
    }

    @Override
    public ProjectTaskDTO updateTache(Long id, ProjectTaskDTO dto) {
        ProjectTask task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tâche non trouvée"));

        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setDateDebut(dto.getDateDebut());
        task.setDateFin(dto.getDateFin());
        task.setStatus(dto.getStatus());

        taskRepository.save(task);

        // Supprimer les anciennes affectations de cette tâche
        assignmentRepository.deleteByTaskId(id);

        if (dto.getAssignments() != null) {
            for (TaskAssignmentDTO assignDTO : dto.getAssignments()) {
                TeamMember member = teamMemberRepository.findById(assignDTO.getTeamMemberId())
                        .orElseThrow(() -> new EntityNotFoundException("Membre introuvable"));

                TaskAssignment assignment = new TaskAssignment();
                assignment.setTask(task);
                assignment.setTeamMember(member);
                assignment.setProgress((int) Math.min(assignDTO.getProgress(), 100.0));
                assignment.setWorkedMD(assignDTO.getWorkedMD());

                assignment.setEstimatedStartDate(assignDTO.getEstimatedStartDate());
                assignment.setEstimatedEndDate(assignDTO.getEstimatedEndDate());
                assignment.setEffectiveStartDate(assignDTO.getEffectiveStartDate());
                assignment.setEffectiveEndDate(assignDTO.getEffectiveEndDate());

                // 🔍 Calcul des jours fériés pour l'intervalle
                LocalDate start = assignment.getEffectiveStartDate() != null ? assignment.getEffectiveStartDate() : assignment.getEstimatedStartDate();
                LocalDate end = assignment.getEffectiveEndDate() != null ? assignment.getEffectiveEndDate() : assignment.getEstimatedEndDate();

                Set<LocalDate> publicHolidays = publicHolidayService.getAllCombinedHolidaysBetween(start, end);
                double estimatedMD = calculateWorkedDays(assignment.getEstimatedStartDate(), assignment.getEstimatedEndDate(), publicHolidays);
                assignment.setEstimatedMD(estimatedMD);
                double remaining = estimatedMD - assignDTO.getWorkedMD();
                assignment.setRemainingMD(remaining);

                // 🧠 Logique complémentaire de jours personnels non travaillés
                if (assignDTO.getWorkedDaysList() != null && start != null && end != null && !start.isAfter(end)) {
                    List<String> personalHolidays = new ArrayList<>();
                    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
                        boolean isHoliday = publicHolidays.contains(date);
                        boolean workedThisDay = assignDTO.getWorkedDaysList().contains(date);

                        if (!isWeekend && !isHoliday && !workedThisDay) {
                            personalHolidays.add(date.toString());
                        }
                    }

                    // Fusion intelligente avec les congés existants
                    if (!personalHolidays.isEmpty()) {
                        if (member.getHoliday() == null) member.setHoliday(new ArrayList<>());
                        Set<String> existing = new HashSet<>(member.getHoliday());
                        existing.addAll(personalHolidays);
                        member.setHoliday(new ArrayList<>(existing));
                    }
                }

                assignmentRepository.save(assignment);
            }
        }

        return getTacheById(id);
    }

    @Override
    public ProjectTaskDTO getTacheById(Long id) {
        ProjectTask task = taskRepository.findById(id).orElseThrow();
        ProjectTaskDTO dto = mapToDTO(task);
        List<TaskAssignmentDTO> assignments = assignmentRepository.findByTaskId(id)
                .stream()
                .map(this::mapAssignmentToDTO)
                .collect(Collectors.toList());
        dto.setAssignments(assignments);
        return dto;
    }

    @Override
    public List<ProjectTaskDTO> getTachesByProject(Long projectId) {
        return taskRepository.findByProject_Id(projectId)
                .stream()
                .map(task -> getTacheById(task.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTache(Long id) {
        assignmentRepository.deleteByTaskId(id);
        workEntryRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }


    @Override
    public List<ProjectTaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(task -> getTacheById(task.getId()))
                .collect(Collectors.toList());
    }

    private ProjectTaskDTO mapToDTO(ProjectTask task) {
        // Récupérer tous les assignments de la tâche
        List<TaskAssignment> assignments = assignmentRepository.findByTaskId(task.getId());

        // Calculer la moyenne des progress (en pourcentage)
        double progress = 0;
        if (!assignments.isEmpty()) {
            progress = assignments.stream()
                    .mapToDouble(a -> {
                        Double estimated = a.getEstimatedMD();
                        Double worked = a.getWorkedMD();
                        if (estimated != null && estimated > 0 && worked != null) {
                            double p = (worked / estimated) * 100;
                            return Math.min(p, 100);
                        }
                        return 0;
                    })
                    .average()
                    .orElse(0);
        }

        ProjectTaskDTO dto = new ProjectTaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setDateDebut(task.getDateDebut());
        dto.setDateFin(task.getDateFin());
        dto.setStatus(task.getStatus());
        dto.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        dto.setProjectName(task.getProject() != null ? task.getProject().getName() : null);
        dto.setProgress((int) Math.round(progress));

        return dto;
    }
    private TaskAssignmentDTO mapAssignmentToDTO(TaskAssignment entity) {
        Double estimated = entity.getEstimatedMD();
        Double worked = entity.getWorkedMD();
        double progress = 0;
        if (estimated != null && estimated > 0 && worked != null) {
            progress = (worked / estimated) * 100;
            if (progress > 100) progress = 100;
        }
        return new TaskAssignmentDTO(
                entity.getId(),
                entity.getTeamMember().getId(),
                entity.getTeamMember().getName(),
                (int) Math.round(progress),
                worked,
                estimated,
                entity.getRemainingMD(),
                null,
                null,
                entity.getEstimatedStartDate(),
                entity.getEstimatedEndDate(),
                entity.getEffectiveStartDate(),
                entity.getEffectiveEndDate()
        );
    }

    @Transactional
    public void processWorkEntryAsHoliday(WorkEntry workEntry) {
        if (workEntry.getStatus() >= 1.0) return;

        TeamMember member = teamMemberRepository.findById(workEntry.getMemberId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        if (member.getHoliday() == null) {
            member.setHoliday(new ArrayList<>());
        }

        String entry = workEntry.getDate() + "|" + getLabel(workEntry.getStatus());
        member.getHoliday().removeIf(e -> e.startsWith(workEntry.getDate() + "|"));
        member.getHoliday().add(entry);

        teamMemberRepository.save(member); 

        System.out.println("✅ Congé enregistré : " + entry);
    }

    private long calculateWorkedDays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        if (start == null || end == null || start.isAfter(end)) return 0;
        long count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !holidays.contains(date)) {
                count++;
            }
        }
        return count;
    }

    private String getLabel(double status) {
        if (status == 0.25) return "QUARTER_DAY_LEAVE";
        if (status == 0.5) return "Half Day";
        if (status == 0.75) return "THREE_QUARTERS_LEAVE";
        if (status == 0) return "1 MD";
        return "PARTIAL_LEAVE";
    }

    @Override
    @Transactional
    public void updateWorkedMD(Long taskId, Long assignmentId, double workedMD) {
        TaskAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!assignment.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Task ID mismatch");
        }

        // Récupérer les work entries pour cette assignation
        List<WorkEntry> workEntries = workEntryRepository.findByTaskIdAndMemberId(
                taskId,
                assignment.getTeamMember().getId()
        );

        // Traiter chaque work entry comme un congé potentiel
        workEntries.forEach(this::processWorkEntryAsHoliday);

        assignment.setWorkedMD(workedMD);
        assignment.setRemainingMD(assignment.getEstimatedMD() - workedMD);
        assignmentRepository.save(assignment);
    }

    // Méthode pour sauvegarder une work entry
    @Transactional
    public WorkEntry saveWorkEntry(WorkEntry workEntry) {
        WorkEntry savedEntry = workEntryRepository.save(workEntry);

        // Traiter automatiquement comme un congé si nécessaire
        processWorkEntryAsHoliday(savedEntry);

        return savedEntry;
    }
}