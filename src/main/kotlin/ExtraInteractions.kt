package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.FireType.Companion.getFromBlock
import dev.tancop.immersivemagic.Recipes.acceptedItems
import net.minecraft.core.BlockPos
import net.minecraft.core.cauldron.CauldronInteraction
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.util.FastColor
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
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
import java.util.stream.Collectors

object ExtraInteractions {
    fun fallbackInteract(
        stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand,
    ): ItemInteractionResult {
        if (state.block is LayeredCauldronBlock) {
            if (!level.isClientSide) {
                // Item might still be part of a recipe
                val insertedItem = stack.item

                val entity = level.getBlockEntity(pos) as? WaterCauldronBlockEntity
                if (entity != null) {
                    if (acceptedItems.contains(insertedItem)) {
                        // Insert if the cauldron already has that ingredient
                        if (entity.items.none { stack -> stack.item == insertedItem }) {
                            entity.items.add(stack)
                            stack.shrink(1)

                            val storedItems = entity.items.stream().map<Item?> { obj -> obj.item }
                                .collect(Collectors.toSet())
                            val fireType = getFromBlock(level, pos.below())
                            val potion = Recipes.tryGetPotion(storedItems, fireType)

                            var color = FastColor.ARGB32.color(255, 255, 255, 255)

                            if (potion != null) {
                                color = potion.getEffectColor()
                            }

                            // Spawn effect particles with the potion's color
                            // White when there's no valid potion with that recipe
                            val particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color)
                            (level as ServerLevel).sendParticles(
                                particle,
                                pos.x + 0.5,
                                pos.y + 1.0,
                                pos.z + 0.5,
                                20,
                                0.0,
                                0.2,
                                0.0,
                                0.5
                            )
                        }
                    }
                }
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide)
        }

        return ItemInteractionResult.FAIL
    }

    val waterBucketInteraction = CauldronInteraction { state, level, pos, player, hand, stack ->
        // Dilute potion
        if (!level.isClientSide) {
            val newState = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3)

            val item = stack.item
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, ItemStack(Items.BUCKET)))

            player.awardStat(Stats.FILL_CAULDRON)
            player.awardStat(Stats.ITEM_USED.get(item))

            val entity = level.getBlockEntity(pos) as? WaterCauldronBlockEntity
            entity?.items?.clear()

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

            val potionStack: ItemStack?
            val lowerState = level.getBlockState(pos.below())

            if (lowerState.`is`(Blocks.FIRE) || lowerState.`is`(Blocks.SOUL_FIRE) || lowerState.`is`(Blocks.CAMPFIRE)
                || lowerState.`is`(Blocks.SOUL_CAMPFIRE)
            ) {
                val entity = level.getBlockEntity(pos) as WaterCauldronBlockEntity

                val storedItems = entity.items.map { stack -> stack.item }.toSet()

                if (storedItems.isEmpty()) {
                    potionStack = PotionContents.createItemStack(Items.POTION, Potions.WATER)
                } else {
                    val fireType = FireType.Companion.getFromBlock(level, pos.below())

                    val potion = Recipes.tryGetPotion(storedItems, fireType)

                    potionStack =
                        potion?.getStack() ?: PotionContents.createItemStack(Items.POTION, Potions.MUNDANE)
                }
            } else {
                potionStack = PotionContents.createItemStack(Items.POTION, Potions.WATER)
            }

            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, potionStack))

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
                    val entity = level.getBlockEntity(pos) as? WaterCauldronBlockEntity
                    entity?.items?.clear()

                    level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL))

                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f)
                    level.gameEvent(null, GameEvent.FLUID_PLACE, pos)
                }

                ItemInteractionResult.sidedSuccess(level.isClientSide)
            } else {
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
            }
        }
    }
}