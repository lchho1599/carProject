@echo off
setlocal
call "%~dp0_common.bat"

echo ============================================================
echo  [주의] 로컬 DB 의 모든 데이터를 삭제하고 새로 만듭니다.
echo         (로컬 개발용 DB 만 해당, 운영 DB 와는 무관)
echo ============================================================
set "CONFIRM="
set /p CONFIRM=계속하려면 RESET 을 입력하세요:
if /i not "%CONFIRM%"=="RESET" (
    echo 취소했습니다.
    goto end
)

call "%~dp0_ensure-docker.bat" || goto end
docker compose -f "%ROOT%\docker-compose.yml" --env-file "%ROOT%\.env" down -v
docker compose -f "%ROOT%\docker-compose.yml" --env-file "%ROOT%\.env" up -d --wait --quiet-pull
echo [DB] 초기화 완료 — 백엔드를 시작하면 Flyway 가 테이블을 다시 만듭니다.

:end
if not defined RENTDB_NO_PAUSE pause
endlocal
