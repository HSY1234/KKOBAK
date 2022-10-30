package com.a104.freeproject.Challenge.response;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChallengeListResponse {
    private Long id;
    private Long categoryId;
    private Long detailCategoryId;
    private Long writer;
    private String title;
    private String contents;
    private String imgurl;
    private boolean isWatch;
    private int roomtype;
    private String password;
    private int limitPeolple;
    private int currentNum;
    private String alarm;
    private int goal;
    private String unit;
    private boolean isFin;
    private String nickName;
    private Timestamp startTime;
    private Timestamp endTime;
    private List<String> tagList;
}
