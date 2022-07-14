package net.mehvahdjukaar.cagerium.common;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.cagerium.mixins.FoxInvoker;
import net.mehvahdjukaar.cagerium.mixins.SlimeInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

import java.util.concurrent.TimeUnit;

public class MobData {

    public static ThreadLocal<LoadingCache<ResourceLocation, MobData>> MOB_CACHE = ThreadLocal.withInitial(() -> CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public MobData load(ResourceLocation key) {
                    return null;
                }
            }));

    //can be called on both sides for loot and item renderer

    //ideally it should never return nullable
    public static MobData getOrCreate(EntityType<?> type, Level level, BlockPos pos) {
        Mth.getSeed(pos);
        return getOrCreate(type.getRegistryName(), level, pos);
    }

    public static MobData getOrCreate(ResourceLocation type, Level level, BlockPos pos) {
        MobData en = MOB_CACHE.get().getIfPresent(type);
        if (en != null) {
            return en;
        } else {
            var entity = Registry.ENTITY_TYPE.getOptional(type);
            if (entity.isPresent()) {
                var m = create(entity.get(), level);
                MOB_CACHE.get().put(type, m);
                return m;
            }
        }
        return null;
    }


    public static MobData create(EntityType<?> type, Level level) {

        var entity = type.create(level);

        //prepares entity
        if (entity instanceof LivingEntity le) {
            le.yHeadRotO = 0;
            le.yHeadRot = 0;
            le.animationSpeed = 0;
            le.animationSpeedOld = 0;
            le.animationPosition = 0;
            le.hurtDuration = 0;
            le.hurtTime = 0;
            le.attackAnim = 0;
        }
        entity.setYRot(0);
        entity.yRotO = 0;
        entity.xRotO = 0;
        entity.setXRot(0);
        entity.clearFire();
        entity.invulnerableTime = 0;

        if (entity instanceof Bat bat) {
            //bat.setResting(true);
        }
        if (entity instanceof FoxInvoker foxInvoker) {
            foxInvoker.invokeSetSleeping(true);
        }
        if (entity instanceof SlimeInvoker slime) {
            slime.invokeSetSize(3, false);
        }

        var dim = calculateDimensions(entity, 0.85f, 0.8f, true);
        var dim1 = calculateDimensions(entity, 0.585f, 0.8f, false);
        var dim2 = calculateDimensions(entity, 0.5f, 0.7f, true);

        return new MobData(entity, dim, dim1, dim2);

    }

    private final Entity entity;
    private final Pair<Float, Float> fullDimensions;
    private final Pair<Float, Float> middleDimensions;
    private final Pair<Float, Float> halfDimensions;

    public MobData(Entity entity, Pair<Float, Float> fullDimensions,
                   Pair<Float, Float> middleDimensions, Pair<Float, Float> halfDimensions) {
        this.entity = entity;
        this.fullDimensions = fullDimensions;
        this.middleDimensions = middleDimensions;
        this.halfDimensions = halfDimensions;
    }

    public Entity getEntity() {
        return entity;
    }

    public float getScale(int level) {
        return switch (level) {
            default -> halfDimensions.getFirst();
            case 0 -> fullDimensions.getFirst();
            case 1 -> middleDimensions.getFirst();
        };
    }

    public float getYOffset(int level) {
        return switch (level) {
            default -> halfDimensions.getSecond();
            case 0 -> fullDimensions.getSecond();
            case 1 -> middleDimensions.getSecond();
        };
    }

    /**
     * get mob scale and vertical offset for a certain container
     */
    private static Pair<Float, Float> calculateDimensions(Entity mob, float width, float height, boolean enlargeAnimals) {
        float babyScale = 1;

        //hack
        if (Tier.BOSSES.acceptsEntityType(mob.getType())) {
            width *= 1.25;
        }

        if (mob instanceof LivingEntity le && le.isBaby()) {
            if ((mob instanceof Villager)) babyScale = 1.125f;
            else if (mob instanceof AgeableMob) babyScale = 2f;
            else babyScale = 1.125f;
        }


        float scale = 1;

        float w = mob.getBbWidth() * babyScale;
        float h = mob.getBbHeight() * babyScale;

        //cap.getHitBoxHeightIncrement();

        /*
        boolean isAir =
                mob.isNoGravity() || mob instanceof FlyingAnimal ||
                mob.isIgnoringBlockTriggers() || mob instanceof WaterAnimal;

         */
        boolean isAir = false; //disabled for now since mobs do not tick

        float aW = w; //+ cap.getHitBoxWidthIncrement();
        float aH = h; //+ cap.getHitBoxHeightIncrement();
        if (enlargeAnimals && mob instanceof Animal) {
            aW *= 1.4f;
            aH *= 1.125f;
        }


        //1 pixel margin
        float margin = 1 / 16f * 2;
        float yMargin = 1 / 16f;

        float maxH = height - 2 * (isAir ? margin : yMargin);
        float maxW = width - 2 * margin;
        //if width and height are greater than maximum allowed vales for container scale down
        if (aW > maxW || aH > maxH) {
            if (aW - maxW > aH - maxH)
                scale = maxW / aW;
            else
                scale = maxH / aH;
        }
        //ice&fire dragons
        String name = mob.getType().getRegistryName().toString();
        if (name.equals("iceandfire:fire_dragon") || name.equals("iceandfire:ice_dragon") || name.equals("iceandfire:lightning_dragon")) {
            scale *= 0.45;
        }

        float yOffset = isAir ? (height / 2f) - aH * scale / 2f : yMargin;

        if (mob instanceof Bat) {
            yOffset *= 1.5f;
        } else if (mob instanceof EnderDragon) {
            scale *= 2;
            yOffset *= 2;
        } else if (mob instanceof WitherBoss) {
            scale *= 1.5f;
            yOffset *= 0.9125;
        }

        return Pair.of(scale, yOffset);
    }


}