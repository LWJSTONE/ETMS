package com.etms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 成绩统计视图对象
 */
@Data
public class ExamResultStatsVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 总考试人次 */
    private Long totalCount;
    
    /** 通过人数 */
    private Long passCount;
    
    /** 未通过人数 */
    private Long failCount;
    
    /** 通过率 */
    private Double passRate;
    
    /** 平均分 */
    private Double avgScore;
}
