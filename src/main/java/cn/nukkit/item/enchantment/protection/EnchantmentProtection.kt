package cn.nukkit.item.enchantment.protection

import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.item.enchantment.EnchantmentType

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EnchantmentProtection protected constructor(id: Int, name: String, weight: Int, protected val protectionType: TYPE) : Enchantment(id, name, weight, EnchantmentType.ARMOR) {
	enum class TYPE {
		ALL, FIRE, FALL, EXPLOSION, PROJECTILE
	}

	override fun isCompatibleWith(enchantment: Enchantment): Boolean {
		return if (enchantment is EnchantmentProtection) {
			if (enchantment.protectionType == protectionType) {
				false
			} else enchantment.protectionType == TYPE.FALL || protectionType == TYPE.FALL
		} else super.isCompatibleWith(enchantment)
	}

	override val maxLevel: Int
		get() = 4

	override fun getName(): String {
		return "%enchantment.protect." + name
	}

	open val typeModifier: Double
		get() = 0

	override val isMajor: Boolean
		get() = true

	init {
		if (protectionType == TYPE.FALL) {
			protectionType = EnchantmentType.ARMOR_FEET
		}
	}
}