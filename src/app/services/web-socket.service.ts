import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);

  constructor(private http: HttpClient, private authService: AuthService) {}

  public loadPersistedNotifications() {
    const token = this.authService.getAccessToken();
    if (!token || token.trim() === '') {
      console.warn('Aucun token trouvé, chargement des notifications annulé.');
      this.notificationsSubject.next([]); // On vide la liste si pas connecté
      return;
    }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

    this.http.get<AppNotification[]>(`${environment.apiUrl}/api/notifications`, { headers })
      .subscribe({
        next: (notifications) => {
          this.notificationsSubject.next(notifications);
        },
        error: (error) => {
          if (error.status === 401) {
            console.warn('Utilisateur non authentifié ou token expiré, notifications non chargées.');
            this.notificationsSubject.next([]); // On vide la liste si non autorisé
          } else {
            console.error('Erreur chargement notifs:', error);
          }
        }
      });
  }
} 