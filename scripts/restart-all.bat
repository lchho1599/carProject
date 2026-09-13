@echo off
setlocal
set "RENTDB_PAUSE_AT_END=%RENTDB_NO_PAUSE%"
set "RENTDB_NO_PAUSE=1"

echo ============================================================
echo  전체 재기동: 백엔드·프론트 재시작 (DB 는 실행 상태 유지)
echo ============================================================
call "%~dp0frontend-stop.bat"
call "%~dp0backend-stop.bat"
ping -n 3 127.0.0.1 >nul
call "%~dp0backend-start.bat"
call "%~dp0frontend-start.bat"

if not defined RENTDB_PAUSE_AT_END pause
endlocal
