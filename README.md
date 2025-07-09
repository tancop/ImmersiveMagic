# Immersive Magic

This mod adds new mechanics to make Minecraft feel more interesting with in-world crafting instead of magic blocks and
UI popups. Inspired by [Violet Moon's Botania](https://modrinth.com/mod/botania) and the original, never fully
implemented Beta 1.9 brewing system.

# Brewing

To get started with brewing, place a cauldron on top of a fire or a campfire and fill it with water. You can put in some
ingredients by right clicking with the item. The order you put them in doesn't matter. The cauldron will give off
colored bubbles if the items in it produce a useful potion. Take it out by right clicking with an empty bottle.
If the recipe was wrong, you get a mundane potion instead.

Some recipes need hotter soul fire (or no fire at all) to work. You can always use a stronger fire instead of a weaker
one, so don't worry about keeping both normal and soul fires around. If you add ingredients before the cauldron heats
up, you need to right click on it with an empty hand to stir the potion.

By default, you can brew all the vanilla potions using the same recipes as normal. Server owners
can also [add new recipes](./docs/Writing_Recipes.md) to tweak gameplay or add new potions without client-side mods.

# Dipping

Some items can be dipped in a potion to transform them into something new. Right click a potion filled cauldron while
sneaking with an item in your hand. This consumes the item and gives you the result.

# Sacrifice

You can sacrifice some mobs to get extra rewards with their normal drops. You need an altar built like this:

```
XXX
XGX
XXX
```

where `G` is a gold block and `X` is a top-half polished diorite slab. When you (or any other player) kill a mob of the
right type on top of the altar, lightning strikes on the gold block and you receive the drops. Some sacrifices cost XP
and can't be performed if you have less than the required amount.

Some recipes need specific items dropped on the altar so the sacrifice can transform them, like turning an iron
ingot into gold by killing a pig. You should only drop one stack for every type of item, but bigger stacks or extra
unrelated items are fine.