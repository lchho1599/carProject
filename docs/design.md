# 신차 장기렌트·리스 상담 DB 사이트 — 설계 문서 v1.0

> 상태: **확정** (2026-09-13) — 변경 시 하단 변경 이력에 기록
> 사이트명: 임시 사이트명 사용, 설정값으로 분리 (§9.3)
> 벤치마킹: m.ks-rentcar.com — 구조·기능만 참고하고 문구·이미지·브랜드는 사용하지 않는다.

---

## 1. 목표

신차 장기렌트·리스 상품(특가, 즉시출고)을 보여주고 방문자의 **상담 신청(리드)** 을 모은다. 신청이 들어오면 운영자에게 이메일로 알리고, 관리자 화면에서 리드와 상품을 관리한다.

- 온라인 결제·계약 기능은 없다. 최종 견적은 상담사가 전화로 안내한다.
- 월 납입료는 **자동 계산하지 않는다.** 관리자가 상품별로 입력한 대표 조건 금액만 노출한다.
- 주 유입 경로는 **광고**다. 자연 검색(SEO)은 추후 과제로 둔다.

## 2. 범위 (MVP)

| 구분 | 포함 | 제외 (추후) |
|---|---|---|
| 사용자 | 메인, 간편견적(2단계), 특가 목록, 즉시출고 목록, 상담신청 폼, 약관·개인정보처리방침 | 회사소개, 후기, 이벤트, 승계, 콘텐츠, 카카오 상담 |
| 관리자 | 로그인, 리드 관리, 차량 마스터, 특가·즉시출고 상품, 배너, 알림 메일 설정 | 상담사 배정, UTM 분석, 권한 세분화, 엑셀 업로드, 비밀번호 재설정 메일 |
| 알림 | 신규 리드 → **운영자 메일** (이름·연락처 마스킹 + 관리자 링크) | 딜러 발송, 문자, 알림톡, 슬랙 |
| 데이터 | 샘플 시드 데이터 + 관리자 직접 등록 | 외부 차량 DB 연동, 월 납입료 계산기 |
| 광고 | 상담신청 완료 시 전환 이벤트를 보낼 자리(훅)만 마련 | GA4·구글·네이버·메타 광고 스크립트 연동 |

## 3. 확정된 결정 사항

| 영역 | 결정 |
|---|---|
| 프론트엔드 | **일반 SPA**: React 19 + TypeScript + Vite 8 + **React Router 8 (라이브러리 모드)** |
| UI | Tailwind CSS 4 + shadcn/ui, 자체 디자인 시스템 |
| 프론트 라이브러리 | TanStack Query(서버 데이터), React Hook Form + Zod(폼 검증) |
| 백엔드 | Java 21, **Spring Boot 4.1**, Gradle |
| 로컬 실행 | 루트 `dev.bat` 메뉴 + `scripts\*.bat` (시작·중지·재기동·상태) |
| DB 접근 | Spring Data JPA + QueryDSL, Flyway(스키마 이력) |
| DB | **PostgreSQL** |
| 관리자 인증 | Spring Security **세션 쿠키**, 세션은 DB 저장(Spring Session JDBC) |
| API 문서 | springdoc(Swagger UI), 운영 환경에서는 비활성화 |
| 프론트 배포 | **Cloudflare Pages** |
| 백엔드 배포 | **GCP 관리형**: Cloud Run(최소 1대 상시) + Cloud SQL for PostgreSQL, 서울 리전 |
| 이미지 저장소 | Cloudflare R2 (로컬 개발은 로컬 파일 저장) |
| 메일 | Gmail SMTP로 시작, 발송 수단을 교체할 수 있게 인터페이스로 분리 |
| 코드 저장소 | GitHub 모노레포 (frontend/, backend/, docs/) |
| 자동 배포 | GitHub Actions |
| 운영 환경 | 로컬 + 운영 1개 (테스트 서버는 추후) |

## 4. 비기능 요구사항

- **반응형:** 모바일(360px~)과 PC(1280px~)를 한 코드베이스로 지원한다. 모바일은 하단 고정 CTA 바, PC는 상단 GNB와 우측 플로팅 상담 버튼을 쓴다.
- **광고 랜딩 성능:**
  - 모바일 LCP 2.5초 이내
  - 라우트별 코드 분할
  - 이미지는 WebP·지연 로딩
  - 초기 JS 번들 최소화
- **공유 미리보기:** `index.html`에 사이트 공통 title·description·OG 이미지를 넣는다.
- **개인정보:**
  - 필수·선택 동의 여부와 동의 시각을 저장한다.
  - 관리자 목록과 알림 메일에서는 이름·연락처를 **마스킹**하고, 관리자 상세 화면에서만 전체를 보여준다.
  - 리드 상세 조회와 CSV 다운로드는 **접근 기록**을 남긴다.
  - 보관 기간은 신청일로부터 1년이며, 파기 배치는 추후 구현한다.
  - IP는 원문이 아닌 해시로 저장한다.
- **스팸 방지:** 서버 측 검증, 허니팟 필드, 같은 연락처로 10분 안에 다시 신청하면 차단(DB 기준), IP당 요청 제한.
- **보안:**
  - 비밀번호는 BCrypt로 해시한다.
  - 세션 쿠키는 HttpOnly·Secure·SameSite=Lax로 설정한다.
  - 관리자 변경 요청에는 CSRF 토큰을 적용한다.
  - CORS는 허용 도메인만 연다.
  - 비밀값은 GCP Secret Manager에 두고 코드에 넣지 않는다.
- **이식성:** 백엔드는 Docker 이미지로 빌드한다. 환경별 설정은 전부 환경변수로 받는다.

## 5. 시스템 구성

```
                    ┌──────────────── Cloudflare ────────────────┐
 사용자/관리자 ──▶  │ example.com      → Pages (React SPA)        │
 (모바일·PC)        │ img.example.com  → R2 (이미지)              │
                    │ api.example.com  → DNS/프록시 ─────────────┼──┐
                    └────────────────────────────────────────────┘  │
                                                                     ▼
                    ┌──────────── GCP (asia-northeast3 서울) ───────────┐
                    │ Cloud Run: Spring Boot (min 1, CPU 상시 할당)       │
                    │   ├─▶ Cloud SQL PostgreSQL (자동 백업)              │
                    │   ├─▶ Secret Manager (DB·SMTP·R2 비밀값)            │
                    │   ├─▶ R2 (S3 호환 API로 이미지 업로드)              │
                    │   └─▶ Gmail SMTP (587) → 운영자 메일                │
                    │ Artifact Registry: 백엔드 Docker 이미지             │
                    └────────────────────────────────────────────────────┘
```

