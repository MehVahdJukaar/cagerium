package net.mehvahdjukaar.cagerium.common;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.cagerium.Cagerium;
import net.mehvahdjukaar.cagerium.mixins.SlimeInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.WeakHashMap;

public class CageriumBlockTile extends BlockEntity {

    private final Tier tier;

    @Nullable
    private EntityType<?> entityType;
    private int tickCount;
    private byte upgradeLevel;
    private boolean burning;
    @Nullable
    private ItemStack groundItem;

    @Nullable
    private BlockState groundState;

    //client only so we can have random mob textures & shit. weak so we remove stuff when upgrades are removed
    private final WeakHashMap<Integer, MobData> renderData = new WeakHashMap<>();

    public MobData getRenderData(int index) {
        return renderData.computeIfAbsent(index, e -> MobData.create(this.entityType, this.level));
    }

    public CageriumBlockTile(BlockPos pos, BlockState state) {
        super(Cagerium.TILE.get(), pos, state);
        this.tier = (state.getBlock() instanceof CageriumBlock cb) ? cb.getTier() : Tier.PASSIVE;
    }

    public Tier getTier() {
        return tier;
    }

    public void saveToNbt(ItemStack stack) {
        if (this.entityType != null) {
            CompoundTag compound = new CompoundTag();
            saveCage(compound, false);
            stack.addTagElement("BlockEntityTag", compound);
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.tickCount = compound.getInt("TickCount");
        this.burning = compound.getBoolean("Burning");
        this.upgradeLevel = compound.getByte("UpgradeLevel");
        if (compound.contains("EntityType")) {
            var res = new ResourceLocation(compound.getString("EntityType"));
            if (ForgeRegistries.ENTITIES.containsKey(res)) {
                this.entityType = ForgeRegistries.ENTITIES.getValue(res);
            } else {
                Cagerium.LOGGER.warn("Found unknown entity type {} when loading cagerium block entity", res);
            }
        }
        if (compound.contains("GroundItem")) {
            this.groundItem = ItemStack.of(compound.getCompound("GroundItem"));
            if (this.groundItem.getItem() instanceof BlockItem bi) {
                this.groundState = bi.getBlock().defaultBlockState();
            }
        }
        if (this.entityType == null) renderData.clear();
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        saveCage(compound, true);

    }

    private void saveCage(CompoundTag compound, boolean saveItem) {
        compound.putInt("TickCount", this.tickCount);
        compound.putBoolean("Burning", this.burning);
        compound.putByte("UpgradeLevel", this.upgradeLevel);
        if (this.entityType != null) {
            compound.putString("EntityType", entityType.getRegistryName().toString());
        }
        //only for block packet
        if (saveItem && this.groundItem != null) {
            compound.put("GroundItem", this.groundItem.save(new CompoundTag()));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(CageriumBlock.FACING);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CageriumBlockTile tile) {
        if (!pLevel.isClientSide) {
            if (tile.tickCount++ >= tile.getTickToDrop()) {
                tile.tickCount = 0;
                tile.tryDropLoot();
            }
        }
    }

    private int getTickToDrop() {
        return switch (this.tier) {
            case PASSIVE -> Cagerium.TICKS_TO_DROP_LOOT_0.get();
            case MOBS -> Cagerium.TICKS_TO_DROP_LOOT_1.get();
            case BOSSES -> Cagerium.TICKS_TO_DROP_LOOT_2.get();
        };
    }

    public boolean isEmpty() {
        return this.entityType == null;
    }

    public InteractionResult onInteract(Level world, BlockPos pos, Player player, InteractionHand hand, Direction direction) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (item instanceof SpawnEggItem spawnEggItem) {
            EntityType<?> type = spawnEggItem.getType(stack.getTag());
            if (this.entityType == null) {
                if (this.tier.acceptsEntityType(type)) {
                    if (!world.isClientSide) {
                        this.entityType = type;
                        world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 1, 1.2f);
                        this.setChanged();
                        world.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 3);
                        if (!player.getAbilities().instabuild) stack.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                } else {
                    player.displayClientMessage(new TextComponent("Does not fit in here"), true);
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            } else if (this.upgradeLevel < 3) {
                if (type == entityType) {
                    this.upgradeLevel++;
                    this.setChanged();
                    world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 1, 1.2f);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    world.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 3);
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
            return InteractionResult.CONSUME_PARTIAL;
        } else if (item == Cagerium.CAGE_KEY.get()) {

            if (this.entityType != null) {
                var i = ForgeSpawnEggItem.fromEntityType(this.entityType);
                if (i != null) {
                    Block.popResourceFromFace(world, pos, direction, i.getDefaultInstance());
                    if (this.upgradeLevel != 0) {
                        this.upgradeLevel--;
                    } else this.entityType = null;
                    this.setChanged();
                    world.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 3);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                        player.broadcastBreakEvent(hand);
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            } else if (this.burning) {
                Block.popResourceFromFace(world, pos, direction, Cagerium.FIRE_UPGRADE.get().getDefaultInstance());
                this.burning = false;
                this.setChanged();
                world.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 3);
                if (!player.isCreative()) {
                    stack.shrink(1);
                    player.broadcastBreakEvent(hand);
                }

                return InteractionResult.sidedSuccess(world.isClientSide);
            }

            return InteractionResult.FAIL;
        } else if (item == Cagerium.FIRE_UPGRADE.get() && !this.burning) {
            this.burning = true;
            this.setChanged();
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1, 1.2f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            world.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 3);
            return InteractionResult.sidedSuccess(world.isClientSide);
        } else if (this.groundItem == null && item instanceof BlockItem bi && isValidBlockItem(pos, bi)) {

            var s = stack.copy();
            s.setCount(1);
            this.groundItem = s;
            if (!world.isClientSide) {
                this.setChanged();
                world.playSound(null, pos, bi.getBlock().defaultBlockState().getSoundType().getPlaceSound(),
                        SoundSource.PLAYERS, 0.6f, 1.2f);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                world.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 3);
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private boolean isValidBlockItem(BlockPos pos, BlockItem bi) {
        BlockState state = bi.getBlock().defaultBlockState();
        return state.is(BlockTags.LEAVES) || (state.isRedstoneConductor(level, pos) && !state.hasBlockEntity());
    }


    @SuppressWarnings("ConstantConditions")
    public void tryDropLoot() {
        if (this.entityType == null) return;
        MobData data = getMobData();

        if (data != null && data.getEntity() instanceof LivingEntity livingEntity) {

            BlockEntity tile = level.getBlockEntity(this.worldPosition.below());
            if (tile != null) {
                IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).orElse(null);
                if (itemHandler != null) {
                    var loot = this.createDropsList(livingEntity);

                    loot.forEach(s -> ItemHandlerHelper.insertItem(itemHandler, s, false));
                }
            }
        }
    }

