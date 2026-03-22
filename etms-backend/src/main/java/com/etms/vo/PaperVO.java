package com.etms.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷VO
 */
@Data
public class PaperVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 试卷ID */
    private Long id;

    /** 试卷名称 */
    private String paperName;

    /** 试卷编码 */
    private String paperCode;

    /** 总分 */
    private Integer totalScore;

    /** 及格分 */
    private Integer passScore;

    /** 考试时长(分钟) */
    private Integer duration;

    /** 题目数量 */
    private Integer questionCount;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 试卷描述 */
    private String description;

    /** 状态(0草稿 1已发布 2已停用) */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 试卷题目列表 */
    private List<PaperQuestionVO> questions;
}
