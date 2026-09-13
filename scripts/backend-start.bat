@echo off
setlocal
call "%~dp0_common.bat"

netstat -ano | findstr /r /c:":%BACKEND_PORT% .*LISTENING" >nul
if not errorlevel 1 (
    echo [백엔드] 이미 %BACKEND_PORT% 포트에서 실행 중입니다. 재기동은 backend-restart.bat 을 사용하세요.
    goto end
)

rem 백엔드는 DB 가 필요하므로 DB 컨테이너를 먼저 확인한다.
set "RENTDB_NO_PAUSE_PREV=%RENTDB_NO_PAUSE%"
set "RENTDB_NO_PAUSE=1"
call "%~dp0db-start.bat" || goto end
set "RENTDB_NO_PAUSE=%RENTDB_NO_PAUSE_PREV%"

echo [백엔드] Spring Boot 를 새 창(%BACKEND_TITLE%)에서 시작합니다...
rem gradlew.bat 은 전체 경로로 실행한다 (현재 폴더 실행 파일 검색이 꺼진 환경 대비)
start "%BACKEND_TITLE%" /D "%ROOT%\backend" cmd /k call "%ROOT%\backend\gradlew.bat" bootRun --no-daemon

echo [백엔드] 기동 대기 중 (최초 실행은 라이브러리 다운로드로 수 분 걸릴 수 있음)...
set /a WAIT=0
:wait_backend
ping -n 3 127.0.0.1 >nul
curl -s -o nul -w "%%{http_code}" http://localhost:%BACKEND_PORT%/actuator/health 2>nul | findstr "200" >nul
if not errorlevel 1 goto ready
set /a WAIT+=2
if %WAIT% GEQ 300 (
    echo [오류] 5분 안에 백엔드가 응답하지 않았습니다. "%BACKEND_TITLE%" 창의 로그를 확인하세요.
    goto end
)
goto wait_backend

:ready
echo [백엔드] 실행 완료
echo      - API     : http://localhost:%BACKEND_PORT%/api/ping
echo      - 상태확인 : http://localhost:%BACKEND_PORT%/actuator/health

:end
if not defined RENTDB_NO_PAUSE pause
endlocal
