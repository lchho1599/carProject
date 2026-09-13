@echo off
setlocal
call "%~dp0_common.bat"

echo [프론트] 중지합니다...
set "STOPPED="
call "%~dp0_kill-port.bat" %FRONTEND_PORT% && set "STOPPED=1"
taskkill /FI "WINDOWTITLE eq %FRONTEND_TITLE%*" /T /F >nul 2>&1 && set "STOPPED=1"

if defined STOPPED (
    echo [프론트] 중지 완료
) else (
    echo [프론트] 실행 중인 프론트가 없습니다.
)

if not defined RENTDB_NO_PAUSE pause
endlocal
