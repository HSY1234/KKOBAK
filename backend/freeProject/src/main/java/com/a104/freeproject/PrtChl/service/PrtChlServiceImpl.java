package com.a104.freeproject.PrtChl.service;

import com.a104.freeproject.Challenge.entity.Challenge;
import com.a104.freeproject.Challenge.entity.ChlTime;
import com.a104.freeproject.Challenge.repository.ChallengeRepository;
import com.a104.freeproject.Challenge.repository.ChlTimeRepository;
import com.a104.freeproject.Challenge.response.ChallengeListResponse;
import com.a104.freeproject.HashTag.entity.ChlTag;
import com.a104.freeproject.Log.entity.Log;
import com.a104.freeproject.Log.repository.LogRepository;
import com.a104.freeproject.Log.service.LogServiceImpl;
import com.a104.freeproject.Member.entity.Member;
import com.a104.freeproject.Member.service.MemberServiceImpl;
import com.a104.freeproject.PrtChl.entity.PrtChl;
import com.a104.freeproject.PrtChl.repository.PrtChlRepository;
import com.a104.freeproject.PrtChl.request.ChlRequest;
import com.a104.freeproject.PrtChl.response.LikeDetail;
import com.a104.freeproject.PrtChl.response.MyChallengeDetailResponse;
import com.a104.freeproject.advice.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

@Service
@Transactional
@RequiredArgsConstructor
public class PrtChlServiceImpl implements PrtChlService{

    private final ChallengeRepository challengeRepository;
    private final PrtChlRepository prtChlRepository;
    private final MemberServiceImpl memberService;
    private final ChlTimeRepository chlTimeRepository;
    private final LogServiceImpl logService;
    private final LogRepository logRepository;

    @Override
    public boolean participate(Long cid, HttpServletRequest req, int alarm) throws NotFoundException {
        if(!challengeRepository.existsById(cid)) throw new NotFoundException("존재하지 않는 챌린지입니다.");
        Challenge c = challengeRepository.findById(cid).get();
        Member member = memberService.findEmailbyToken(req);

        Timestamp time = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
        sdf.setTimeZone(tz);
        Timestamp now = Timestamp.valueOf(sdf.format(time));

        if(!chlTimeRepository.existsByChallenge(c)) throw new NotFoundException("챌린지 스케쥴러에 존재하지 않습니다. 이 문제가 발생하면 백엔드에 문의하세용");
        ChlTime chl = chlTimeRepository.findByChallenge(c);

        if(prtChlRepository.existsByChallengeAndMember(c,member)) throw new NotFoundException("이미 가입한 챌린지입니다.");
        prtChlRepository.save(PrtChl.builder().challenge(c).member(member)
                .is_fin(false).startDate(now.toLocalDateTime().toLocalDate()).alarmDir(alarm)
                .endDate(chl.getEndTime().toLocalDateTime().toLocalDate()).sucDay(0).failDay(0).build());
        Long prtId = prtChlRepository.findByChallengeAndMember(c,member).getId();

        // 로그 생성
        logService.createLog(prtChlRepository.findById(prtId).get());

        return true;
    }

    @Override
    public boolean dropChl(Long cid, HttpServletRequest req) throws NotFoundException {

        if(!challengeRepository.existsById(cid)) throw new NotFoundException("존재하지 않는 챌린지입니다.");
        Challenge c = challengeRepository.findById(cid).get();
        Member member = memberService.findEmailbyToken(req);

        if(!prtChlRepository.existsByChallengeAndMember(c,member))
            throw new NotFoundException("가입하지 않은 챌린지입니다.");

        PrtChl p = prtChlRepository.findByChallengeAndMember(c,member);
        p.set_fin(true);
        prtChlRepository.save(p);

        return true;
    }