- `example.com`과 `api.example.com`은 같은 사이트(same-site)로 취급된다. 그래서 SameSite=Lax 세션 쿠키가 정상적으로 전달된다. 프론트는 `credentials: 'include'`로 요청한다.
- **Cloud Run CPU 상시 할당:** 응답을 보낸 뒤 비동기로 보내는 메일이 끊기지 않게 CPU를 상시 할당한다.
- **로컬 개발:** Vite 개발 서버가 `/api`를 `localhost:8080`으로 프록시한다(CORS 불필요). DB는 로컬 PostgreSQL을 쓰고, 메일은 콘솔 로그 발송기로 대체한다.

## 6. 사이트맵

```
[사용자]
/                         메인
/estimate                 간편견적 1단계 — 국산/수입 → 브랜드 → 모델
/estimate/:modelId        간편견적 2단계 — 트림·색상·옵션·이용조건 → 신청
/deals?type=time|nodeposit 특가 목록 (탭: 타임특가 | 무보증특가)
/instant                  즉시출고 목록 (브랜드 탭, 차종 필터)
/complete                 신청 완료
/terms, /privacy          약관, 개인정보처리방침
(공통) 빠른상담 모달

[관리자]  — 로그인 필요, 검색엔진 차단(noindex)
/admin/login
/admin                    대시보드 (오늘·이번주 리드 수, 상태별 건수, 최근 리드)
/admin/leads              리드 목록
/admin/leads/:id          리드 상세 / 상태 변경 / 메모
/admin/vehicles           브랜드 > 모델 > 트림·옵션·색상
/admin/deals              특가 상품
/admin/instant            즉시출고 재고
/admin/banners            메인 배너
/admin/settings           알림 수신 메일, 관리자 계정
```

## 7. 화면 설계

### 7.1 메인 `/`
1. 헤더: 로고, (PC) GNB, 전화 버튼
2. 히어로 배너 슬라이드 (관리자 등록, PC·모바일 이미지 별도, 링크 가능)
3. **빠른견적 폼:** 브랜드 → 모델 → 계약기간 → 이름 → 연락처 → 동의 → 신청
4. 타임특가 섹션: 모바일은 가로 스크롤, PC는 4열 그리드, 전체보기 버튼
5. 무보증특가 섹션
6. 즉시출고 섹션
7. 이용 절차 3단계 안내 (정적)
8. 푸터: 사업자 정보(설정값), 약관 링크
9. (모바일) 하단 고정 바: 간편견적 / 특가 / 즉시출고 / 상담신청

### 7.2 간편견적
- **1단계:** 국산/수입 탭 → 브랜드 로고 그리드 → 모델 카드 → 다음
- **2단계:**
  - 차량 이미지와 기본 정보(차급, 연료)
  - 트림 선택 (가격 표시)
  - 외장색상 선택 (추가금 표시)
  - 옵션 다중 선택 (가격 표시)
  - 이용조건: 이용방법(렌트/리스), 기간(36/48/60), 보증금(0/10/20/30/40%), 선납금(0/10/20/30/40%), 보험연령(21/26), 연 주행거리(1/2/3만km, 무제한), 신용도(700 미만/이상/모름)
  - **합계 차량가** = 트림가 + 색상 추가금 + 옵션 합계 (화면에서 계산하고 서버에서 다시 계산해 저장)
  - 이름·연락처·동의 → "견적 상담 신청" → `/complete`
- **진입 경로:** 특가 카드에서 들어오면 `?trimId=`로 트림이 미리 선택된다.

**구현 메모 (6단계 반영)**
- **1단계 `/estimate`:** 국산차·수입차 탭 → 브랜드 버튼(로고가 없으면 첫 글자) → 모델 카드(최저 트림가 "~만원부터")를 고르면 `/estimate/{modelId}`로 이동한다. 선택 상태는 주소 `?origin=IMPORTED&brand=`에 남긴다.
- **2단계 `/estimate/{modelId}`**
  - 선택 항목: 세부모델(라디오), 외장색상(색 원형 버튼, "미정" 선택 가능), 옵션(체크박스), 이용조건 7종(칸 모양 선택 버튼)
  - 기본 조건: 장기렌트 · 48개월 · 보증금 0% · 선납금 30% · 만 26세 · 연 2만km · 신용도 모름
  - 합계: 트림가 + 색상 추가금 + 옵션가를 화면에서 바로 계산해 보여준다. 신청할 때 금액은 보내지 않고, 서버가 다시 계산한다.
  - 특가 카드에서 `?trimId=`로 들어오면 그 트림을 미리 선택하고, 이 모델의 트림이 아니면 첫 트림을 선택한다.
  - PC는 오른쪽에 견적 요약과 신청 폼을 고정해 두고, 모바일은 하단 메뉴 위에 합계와 "견적 신청" 바로가기 바를 둔다.
  - 없는 모델이면 "차량 다시 선택하기" 안내를 보여준다.

### 7.3 특가 목록 `/deals`
- 탭: 타임특가 / 무보증특가
- 카드: 배지, 브랜드·모델, 트림 설명, 정가 월납입료(취소선) → 할인 월납입료, 조건(기간/보증금/선납), CTA
- CTA: 타임특가는 간편견적 2단계(트림 선택된 상태)로, 무보증특가는 상담 모달(상품 정보 채워짐)로 연결
- 노출 기간 밖이거나 비공개인 상품은 서버에서 제외한다.

### 7.4 즉시출고 `/instant`
- 브랜드 탭(전체/국산 브랜드별/수입), 차종 필터(SUV/세단·소형/전기차/승합), "총 N대 대기중"
- 카드: 배지, 모델·트림, 외장·내장색, 추가옵션, 차량가, 월납입료 + 조건 문구, CTA "상담신청"(모달)
- 판매완료·비공개 상품은 제외한다. 예약 상품은 "예약중" 배지를 달고 노출한다.

### 7.5 상담신청 폼 (공통 컴포넌트)
- 필드: 이름(필수, 2~20자), 연락처(필수, `01[016789]` 휴대폰 형식), 필수 동의(개인정보 수집·이용, [보기] 모달), 선택 동의(마케팅 수신)
- 숨은 값: 신청 유형, 차량·상품 참조 ID, 이용조건, 유입 페이지 URL, UTM 파라미터, 허니팟
- UTM은 첫 방문 시 `sessionStorage`에 저장했다가 신청할 때 함께 보낸다.
- 신청 성공 시 `trackLeadSubmitted(type)` 훅을 호출하고(현재는 빈 함수) 완료 화면으로 이동한다.

