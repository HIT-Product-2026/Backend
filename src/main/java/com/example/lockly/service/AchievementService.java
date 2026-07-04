package com.example.lockly.service;

import com.example.lockly.domain.entity.main.Profile;

public interface AchievementService {

    // Danh hiệu "Nhà thám hiểm"
    boolean isExplorer();

    // Danh hiệu "Kế thừa di sản"
    boolean isLegacyInheritor();

    // Danh hiệu "Được lòng dân, bình thiên hạ"
    boolean isPopularLeader();

    // Danh hiệu "Kỷ luật thép"
    boolean isDisciplinedSteel();

    // Danh hiệu "Bạn của tôi, cậu còn nhớ chứ?"
    boolean isOldFriend();

    // Cup "Bốn bể là nhà"
    boolean isFourSeasHome();

    // Cup "Đông phương bất bại"
    boolean isEasternUndefeated();

    // Cup "Lời hứa năm xưa"
    boolean isOldPromise();

    void refreshProfileCups(Profile profile);
}
