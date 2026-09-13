-- ============================================================
-- V2: 관리자 로그인 세션 저장소 (Spring Session JDBC 표준 스키마, PostgreSQL)
-- 서버가 재시작되거나 여러 대로 늘어나도 로그인 상태를 유지한다.
-- ============================================================

CREATE TABLE spring_session (
    primary_id             char(36)     NOT NULL,
    session_id             char(36)     NOT NULL,
    creation_time          bigint       NOT NULL,
    last_access_time       bigint       NOT NULL,
    max_inactive_interval  integer      NOT NULL,
    expiry_time            bigint       NOT NULL,
    principal_name         varchar(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX spring_session_ix1 ON spring_session (session_id);
CREATE INDEX spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE spring_session_attributes (
    session_primary_id  char(36)      NOT NULL,
    attribute_name      varchar(200)  NOT NULL,
    attribute_bytes     bytea         NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id)
        REFERENCES spring_session (primary_id) ON DELETE CASCADE
);
