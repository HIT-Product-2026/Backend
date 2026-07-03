package com.example.lockly.domain.entity.enumEntity;

public enum AchievementName {

    // Title
    EXPLORER("Nhà thám hiểm"),
    LEGACY_INHERITOR("Kế thừa di sản"),
    POPULAR_LEADER("Được lòng dân, bình thiên hạ"),
    DISCIPLINED_STEEL("Kỷ luật thép"),
    OLD_FRIEND("Bạn của tôi, cậu còn nhớ chứ?"),

    // Cup
    FOUR_SEAS_HOME("Bốn bể là nhà"),
    EASTERN_UNDEFEATED("Đông phương bất bại"),
    OLD_PROMISE("Lời hứa năm xưa");

    private final String displayName;

    AchievementName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}