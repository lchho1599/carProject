-- ============================================================
-- 로컬 개발용 샘플 데이터 (local 프로필에서만 실행, 운영 DB에는 들어가지 않음)
-- 가격·월납입료는 화면 개발용 가상 금액이다.
-- 규모: 브랜드 6, 모델 15, 트림 30, 특가 10, 즉시출고 10, 배너 3, 리드 6
-- ============================================================

-- 브랜드 ------------------------------------------------------
INSERT INTO brand (name, origin, sort_order) VALUES
    ('현대',       'DOMESTIC', 1),
    ('기아',       'DOMESTIC', 2),
    ('제네시스',   'DOMESTIC', 3),
    ('르노코리아', 'DOMESTIC', 4),
    ('BMW',        'IMPORTED', 5),
    ('벤츠',       'IMPORTED', 6);

-- 모델 --------------------------------------------------------
INSERT INTO vehicle_model (brand_id, name, segment, body_type, fuel, sort_order)
SELECT b.id, v.name, v.segment, v.body_type, v.fuel, v.sort_order
FROM (VALUES
    ('현대',       '아반떼',         '준중형', 'SEDAN', 'GASOLINE', 1),
    ('현대',       '쏘나타',         '중형',   'SEDAN', 'GASOLINE', 2),
    ('현대',       '그랜저',         '준대형', 'SEDAN', 'HYBRID',   3),
    ('현대',       '투싼',           '준중형', 'SUV',   'GASOLINE', 4),
    ('현대',       '싼타페',         '중형',   'SUV',   'HYBRID',   5),
    ('현대',       '아이오닉 5',     '준중형', 'SUV',   'EV',       6),
    ('기아',       'K5',             '중형',   'SEDAN', 'GASOLINE', 1),
    ('기아',       '스포티지',       '준중형', 'SUV',   'HYBRID',   2),
    ('기아',       '쏘렌토',         '중형',   'SUV',   'HYBRID',   3),
    ('기아',       '카니발',         '대형',   'VAN',   'HYBRID',   4),
    ('기아',       'EV3',            '소형',   'SUV',   'EV',       5),
    ('제네시스',   'G80',            '준대형', 'SEDAN', 'GASOLINE', 1),
    ('르노코리아', '그랑 콜레오스',  '중형',   'SUV',   'HYBRID',   1),
    ('BMW',        '5 Series',       '준대형', 'SEDAN', 'GASOLINE', 1),
    ('벤츠',       'E-Class',        '준대형', 'SEDAN', 'GASOLINE', 1)
) AS v(brand_name, name, segment, body_type, fuel, sort_order)
JOIN brand b ON b.name = v.brand_name;

