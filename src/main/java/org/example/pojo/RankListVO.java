package org.example.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RankListVO {

    private List<RankItemDTO> dayRankList;

    private List<RankItemDTO> monthRankList;
}
