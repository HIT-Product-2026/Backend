package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.response.PostEmojiCount;
import com.example.lockly.domain.entity.main.Achievement;
import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.AchievementName;
import com.example.lockly.domain.entity.main.enumEntity.AchievementType;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.repository.main.*;
import com.example.lockly.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final FriendshipsRepository friendshipsRepository;
    private final ProfileRepository profileRepository;
    private final EmojiPostRepository emojiPostRepository;

    // Danh hiệu "Nhà thám hiểm"
    @Override
    public boolean isExplorer(User user, Profile profile) {

        List<UUID> friendIds = friendshipsRepository.findFriendIds(
                user.getId(),
                FriendshipStatus.ACCEPTED
        );

        if (friendIds.isEmpty()) {
            return false;
        }

        int maxCityVisited = profileRepository.findByUserIdIn(friendIds)
                .stream()
                .mapToInt(p -> p.getCityCodeList().size())
                .max()
                .orElse(0);

        return profile.getCityCodeList().size() >= maxCityVisited;
    }

    // Danh hiệu "Kế thừa di sản"
    @Override
    public boolean isLegacyInheritor(User user, Profile profile){

        // Số bài viết được kế thừa
        long countPostReup = profile.getCountPostReup();

        long countFriends = friendshipsRepository
                .countFriendships(
                        user.getId(),
                        FriendshipStatus.ACCEPTED
                );

        if (countFriends <= 10) {
            return countPostReup >= 100;
        }

        return countPostReup >= 2 * countFriends;
    }

    // Danh hiệu "Được lòng dân, bình thiên hạ"
    @Override
    public boolean isPopularLeader(User user, Profile profile){

        PostEmojiCount result = emojiPostRepository
                .findTopPostWithEmojiCount(
                        user.getId(),
                        LocalDateTime.now().minusDays(90),
                        PageRequest.of(0,1)
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (result == null) {
            return false;
        }

        long countFriends = friendshipsRepository.countFriendships(
                user.getId(),
                FriendshipStatus.ACCEPTED
        );

        if (countFriends == 0) {
            return false;
        }

        return result.emojiCount() >= countFriends;
    }

    // Danh hiệu "Kỷ luật thép"
    @Override
    public boolean isDisciplinedSteel(User user, Profile profile) {

        List<UUID> friendIds = friendshipsRepository.findFriendIds(
                user.getId(),
                FriendshipStatus.ACCEPTED
        );

        if (friendIds.isEmpty()) {
            return false;
        }

        List<Profile> friendProfiles = profileRepository.findByUserIdIn(friendIds);

        int maxCurrentStreak = friendProfiles.stream()
                .mapToInt(Profile::getCurrentPostStreak)
                .max()
                .orElse(0);

        return profile.getCurrentPostStreak() >= maxCurrentStreak;
    }


    // Cup "Bạn của tôi, cậu còn nhớ chứ?"
    @Override
    public boolean isOldFriend(User user, Profile profile) {

        List<UUID> friendIds = friendshipsRepository.findFriendIds(
                user.getId(),
                FriendshipStatus.ACCEPTED
        );

        if (friendIds.isEmpty()) {
            return false;
        }

        return profile.getPhotographedFriendIds().containsAll(friendIds);
    }

    // Cup "Bốn bể là nhà"
    @Override
    public boolean isFourSeasHome(User user, Profile profile) {
        return profile.getCityCodeList().size() >= 34;
    }

    // Cup "Đông phương bất bại"
    @Override
    public boolean isEasternUndefeated(User user, Profile profile){
        return false;
    }


    @Override
    @Transactional
    public void refreshProfileCups(User user) {

        Profile profile = profileRepository
                .findByUser(user)
                .orElse(null);

        if (profile == null) {
            return;
        }

        achievementRepository.deleteByProfile(profile);

        // Cập nhật favorite post trước khi xét achievement
        PostEmojiCount result = emojiPostRepository
                .findTopPostWithEmojiCount(
                        user.getId(),
                        LocalDateTime.now().minusDays(90),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElse(null);

        profile.setFavoritePost(result == null ? null : result.post());

        List<Achievement> achievementList = new ArrayList<>();

        // ===== Danh hiệu =====

        if (isExplorer(user, profile)) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.EXPLORER)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        if (isLegacyInheritor(user, profile)) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.LEGACY_INHERITOR)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        if (isPopularLeader(user, profile)) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.POPULAR_LEADER)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        if (isDisciplinedSteel(user, profile)) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.DISCIPLINED_STEEL)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        if (isOldFriend(user, profile)) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.OLD_FRIEND)
                            .type(AchievementType.TITLE)
                            .build()
            );
        }

        // ===== Cup =====

        if (isFourSeasHome(user, profile)) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.FOUR_SEAS_HOME)
                            .type(AchievementType.CUP)
                            .build()
            );
        }

        if (isEasternUndefeated(user, profile)) {
            achievementList.add(
                    Achievement.builder()
                            .profile(profile)
                            .achievementName(AchievementName.EASTERN_UNDEFEATED)
                            .type(AchievementType.CUP)
                            .build()
            );
        }

        // Lưu achievement
        achievementRepository.saveAll(achievementList);

        // Reset tiến trình mùa mới
        resetSeasonProgress(profile);

        // Lưu profile
        profileRepository.save(profile);
    }

    private void resetSeasonProgress(Profile profile) {
        profile.getPhotographedFriendIds().clear();
        profile.getVisitedProfileIds().clear();
        profile.getVisitorProfileIds().clear();
        profile.getReupPostIds().clear();

        profile.setCountPostReup(0);
    }
}
