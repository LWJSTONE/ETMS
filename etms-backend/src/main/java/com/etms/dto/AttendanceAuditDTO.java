package com.etms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 补签审核请求DTO
 */
@Data
@ApiModel(description = "补签审核请求参数")
public class AttendanceAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "审核状态：1-通过，2-驳回", required = true, example = "1")
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态值不合法，有效值为1或2")
    @Max(value = 2, message = "审核状态值不合法，有效值为1或2")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;
}
