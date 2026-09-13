@echo off
rem Docker 엔진이 꺼져 있으면 Docker Desktop을 실행하고 준비될 때까지 기다린다.
docker info >nul 2>&1
if not errorlevel 1 exit /b 0

echo [Docker] Docker Desktop 을 시작합니다...
if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
    start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
) else (
    echo [오류] Docker Desktop 을 찾을 수 없습니다. 설치 여부를 확인하세요.
    exit /b 1
)

set /a WAIT=0
:wait_docker
ping -n 3 127.0.0.1 >nul
docker info >nul 2>&1
if not errorlevel 1 (
    echo [Docker] 준비 완료
    exit /b 0
)
set /a WAIT+=2
if %WAIT% GEQ 120 (
    echo [오류] 2분 안에 Docker 가 시작되지 않았습니다. Docker Desktop 화면을 확인하세요.
    exit /b 1
)
goto wait_docker
