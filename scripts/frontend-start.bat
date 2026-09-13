@echo off
setlocal
call "%~dp0_common.bat"

netstat -ano | findstr /r /c:":%FRONTEND_PORT% .*LISTENING" >nul
if not errorlevel 1 (
    echo [프론트] 이미 %FRONTEND_PORT% 포트에서 실행 중입니다. 재기동은 frontend-restart.bat 을 사용하세요.
    goto end
)

if not exist "%ROOT%\frontend\node_modules" (
    echo [프론트] 처음 실행이라 npm install 을 진행합니다...
    pushd "%ROOT%\frontend"
    call npm install || (popd & echo [오류] npm install 실패 & goto end)
    popd
)

echo [프론트] Vite 개발 서버를 새 창(%FRONTEND_TITLE%)에서 시작합니다...
start "%FRONTEND_TITLE%" /D "%ROOT%\frontend" cmd /k npm run dev

set /a WAIT=0
:wait_frontend
ping -n 2 127.0.0.1 >nul
netstat -ano | findstr /r /c:":%FRONTEND_PORT% .*LISTENING" >nul
if not errorlevel 1 goto ready
set /a WAIT+=1
if %WAIT% GEQ 60 (
    echo [오류] 1분 안에 프론트가 시작되지 않았습니다. "%FRONTEND_TITLE%" 창의 로그를 확인하세요.
    goto end
)
goto wait_frontend

:ready
echo [프론트] 실행 완료
echo      - 사용자 화면 : http://localhost:%FRONTEND_PORT%
echo      - 관리자 화면 : http://localhost:%FRONTEND_PORT%/admin

:end
if not defined RENTDB_NO_PAUSE pause
endlocal
