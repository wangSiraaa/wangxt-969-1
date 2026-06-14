package com.eldercare.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private int page = 0;
    private int size = 20;
    private String sortBy;
    private String sortDir = "desc";
}