-- 트림 (모델당 2개) -------------------------------------------
INSERT INTO vehicle_trim (model_id, name, price, sort_order)
SELECT m.id, v.name, v.price, v.sort_order
FROM (VALUES
    ('아반떼',        '2026년형 가솔린 1.6 스마트',                 21500000, 1),
    ('아반떼',        '2026년형 가솔린 1.6 인스퍼레이션',           27800000, 2),
    ('쏘나타',        '2026년형 가솔린 2.0 프리미엄',               28900000, 1),
    ('쏘나타',        '2026년형 가솔린 2.0 익스클루시브',           33500000, 2),
    ('그랜저',        '2026년형 하이브리드 1.6T 프리미엄',          44500000, 1),
    ('그랜저',        '2026년형 하이브리드 1.6T 캘리그래피',        53200000, 2),
    ('투싼',          '2026년형 가솔린 1.6T 모던 2WD',              29800000, 1),
    ('투싼',          '2026년형 가솔린 1.6T 인스퍼레이션 4WD',      37900000, 2),
    ('싼타페',        '2026년형 하이브리드 1.6T 익스클루시브 2WD',  41200000, 1),
    ('싼타페',        '2026년형 하이브리드 1.6T 캘리그래피 4WD',    50800000, 2),
    ('아이오닉 5',    '2027년형 전기 스탠다드 2WD',                 49800000, 1),
    ('아이오닉 5',    '2027년형 전기 롱레인지 AWD',                 58900000, 2),
    ('K5',            '2026년형 가솔린 2.0 스마트',                 27200000, 1),
    ('K5',            '2026년형 가솔린 2.0 시그니처',               34100000, 2),
    ('스포티지',      '2026년형 하이브리드 1.6T 프레스티지 2WD',    35300000, 1),
    ('스포티지',      '2026년형 하이브리드 1.6T 시그니처 4WD',      41800000, 2),
    ('쏘렌토',        '2026년형 하이브리드 1.6T 프레스티지 2WD',    39900000, 1),
    ('쏘렌토',        '2026년형 하이브리드 1.6T 시그니처 4WD',      48600000, 2),
    ('카니발',        '2026년형 하이브리드 1.6T 9인승 프레스티지',  40900000, 1),
    ('카니발',        '2026년형 하이브리드 1.6T 9인승 시그니처',    49700000, 2),
    ('EV3',           '2026년형 전기 스탠다드 에어',                42500000, 1),
    ('EV3',           '2026년형 전기 롱레인지 어스',                49100000, 2),
    ('G80',           '2026년형 가솔린 2.5T 2WD',                   60600000, 1),
    ('G80',           '2026년형 가솔린 3.5T AWD',                   69800000, 2),
    ('그랑 콜레오스', '2026년형 하이브리드 E-Tech 테크노',          38900000, 1),
    ('그랑 콜레오스', '2026년형 하이브리드 E-Tech 아이코닉',        45900000, 2),
    ('5 Series',      '2026년형 가솔린 2.0 520i',                   70500000, 1),
    ('5 Series',      '2026년형 PHEV 530e M Sport',                 91500000, 2),
    ('E-Class',       '2026년형 가솔린 2.0 E200 Avantgarde',        77500000, 1),
    ('E-Class',       '2026년형 가솔린 2.0 E300 4MATIC',            89900000, 2)
) AS v(model_name, name, price, sort_order)
JOIN vehicle_model m ON m.name = v.model_name;

-- 옵션 (모든 모델 공통 샘플) ----------------------------------
INSERT INTO model_option (model_id, name, price, sort_order)
SELECT m.id, o.name, o.price, o.sort_order
FROM vehicle_model m
CROSS JOIN (VALUES
    ('파노라마 선루프',            1200000, 1),
    ('주행 보조 패키지',            950000, 2),
    ('빌트인 캠',                   450000, 3),
    ('프리미엄 사운드 패키지',      800000, 4)
) AS o(name, price, sort_order);

-- 색상 (모든 모델 공통 샘플) ----------------------------------
INSERT INTO model_color (model_id, name, hex_code, extra_price, sort_order)
SELECT m.id, c.name, c.hex_code, c.extra_price, c.sort_order
FROM vehicle_model m
CROSS JOIN (VALUES
    ('스노우 화이트 펄',  '#F4F4F2', 80000, 1),
    ('어비스 블랙 펄',    '#1B1C1E', 0,     2),
    ('그래비티 그레이',   '#6E7277', 0,     3),
    ('딥 블루',           '#1F3A5F', 0,     4)
) AS c(name, hex_code, extra_price, sort_order);

-- 특가 10 (타임특가 5, 무보증특가 5) ---------------------------
INSERT INTO deal (type, trim_id, title, badge, original_monthly, monthly_price, lease_monthly,
                  period_months, deposit_rate, prepay_rate, starts_at, ends_at, sort_order, is_published)
SELECT v.type, t.id, v.title, v.badge, v.original_monthly, v.monthly_price, v.lease_monthly,
       v.period_months, v.deposit_rate, v.prepay_rate, now() - interval '1 day', v.ends_at, v.sort_order, true
