package org.example.controller;


import lombok.extern.slf4j.Slf4j;
import org.example.Enum.ActivityRankTimeEnum;
import org.example.Enum.BaseExceptionEnum;
import org.example.annotation.AccessLimit;
import org.example.exception.BaseException;
import org.example.pojo.RankItemDTO;
import org.example.pojo.RankListVO;

import org.example.result.ResultData;
import org.example.service.RankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class RankController {
    @Autowired
    private RankService rankService;


    /**
     * 获取排行榜
     * @return
     */
    @RequestMapping("/rank")
    public ResultData<RankListVO> rank(){


        List<RankItemDTO> todayRank = rankService.queryTopRankList(ActivityRankTimeEnum.DAY,30);
        List<RankItemDTO> monthRank = rankService.queryTopRankList(ActivityRankTimeEnum.MONTH,30);
        RankListVO rankListVO=new RankListVO();
        rankListVO.setDayRankList(todayRank);
        rankListVO.setDayRankList(monthRank);
        return ResultData.success(rankListVO);
    }

    /**
     * 更新用户活跃度
     * @param userId
     * @param activityItem
     * @return
     */
    @AccessLimit(second = 10,maxRequestCount = 3,forbiddenTime = 120)
    @RequestMapping("/rank/updateUserActivity")
    public ResultData updateUserActivity(@RequestParam("userId") Long userId,@RequestParam("activityItem") String activityItem)  {

        if(userId==null){

            throw new BaseException(BaseExceptionEnum.USER_NOT_EXISTS);
        }

        rankService.addActivityScore(userId,activityItem);

        return ResultData.success();
    }



}
