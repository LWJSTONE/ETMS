package com.etms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 试卷题目添加请求DTO
 * 用于批量添加题目到试卷
 */
@Data
@ApiModel(description = "试卷题目添加请求参数")
public class PaperQuestionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "题目ID", required = true)
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @ApiModelProperty(value = "题目分数", example = "10")
    @Min(value = 0, message = "题目分数不能为负数")
    private Integer score = 1;

    @ApiModelProperty(value = "排序序号", example = "1")
    @Min(value = 1, message = "排序序号最小为1")
    private Integer sortOrder;
}
