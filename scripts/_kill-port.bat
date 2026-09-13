@echo off
rem 사용법: call _kill-port.bat 포트번호
rem 해당 포트를 LISTEN 중인 프로세스(와 하위 프로세스)를 종료한다.
set "KILL_PORT=%~1"
set "KILLED="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%KILL_PORT% .*LISTENING"') do (
    if not "%%p"=="0" (
        taskkill /PID %%p /T /F >nul 2>&1
        set "KILLED=1"
    )
)
if defined KILLED (exit /b 0) else (exit /b 1)
