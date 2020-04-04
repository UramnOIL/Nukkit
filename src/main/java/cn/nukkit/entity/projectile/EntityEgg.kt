package cn.nukkit.entity.projectile

import cn.nukkit.entity.Entity
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityEgg @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null) : EntityProjectile(chunk, nbt, shootingEntity) {

	override val width: Float
		get() = 0.25f

	override val length: Float
		get() = 0.25f

	override val height: Float
		get() = 0.25f

	protected override val gravity: Float
		protected get() = 0.03f

	protected override val drag: Float
		protected get() = 0.01f

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		var hasUpdate = super.onUpdate(currentTick)
		if (age > 1200 || isCollided) {
			kill()
			hasUpdate = true
		}
		return hasUpdate
	}

	companion object {
		const val networkId = 82
	}
}