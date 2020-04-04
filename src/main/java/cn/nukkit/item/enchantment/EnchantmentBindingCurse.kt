package cn.nukkit.item.enchantment

class EnchantmentBindingCurse : Enchantment(Enchantment.Companion.ID_BINDING_CURSE, "bindingCurse", 1, EnchantmentType.WEARABLE) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 25
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return 50
	}

	override val maxLevel: Int
		get() = 1
}