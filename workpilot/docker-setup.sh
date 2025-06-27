#!/bin/bash

# Workpilot Docker Setup Script
# This script helps you set up and run the complete Workpilot application

set -e

echo "🚀 Workpilot Docker Setup"
echo "=========================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install it and try again."
    exit 1
fi

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "⚠️  Port $port is already in use. Please stop the service using this port or change the port in docker-compose.yml"
        return 1
    fi
    return 0
}

# Check for port conflicts
echo "🔍 Checking for port conflicts..."
check_port 3000 || exit 1
check_port 8080 || exit 1
check_port 8081 || exit 1
check_port 3306 || exit 1

echo "✅ Ports are available"

# Check if frontend project exists
if [ ! -d "../workpilot-front" ]; then
    echo "⚠️  Frontend project not found at ../workpilot-front"
    echo "   Please ensure your project structure is:"
    echo "   /your-workspace/"
    echo "   ├── workpilot/           # Backend (current directory)"
    echo "   └── workpilot-front/     # Frontend"
    echo ""
    read -p "Do you want to continue without the frontend? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
    FRONTEND_AVAILABLE=false
else
    FRONTEND_AVAILABLE=true
    echo "✅ Frontend project found"
fi

# Build and start services
echo "🔨 Building and starting services..."

if [ "$FRONTEND_AVAILABLE" = true ]; then
    docker-compose up -d --build
else
    # Start only backend services
    docker-compose up -d --build mysql workpilot-app phpmyadmin
fi

echo "⏳ Waiting for services to start..."

# Wait for MySQL to be ready
echo "📊 Waiting for MySQL..."
until docker-compose exec -T mysql mysqladmin ping -h localhost -u root -pworkpilot_root_password --silent; do
    sleep 2
done
echo "✅ MySQL is ready"

# Wait for backend to be ready
echo "🔧 Waiting for backend..."
until curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; do
    sleep 5
done
echo "✅ Backend is ready"

# Wait for frontend to be ready (if available)
if [ "$FRONTEND_AVAILABLE" = true ]; then
    echo "🎨 Waiting for frontend..."
    until curl -f http://localhost:3000/health > /dev/null 2>&1; do
        sleep 2
    done
    echo "✅ Frontend is ready"
fi

echo ""
echo "🎉 Workpilot is now running!"
echo "=========================="
echo ""
echo "📱 Services:"
echo "   • Frontend:     http://localhost:3000"
echo "   • Backend API:  http://localhost:8080"
echo "   • phpMyAdmin:   http://localhost:8081"
echo ""
echo "🔑 Default credentials:"
echo "   • phpMyAdmin: root / workpilot_root_password"
echo "   • Database:   workpilot_user / workpilot_password"
echo ""
echo "📋 Useful commands:"
echo "   • View logs:     docker-compose logs -f"
echo "   • Stop services: docker-compose down"
echo "   • Restart:       docker-compose restart"
echo ""
echo "🔧 Development:"
echo "   • Frontend logs: docker-compose logs -f workpilot-frontend"
echo "   • Backend logs:  docker-compose logs -f workpilot-app"
echo "   • Database:      docker-compose exec mysql mysql -u root -pworkpilot_root_password workpilot"
echo ""

# Check if services are healthy
echo "🏥 Health check:"
docker-compose ps

echo ""
echo "✨ Setup complete! Enjoy developing with Workpilot!" 