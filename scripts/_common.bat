@echo off
rem ============================================================
rem  공통 설정 (다른 스크립트에서 call 로 불러 사용)
rem ============================================================
chcp 65001 >nul
for %%i in ("%~dp0..") do set "ROOT=%%~fi"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=5173"
set "BACKEND_TITLE=rentdb-backend"
set "FRONTEND_TITLE=rentdb-frontend"

if not exist "%ROOT%\.env" (
    copy "%ROOT%\.env.example" "%ROOT%\.env" >nul
    echo [안내] .env 파일이 없어 .env.example 을 복사했습니다.
)
exit /b 0
