package com.eldercare.repository;

import com.eldercare.entity.AbnormalEvent;
import com.eldercare.common.enums.AbnormalStatus;
import com.eldercare.common.enums.AbnormalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbnormalEventRepository extends JpaRepository<AbnormalEvent, Long>, JpaSpecificationExecutor<AbnormalEvent> {

    List<AbnormalEvent> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);

    List<AbnormalEvent> findByWorkOrderIdOrderByDetectedAtDesc(Long workOrderId);

    List<AbnormalEvent> findByStatusOrderByCreatedAtDesc(AbnormalStatus status);

    List<AbnormalEvent> findByStatusOrderByDetectedAtDesc(AbnormalStatus status);

    List<AbnormalEvent> findByAbnormalTypeOrderByCreatedAtDesc(AbnormalType abnormalType);

    List<AbnormalEvent> findByAbnormalTypeOrderByDetectedAtDesc(AbnormalType abnormalType);

    List<AbnormalEvent> findByStatusInOrderByCreatedAtDesc(List<AbnormalStatus> statuses);

    Long countByStatus(AbnormalStatus status);

    @Query("SELECT COUNT(a) FROM AbnormalEvent a WHERE a.status = :status")
    Long countByStatusString(@Param("status") String status);

    List<AbnormalEvent> findAllByOrderByDetectedAtDesc();

    List<AbnormalEvent> findByWorkOrderIdAndAbnormalType(Long workOrderId, AbnormalType abnormalType);
}
