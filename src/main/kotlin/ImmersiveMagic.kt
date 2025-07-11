package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.recipes.*
import dev.tancop.immersivemagic.spells.FireballSpellComponent
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
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
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
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
        DATA_COMPONENT_TYPES.register(modEventBus)

        fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) {
            if (event.level.isClientSide) return

            val state = event.level.getBlockState(event.pos)
            if (state.`is`(Blocks.WATER_CAULDRON) && event.entity.isCrouching) {
                // Sneaking normally disables interactions but we want to use it for dipping
                event.useBlock = TriState.TRUE
            }

            val heldItem = event.entity.getItemInHand(event.hand)

            if (heldItem.has(FIREBALL_SPELL)) {
                val result = heldItem.get(FIREBALL_SPELL)!!.castOnBlock(event)
                if (result == InteractionResult.SUCCESS) {
                    event.cancellationResult = InteractionResult.SUCCESS
                    return
                }
            }
        }

        fun onLivingDeath(event: LivingDeathEvent) {
            val entity = event.entity
            val level = entity.level()
            if (level.isClientSide) return

            val pos = entity.blockPosition().below()

            val source = event.source
            val killer = source.entity
            if (killer != null && killer.type == (EntityType.PLAYER)) {
                SacrificeMechanics.handleEntityDeath(level, pos, entity, killer as Player)
            }
        }

        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickBlock> { onRightClickBlock(it) }
        NeoForge.EVENT_BUS.addListener<LivingDeathEvent> { onLivingDeath(it) }
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

        val DIPPING: DeferredHolder<RecipeType<*>, RecipeType<DippingRecipe>> = RECIPE_TYPES.register(
            "dipping",
            Supplier {
                RecipeType.simple<DippingRecipe>(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "dipping")
                )
            })

        val SACRIFICE: DeferredHolder<RecipeType<*>, RecipeType<SacrificeRecipe>> = RECIPE_TYPES.register(
            "sacrifice",
            Supplier {
                RecipeType.simple<SacrificeRecipe>(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "sacrifice")
                )
            })

        val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>> =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID)

        val BREWING_SERIALIZER: DeferredHolder<RecipeSerializer<*>, BrewingRecipeSerializer> =
            RECIPE_SERIALIZERS.register("brewing", Supplier { BrewingRecipeSerializer() })

        val DIPPING_SERIALIZER: DeferredHolder<RecipeSerializer<*>, DippingRecipeSerializer> =
            RECIPE_SERIALIZERS.register("dipping", Supplier { DippingRecipeSerializer() })

        val SACRIFICE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, SacrificeRecipeSerializer> =
            RECIPE_SERIALIZERS.register("sacrifice", Supplier { SacrificeRecipeSerializer() })

        val DATA_COMPONENT_TYPES: DeferredRegister.DataComponents =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID)

        val FIREBALL_SPELL: DeferredHolder<DataComponentType<*>, DataComponentType<FireballSpellComponent>> =
            DATA_COMPONENT_TYPES.registerComponentType("fireball_spell") { builder ->
                builder
                    .persistent(FireballSpellComponent.CODEC.codec())
                    .networkSynchronized(StreamCodec.unit(FireballSpellComponent(3)))
            }

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
