package com.etms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 试卷题目关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("etms_paper_question")
public class PaperQuestion extends BaseEntity {
    
    /**
     * 试卷ID
     */
    private Long paperId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 题目分数
     */
    private Integer score;
    
    /**
     * 题目排序
     */
    private Integer sortOrder;
}
