package com.a104.freeproject.Statgps.service;

import com.a104.freeproject.Challenge.entity.Challenge;
import com.a104.freeproject.Challenge.repository.ChallengeRepository;
import com.a104.freeproject.Log.entity.Log;
import com.a104.freeproject.Log.repository.LogRepository;
import com.a104.freeproject.Member.entity.Member;
import com.a104.freeproject.Member.service.MemberServiceImpl;
import com.a104.freeproject.PrtChl.entity.PrtChl;
import com.a104.freeproject.PrtChl.repository.PrtChlRepository;
import com.a104.freeproject.Statbpm.response.BpmListResponse;
import com.a104.freeproject.Statbpm.response.BpmResultResponse;
import com.a104.freeproject.Statgps.entity.Statgps;
import com.a104.freeproject.Statgps.repository.StatgpsRepository;
import com.a104.freeproject.Statgps.request.GpsInputRequest;
import com.a104.freeproject.Statgps.response.GpsMiddleInterface;
import com.a104.freeproject.Statgps.response.GpsResultResponse;
import com.a104.freeproject.Statgps.response.ResultResponse;
import com.a104.freeproject.advice.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StatgpsServiceImpl implements StatgpsService{

    private final MemberServiceImpl memberService;
    private final ChallengeRepository challengeRepository;
    private final PrtChlRepository prtChlRepository;
    private final StatgpsRepository statgpsRepository;
    private final LogRepository logRepository;
    private final static int  EARTH_RADIUS = 6371;

    @Override
    public boolean addData(GpsInputRequest input, HttpServletRequest req) throws NotFoundException {

        Member member = memberService.findEmailbyToken(req);
        if(!challengeRepository.existsById(input.getChlId()))
            throw new NotFoundException("?????? ???????????? ???????????? ????????????.");
        Challenge c = challengeRepository.findById(input.getChlId()).get();

        if(!prtChlRepository.existsByChallengeAndMember(c,member))
            throw new NotFoundException("???????????? ?????? ??????????????????.");
        PrtChl p = prtChlRepository.findByChallengeAndMember(c,member);

        Statgps statgps = Statgps.builder().prtChl(p)
                .time(input.getTime().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .lat(input.getLat())
                .lng(input.getLng())
                .success(false)
                .chk(input.getChk().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .build();

        statgpsRepository.save(statgps);

        return true;
    }

    @Override
    public ResultResponse getTryList(String year, String month, String day, Long cid, HttpServletRequest req) throws NotFoundException {

        Member member = memberService.findEmailbyToken(req);

        if(!challengeRepository.existsById(cid))
            throw new NotFoundException("?????? ???????????? ???????????? ????????????.");
        Challenge c = challengeRepository.findById(cid).get();
        if(c.isFin()) throw new NotFoundException("?????? ????????? ??????????????????.");

        if(!prtChlRepository.existsByChallengeAndMember(c,member))
            throw new NotFoundException("???????????? ?????? ??????????????????.");
        PrtChl p = prtChlRepository.findByChallengeAndMember(c,member);
        if(p.is_fin()) throw new NotFoundException("?????? ?????? ????????? ?????????.");

        LocalDate date = LocalDate.of(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day));

        if(!logRepository.existsByPrtChlAndDate(p,date))
            return ResultResponse.builder()
                    .flag(false)
                    .gpsList(new LinkedList<GpsResultResponse>())
                    .total_dist(0)
                    .time_len("0")
                    .avg_speed(0)
                    .build();

        Log log = logRepository.findByPrtChlAndDate(p,date);
        List<GpsMiddleInterface> statgpsList = statgpsRepository.findByChkAndPrtChl(date.toString(),p);

        if(statgpsList.size()==0)
            return ResultResponse.builder()
                    .flag(false)
                    .gpsList(new LinkedList<GpsResultResponse>())
                    .total_dist(0)
                    .time_len("0")
                    .avg_speed(0)
                    .build();

        boolean flag = statgpsList.get(statgpsList.size()-1).getSuccess();
        LocalDateTime sendTime = statgpsList.get(statgpsList.size()-1).getChk();
        if(!flag){
            for(int i = statgpsList.size()-2;i>=0;i--){
                boolean f = statgpsList.get(i).getSuccess();
                if(f) {
                    flag = f;
                    sendTime = statgpsList.get(i).getChk();
                    break;
                }
            }
        }

        List<Statgps> list = statgpsRepository.findByChkTimeAndPrtChl(p,sendTime);
        List<GpsResultResponse> output = new LinkedList<>();
        for(Statgps statgps:list){
            output.add(GpsResultResponse.builder()
                    .lat(statgps.getLat()).lng(statgps.getLng()).time(statgps.getTime())
                    .build());
        }
        LocalDateTime st=LocalDateTime.now(), ed=LocalDateTime.now();
        if (list.size() != 0) {
            st = list.get(0).getTime();
            ed = list.get(list.size()-1).getTime();
        }

        long hour = ChronoUnit.HOURS.between(st, ed);
        long minute = ChronoUnit.MINUTES.between(st, ed)%60;
        long sec = ChronoUnit.SECONDS.between(st, ed)%60;

        String h,m,s;
        if(hour<=9) h = "0"+hour;
        else h = ""+hour;

        if(minute<=9) m = "0"+minute;
        else m = ""+minute;

        if(sec<=9) s = "0"+sec;
        else s = ""+sec;

        String timelen = h+":"+m+":"+s;

        double dist=0;

        long beforeTime = System.currentTimeMillis(); //?????? ?????? ?????? ?????? ????????????

        // =============================================================

        // ????????? ????????? ?????? ???
        // OUTPUT??? LAT, LNG, TIME(LOCALDATETIME) ?????? ???????????? list. ???????????? ?????? ?????? ??????
        // ????????? ????????? dist??? ???????????? ???. ????????? ????????? ?????????. ex) dist = 123;
        // ????????? ?????????
        // =============================================================
        System.out.println();
        for (int i=0; i< output.size()-1; i++){
            GpsResultResponse now = output.get(i);
            GpsResultResponse next = output.get(i+1);
            dist += getDistance(now.getLat(), now.getLng(), next.getLat(), next.getLng());
        }

        //?????? ???????????? ?????? ????????? ??????
        long afterTime = System.currentTimeMillis(); // ?????? ?????? ?????? ?????? ????????????
        long secDiffTime = (afterTime - beforeTime)/1000; //??? ????????? ??? ??????
        System.out.println("GPS ?????? ?????? : "+secDiffTime);

        double speed = 0;
        if(list.size() == 0){
            speed=0;
        }
        else {
            speed = dist/ChronoUnit.SECONDS.between(st, ed);
        }

        return ResultResponse.builder()
                .flag(flag)
                .gpsList(output)
                .total_dist(dist)
                .time_len(timelen)
                .avg_speed(speed)
                .build();
    }

    @Override
    public ResultResponse findRank(PrtChl p) throws NotFoundException {

        LocalDate date = LocalDate.now(ZoneId.of("Asia/Seoul"));

        Log log = logRepository.findByPrtChlAndDate(p,date);
        List<GpsMiddleInterface> statgpsList = statgpsRepository.findByChkAndPrtChl(date.toString(),p);

        if(statgpsList.size()==0)
            return ResultResponse.builder()
                    .flag(false)
                    .gpsList(new LinkedList<GpsResultResponse>())
                    .total_dist(0)
                    .time_len("0")
                    .avg_speed(0)
                    .build();

        boolean flag = statgpsList.get(statgpsList.size()-1).getSuccess();
        LocalDateTime sendTime = statgpsList.get(statgpsList.size()-1).getChk();
//        if(!flag){
            for(int i = statgpsList.size()-2;i>=0;i--){
                boolean f = statgpsList.get(i).getSuccess();
                if(f) {
                    flag = f;
                    sendTime = statgpsList.get(i).getChk();
                    break;
                }
            }
//        }

        List<Statgps> list = statgpsRepository.findByChkTimeAndPrtChl(p,sendTime);
        List<GpsResultResponse> output = new LinkedList<>();
        for(Statgps statgps:list){
            output.add(GpsResultResponse.builder()
                    .lat(statgps.getLat()).lng(statgps.getLng()).time(statgps.getTime())
                    .build());
        }
        LocalDateTime st=LocalDateTime.now(), ed=LocalDateTime.now();
        if (list.size() != 0) {
            st = list.get(0).getTime();
            ed = list.get(list.size()-1).getTime();
        }

        long hour = ChronoUnit.HOURS.between(st, ed);
        long minute = ChronoUnit.MINUTES.between(st, ed)%60;
        long sec = ChronoUnit.SECONDS.between(st, ed)%60;

        String h,m,s;
        if(hour<=9) h = "0"+hour;
        else h = ""+hour;

        if(minute<=9) m = "0"+minute;
        else m = ""+minute;

        if(sec<=9) s = "0"+sec;
        else s = ""+sec;

        String timelen = h+":"+m+":"+s;

        double dist=0;

        for (int i=0; i< output.size()-1; i++){
            GpsResultResponse now = output.get(i);
            GpsResultResponse next = output.get(i+1);
            dist += getDistance(now.getLat(), now.getLng(), next.getLat(), next.getLng());
        }

        double speed = 0;
        if(list.size() == 0){
            speed=0;
        }
        else {
            speed = dist/ChronoUnit.SECONDS.between(st, ed);
        }

        return ResultResponse.builder()
                .flag(flag)
                .gpsList(output)
                .total_dist(dist)
                .time_len(timelen)
                .avg_speed(speed)
                .build();
    }

    //gps ?????? ????????? ??????
    public static double getDistance(String lat1_s, String lng1_s, String lat2_s, String lng2_s) {

        double lat1 = Double.parseDouble(lat1_s);
        double lng1 = Double.parseDouble(lng1_s);
        double lat2 = Double.parseDouble(lat2_s);
        double lng2 = Double.parseDouble(lng2_s);

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat/2)* Math.sin(dLat/2)+ Math.cos(Math.toRadians(lat1))* Math.cos(Math.toRadians(lat2))* Math.sin(dLon/2)* Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = EARTH_RADIUS * c * 1000;    // ????????? ?????????
        return d;
    }

}
