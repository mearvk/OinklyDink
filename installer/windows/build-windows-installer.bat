@echo off
REM ============================================================
REM OinklyDink - Windows Native Installer Builder
REM
REM Produces: OinklyDink-1.0.0.exe (app-image based installer)
REM           or OinklyDink-1.0.0.msi (Windows Installer package)
REM
REM Requirements:
REM   - JDK 14+ with jpackage
REM   - WiX Toolset 3.x (for .msi output)
REM     https://wixtoolset.org/releases/
REM
REM Run from project root after: mvn clean package
REM ============================================================

set APP_NAME=OinklyDink
set APP_VERSION=1.0.0
set MAIN_JAR=oinklydink-launcher-%APP_VERSION%.jar
set MAIN_CLASS=com.oinklydink.launcher.OinklyDink

echo === Building %APP_NAME% Windows Installer ===
echo.

REM Check for jpackage
where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: jpackage not found. Requires JDK 14+.
    exit /b 1
)

REM Check for source JAR
if not exist "target\%MAIN_JAR%" (
    echo ERROR: target\%MAIN_JAR% not found.
    echo Run: mvn clean package
    exit /b 1
)

REM Prepare input
if not exist "dist\input" mkdir "dist\input"
copy /Y "target\%MAIN_JAR%" "dist\input\" >nul

REM --- Build .exe installer ---
echo Building EXE installer...
jpackage ^
  --type exe ^
  --input dist\input ^
  --name %APP_NAME% ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --app-version %APP_VERSION% ^
  --description "Pig's Tail Java Launcher" ^
  --vendor "OinklyDink" ^
  --win-shortcut ^
  --win-shortcut-prompt ^
  --win-dir-chooser ^
  --win-menu ^
  --win-menu-group "OinklyDink" ^
  --dest dist\windows

echo.

REM --- Build .msi installer ---
echo Building MSI installer...
jpackage ^
  --type msi ^
  --input dist\input ^
  --name %APP_NAME% ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --app-version %APP_VERSION% ^
  --description "Pig's Tail Java Launcher" ^
  --vendor "OinklyDink" ^
  --win-shortcut ^
  --win-shortcut-prompt ^
  --win-dir-chooser ^
  --win-menu ^
  --win-menu-group "OinklyDink" ^
  --win-upgrade-uuid "a1b2c3d4-e5f6-7890-abcd-ef1234567890" ^
  --dest dist\windows

echo.
echo === Done ===
echo Output in: dist\windows\
dir dist\windows\*.exe dist\windows\*.msi 2>nul