FROM (VALUES
    ('TIME_SALE',  '쏘렌토',     '2026년형 하이브리드 1.6T 프레스티지 2WD',   '기아 쏘렌토 하이브리드',   'HOT SALE',   230000, 209000, 188000, 48, 0, 30, now() + interval '14 days', 1),
    ('TIME_SALE',  '그랜저',     '2026년형 하이브리드 1.6T 프리미엄',         '현대 그랜저 하이브리드',   '마감임박',   247000, 229000, 231000, 48, 0, 30, now() + interval '3 days',  2),
    ('TIME_SALE',  'G80',        '2026년형 가솔린 2.5T 2WD',                  '제네시스 G80',             '게릴라 세일', 443000, 399000, 347000, 48, 0, 30, now() + interval '7 days',  3),
    ('TIME_SALE',  'EV3',        '2026년형 전기 스탠다드 에어',               '기아 EV3',                 '한정수량',   254000, 227000, 97000,  48, 0, 30, now() + interval '10 days', 4),
    ('TIME_SALE',  '카니발',     '2026년형 하이브리드 1.6T 9인승 프레스티지', '기아 카니발 하이브리드',   'HOT SALE',   280000, 255000, 191000, 48, 0, 30, NULL,                       5),
    ('NO_DEPOSIT', '스포티지',   '2026년형 하이브리드 1.6T 프레스티지 2WD',   '기아 스포티지 무보증',     '무보증 특가', NULL,   390000, NULL,   60, 0, 0,  NULL,                       1),
    ('NO_DEPOSIT', '아반떼',     '2026년형 가솔린 1.6 스마트',                '현대 아반떼 무보증',       '한정수량',   NULL,   319000, NULL,   60, 0, 0,  NULL,                       2),
    ('NO_DEPOSIT', '투싼',       '2026년형 가솔린 1.6T 모던 2WD',             '현대 투싼 무보증',         '예약판매',   NULL,   394000, NULL,   60, 0, 0,  NULL,                       3),
    ('NO_DEPOSIT', 'K5',         '2026년형 가솔린 2.0 스마트',                '기아 K5 무보증',           '마감임박',   NULL,   384000, NULL,   60, 0, 0,  now() + interval '5 days',  4),
    ('NO_DEPOSIT', '5 Series',   '2026년형 가솔린 2.0 520i',                  'BMW 5 Series 무보증',      '무보증 특가', NULL,  1205000, NULL,   60, 0, 0,  NULL,                       5)
) AS v(type, model_name, trim_name, title, badge, original_monthly, monthly_price, lease_monthly,
       period_months, deposit_rate, prepay_rate, ends_at, sort_order)
JOIN vehicle_model m ON m.name = v.model_name
JOIN vehicle_trim t ON t.model_id = m.id AND t.name = v.trim_name;

-- 즉시출고 10 (예약 1, 판매완료 1 포함) -------------------------
INSERT INTO instant_stock (trim_id, exterior_color, interior_color, options_text, vehicle_price, monthly_price,
                           condition_text, badge, status, sort_order, is_published)
SELECT t.id, v.exterior_color, v.interior_color, v.options_text, v.vehicle_price, v.monthly_price,
       '48개월 / 선납금 30% 기준', v.badge, v.status, v.sort_order, true
FROM (VALUES
    ('아이오닉 5',    '2027년형 전기 스탠다드 2WD',                '스노우 화이트 펄', '블랙',   '기본가',                    49800000, 338000, '5일이내 출고', 'AVAILABLE', 1),
    ('K5',            '2026년형 가솔린 2.0 스마트',                '스노우 화이트 펄', '블랙',   '기본가',                    27200000, 213000, '5일이내 출고', 'AVAILABLE', 2),
    ('5 Series',      '2026년형 PHEV 530e M Sport',                '그래비티 그레이',  '블랙',   '기본가',                    91500000, 1023000, '5일이내 출고', 'AVAILABLE', 3),
    ('스포티지',      '2026년형 하이브리드 1.6T 프레스티지 2WD',   '스노우 화이트 펄', '블랙',   '파노라마 선루프',           36500000, 208000, '5일이내 출고', 'AVAILABLE', 4),
    ('E-Class',       '2026년형 가솔린 2.0 E200 Avantgarde',       '스노우 화이트 펄', '베이지', '기본가',                    77500000, 859000, '5일이내 출고', 'AVAILABLE', 5),
    ('EV3',           '2026년형 전기 스탠다드 에어',               '딥 블루',          '그레이', '기본가',                    42500000, 175000, '인기 전기차',  'AVAILABLE', 6),
    ('G80',           '2026년형 가솔린 2.5T 2WD',                  '어비스 블랙 펄',   '브라운', '주행 보조 패키지, 빌트인 캠', 62000000, 433000, '5일이내 출고', 'AVAILABLE', 7),
    ('카니발',        '2026년형 하이브리드 1.6T 9인승 프레스티지', '스노우 화이트 펄', '블랙',   '기본가',                    40900000, 255000, '5일이내 출고', 'AVAILABLE', 8),
    ('싼타페',        '2026년형 하이브리드 1.6T 익스클루시브 2WD', '어비스 블랙 펄',   '블랙',   '기본가',                    41200000, 251000, '예약중',       'RESERVED',  9),
    ('그랑 콜레오스', '2026년형 하이브리드 E-Tech 테크노',         '그래비티 그레이',  '블랙',   '기본가',                    38900000, 265000, NULL,           'SOLD',      10)
) AS v(model_name, trim_name, exterior_color, interior_color, options_text, vehicle_price, monthly_price,
       badge, status, sort_order)
