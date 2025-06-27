# WorkPilot Telnet - Backend Application

## Description

WorkPilot Telnet is a comprehensive Spring Boot backend application designed for project management, resource allocation, and business process automation. The application provides a robust API for managing clients, projects, devis (quotes), PSR (Project Status Reports), and various business workflows.

## Features

### 🔐 Authentication & Authorization
- JWT-based authentication system
- Role-based access control
- User management with permissions
- Password change functionality

### 📊 Dashboard & Analytics
- Planned workload management
- Task timesheet tracking
- Real-time dashboard metrics
- Consolidated weekly workload reports

### 💼 Client & Resource Management
- Client information management
- Resource allocation and planning
- Holiday management
- Demand tracking and approval workflows

### 📋 Devis (Quotes) Management
- Quote creation and management
- Devis history tracking
- Distribution management
- Financial and invoicing details
- Export functionality (Word documents)

### 📈 PSR (Project Status Reports)
- Project status tracking
- Risk management
- Delivery tracking
- PSR generation and export

### 📧 Notifications & Communication
- Email service integration
- Notification system
- Audit logging for all operations

### 📁 File Management
- File upload functionality
- Excel import capabilities
- Document management

## Technology Stack

- **Backend Framework**: Spring Boot 2.x
- **Database**: MySQL
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **Containerization**: Docker
- **Frontend**: Angular (static assets included)

## Project Structure

```
workpilot/
├── src/main/java/com/workpilot/
│   ├── auditing/          # Audit logging functionality
│   ├── authentification/  # Authentication & authorization
│   ├── configuration/     # Application configuration
│   ├── controller/        # REST API controllers
│   ├── dto/              # Data Transfer Objects
│   ├── entity/           # JPA entities
│   ├── exception/        # Custom exceptions
│   ├── Export/           # Export services
│   ├── repository/       # Data access layer
│   └── service/          # Business logic services
├── src/main/resources/
│   ├── static/           # Frontend assets
│   ├── templates/        # Email templates
│   └── application.properties
├── docker-compose.yml    # Docker configuration
└── Dockerfile           # Container definition
```

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6+
- MySQL 5.7+
- Docker (optional)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/kharratyasmine/BackendPFETelnet.git
   cd BackendPFETelnet
   ```

2. **Configure Database**
   - Create a MySQL database
   - Update `src/main/resources/application.properties` with your database credentials

3. **Run the application**
   ```bash
   # Using Maven
   mvn spring-boot:run
   
   # Or build and run JAR
   mvn clean package
   java -jar target/workpilot-*.jar
   ```

### Docker Setup

1. **Build and run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

2. **Or build Docker image manually**
   ```bash
   docker build -t workpilot-telnet .
   docker run -p 8080:8080 workpilot-telnet
   ```

## API Documentation

The application exposes REST APIs for:

- **Authentication**: `/api/auth/**`
- **Dashboard**: `/api/dashboard/**`
- **Clients**: `/api/clients/**`
- **Devis**: `/api/devis/**`
- **PSR**: `/api/psr/**`
- **Resources**: `/api/resources/**`
- **Audit**: `/api/audit/**`

## Configuration

Key configuration files:
- `application.properties` - Main application configuration
- `application-docker.properties` - Docker-specific configuration
- `docker-compose.yml` - Docker services configuration

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is part of a PFE (Projet de Fin d'Études) for Telnet.

## Contact

- **Author**: Kharrat Yasmine
- **Organization**: Telnet
- **Project**: WorkPilot Backend Application

---

*This application is designed to streamline business processes and improve project management efficiency.* 