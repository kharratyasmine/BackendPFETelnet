package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.*;
import com.workpilot.entity.ressources.ProjectTask;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.WorkEntry;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.service.GestionRessources.teamMember.TeamMemberService;
import com.workpilot.service.GestionRessources.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teamMembers")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamMemberController {

    @Autowired
    private TeamMemberService teamMemberService;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<TeamMemberDTO> getAllTeamMembers() {
        return teamMemberService.getAllTeamMembers();
    }

    @GetMapping("/{id}")
    public TeamMemberDTO getTeamMemberById(@PathVariable Long id) {
        return teamMemberService.getTeamMemberById(id);
    }

    @PostMapping
    public TeamMemberDTO createTeamMember(@RequestBody TeamMemberDTO teamMemberDTO) {
        return teamMemberService.createTeamMember(teamMemberDTO);
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadTeamMemberImage(@PathVariable Long id,
                                                   @RequestParam("image") MultipartFile file) {
        try {
            TeamMember member = teamMemberRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (ext.equals(".jfif")) ext = ".jpg";

            if (!ext.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
                throw new RuntimeException("Type de fichier non supporté : " + ext);
            }

            String fileName = "member_" + id + "_" + System.currentTimeMillis() + ext;
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", fileName);
            Files.createDirectories(uploadPath.getParent());
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

            member.setImage("/uploads/" + fileName);
            teamMemberRepository.save(member);

            return ResponseEntity.ok().body(Map.of("imagePath", "/uploads/" + fileName));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur upload image : " + e.getMessage());
        }
    }



    @GetMapping("/project/{projectId}/members")
    public List<TeamMemberDTO> getMembersByProject(@PathVariable Long projectId) {
        return teamMemberService.getMembersByProjectId(projectId);
    }

    // TeamMemberController.java
    @GetMapping("/team/{teamId}")
    public List<TeamMemberDTO> getMembersByTeam(@PathVariable Long teamId) {
        return teamMemberService.getTeamMembersByTeamId(teamId);
    }

    @GetMapping("/team/{teamId}/members")
    public List<TeamMemberDTO> getTeamMembersByTeam(@PathVariable Long teamId) {
        return teamMemberService.getTeamMembersByTeamId(teamId);
    }


    @PutMapping("/{id}")
    public TeamMemberDTO updateTeamMember(@PathVariable Long id, @RequestBody TeamMemberDTO teamMemberDTO) {
        return teamMemberService.updateTeamMember(id, teamMemberDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteTeamMember(@PathVariable Long id) {
        teamMemberService.deleteTeamMember(id);
    }

    @GetMapping("/me")
    public ResponseEntity<TeamMemberDTO> getMyTeamMemberInfo(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMemberDTO = teamMemberService.getTeamMemberByEmail(email);
        return ResponseEntity.ok(teamMemberDTO);
    }

    @GetMapping("/me/id")
    public ResponseEntity<Long> getMyTeamMemberId(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMemberDTO = teamMemberService.getTeamMemberByEmail(email);
        return ResponseEntity.ok(teamMemberDTO.getId());
    }

    @GetMapping("/me/allocations")
    public ResponseEntity<List<TeamMemberAllocationDTO>> getMyAllocations(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        List<TeamMemberAllocationDTO> allocations = teamMemberService.getMemberAllocations(teamMember.getId());
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/me/projects")
    public ResponseEntity<List<ProjectDTO>> getMyProjects(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        List<ProjectDTO> projects = teamMemberService.getMemberProjects(teamMember.getId());
        return ResponseEntity.ok(projects);
    }


    @GetMapping("/me/teams")
    public ResponseEntity<TeamDTO> getMyTeam(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        TeamDTO team = teamMemberService.getMemberTeam(teamMember.getId());
        return ResponseEntity.ok(team);
    }

    @GetMapping("/me/colleagues")
    public ResponseEntity<List<TeamMemberDTO>> getMyColleagues(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        List<TeamMemberDTO> colleagues = teamMemberService.getMemberColleagues(teamMember.getId());
        return ResponseEntity.ok(colleagues);
    }

    @GetMapping("/me/holidays")
    public ResponseEntity<List<String>> getMyHolidays(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        return ResponseEntity.ok(teamMember.getHoliday());
    }

    @GetMapping("/me/tasks")
    public ResponseEntity<List<ProjectTaskDTO>> getMyTasks(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        List<ProjectTaskDTO> tasks = teamMemberService.getMemberTasks(teamMember.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/me/work-entries")
    public ResponseEntity<List<WorkEntry>> getMyWorkEntries(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        List<WorkEntry> workEntries = teamMemberService.getMemberWorkEntries(teamMember.getId());
        return ResponseEntity.ok(workEntries);
    }

    @PostMapping("/me/work-entries")
    public ResponseEntity<WorkEntry> addWorkEntryForCurrentUser(
            @RequestBody WorkEntry workEntry,
            Principal principal
    ) {
        WorkEntry savedEntry = teamMemberService.addWorkEntry(workEntry, principal);
        return ResponseEntity.ok(savedEntry);
    }



    @PutMapping("/me/work-entries/{workEntryId}")
    public ResponseEntity<WorkEntry> updateWorkEntry(
            Principal principal,
            @PathVariable Long workEntryId,
            @RequestBody WorkEntry workEntry) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        WorkEntry updatedEntry = teamMemberService.updateWorkEntry(teamMember.getId(), workEntryId, workEntry);
        return ResponseEntity.ok(updatedEntry);
    }

    @GetMapping("/test-initials")
    public ResponseEntity<Map<String, String>> testInitialsGeneration(
            @RequestParam String firstname,
            @RequestParam String lastname) {
        String initials = teamMemberService.generateInitials(firstname, lastname);
        return ResponseEntity.ok(Map.of(
            "firstname", firstname,
            "lastname", lastname,
            "initials", initials
        ));
    }
}