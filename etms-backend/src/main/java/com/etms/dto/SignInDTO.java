package com.etms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 签到请求DTO
 */
@Data
@ApiModel(description = "签到请求参数")
public class SignInDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "培训计划ID", required = true)
    @NotNull(message = "培训计划ID不能为空")
    private Long planId;

    @ApiModelProperty(value = "签到类型：1-二维码，2-GPS定位，3-人脸识别", example = "1")
    @Min(value = 1, message = "签到类型不合法，有效值为1-3")
    @Max(value = 3, message = "签到类型不合法，有效值为1-3")
    private Integer signType = 1;

    @ApiModelProperty(value = "签到类别：1-签到，2-签退", example = "1")
    @Min(value = 1, message = "签到类别不合法，有效值为1-2")
    @Max(value = 2, message = "签到类别不合法，有效值为1-2")
    private Integer signCategory = 1;

    @ApiModelProperty(value = "签到位置")
    private String location;
}
