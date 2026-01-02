package com.smartinterview.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页大小，默认 10 条 */
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderBy;

    /** 是否升序 */
    private Boolean asc = false;
}
