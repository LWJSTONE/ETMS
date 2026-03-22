package com.etms.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 试卷题目VO
 */
@Data
public class PaperQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;

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

    /** 该题分数 */
    private Integer score;

    /** 题目排序 */
    private Integer sortOrder;
}