### 7.6 관리자
- **대시보드:** 오늘·이번주·이번달 리드 수, 상태별 건수, 최근 리드 10건
- **리드 목록:**
  - 필터: 기간, 유형, 상태, 검색(이름·연락처 뒷자리)
  - 컬럼: 신청일시, 유형, 차량, 이름(마스킹), 연락처(마스킹), 상태
  - 20건 페이지네이션
  - CSV 다운로드 (현재 필터 기준, 접근 기록 남김)
- **리드 상세:**
  - 전체 신청 내용(차량 스냅샷, 이용조건, 합계), 동의 내역, 유입 URL·UTM
  - 상태 변경: 신규 → 상담중 → 계약완료 / 부재 / 취소
  - 메모 작성·이력 (작성자, 시각)
- **차량 마스터:** 브랜드 CRUD(로고) → 모델 CRUD(이미지, 차급, 차종, 연료) → 트림 CRUD(가격). 모델마다 옵션·색상 CRUD. 삭제 대신 비활성화한다.
- **특가:** 유형, 트림 선택(브랜드 → 모델 → 트림), 표시 제목, 배지, 정가·할인 월납입료, 리스 월납입료(선택), 기간·보증금·선납, 노출 기간, 순서, 공개 여부
- **즉시출고:** 트림 선택, 외장·내장색, 옵션 텍스트, 차량가, 월납입료, 조건 문구, 배지, 재고 상태(판매중/예약/판매완료), 순서, 공개 여부
- **배너:** 제목, PC·모바일 이미지, 링크, 노출 기간, 순서, 공개 여부
- **설정:** 알림 수신 메일 목록, 관리자 계정 추가·비활성화, 내 비밀번호 변경

## 8. 데이터 모델 (PostgreSQL)

**공통 규칙**
- 테이블·컬럼 이름은 snake_case로 쓴다.
- PK는 `bigint generated always as identity`로 만든다.
- 모든 테이블에 `created_at`, `updated_at` (`timestamptz`)을 둔다.
- 금액은 `bigint` (원 단위)로 저장한다.
- enum은 `varchar` + CHECK 제약으로 만든다 (JPA `@Enumerated(STRING)`).
- 차량 마스터는 삭제하지 않고 `is_active=false`로 숨긴다.

```
brand           id, name, origin(DOMESTIC|IMPORTED), logo_url, sort_order, is_active
vehicle_model   id, brand_id→brand, name, segment, body_type(SEDAN|SUV|VAN|TRUCK|ETC),
                fuel(GASOLINE|DIESEL|HYBRID|EV|LPG), image_url, sort_order, is_active
vehicle_trim    id, model_id→vehicle_model, name, price, sort_order, is_active
model_option    id, model_id→model, name, price, sort_order, is_active
model_color     id, model_id→model, name, hex_code, extra_price, sort_order, is_active

deal            id, type(TIME_SALE|NO_DEPOSIT), trim_id→trim, title, badge,
                original_monthly, monthly_price, lease_monthly NULL,
                period_months, deposit_rate, prepay_rate,
                starts_at, ends_at NULL, sort_order, is_published

instant_stock   id, trim_id→trim, exterior_color, interior_color, options_text,
                vehicle_price, monthly_price, condition_text, badge,
                status(AVAILABLE|RESERVED|SOLD), sort_order, is_published

banner          id, title, image_pc_url, image_mobile_url, link_url NULL,
                starts_at, ends_at NULL, sort_order, is_published

lead            id, type(QUICK|ESTIMATE|DEAL|INSTANT),
                name, phone,
                brand_id NULL, model_id NULL, trim_id NULL, deal_id NULL, instant_stock_id NULL,
                vehicle_snapshot jsonb,     -- 신청 시점 브랜드·모델·트림·색상·옵션·가격
                conditions jsonb,           -- 이용방법·기간·보증금·선납·보험연령·주행거리·신용도
                total_price NULL,
                agree_privacy, agree_marketing, agreed_at,
                source_url, utm jsonb, ip_hash, user_agent,
                status(NEW|IN_PROGRESS|CONTRACTED|NO_ANSWER|CANCELED)

lead_note       id, lead_id→lead, admin_id→admin_user, content
admin_user      id, email UNIQUE, password_hash, name, is_active, last_login_at
admin_access_log id, admin_id, action(LEAD_VIEW|LEAD_EXPORT|LOGIN|LOGIN_FAIL),
                target_id NULL, ip_hash, created_at
notification_log id, lead_id, channel(EMAIL), recipient, status(SENT|FAILED), error_message NULL
app_setting     key PK, value jsonb        -- notify_emails 등
spring_session / spring_session_attributes  -- Spring Session JDBC 표준 스키마 (7단계에서 추가)
```

**구현 메모 (1단계 반영)**
- 테이블 이름 `trim`은 SQL 함수 이름과 겹쳐 `vehicle_trim`으로, `model`은 `vehicle_model`로 정했다. 참조 컬럼 이름은 `trim_id`, `model_id`를 유지한다.
- 즉시출고 "전기차" 필터는 차종(body_type)이 아니라 연료(`fuel = EV`)로 거른다.
- 비율 컬럼은 `integer`, 긴 글은 `varchar(2000)`으로 둔다(Hibernate 스키마 검증과 타입 일치).
- 설정 테이블의 키 컬럼 이름은 `setting_key`로 한다(`key`는 SQL 예약어).
- 마이그레이션 파일 위치
  - `db/migration/V1__init.sql`: 스키마. 모든 환경에서 실행한다.
  - `db/seed/V1_1__sample_data.sql`: 샘플 데이터. local 프로필에서만 실행하고 운영에는 들어가지 않는다.

**인덱스**
- `lead (created_at desc)`
- `lead (status, created_at desc)`
- `lead (phone, created_at desc)` — 중복 신청 확인, 검색에 사용
- `deal (type, is_published, starts_at, ends_at)`
- `instant_stock (is_published, status)`
- `model (brand_id)`
- `trim (model_id)`

**설계 원칙**
- 리드는 신청 시점의 차량·가격을 `vehicle_snapshot`에 복사해 둔다. 상품이 바뀌어도 신청 내용은 그대로 남는다.
- 금액은 클라이언트가 보낸 값을 믿지 않는다. 서버가 참조 ID로 다시 조회해서 계산한다.
- 옵션·색상은 모델 단위로 둔다. 트림별로 옵션을 나누는 건 추후 확장한다.

