# Workpilot Docker Setup (Full Stack)

This document provides instructions for running the complete Workpilot application (Frontend + Backend + Database) using Docker and Docker Compose.

## Prerequisites

- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)
- Node.js 18+ (for local frontend development)

## Project Structure

```
workpilot/                    # Backend project
├── Dockerfile               # Backend container definition
├── docker-compose.yml       # Multi-service orchestration
├── nginx.conf              # Frontend nginx configuration
├── frontend.Dockerfile     # Frontend container definition
├── .dockerignore           # Files to exclude from build
├── src/main/resources/
│   └── application-docker.properties  # Docker-specific config
├── mysql/
│   └── init/
│       └── 01-init.sql     # Database initialization script
└── README-Docker.md        # This file

../workpilot-front/          # Frontend project (Angular)
├── Dockerfile              # Frontend container definition
├── package.json
├── angular.json
└── src/
```

## Quick Start

1. **Ensure both projects are in the correct structure:**
   ```
   /your-workspace/
   ├── workpilot/           # Backend
   └── workpilot-front/     # Frontend
   ```

2. **Navigate to the backend project directory:**
   ```bash
   cd workpilot
   ```

3. **Build and start all services:**
   ```bash
   docker-compose up -d --build
   ```

4. **Check the status of services:**
   ```bash
   docker-compose ps
   ```

5. **View logs:**
   ```bash
   # View all logs
   docker-compose logs -f
   
   # View specific service logs
   docker-compose logs -f workpilot-frontend
   docker-compose logs -f workpilot-app
   docker-compose logs -f mysql
   ```

## Services

The Docker Compose setup includes the following services:

### 1. MySQL Database (`mysql`)
- **Port:** 3306
- **Database:** workpilot
- **Username:** workpilot_user
- **Password:** workpilot_password
- **Root Password:** workpilot_root_password

### 2. Spring Boot Application (`workpilot-app`)
- **Port:** 8080
- **URL:** http://localhost:8080
- **Health Check:** http://localhost:8080/actuator/health
- **API Base:** http://localhost:8080/

### 3. Angular Frontend (`workpilot-frontend`)
- **Port:** 3000
- **URL:** http://localhost:3000
- **Health Check:** http://localhost:3000/health
- **API Proxy:** http://localhost:3000/api/ → http://workpilot-app:8080/

### 4. phpMyAdmin (Optional)
- **Port:** 8081
- **URL:** http://localhost:8081
- **Username:** root
- **Password:** workpilot_root_password

## Frontend-Backend Integration

### API Communication
The frontend communicates with the backend through nginx proxy:

- **Frontend requests to `/api/*`** are automatically proxied to the backend
- **WebSocket connections** to `/ws/*` are proxied for real-time features
- **File uploads** to `/uploads/*` are proxied to the backend

### CORS Configuration
- Backend is configured to accept requests from frontend origins
- nginx handles CORS preflight requests
- All necessary headers are properly forwarded

### Environment Variables

You can customize the configuration by setting environment variables:

#### Database Configuration
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/workpilot?createDatabaseIfNotExist=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=workpilot_user
SPRING_DATASOURCE_PASSWORD=workpilot_password
```

#### CORS Configuration
```bash
SPRING_WEB_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://workpilot-frontend:80
SPRING_WEB_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS,PATCH
SPRING_WEB_CORS_ALLOWED_HEADERS=*,Authorization,Content-Type,X-Requested-With
```

#### Email Configuration
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=your-email@gmail.com
```

#### JWT Configuration
```bash
JWT_SECRET_KEY=your-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

## Useful Commands

### Start Services
```bash
# Start all services in background
docker-compose up -d

# Start specific service
docker-compose up -d workpilot-frontend

# Rebuild and start
docker-compose up -d --build
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

### View Logs
```bash
# Follow logs for all services
docker-compose logs -f

# Follow logs for specific service
docker-compose logs -f workpilot-frontend
docker-compose logs -f workpilot-app

# View last 100 lines
docker-compose logs --tail=100 workpilot-frontend
```

