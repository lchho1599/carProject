@echo off
setlocal
set "RESULT=0"
call "%~dp0_common.bat"
call "%~dp0_ensure-docker.bat" || goto fail

echo [DB] PostgreSQL(5433) / Mailpit 컨테이너를 시작합니다...
docker compose -f "%ROOT%\docker-compose.yml" --env-file "%ROOT%\.env" up -d --wait --quiet-pull || goto fail

echo [DB] 실행 완료
echo      - PostgreSQL : localhost:5433 (DB: rentdb)
echo      - Mailpit    : http://localhost:8025
goto end

:fail
echo [오류] DB 컨테이너 시작에 실패했습니다.
set "RESULT=1"

:end
if not defined RENTDB_NO_PAUSE pause
endlocal & exit /b %RESULT%
