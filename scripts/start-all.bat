@echo off
setlocal
set "RENTDB_PAUSE_AT_END=%RENTDB_NO_PAUSE%"
set "RENTDB_NO_PAUSE=1"

echo ============================================================
echo  전체 시작: DB → 백엔드 → 프론트
echo ============================================================
call "%~dp0backend-start.bat"
call "%~dp0frontend-start.bat"

echo.
echo  모두 시작했습니다. 브라우저에서 http://localhost:5173 을 여세요.
if not defined RENTDB_PAUSE_AT_END pause
endlocal