    @Nullable
    public MobData getMobData() {
        if (this.entityType == null) return null;
        return MobData.getOrCreate(this.entityType, this.level, this.worldPosition);
    }

    // Gives back a list of items that harvest will yield
    private NonNullList<ItemStack> createDropsList(LivingEntity entity) {
        NonNullList<ItemStack> drops = NonNullList.create();


        FakePlayer player = FakePlayerFactory.get((ServerLevel) this.level, DUMMY_PROFILE);
        if (this.burning) {
            ItemStack stack = new ItemStack(Items.IRON_SWORD);
            EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(Enchantments.FIRE_ASPECT, 1));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            entity.setSecondsOnFire(1);
        } else {
            entity.clearFire();
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        //slimes only drop when small...
        if (entity instanceof SlimeInvoker s && !(entity instanceof MagmaCube)) {
            s.invokeSetSize(0, false);
        }


        LootTable loottable;
        var tables = this.level.getServer().getLootTables();
        var type = entity.getType().getRegistryName();
        loottable = tables.get(new ResourceLocation(type.getNamespace(), Cagerium.MOD_ID + "/" + type.getPath()));

        if (loottable == LootTable.EMPTY) {
            loottable = tables.get(entity.getLootTable());
        }

        LootContext.Builder builder = (new LootContext.Builder((ServerLevel) this.level))
                .withRandom(this.level.getRandom())
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                .withParameter(LootContextParams.DAMAGE_SOURCE, this.burning ? DamageSource.ON_FIRE : DamageSource.GENERIC)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck());

        LootContext ctx = builder.create(LootContextParamSets.ENTITY);

        for (int i = 0; i < upgradeLevel + 1; i++) {
            if (entity instanceof WitherBoss) {
                drops.add(Items.NETHER_STAR.getDefaultInstance());
            } else if (entity.getType() == EntityType.MAGMA_CUBE) { //some issue specific to vh... idk
                drops.add(Items.MAGMA_CREAM.getDefaultInstance());
            } else drops.addAll(loottable.getRandomItems(ctx));

        }

        entity.setSecondsOnFire(0);

        if (entity instanceof SlimeInvoker s) {
            s.invokeSetSize(3, false);
        }


        return drops;
    }

    private static final GameProfile DUMMY_PROFILE = new GameProfile(
            UUID.fromString("5b580441-d54a-38f1-1120-e3842a80632b"), "[cagerium]");

    public byte getUpgradeLevel() {
        return upgradeLevel;
    }

    public boolean isBurning() {
        return this.burning;
    }

    @Nullable
    public ItemStack getGroundStack() {
        return this.groundItem;
    }

    public BlockState getGroundState() {
        return groundState;
    }


}
