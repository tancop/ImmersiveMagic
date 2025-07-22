package dev.tancop.immersivemagic

import com.mojang.datafixers.util.Either
import dev.tancop.immersivemagic.recipes.*
import dev.tancop.immersivemagic.spells.EmptyScrollComponent
import dev.tancop.immersivemagic.spells.EvokerFangsSpellComponent
import dev.tancop.immersivemagic.spells.FireballSpellComponent
import dev.tancop.immersivemagic.spells.SpellComponent
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
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
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NewRegistryEvent
import net.neoforged.neoforge.registries.RegistryBuilder
import java.util.function.Supplier


@Mod(ImmersiveMagic.MOD_ID)
class ImmersiveMagic(modEventBus: IEventBus, modContainer: ModContainer) {
    init {
        BLOCK_ENTITY_TYPES.register(modEventBus)
        RECIPE_TYPES.register(modEventBus)
        RECIPE_SERIALIZERS.register(modEventBus)
        DATA_COMPONENT_TYPES.register(modEventBus)
        SPELL_COMPONENTS.register(modEventBus)


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

            val source = event.source
            val killer = source.entity
            if (killer != null && killer.type == (EntityType.PLAYER)) {
                SoulBindingMechanics.handleEntityDeath(level, killer as Player, entity)
            }
        }

        fun onAttackEntity(event: AttackEntityEvent) {
            val player = event.entity
            val target = event.target

            val heldItem = player.getItemInHand(InteractionHand.MAIN_HAND)

            if (!heldItem.isEmpty) {
                val map = ServerComponentMap.fromStack(heldItem)

                val component = map.get(DIPPED_WEAPON.get())

                if (component != null) {
                    if (component.charges == 1) {
                        map.remove(DIPPED_WEAPON.get())
                    } else {
                        val newComponent = component.withLowerCharge()
                        map.set(DIPPED_WEAPON.get(), newComponent)
                    }

                    if (target is LivingEntity) {
                        component.applyEffects(target)
                    }

                    heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(map.encode()))

                    heldItem.applyServerComponentLore()
                }
            }
        }

        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickBlock> { onRightClickBlock(it) }
        NeoForge.EVENT_BUS.addListener<LivingDeathEvent> { onLivingDeath(it) }

        NeoForge.EVENT_BUS.addListener<AttackEntityEvent> { onAttackEntity(it) }

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

        val TOOL_DIPPING: DeferredHolder<RecipeType<*>, RecipeType<ToolDippingRecipe>> = RECIPE_TYPES.register(
            "tool_dipping",
            Supplier {
                RecipeType.simple<ToolDippingRecipe>(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "tool_dipping")
                )
            })

        val SOUL_BINDING: DeferredHolder<RecipeType<*>, RecipeType<SoulBindingRecipe>> = RECIPE_TYPES.register(
            "soul_binding",
            Supplier {
                RecipeType.simple<SoulBindingRecipe>(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "soul_binding")
                )
            })

        val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>> =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID)

        val BREWING_SERIALIZER: DeferredHolder<RecipeSerializer<*>, BrewingRecipeSerializer> =
            RECIPE_SERIALIZERS.register("brewing", Supplier { BrewingRecipeSerializer() })

        val DIPPING_SERIALIZER: DeferredHolder<RecipeSerializer<*>, DippingRecipeSerializer> =
            RECIPE_SERIALIZERS.register("dipping", Supplier { DippingRecipeSerializer() })

        val TOOL_DIPPING_SERIALIZER: DeferredHolder<RecipeSerializer<*>, ToolDippingRecipeSerializer> =
            RECIPE_SERIALIZERS.register("tool_dipping", Supplier { ToolDippingRecipeSerializer() })

        val SOUL_BINDING_SERIALIZER: DeferredHolder<RecipeSerializer<*>, SoulBindingRecipeSerializer> =
            RECIPE_SERIALIZERS.register("soul_binding", Supplier { SoulBindingRecipeSerializer() })

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

        @Suppress("unused")
        val EVOKER_FANGS_SPELL: DeferredHolder<DataComponentType<*>, DataComponentType<EvokerFangsSpellComponent>> =
            DATA_COMPONENT_TYPES.registerComponentType("evoker_fangs_spell") { builder ->
                builder
                    .persistent(EvokerFangsSpellComponent.CODEC.codec())
                    .networkSynchronized(EmptyStreamCodec(EvokerFangsSpellComponent(0, 3)))
            }

        @Suppress("unused")
        val EMPTY_SCROLL: DeferredHolder<DataComponentType<*>, DataComponentType<EmptyScrollComponent>> =
            DATA_COMPONENT_TYPES.registerComponentType("empty_scroll") { builder ->
                builder
                    .persistent(EmptyScrollComponent.CODEC)
                    .networkSynchronized(EmptyStreamCodec(EmptyScrollComponent()))
            }

        @Suppress("unused")
        val DIPPED_WEAPON: DeferredHolder<DataComponentType<*>, DataComponentType<DippedWeaponComponent>> =
            DATA_COMPONENT_TYPES.registerComponentType("dipped_weapon") { builder ->
                builder
                    .persistent(DippedWeaponComponent.CODEC.codec())
                    .networkSynchronized(EmptyStreamCodec(DippedWeaponComponent(0, Either.left(null))))
            }

        init {
            SPELL_COMPONENTS.register("fireball_spell", Supplier { })
            SPELL_COMPONENTS.register("evoker_fangs_spell", Supplier { })
        }

        fun gatherData(event: GatherDataEvent) {
            val generator = event.generator
            val output = generator.packOutput
            val lookupProvider = event.lookupProvider
            val existingFileHelper = event.existingFileHelper

            generator.addProvider(
                event.includeServer(),
                CustomRecipeProvider(output, lookupProvider)
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
