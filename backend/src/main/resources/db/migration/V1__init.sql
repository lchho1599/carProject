-- ============================================================
-- V1: 초기 스키마 (설계 문서 §8)
-- 규칙: snake_case, bigint identity PK, timestamptz, 금액 bigint(원), enum은 varchar + CHECK
-- ============================================================

-- ------------------------------------------------------------
-- 차량 마스터: 브랜드 > 모델 > 트림, 모델별 옵션·색상
-- ------------------------------------------------------------
CREATE TABLE brand (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        varchar(50)  NOT NULL,
    origin      varchar(20)  NOT NULL CHECK (origin IN ('DOMESTIC', 'IMPORTED')),
    logo_url    varchar(500),
    sort_order  integer      NOT NULL DEFAULT 0,
    is_active   boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uk_brand_name UNIQUE (name)
);

CREATE TABLE vehicle_model (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    brand_id    bigint       NOT NULL REFERENCES brand (id),
    name        varchar(100) NOT NULL,
    segment     varchar(30),                                  -- 차급 (경차, 준중형, 중형 ...)
    body_type   varchar(20)  NOT NULL CHECK (body_type IN ('SEDAN', 'SUV', 'VAN', 'TRUCK', 'ETC')),
    fuel        varchar(20)  NOT NULL CHECK (fuel IN ('GASOLINE', 'DIESEL', 'HYBRID', 'EV', 'LPG')),
    image_url   varchar(500),
    sort_order  integer      NOT NULL DEFAULT 0,
    is_active   boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uk_vehicle_model_brand_name UNIQUE (brand_id, name)
);
CREATE INDEX idx_vehicle_model_brand ON vehicle_model (brand_id);

