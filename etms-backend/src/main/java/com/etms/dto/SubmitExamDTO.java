package com.etms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 提交试卷请求DTO
 */
@Data
@ApiModel(description = "提交试卷请求参数")
public class SubmitExamDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "考试记录ID", required = true)
    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;

    @ApiModelProperty(value = "答案(JSON格式)", required = true)
    @NotBlank(message = "答案不能为空")
    private String answers;
}
