package cn.nukkit.event.entity

import cn.nukkit.entity.EntityLiving
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class EntityDeathEvent @JvmOverloads constructor(entity: EntityLiving?, drops: Array<Item?> = arrayOfNulls(0)) : EntityEvent() {
	private var drops: Array<Item?>
	fun getDrops(): Array<Item?> {
		return drops
	}

	fun setDrops(drops: Array<Item?>?) {
		var drops = drops
		if (drops == null) {
			drops = arrayOfNulls(0)
		}
		this.drops = drops
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		this.drops = drops
	}
}