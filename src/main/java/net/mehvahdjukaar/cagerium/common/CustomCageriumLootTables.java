package net.mehvahdjukaar.cagerium.common;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.mehvahdjukaar.cagerium.Cagerium;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nullable;
import java.util.Map;
/*
public class CustomCageriumLootTables extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private static Map<ResourceLocation, LootTable> TABLES = ImmutableMap.of();

    @Nullable
    public static LootTable getCustomLoot(EntityType<?> type) {
        return TABLES.get(Registry.ENTITY_TYPE.getKey(type));
    }

    private final Object predicateManager;

    public CustomCageriumLootTables(Object predicateManager) {
        super(GSON, Cagerium.MOD_ID);
        this.predicateManager = predicateManager;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profile) {
        //thanks forge... cant deserialize loot tables outside or LootTables...
        ImmutableMap.Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();

        jsons.forEach((id, j) -> {
            try {
                LootTable lootTable = GSON.fromJson(j, LootTable.class);
                builder.put(id, lootTable);
            } catch (Exception exception) {
                Cagerium.LOGGER.error("Couldn't parse loot table {}", id, exception);
            }

        });

        ImmutableMap<ResourceLocation, LootTable> immutablemap = builder.build();
        ValidationContext validationcontext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, immutablemap::get);
        immutablemap.forEach((location, lootTable) -> LootTables.validate(validationcontext, location, lootTable));
        validationcontext.getProblems().forEach((s, s1) -> {
            Cagerium.LOGGER.warn("Found validation problem in {}: {}", s, s1);
        });
        TABLES = immutablemap;
    }


}
*/