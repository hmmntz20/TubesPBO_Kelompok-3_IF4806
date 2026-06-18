@echo off

echo Starting Backend...
start "Backend" cmd /k "cd Backend && mvnw.cmd clean spring-boot:run"

echo Starting Frontend...
start "Frontend" cmd /k "cd Frontend && npx expo start -c"

echo All services started.