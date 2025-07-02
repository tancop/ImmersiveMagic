package dev.tancop.immersivemagic

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec

@EventBusSubscriber(modid = ImmersiveMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object Config {
    private val BUILDER = ModConfigSpec.Builder()

    private val DISABLE_VANILLA_BREWING: ModConfigSpec.BooleanValue = BUILDER
        .comment("Disable normal brewing for vanilla effect potions? You need to restart the server to apply this setting.")
        .translation("config.immersivemagic.disable_vanilla_brewing")
        .define("disable_vanilla_brewing", false)

    val SPEC: ModConfigSpec = BUILDER.build()

    var disableVanillaBrewing: Boolean = false

    @SubscribeEvent
    fun onLoad(event: ModConfigEvent?) {
        when (event) {
            is ModConfigEvent.Loading, is ModConfigEvent.Reloading -> {
                disableVanillaBrewing = DISABLE_VANILLA_BREWING.get()
            }
        }
    }
}
