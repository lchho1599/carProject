@echo off
setlocal
set "RENTDB_PAUSE_AT_END=%RENTDB_NO_PAUSE%"
set "RENTDB_NO_PAUSE=1"

echo ============================================================
echo  전체 중지: 프론트 → 백엔드 → DB
echo ============================================================
call "%~dp0frontend-stop.bat"
call "%~dp0backend-stop.bat"
call "%~dp0db-stop.bat"

if not defined RENTDB_PAUSE_AT_END pause
endlocal
