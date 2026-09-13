@echo off
setlocal
set "RENTDB_PAUSE_AT_END=%RENTDB_NO_PAUSE%"
set "RENTDB_NO_PAUSE=1"

call "%~dp0backend-stop.bat"
ping -n 3 127.0.0.1 >nul
call "%~dp0backend-start.bat"

if not defined RENTDB_PAUSE_AT_END pause
endlocal
