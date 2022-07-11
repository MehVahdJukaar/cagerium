package net.mehvahdjukaar.cagerium.common;

import net.mehvahdjukaar.cagerium.Cagerium;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public enum Tier {
    PASSIVE, MOBS, BOSSES;

    public boolean acceptsEntityType(EntityType<?> type) {
        if (type.is(Cagerium.BOSSES)) return this == BOSSES;
        if (type.getCategory() == MobCategory.MONSTER) return this != PASSIVE;
        return this == PASSIVE;
    }
}
