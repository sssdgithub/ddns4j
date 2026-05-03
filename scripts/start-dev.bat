@echo off
title DDNS4J Development Environment

echo.
echo ========================================
echo    DDNS4J Development Environment
echo ========================================
echo.

cd /d "%~dp0.."

echo Current directory: %CD%
echo.
echo Starting...
echo.

cd ddns4j-web\src\main\resources\static

if not exist "package.json" (
    echo ERROR: package.json not found
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
)

echo.
echo Starting backend in new window...
set "PROJECT_ROOT=%CD%\..\..\..\..\.."
echo Project root: %PROJECT_ROOT%
echo Checking pom.xml in project root...
if exist "%PROJECT_ROOT%\pom.xml" (
    echo [OK] pom.xml found
) else (
    echo [ERROR] pom.xml NOT found at %PROJECT_ROOT%
    pause
    exit /b 1
)

echo.
echo [INFO] Starting Spring Boot backend on port 10000...
start "DDNS4J Backend" cmd /k "cd /d %PROJECT_ROOT% && mvn spring-boot:run -pl ddns4j-web"

echo.
echo Waiting for backend to start (this may take 30-60 seconds)...
echo [TIP] Watch the backend window for startup progress
timeout /t 30 /nobreak >nul

echo.
echo Checking if backend is ready...
set RETRY_COUNT=0
set MAX_RETRIES=10

:check_backend
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:10000/publicAccess/publicAccessDisabled' -UseBasicParsing -TimeoutSec 2; exit 0 } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Backend is ready!
    goto backend_ready
)

set /a RETRY_COUNT+=1
if %RETRY_COUNT% geq %MAX_RETRIES% (
    echo [WARNING] Backend may not be ready yet, but continuing...
    echo [TIP] You can manually check backend status at: http://localhost:10000
    goto backend_ready
)

echo   Waiting... (%RETRY_COUNT%/%MAX_RETRIES%)
timeout /t 5 /nobreak >nul
goto check_backend

:backend_ready
echo.
echo Starting frontend development server...
call npm run dev:open

echo.
echo Done! Press any key to close this window.
pause
