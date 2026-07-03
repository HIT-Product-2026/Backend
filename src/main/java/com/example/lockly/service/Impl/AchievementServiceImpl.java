package com.example.lockly.service.Impl;

import com.example.lockly.domain.entity.Achievement;
import com.example.lockly.domain.entity.Profile;
import com.example.lockly.domain.entity.enumEntity.AchievementName;
import com.example.lockly.domain.entity.enumEntity.AchievementType;
import com.example.lockly.repository.AchievementRepository;
import com.example.lockly.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;

    // Danh hiệu "Nhà thám hiểm"
    @Override
    public boolean isExplorer(){

    }

    // Danh hiệu "Kế thừa di sản"
    @Override
    public boolean isLegacyInheritor(){}

    // Danh hiệu "Được lòng dân, bình thiên hạ"
    @Override
    public boolean isPopularLeader(){}

    // Danh hiệu "Kỷ luật thép"
    @Override
    public boolean isDisciplinedSteel(){}

    // Danh hiệu "Bạn của tôi, cậu còn nhớ chứ?"
    @Override
    public boolean isOldFriend(){}


    // Cup "Bốn bể là nhà"
    @Override
    public boolean isFourSeasHome(){}

    // Cup "Đông phương bất bại"
    @Override
    public boolean isEasternUndefeated(){}

    // Cup "Lời hứa năm xưa"
    @Override
    public boolean isOldPromise(){}


    @Override
    @Transactional
    public void refreshProfileCups(Profile profile) {
        achievementRepository.deleteByProfile(profile);

        // Tính toán cup
        List<Achievement> achievementList = new ArrayList<>();

        // Danh hiệu "Nhà thám hiểm
        if (isExplorer()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.EXPLORER)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        // Danh hiệu "Kế thừa di sản"
        if (isLegacyInheritor()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.LEGACY_INHERITOR)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        // Danh hiệu "Được lòng dân, bình thiên hạ"
        if (isPopularLeader()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.POPULAR_LEADER)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        // Danh hiệu "Kỷ luật thép"
        if (isDisciplinedSteel()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.DISCIPLINED_STEEL)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        // Danh hiệu "Bạn của tôi, cậu còn nhớ chứ?"
        if (isOldFriend()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.OLD_FRIEND)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }


        // Cup "Bốn bể là nhà"
        if (isFourSeasHome()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.FOUR_SEAS_HOME)
                            .type(AchievementType.CUP)
                            .build()
            );
        }

        // Cup "Đông phương bất bại"
        if (isEasternUndefeated()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.EASTERN_UNDEFEATED)
                            .type(AchievementType.CUP)
                            .build()
            );
        }

        // Cup "Lời hứa năm xưa"
        if (isOldPromise()) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.OLD_PROMISE)
                            .type(AchievementType.CUP)
                            .build()
            );
        }

        achievementRepository.saveAll(achievementList);
    }
}
