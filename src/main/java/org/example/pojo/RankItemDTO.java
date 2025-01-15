package org.example.pojo;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RankItemDTO {
    /**
     * 用户
     */
    private Long userId;

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 评分
     */
    private Integer score;
}
