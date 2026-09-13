# 렌트DB(가칭) — 신차 장기렌트·리스 상담 DB 사이트

설계 문서: [docs/design.md](docs/design.md)

## 구성

| 폴더 | 내용 | 기술 |
|---|---|---|
| `backend/` | API 서버 | Java 21, Spring Boot 4.1, Gradle, JPA, Flyway |
| `frontend/` | 사용자·관리자 화면 (SPA) | React 19, Vite 8, React Router 8, TypeScript, Tailwind CSS 4 |
| `docker-compose.yml` | 로컬 DB·메일 확인 도구 | PostgreSQL 17, Mailpit |
| `scripts/` | 실행·중지·재기동 스크립트 | Windows `.bat` |

## 필요 프로그램

- JDK 21, Node.js 20 이상, Git
- Docker Desktop (WSL2)

## 실행 방법 (Windows)

루트의 **`dev.bat`** 을 더블클릭하면 메뉴가 뜬다.

```
[전체]   1. 전체 시작      2. 전체 중지      3. 전체 재기동
[백엔드] 4. 시작           5. 중지           6. 재기동
[프론트] 7. 시작           8. 중지           9. 재기동
[DB]     10. 시작          11. 중지          12. 초기화(데이터 삭제)
[기타]   13. 실행 상태     14. 메일함 열기   15. 사이트 열기
```

`scripts\` 폴더의 개별 파일을 더블클릭해도 된다.

| 스크립트 | 동작 |
|---|---|
| `start-all.bat` | DB → 백엔드 → 프론트 순서로 시작 |
| `stop-all.bat` | 프론트 → 백엔드 → DB 순서로 중지 |
| `restart-all.bat` | 백엔드·프론트 재시작 (DB 유지) |
| `backend-start.bat` / `-stop` / `-restart` | 백엔드 (필요 시 DB 자동 시작) |
| `frontend-start.bat` / `-stop` / `-restart` | 프론트 (최초 실행 시 `npm install` 자동) |
| `db-start.bat` / `db-stop.bat` | PostgreSQL·Mailpit 컨테이너 (Docker Desktop 자동 실행) |
| `db-reset.bat` | 로컬 DB 데이터 전체 삭제 후 재생성 (`RESET` 입력 필요) |
| `status.bat` | Docker·백엔드·프론트 실행 상태 |

- 백엔드·프론트는 각각 **새 창**(`rentdb-backend`, `rentdb-frontend`)에서 실행되며 로그가 그 창에 표시된다.
- 처음 실행하면 루트에 `.env` 가 `.env.example` 에서 자동 생성된다.

## 관리자 계정 만들기 (처음 한 번)

1. 루트의 `.env` 파일을 열어 아래 값을 직접 입력한다 (비밀번호 8자 이상, `.env`는 Git에 올라가지 않음).
   ```
   INITIAL_ADMIN_EMAIL=사용할 이메일
   INITIAL_ADMIN_PASSWORD=사용할 비밀번호
   INITIAL_ADMIN_NAME=표시할 이름
   ```
2. 백엔드를 재기동한다 (`dev.bat` → 6 또는 `scripts\backend-restart.bat`).
3. 관리자 계정이 하나도 없을 때만 이 값으로 계정이 만들어진다. 로그인 확인 후 `.env`의 비밀번호 줄은 지워도 된다.
4. http://localhost:5173/admin 에서 로그인한다.

## 접속 주소 (로컬)

| 대상 | 주소 |
|---|---|
| 사용자 화면 | http://localhost:5173 |
| 관리자 화면 | http://localhost:5173/admin |
| 백엔드 API | http://localhost:8080/api/ping |
| 백엔드 상태 | http://localhost:8080/actuator/health |
| 메일 확인 (Mailpit) | http://localhost:8025 |
| DB | `localhost:5433` / DB `rentdb` / 계정 `rentdb` (비밀번호는 `.env`) |

> **프로젝트 DB 포트는 5433** 이다. PC에 설치된 PostgreSQL(5432)은 이 프로젝트에서 사용하지 않는다.

## 수동 실행 (스크립트 없이)

```bash
docker compose up -d --wait
```

```bash
cd backend && ./gradlew bootRun
```

```bash
cd frontend && npm run dev
```

## 테스트·빌드

```bash
cd backend && ./gradlew test
```

```bash
cd frontend && npm run build
```

백엔드 테스트는 로컬 DB(5433)가 실행 중이어야 한다.
