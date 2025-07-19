package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.PotionEffect
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.util.FastColor
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.Ingredient
import java.util.concurrent.CompletableFuture

class BrewingRecipeProvider(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    RecipeProvider(output, registries) {

    override fun buildRecipes(output: RecipeOutput) {
        addWitherPotions(output)

        // Positive effects

        addVanillaPotion(output, "healing", Potions.HEALING, Items.GLISTERING_MELON_SLICE)
        addVanillaPotion(
            output, "strong_healing", Potions.STRONG_HEALING, Items.GLISTERING_MELON_SLICE, Items.GLOWSTONE_DUST
        )

        addVanillaPotion(output, "fire_resistance", Potions.FIRE_RESISTANCE, Items.MAGMA_CREAM)
        addVanillaPotion(
            output, "long_fire_resistance", Potions.LONG_FIRE_RESISTANCE, Items.MAGMA_CREAM, Items.REDSTONE
        )

        addVanillaPotion(output, "regeneration", Potions.REGENERATION, Items.GHAST_TEAR)
        addVanillaPotion(output, "long_regeneration", Potions.LONG_REGENERATION, Items.GHAST_TEAR, Items.REDSTONE)
        addVanillaPotion(
            output, "strong_regeneration", Potions.STRONG_REGENERATION, Items.GHAST_TEAR, Items.GLOWSTONE_DUST
        )

        addVanillaPotion(output, "strength", Potions.STRENGTH, Items.BLAZE_POWDER)
        addVanillaPotion(output, "long_strength", Potions.LONG_STRENGTH, Items.BLAZE_POWDER, Items.REDSTONE)
        addVanillaPotion(output, "strong_strength", Potions.STRONG_STRENGTH, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST)

        addVanillaPotion(output, "swiftness", Potions.SWIFTNESS, Items.SUGAR)
        addVanillaPotion(output, "long_swiftness", Potions.LONG_SWIFTNESS, Items.SUGAR, Items.REDSTONE)
        addVanillaPotion(
            output, "strong_swiftness", Potions.STRONG_SWIFTNESS, Items.SUGAR, Items.GLOWSTONE_DUST
        )

        addVanillaPotion(output, "night_vision", Potions.NIGHT_VISION, Items.GOLDEN_CARROT)
        addVanillaPotion(output, "long_night_vision", Potions.LONG_NIGHT_VISION, Items.GOLDEN_CARROT, Items.REDSTONE)

        addVanillaPotion(output, "invisibility", Potions.INVISIBILITY, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE)
        addVanillaPotion(
            output,
            "long_invisibility",
            Potions.LONG_INVISIBILITY,
            Items.GOLDEN_CARROT,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE
        )

        addVanillaPotion(output, "water_breathing", Potions.WATER_BREATHING, Items.PUFFERFISH)
        addVanillaPotion(output, "long_water_breathing", Potions.LONG_WATER_BREATHING, Items.PUFFERFISH, Items.REDSTONE)

        addVanillaPotion(output, "leaping", Potions.LEAPING, Items.RABBIT_FOOT)
        addVanillaPotion(output, "long_leaping", Potions.LONG_LEAPING, Items.RABBIT_FOOT, Items.REDSTONE)
        addVanillaPotion(output, "strong_leaping", Potions.STRONG_LEAPING, Items.RABBIT_FOOT, Items.GLOWSTONE_DUST)

        addVanillaPotion(output, "slow_falling", Potions.SLOW_FALLING, Items.PHANTOM_MEMBRANE)
        addVanillaPotion(output, "long_slow_falling", Potions.LONG_SLOW_FALLING, Items.PHANTOM_MEMBRANE, Items.REDSTONE)

        // Negative effects

        addVanillaPotion(output, "poison", Potions.POISON, Items.SPIDER_EYE)
        addVanillaPotion(output, "long_poison", Potions.LONG_POISON, Items.SPIDER_EYE, Items.REDSTONE)
        addVanillaPotion(output, "strong_poison", Potions.STRONG_POISON, Items.SPIDER_EYE, Items.GLOWSTONE_DUST)

        addVanillaPotion(output, "weakness", Potions.WEAKNESS, Items.FERMENTED_SPIDER_EYE, addNetherWart = false)
        addVanillaPotion(
            output,
            "long_weakness",
            Potions.LONG_WEAKNESS,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE,
            addNetherWart = false
        )

        addVanillaPotion(output, "harming", Potions.HARMING, Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE)
        addVanillaPotion(
            output,
            "strong_harming",
            Potions.STRONG_HARMING,
            Items.SPIDER_EYE,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        // Harming can be corrupted from poison or healing
        addVanillaPotion(
            output,
            "harming_from_healing",
            Potions.HARMING,
            Items.GLISTERING_MELON_SLICE,
            Items.FERMENTED_SPIDER_EYE
        )
        addVanillaPotion(
            output,
            "strong_harming_from_healing",
            Potions.STRONG_HARMING,
            Items.GLISTERING_MELON_SLICE,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(output, "slowness", Potions.SLOWNESS, Items.SUGAR, Items.FERMENTED_SPIDER_EYE)
        addVanillaPotion(
            output,
            "long_slowness",
            Potions.LONG_SLOWNESS,
            Items.SUGAR,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE
        )
        addVanillaPotion(
            output,
            "strong_slowness",
            Potions.STRONG_SLOWNESS,
            Items.SUGAR,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(
            output,
            "slowness_from_leaping",
            Potions.SLOWNESS,
            Items.RABBIT_FOOT,
            Items.FERMENTED_SPIDER_EYE
        )
        addVanillaPotion(
            output,
            "long_slowness_from_leaping",
            Potions.LONG_SLOWNESS,
            Items.RABBIT_FOOT,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE
        )
        addVanillaPotion(
            output,
            "strong_slowness_from_leaping",
            Potions.STRONG_SLOWNESS,
            Items.RABBIT_FOOT,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(output, "oozing", Potions.OOZING, Items.SLIME_BLOCK)
        addVanillaPotion(output, "weaving", Potions.WEAVING, Items.COBWEB)
        addVanillaPotion(output, "infestation", Potions.INFESTED, Items.STONE)
        addVanillaPotion(output, "wind_charging", Potions.WIND_CHARGED, Items.BREEZE_ROD)
    }

    fun addWitherPotions(output: RecipeOutput) {
        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.WITHER_ROSE)), FireType.SOUL,
            PotionRef.of(
                "potion.immersivemagic.decay_potion", listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ), FastColor.ARGB32.opaque(0x736156)
            )
        ).save(output, "immersivemagic:decay_potion")

        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.WITHER_ROSE), Ingredient.of(Items.GUNPOWDER)),
            FireType.SOUL,
            PotionRef.of(
                "potion.immersivemagic.decay_splash_potion", listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ), FastColor.ARGB32.opaque(0x736156), type = PotionRef.PotionType.SPLASH
            )
        ).save(output, "immersivemagic:decay_splash_potion")

        BrewingRecipeBuilder(
            listOf(
                Ingredient.of(Items.NETHER_WART),
                Ingredient.of(Items.WITHER_ROSE),
                Ingredient.of(Items.GUNPOWDER),
                Ingredient.of(Items.DRAGON_BREATH)
            ), FireType.SOUL,
            PotionRef.of(
                "potion.immersivemagic.decay_lingering_potion", listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ), FastColor.ARGB32.opaque(0x736156), type = PotionRef.PotionType.LINGERING
            )
        ).save(output, "immersivemagic:decay_lingering_potion")
    }

    fun addVanillaPotion(
        output: RecipeOutput, id: String, result: Holder<Potion>, vararg items: Item, addNetherWart: Boolean = true
    ) {
        val baseIngredients = if (addNetherWart) {
            items.map { Ingredient.of(it) } + Ingredient.of(Items.NETHER_WART)
        } else {
            items.map { Ingredient.of(it) }
        }

        BrewingRecipeBuilder(
            baseIngredients, FireType.SOUL, PotionRef.of(result)
        ).save(output, "immersivemagic:${id}_potion")

        BrewingRecipeBuilder(
            baseIngredients + Ingredient.of(Items.GUNPOWDER),
            FireType.SOUL,
            PotionRef.of(result, PotionRef.PotionType.SPLASH)
        ).save(output, "immersivemagic:${id}_splash_potion")

        BrewingRecipeBuilder(
            baseIngredients + Ingredient.of(Items.GUNPOWDER) + Ingredient.of(Items.DRAGON_BREATH),
            FireType.SOUL,
            PotionRef.of(result, PotionRef.PotionType.LINGERING)
        ).save(output, "immersivemagic:${id}_lingering_potion")
    }
}