package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalHandleDTO {

    private Long abnormalEventId;

    private Long handlerId;

    private String handlerName;

    private String handlingNotes;

    private String resolution;

    private String targetNurseId;

    private String targetStatus;
}
