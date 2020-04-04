package cn.nukkit.entity.item

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityExplosive
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.level.Explosion
import cn.nukkit.level.GameRule
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by PetteriM1
 */
class EntityEndCrystal(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt), EntityExplosive {

	override fun initEntity() {
		super.initEntity()
		if (namedTag!!.contains("ShowBottom")) {
			setShowBase(namedTag!!.getBoolean("ShowBottom"))
		}
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putBoolean("ShowBottom", showBase())
	}

	override val height: Float
		get() = 0.98f

	override val width: Float
		get() = 0.98f

	override fun attack(source: EntityDamageEvent): Boolean {
		if (source.cause == DamageCause.FIRE || source.cause == DamageCause.FIRE_TICK || source.cause == DamageCause.LAVA) {
			return false
		}
		if (!super.attack(source)) {
			return false
		}
		explode()
		return true
	}

	override fun explode() {
		val pos = this.position
		val explode = Explosion(pos, 6, this)
		close()
		if (level.getGameRules().getBoolean(GameRule.MOB_GRIEFING)) {
			explode.explodeA()
		}
		explode.explodeB()
	}

	override fun canCollideWith(entity: Entity): Boolean {
		return false
	}

	fun showBase(): Boolean {
		return getDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_SHOWBASE)
	}

	fun setShowBase(value: Boolean) {
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_SHOWBASE, value)
	}

	companion object {
		const val networkId = 71
	}
}