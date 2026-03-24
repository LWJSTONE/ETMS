package com.etms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 状态修改请求DTO
 */
@Data
@ApiModel(description = "状态修改请求参数")
public class StatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "状态：0-禁用，1-启用", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法，有效值为0(禁用)或1(正常)")
    @Max(value = 1, message = "状态值不合法，有效值为0(禁用)或1(正常)")
    private Integer status;
}