JOIN vehicle_model m ON m.name = v.model_name
JOIN vehicle_trim t ON t.model_id = m.id AND t.name = v.trim_name;

-- 배너 3 (이미지는 4단계에서 프론트 샘플 이미지로 교체) ----------
INSERT INTO banner (title, image_pc_url, image_mobile_url, link_url, sort_order, is_published) VALUES
    ('이달의 타임특가',     '/images/sample/banner-1-pc.jpg', '/images/sample/banner-1-mobile.jpg', '/deals?type=time',      1, true),
    ('보증금 0원 무보증특가', '/images/sample/banner-2-pc.jpg', '/images/sample/banner-2-mobile.jpg', '/deals?type=nodeposit', 2, true),
    ('5일 이내 즉시출고',   '/images/sample/banner-3-pc.jpg', '/images/sample/banner-3-mobile.jpg', '/instant',              3, true);

-- 리드 6 (관리자 화면 개발용, 이름·연락처는 가상) ---------------
INSERT INTO lead (type, name, phone, brand_id, model_id, trim_id, vehicle_snapshot, conditions, total_price,
                  agree_privacy, agree_marketing, agreed_at, source_url, utm, status, created_at, updated_at)
SELECT v.type, v.name, v.phone, b.id, m.id, t.id,
       jsonb_build_object('brand', b.name, 'model', m.name, 'trim', t.name, 'trimPrice', t.price),
       v.conditions::jsonb, t.price, true, v.agree_marketing, now() - v.ago, v.source_url,
       v.utm::jsonb, v.status, now() - v.ago, now() - v.ago
FROM (VALUES
    ('ESTIMATE', '김테스트', '01000000001', '기아',     '쏘렌토', '2026년형 하이브리드 1.6T 프레스티지 2WD',
     '{"useType":"RENT","periodMonths":48,"depositRate":0,"prepayRate":30,"insuranceAge":26,"annualMileage":20000,"creditScore":"OVER_700"}',
     true,  '/estimate/9', '{"utm_source":"naver","utm_medium":"cpc","utm_campaign":"sorento"}', 'NEW',         interval '10 minutes'),
    ('QUICK',    '이샘플',   '01000000002', '현대',     '그랜저', '2026년형 하이브리드 1.6T 프리미엄',
     '{"periodMonths":60}',
     false, '/',           '{"utm_source":"google","utm_medium":"cpc"}',                            'NEW',         interval '2 hours'),
    ('DEAL',     '박예시',   '01000000003', '제네시스', 'G80',    '2026년형 가솔린 2.5T 2WD',
     '{"useType":"LEASE","periodMonths":48,"prepayRate":30}',
     true,  '/deals',      NULL,                                                                   'IN_PROGRESS', interval '1 day'),
    ('INSTANT',  '최가상',   '01000000004', '기아',     'K5',     '2026년형 가솔린 2.0 스마트',
     '{}',
     false, '/instant',    '{"utm_source":"meta","utm_medium":"social"}',                           'CONTRACTED',  interval '3 days'),
    ('QUICK',    '정더미',   '01000000005', '현대',     '아반떼', '2026년형 가솔린 1.6 스마트',
     '{"periodMonths":36}',
     false, '/',           NULL,                                                                   'NO_ANSWER',   interval '5 days'),
    ('ESTIMATE', '한임시',   '01000000006', 'BMW',      '5 Series', '2026년형 가솔린 2.0 520i',
     '{"useType":"RENT","periodMonths":60,"depositRate":20,"prepayRate":0,"insuranceAge":26,"annualMileage":30000,"creditScore":"UNKNOWN"}',
     true,  '/estimate/14', NULL,                                                                  'CANCELED',    interval '7 days')
) AS v(type, name, phone, brand_name, model_name, trim_name, conditions, agree_marketing, source_url, utm, status, ago)
JOIN brand b ON b.name = v.brand_name
JOIN vehicle_model m ON m.brand_id = b.id AND m.name = v.model_name
JOIN vehicle_trim t ON t.model_id = m.id AND t.name = v.trim_name;
