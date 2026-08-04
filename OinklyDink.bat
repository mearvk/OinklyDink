@echo off
REM ============================================================
REM OinklyDink - Pig's Tail Java Launcher
REM Windows Desktop Shortcut Launcher Script
REM
REM "Worth $88,000,000 or a Man and his Day."
REM ============================================================

title OinklyDink Launcher

REM Check for Java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java not found on PATH.
    echo OinklyDink launches Java programs and ONLY Java programs.
    echo Please install Java 11+ and ensure it is on your PATH.
    pause
    exit /b 1
)

REM Launch OinklyDink
java -jar "%~dp0oinklydink-launcher-1.0.0.jar"