    @Override
    public boolean changeKkobak(ChlRequest chlRequest, HttpServletRequest req) throws NotFoundException{
        long cid = chlRequest.getChlId();
        if(!challengeRepository.existsById(cid)) throw new NotFoundException("존재하지 않는 챌린지입니다.");
        Challenge c = challengeRepository.findById(cid).get();
        Member member = memberService.findEmailbyToken(req);
        if(!prtChlRepository.existsByChallengeAndMember(c,member))
            throw new NotFoundException("가입하지 않은 챌린지입니다.");
        PrtChl p = prtChlRepository.findByChallengeAndMember(c,member);

        if(p.getKkobak()==0){
            p.setKkobak(1);
        }
        else if(p.getKkobak()==1){
            p.setKkobak(0);
        }

//        prtChlRepository.save(p);
        return true;
    }

    @Override
    public MyChallengeDetailResponse getPtrChlDetail(Long cid, HttpServletRequest req) throws NotFoundException{
        if(!challengeRepository.existsById(cid)) throw new NotFoundException("존재하지 않는 챌린지입니다.");
        Challenge c = challengeRepository.findById(cid).get();
        Member member = memberService.findEmailbyToken(req);
        if(!prtChlRepository.existsByChallengeAndMember(c,member))
            throw new NotFoundException("가입하지 않은 챌린지입니다.");
        PrtChl p = prtChlRepository.findByChallengeAndMember(c,member);

        List<ChlTag> tagList= c.getTagList();
        List<String> tagListName = new ArrayList<>();
        for (ChlTag a: tagList){
            tagListName.add(a.getHashtag().getName());
        }

        MyChallengeDetailResponse result = MyChallengeDetailResponse.builder()
                .id(c.getId())
                .categoryId(c.getCategory().getId())
                .detailCategoryId(c.getDetailCategory().getId())
                .writer(c.getWriter())
                .title(c.getTitle())
                .contents(c.getContents())
                .imgurl(c.getImgurl())
                .watch(c.isWatch())
                .roomtype(c.getRoomtype())
                .password(c.getPassword())
                .limitPeolple(c.getLimitPeople())
                .currentNum(c.getCurrentNum())
                .alarm(c.getAlarm())
                .goal(c.getGoal())
                .unit(c.getUnit())
                .isFin(c.isFin())
                .nickName(member.getNickname())
                .startTime(c.getChlTime().getStartTime())
                .endTime(c.getChlTime().getEndTime())
                .tagList(tagListName)
                .kkobak(p.getKkobak())
                .build();

        return result;
    }

    @Override
    public List<LikeDetail> getLikeList(HttpServletRequest req) throws NotFoundException {

        Member member = memberService.findEmailbyToken(req);
        List<PrtChl> prtChlList = prtChlRepository.findByMember(member);

        List<LikeDetail> output = new LinkedList<>();
        for(PrtChl p : prtChlList){
            if(p.is_fin()||p.getKkobak()==0) continue;
            Challenge c = p.getChallenge();
            if(c.isFin()) continue;
            ChlTime chlTime = c.getChlTime();

            output.add(LikeDetail.builder()
                    .chlId(c.getId()).title(c.getTitle()).imgurl(c.getImgurl()).watch(c.isWatch())
                    .roomtype(c.getRoomtype()).detailCategoryId(c.getDetailCategory().getId())
                    .startTime(chlTime.getStartTime().toLocalDateTime().toLocalDate())
                    .endTime(chlTime.getEndTime().toLocalDateTime().toLocalDate())
                    .build());
        }

        return output;
    }

    @Override
    public boolean test() {

        List<PrtChl> pList = prtChlRepository.findAll();
        for (PrtChl p : pList){
            List<Log> logs = p.getLogs();
            int sucDay = 0, FailDay = 0;
            for (Log log : logs){
                if(log.isFin()) sucDay++;
                else FailDay++;
            }
            p.setFailDay(FailDay);
            p.setSucDay(sucDay);
        }

        return true;
    }
}
