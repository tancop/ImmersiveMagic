package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.ImmersiveMagic.Companion.SOUL_BINDING
import dev.tancop.immersivemagic.recipes.SoulBindingRecipeInput
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.level.Level
import kotlin.jvm.optionals.getOrNull

object SoulBindingMechanics {
    fun handleEntityDeath(level: Level, killer: Player, deadEntity: LivingEntity): Boolean {
        val recipes = level.recipeManager
        println("checking on entity death, $deadEntity killed by $killer")

        val input = SoulBindingRecipeInput(killer, deadEntity)
        val recipe = recipes.getRecipeFor(SOUL_BINDING.get(), input, level).getOrNull()

        if (recipe == null) return false

        println("found recipe for entity death")

        killer.setItemInHand(
            InteractionHand.OFF_HAND,
            ItemUtils.createFilledResult(
                killer.getItemInHand(InteractionHand.OFF_HAND),
                killer,
                recipe.value.assemble(input, level.registryAccess()).apply {
                    applyServerComponentLore()
                }
            )
        )

        killer.giveExperiencePoints(-recipe.value.xpCost)

        return true
    }
}