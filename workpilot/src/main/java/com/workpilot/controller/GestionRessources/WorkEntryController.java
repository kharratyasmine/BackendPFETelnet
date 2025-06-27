package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.TeamMemberDTO;
import com.workpilot.entity.ressources.WorkEntry;
import com.workpilot.service.GestionRessources.WorkEntry.WorkEntryService;
import com.workpilot.service.GestionRessources.teamMember.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/work-entries")
@CrossOrigin(origins = "http://localhost:4200")
public class WorkEntryController {
    @Autowired
    private WorkEntryService workEntryService;
    
    @Autowired
    private TeamMemberService teamMemberService;

    @GetMapping("/task/{taskId}")
    public List<WorkEntry> getWorkEntriesByTask(@PathVariable Long taskId) {
        return workEntryService.getWorkEntriesByTask(taskId);
    }

    @GetMapping("/member/{memberId}")
    public List<WorkEntry> getWorkEntriesByMember(@PathVariable Long memberId) {
        return workEntryService.getWorkEntriesByMember(memberId);
    }

    @GetMapping("/member/{memberId}/task/{taskId}")
    public List<WorkEntry> getWorkEntriesByMemberAndTask(
            @PathVariable Long memberId,
            @PathVariable Long taskId) {
        return workEntryService.getWorkEntriesByMemberAndTask(memberId, taskId);
    }

    @GetMapping("/me")
    public List<WorkEntry> getMyWorkEntries(Principal principal) {
        String email = principal.getName();
        TeamMemberDTO teamMember = teamMemberService.getTeamMemberByEmail(email);
        return workEntryService.getWorkEntriesByMember(teamMember.getId());
    }

    @PostMapping
    public WorkEntry createWorkEntry(@RequestBody WorkEntry workEntry, Principal principal) {
        // Utiliser le TeamMemberService pour identifier l'utilisateur connecté
        return teamMemberService.addWorkEntry(workEntry, principal);
    }

    @PutMapping("/{id}")
    public WorkEntry updateWorkEntry(
            @PathVariable Long id,
            @RequestBody WorkEntry workEntry) {
        return workEntryService.updateWorkEntry(id, workEntry);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkEntry(@PathVariable Long id) {
        workEntryService.deleteWorkEntry(id);
    }
}