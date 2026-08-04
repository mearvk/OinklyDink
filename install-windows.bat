@echo off
REM ============================================================
REM OinklyDink - Windows Installer
REM
REM Installs OinklyDink, verifies Java GUI runtime, creates
REM desktop shortcut and Start Menu entry.
REM
REM Run as: install-windows.bat
REM
REM "Worth $88,000,000 or a Man and his Day."
REM ============================================================

setlocal enabledelayedexpansion

set APP_NAME=OinklyDink
set VERSION=1.0.0
set JAR_NAME=oinklydink-launcher-%VERSION%.jar
set INSTALL_DIR=%LOCALAPPDATA%\OinklyDink

echo.
echo   ========================================
echo     OinklyDink Installer - Pig's Tail
echo     Version %VERSION%
echo   ========================================
echo.

REM --- Step 1: Check Java ---
echo [1/4] Checking for Java...

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo    Java not found on PATH.
    echo.
    echo    OinklyDink requires Java 11+ with GUI support.
    echo    Please download and install from:
    echo      https://adoptium.net/
    echo.
    echo    After installing Java, run this installer again.
    pause
    exit /b 1
)

REM Verify Java can do GUI (quick check)
java -Djava.awt.headless=false -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo    WARNING: Java found but may not have GUI support.
    echo    Continuing anyway...
)

echo    Java found: OK
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do echo    %%i
echo.

REM --- Step 2: Find JAR ---
echo [2/4] Locating OinklyDink JAR...

set JAR_PATH=
if exist "%~dp0%JAR_NAME%" (
    set "JAR_PATH=%~dp0%JAR_NAME%"
) else if exist "%~dp0target\%JAR_NAME%" (
    set "JAR_PATH=%~dp0target\%JAR_NAME%"
)

if "%JAR_PATH%"=="" (
    echo    ERROR: Cannot find %JAR_NAME%
    echo    Place it in the same directory as this script.
    pause
    exit /b 1
)
echo    Found: %JAR_PATH%
echo.

REM --- Step 3: Install files ---
echo [3/4] Installing to %INSTALL_DIR%...

if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"
copy /Y "%JAR_PATH%" "%INSTALL_DIR%\%JAR_NAME%" >nul
echo    Copied %JAR_NAME%

REM Create launcher bat (animated Dink 5 widget — the primary experience)
(
echo @echo off
echo java -cp "%INSTALL_DIR%\%JAR_NAME%" com.oinklydink.launcher.DesktopWidget %%*
) > "%INSTALL_DIR%\OinklyDink.bat"

REM Also create a separate bat for the config UI
(
echo @echo off
echo java -jar "%INSTALL_DIR%\%JAR_NAME%" %%*
) > "%INSTALL_DIR%\OinklyDink-Config.bat"
echo    Created OinklyDink.bat (animated widget)
echo    Created OinklyDink-Config.bat (configuration UI)
echo.

REM --- Step 4: Export pig tail icon ---
echo [4/5] Exporting pig tail icon...

if not exist "%INSTALL_DIR%\icons" mkdir "%INSTALL_DIR%\icons"
java -cp "%INSTALL_DIR%\%JAR_NAME%" com.oinklydink.launcher.IcoExporter "%INSTALL_DIR%\icons" >nul 2>&1

if exist "%INSTALL_DIR%\icons\oinklydink.ico" (
    echo    Pig tail icon exported.
) else (
    echo    WARNING: Icon export failed. Shortcuts will use default icon.
)

set "ICO_PATH=%INSTALL_DIR%\icons\oinklydink.ico"
echo.

REM --- Step 5: Shortcuts ---
echo [5/5] Creating shortcuts...

REM Desktop shortcut via PowerShell (with pig tail icon)
REM Using a temp .ps1 to avoid CMD parenthesis parsing issues
set "PS_SCRIPT=%TEMP%\oinklydink-shortcut.ps1"
>"%PS_SCRIPT%" echo $ws = New-Object -ComObject WScript.Shell
>>"%PS_SCRIPT%" echo $desktop = [Environment]::GetFolderPath('Desktop')
>>"%PS_SCRIPT%" echo $sc = $ws.CreateShortcut("$desktop\Dink 5.lnk")
>>"%PS_SCRIPT%" echo $sc.TargetPath = '%INSTALL_DIR%\OinklyDink.bat'
>>"%PS_SCRIPT%" echo $sc.WorkingDirectory = '%INSTALL_DIR%'
>>"%PS_SCRIPT%" echo $sc.IconLocation = '%ICO_PATH%,0'
>>"%PS_SCRIPT%" echo $sc.Description = 'Pigs Tail Java Launcher'
>>"%PS_SCRIPT%" echo $sc.Save()
powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" 2>nul
if errorlevel 1 (echo    Note: Could not create desktop shortcut automatically.) else (echo    Desktop shortcut created.)

REM Start Menu shortcut (with pig tail icon)
set "STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\OinklyDink"
if not exist "%STARTMENU%" mkdir "%STARTMENU%"
>"%PS_SCRIPT%" echo $ws = New-Object -ComObject WScript.Shell
>>"%PS_SCRIPT%" echo $sc = $ws.CreateShortcut('%STARTMENU%\Dink 5.lnk')
>>"%PS_SCRIPT%" echo $sc.TargetPath = '%INSTALL_DIR%\OinklyDink.bat'
>>"%PS_SCRIPT%" echo $sc.WorkingDirectory = '%INSTALL_DIR%'
>>"%PS_SCRIPT%" echo $sc.IconLocation = '%ICO_PATH%,0'
>>"%PS_SCRIPT%" echo $sc.Description = 'Pigs Tail Java Launcher'
>>"%PS_SCRIPT%" echo $sc.Save()
powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%" 2>nul
echo    Start Menu entry created.

del "%PS_SCRIPT%" 2>nul
echo.

REM --- Done ---
echo   ========================================
echo     Installation complete!
echo.
echo     Location: %INSTALL_DIR%
echo     Launch:   Double-click desktop icon
echo              or Start Menu ^> OinklyDink
echo   ========================================
echo.

REM Create uninstaller
(
echo @echo off
echo echo Uninstalling OinklyDink...
echo rd /s /q "%INSTALL_DIR%"
echo rd /s /q "%STARTMENU%"
echo del /q "%USERPROFILE%\Desktop\Dink 5.lnk" 2^>nul
echo echo Done.
echo pause
) > "%INSTALL_DIR%\uninstall.bat"

set /p LAUNCH="Launch OinklyDink now? [Y/n] "
if /i not "%LAUNCH%"=="n" (
    echo Launching...
    start "" "%INSTALL_DIR%\OinklyDink.bat"
)
