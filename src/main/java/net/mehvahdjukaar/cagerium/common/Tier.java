package net.mehvahdjukaar.cagerium.common;

import net.mehvahdjukaar.cagerium.Cagerium;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public enum Tier {
    PASSIVE, MOBS, BOSSES;

    public boolean acceptsEntityType(EntityType<?> type) {
        if(type.is(Cagerium.BLACKLIST))return false;
        return switch (this) {
            case BOSSES -> type.is(Cagerium.BOSSES);
            case MOBS -> type.getCategory() == MobCategory.MONSTER;
            case PASSIVE -> !type.is(Cagerium.BOSSES) && type.getCategory() != MobCategory.MONSTER;
        };
    }

    public float getHeight() {
        return this == BOSSES ? 4 / 16f : 2 / 16f;
    }
}
