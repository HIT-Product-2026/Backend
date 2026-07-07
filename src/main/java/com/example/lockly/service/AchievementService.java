package com.example.lockly.service;

import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;

public interface AchievementService {

    // Danh hiệu "Nhà thám hiểm"
    boolean isExplorer(User user, Profile profile);

    // Danh hiệu "Kế thừa di sản"
    boolean isLegacyInheritor(User user, Profile profile);

    // Danh hiệu "Được lòng dân, bình thiên hạ"
    boolean isPopularLeader(User user, Profile profile);

    // Danh hiệu "Kỷ luật thép"
    boolean isDisciplinedSteel(User user, Profile profile);

    // Danh hiệu "Bạn của tôi, cậu còn nhớ chứ?"
    boolean isOldFriend(User user, Profile profile);

    // Cup "Bốn bể là nhà"
    boolean isFourSeasHome(User user, Profile profile);

    // Cup "Đông phương bất bại"
    boolean isEasternUndefeated(User user, Profile profile);

    void refreshProfileCups(User user);
}
