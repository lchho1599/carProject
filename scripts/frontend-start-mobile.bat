@echo off
chcp 65001 >nul
setlocal
set "RENTDB_PAUSE_AT_END=%RENTDB_NO_PAUSE%"
set "RENTDB_NO_PAUSE=1"

echo ============================================================
echo  모바일 테스트용 프론트 재기동 (같은 Wi-Fi 에서 접속 허용)
echo ============================================================
set "DEV_LAN=true"
call "%~dp0frontend-restart.bat"

echo.
echo  휴대폰을 PC 와 같은 Wi-Fi 에 연결한 뒤 아래 주소로 접속하세요.
rem 파이프가 들어간 명령은 for /f 안에서 깨지기 쉬워 별도 스크립트로 IP 를 구한다
for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0_lan-ip.ps1"`) do (
    echo      사용자 화면 : http://%%i:5173
    echo      관리자 화면 : http://%%i:5173/admin
)
echo.
echo  접속이 안 되면 Windows 방화벽에서 Node.js 허용 여부를 확인하세요.
echo  평소 실행(이 PC 전용)으로 되돌리려면 frontend-restart.bat 을 실행하세요.

if not defined RENTDB_PAUSE_AT_END pause
endlocal
