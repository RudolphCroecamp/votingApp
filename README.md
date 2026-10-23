# VotingApp

VotingApp is a secure, mobile-first voting platform with a companion admin dashboard for managing elections, parties, voters, and results. The project combines a Kotlin/Jetpack Compose Android app for voters with a React admin portal for election officials.

## Overview

This repository contains two client applications:

- Mobile app for voters and election participants
- Web app for admins and election staff

Together they support the complete election lifecycle, from registration and identity checks to voting, results management, and operational monitoring.

## Features

### Mobile voting app

- Voter registration flow
- South African ID validation and address capture
- Face verification and camera-based checks
- Privacy consent and election information screens
- Party manifesto browsing
- Voting flow with confirmation and receipt validation
- Reminders and election status screens
- Results viewing
- Offline / server error / locked account handling

### Admin web app

- Login and secure admin access
- Dashboard overview
- Manage elections
- Manage political parties
- View and manage voters
- Election results reporting
- API health monitoring
- Admin management views



# Project Setup Guide

## Requirements

Before setting up the project, install the following software:

- **Git** – to clone the project repository.
- **Docker Desktop** – to run the backend, PostgreSQL database and blockchain nodes.
- **Node.js (LTS) and npm** – to install and run the React frontend.
- **A modern web browser** – to access the admin dashboard.

## 1. Clone the Repository

Open a terminal and run the following commands:

```bash
git clone https://github.com/RudolphCroecamp/votingApp.git
cd ~\votingApp
```


## 2. Backend Setup

1. Open Docker Desktop and ensure it is running.
2. Open a terminal in the directory containing the `compose.yaml` or `docker-compose.yml` file. cd ~\votingApp\votingBackend3
3. Build and start the backend, PostgreSQL database and blockchain nodes:

```bash
docker compose up --build -d
```

4. Check that the containers are running:

```bash
docker compose ps
```

5. To view the backend logs if needed:

```bash
docker compose logs votingbackend3
```

Wait for the services to start before opening the frontend.

## 3. Frontend Setup

1. Open a new terminal in the cloned project directory.
2. Navigate to the React frontend directory:

```bash
cd ~\votingApp\web-app
```

3. Install the required dependencies:

```bash
npm install
```

4. Start the development server:

```bash
npm run dev
```

5. Open the URL displayed in the terminal, usually:

**http://localhost:5173**

The voting system admin dashboard should now be accessible in your browser.

# Admin login credentials
    username = 0000000000001
    password = Admin123!ChangeMe


## 4. Stopping the Application

To stop the backend, database and blockchain containers, run the following command from the directory containing the Docker Compose file:

```bash
docker compose down
```

To run the backend services again later:

```bash
docker compose up -d
```


