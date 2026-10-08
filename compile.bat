@echo off
echo ===================================================
echo  Compiling Employee Information Management System
echo ===================================================

if not exist "bin" mkdir bin

javac -d bin -cp ".;lib/mysql-connector-j.jar;lib/mysql-connector-j-8.3.0.jar" src/com/ems/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Compilation completed successfully!
    echo Compiled classes are placed in the 'bin/' directory.
) else (
    echo.
    echo [ERROR] Compilation failed. Please check the error messages above.
)

pause
