package com.eldercare.config;

import com.eldercare.entity.Caregiver;
import com.eldercare.entity.Elder;
import com.eldercare.entity.ServiceDemand;
import com.eldercare.enums.DemandStatus;
import com.eldercare.enums.ElderStatus;
import com.eldercare.repository.CaregiverRepository;
import com.eldercare.repository.ElderRepository;
import com.eldercare.repository.ServiceDemandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ElderRepository elderRepository;
    private final CaregiverRepository caregiverRepository;
    private final ServiceDemandRepository demandRepository;

    public DataInitializer(ElderRepository elderRepository,
                           CaregiverRepository caregiverRepository,
                           ServiceDemandRepository demandRepository) {
        this.elderRepository = elderRepository;
        this.caregiverRepository = caregiverRepository;
        this.demandRepository = demandRepository;
    }

    @Override
    public void run(String... args) {
        if (elderRepository.count() == 0) {
            initElders();
        }
        if (caregiverRepository.count() == 0) {
            initCaregivers();
        }
        if (demandRepository.count() == 0) {
            initDemands();
        }
    }

    private void initElders() {
        log.info("初始化老人数据...");

        Elder e1 = new Elder();
        e1.setName("张爷爷");
        e1.setIdCard("110101194001011234");
        e1.setPhone("13800138001");
        e1.setAddress("北京市朝阳区幸福小区1号楼101室");
        e1.setEmergencyContact("张小明");
        e1.setEmergencyPhone("13900139001");
        e1.setHealthCondition("高血压，行动不便，需要协助日常生活");
        e1.setStatus(ElderStatus.ACTIVE);
        elderRepository.save(e1);

        Elder e2 = new Elder();
        e2.setName("李奶奶");
        e2.setIdCard("110101194505052345");
        e2.setPhone("13800138002");
        e2.setAddress("北京市朝阳区幸福小区2号楼202室");
        e2.setEmergencyContact("李小红");
        e2.setEmergencyPhone("13900139002");
        e2.setHealthCondition("糖尿病，日常注射胰岛素");
        e2.setStatus(ElderStatus.ACTIVE);
        elderRepository.save(e2);

        Elder e3 = new Elder();
        e3.setName("王爷爷");
        e3.setIdCard("110101193812123456");
        e3.setPhone("13800138003");
        e3.setAddress("北京市朝阳区幸福小区3号楼303室");
        e3.setEmergencyContact("王大明");
        e3.setEmergencyPhone("13900139003");
        e3.setHealthCondition("阿尔茨海默症早期");
        e3.setStatus(ElderStatus.SUSPENDED);
        e3.setSuspendReason("住院治疗中");
        e3.setSuspendTime(LocalDateTime.now().minusDays(3));
        elderRepository.save(e3);

        Elder e4 = new Elder();
        e4.setName("赵奶奶");
        e4.setIdCard("110101194208084567");
        e4.setPhone("13800138004");
        e4.setAddress("北京市海淀区阳光家园5号楼501室");
        e4.setEmergencyContact("赵小芳");
        e4.setEmergencyPhone("13900139004");
        e4.setHealthCondition("心脏病，术后康复中");
        e4.setStatus(ElderStatus.ACTIVE);
        elderRepository.save(e4);

        log.info("老人数据初始化完成，共4条");
    }

    private void initCaregivers() {
        log.info("初始化护理员数据...");

        Caregiver c1 = new Caregiver();
        c1.setName("刘护工");
        c1.setIdCard("110101198501015678");
        c1.setPhone("13700137001");
        c1.setAddress("北京市朝阳区");
        c1.setQualifications("养老护理员高级, 护士资格证");
        c1.setSkillLevel("高级");
        c1.setServiceTypes("日常生活照料, 医疗护理, 康复训练");
        c1.setActive(true);
        caregiverRepository.save(c1);

        Caregiver c2 = new Caregiver();
        c2.setName("陈护工");
        c2.setIdCard("110101198803036789");
        c2.setPhone("13700137002");
        c2.setAddress("北京市朝阳区");
        c2.setQualifications("养老护理员中级");
        c2.setSkillLevel("中级");
        c2.setServiceTypes("日常生活照料, 康复训练");
        c2.setActive(true);
        caregiverRepository.save(c2);

        Caregiver c3 = new Caregiver();
        c3.setName("孙护工");
        c3.setIdCard("110101199005057890");
        c3.setPhone("13700137003");
        c3.setAddress("北京市海淀区");
        c3.setQualifications("养老护理员初级, 心理咨询师");
        c3.setSkillLevel("初级");
        c3.setServiceTypes("日常生活照料, 心理陪伴");
        c3.setActive(true);
        caregiverRepository.save(c3);

        Caregiver c4 = new Caregiver();
        c4.setName("周护工");
        c4.setIdCard("110101198207078901");
        c4.setPhone("13700137004");
        c4.setAddress("北京市海淀区");
        c4.setQualifications("养老护理员高级, 康复治疗师");
        c4.setSkillLevel("高级");
        c4.setServiceTypes("日常生活照料, 医疗护理, 康复训练");
        c4.setActive(true);
        caregiverRepository.save(c4);

        log.info("护理员数据初始化完成，共4条");
    }

    private void initDemands() {
        log.info("初始化服务需求数据...");

        ServiceDemand d1 = new ServiceDemand();
        d1.setElderId(1L);
        d1.setApplicant("张小明");
        d1.setApplicantPhone("13900139001");
        d1.setRelation("子女");
        d1.setServiceType("日常生活照料");
        d1.setServiceContent("协助穿衣、洗漱、进食，简单家务，陪伴聊天2小时");
        d1.setExpectedTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
        d1.setSpecialRequirement("张爷爷行动不便，需要耐心");
        d1.setEstimatedAmount(new BigDecimal("150.00"));
        d1.setRequiredQualifications("养老护理员中级");
        d1.setStatus(DemandStatus.PENDING_DISPATCH);
        demandRepository.save(d1);

        ServiceDemand d2 = new ServiceDemand();
        d2.setElderId(2L);
        d2.setApplicant("李小红");
        d2.setApplicantPhone("13900139002");
        d2.setRelation("子女");
        d2.setServiceType("医疗护理");
        d2.setServiceContent("协助注射胰岛素，测量血糖，观察身体状况");
        d2.setExpectedTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0));
        d2.setSpecialRequirement("需要具备护士资格，熟悉胰岛素注射");
        d2.setEstimatedAmount(new BigDecimal("200.00"));
        d2.setRequiredQualifications("护士资格证");
        d2.setStatus(DemandStatus.PENDING_DISPATCH);
        demandRepository.save(d2);

        ServiceDemand d3 = new ServiceDemand();
        d3.setElderId(4L);
        d3.setApplicant("赵小芳");
        d3.setApplicantPhone("13900139004");
        d3.setRelation("子女");
        d3.setServiceType("康复训练");
        d3.setServiceContent("术后康复训练指导，协助肢体活动，陪伴散步");
        d3.setExpectedTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0));
        d3.setSpecialRequirement("心脏病术后，活动需适度");
        d3.setEstimatedAmount(new BigDecimal("180.00"));
        d3.setRequiredQualifications("康复治疗师");
        d3.setStatus(DemandStatus.PENDING_DISPATCH);
        demandRepository.save(d3);

        log.info("服务需求数据初始化完成，共3条");
    }
}
