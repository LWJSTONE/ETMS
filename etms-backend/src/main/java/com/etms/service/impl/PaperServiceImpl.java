package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.ExamRecord;
import com.etms.entity.Paper;
import com.etms.mapper.ExamRecordMapper;
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
    
    private final ExamRecordMapper examRecordMapper;
    
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
        // 检查试卷编码是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Paper>().eq(Paper::getPaperCode, paper.getPaperCode())
        );
        if (count > 0) {
            throw new RuntimeException("试卷编码已存在");
        }
        
        paper.setStatus(0); // 草稿状态
        baseMapper.insert(paper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaper(Paper paper) {
        // 检查试卷编码是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Paper>()
                .eq(Paper::getPaperCode, paper.getPaperCode())
                .ne(Paper::getId, paper.getId())
        );
        if (count > 0) {
            throw new RuntimeException("试卷编码已存在");
        }
        
        baseMapper.updateById(paper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long id) {
        // 检查试卷是否有考试记录
        Long count = examRecordMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getPaperId, id)
        );
        if (count > 0) {
            throw new RuntimeException("试卷存在考试记录，无法删除");
        }
        
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
