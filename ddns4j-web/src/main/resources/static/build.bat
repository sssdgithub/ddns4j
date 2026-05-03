@echo off
REM DDNS4J Frontend Build Script for Windows
REM This script builds the Vue 3 frontend and copies the output to Spring Boot static resources

echo Building DDNS4J frontend...

REM Install dependencies
echo Installing dependencies...
call npm install

REM Build for production
echo Building for production...
call npm run build

REM Copy built files to parent directory (Spring Boot serves from static/)
echo.
echo Copying built files...
if exist "dist\" (
    xcopy /E /I /Y "dist\assets" "assets" >nul
    copy /Y "dist\index.html" "index.html" >nul
    echo Built files copied successfully!
)

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
echo.
echo The application is ready to run:
echo   cd ../../../../..
echo   mvn spring-boot:run
echo.
pause
