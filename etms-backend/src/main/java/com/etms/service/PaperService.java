package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Paper;

/**
 * 试卷服务接口
 */
public interface PaperService extends IService<Paper> {
    
    /**
     * 分页查询试卷列表
     */
    Page<Paper> pagePapers(Page<Paper> page, String paperName, String paperCode, Integer status);
    
    /**
     * 获取试卷详情（含题目）
     */
    Object getPaperDetail(Long id);
    
    /**
     * 新增试卷
     */
    void addPaper(Paper paper);
    
    /**
     * 更新试卷
     */
    void updatePaper(Paper paper);
    
    /**
     * 删除试卷
     */
    void deletePaper(Long id);
    
    /**
     * 发布试卷
     */
    void publishPaper(Long id);
    
    /**
     * 停用试卷
     */
    void disablePaper(Long id);
}
