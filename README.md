# Immersive Magic

This mod adds new mechanics to

# Brewing

Place a cauldron on top of a campfire and fill it with water.

# Recipes

You can add new brewing recipes with a data pack. The most common type is a registered potion from vanilla or another
mod:

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

Custom potions are a bit more complicated. These let you define any set of effects:

```json5
{
  "type": "immersivemagic:brewing",
  "fireType": "SOUL",
  "ingredients": [
    {
      "item": "minecraft:wither_rose"
    }
  ],
  "result": {
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
}
```

The last feature is changing the receiver item the player uses to take out the potion. This lets you do dipping or
infusion recipes where you right click with something other than a bottle. A recipe for making poison arrows can look
like this:

```json5
{
  "type": "immersivemagic:brewing",
  "fireType": "NORMAL",
  "ingredients": [
    {
      "item": "minecraft:spider_eye"
    }
  ],
  "receiver": {
    "item": "minecraft:arrow"
  },
  "result": {
    // color used for particle effects, items don't come with a color like potions
    "color": {
      "blue": 99,
      "green": 163,
      "red": 135
    },
    // the item stack this recipe gives you
    "item": {
      // optional data components (https://minecraft.wiki/w/Data_component_format)
      "components": {
        "minecraft:potion_contents": {
          "potion": "minecraft:poison"
        }
      },
      "count": 1,
      "id": "minecraft:tipped_arrow"
    }
  }
}
```

## Fire Types

The `fireType` field decides what level of heat is needed to brew that recipe. You can always use a higher level instead
of a lower one - a recipe that needs a normal fire also works with a soul fire.

* `NONE` - no fire
* `NORMAL` - normal fire or campfire
* `SOUL` - soul fire or soul campfire
* `BLAZE` - Create blaze burner