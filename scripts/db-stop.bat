@echo off
setlocal
call "%~dp0_common.bat"

docker info >nul 2>&1
if errorlevel 1 (
    echo [DB] Docker 가 실행 중이 아니므로 이미 중지된 상태입니다.
    goto end
)

echo [DB] PostgreSQL / Mailpit 컨테이너를 중지합니다. (데이터는 유지됩니다)
docker compose -f "%ROOT%\docker-compose.yml" --env-file "%ROOT%\.env" stop
echo [DB] 중지 완료

:end
if not defined RENTDB_NO_PAUSE pause
endlocal
