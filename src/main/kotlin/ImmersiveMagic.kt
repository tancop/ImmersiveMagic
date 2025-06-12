package dev.tancop.immersivemagic

import com.mojang.logging.LogUtils
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.util.TriState
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

        fun onRightClick(event: PlayerInteractEvent.RightClickBlock) {
            if (event.level.isClientSide) return

            val state = event.level.getBlockState(event.pos)
            if (state.`is`(Blocks.WATER_CAULDRON) && event.entity.isCrouching) {
                event.useBlock = TriState.TRUE
            }
        }

        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickBlock> { onRightClick(it) }

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC)
    }

    companion object {
        // Define mod id in a common place for everything to reference
        const val MOD_ID: String = "immersivemagic"

        // Directly reference a slf4j logger
        private val LOGGER: Logger = LogUtils.getLogger()

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
    }
}