## 9. 백엔드 설계

### 9.1 패키지 구조
```
backend/src/main/java/com/rentdb/
├─ RentDbApplication.java
├─ global/
│  ├─ config/        (Web, CORS, QueryDSL, Async, Jackson, OpenAPI)
│  ├─ security/      (SecurityConfig, 로그인 핸들러, 접근 기록)
│  ├─ error/         (ErrorCode, BusinessException, GlobalExceptionHandler)
│  ├─ common/        (BaseTimeEntity, PageResponse, Masking 유틸)
│  └─ ratelimit/     (IP 요청 제한 필터)
├─ vehicle/          (brand, model, trim, option, color — controller/service/repository/domain/dto)
├─ deal/
├─ instant/
├─ banner/
├─ lead/             (신청, 스냅샷 생성, 중복 확인, 관리자 조회·상태·메모·CSV)
├─ notification/     (MailSender 인터페이스, SmtpMailSender, LogMailSender, 템플릿)
├─ storage/          (FileStorage 인터페이스, R2FileStorage, LocalFileStorage)
├─ admin/            (admin_user, 인증, 대시보드, 접근 기록)
└─ setting/
backend/src/main/resources/
├─ application.yml, application-local.yml, application-prod.yml   (접속 정보는 환경변수로만 주입)
├─ db/migration/  V1__init.sql, V2__spring_session.sql, R__seed_sample.sql(local 전용)
└─ templates/mail/ lead-notification.html
backend/Dockerfile    (멀티 스테이지 빌드 → JRE 21 실행 이미지, Cloud Run·VM 공용)
```

**교체 가능한 구현 (프로필별)**

| 인터페이스 | local | prod |
|---|---|---|
| `MailSender` | `SmtpMailSender` → Mailpit(1025), 테스트는 `LogMailSender` | `SmtpMailSender` → Gmail SMTP (추후 발송 서비스 구현 추가) |
| `FileStorage` | `LocalFileStorage` → 프로젝트 `uploads/` 폴더 | `R2FileStorage` → Cloudflare R2 |

### 9.2 API 규칙
- 공개 API는 `/api/...`, 관리자 API는 `/api/admin/...` 경로를 쓴다. 관리자 API는 세션 인증이 필요하다.
- 요청·응답은 JSON(camelCase)으로 주고받는다. 날짜는 ISO-8601(KST 오프셋 포함) 형식이다.
- 목록 응답 형식: `{ content, page, size, totalElements, totalPages }`
- 오류 응답 형식: `{ code, message, fieldErrors: [{ field, message }] }` (예: `LEAD_DUPLICATED`, `VALIDATION_FAILED`)

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/brands?origin=` | 활성 브랜드 |
| GET | `/api/brands/{id}/models` | 활성 모델 |
| GET | `/api/models/{id}` | 모델 상세(트림·옵션·색상) |
| GET | `/api/deals?type=` | 노출 중인 특가 |
| GET | `/api/instant?brandId=&origin=&bodyType=` | 노출 중인 즉시출고 + 총 대수 |
| GET | `/api/banners` | 노출 중인 배너 |
| GET | `/api/site` | 사이트 공개 설정(사이트명, 대표번호, 사업자 정보) |
| POST | `/api/leads` | 상담 신청 |
| POST | `/api/admin/auth/login`, `/logout` · GET `/me` | 관리자 인증 |
| GET | `/api/admin/dashboard` | 대시보드 통계 |
| GET | `/api/admin/leads?from=&to=&type=&status=&q=&page=` | 리드 목록(마스킹) |
| GET · PATCH | `/api/admin/leads/{id}` | 상세(전체 정보, 접근 기록) · 상태 변경 |
| POST | `/api/admin/leads/{id}/notes` | 메모 추가 |
| GET | `/api/admin/leads/export.csv` | CSV (접근 기록) |
| CRUD | `/api/admin/{brands,models,trims,model-options,model-colors,deals,instant,banners}` | 관리 |
| POST | `/api/admin/uploads` | 이미지 업로드 (jpg/png/webp, 5MB 이하) → URL 반환 |
| GET · PUT | `/api/admin/settings` | 알림 메일 등 설정 |
| CRUD | `/api/admin/users` | 관리자 계정 |

### 9.3 설정값 (환경변수)
| 키 | 설명 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | local / prod |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL |
| `CORS_ALLOWED_ORIGINS` | 프론트 도메인 |
| `ADMIN_BASE_URL` | 알림 메일의 관리자 링크 도메인 |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP |
| `STORAGE_TYPE` (local/r2), `R2_ENDPOINT`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_BUCKET`, `R2_PUBLIC_BASE_URL` | 이미지 |
| `INITIAL_ADMIN_EMAIL`, `INITIAL_ADMIN_PASSWORD` | 관리자가 한 명도 없을 때 첫 계정 생성 |
| `IP_HASH_SALT` | IP 해시용 |
| `SITE_NAME`, `SITE_PHONE`, `SITE_BUSINESS_INFO` | 사이트 공개 정보 (임시값) |

프론트: `VITE_API_BASE_URL`, `VITE_SITE_NAME`

### 9.4 상담 신청 처리 흐름 `POST /api/leads`
1. Bean Validation으로 검증한다. 필수 동의가 없거나 형식이 틀리면 400을 반환한다.
2. 허니팟 필드에 값이 있으면 저장하지 않고 성공 응답만 보낸다(봇이 알아채지 못하게).
3. IP 요청 제한을 확인한다(인스턴스 메모리 기준). 같은 연락처로 10분 안에 신청한 기록이 DB에 있으면 409 `LEAD_DUPLICATED`를 반환한다.
4. 유형별로 필요한 참조 ID를 검증하고 서버에서 다시 조회해 `vehicle_snapshot`과 `total_price`를 만든다.
5. Lead를 저장하고 커밋한다.
6. 커밋 후 이벤트(`@TransactionalEventListener(AFTER_COMMIT)` + `@Async`)로 운영자 메일을 발송하고 `notification_log`에 결과를 기록한다. 발송이 실패해도 신청은 성공이다.
7. 201 `{ id }`를 반환한다.

