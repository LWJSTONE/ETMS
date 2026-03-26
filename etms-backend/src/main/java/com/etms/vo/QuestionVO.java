package com.etms.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 题目响应VO
 */
@Data
public class QuestionVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 题目ID */
    private Long id;
    
    /** 题目编码 */
    private String questionCode;
    
    /** 题目类型 */
    private Integer questionType;
    
    /** 题目类型名称 */
    private String questionTypeName;
    
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
    
    /** 难度 */
    private Integer difficulty;
    
    /** 难度名称 */
    private String difficultyName;
    
    /** 题目分值 */
    private Integer score;
    
    /** 关联课程ID */
    private Long courseId;
    
    /** 题目分类ID */
    private Long categoryId;
    
    /** 标签ID集合 */
    private String tagIds;
    
    /** 状态 */
    private Integer status;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
