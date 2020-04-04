package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityCreature
import cn.nukkit.entity.EntityHuman
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Position

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntitySpawnEvent(entity: Entity) : EntityEvent() {
	val type: Int
	val position: Position
		get() = entity!!.position

	val isCreature: Boolean
		get() = entity is EntityCreature

	val isHuman: Boolean
		get() = entity is EntityHuman

	val isProjectile: Boolean
		get() = entity is EntityProjectile

	val isVehicle: Boolean
		get() = entity is Entity

	val isItem: Boolean
		get() = entity is EntityItem

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		type = entity.networkId
	}
}