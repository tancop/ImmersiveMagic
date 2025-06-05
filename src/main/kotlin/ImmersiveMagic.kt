package dev.tancop.immersivemagic

import com.google.gson.Gson
import com.mojang.logging.LogUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import org.slf4j.Logger
import java.util.function.Supplier

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ImmersiveMagic.Companion.MOD_ID)
class ImmersiveMagic
    (modEventBus: IEventBus, modContainer: ModContainer) {
    // The constructor for the mod class is the first code run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    init {
        BLOCK_ENTITY_TYPES.register(modEventBus)

        // Listen for right clicks on cauldrons
        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickBlock> { event ->
            if (event.level.isClientSide) return@addListener

            // Block is a cauldron with water
            (event.level.getBlockState(event.pos).block as? LayeredCauldronBlock)?.let { block ->
                if (event.itemStack.item !in (Recipes.acceptedItems + Items.GLASS_BOTTLE + Items.POTION)) return@addListener

                val entity = (event.level.getBlockEntity(event.pos) as? WaterCauldronBlockEntity)
                    ?: WaterCauldronBlockEntity(event.pos, event.level.getBlockState(event.pos)).also {
                        event.level.setBlockEntity(it)
                    }

                val lowerPos = event.pos.offset(0, -1, 0)
                val lowerState = event.level.getBlockState(lowerPos)

                val lowerBlock = lowerState.block
                var fireType = when (lowerBlock) {
                    is CampfireBlock -> FireType.NORMAL
                    is FireBlock -> FireType.NORMAL
                    is SoulFireBlock -> FireType.SOUL
                    else -> return@addListener
                }

                // Unlit campfires don't work
                if (lowerBlock is CampfireBlock && !lowerState.getValue(CampfireBlock.LIT)) return@addListener
                // Set type to soul if it's a soul fire
                if (lowerState.`is`(Blocks.SOUL_CAMPFIRE)) fireType = FireType.SOUL

                LOGGER.info("Fire Type: $fireType")

                val stack = event.itemStack
                val item = stack.item

                val state = event.level.getBlockState(event.pos)

                when (item) {
                    Items.POTION -> {
                        val comp = stack.get(DataComponents.POTION_CONTENTS)
                        LOGGER.info("Used potion ${comp?.potion} on cauldron")
                        if (state.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL
                            && comp?.`is`(Potions.WATER) ?: false
                        ) {
                            LOGGER.info("Diluted potion")
                            entity.items.clear()
                        }
                    }

                    Items.GLASS_BOTTLE -> {
                        if (entity.items.isNotEmpty()) {
                            val itemSet = entity.items.map { it.item }.toSet()

                            val foundRecipe = Recipes.recipes[itemSet]?.let { (fireNeeded, potion) ->
                                if (fireType >= fireNeeded) {
                                    LOGGER.info("Taking out potion")
                                    event.entity.inventory.add(potion.getStack())
                                }
                                true
                            } ?: false

                            if (!foundRecipe) {
                                LOGGER.info("Taking out bad potion")
                                val contents = PotionContents(Potions.MUNDANE)
                                val potionStack = ItemStack(Items.POTION, 1)
                                potionStack.set(DataComponents.POTION_CONTENTS, contents)

                                event.entity.inventory.add(potionStack)
                            }

                            event.isCanceled = true
                        }
                    }

                    else -> {
                        if (entity.items.none { it.item == stack.item }) {
                            println("Adding new item: ${stack.item}")
                            val newStack = stack.copy()
                            newStack.count = 1
                            entity.items.add(newStack)
                            stack.shrink(1)
                        }

                        // Emit colored particles if the current ingredients are a valid potion,
                        // white particles if not
                        (event.level as? ServerLevel)?.let { level ->
                            val itemSet = entity.items.map { it.item }.toSet()
                            val color = Recipes.recipes[itemSet]?.let { (fireNeeded, potion) ->
                                if (fireType >= fireNeeded)
                                    return@let potion.getEffectColor()
                                FastColor.ARGB32.color(255, 255, 255) // White
                            } ?: FastColor.ARGB32.color(255, 255, 255)

                            val particle = ColorParticleOption.create(
                                ParticleTypes.ENTITY_EFFECT,
                                color
                            )

                            level.sendParticles(
                                particle,
                                event.pos.x.toDouble() + 0.5,
                                event.pos.y.toDouble() + 1.0,
                                event.pos.z.toDouble() + 0.5,
                                20,
                                0.0, 0.2, 0.0,
                                0.5
                            )
                        }
                    }
                }

                LOGGER.info("Cauldron Data: ${entity.items.map { it.displayName.string }}")
            }
        }

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC)
    }

    companion object {
        // Define mod id in a common place for everything to reference
        const val MOD_ID: String = "immersivemagic"

        // Directly reference a slf4j logger
        private val LOGGER: Logger = LogUtils.getLogger()

        val GSON = Gson()

        val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID)

        val WATER_CAULDRON_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<WaterCauldronBlockEntity>> =
            BLOCK_ENTITY_TYPES.register(
                "water_cauldron",
                Supplier {
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    // Building with null works just fine

                    BlockEntityType.Builder.of(
                        { pos, state -> WaterCauldronBlockEntity(pos, state) },
                        Blocks.WATER_CAULDRON
                    ).build(null)
                })
    }
}
