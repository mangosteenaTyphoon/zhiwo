package com.shanzhu.service;

import com.shanzhu.model.dto.ShanzhuReviewQueryDTO;
import com.shanzhu.model.vo.ShanzhuReviewVO;

public interface ShanzhuReviewService {
    /**
     * 查询复盘分析报告
     */
    ShanzhuReviewVO queryReview(ShanzhuReviewQueryDTO queryDTO);
}
