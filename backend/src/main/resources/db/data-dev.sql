-- =====================================================
-- 养老上门服务工单系统 - 演示初始化数据
-- 包含：护理等级、服务包、禁忌事项、老人、紧急联系人、
--       护理员、资质、排班容量、完整业务流程演示数据
-- =====================================================

-- ============================================
-- 1. 护理等级 (nursing_level)
-- ============================================
MERGE INTO nursing_level (id, code, name, description, min_score, max_score, base_hourly_rate, risk_level, required_qualifications, enabled, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'NL01', '自理老人', '基本可自理，仅需轻度照护', 0, 30, 50.00, 'LOW', 'CAREGIVER_BASIC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'NL02', '半失能老人', '部分自理能力丧失，需要中度照护', 31, 60, 80.00, 'MEDIUM', 'CAREGIVER_BASIC,CAREGIVER_INTERMEDIATE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'NL03', '失能老人', '完全失能，需专业照护', 61, 90, 120.00, 'HIGH', 'CAREGIVER_INTERMEDIATE,CAREGIVER_SENIOR,NURSE_LICENSE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 'NL04', '特护老人', '重症/临终关怀，需24小时专业护理', 91, 100, 200.00, 'CRITICAL', 'CAREGIVER_SENIOR,NURSE_LICENSE,MEDICAL_TRAINING', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 2. 服务包 (service_package)
-- ============================================
MERGE INTO service_package (id, code, name, description, service_type, standard_duration_minutes, base_price, hourly_rate, minimum_nursing_level_code, required_qualifications, risk_level, insurable, insurance_code, enabled, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'SP001', '日常家政清洁', '室内清洁、衣物整理、厨房清洁', 'HOUSEKEEPING', 120, 120.00, 60.00, 'NL01', 'CAREGIVER_BASIC', 'LOW', TRUE, 'INS_HK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'SP002', '助餐服务', '订餐配送、协助进餐、观察饮食情况', 'MEAL_ASSIST', 60, 60.00, 60.00, 'NL01', 'CAREGIVER_BASIC', 'LOW', TRUE, 'INS_MA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'SP003', '生活照护-个人卫生', '助浴、洗头、修剪指甲、仪容整理', 'PERSONAL_CARE', 90, 150.00, 100.00, 'NL02', 'CAREGIVER_BASIC,CAREGIVER_INTERMEDIATE', 'MEDIUM', TRUE, 'INS_PC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 'SP004', '康复理疗按摩', '基础康复训练、穴位按摩、关节活动', 'REHABILITATION', 90, 200.00, 130.00, 'NL02', 'CAREGIVER_INTERMEDIATE,REHAB_TRAINING', 'MEDIUM', TRUE, 'INS_RH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(5, 'SP005', '医学护理-压疮护理', '压疮评估、换药、伤口护理', 'MEDICAL_NURSING', 60, 280.00, 280.00, 'NL03', 'NURSE_LICENSE,MEDICAL_TRAINING', 'HIGH', TRUE, 'INS_MN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(6, 'SP006', '陪同就医', '挂号、陪同就诊、取药、医嘱记录', 'MEDICAL_COMPANION', 180, 300.00, 100.00, 'NL01', 'CAREGIVER_BASIC', 'MEDIUM', TRUE, 'INS_MC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(7, 'SP007', '夜间陪护', '夜间8小时照护、观察生命体征、应急处理', 'NIGHT_CARE', 480, 600.00, 75.00, 'NL03', 'CAREGIVER_SENIOR,NURSE_LICENSE', 'HIGH', TRUE, 'INS_NC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(8, 'SP008', '综合日间照护', '全天综合照护：个人卫生+助餐+活动+健康监测', 'DAY_CARE', 480, 800.00, 100.00, 'NL02', 'CAREGIVER_INTERMEDIATE,CAREGIVER_SENIOR', 'MEDIUM', TRUE, 'INS_DC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 3. 禁忌事项 (contraindication)
-- ============================================
MERGE INTO contraindication (id, code, name, category, severity, description, handling_notes, excluded_qualifications, enabled, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'CT01', '严重过敏性皮炎', 'SKIN', 'HIGH', '全身多处严重皮炎，禁止接触性按摩', '护理时需佩戴医用手套，避免直接皮肤接触', 'MASSAGE_THERAPIST', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'CT02', '骨折未愈合期', 'BONE', 'CRITICAL', '股骨颈骨折保守治疗期', '禁止移动患肢，需持证护士操作', 'CAREGIVER_BASIC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'CT03', '高热抽搐史', 'NEURO', 'HIGH', '体温超过38.5度可能引发惊厥', '需每2小时监测体温，备退烧药', '', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 'CT04', '吞咽障碍-呛咳风险', 'DIGESTIVE', 'HIGH', '延髓性麻痹导致严重呛咳', '只能进食流质，禁止干硬食物，就餐时需坐起并拍背', 'CAREGIVER_BASIC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(5, 'CT05', '晚期老年痴呆-躁狂型', 'PSYCHO', 'CRITICAL', '认知障碍伴随攻击性和躁狂表现', '需男护理员双人作业，备约束措施', '', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(6, 'CT06', '药物过敏-青霉素类', 'MEDICATION', 'MEDIUM', '青霉素及头孢类过敏史', '所有药物使用前必须核对过敏史', '', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 4. 老人档案 (elder)
-- ============================================
MERGE INTO elder (id, elder_code, name, id_card, gender, birth_date, phone, address, longitude, latitude, service_area, nursing_level_id, service_package_id, health_status, allergies, contraindication_ids, emergency_contacts, insurant_no, insurance_type, insurance_coverage, status, pause_start_time, pause_end_time, pause_reason, risk_level, credit_score, avatar_url, remark, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'ELD202401001', '张桂兰', '110101193501010011', 'FEMALE', '1935-01-01', '13800000001', '北京市朝阳区建国路88号院3号楼501', 116.472800, 39.908900, 'CHAOWAYANG', 2, 3, '高血压3级、糖尿病II型、右膝关节退行性病变', '青霉素、海鲜', '', '女儿-李敏(13900000001); 儿子-李强(13900000002)', 'YB20240001', '职工医保', 85.00, 'ACTIVE', NULL, NULL, NULL, 'MEDIUM', 100, NULL, '独居老人，听力下降明显，需大声说话', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'ELD202401002', '王建国', '110101193006150033', 'MALE', '1930-06-15', '13800000002', '北京市海淀区中关村南大街5号院12号楼302', 116.328600, 39.960000, 'HAIDIAN', 3, 5, '脑梗塞后遗症-左侧肢体偏瘫、压疮二期、留置导尿', '磺胺类药物', '2,6', '儿子-王军(13900000011)', 'YB20240002', '离休医保', 95.00, 'ACTIVE', NULL, NULL, NULL, 'HIGH', 95, NULL, '完全失能卧床，体重85kg移动困难，需要男护理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'ELD202401003', '刘淑珍', '110101194203220022', 'FEMALE', '1942-03-22', '13800000003', '北京市西城区金融街15号院2号楼801', 116.360400, 39.915300, 'XICHENG', 1, 1, '冠心病稳定型、轻度骨质疏松', '花粉过敏', '', '女儿-陈静(13900000021); 老伴-陈国华(13900000022)', 'YB20240003', '居民医保', 70.00, 'PAUSED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '14' DAY, '【暂停服务演示】14天后住院做膝关节置换手术', 'LOW', 98, NULL, '手术期间暂停所有上门服务', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 'ELD202401004', '赵振华', '110101192811080055', 'MALE', '1928-11-08', '13800000004', '北京市东城区东四北大街107号', 116.418200, 39.928800, 'DONGCHENG', 4, 7, '肺癌晚期骨转移、恶病质、重度疼痛、抑郁倾向', '吗啡过敏', '3,4', '女儿-赵雪梅(13900000031); 儿子-赵雪松(13900000032)', 'YB20240004', '离休医保', 100.00, 'ACTIVE', NULL, NULL, NULL, 'CRITICAL', 100, NULL, '【高风险演示】临终关怀期，需有心理准备的专业护理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(5, 'ELD202401005', '孙秀芬', '110101195505180066', 'FEMALE', '1955-05-18', '13800000005', '北京市丰台区方庄小区15号楼4单元102', 116.435600, 39.862500, 'FENGTAI', 1, 2, '慢性胃炎、白内障术后、高血压1级', '无', '', '女儿-周芳(13900000041)', 'YB20240005', '职工医保', 80.00, 'ACTIVE', NULL, NULL, NULL, 'LOW', 100, NULL, '【资质不匹配演示】老人要求护理员必须有助浴证+中级护理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 5. 紧急联系人 (emergency_contact)
-- ============================================
MERGE INTO emergency_contact (id, elder_id, name, relation, phone, alt_phone, address, is_primary, can_confirm_service, notify_on_abnormal, created_at, updated_at, deleted) KEY(id) VALUES
(1, 1, '李敏', '女儿', '13900000001', '010-88880001', '北京市朝阳区青年路朝阳大悦城公寓', TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 1, '李强', '儿子', '13900000002', NULL, '上海市浦东新区陆家嘴环路1000号', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 2, '王军', '儿子', '13900000011', '010-88880011', '北京市海淀区万柳中路3号', TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 3, '陈静', '女儿', '13900000021', NULL, '北京市西城区金融街15号院2号楼', TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(5, 3, '陈国华', '老伴', '13900000022', NULL, '同地址', FALSE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(6, 4, '赵雪梅', '女儿', '13900000031', NULL, '北京市朝阳区望京SOHO', TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(7, 4, '赵雪松', '儿子', '13900000032', NULL, '深圳市南山区科技园', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(8, 5, '周芳', '女儿', '13900000041', NULL, '北京市丰台区方庄小区', TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 6. 护理员档案 (nurse)
-- ============================================
MERGE INTO nurse (id, nurse_code, name, id_card, gender, birth_date, phone, address, longitude, latitude, service_areas, max_daily_orders, max_weekly_hours, max_continuous_hours, travel_time_radius_minutes, highest_nursing_level_id, qualification_summary, average_rating, completed_orders_count, on_time_rate, status, hire_date, avatar_url, remark, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'NRS20230101', '周红梅', '110221198503152011', 'FEMALE', '1985-03-15', '13700000101', '北京市朝阳区常营乡', 116.576400, 39.926300, 'CHAOWAYANG,DONGCHENG', 6, 42, 6, 40, 2, '5年养老护理经验，耐心细致，擅长失智老人照护。持有中级护理员证、助浴证、急救证。', 4.85, 358, 96.5, 'ACTIVE', '2023-01-15', NULL, '【资质匹配-NL2】可服务半失能、自理老人', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'NRS20220615', '吴志强', '110224197812201022', 'MALE', '1978-12-20', '13700000102', '北京市海淀区西三旗', 116.366200, 40.048400, 'HAIDIAN,XICHENG,FENGTAI', 5, 40, 8, 45, 3, '10年三甲医院男护士经验，后转入养老行业。擅长压疮护理、鼻饲、导尿、转移搬运体重较大老人。持有护士执业证、高级护理员证、康复训练证。', 4.92, 512, 98.2, 'ACTIVE', '2022-06-15', NULL, '【资质匹配-NL3】唯一能服务NL3级+男护理员+体重85kg需求', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'NRS20230820', '郑晓丽', '110227199208101033', 'FEMALE', '1992-08-10', '13700000103', '北京市西城区广安门内', 116.360800, 39.879500, 'XICHENG,HAIDIAN,DONGCHENG', 7, 48, 6, 35, 4, '北京协和医院护理学硕士，主管护师，7年ICU经验后加入安宁疗护团队。持有护士执业证、高级护理员证、安宁疗护专科证、PICC维护证。医学培训证书齐全。', 4.97, 286, 99.5, 'ACTIVE', '2023-08-20', NULL, '【资质匹配-NL4】唯一能处理临终关怀(NL4)+全套医学资质+心理陪护', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 'NRS20240201', '林小芳', '110228199806221044', 'FEMALE', '1998-06-22', '13700000104', '北京市朝阳区垡头', 116.511200, 39.855100, 'CHAOWAYANG,FENGTAI', 8, 48, 6, 30, 1, '刚毕业2年，有活力有热情。持有初级护理员证、急救证。正在考取中级护理员证。', 4.45, 124, 92.3, 'ACTIVE', '2024-02-01', NULL, '【资质不匹配演示对象】只有初级护理员资质：CAREGIVER_BASIC，缺少NL2要求的CAREGIVER_INTERMEDIATE，缺少助浴证', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(5, 'NRS20231105', '孙玉梅', '110226198804181055', 'FEMALE', '1988-04-18', '13700000105', '北京市东城区广渠门', 116.438800, 39.886500, 'DONGCHENG,CHAOWAYANG', 6, 44, 6, 35, 2, '6年养老护理经验，温柔耐心。持有中级护理员证、助餐培训证、认知症照护证。', 4.78, 298, 97.1, 'ACTIVE', '2023-11-05', NULL, '资质齐全中级护理员，但服务区域不含FENGTAI【改派演示对象】', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(6, 'NRS20230412', '王海峰', '110223198210251066', 'MALE', '1982-10-25', '13700000106', '北京市丰台区方庄', 116.427500, 39.860000, 'FENGTAI,CHAOWAYANG,DONGCHENG', 5, 40, 8, 40, 2, '8年养老护理经验，力气大，擅长转移搬运。持有中级护理员证、助浴证、急救证、康复训练证。', 4.88, 402, 97.8, 'ACTIVE', '2023-04-12', NULL, '丰台区+中级+男护理员【孙秀芬正确匹配人选】', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 7. 护理员资质 (nurse_qualification)
-- ============================================
MERGE INTO nurse_qualification (id, nurse_id, type, name, certificate_no, issued_date, expiry_date, issuing_authority, level, service_type_scope, nursing_level_scope, certificate_url, status, verified, verified_by, created_at, updated_at, deleted) KEY(id) VALUES
(1, 1, 'CAREGIVER_BASIC', '初级养老护理员证', 'BJ-YL-2021-05678', '2021-05-10', '2026-05-09', '北京市民政局', '初级', 'HOUSEKEEPING,MEAL_ASSIST,PERSONAL_CARE', 'NL01,NL02', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 1, 'CAREGIVER_INTERMEDIATE', '中级养老护理员证', 'BJ-YL-2022-03456', '2022-08-15', '2027-08-14', '北京市民政局', '中级', 'PERSONAL_CARE,REHABILITATION', 'NL01,NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 1, 'BATH_ASSIST', '助浴服务资格证', 'BJ-ZY-2022-07890', '2022-11-01', '2025-10-31', '北京市养老服务行业协会', '中级', 'PERSONAL_CARE', 'NL01,NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 1, 'FIRST_AID', '红十字急救证', 'BJ-RC-2023-00123', '2023-03-20', '2026-03-19', '中国红十字会北京分会', '初级', 'ALL', 'ALL', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(5, 2, 'CAREGIVER_BASIC', '初级养老护理员证', 'BJ-YL-2019-08901', '2019-07-10', '2024-07-09', '北京市民政局', '初级', 'HOUSEKEEPING', 'NL01', NULL, 'EXPIRED', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(6, 2, 'CAREGIVER_INTERMEDIATE', '中级养老护理员证', 'BJ-YL-2020-06789', '2020-09-25', '2025-09-24', '北京市民政局', '中级', 'PERSONAL_CARE,MEDICAL_NURSING', 'NL01,NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(7, 2, 'CAREGIVER_SENIOR', '高级养老护理员证', 'BJ-YL-2022-01234', '2022-03-18', '2027-03-17', '北京市民政局', '高级', 'MEDICAL_NURSING,NIGHT_CARE', 'NL02,NL03,NL04', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(8, 2, 'NURSE_LICENSE', '护士执业证书', 'HS-11000-2008-12345', '2008-06-30', '2028-06-29', '北京市卫生健康委员会', '注册护士', 'MEDICAL_NURSING,NIGHT_CARE', 'NL03,NL04', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(9, 2, 'REHAB_TRAINING', '康复训练师资格证', 'BJ-KF-2021-00456', '2021-12-05', '2026-12-04', '中国康复医学会', '中级', 'REHABILITATION', 'NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(10, 2, 'MEDICAL_TRAINING', '医学护理专项培训证书', 'BJ-YX-2023-00890', '2023-05-22', '2026-05-21', '北京协和医学院继续教育学院', '专项', 'MEDICAL_NURSING', 'NL03,NL04', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(11, 2, 'FIRST_AID', '红十字急救证-高级', 'BJ-RC-2022-00056', '2022-04-15', '2025-04-14', '中国红十字会北京分会', '高级', 'ALL', 'ALL', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(12, 3, 'CAREGIVER_SENIOR', '高级养老护理员证', 'BJ-YL-2021-00897', '2021-10-12', '2026-10-11', '北京市民政局', '高级', 'NIGHT_CARE,DAY_CARE', 'NL02,NL03,NL04', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(13, 3, 'NURSE_LICENSE', '护士执业证书-主管护师', 'HS-11000-2015-56789', '2015-07-01', '2030-06-30', '北京市卫生健康委员会', '主管护师', 'MEDICAL_NURSING,NIGHT_CARE', 'NL03,NL04', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(14, 3, 'MEDICAL_TRAINING', 'ICU专科护理培训证书', 'BJ-YX-2018-00123', '2018-11-20', '2099-12-31', '北京协和医院护理部', '专科', 'MEDICAL_NURSING,NIGHT_CARE', 'NL03,NL04', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(15, 3, 'HOSPICE_CARE', '安宁疗护专科证书', 'BJ-LZ-2022-00045', '2022-06-08', '2025-06-07', '北京生前预嘱推广协会', '专科', 'NIGHT_CARE,DAY_CARE', 'NL04', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(16, 4, 'CAREGIVER_BASIC', '初级养老护理员证', 'BJ-YL-2023-04567', '2023-05-18', '2028-05-17', '北京市民政局', '初级', 'HOUSEKEEPING,MEAL_ASSIST', 'NL01', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(17, 4, 'FIRST_AID', '红十字急救证', 'BJ-RC-2023-00456', '2023-08-09', '2026-08-08', '中国红十字会北京分会', '初级', 'ALL', 'ALL', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(18, 5, 'CAREGIVER_BASIC', '初级养老护理员证', 'BJ-YL-2019-01122', '2019-04-22', '2024-04-21', '北京市民政局', '初级', 'HOUSEKEEPING,MEAL_ASSIST', 'NL01', NULL, 'EXPIRED', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(19, 5, 'CAREGIVER_INTERMEDIATE', '中级养老护理员证', 'BJ-YL-2021-03344', '2021-07-14', '2026-07-13', '北京市民政局', '中级', 'PERSONAL_CARE,MEAL_ASSIST', 'NL01,NL02', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(20, 5, 'MEAL_TRAINING', '助餐服务专项培训', 'BJ-ZC-2022-00223', '2022-05-11', '2025-05-10', '北京市营养师协会', '专项', 'MEAL_ASSIST', 'NL01,NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(21, 5, 'DEMENTIA_CARE', '认知症照护培训证书', 'BJ-RZ-2023-00011', '2023-03-30', '2026-03-29', '中国阿尔茨海默病协会', '专项', 'DAY_CARE', 'NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(22, 6, 'CAREGIVER_INTERMEDIATE', '中级养老护理员证', 'BJ-YL-2020-08899', '2020-11-08', '2025-11-07', '北京市民政局', '中级', 'PERSONAL_CARE,HOUSEKEEPING', 'NL01,NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(23, 6, 'BATH_ASSIST', '助浴服务资格证', 'BJ-ZY-2021-06677', '2021-09-15', '2024-09-14', '北京市养老服务行业协会', '中级', 'PERSONAL_CARE', 'NL01,NL02', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(24, 6, 'FIRST_AID', '红十字急救证', 'BJ-RC-2021-00789', '2021-06-05', '2024-06-04', '中国红十字会北京分会', '初级', 'ALL', 'ALL', NULL, 'EXPIRED', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(25, 6, 'REHAB_TRAINING', '康复训练师资格证', 'BJ-KF-2022-00123', '2022-08-18', '2027-08-17', '中国康复医学会', '初级', 'REHABILITATION', 'NL02,NL03', NULL, 'VALID', TRUE, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 8. 护理员排班容量 (nurse_schedule_capacity)
-- ============================================
-- 周一到周日排班容量，每个护理员默认排满
INSERT INTO nurse_schedule_capacity (nurse_id, day_of_week, start_time, end_time, max_orders, max_hours, priority, enabled, remark, created_at, updated_at, deleted)
SELECT n.id, d.dow, '08:00:00', '18:00:00', n.max_daily_orders, n.max_continuous_hours, 0, TRUE, '默认工作日排班', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
FROM nurse n
CROSS JOIN (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY')) AS d(dow)
WHERE n.status = 'ACTIVE';

INSERT INTO nurse_schedule_capacity (nurse_id, day_of_week, start_time, end_time, max_orders, max_hours, priority, enabled, remark, created_at, updated_at, deleted)
SELECT n.id, d.dow, '09:00:00', '17:00:00', 4, 8, 1, TRUE, '周末减半排班', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
FROM nurse n
CROSS JOIN (VALUES ('SATURDAY'), ('SUNDAY')) AS d(dow)
WHERE n.status = 'ACTIVE';

-- ============================================
-- 9. 老人服务包账户 (elder_service_package)
-- ============================================
MERGE INTO elder_service_package (id, account_code, elder_id, elder_name, service_package_id, service_package_name,
    package_type, total_sessions, used_sessions, remaining_sessions,
    total_minutes, used_minutes, remaining_minutes,
    total_amount, used_amount, remaining_amount,
    effective_date, expiry_date, purchase_date, purchase_channel,
    status, pause_start_time, pause_end_time, pause_reason, paused_days,
    remark, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'ESP202401001', 1, '张桂兰', 3, '生活照护-个人卫生', 'LONG_TERM',
    30, 5, 25,
    2700, 450, 2250,
    4500.00, 750.00, 3750.00,
    '2024-01-01', '2024-12-31', '2024-01-01 10:00:00', 'OFFLINE',
    'ACTIVE', NULL, NULL, NULL, 0,
    '年度套餐：30次/2700分钟/4500元，每周3次上门服务', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'ESP202401002', 2, '王建国', 5, '医学护理-压疮护理', 'LONG_TERM',
    60, 12, 48,
    3600, 720, 2880,
    16800.00, 3360.00, 13440.00,
    '2024-01-01', '2024-12-31', '2024-01-01 14:00:00', 'ONLINE',
    'ACTIVE', NULL, NULL, NULL, 0,
    '季度套餐：60次/3600分钟/16800元，每周5次压疮护理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'ESP202401003', 3, '刘淑珍', 1, '日常家政清洁', 'LONG_TERM',
    12, 3, 9,
    1440, 360, 1080,
    1440.00, 360.00, 1080.00,
    '2024-01-15', '2024-07-15', '2024-01-15 09:30:00', 'OFFLINE',
    'PAUSED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '14' DAY, '膝关节置换手术暂停', 14,
    '半年套餐：12次/1440分钟，手术期间暂停，有效期顺延', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(4, 'ESP202401004', 4, '赵振华', 7, '夜间陪护', 'LONG_TERM',
    90, 25, 65,
    43200, 12000, 31200,
    54000.00, 15000.00, 39000.00,
    '2024-01-01', '2024-12-31', '2024-01-01 16:00:00', 'OFFLINE',
    'ACTIVE', NULL, NULL, NULL, 0,
    '季度套餐：90次夜间陪护，每晚8小时', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(5, 'ESP202401005', 5, '孙秀芬', 2, '助餐服务', 'LONG_TERM',
    20, 8, 12,
    1200, 480, 720,
    1200.00, 480.00, 720.00,
    '2024-02-01', '2025-01-31', '2024-02-01 11:00:00', 'ONLINE',
    'ACTIVE', NULL, NULL, NULL, 0,
    '年度助餐套餐：20次/1200分钟，每日午餐配送', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 10. 服务包余额变动日志 (package_balance_log)
-- ============================================
MERGE INTO package_balance_log (id, log_code, elder_service_package_id, account_code,
    elder_id, elder_name, service_package_id, service_package_name,
    change_type, change_reason, work_order_id, order_code,
    abnormal_event_id, event_code,
    before_sessions, change_sessions, after_sessions,
    before_minutes, change_minutes, after_minutes,
    before_amount, change_amount, after_amount,
    operator_name, operated_at, remark, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'PBL20240100101', 1, 'ESP202401001', 1, '张桂兰', 3, '生活照护-个人卫生',
    'PURCHASE', '购买套餐', NULL, NULL, NULL, NULL,
    0, 30, 30,
    0, 2700, 2700,
    0.00, 4500.00, 4500.00,
    'admin', '2024-01-01 10:00:00', '首次购买年度套餐', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'PBL20240100201', 2, 'ESP202401002', 2, '王建国', 5, '医学护理-压疮护理',
    'PURCHASE', '购买套餐', NULL, NULL, NULL, NULL,
    0, 60, 60,
    0, 3600, 3600,
    0.00, 16800.00, 16800.00,
    'admin', '2024-01-01 14:00:00', '购买季度压疮护理套餐', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'PBL20240100102', 1, 'ESP202401001', 1, '张桂兰', 3, '生活照护-个人卫生',
    'DEDUCT', '服务完成扣减', 1, 'WO202401001', NULL, NULL,
    29, -1, 28,
    2610, -90, 2520,
    4350.00, -150.00, 4200.00,
    'system', '2024-01-05 11:30:00', '正常服务完成', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 11. 护理员请假 (nurse_leave)
-- ============================================
MERGE INTO nurse_leave (id, leave_code, nurse_id, nurse_name, leave_type,
    start_date, end_date, start_time, end_time, total_days, total_hours,
    reason, status, approved_by, approved_at,
    reassignment_count, remark, created_at, updated_at, deleted) KEY(id) VALUES
(1, 'NLV202401001', 1, '李红英', 'SICK_LEAVE',
    '2024-01-20', '2024-01-22', '09:00:00', '18:00:00', 3, 24,
    '感冒发烧，医生建议休息3天', 'APPROVED', 'admin', '2024-01-19 16:30:00',
    5, '已安排护理员王桂芳顶替', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(2, 'NLV202401002', 3, '张秀兰', 'ANNUAL_LEAVE',
    '2024-02-01', '2024-02-07', '00:00:00', '23:59:59', 7, 56,
    '回老家过年，提前申请年假', 'PENDING_APPROVAL', NULL, NULL,
    0, '等待主管审批', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
(3, 'NLV202401003', 2, '王桂芳', 'PERSONAL_LEAVE',
    '2024-01-25', '2024-01-25', '09:00:00', '18:00:00', 1, 8,
    '家中有事，需请假一天处理', 'APPROVED', 'admin', '2024-01-23 10:00:00',
    2, '已安排调班', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- ============================================
-- 12. 结算复核队列 (settlement_review_queue)
-- ============================================
-- 由于需要关联已存在的结算单，这里先不插入演示数据
-- 实际使用时会通过触发条件自动加入复核队列
