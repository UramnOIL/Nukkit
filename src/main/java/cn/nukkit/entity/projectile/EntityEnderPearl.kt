package cn.nukkit.entity.projectile

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause
import cn.nukkit.level.Sound
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag

class EntityEnderPearl @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null) : EntityProjectile(chunk, nbt, shootingEntity) {

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
		timing!!.startTiming()
		var hasUpdate = super.onUpdate(currentTick)
		if (isCollided && shootingEntity is Player) {
			teleport()
		}
		if (age > 1200 || isCollided) {
			kill()
			hasUpdate = true
		}
		timing!!.stopTiming()
		return hasUpdate
	}

	override fun onCollideWithEntity(entity: Entity) {
		if (shootingEntity is Player) {
			teleport()
		}
		super.onCollideWithEntity(entity)
	}

	private fun teleport() {
		shootingEntity!!.teleport(Vector3(NukkitMath.floorDouble(x) + 0.5, y, NukkitMath.floorDouble(z) + 0.5), TeleportCause.ENDER_PEARL)
		if ((shootingEntity as Player).gamemode and 0x01 == 0) {
			shootingEntity!!.attack(EntityDamageByEntityEvent(this, shootingEntity, DamageCause.PROJECTILE, 5f, 0f))
		}
		level.addSound(this, Sound.MOB_ENDERMEN_PORTAL)
	}

	companion object {
		const val networkId = 87
	}
}