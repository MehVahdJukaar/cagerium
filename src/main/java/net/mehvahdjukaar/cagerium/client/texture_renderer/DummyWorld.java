package net.mehvahdjukaar.cagerium.client.texture_renderer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

public class DummyWorld extends Level {

    public static final DummyWorld INSTANCE = new DummyWorld();

    private final Scoreboard scoreboard = new Scoreboard();
    private final ChunkSource chunkManager = new DummyChunkManager(this);

    private DummyWorld() {
        super(new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false),
                ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("dummy")),
                RegistryAccess.BUILTIN.get().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getOrCreateHolder(DimensionType.OVERWORLD_LOCATION),
                () -> InactiveProfiler.INSTANCE, true, false, 0);

    }

    @Override
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.chunkManager;
    }

    @Override
    public void playSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void playSound(Player player, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String gatherChunkSourceStats() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Entity getEntity(int id) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public MapItemSavedData getMapData(String id) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void setMapData(String pMapId, MapItemSavedData pData) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public int getFreeMapId() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void destroyBlockProgress(int entityId, BlockPos pos, int progress) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RecipeManager getRecipeManager() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void levelEvent(Player player, int eventId, BlockPos pos, int data) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos) {

    }

    @Override
    public float getShade(Direction direction, boolean shaded) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public List<? extends Player> players() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RegistryAccess registryAccess() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
        throw new IllegalStateException("not implemented");
    }

    private static class DummyChunkManager extends ChunkSource {

        private final Level world;

        public DummyChunkManager(Level world) {
            this.world = world;
        }

        @Override
        public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return new EmptyLevelChunk(this.world, new ChunkPos(x, z), BuiltinRegistries.BIOME.getHolderOrThrow(Biomes.FOREST));
        }

        @Override
        public void tick(BooleanSupplier supplier, boolean b) {
        }

        @Override
        public String gatherStats() {
            return "";
        }

        @Override
        public int getLoadedChunksCount() {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            throw new IllegalStateException("not implemented"); // TODO
        }

        @Override
        public BlockGetter getLevel() {
            return this.world;
        }
    }
}