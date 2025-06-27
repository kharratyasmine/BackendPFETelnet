package com.workpilot.controller;

import com.workpilot.entity.Notification;
import com.workpilot.repository.NotificationRepository;
import com.workpilot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(null); // Utilisateur non authentifié
        }
        try {
        String userRole = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
        List<Notification> notifications = notificationRepository.findAll().stream()
                .filter(n -> n.getRoleTargeted() == null || n.getRoleTargeted().equalsIgnoreCase(userRole))
                .collect(Collectors.toList());
        List<NotificationDTO> dtos = notifications.stream()
                .map(n -> new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt(), n.getRoleTargeted()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace(); // Affiche l'erreur dans la console backend
            throw e; // Propage l'erreur pour voir le détail dans les logs
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    public static class NotificationDTO {
        public Long id;
        public String title;
        public String message;
        public boolean read;
        public LocalDateTime createdAt;
        public String roleTargeted;

        public NotificationDTO(Long id, String title, String message, boolean read, LocalDateTime createdAt, String roleTargeted) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.read = read;
            this.createdAt = createdAt;
            this.roleTargeted = roleTargeted;
        }
    }

    // Dans NotificationController.java
    @PostMapping("/test")
    public ResponseEntity<Void> sendTestNotification() {
        notificationService.sendNotification("Test", "Ceci est une notification de test", "ADMIN");
        return ResponseEntity.ok().build();
    }
}