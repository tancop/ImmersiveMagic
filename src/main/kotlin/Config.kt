package dev.tancop.immersivemagic

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = ImmersiveMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object Config {
    private val BUILDER = ModConfigSpec.Builder()

    private val LOG_DIRT_BLOCK: ModConfigSpec.BooleanValue = BUILDER
        .comment("Whether to log the dirt block on common setup")
        .define("logDirtBlock", true)

    val SPEC: ModConfigSpec = BUILDER.build()

    var logDirtBlock: Boolean = false

    @SubscribeEvent
    fun onLoad(event: ModConfigEvent?) {
        when (event) {
            is ModConfigEvent.Loading, is ModConfigEvent.Reloading -> {
                logDirtBlock = LOG_DIRT_BLOCK.get()
            }
        }
    }
}
