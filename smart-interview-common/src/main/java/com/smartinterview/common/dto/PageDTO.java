package com.smartinterview.common.dto;
import lombok.Data;

@Data
public class PageDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderBy;
    private Boolean asc = true;
}