### Frontend Development
```bash
# Access frontend container
docker-compose exec workpilot-frontend sh

# View nginx configuration
docker-compose exec workpilot-frontend cat /etc/nginx/conf.d/default.conf

# Test nginx configuration
docker-compose exec workpilot-frontend nginx -t
```

### Backend Development
```bash
# Access backend container
docker-compose exec workpilot-app sh

# View application logs
docker-compose logs -f workpilot-app

# Restart backend only
docker-compose restart workpilot-app
```

### Database Operations
```bash
# Access MySQL directly
docker-compose exec mysql mysql -u root -pworkpilot_root_password workpilot

# Backup database
docker-compose exec mysql mysqldump -u root -pworkpilot_root_password workpilot > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -pworkpilot_root_password workpilot < backup.sql
```

## Troubleshooting

### Frontend Issues
1. **Frontend not loading:**
   ```bash
   docker-compose logs workpilot-frontend
   docker-compose exec workpilot-frontend nginx -t
   ```

2. **API calls failing:**
   - Check if backend is healthy: `docker-compose ps workpilot-app`
   - Verify nginx proxy configuration
   - Check CORS settings in backend logs

3. **Build issues:**
   ```bash
   # Rebuild frontend only
   docker-compose build workpilot-frontend
   docker-compose up -d workpilot-frontend
   ```

### Backend Issues
1. **Application won't start:**
   ```bash
   docker-compose logs workpilot-app
   docker-compose exec workpilot-app java -jar app.jar --debug
   ```

2. **Database connection issues:**
   ```bash
   docker-compose exec workpilot-app ping mysql
   docker-compose exec mysql mysql -u workpilot_user -pworkpilot_password -e "SELECT 1;"
   ```

### Network Issues
1. **Services can't communicate:**
   ```bash
   # Check network
   docker network ls
   docker network inspect workpilot_workpilot-network
   
   # Test connectivity
   docker-compose exec workpilot-frontend ping workpilot-app
   docker-compose exec workpilot-app ping mysql
   ```

### Port Conflicts
If ports are already in use, modify the `docker-compose.yml` file:

```yaml
services:
  workpilot-frontend:
    ports:
      - "3001:80"  # Change external port
  workpilot-app:
    ports:
      - "8081:8080"  # Change external port
```

## Development Workflow

### Frontend Development
1. **Local development with Docker backend:**
   ```bash
   # Start only backend services
   docker-compose up -d mysql workpilot-app
   
   # Run frontend locally
   cd ../workpilot-front
   npm install
   npm start
   ```

2. **Update frontend configuration:**
   - Set API base URL to `http://localhost:8080` for local development
   - Use `http://workpilot-app:8080` for Docker-to-Docker communication

### Backend Development
1. **Local development with Docker database:**
   ```bash
   # Start only database
   docker-compose up -d mysql
   
   # Run backend locally with Docker profile
   ./mvnw spring-boot:run -Dspring.profiles.active=docker
   ```

## Production Considerations

### Security
1. Change all default passwords
2. Use environment variables for sensitive data
3. Enable HTTPS with proper SSL certificates
4. Configure proper CORS origins for production domains
5. Use Docker secrets for sensitive configuration

### Performance
1. Configure nginx caching for static assets
2. Optimize Angular build for production
3. Configure MySQL performance settings
4. Use external volumes for data persistence
5. Consider using a CDN for static assets

### Monitoring
1. Enable Spring Boot Actuator endpoints
2. Configure nginx access logs
3. Set up health checks and monitoring
4. Use Docker monitoring tools
5. Configure log aggregation

### Scaling
1. Use Docker Swarm or Kubernetes for orchestration
2. Configure load balancing for multiple instances
3. Use external databases for high availability
4. Implement proper session management
5. Configure horizontal scaling for both frontend and backend

## Support

For issues related to:
- **Docker setup:** Check this README and Docker documentation
- **Frontend issues:** Check Angular documentation and nginx logs
- **Backend issues:** Check Spring Boot documentation and application logs
- **Database issues:** Check MySQL logs and documentation
- **Integration issues:** Check CORS configuration and network connectivity 