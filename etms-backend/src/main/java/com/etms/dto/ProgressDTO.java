package com.etms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 学习进度更新请求DTO
 */
@Data
@ApiModel(description = "学习进度更新请求参数")
public class ProgressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "培训计划ID", required = true)
    @NotNull(message = "planId不能为空")
    private Long planId;

    @ApiModelProperty(value = "课程ID", required = true)
    @NotNull(message = "courseId不能为空")
    private Long courseId;

    @ApiModelProperty(value = "进度百分比(0-100)", required = true, example = "50")
    @NotNull(message = "progress不能为空")
    @Min(value = 0, message = "进度值必须在0-100之间")
    @Max(value = 100, message = "进度值必须在0-100之间")
    private Integer progress;
}
