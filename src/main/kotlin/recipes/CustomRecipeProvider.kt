package dev.tancop.immersivemagic.recipes

import com.mojang.datafixers.util.Either
import dev.tancop.immersivemagic.DippedWeaponComponent
import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.ImmersiveMagic
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.common.crafting.CompoundIngredient
import java.util.concurrent.CompletableFuture

class CustomRecipeProvider(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    RecipeProvider(output, registries) {

    override fun buildRecipes(recipeOutput: RecipeOutput) {
        addPotionRecipes(recipeOutput)

        addBladeDippingRecipe(recipeOutput, Potions.POISON)
        addBladeDippingRecipe(recipeOutput, Potions.LONG_POISON)
        addBladeDippingRecipe(recipeOutput, Potions.STRONG_POISON)

        addBladeDippingRecipe(recipeOutput, Potions.WEAKNESS)
        addBladeDippingRecipe(recipeOutput, Potions.LONG_WEAKNESS)

        addBladeDippingRecipe(recipeOutput, Potions.SLOWNESS)
        addBladeDippingRecipe(recipeOutput, Potions.LONG_SLOWNESS)
        addBladeDippingRecipe(recipeOutput, Potions.STRONG_SLOWNESS)
    }

    fun addBladeDippingRecipe(recipeOutput: RecipeOutput, potion: Holder<Potion>) {
        val potionName = when (val path = potion.key?.location()?.path) {
            null -> {
                LOGGER.error("Could not find resource key for potion: $potion")
                return
            }

            else -> path
        }

        val recipeName = ResourceLocation.fromNamespaceAndPath(ImmersiveMagic.MOD_ID, "dip_blade_in_$potionName")

        ToolDippingRecipeBuilder(
            PotionRef.of(potion),
            CompoundIngredient.of(
                Ingredient.of(ItemTags.SWORDS),
                Ingredient.of(ItemTags.AXES)
            ),
            null,
            DataComponentPatch.builder()
                .set(ImmersiveMagic.DIPPED_WEAPON.get(), DippedWeaponComponent(5, Either.left(potion)))
                .build()
        ).save(recipeOutput, recipeName)
    }

    fun addPotionRecipes(recipeOutput: RecipeOutput) {
        addWitherPotions(recipeOutput)

        // Positive effects

        addVanillaPotion(recipeOutput, "healing", Potions.HEALING, Items.GLISTERING_MELON_SLICE)
        addVanillaPotion(
            recipeOutput, "strong_healing", Potions.STRONG_HEALING, Items.GLISTERING_MELON_SLICE, Items.GLOWSTONE_DUST
        )

        addVanillaPotion(recipeOutput, "fire_resistance", Potions.FIRE_RESISTANCE, Items.MAGMA_CREAM)
        addVanillaPotion(
            recipeOutput, "long_fire_resistance", Potions.LONG_FIRE_RESISTANCE, Items.MAGMA_CREAM, Items.REDSTONE
        )

        addVanillaPotion(recipeOutput, "regeneration", Potions.REGENERATION, Items.GHAST_TEAR)
        addVanillaPotion(recipeOutput, "long_regeneration", Potions.LONG_REGENERATION, Items.GHAST_TEAR, Items.REDSTONE)
        addVanillaPotion(
            recipeOutput, "strong_regeneration", Potions.STRONG_REGENERATION, Items.GHAST_TEAR, Items.GLOWSTONE_DUST
        )

        addVanillaPotion(recipeOutput, "strength", Potions.STRENGTH, Items.BLAZE_POWDER)
        addVanillaPotion(recipeOutput, "long_strength", Potions.LONG_STRENGTH, Items.BLAZE_POWDER, Items.REDSTONE)
        addVanillaPotion(
            recipeOutput,
            "strong_strength",
            Potions.STRONG_STRENGTH,
            Items.BLAZE_POWDER,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(recipeOutput, "swiftness", Potions.SWIFTNESS, Items.SUGAR)
        addVanillaPotion(recipeOutput, "long_swiftness", Potions.LONG_SWIFTNESS, Items.SUGAR, Items.REDSTONE)
        addVanillaPotion(
            recipeOutput, "strong_swiftness", Potions.STRONG_SWIFTNESS, Items.SUGAR, Items.GLOWSTONE_DUST
        )

        addVanillaPotion(recipeOutput, "night_vision", Potions.NIGHT_VISION, Items.GOLDEN_CARROT)
        addVanillaPotion(
            recipeOutput,
            "long_night_vision",
            Potions.LONG_NIGHT_VISION,
            Items.GOLDEN_CARROT,
            Items.REDSTONE
        )

        addVanillaPotion(
            recipeOutput,
            "invisibility",
            Potions.INVISIBILITY,
            Items.GOLDEN_CARROT,
            Items.FERMENTED_SPIDER_EYE
        )
        addVanillaPotion(
            recipeOutput,
            "long_invisibility",
            Potions.LONG_INVISIBILITY,
            Items.GOLDEN_CARROT,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE
        )

        addVanillaPotion(recipeOutput, "water_breathing", Potions.WATER_BREATHING, Items.PUFFERFISH)
        addVanillaPotion(
            recipeOutput,
            "long_water_breathing",
            Potions.LONG_WATER_BREATHING,
            Items.PUFFERFISH,
            Items.REDSTONE
        )

        addVanillaPotion(recipeOutput, "leaping", Potions.LEAPING, Items.RABBIT_FOOT)
        addVanillaPotion(recipeOutput, "long_leaping", Potions.LONG_LEAPING, Items.RABBIT_FOOT, Items.REDSTONE)
        addVanillaPotion(
            recipeOutput,
            "strong_leaping",
            Potions.STRONG_LEAPING,
            Items.RABBIT_FOOT,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(recipeOutput, "slow_falling", Potions.SLOW_FALLING, Items.PHANTOM_MEMBRANE)
        addVanillaPotion(
            recipeOutput,
            "long_slow_falling",
            Potions.LONG_SLOW_FALLING,
            Items.PHANTOM_MEMBRANE,
            Items.REDSTONE
        )

        // Negative effects

        addVanillaPotion(recipeOutput, "poison", Potions.POISON, Items.SPIDER_EYE)
        addVanillaPotion(recipeOutput, "long_poison", Potions.LONG_POISON, Items.SPIDER_EYE, Items.REDSTONE)
        addVanillaPotion(recipeOutput, "strong_poison", Potions.STRONG_POISON, Items.SPIDER_EYE, Items.GLOWSTONE_DUST)

        addVanillaPotion(recipeOutput, "weakness", Potions.WEAKNESS, Items.FERMENTED_SPIDER_EYE, addNetherWart = false)
        addVanillaPotion(
            recipeOutput,
            "long_weakness",
            Potions.LONG_WEAKNESS,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE,
            addNetherWart = false
        )

        addVanillaPotion(recipeOutput, "harming", Potions.HARMING, Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE)
        addVanillaPotion(
            recipeOutput,
            "strong_harming",
            Potions.STRONG_HARMING,
            Items.SPIDER_EYE,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        // Harming can be corrupted from poison or healing
        addVanillaPotion(
            recipeOutput,
            "harming_from_healing",
            Potions.HARMING,
            Items.GLISTERING_MELON_SLICE,
            Items.FERMENTED_SPIDER_EYE
        )
        addVanillaPotion(
            recipeOutput,
            "strong_harming_from_healing",
            Potions.STRONG_HARMING,
            Items.GLISTERING_MELON_SLICE,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(recipeOutput, "slowness", Potions.SLOWNESS, Items.SUGAR, Items.FERMENTED_SPIDER_EYE)
        addVanillaPotion(
            recipeOutput,
            "long_slowness",
            Potions.LONG_SLOWNESS,
            Items.SUGAR,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE
        )
        addVanillaPotion(
            recipeOutput,
            "strong_slowness",
            Potions.STRONG_SLOWNESS,
            Items.SUGAR,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(
            recipeOutput,
            "slowness_from_leaping",
            Potions.SLOWNESS,
            Items.RABBIT_FOOT,
            Items.FERMENTED_SPIDER_EYE
        )
        addVanillaPotion(
            recipeOutput,
            "long_slowness_from_leaping",
            Potions.LONG_SLOWNESS,
            Items.RABBIT_FOOT,
            Items.FERMENTED_SPIDER_EYE,
            Items.REDSTONE
        )
        addVanillaPotion(
            recipeOutput,
            "strong_slowness_from_leaping",
            Potions.STRONG_SLOWNESS,
            Items.RABBIT_FOOT,
            Items.FERMENTED_SPIDER_EYE,
            Items.GLOWSTONE_DUST
        )

        addVanillaPotion(recipeOutput, "oozing", Potions.OOZING, Items.SLIME_BLOCK)
        addVanillaPotion(recipeOutput, "weaving", Potions.WEAVING, Items.COBWEB)
        addVanillaPotion(recipeOutput, "infestation", Potions.INFESTED, Items.STONE)
        addVanillaPotion(recipeOutput, "wind_charging", Potions.WIND_CHARGED, Items.BREEZE_ROD)
    }

    fun addWitherPotions(recipeOutput: RecipeOutput) {
        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.WITHER_ROSE)), FireType.SOUL,
            PotionRef.Custom(
                ResourceLocation.fromNamespaceAndPath(ImmersiveMagic.MOD_ID, "decay"),
                PotionRef.PotionType.NORMAL
            ),
        ).save(recipeOutput, "immersivemagic:decay_potion")

        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.WITHER_ROSE), Ingredient.of(Items.GUNPOWDER)),
            FireType.SOUL,
            PotionRef.Custom(
                ResourceLocation.fromNamespaceAndPath(ImmersiveMagic.MOD_ID, "decay"),
                PotionRef.PotionType.SPLASH
            ),
        ).save(recipeOutput, "immersivemagic:decay_splash_potion")

        BrewingRecipeBuilder(
            listOf(
                Ingredient.of(Items.NETHER_WART),
                Ingredient.of(Items.WITHER_ROSE),
                Ingredient.of(Items.GUNPOWDER),
                Ingredient.of(Items.DRAGON_BREATH)
            ),
            FireType.SOUL,
            PotionRef.Custom(
                ResourceLocation.fromNamespaceAndPath(ImmersiveMagic.MOD_ID, "decay"),
                PotionRef.PotionType.LINGERING
            ),
        ).save(recipeOutput, "immersivemagic:decay_lingering_potion")
    }

    fun addVanillaPotion(
        recipeOutput: RecipeOutput,
        id: String,
        result: Holder<Potion>,
        vararg items: Item,
        addNetherWart: Boolean = true
    ) {
        val baseIngredients = if (addNetherWart) {
            items.map { Ingredient.of(it) } + Ingredient.of(Items.NETHER_WART)
        } else {
            items.map { Ingredient.of(it) }
        }

        BrewingRecipeBuilder(
            baseIngredients, FireType.SOUL, PotionRef.of(result)
        ).save(recipeOutput, "immersivemagic:${id}_potion")

        BrewingRecipeBuilder(
            baseIngredients + Ingredient.of(Items.GUNPOWDER),
            FireType.SOUL,
            PotionRef.of(result, PotionRef.PotionType.SPLASH)
        ).save(recipeOutput, "immersivemagic:${id}_splash_potion")

        BrewingRecipeBuilder(
            baseIngredients + Ingredient.of(Items.GUNPOWDER) + Ingredient.of(Items.DRAGON_BREATH),
            FireType.SOUL,
            PotionRef.of(result, PotionRef.PotionType.LINGERING)
        ).save(recipeOutput, "immersivemagic:${id}_lingering_potion")
    }
}