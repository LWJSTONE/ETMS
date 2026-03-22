package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Paper;
import com.etms.mapper.PaperMapper;
import com.etms.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 试卷服务实现类
 */
@Service
@RequiredArgsConstructor
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {
    
    @Override
    public Page<Paper> pagePapers(Page<Paper> page, String paperName, String paperCode, Integer status) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(paperName), Paper::getPaperName, paperName)
               .like(StringUtils.hasText(paperCode), Paper::getPaperCode, paperCode)
               .eq(status != null, Paper::getStatus, status)
               .orderByDesc(Paper::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Object getPaperDetail(Long id) {
        Paper paper = baseMapper.selectById(id);
        // TODO: 查询试卷关联的题目
        return paper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPaper(Paper paper) {
        paper.setStatus(0); // 草稿状态
        baseMapper.insert(paper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaper(Paper paper) {
        baseMapper.updateById(paper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long id) {
        baseMapper.deleteById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPaper(Long id) {
        Paper paper = new Paper();
        paper.setId(id);
        paper.setStatus(1); // 已发布
        baseMapper.updateById(paper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disablePaper(Long id) {
        Paper paper = new Paper();
        paper.setId(id);
        paper.setStatus(3); // 已停用
        baseMapper.updateById(paper);
    }
}
