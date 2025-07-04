# Writing Recipes

Immersive Magic supports customization with data packs. Recipes should be placed in `data/recipe` like with vanilla
crafting mechanics.

## Brewing

```json5
{
  "type": "immersivemagic:brewing",
  // what type of fire is needed to brew this
  "fireType": "NORMAL",
  // ingredients work just like a vanilla recipe
  "ingredients": [
    {
      "item": "minecraft:spider_eye"
    }
  ],
  // the potion you get when taking out a bottle
  "result": {
    "potion": "minecraft:poison"
  }
}
```

Custom potions let you define any set of effects. A simple Bedrock-style wither potion looks like this:

```json5
// use this as the `result`
{
  // normal, splash or lingering
  "type": "NORMAL",
  // RGB color used for the potion item and effect particles
  "color": {
    "blue": 86,
    "green": 97,
    "red": 115
  },
  "effects": [
    {
      // optional, default `false`
      "ambient": false,
      // optional, default `0` (level 1)
      "amplifier": 0,
      "duration": 800,
      "effect": "minecraft:wither",
      // optional, default `true`
      "showIcon": true,
      // optional, default `true`
      "visible": true
    }
  ],
  // translation key for the display name, you should put the actual name
  // in a server-provided resource pack
  "name": "potion.immersivemagic.decay_potion"
}
```

You can also use any item stack instead of a potion. This lets you define recipes for items like honey or ominous
bottles that have a separate item ID from normal potions.

```json5
{
  // color used for particle effects, items don't come with a color like potions
  "color": {
    "blue": 99,
    "green": 163,
    "red": 135
  },
  // the item stack this recipe gives you
  "item": {
    "count": 1,
    "id": "minecraft:ominous_bottle"
  }
}
```

### Fire Types

The `fireType` field decides what level of heat is needed to brew that recipe. You can always use a higher level instead
of a lower one - a recipe that needs a normal fire also works with a soul fire.

* `NONE` - no fire
* `NORMAL` - normal fire or campfire
* `SOUL` - soul fire or soul campfire
* `BLAZE` - Create blaze burner

## Dipping

Dipping recipes work similar to brewing. The `potion` field is the potion inside the cauldron and `container` is the
dipped item. The container is consumed together with one level from the cauldron.

```json5
// Recipe for a poison arrow
{
  "type": "immersivemagic:dipping",
  "potion": {
    "type": "NORMAL",
    "potion": "minecraft:poison"
  },
  "container": {
    "item": "minecraft:arrow"
  },
  "result": {
    "count": 1,
    "id": "minecraft:tipped_arrow",
    "components": {
      "minecraft:potion_contents": {
        "potion": "minecraft:poison"
      }
    }
  }
}
```