**구현 메모 (3단계 반영)**
- **요청 형식:** 동의 값은 `Boolean`으로 받는다. Jackson 3은 JSON에 없는 `boolean`을 오류로 처리하기 때문이다. `agreePrivacy`는 true가 아니면 400, `agreeMarketing`은 생략하면 미동의로 저장한다.
- **유형별 필수 값**
  - ESTIMATE: `trimId`. 선택 값 `colorId`, `optionIds`는 같은 모델 소속만 허용한다.
  - DEAL: `dealId`. 노출 중인 특가만 허용한다.
  - INSTANT: `instantStockId`. 판매완료·비공개 차량은 거부한다.
  - QUICK: 필수 값 없음. `modelId`나 `brandId`를 보내면 함께 저장한다.
- **이용조건 `annualMileage`:** 10000/20000/30000km이고, **0은 무제한**을 뜻한다.
- **동시 신청 대응:** 같은 연락처로 동시에 들어온 신청(더블 클릭 등)은 PostgreSQL 트랜잭션 잠금(`pg_advisory_xact_lock`)으로 순서대로 처리한 뒤 중복을 확인한다.
- **IP 확인 순서:** `CF-Connecting-IP` → `X-Forwarded-For` 첫 값 → 접속 주소. 헤더는 위조될 수 있으므로 요청 제한 용도로만 쓴다.
- **설정값:** `app.lead.*`(중복 허용 시간 10분, IP당 10분에 10회), `app.notification.*`(`mail-type` smtp/log, `from`, `admin-base-url`), `app.security.ip-hash-salt`

### 9.5 알림 메일
- 수신자: `app_setting.notify_emails`
- 제목: `[신규 상담] {유형} · {차량명} · {마스킹 이름}`
- 본문: 유형, 차량, 조건 요약, 신청일시, 마스킹 이름(홍*동)·연락처(010-****-5678), `{ADMIN_BASE_URL}/admin/leads/{id}` 링크
- 전체 이름·연락처는 메일에 넣지 않는다.

## 10. 프론트엔드 설계

### 10.1 폴더 구조
```
frontend/
├─ src/
│  ├─ main.tsx                     (RouterProvider)
│  ├─ app/router.tsx               (createBrowserRouter — 라우트 목록)
│  ├─ pages/
│  │  ├─ public/  HomePage, EstimatePage, EstimateDetailPage, DealsPage, InstantPage, CompletePage, TermsPage, PrivacyPage
│  │  └─ admin/   LoginPage, DashboardPage, LeadsPage, LeadDetailPage, VehiclesPage, DealsPage, InstantPage, BannersPage, SettingsPage
│  ├─ components/
│  │  ├─ ui/        (shadcn/ui)
│  │  ├─ layout/    (PublicLayout, AdminLayout(인증 가드), Header, Footer, MobileBottomBar, FloatingConsult)
│  │  └─ lead/      (LeadForm, ConsultModal, AgreementModal)
│  ├─ features/     (vehicle, deal, instant, banner, lead, admin — api 훅·타입·컴포넌트)
│  ├─ lib/          (apiClient, queryClient, format(원·월), mask, utm, tracking)
│  └─ config/site.ts
├─ public/          (favicon, og-image, _redirects — Cloudflare Pages SPA 새로고침 대응)
├─ vite.config.ts   (/api → localhost:8080 프록시)
└─ index.html       (공통 meta·OG)
```

### 10.2 디자인 시스템 (자체)
- **색상 토큰:** primary(신뢰감 있는 네이비 계열), accent(CTA·할인가 강조), 회색 스케일, 성공/경고/오류
- **글꼴:** Pretendard (웹폰트, 시스템 폰트 폴백)
- **브레이크포인트:** `sm 640 / md 768 / lg 1024 / xl 1280`, 모바일 우선
- **공통 컴포넌트:** Button, Badge, PriceText(취소선·강조), VehicleCard, Tabs, Chip 선택, Modal/Sheet(모바일은 하단 시트), Toast, Skeleton
- **관리자:** shadcn/ui 기본 테마 + DataTable, Form, Dialog

**구현 메모 (4단계 반영)**
- **UI 부품:** 사용자 화면은 shadcn/ui 없이 Tailwind로 직접 만든 컴포넌트를 쓴다(`components/ui`: Button, Badge, Modal, Spinner, icons). 부품 수가 적고, 네이비·주황 색상 토큰과 충돌하지 않게 하기 위해서다. 관리자 화면의 shadcn 도입 여부는 7단계에서 정한다.
- **색상 토큰(`index.css` @theme)**
  - `primary`: 네이비 #1e3a5f, 단계 50~900
  - `accent`: 주황 #ff6b2c, 단계 50~700
  - 글꼴: Pretendard Variable
- **상담 모달:** `ConsultModalProvider`가 `PublicLayout`에서 감싼다. 어느 화면에서든 `useConsultModal().openConsult({ type, dealId, label ... })`로 연다. 모바일은 하단 시트, PC는 가운데 창이다.
- **LeadForm:** 입력은 이름·연락처·동의만 받고, 차량·조건은 `context`로 받는다. 서버 필드 오류는 해당 입력칸에, 그 밖의 오류(409 등)는 폼 상단에 표시한다. 한 화면에 폼이 여러 개 있어도 되도록 입력칸 id는 `useId`로 만든다.
- **테스트:** `npm test`(Vitest + Testing Library, jsdom)

**구현 메모 (5단계 반영)**
- **메인:** 배너 슬라이더(5초 자동 전환, 마우스를 올리면 멈춤)와 빠른 견적 문의(브랜드→모델→계약기간 + 연락처)를 PC에서는 나란히, 모바일에서는 위아래로 둔다. 그 아래 타임특가·무보증특가·즉시출고 섹션(각 최대 8개, 모바일은 가로 스크롤)과 이용 절차 3단계를 둔다.
- **배너 이미지:** 이미지가 없거나 불러오지 못하면 제목 문구를 넣은 기본 그라데이션 배너로 대신 보여준다. 차량 이미지가 없으면 "이미지 준비 중" 자리표시를 보여준다.
- **특가 카드**
  - 타임특가: "견적 확인"(`/estimate/{modelId}?trimId=`) + "바로 상담"(DEAL 상담 모달)
  - 무보증특가: "간편 상담신청"만 둔다.
  - 종료일이 있으면 D-day를 표시하고, 한국 날짜 기준으로 계산한다.
- **특가 목록:** 탭 상태는 주소 `?type=time|nodeposit`에 둔다. 광고를 특정 탭에 바로 연결하기 위해서다.
- **즉시출고 목록:** 브랜드 탭(전체 / 국산 브랜드 / 수입)과 차종 필터(SUV·세단·승합·전기차)를 둔다. 선택 상태는 주소 `?brand=&kind=`에 남긴다. 예약중 차량은 흐리게 표시하고 "대기 상담신청" 버튼을 보여준다.

