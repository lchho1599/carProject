@echo off
setlocal
set "RENTDB_PAUSE_AT_END=%RENTDB_NO_PAUSE%"
set "RENTDB_NO_PAUSE=1"

call "%~dp0frontend-stop.bat"
ping -n 2 127.0.0.1 >nul
call "%~dp0frontend-start.bat"

if not defined RENTDB_PAUSE_AT_END pause
endlocal
