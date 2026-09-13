-- ============================================================
-- V3: 관리자 계정 관리 작업을 접근 기록에 남길 수 있도록 action 값 추가
--   ADMIN_CREATE     관리자 추가
--   ADMIN_UPDATE     이름 변경·활성화·비활성화
--   PASSWORD_CHANGE  본인 비밀번호 변경
--   PASSWORD_RESET   다른 관리자 비밀번호 재설정
-- ============================================================

ALTER TABLE admin_access_log DROP CONSTRAINT IF EXISTS admin_access_log_action_check;

ALTER TABLE admin_access_log ADD CONSTRAINT admin_access_log_action_check
    CHECK (action IN ('LOGIN', 'LOGIN_FAIL', 'LEAD_VIEW', 'LEAD_EXPORT',
                      'ADMIN_CREATE', 'ADMIN_UPDATE', 'PASSWORD_CHANGE', 'PASSWORD_RESET'));
