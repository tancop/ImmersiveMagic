package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.recipes.BrewingRecipe
import dev.tancop.immersivemagic.recipes.BrewingRecipeProvider
import dev.tancop.immersivemagic.recipes.BrewingRecipeSerializer
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.util.TriState
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ImmersiveMagic.Companion.MOD_ID)
class ImmersiveMagic(modEventBus: IEventBus, modContainer: ModContainer) {
    // The constructor for the mod class is the first code run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    init {
        BLOCK_ENTITY_TYPES.register(modEventBus)
        RECIPE_TYPES.register(modEventBus)
        RECIPE_SERIALIZERS.register(modEventBus)

        fun onRightClick(event: PlayerInteractEvent.RightClickBlock) {
            if (event.level.isClientSide) return

            val state = event.level.getBlockState(event.pos)
            if (state.`is`(Blocks.WATER_CAULDRON) && event.entity.isCrouching) {
                event.useBlock = TriState.TRUE
            }
        }

        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickBlock> { onRightClick(it) }
        modEventBus.addListener<GatherDataEvent> { gatherData(it) }

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC)
    }

    companion object {
        // Define mod id in a common place for everything to reference
        const val MOD_ID: String = "immersivemagic"

        val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID)

        val WATER_CAULDRON_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<LayeredCauldronBlockEntity>> =
            BLOCK_ENTITY_TYPES.register(
                "water_cauldron",
                Supplier {
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    // Building with null works just fine

                    BlockEntityType.Builder.of(
                        { pos, state -> LayeredCauldronBlockEntity(pos, state) },
                        Blocks.WATER_CAULDRON
                    ).build(null)
                })

        val RECIPE_TYPES: DeferredRegister<RecipeType<*>> = DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID)

        val BREWING: DeferredHolder<RecipeType<*>, RecipeType<BrewingRecipe>> = RECIPE_TYPES.register(
            "brewing",
            Supplier {
                RecipeType.simple<BrewingRecipe>(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "brewing")
                )
            })

        val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>> =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID)

        val BREWING_SERIALIZER: DeferredHolder<RecipeSerializer<*>, BrewingRecipeSerializer> =
            RECIPE_SERIALIZERS.register("brewing", Supplier { BrewingRecipeSerializer() })

        fun gatherData(event: GatherDataEvent) {
            val generator = event.generator
            val output = generator.packOutput
            val lookupProvider = event.lookupProvider
            val existingFileHelper = event.existingFileHelper

            generator.addProvider(
                event.includeServer(),
                BrewingRecipeProvider(output, lookupProvider)
            )

            generator.addProvider(
                event.includeServer(),
                CompatTagsProvider(output, lookupProvider, existingFileHelper)
            )
        }

        val PISTON_BEHAVIOR_NORMAL: TagKey<Block?> = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath(
                "pistoncommand",
                "piston_behavior_normal"
            )
        )
    }
}
