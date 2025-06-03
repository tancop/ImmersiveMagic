package dev.tancop.immersivemagic

import com.mojang.logging.LogUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.block.*
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

            // Block is a cauldron with water
            (event.level.getBlockState(event.pos).block as? LayeredCauldronBlock)?.let { block ->
                if (event.itemStack.item !in (Recipes.acceptedItems + Items.GLASS_BOTTLE + Items.POTION)) return@addListener

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

                val chunk = event.level.getChunk(event.pos)

                val data = chunk.getData(CAULDRON_DATA)
                var blockEntry = data.items.find { it.first == event.pos }?.second

                val stack = event.itemStack
                val item = stack.item

                val state = event.level.getBlockState(event.pos)

                when (item) {
                    Items.POTION -> {
                        val comp = stack.get(DataComponents.POTION_CONTENTS)
                        LOGGER.info("Used potion ${comp?.potion} on cauldron")
                        if (blockEntry != null
                            && state.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL
                            && comp?.`is`(Potions.WATER) ?: false
                        ) {
                            LOGGER.info("Diluted potion")
                            blockEntry.items = emptyList()
                        }
                    }

                    Items.GLASS_BOTTLE -> {
                        if (blockEntry != null && blockEntry.items.isNotEmpty()) {
                            val ingredientSet = blockEntry.items.map { it.item }.toSet()

                            val foundRecipe = Recipes.recipes[ingredientSet]?.let { (fireNeeded, potion) ->
                                if (fireType >= fireNeeded) {
                                    LOGGER.info("Taking out potion")
                                    val contents = PotionContents(potion)
                                    val potionStack = ItemStack(Items.POTION, 1)
                                    potionStack.set(DataComponents.POTION_CONTENTS, contents)

                                    event.entity.inventory.add(potionStack)
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
                        (event.level as? ServerLevel)?.let { level ->
                            val particle = ColorParticleOption.create(
                                ParticleTypes.ENTITY_EFFECT,
                                0.1f, 0.2f, 0.8f
                            )

                            level.sendParticles(
                                particle,
                                event.pos.x.toDouble() + 0.5,
                                event.pos.y.toDouble() + 1.5,
                                event.pos.z.toDouble() + 0.5,
                                30,
                                0.1, 0.5, 0.1,
                                0.5
                            )
                        }

                        val newStack = stack.copy()
                        stack.shrink(1)
                        newStack.count = 1

                        if (blockEntry != null) {
                            blockEntry.items = blockEntry.items + newStack
                        } else {
                            val newEntry = CauldronData(mutableListOf(newStack))
                            data.items = data.items + MojangPair(event.pos, newEntry)
                            blockEntry = newEntry
                        }
                    }
                }

                chunk.isUnsaved = true

                LOGGER.info("Cauldron Data: ${blockEntry?.items?.map { it.displayName.string }}")
            }

            // Block is an empty cauldron, check if the player is filling it up
            (event.level.getBlockState(event.pos).block as? CauldronBlock)?.let { block ->
                if (event.itemStack.item == Items.POTION) {
                    val chunk = event.level.getChunk(event.pos)

                    val data = chunk.getData(CAULDRON_DATA)
                    var blockEntry = data.items.find { it.first == event.pos }?.second

                    val stack = event.itemStack

                    val comp = stack.get(DataComponents.POTION_CONTENTS)

                    if (blockEntry != null && comp?.`is`(Potions.WATER) ?: false) {
                        blockEntry.items = emptyList()
                    }
                } else if (event.itemStack.item == Items.WATER_BUCKET) {
                    val chunk = event.level.getChunk(event.pos)

                    val data = chunk.getData(CAULDRON_DATA)
                    var blockEntry = data.items.find { it.first == event.pos }?.second

                    if (blockEntry != null) {
                        blockEntry.items = emptyList()
                    }
                }
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
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID)

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
