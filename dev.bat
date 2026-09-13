@echo off
setlocal
chcp 65001 >nul
set "RENTDB_NO_PAUSE=1"
set "S=%~dp0scripts"

:menu
cls
echo ============================================================
echo   렌트DB 로컬 개발 도구
echo ============================================================
echo   [전체]   1. 전체 시작      2. 전체 중지      3. 전체 재기동
echo   [백엔드] 4. 시작           5. 중지           6. 재기동
echo   [프론트] 7. 시작           8. 중지           9. 재기동
echo   [DB]     10. 시작          11. 중지          12. 초기화(데이터 삭제)
echo   [기타]   13. 실행 상태     14. 메일함 열기   15. 사이트 열기
echo            16. 모바일 테스트용 프론트 재기동 (같은 Wi-Fi 접속 허용)
echo            0. 종료
echo ============================================================
set "CHOICE="
set /p CHOICE=번호를 입력하세요:

if "%CHOICE%"=="1"  call "%S%\start-all.bat"
if "%CHOICE%"=="2"  call "%S%\stop-all.bat"
if "%CHOICE%"=="3"  call "%S%\restart-all.bat"
if "%CHOICE%"=="4"  call "%S%\backend-start.bat"
if "%CHOICE%"=="5"  call "%S%\backend-stop.bat"
if "%CHOICE%"=="6"  call "%S%\backend-restart.bat"
if "%CHOICE%"=="7"  call "%S%\frontend-start.bat"
if "%CHOICE%"=="8"  call "%S%\frontend-stop.bat"
if "%CHOICE%"=="9"  call "%S%\frontend-restart.bat"
if "%CHOICE%"=="10" call "%S%\db-start.bat"
if "%CHOICE%"=="11" call "%S%\db-stop.bat"
if "%CHOICE%"=="12" call "%S%\db-reset.bat"
if "%CHOICE%"=="13" call "%S%\status.bat"
if "%CHOICE%"=="14" start "" http://localhost:8025
if "%CHOICE%"=="15" start "" http://localhost:5173
if "%CHOICE%"=="16" call "%S%\frontend-start-mobile.bat"
if "%CHOICE%"=="0"  goto :eof

echo.
pause
goto menu
