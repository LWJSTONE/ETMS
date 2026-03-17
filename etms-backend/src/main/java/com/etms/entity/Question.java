package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题库实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_question")
public class Question extends BaseEntity {
    
    /** 题目编码 */
    private String questionCode;
    
    /** 题目类型(1单选 2多选 3判断 4填空 5简答) */
    private Integer questionType;
    
    /** 题目内容 */
    private String questionContent;
    
    /** 选项A */
    private String optionA;
    
    /** 选项B */
    private String optionB;
    
    /** 选项C */
    private String optionC;
    
    /** 选项D */
    private String optionD;
    
    /** 选项E */
    private String optionE;
    
    /** 正确答案 */
    private String answer;
    
    /** 答案解析 */
    private String answerAnalysis;
    
    /** 难度(1简单 2中等 3困难) */
    private Integer difficulty;
    
    /** 题目分值 */
    private Integer score;
    
    /** 题目分类ID */
    private Long categoryId;
    
    /** 标签ID集合 */
    private String tagIds;
    
    /** 关联课程ID */
    private Long courseId;
    
    /** 状态(0禁用 1启用) */
    private Integer status;
}
