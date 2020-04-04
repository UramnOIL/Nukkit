package cn.nukkit.item.enchantment

import cn.nukkit.item.Item
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentDurability : Enchantment(Enchantment.Companion.ID_DURABILITY, "durability", 5, EnchantmentType.BREAKABLE) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 5 + (level - 1) * 8
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 3

	override fun isCompatibleWith(enchantment: Enchantment): Boolean {
		return super.isCompatibleWith(enchantment) && enchantment.id != Enchantment.Companion.ID_FORTUNE_DIGGING
	}

	override fun canEnchant(item: Item): Boolean {
		return item.maxDurability >= 0 || super.canEnchant(item)
	}

	companion object {
		fun negateDamage(item: Item, level: Int, random: Random): Boolean {
			return !(item.isArmor && random.nextFloat() < 0.6f) && random.nextInt(level + 1) > 0
		}
	}
}