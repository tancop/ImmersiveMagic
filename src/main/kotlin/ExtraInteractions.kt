package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.recipes.BrewingRecipe
import dev.tancop.immersivemagic.recipes.BrewingRecipeInput
import dev.tancop.immersivemagic.recipes.DippingRecipeInput
import net.minecraft.core.BlockPos
import net.minecraft.core.cauldron.CauldronInteraction
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LayeredCauldronBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import kotlin.jvm.optionals.getOrNull

object ExtraInteractions {
    // Checks for a matching recipe, sets the stored potion and emits particles
    private fun checkRecipesAndUpdate(
        level: Level,
        entity: LayeredCauldronBlockEntity,
        pos: BlockPos
    ) {
        val recipes = level.recipeManager

        val input = BrewingRecipeInput(entity)
        val recipe = recipes.getAllRecipesFor(ImmersiveMagic.BREWING.get())
            .filter { it.value.matches(input, level) }.maxByOrNull { it.value.ingredients.size }

        entity.storedPotion = recipe?.value?.result

        entity.spawnParticles(level, pos, 20)
    }

    fun fallbackInteract(
        stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand,
    ): ItemInteractionResult {
        if (state.block is LayeredCauldronBlock) {
            if (!level.isClientSide) {
                val entity = level.getBlockEntity(pos) as LayeredCauldronBlockEntity
                if (player.isShiftKeyDown && hand == InteractionHand.MAIN_HAND) {
                    // Look for dipping recipes
                    val recipes = level.recipeManager

                    val input = DippingRecipeInput(entity, stack)
                    val recipe = recipes.getRecipeFor(ImmersiveMagic.DIPPING.get(), input, level)

                    val result = recipe.getOrNull()?.value?.result

                    if (result != null) {
                        player.inventory.add(result)
                        LayeredCauldronBlock.lowerFillLevel(state, level, pos)

                        if (!player.isCreative) stack.shrink(1)

                        return ItemInteractionResult.CONSUME
                    }
                } else {
                    // Item might still be part of a recipe
                    if (BrewingRecipe.getAcceptedIngredients(level).any { it.test(stack) }) {
                        entity.items.add(stack)

                        checkRecipesAndUpdate(level, entity, pos)

                        if (!player.isCreative) stack.shrink(1)

                        return ItemInteractionResult.CONSUME
                    }
                }
                // no match
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
            }
        }

        return ItemInteractionResult.FAIL
    }

    val waterBucketInteraction = CauldronInteraction { state, level, pos, player, hand, stack ->
        // Filling with a water bucket dilutes any potion inside
        if (!level.isClientSide) {
            val newState = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3)

            val item = stack.item
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, ItemStack(Items.BUCKET)))

            player.awardStat(Stats.FILL_CAULDRON)
            player.awardStat(Stats.ITEM_USED.get(item))

            val entity = level.getBlockEntity(pos) as? LayeredCauldronBlockEntity
            entity?.items?.clear()
            entity?.storedPotion = null

            level.setBlockAndUpdate(pos, newState)

            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F)
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos)
        }

        ItemInteractionResult.sidedSuccess(level.isClientSide)
    }

    val bottleInteraction = CauldronInteraction { state, level, pos, player, hand, stack ->
        // Taking out water/potions from the cauldron
        if (!level.isClientSide) {
            val item = stack.item

            val entity = level.getBlockEntity(pos) as LayeredCauldronBlockEntity

            val potionStack = if (entity.items.isEmpty()) {
                // No ingredients -> water
                PotionContents.createItemStack(Items.POTION, Potions.WATER)
            } else {
                // Stored potion if there is one, mundane if not (= invalid recipe)
                entity.storedPotion?.getStack() ?: PotionContents.createItemStack(Items.POTION, Potions.MUNDANE)
            }

            player.inventory.add(potionStack)

            player.awardStat(Stats.USE_CAULDRON)
            player.awardStat(Stats.ITEM_USED.get(item))

            LayeredCauldronBlock.lowerFillLevel(state, level, pos)

            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f)
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos)
        }

        ItemInteractionResult.sidedSuccess(level.isClientSide)
    }

    val potionInteraction = CauldronInteraction { state, level, pos, player, hand, stack ->
        // Inserting a water bottle or some other type of potion
        if (state.getValue(LayeredCauldronBlock.LEVEL) == 3) {
            // Cauldron is already full
            ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        } else {
            val potionContents = stack.get(DataComponents.POTION_CONTENTS)
            if (potionContents != null && potionContents.`is`(Potions.WATER)) {
                if (!level.isClientSide) {
                    player.setItemInHand(
                        hand,
                        ItemUtils.createFilledResult(stack, player, ItemStack(Items.GLASS_BOTTLE))
                    )

                    player.awardStat(Stats.USE_CAULDRON)
                    player.awardStat(Stats.ITEM_USED.get(stack.item))

                    // Adding water dilutes any stored potion
                    val entity = level.getBlockEntity(pos) as? LayeredCauldronBlockEntity
                    entity?.items?.clear()

                    level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL))

                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f)
                    level.gameEvent(null, GameEvent.FLUID_PLACE, pos)
                }

                ItemInteractionResult.sidedSuccess(level.isClientSide)
            } else {
                // Only water potions can be inserted
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
            }
        }
    }

    val stirInteraction = CauldronInteraction { state, level, pos, player, hand, stack ->
        // Player right-clicked with an empty hand to force a recipe check
        if (!level.isClientSide) {
            val entity = level.getBlockEntity(pos) as LayeredCauldronBlockEntity

            checkRecipesAndUpdate(level, entity, pos)
        }

        ItemInteractionResult.SUCCESS
    }
}