package ai.startup.usuario.badge;

import java.time.LocalDateTime;

public record BadgeDTO(
    String id,
    String userId,
    String badgeId,
    String category,
    String title,
    String rarity,
    String img,
    Integer current,
    Integer target,
    LocalDateTime earnedAt,
    Boolean claimed,
    Integer xpReward,
    Integer coinsReward
) {}

