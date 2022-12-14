package com.a104.freeproject.Challenge.response;

import com.a104.freeproject.HashTag.entity.Hashtag;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChlSimpleResponse {

    private Long ChlId;
    private String title;
    private String imgurl;
    private boolean isWatch;
    private int roomtype;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startTime;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endTime;
    List<String> tags;
}
