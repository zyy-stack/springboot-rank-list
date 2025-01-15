package org.example.service;

import org.example.Enum.ActivityRankTimeEnum;
import org.example.pojo.RankItemDTO;

import java.util.List;

public interface RankService {

    void addActivityScore(Long userId,String activityItem);

    List<RankItemDTO> queryTopRankList(ActivityRankTimeEnum timeEnum,int topN);



}
