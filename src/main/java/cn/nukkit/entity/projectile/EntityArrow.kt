package cn.nukkit.entity.projectile

import cn.nukkit.entity.Entity
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityArrow @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null, critical: Boolean = false) : EntityProjectile(chunk, nbt, shootingEntity) {
	var pickupMode = 0

	override val width: Float
		get() = 0.5f

	override val length: Float
		get() = 0.5f

	override val height: Float
		get() = 0.5f

	public override fun getGravity(): Float {
		return 0.05f
	}

	public override fun getDrag(): Float {
		return 0.01f
	}

	protected override var gravity = 0.05f
	protected override var drag = 0.01f
	override fun initEntity() {
		super.initEntity()
		damage = if (namedTag!!.contains("damage")) namedTag!!.getDouble("damage") else 2
		pickupMode = if (namedTag!!.contains("pickup")) namedTag!!.getByte("pickup") else PICKUP_ANY
	}

	fun setCritical() {
		this.critical = true
	}

	var isCritical: Boolean
		get() = getDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_CRITICAL)
		set(value) {
			this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_CRITICAL, value)
		}

	override val resultDamage: Int
		get() {
			var base = super.getResultDamage()
			if (isCritical) {
				base += ThreadLocalRandom.current().nextInt(base / 2 + 2)
			}
			return base
		}

	protected override val baseDamage: Double
		protected get() = 2

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		timing!!.startTiming()
		var hasUpdate = super.onUpdate(currentTick)
		if (onGround || hadCollision) {
			this.critical = false
		}
		if (age > 1200) {
			close()
			hasUpdate = true
		}
		timing!!.stopTiming()
		return hasUpdate
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putByte("pickup", pickupMode)
	}

	companion object {
		const val networkId = 80
		const val DATA_SOURCE_ID = 17
		const val PICKUP_NONE = 0
		const val PICKUP_ANY = 1
		const val PICKUP_CREATIVE = 2
	}

	init {
		critical = critical
	}
}