CREATE TABLE vehicle_trim (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    model_id    bigint       NOT NULL REFERENCES vehicle_model (id),
    name        varchar(200) NOT NULL,                        -- 연식·엔진·세부모델명
    price       bigint       NOT NULL CHECK (price >= 0),
    sort_order  integer      NOT NULL DEFAULT 0,
    is_active   boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX idx_vehicle_trim_model ON vehicle_trim (model_id);

CREATE TABLE model_option (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    model_id    bigint       NOT NULL REFERENCES vehicle_model (id),
    name        varchar(200) NOT NULL,
    price       bigint       NOT NULL CHECK (price >= 0),
    sort_order  integer      NOT NULL DEFAULT 0,
    is_active   boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX idx_model_option_model ON model_option (model_id);

CREATE TABLE model_color (
    id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    model_id     bigint       NOT NULL REFERENCES vehicle_model (id),
    name         varchar(100) NOT NULL,
    hex_code     varchar(7),
    extra_price  bigint       NOT NULL DEFAULT 0 CHECK (extra_price >= 0),
    sort_order   integer      NOT NULL DEFAULT 0,
    is_active    boolean      NOT NULL DEFAULT true,
    created_at   timestamptz  NOT NULL DEFAULT now(),
    updated_at   timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX idx_model_color_model ON model_color (model_id);

-- ------------------------------------------------------------
-- 상품: 특가, 즉시출고 재고, 배너
-- ------------------------------------------------------------
CREATE TABLE deal (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type              varchar(20)  NOT NULL CHECK (type IN ('TIME_SALE', 'NO_DEPOSIT')),
    trim_id           bigint       NOT NULL REFERENCES vehicle_trim (id),
    title             varchar(200) NOT NULL,
    badge             varchar(30),
    original_monthly  bigint       CHECK (original_monthly >= 0),     -- 정가 월납입료 (취소선)
    monthly_price     bigint       NOT NULL CHECK (monthly_price >= 0),
    lease_monthly     bigint       CHECK (lease_monthly >= 0),
    period_months     integer      NOT NULL CHECK (period_months IN (24, 36, 48, 60)),
    deposit_rate      integer      NOT NULL DEFAULT 0 CHECK (deposit_rate BETWEEN 0 AND 100),
    prepay_rate       integer      NOT NULL DEFAULT 0 CHECK (prepay_rate BETWEEN 0 AND 100),
    starts_at         timestamptz  NOT NULL DEFAULT now(),
    ends_at           timestamptz,
    sort_order        integer      NOT NULL DEFAULT 0,
    is_published      boolean      NOT NULL DEFAULT false,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_deal_period CHECK (ends_at IS NULL OR ends_at > starts_at)
);
CREATE INDEX idx_deal_listing ON deal (type, is_published, starts_at, ends_at);

CREATE TABLE instant_stock (
    id              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trim_id         bigint       NOT NULL REFERENCES vehicle_trim (id),
    exterior_color  varchar(100) NOT NULL,
    interior_color  varchar(100),
    options_text    varchar(500),
    vehicle_price   bigint       NOT NULL CHECK (vehicle_price >= 0),
    monthly_price   bigint       NOT NULL CHECK (monthly_price >= 0),
    condition_text  varchar(100) NOT NULL,                    -- 예: 48개월 / 선납금 30% 기준
    badge           varchar(30),
    status          varchar(20)  NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD')),
    sort_order      integer      NOT NULL DEFAULT 0,
    is_published    boolean      NOT NULL DEFAULT false,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX idx_instant_stock_listing ON instant_stock (is_published, status);

CREATE TABLE banner (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title             varchar(200) NOT NULL,
    image_pc_url      varchar(500) NOT NULL,
    image_mobile_url  varchar(500) NOT NULL,
    link_url          varchar(500),
    starts_at         timestamptz  NOT NULL DEFAULT now(),
    ends_at           timestamptz,
    sort_order        integer      NOT NULL DEFAULT 0,
    is_published      boolean      NOT NULL DEFAULT false,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz  NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- 관리자
-- ------------------------------------------------------------
CREATE TABLE admin_user (
    id             bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          varchar(200) NOT NULL,
    password_hash  varchar(100) NOT NULL,
    name           varchar(50)  NOT NULL,
    is_active      boolean      NOT NULL DEFAULT true,
    last_login_at  timestamptz,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uk_admin_user_email UNIQUE (email)
);

-- ------------------------------------------------------------
-- 상담 신청(리드)
-- ------------------------------------------------------------
CREATE TABLE lead (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type              varchar(20)   NOT NULL CHECK (type IN ('QUICK', 'ESTIMATE', 'DEAL', 'INSTANT')),
    name              varchar(50)   NOT NULL,
    phone             varchar(20)   NOT NULL,                  -- 숫자만 저장 (01012345678)
    brand_id          bigint        REFERENCES brand (id) ON DELETE SET NULL,
    model_id          bigint        REFERENCES vehicle_model (id) ON DELETE SET NULL,
    trim_id           bigint        REFERENCES vehicle_trim (id) ON DELETE SET NULL,
    deal_id           bigint        REFERENCES deal (id) ON DELETE SET NULL,
    instant_stock_id  bigint        REFERENCES instant_stock (id) ON DELETE SET NULL,
    vehicle_snapshot  jsonb,                                   -- 신청 시점 차량·가격 복사본
    conditions        jsonb,                                   -- 이용방법·기간·보증금·선납·보험연령·주행거리·신용도
    total_price       bigint        CHECK (total_price >= 0),
    agree_privacy     boolean       NOT NULL CHECK (agree_privacy),
    agree_marketing   boolean       NOT NULL DEFAULT false,
    agreed_at         timestamptz   NOT NULL,
    source_url        varchar(1000),
    utm               jsonb,
    ip_hash           varchar(64),
    user_agent        varchar(500),
    status            varchar(20)   NOT NULL DEFAULT 'NEW'
                      CHECK (status IN ('NEW', 'IN_PROGRESS', 'CONTRACTED', 'NO_ANSWER', 'CANCELED')),
    created_at        timestamptz   NOT NULL DEFAULT now(),
    updated_at        timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX idx_lead_created ON lead (created_at DESC);
CREATE INDEX idx_lead_status_created ON lead (status, created_at DESC);
CREATE INDEX idx_lead_phone_created ON lead (phone, created_at DESC);

CREATE TABLE lead_note (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lead_id     bigint        NOT NULL REFERENCES lead (id) ON DELETE CASCADE,
    admin_id    bigint        REFERENCES admin_user (id) ON DELETE SET NULL,
    content     varchar(2000) NOT NULL,
    created_at  timestamptz   NOT NULL DEFAULT now(),
    updated_at  timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX idx_lead_note_lead ON lead_note (lead_id);

-- ------------------------------------------------------------
-- 기록: 관리자 개인정보 접근, 알림 발송
-- ------------------------------------------------------------
CREATE TABLE admin_access_log (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_id    bigint       REFERENCES admin_user (id) ON DELETE SET NULL,
    action      varchar(30)  NOT NULL CHECK (action IN ('LOGIN', 'LOGIN_FAIL', 'LEAD_VIEW', 'LEAD_EXPORT')),
    target_id   bigint,
    ip_hash     varchar(64),
    created_at  timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX idx_admin_access_log_created ON admin_access_log (created_at DESC);

CREATE TABLE notification_log (
    id             bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lead_id        bigint        REFERENCES lead (id) ON DELETE SET NULL,
    channel        varchar(20)   NOT NULL CHECK (channel IN ('EMAIL')),
    recipient      varchar(200)  NOT NULL,
    status         varchar(20)   NOT NULL CHECK (status IN ('SENT', 'FAILED')),
    error_message  varchar(1000),
    created_at     timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_log_lead ON notification_log (lead_id);

-- ------------------------------------------------------------
-- 설정
-- ------------------------------------------------------------
CREATE TABLE app_setting (
    setting_key  varchar(100) PRIMARY KEY,
    value        jsonb        NOT NULL,
    created_at   timestamptz  NOT NULL DEFAULT now(),
    updated_at   timestamptz  NOT NULL DEFAULT now()
);

INSERT INTO app_setting (setting_key, value) VALUES ('notify_emails', '[]'::jsonb);
