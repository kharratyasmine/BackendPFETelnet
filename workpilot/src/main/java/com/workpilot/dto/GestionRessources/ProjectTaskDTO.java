package com.workpilot.dto.GestionRessources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.workpilot.entity.ressources.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskDTO {
    private Long id;
    private String name;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateFin;

    private TaskStatus status;
    private Long projectId;
    private String projectName;

    private List<TaskAssignmentDTO> assignments;
    private Integer progress; // ou Double progress;

    public ProjectTaskDTO(Long id, String name, String description, LocalDate dateDebut, LocalDate dateFin, TaskStatus status, Long aLong, @NotBlank(message = "Le nom du projet est obligatoire") String s, Object o) {
    }
}