## 11. 로컬 개발 환경

| 도구 | 상태 | 비고 |
|---|---|---|
| JDK 21 | 설치됨 | |
| Node.js (v24) / npm | 설치됨 | |
| Gradle | 불필요 | 프로젝트에 포함되는 Gradle Wrapper 사용 |
| **Git** | **설치 필요** | GitHub 저장소·자동 배포에 필요 |
| **Docker Desktop (+ WSL2)** | **설치 필요** | 로컬 DB·메일 확인 도구 실행, 백엔드 이미지 확인 |
| PostgreSQL 18 (설치판) | 설치됨, **이 프로젝트에서는 사용 안 함** | 5432 포트 유지. 프로젝트 DB는 Docker로 5433 포트 사용 |

**로컬 컨테이너 (`docker-compose.yml`)**

| 서비스 | 포트 | 용도 |
|---|---|---|
| PostgreSQL 17 (운영 Cloud SQL과 같은 메이저 버전으로 유지) | **5433** → 컨테이너 5432 | 프로젝트 DB `rentdb`, 계정 `rentdb` 자동 생성 |
| Mailpit | 1025(SMTP), 8025(웹 화면) | 로컬 알림 메일 확인 |

- 설치판 PostgreSQL(5432)과 헷갈리지 않도록 `application-local.yml`의 DB URL을 `localhost:5433`으로 고정한다. README에 "프로젝트 DB는 5433"을 명시한다.
- 로컬 메일은 로그 발송기 대신 Mailpit SMTP로 보낸다.

## 11-1. 보안 구현 (7단계 반영)

**필터 체인 (`global/security/SecurityConfig`)** — 요청 경로별로 체인을 나눈다.

| 순서 | 체인 | 대상 | 규칙 |
|---|---|---|---|
| ① | 관리자 | `/api/admin/**` | ROLE_ADMIN 필요, 세션(Spring Session JDBC), CSRF(세션 저장·`X-CSRF-TOKEN` 헤더), JSON 로그인 필터, 로그아웃, CSP·`X-Robots-Tag: noindex` |
| ② | 공개 | `/api/**`, `/actuator/**`, API 문서, `/error` | **허용 목록에 적은 API만** 익명 허용(GET 조회 8개, POST `/api/leads`, health/info), 나머지 차단. 세션 미사용, CSRF 미적용(쿠키 인증 없음) |
| ③ | 기본 | 그 밖의 모든 경로 | 전부 차단 |

**직접 만든 필터·핸들러**

| 구성 요소 | 역할 |
|---|---|
| `JsonLoginFilter` | `POST /api/admin/auth/login {email, password}` 처리. 성공하면 **세션 ID 교체**(세션 고정 공격 방지)와 **CSRF 토큰 재발급**을 하고, 인증 정보를 세션에 저장한다 |
| `AdminLoginHandlers` | 성공 시 `{id, email, name, csrfToken}`을 응답하고 마지막 로그인 시각과 LOGIN 기록을 남긴다. 실패 시 계정 존재 여부와 상관없이 같은 메시지(401 `LOGIN_FAILED`)를 주고 LOGIN_FAIL 기록을 남긴다 |
| `RateLimitFilter` | IP 해시당 요청 제한. 상담 신청은 10분에 10회, **관리자 로그인도 10분에 10회**(비밀번호 대입 공격 방지). 초과하면 429를 준다. 공개·관리자 체인 안에서만 동작하고 서블릿 필터로는 중복 등록하지 않는다 |
| `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler` | 401 `UNAUTHORIZED`, 403 `FORBIDDEN`, 403 `CSRF_TOKEN_INVALID`를 JSON으로 응답한다 |
| `AdminPrincipal` | 세션에 저장되는 관리자 정보. 인증 후 비밀번호 해시를 지운다 |
| `SessionCookieConfig` | 세션 쿠키 `RENTDB_SESSION`: HttpOnly, SameSite=Lax, 운영은 Secure. Boot 4에서는 `server.servlet.session.cookie.*`가 적용되지 않아 직접 설정한다 |

**CSRF 토큰 전달 방식:** 프론트(example.com)와 API(api.example.com)는 도메인이 달라 쿠키로 토큰을 읽을 수 없다. 그래서 로그인 응답과 `GET /api/admin/auth/me` 응답 본문으로 토큰을 주고, 프론트는 토큰을 메모리에만 두었다가 변경 요청마다 `X-CSRF-TOKEN` 헤더로 보낸다.

**첫 관리자 계정:** 관리자가 한 명도 없으면 서버 시작 시 `INITIAL_ADMIN_EMAIL`, `INITIAL_ADMIN_PASSWORD`(8자 이상) 값으로 한 번만 만든다. 비밀번호는 BCrypt(`{bcrypt}`)로 저장하고 로그에 남기지 않는다.

**관리자 API (7단계)**
- `GET /api/admin/auth/me`: 로그인 정보와 CSRF 토큰
- `GET /api/admin/dashboard`: 대시보드
- `GET /api/admin/leads`: 목록. 마스킹하고, 필터 from·to(한국 날짜)·type·status·q(숫자면 연락처, 그 외는 이름)를 지원한다
- `GET /api/admin/leads/{id}`: 상세. 전체 정보를 주고 LEAD_VIEW 기록을 남긴다
- `PATCH /api/admin/leads/{id}`: 상태 변경
- `POST /api/admin/leads/{id}/notes`: 메모 추가
- `GET /api/admin/leads/export.csv`: UTF-8 BOM, 수식 주입 방지, 최대 1만 건, LEAD_EXPORT 기록

**동적 검색은 QueryDSL을 쓴다 (2026-09-13 전환)**
- **라이브러리:** `io.github.openfeign.querydsl` 7.6. 원조 `com.querydsl`은 2024년 1월(5.1.0) 이후 갱신이 없어, 관리 중인 OpenFeign 포크를 쓴다. Spring Boot 4 + Hibernate 7에서 Q클래스 생성과 쿼리 동작을 확인했다.
- **구조:** `LeadRepository extends JpaRepository, LeadRepositoryCustom`이다. 구현은 `LeadRepositoryCustomImpl`(JPAQueryFactory)에 둔다. 조건 메서드가 null을 돌려주면 where 절에서 빠진다.
- **검색 동작:** `contains`는 검색어의 `%`·`_`를 자동으로 이스케이프한다(`like ? escape '!'`). `AdminLeadApiTest`에서 검증했다.
- **UI:** 관리자 화면은 shadcn/ui 대신 자체 컴포넌트를 쓴다.

