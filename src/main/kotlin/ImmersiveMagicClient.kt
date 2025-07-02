package dev.tancop.immersivemagic

import net.minecraft.client.gui.screens.Screen
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory


@Mod(value = ImmersiveMagic.MOD_ID, dist = [Dist.CLIENT])
class ImmersiveMagicClient(container: ModContainer) {
    init {
        container.registerExtensionPoint<IConfigScreenFactory?>(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { mod: ModContainer, parent: Screen -> ConfigurationScreen(mod, parent) })
    }
}