package dev.tancop.immersivemagic

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.tancop.immersivemagic.recipes.*
import dev.tancop.immersivemagic.spells.FireballSpellComponent
import dev.tancop.immersivemagic.spells.SpellComponent
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.component.CustomData
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
import net.neoforged.neoforge.registries.NewRegistryEvent
import net.neoforged.neoforge.registries.RegistryBuilder
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull


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
        SPELL_COMPONENTS.register(modEventBus)


        fun <T : PlayerInteractEvent> handleSpellCast(
            event: T,
            castFunction: SpellComponent.(T) -> InteractionResult,
            onSuccess: (T) -> Unit
        ) {
            val heldItem = event.entity.getItemInHand(event.hand)

            val entry = heldItem.get(DataComponents.CUSTOM_DATA)
            if (entry != null) {
                val compoundTag = entry.copyTag() ?: CompoundTag()

                val key = compoundTag.allKeys
                    .firstOrNull { SPELL_COMPONENTS_REGISTRY.containsKey(ResourceLocation.parse(it)) }
                if (key == null) return

                val resourceLocation = ResourceLocation.parse(key)

                val codec = BuiltInRegistries.DATA_COMPONENT_TYPE.get(resourceLocation)?.codec()
                if (codec == null) return

                val tag = compoundTag.get(key)

                val component = codec.decode(NbtOps.INSTANCE, tag).result().getOrNull()?.first
                    ?: if (tag is StringTag) {
                        // Might be JSON like when reading from a recipe
                        codec.decode(
                            JsonOps.INSTANCE,
                            JsonParser.parseString(tag.asString)
                        ).result().getOrNull()?.first
                    } else {
                        null
                    }

                if (component is SpellComponent) {
                    if (component.charges > 0) {
                        val result = component.castFunction(event)
                        if (result == InteractionResult.SUCCESS) {
                            val newComponent = component.withLowerCharge()

                            val tag = newComponent.encodeNbt()
                            // preserve other "fake" components if any
                            if (tag != null) {
                                compoundTag.put(key, tag)
                            } else {
                                compoundTag.remove(key)
                            }

                            heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag))
                            heldItem.set(DataComponents.LORE, newComponent.getItemLore())

                            onSuccess(event)
                            return
                        }
                    }
                }
            }
        }

        fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) {
            if (event.level.isClientSide) return

            val state = event.level.getBlockState(event.pos)
            if (state.`is`(Blocks.WATER_CAULDRON) && event.entity.isCrouching) {
                // Sneaking normally disables interactions but we want to use it for dipping
                event.useBlock = TriState.TRUE
            }

            handleSpellCast(event, SpellComponent::castOnBlock) {
                it.useBlock = TriState.FALSE
                it.useItem = TriState.FALSE
            }
        }

        fun onRightClickEntity(event: PlayerInteractEvent.EntityInteractSpecific) {
            if (event.level.isClientSide) return

            handleSpellCast(event, SpellComponent::castOnEntity) {}
        }

        fun onRightClickItem(event: PlayerInteractEvent.RightClickItem) {
            if (event.level.isClientSide) return

            handleSpellCast(event, SpellComponent::cast) {}
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

        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.EntityInteractSpecific> { onRightClickEntity(it) }
        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickItem> { onRightClickItem(it) }

        modEventBus.addListener<GatherDataEvent> { gatherData(it) }
        modEventBus.addListener<NewRegistryEvent> { event ->
            event.register(SPELL_COMPONENTS_REGISTRY)
        }

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

        val SPELL_COMPONENTS_REGISTRY_KEY: ResourceKey<Registry<Unit>> =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "spell_components"))

        val SPELL_COMPONENTS_REGISTRY: Registry<Unit> = RegistryBuilder(SPELL_COMPONENTS_REGISTRY_KEY)
            .create()

        val SPELL_COMPONENTS: DeferredRegister<Unit> =
            DeferredRegister.create(SPELL_COMPONENTS_REGISTRY_KEY, MOD_ID)

        val DATA_COMPONENT_TYPES: DeferredRegister.DataComponents =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID)

        @Suppress("unused") // registered and might be used later
        val FIREBALL_SPELL: DeferredHolder<DataComponentType<*>, DataComponentType<FireballSpellComponent>> =
            DATA_COMPONENT_TYPES.registerComponentType("fireball_spell") { builder ->
                builder
                    .persistent(FireballSpellComponent.CODEC.codec())
                    .networkSynchronized(EmptyStreamCodec(FireballSpellComponent(0, 3)))
            }

        init {
            SPELL_COMPONENTS.register("fireball_spell", Supplier { })
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
