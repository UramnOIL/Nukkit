package cn.nukkit.item

import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created on 2015/12/27 by xtypr.
 * Package cn.nukkit.item in project Nukkit .
 */
class ItemPotionSplash @JvmOverloads constructor(meta: Int?, count: Int = 1) : ProjectileItem(ItemID.Companion.SPLASH_POTION, meta, count, "Splash Potion") {
	override val maxStackSize: Int
		get() = 1

	override fun canBeActivated(): Boolean {
		return true
	}

	override val projectileEntityType: String
		get() = "ThrownPotion"

	override val throwForce: Float
		get() = 0.5f

	override fun correctNBT(nbt: CompoundTag) {
		nbt.putInt("PotionId", meta)
	}
}