@echo off
echo =========================================================
echo  Launching Employee Information Management System (EMS)
echo =========================================================

REM 1. Ensure bin folder exists and compile sources
if not exist "bin" mkdir bin
echo [INFO] Compiling Java sources...
javac -encoding UTF-8 -d bin -cp ".;lib/*" src/com/ems/*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Cannot launch application.
    pause
    exit /b %ERRORLEVEL%
)

REM 2. Run the application
echo [INFO] Starting Application...
java -cp "bin;lib/*" com.ems.Main

pause
