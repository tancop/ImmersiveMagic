package dev.tancop.immersivemagic

import com.mojang.logging.LogUtils
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.AbstractCauldronBlock
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import org.slf4j.Logger
import java.util.function.Supplier
import com.mojang.datafixers.util.Pair as MojangPair

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ImmersiveMagic.Companion.MOD_ID)
class ImmersiveMagic
    (modEventBus: IEventBus, modContainer: ModContainer) {
    // The constructor for the mod class is the first code run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    init {
        // Register the commonSetup method for modloading
        modEventBus.addListener<FMLCommonSetupEvent> { event -> this.commonSetup(event) }
        ATTACHMENT_TYPES.register(modEventBus)

        // Listen for right clicks on cauldrons
        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickBlock> { event ->
            if (event.level.isClientSide) return@addListener

            (event.level.getBlockState(event.pos).block as? AbstractCauldronBlock)?.let { block ->
                LOGGER.info(
                    "Right Clicked Cauldron: ${block.name.string} with ${
                        event.itemStack
                            .displayName.string
                    }"
                )

                val chunk = event.level.getChunk(event.pos)
                
                val data = chunk.getData(CAULDRON_DATA)
                var blockEntry = data.items.find { it.first == event.pos }?.second

                if (blockEntry != null) {
                    blockEntry.items = blockEntry.items + event.itemStack
                } else {
                    val newEntry = CauldronData(mutableListOf(event.itemStack))
                    data.items = data.items + MojangPair(event.pos, newEntry)
                    blockEntry = newEntry
                }

                chunk.isUnsaved = true

                LOGGER.info("Cauldron Data: ${blockEntry.items.map { it.displayName.string }}")

                event.level.addFreshEntity(
                    ItemEntity(
                        event.level,
                        event.pos.x.toDouble(),
                        event.pos.y.toDouble() + 1,
                        event.pos.z.toDouble(),
                        ItemStack(Items.POTION, 1)
                    )
                )
            }
        }


        // Remove items from cauldrons when they are broken
        NeoForge.EVENT_BUS.addListener<BlockEvent.BreakEvent> { event ->
            if (event.level.isClientSide) return@addListener

            val chunk = event.level.getChunk(event.pos)

            val data = chunk.getData(CAULDRON_DATA)
            data.items = data.items.filter { it.first != event.pos }

            chunk.isUnsaved = true
        }

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP")
    }


    companion object {
        // Define mod id in a common place for everything to reference
        const val MOD_ID: String = "immersivemagic"

        // Directly reference a slf4j logger
        private val LOGGER: Logger = LogUtils.getLogger()


        val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>?> =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

        val CAULDRON_DATA = ATTACHMENT_TYPES.register(
            "cauldron_data",
            Supplier {
                AttachmentType.builder(Supplier {
                    ChunkData(mutableListOf())
                }).serialize(ChunkData.CODEC).build()
            }
        )
    }
}
