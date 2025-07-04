package com.workpilot.dto.GestionRessources;

import com.workpilot.entity.ressources.Seniority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDTO {
    private Long id;
    private String name;
    private String initial;
    private String jobTitle;
    private List<String> holiday;
    private Seniority role;
    private Double cost;
    private String note;
    private String image;
    private Double allocation = 0.0;
    private List<Long> teams;
    private String experienceRange;
    private LocalDate StartDate;
    private LocalDate EndDate;
    private String status;
    private boolean fake;
    private String email;



    // ✅ Ajout d'un constructeur sans projectId (si nécessaire)
    public TeamMemberDTO(Long id, String name, String initial, String jobTitle, List<String> holiday,
                         Seniority role, Double cost, String note, String image , List<Long> teams, String experienceRange,
                         Double allocation) {
        this.id = id;
        this.name = name;
        this.initial = initial;
        this.jobTitle = jobTitle;
        this.holiday = holiday;
        this.role = role;
        this.cost = cost;
        this.note = note;
        this.image = image;
        this.teams = teams;
        this.experienceRange = experienceRange;
        this.allocation = allocation;
    }

    // Constructeur avec email
    public TeamMemberDTO(Long id, String name, String initial, String jobTitle, List<String> holiday,
                        Seniority role, Double cost, String note, String image, List<Long> teams, String experienceRange,
                        Double allocation, String email) {
        this(id, name, initial, jobTitle, holiday, role, cost, note, image, teams, experienceRange, allocation);
        this.email = email;
    }

}
