@echo off
setlocal
call "%~dp0_common.bat"

echo ============================================================
echo  실행 상태
echo ============================================================

docker info >nul 2>&1
if errorlevel 1 (
    echo [Docker ] 꺼짐
) else (
    echo [Docker ] 실행 중
    docker compose -f "%ROOT%\docker-compose.yml" --env-file "%ROOT%\.env" ps --format "           {{.Name}}  {{.Status}}"
)

set "STATE=중지"
curl -s -o nul -w "%%{http_code}" http://localhost:%BACKEND_PORT%/actuator/health 2>nul | findstr "200" >nul && set "STATE=실행 중 (http://localhost:%BACKEND_PORT%)"
echo [백엔드 ] %STATE%

set "STATE=중지"
netstat -ano | findstr /r /c:":%FRONTEND_PORT% .*LISTENING" >nul && set "STATE=실행 중 (http://localhost:%FRONTEND_PORT%)"
echo [프론트 ] %STATE%

if not defined RENTDB_NO_PAUSE pause
endlocal
