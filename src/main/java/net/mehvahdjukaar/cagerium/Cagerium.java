package net.mehvahdjukaar.cagerium;

import net.mehvahdjukaar.cagerium.common.*;
import net.mehvahdjukaar.cagerium.common.Tier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeSpawnEggItem;
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

    public static ForgeConfigSpec.IntValue TICKS_TO_DROP_LOOT;

    public Cagerium() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        RECIPES.register(bus);

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        TICKS_TO_DROP_LOOT = builder.defineInRange("ticks_to_drop", 100, 1, 100000);
        SERVER_SPEC = builder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SERVER_SPEC);
    }

    public static final RegistryObject<RecipeSerializer<?>> UPGRADE_RECIPE = RECIPES.register(
            "spawn_egg_shaped", SpawnEggShapedRecipe.Serializer::new);

    public static final String CAGE_NAME = "cage";

    public static final RegistryObject<Block> CAGE = BLOCKS.register(CAGE_NAME, () -> new CageriumBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .isViewBlocking((s, p, l) -> false)
                    .noOcclusion()
                    .isRedstoneConductor((s,p,l)->false)
                    .sound(SoundType.METAL), Tier.MOBS
    ));

    public static final RegistryObject<Item> CAGE_ITEM = ITEMS.register(CAGE_NAME, () -> new CageItem(CAGE.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.EPIC)));


    public static final String TERRARIUM_NAME = "terrarium";

    public static final RegistryObject<Block> TERRARIUM = BLOCKS.register(TERRARIUM_NAME, () -> new CageriumBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(2f, 4f)
                    .isViewBlocking((s, p, l) -> false)
                    .noOcclusion()
                    .isRedstoneConductor((s,p,l)->false)
                    .sound(SoundType.GLASS), Tier.PASSIVE
    ));

    public static final RegistryObject<Item> TERRARIUM_ITEM = ITEMS.register(TERRARIUM_NAME, () -> new CageItem(TERRARIUM.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.RARE)));


    public static final RegistryObject<BlockEntityType<CageriumBlockTile>> TILE = TILES.register("cagerium", () -> BlockEntityType.Builder.of(
            CageriumBlockTile::new, CAGE.get(), TERRARIUM.get()).build(null));

    public static final RegistryObject<Item> CAGE_BASE = ITEMS.register("cage_base", () -> new Item(
            new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> TERRARIUM_BASE = ITEMS.register("terrarium_base", () -> new Item(
            new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.RARE)));

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


}
