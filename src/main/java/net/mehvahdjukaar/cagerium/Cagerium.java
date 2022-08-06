package net.mehvahdjukaar.cagerium;

import net.mehvahdjukaar.cagerium.common.*;
import net.mehvahdjukaar.cagerium.delete.MagnetTableContainerMenu;
import net.mehvahdjukaar.cagerium.delete.MagnetTableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Mod(Cagerium.MOD_ID)
public class Cagerium {

    public static final String MOD_ID = "cagerium";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, res("bosses"));

    public static ResourceLocation res(String n) {
        return new ResourceLocation(MOD_ID, n);
    }

    public static String str(String n) {
        return MOD_ID + ":" + n;
    }

    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Cagerium.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Cagerium.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Cagerium.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Cagerium.MOD_ID);


    public static ForgeConfigSpec SERVER_SPEC;

    public static ForgeConfigSpec.IntValue TICKS_TO_DROP_LOOT_0;
    public static ForgeConfigSpec.IntValue TICKS_TO_DROP_LOOT_1;
    public static ForgeConfigSpec.IntValue TICKS_TO_DROP_LOOT_2;

    public Cagerium() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        RECIPES.register(bus);
        CONT.register(bus);

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        TICKS_TO_DROP_LOOT_0 = builder.defineInRange("terrarium_ticks_to_drop", 100, 1, 100000);
        TICKS_TO_DROP_LOOT_1 = builder.defineInRange("cage_ticks_to_drop", 100, 1, 100000);
        TICKS_TO_DROP_LOOT_2 = builder.defineInRange("plate_ticks_to_drop", 300, 1, 100000);
        SERVER_SPEC = builder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SERVER_SPEC);

        MinecraftForge.EVENT_BUS.addListener(Cagerium::addReloadListener);
    }

    public static void addReloadListener(AddReloadListenerEvent event) {
        // event.addListener(new CustomCageriumLootTables(event.getServerResources().getPredicateManager()));
    }

    public static final RegistryObject<RecipeSerializer<?>> UPGRADE_RECIPE = RECIPES.register(
            "spawn_egg_shaped", SpawnEggShapedRecipe.Serializer::new);

    public static final String CAGE_NAME = "cage";

    public static final RegistryObject<Block> CAGE = BLOCKS.register(CAGE_NAME, () -> new CageriumBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .isViewBlocking((s, p, l) -> false)
                    .noOcclusion()
                    .isRedstoneConductor((s, p, l) -> false)
                    .sound(SoundType.METAL), Tier.MOBS
    ));

    public static final RegistryObject<Item> CAGE_ITEM = ITEMS.register(CAGE_NAME, () -> new CageItem(CAGE.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> CAGE_BASE = ITEMS.register("ominous_skull", () -> new Item(
            new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.RARE)));

    public static final String TERRARIUM_NAME = "terrarium";

    public static final RegistryObject<Block> TERRARIUM = BLOCKS.register(TERRARIUM_NAME, () -> new CageriumBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(2f, 4f)
                    .isViewBlocking((s, p, l) -> false)
                    .noOcclusion()
                    .isRedstoneConductor((s, p, l) -> false)
                    .sound(SoundType.GLASS), Tier.PASSIVE
    ));

    public static final RegistryObject<Item> TERRARIUM_ITEM = ITEMS.register(TERRARIUM_NAME, () -> new CageItem(TERRARIUM.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> TERRARIUM_BASE = ITEMS.register("binding_wood_plate", () -> new Item(
            new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.RARE)));


    public static final String PLATE_NAME = "plate";

    public static final RegistryObject<Block> PLATE = BLOCKS.register(PLATE_NAME, () -> new CageriumBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .isViewBlocking((s, p, l) -> false)
                    .noOcclusion()
                    .isRedstoneConductor((s, p, l) -> false)
                    .sound(SoundType.STONE), Tier.BOSSES
    ));

    public static final RegistryObject<Item> PLATE_ITEM = ITEMS.register(PLATE_NAME, () -> new CageItem(PLATE.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> PLATE_GEM = ITEMS.register("binding_gemstone", () -> new Item(
            new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.EPIC)));


    public static final RegistryObject<BlockEntityType<CageriumBlockTile>> TILE = TILES.register("cagerium", () -> BlockEntityType.Builder.of(
            CageriumBlockTile::new, CAGE.get(), TERRARIUM.get(), PLATE.get()).build(null));


    public static final RegistryObject<Item> FIRE_UPGRADE = ITEMS.register("burning_upgrade", () -> new Item(
            new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS)));

    public static final RegistryObject<Item> CAGE_UPGRADE = ITEMS.register("capacity_upgrade", () -> new UpgradeItem(
            new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS)));

    public static final RegistryObject<Item> IRON_GOLEM_SPAWN_EGG = ITEMS.register("iron_golem" + "_spawn_egg", () ->
            new ForgeSpawnEggItem(() -> EntityType.IRON_GOLEM, 0xC4C3C4, 0xFCFCFC,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> WITHER_SPAWN_EGG = ITEMS.register("wither" + "_spawn_egg", () ->
            new ForgeSpawnEggItem(() -> EntityType.WITHER, 0x131313, 0xC3F6E8,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> ENDER_DRAGON_SPAWN_EGG = ITEMS.register("ender_dragon" + "_spawn_egg", () ->
            new ForgeSpawnEggItem(() -> EntityType.ENDER_DRAGON, 0x121212, 0xA82BC5,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));






    private static final DeferredRegister<MenuType<?>> CONT = DeferredRegister.create(ForgeRegistries.CONTAINERS, Cagerium.MOD_ID);


    public static final RegistryObject<MenuType<MagnetTableContainerMenu>> MAGNET_TABLE_CONTAINER = CONT.register("test",
            () -> IForgeMenuType.create(MagnetTableContainerMenu::new));

    public static final RegistryObject<Block> MAGNET_TABLE = BLOCKS.register("magnet_table", () -> new B(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .isViewBlocking((s, p, l) -> false)
                    .noOcclusion()
                    .isRedstoneConductor((s, p, l) -> false)
                    .sound(SoundType.STONE)
    ));

    private static class B extends Block implements EntityBlock{

        public B(Properties p_49795_) {
            super(p_49795_);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new MagnetTableTile(pPos, pState);
        }

        public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
            if (pLevel.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                if (pLevel.getBlockEntity(pPos) instanceof MagnetTableTile tile) {
                    pPlayer.openMenu(tile);
                }
                return InteractionResult.CONSUME;
            }
        }
    }

    public static final RegistryObject<BlockEntityType<MagnetTableTile>> MAGNET_TABLE_TILE = TILES.register("magnet_table", () -> BlockEntityType.Builder.of(
            MagnetTableTile::new, MAGNET_TABLE.get()).build(null));


}
