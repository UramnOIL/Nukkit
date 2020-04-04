package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityLiving
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

/**
 * author: Box
 * Nukkit Project
 */
class EntityShootBowEvent(shooter: EntityLiving?, bow: Item, projectile: EntityProjectile, force: Double) : EntityEvent(), Cancellable {
	val bow: Item
	private var projectile: EntityProjectile
	var force: Double
	override var entity: Entity?
		get() = field as EntityLiving?
		set(entity) {
			super.entity = entity
		}

	fun getProjectile(): EntityProjectile {
		return projectile
	}

	fun setProjectile(projectile: Entity) {
		if (projectile !== this.projectile) {
			if (this.projectile.viewers.size == 0) {
				this.projectile.kill()
				this.projectile.close()
			}
			this.projectile = projectile as EntityProjectile
		}
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = shooter
		this.bow = bow
		this.projectile = projectile
		this.force = force
	}
}