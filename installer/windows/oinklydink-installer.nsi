; OinklyDink - NSIS Windows Installer Script
; Produces a native Windows installer (.exe) with Start Menu shortcuts,
; uninstaller, and Add/Remove Programs integration.
;
; Requirements: NSIS 3.x (https://nsis.sourceforge.io)
; Build: makensis oinklydink-installer.nsi
;
; Place oinklydink-launcher-1.0.0.jar in the same directory before building.

!define APP_NAME "OinklyDink"
!define APP_VERSION "1.0.0"
!define APP_PUBLISHER "OinklyDink"
!define APP_DESCRIPTION "Pig's Tail Java Launcher"
!define JAR_FILE "oinklydink-launcher-${APP_VERSION}.jar"
!define INSTALL_SIZE 200 ; KB estimate

Name "${APP_NAME} ${APP_VERSION}"
OutFile "OinklyDink-Setup-${APP_VERSION}.exe"
InstallDir "$LOCALAPPDATA\${APP_NAME}"
InstallDirRegKey HKCU "Software\${APP_NAME}" "InstallDir"
RequestExecutionLevel user

; UI
!include "MUI2.nsh"

!define MUI_ABORTWARNING
!define MUI_WELCOMEPAGE_TITLE "Install ${APP_NAME}"
!define MUI_WELCOMEPAGE_TEXT "This will install ${APP_NAME} - the Pig's Tail Java Launcher.$\r$\n$\r$\nLaunches Java programs. Only Java programs.$\r$\n$\r$\nWorth $$88,000,000 or a Man and his Day."

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

; Installer Section
Section "Install"
    SetOutPath "$INSTDIR"

    ; Copy files
    File "${JAR_FILE}"
    
    ; Create launcher batch file
    FileOpen $0 "$INSTDIR\OinklyDink.bat" w
    FileWrite $0 '@echo off$\r$\n'
    FileWrite $0 'title OinklyDink$\r$\n'
    FileWrite $0 'java -jar "$INSTDIR\${JAR_FILE}" %*$\r$\n'
    FileClose $0

    ; Create Start Menu shortcuts
    CreateDirectory "$SMPROGRAMS\${APP_NAME}"
    CreateShortcut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" \
        "$INSTDIR\OinklyDink.bat" "" "" "" SW_SHOWMINIMIZED "" "${APP_DESCRIPTION}"
    CreateShortcut "$SMPROGRAMS\${APP_NAME}\Uninstall.lnk" \
        "$INSTDIR\uninstall.exe"

    ; Desktop shortcut
    CreateShortcut "$DESKTOP\${APP_NAME}.lnk" \
        "$INSTDIR\OinklyDink.bat" "" "" "" SW_SHOWMINIMIZED "" "${APP_DESCRIPTION}"

    ; Write uninstaller
    WriteUninstaller "$INSTDIR\uninstall.exe"

    ; Add/Remove Programs registry
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
        "DisplayName" "${APP_NAME} - ${APP_DESCRIPTION}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
        "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
        "DisplayVersion" "${APP_VERSION}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
        "Publisher" "${APP_PUBLISHER}"
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
        "EstimatedSize" ${INSTALL_SIZE}
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
        "InstallLocation" "$INSTDIR"

    ; Save install dir
    WriteRegStr HKCU "Software\${APP_NAME}" "InstallDir" "$INSTDIR"
SectionEnd

; Uninstaller Section
Section "Uninstall"
    ; Remove files
    Delete "$INSTDIR\${JAR_FILE}"
    Delete "$INSTDIR\OinklyDink.bat"
    Delete "$INSTDIR\uninstall.exe"
    RMDir "$INSTDIR"

    ; Remove shortcuts
    Delete "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk"
    Delete "$SMPROGRAMS\${APP_NAME}\Uninstall.lnk"
    RMDir "$SMPROGRAMS\${APP_NAME}"
    Delete "$DESKTOP\${APP_NAME}.lnk"

    ; Remove registry
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
    DeleteRegKey HKCU "Software\${APP_NAME}"
SectionEnd
