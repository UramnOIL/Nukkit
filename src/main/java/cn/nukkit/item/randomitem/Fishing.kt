package cn.nukkit.item.randomitem

import cn.nukkit.block.BlockID
import cn.nukkit.item.Item
import cn.nukkit.item.ItemID
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.potion.Potion
import cn.nukkit.utils.DyeColor

/**
 * Created by Snake1999 on 2016/1/15.
 * Package cn.nukkit.item.randomitem in project nukkit.
 */
object Fishing {
	val ROOT_FISHING: Selector = putSelector(Selector(RandomItem.ROOT))
	val FISHES = RandomItem.putSelector(Selector(ROOT_FISHING), 0.85f)
	val TREASURES = RandomItem.putSelector(Selector(ROOT_FISHING), 0.05f)
	val JUNKS = RandomItem.putSelector(Selector(ROOT_FISHING), 0.1f)
	val FISH = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.RAW_FISH, FISHES), 0.6f)
	val SALMON = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.RAW_SALMON, FISHES), 0.25f)
	val CLOWNFISH = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.CLOWNFISH, FISHES), 0.02f)
	val PUFFERFISH = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.PUFFERFISH, FISHES), 0.13f)
	val TREASURE_BOW = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.BOW, TREASURES), 0.1667f)
	val TREASURE_ENCHANTED_BOOK = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.ENCHANTED_BOOK, TREASURES), 0.1667f)
	val TREASURE_FISHING_ROD = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.FISHING_ROD, TREASURES), 0.1667f)
	val TREASURE_NAME_TAG = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.NAME_TAG, TREASURES), 0.1667f)
	val TREASURE_SADDLE = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.SADDLE, TREASURES), 0.1667f)
	val JUNK_BOWL = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.BOWL, JUNKS), 0.12f)
	val JUNK_FISHING_ROD = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.FISHING_ROD, JUNKS), 0.024f)
	val JUNK_LEATHER = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.LEATHER, JUNKS), 0.12f)
	val JUNK_LEATHER_BOOTS = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.LEATHER_BOOTS, JUNKS), 0.12f)
	val JUNK_ROTTEN_FLESH = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.ROTTEN_FLESH, JUNKS), 0.12f)
	val JUNK_STICK = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.STICK, JUNKS), 0.06f)
	val JUNK_STRING_ITEM = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.STRING, JUNKS), 0.06f)
	val JUNK_WATTER_BOTTLE = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.POTION, Potion.NO_EFFECTS, JUNKS), 0.12f)
	val JUNK_BONE = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.BONE, JUNKS), 0.12f)
	val JUNK_INK_SAC = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.DYE, DyeColor.BLACK.dyeData, 10, JUNKS), 0.012f)
	val JUNK_TRIPWIRE_HOOK = RandomItem.putSelector(ConstantItemSelector(BlockID.TRIPWIRE_HOOK, JUNKS), 0.12f)
	fun getFishingResult(rod: Item?): Item? {
		var fortuneLevel = 0
		var lureLevel = 0
		if (rod != null) {
			if (rod.getEnchantment(Enchantment.Companion.ID_FORTUNE_FISHING) != null) {
				fortuneLevel = rod.getEnchantment(Enchantment.Companion.ID_FORTUNE_FISHING).level
			} else if (rod.getEnchantment(Enchantment.Companion.ID_LURE) != null) {
				lureLevel = rod.getEnchantment(Enchantment.Companion.ID_LURE).level
			}
		}
		return getFishingResult(fortuneLevel, lureLevel)
	}

	fun getFishingResult(fortuneLevel: Int, lureLevel: Int): Item? {
		val treasureChance = limitRange(0f, 1f, 0.05f + 0.01f * fortuneLevel - 0.01f * lureLevel)
		val junkChance = limitRange(0f, 1f, 0.05f - 0.025f * fortuneLevel - 0.01f * lureLevel)
		val fishChance = limitRange(0f, 1f, 1 - treasureChance - junkChance)
		RandomItem.putSelector(FISHES, fishChance)
		RandomItem.putSelector(TREASURES, treasureChance)
		RandomItem.putSelector(JUNKS, junkChance)
		val result = RandomItem.selectFrom(ROOT_FISHING)
		return if (result is Item) result else null
	}

	private fun limitRange(min: Float, max: Float, value: Float): Float {
		if (value >= max) return max
		return if (value <= min) min else value
	}
}