## 11-2. 관리자 상품 관리 (8단계 반영)

**API** (모두 `/api/admin/**`, 로그인·CSRF 필요)

| 대상 | 기능 | 비고 |
|---|---|---|
| 브랜드 | 목록 / 등록 / 수정 | 이름 중복 409 `DUPLICATED` |
| 모델 | 브랜드별 목록 / 등록 / 상세(트림·옵션·색상) / 수정 | 같은 브랜드 안에서 이름 중복 불가, 브랜드 변경 불가 |
| 트림·옵션·색상 | 모델별 등록 / 수정 | 색상 코드는 `#RRGGBB`(대문자로 저장) |
| 특가 | 목록(유형 필터) / 상세 / 등록 / 수정 / **삭제** | 기간 24·36·48·60개월만, 종료일은 시작일보다 뒤. 노출 상태(VISIBLE·SCHEDULED·ENDED·HIDDEN)를 계산해 준다 |
| 즉시출고 | 목록(상태 필터) / 상세 / 등록 / 수정 / **삭제** | 판매가 끝난 차량은 삭제보다 "판매완료" 처리를 권장한다 |
| 배너 | 목록 / 상세 / 등록 / 수정 / **삭제** | 노출 상태를 계산해 준다 |
| 이미지 | `POST /api/admin/uploads` (multipart `file`) | JPG·PNG·WEBP, 5MB 이하 |

- **삭제 정책:** 차량 마스터(브랜드·모델·트림·옵션·색상)는 삭제하지 않고 `active=false`로 숨긴다. 특가·즉시출고·배너는 삭제할 수 있다. 삭제해도 해당 상담 신청의 차량 스냅샷은 그대로 남는다(참조만 NULL).
- **URL 검증(`@PublicUrl`):** 링크·이미지 주소는 `/`로 시작하는 사이트 경로나 `https://`만 허용한다. `javascript:`, `//외부`, `http://`는 거부해서, 저장된 주소가 화면에서 실행되는 공격을 막는다.
- **이미지 업로드 보안**
  - 파일 이름과 브라우저가 알려준 형식은 무시하고, 파일 앞부분(시그니처)으로 형식을 판별한다.
  - 파일 이름은 서버가 `yyyy/MM/{UUID}.{확장자}`로 새로 만든다.
  - 공개 주소 `GET /api/files/{yyyy}/{MM}/{UUID}.{ext}`는 이 형식만 허용하고, 저장 폴더 밖 경로는 차단한다. 1년 캐시를 건다.
- **저장소:** `FileStorage` 인터페이스로 분리했다. 로컬은 `LocalFileStorage`(`app.storage.local-dir`, 기본 `./uploads`)를 쓴다. **Cloudflare R2 구현은 배포 단계에서 추가한다.**
- **샘플 배너 이미지:** 직접 제작한 이미지(그라데이션 배경, 문구, 단순 차량 그림)를 `frontend/public/images/sample/banner-{1..3}-{pc,mobile}.jpg`에 둔다. PC는 1800×600, 모바일은 1080×608이다.

**관리자 화면**
- `/admin/vehicles`: 브랜드 목록(추가·수정 창)과 선택한 브랜드의 모델 목록
- `/admin/vehicles/models/:id|new`: 모델 기본 정보(대표 이미지 업로드), 트림·옵션·색상 표(줄마다 바로 수정, 맨 아래 줄에서 추가)
- `/admin/deals`, `/admin/deals/:id|new`: 특가 목록(노출 상태 배지)과 등록·수정·삭제. 차량 선택은 브랜드 → 모델 → 트림 순서다.
- `/admin/instant`, `/admin/instant/:id|new`: 즉시출고 목록(상태 필터, 화면 노출 여부 표시)과 등록·수정·삭제
- `/admin/banners`, `/admin/banners/:id|new`: 배너 카드 목록(미리보기)과 등록·수정·삭제. PC·모바일 이미지를 업로드한다.
- **날짜 입력:** 모두 한국 시간으로 입력받고 `+09:00`으로 저장한다.
- **남은 메뉴:** "설정"(알림 수신 메일, 관리자 계정)은 기능 개발 마지막에 구현한다.

## 11-3. 관리자 계정 관리

**API** (`/api/admin/users`, 로그인·CSRF 필요)

| 기능 | 요청 | 규칙 |
|---|---|---|
| 목록 | `GET /api/admin/users` | 비밀번호 해시는 응답하지 않음, 본인에 `me: true` |
| 추가 | `POST /api/admin/users {email, name, password}` | 이메일 소문자·공백 제거, 중복 409 |
| 이름 변경·활성화·비활성화 | `PUT /api/admin/users/{id} {name, active}` | **본인 비활성화 불가**, 비활성화 시 대상 세션 즉시 삭제 |
| 다른 관리자 비밀번호 재설정 | `PUT /api/admin/users/{id}/password {newPassword}` | 본인은 불가(내 비밀번호 변경 사용), 대상 세션 즉시 삭제 |
| 내 비밀번호 변경 | `PUT /api/admin/users/me/password {currentPassword, newPassword}` | 현재 비밀번호 확인, 기존과 같으면 거부, **지금 세션만 유지하고 다른 기기 세션 삭제** |

- **비밀번호 규칙 (`PasswordPolicy`, 프론트 `features/admin/password.ts`와 동일):** 8~100자, 영문과 숫자 포함, 앞뒤 공백 불가. 서버에서 반드시 다시 검사한다.
- **"활성 관리자 0명" 방지:** 로그인한 관리자는 항상 활성 상태다. 그래서 "본인 비활성화 불가" 규칙만으로 모든 관리자가 잠기는 상황을 막는다.
- **세션 강제 종료 (`AdminSessionService`):** Spring Session JDBC의 `findByPrincipalName(email)`로 세션을 찾아 삭제한다.
- **접근 기록:** `ADMIN_CREATE`, `ADMIN_UPDATE`, `PASSWORD_CHANGE`, `PASSWORD_RESET` (Flyway V3에서 허용값 추가)
- **화면:** `/admin/accounts`("관리자 계정" 메뉴). 내 비밀번호 변경 카드, 관리자 목록 표, 추가·이름 변경·비밀번호 재설정 창을 둔다. 비활성화는 확인 창을 띄운 뒤 실행한다.
- **남은 설정:** "설정" 메뉴(알림 수신 메일)는 기능 개발 맨 마지막에 구현한다.

## 12. 구현 단계

