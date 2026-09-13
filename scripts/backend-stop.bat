@echo off
setlocal
call "%~dp0_common.bat"

echo [백엔드] 중지합니다...
set "STOPPED="
call "%~dp0_kill-port.bat" %BACKEND_PORT% && set "STOPPED=1"
taskkill /FI "WINDOWTITLE eq %BACKEND_TITLE%*" /T /F >nul 2>&1 && set "STOPPED=1"

if defined STOPPED (
    echo [백엔드] 중지 완료
) else (
    echo [백엔드] 실행 중인 백엔드가 없습니다.
)

if not defined RENTDB_NO_PAUSE pause
endlocal
