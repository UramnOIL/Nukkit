package cn.nukkit.item.enchantment

import cn.nukkit.block.BlockPumpkin
import cn.nukkit.item.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
enum class EnchantmentType {
	ALL, ARMOR, ARMOR_HEAD, ARMOR_TORSO, ARMOR_LEGS, ARMOR_FEET, SWORD, DIGGER, FISHING_ROD, BREAKABLE, BOW, WEARABLE, TRIDENT;

	fun canEnchantItem(item: Item): Boolean {
		return if (this == ALL) {
			true
		} else if (this == BREAKABLE && item.maxDurability >= 0) {
			true
		} else if (item is ItemArmor) {
			if (this == ARMOR) {
				return true
			}
			when (this) {
				ARMOR_HEAD -> item.isHelmet()
				ARMOR_TORSO -> item.isChestplate()
				ARMOR_LEGS -> item.isLeggings()
				ARMOR_FEET -> item.isBoots()
				else -> false
			}
		} else {
			when (this) {
				SWORD -> item.isSword
				DIGGER -> item.isPickaxe || item.isShovel || item.isAxe
				BOW -> item is ItemBow
				FISHING_ROD -> item is ItemFishingRod
				WEARABLE -> item is ItemArmor || item is ItemElytra || item is ItemSkull || item.block is BlockPumpkin
				TRIDENT -> item is ItemTrident
				else -> false
			}
		}
	}
}