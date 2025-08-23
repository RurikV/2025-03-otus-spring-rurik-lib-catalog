# HW17 — Docker. Containerizing the Application

This module demonstrates deploying the "Library Catalog" web application with Docker.
The application image is built using a multi-stage Dockerfile located in this hw17-docker directory,
and the stack (app + MongoDB + mongo-express) is launched via Docker Compose from the hw17-docker directory.

## Prerequisites
- Docker 24+ and Docker Compose v2 (the `docker compose` command)
- Free ports: 8080 (application), 8081 (mongo-express), 27017 (MongoDB)

## Quick Start
1. Go to the module directory:
   ```bash
   cd hw17-docker
   ```
2. Build and start the entire environment (the app image will be built automatically from the root Dockerfile):
   ```bash
   docker compose up --build -d
   ```
3. Check application logs:
   ```bash
   docker compose logs -f app
   ```
4. Open in your browser:
   - Application: http://localhost:8080
   - Mongo Express: http://localhost:8081

Default MongoDB credentials:
- User: `root`
- Password: `example`
- URI (used by the app): `mongodb://root:example@mongo:27017/library?authSource=admin`

## How It Works
- The application image is built with Maven (build stage) and runs on a lightweight JRE (alpine).
  The Dockerfile features:
  - cached build of the `hw17-docker` module only (`-pl hw17-docker -am`);
  - running as a non-root user;
  - logging to stdout/stderr (Spring Boot defaults) — ready for modern DevOps stacks.
- The compose.yaml (in this directory) runs 3 services:
  - `app` — the Spring Boot application, port 8080;
  - `mongo` — MongoDB 7, port 27017, login/password `root/example`;
  - `mongo-express` — UI for MongoDB on port 8081.
- The `SPRING_DATA_MONGODB_URI` environment variable is passed to the application so that DB configuration is not baked into the image and can be provided at runtime.

## Useful Commands
- List services:
  ```bash
  docker compose ps
  ```
- Application logs:
  ```bash
  docker compose logs -f app
  ```
- Rebuild application without cache:
  ```bash
  docker compose build --no-cache app && docker compose up -d app
  ```
- Stop and remove the environment:
  ```bash
  docker compose down
  ```
  (add `-v` if you need to remove anonymous volumes)

## Local Run Without Docker (optional)
MongoDB must be available locally. Then run:
```bash
mvn -pl hw17-docker -am spring-boot:run
```
and set the environment variable, for example:
```bash
SPRING_DATA_MONGODB_URI="mongodb://root:example@localhost:27017/library?authSource=admin"
```

## Common Issues
- Ports are busy: adjust port mappings in compose.yaml or free ports 8080/8081/27017.
- No access to Mongo: verify `root/example` credentials and that `SPRING_DATA_MONGODB_URI` is correct.
- Slow rebuilds: leverage Maven cache; when only `hw17-docker` code changes, rebuilds are faster.

## License
See the LICENSE file at the repository root.
