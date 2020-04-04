package cn.nukkit.item.enchantment.damage

import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.item.enchantment.EnchantmentType

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EnchantmentDamage protected constructor(id: Int, name: String, weight: Int, protected val damageType: TYPE) : Enchantment(id, name, weight, EnchantmentType.SWORD) {
	enum class TYPE {
		ALL, SMITE, ARTHROPODS
	}

	override fun isCompatibleWith(enchantment: Enchantment): Boolean {
		return enchantment !is EnchantmentDamage
	}

	override fun canEnchant(item: Item): Boolean {
		return item.isAxe || super.canEnchant(item)
	}

	override val maxLevel: Int
		get() = 5

	override fun getName(): String {
		return "%enchantment.damage." + name
	}

	override val isMajor: Boolean
		get() = true

}