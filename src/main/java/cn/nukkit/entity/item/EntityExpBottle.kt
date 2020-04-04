package cn.nukkit.entity.item

import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.EnchantParticle
import cn.nukkit.level.particle.Particle
import cn.nukkit.level.particle.SpellParticle
import cn.nukkit.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom

/**
 * @author xtypr
 */
class EntityExpBottle @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null) : EntityProjectile(chunk, nbt, shootingEntity) {

	override val width: Float
		get() = 0.25f

	override val length: Float
		get() = 0.25f

	override val height: Float
		get() = 0.25f

	protected override val gravity: Float
		protected get() = 0.1f

	protected override val drag: Float
		protected get() = 0.01f

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		timing!!.startTiming()
		var hasUpdate = super.onUpdate(currentTick)
		if (age > 1200) {
			kill()
			hasUpdate = true
		}
		if (isCollided) {
			kill()
			dropXp()
			hasUpdate = true
		}
		timing!!.stopTiming()
		return hasUpdate
	}

	override fun onCollideWithEntity(entity: Entity) {
		kill()
		dropXp()
	}

	fun dropXp() {
		val particle1: Particle = EnchantParticle(this)
		getLevel().addParticle(particle1)
		val particle2: Particle = SpellParticle(this, 0x00385dc6)
		getLevel().addParticle(particle2)
		getLevel().dropExpOrb(this, ThreadLocalRandom.current().nextInt(3, 12))
	}

	companion object {
		const val networkId = 68
	}
}