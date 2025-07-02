package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.advancements.AdvancementRequirements
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient


class BrewingRecipeBuilder(
    val ingredients: List<Ingredient>,
    val fireType: FireType,
    val result: PotionRef
) :
    RecipeBuilder {
    val criteria: MutableMap<String, Criterion<*>> = LinkedHashMap()

    override fun unlockedBy(name: String, criterion: Criterion<*>): BrewingRecipeBuilder {
        this.criteria.put(name, criterion)
        return this
    }

    override fun group(group: String?): BrewingRecipeBuilder {
        return this
    }

    override fun getResult(): Item {
        return this.result.getStack().item
    }

    override fun save(output: RecipeOutput, id: ResourceLocation) {
        val advancement = output.advancement()
            .addCriterion("has_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR)

        this.criteria.forEach { (key, criterion) -> advancement.addCriterion(key, criterion) }

        val recipe = BrewingRecipe(ingredients, fireType, result)
        output.accept(id, recipe, advancement.build(id.withPrefix("brew_")))
    }
}