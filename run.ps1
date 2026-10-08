Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Launching Employee Information Management System (EMS)" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

# 1. Compile if necessary
if (-not (Test-Path "bin\com\ems\Main.class")) {
    Write-Host "[INFO] Binaries not found. Compiling Java sources first..." -ForegroundColor Yellow
    if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
    javac -d bin -cp ".;lib/mysql-connector-j.jar;lib/mysql-connector-j-8.3.0.jar" src/com/ems/*.java
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Compilation failed. Cannot launch application." -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

# 2. Run the application
Write-Host "[INFO] Starting Java Application..." -ForegroundColor Green
java -cp "bin;lib/mysql-connector-j.jar;lib/mysql-connector-j-8.3.0.jar" com.ems.Main