> 진행 순서 변경(2026-09-13): 보안 기반과 운영 흐름을 먼저 갖추기 위해 **7단계를 6단계보다 먼저** 진행한다. 순서: 0→1→2→3→4→5→**7→6**→8→9→10

| 단계 | 내용 | 완료 기준 |
|---|---|---|
| 0. 환경 준비 | Git·WSL2·Docker 설치 확인, 모노레포 생성, `docker-compose.yml`(PostgreSQL 5433, Mailpit), 백엔드(Spring Boot)·프론트(React Router) 뼈대, 프로필별 설정·환경변수 분리, 백엔드 Dockerfile | `docker compose up`으로 DB·Mailpit 실행, 백엔드 `/actuator/health` 응답, 프론트 첫 화면 표시 |
| 1. DB·도메인 | Flyway V1 스키마, JPA 엔티티, 샘플 시드 | 로컬 DB에 테이블·샘플 데이터 생성 |
| 2. 공개 API | 브랜드/모델/특가/즉시출고/배너/사이트 조회 | Swagger에서 조회 확인, 서비스 테스트 통과 |
| 3. 상담 신청 | `POST /api/leads`, 검증·중복·스냅샷, 메일 알림(로컬은 로그 발송기) | 신청 저장, 중복 409, 메일 로그 확인, 테스트 통과 |
| 4. 프론트 공통 | 디자인 토큰, 반응형 레이아웃, API 클라이언트, 상담신청 폼·모달 | 모바일·PC 레이아웃 확인, 폼 검증 동작 |
| 5. 사용자 화면 | 메인, 특가, 즉시출고, 완료, 약관 | 실제 API 데이터로 화면 표시, 신청 완료까지 동작 |
| 6. 간편견적 | 1·2단계, 합계 계산, 특가에서 진입 | 견적 신청 후 리드에 스냅샷·조건 저장 |
| 7. 관리자 인증·리드 | 로그인·세션·CSRF, 대시보드, 리드 목록·상세·상태·메모·CSV, 접근 기록 | 로그인 후 리드 관리 가능, 비로그인 차단 |
| 8. 관리자 상품 | 차량 마스터, 특가, 즉시출고, 배너, 이미지 업로드, 설정 | 관리자에서 등록한 상품이 사용자 화면에 노출 |
| 9. 배포 | GCP(Cloud SQL, Cloud Run, Secret Manager, Artifact Registry), Cloudflare(Pages, R2, DNS), Gmail SMTP, GitHub Actions | 운영 도메인에서 신청 → 운영자 메일 수신 |
| 10. QA | 모바일·PC 브라우저 점검, 성능(LCP), 보안 점검, 운영 문서(README) | 점검 목록 통과 |

## 12-1. 남은 작업 순서 (2026-09-13 결정)

1. **어드민·사용자 화면 수정** ← 현재 진행
2. **[TODO] 알림 수신 메일 설정** — 기능 구현의 마지막 작업
   - 관리자 "설정" 메뉴(`/admin/settings`)에서 알림 받을 운영자 메일 목록을 추가·삭제한다(`app_setting.notify_emails`).
   - 백엔드 `GET/PUT /api/admin/settings`와 메일 형식 검증을 추가하고, 변경 이력을 접근 기록에 남긴다.
   - "테스트 메일 보내기"로 로컬 Mailpit에서 확인한다.
   - **현재 상태:** 수신 메일이 비어 있어서 상담 신청이 들어와도 알림 메일은 발송되지 않는다. 신청 저장과 관리자 조회는 정상이다.
3. 9·10단계 배포·점검 — 모든 기능 개발이 끝난 뒤 진행한다.

## 13. 미결 사항 (개발과 병행)

- [ ] 도메인 구매
- [ ] GCP·Cloudflare·GitHub 계정 (사업자 명의 이메일 권장)
- [ ] 사업자 정보 (상호, 대표, 사업자번호, 주소, 대표번호)
- [ ] 약관·개인정보처리방침 최종 문구 (전문가 검토 권장)
- [ ] 정식 사이트명·로고
- [ ] 운영 주체 (직접 운영 / 인수인계) — 배포 전 결정
- [ ] 로컬 PostgreSQL 설치 방식 (Windows 설치판 / Docker)

## 변경 이력

| 버전 | 날짜 | 내용 |
|---|---|---|
| v0.1 | 2026-09-13 | 초안 (요구사항·화면·데이터 모델·API) |
| v1.0 | 2026-09-13 | 기술 스택·인프라·인증·알림 방식·구현 단계 확정 |
| v1.10 | 2026-09-13 | 관리자 계정 관리 반영: 추가·이름 변경·비활성화·비밀번호 재설정·내 비밀번호 변경, 세션 강제 종료, 접근 기록 V3 |
| v1.9 | 2026-09-13 | 8단계 반영: 관리자 차량·특가·즉시출고·배너 관리 API·화면, 이미지 업로드, URL 검증, 샘플 배너 이미지 |
| v1.8 | 2026-09-13 | 6단계 반영: 간편견적 1·2단계 화면, 기본 이용조건, 특가→견적 트림 연결 |
| v1.7 | 2026-09-13 | 동적 검색을 Specification → QueryDSL(OpenFeign 7.6)로 전환 |
| v1.6 | 2026-09-13 | 7단계 반영: Spring Security 필터 체인 3개, JSON 로그인 필터, 세션 쿠키·CSRF 방식, 로그인 시도 제한, 관리자 리드 API·화면, 구현 순서 7→6 변경 |
| v1.5 | 2026-09-13 | 5단계 반영: 메인·특가 목록·즉시출고 목록 화면 구성, 주소 기반 탭·필터, 이미지 대체 표시 |
| v1.4 | 2026-09-13 | 4단계 반영: 네이비+주황 색상 토큰, 사용자 화면 자체 UI 부품(shadcn 미사용), 상담 모달 구조, 프론트 테스트 |
| v1.3 | 2026-09-13 | 2·3단계 구현 반영: 조회 API, 상담 신청 규칙(Boolean 동의값, 유형별 필수값, 동시 신청 잠금), 알림 설정값 |
| v1.2 | 2026-09-13 | 1단계 구현 반영: vehicle_trim·vehicle_model 테이블명, body_type/fuel 분리, 샘플 데이터 위치 |
| v1.1 | 2026-09-13 | 프론트를 일반 SPA(Vite + React Router 8 라이브러리 모드)로 변경, Spring Boot 4.1 반영, Docker(5433)·Mailpit·실행 스크립트(.bat) 반영 |
