package cn.nukkit.entity.mob

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.ByteEntityData
import cn.nukkit.entity.weather.EntityLightningStrike
import cn.nukkit.event.entity.CreeperPowerEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom

/**
 * @author Box.
 */
class EntityCreeper(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override val width: Float
		get() = 0.6f

	override val height: Float
		get() = 1.7f

	val isPowered: Boolean
		get() = getDataPropertyBoolean(DATA_POWERED)

	fun setPowered(bolt: EntityLightningStrike?) {
		val ev = CreeperPowerEvent(this, bolt, CreeperPowerEvent.PowerCause.LIGHTNING)
		getServer().pluginManager.callEvent(ev)
		if (!ev.isCancelled) {
			this.setDataProperty(ByteEntityData(DATA_POWERED, 1))
			namedTag!!.putBoolean("powered", true)
		}
	}

	fun setPowered(powered: Boolean) {
		val ev = CreeperPowerEvent(this, if (powered) CreeperPowerEvent.PowerCause.SET_ON else CreeperPowerEvent.PowerCause.SET_OFF)
		getServer().pluginManager.callEvent(ev)
		if (!ev.isCancelled) {
			this.setDataProperty(ByteEntityData(DATA_POWERED, if (powered) 1 else 0))
			namedTag!!.putBoolean("powered", powered)
		}
	}

	override fun onStruckByLightning(entity: Entity?) {
		this.setPowered(true)
	}

	override fun initEntity() {
		super.initEntity()
		if (namedTag!!.getBoolean("powered") || namedTag!!.getBoolean("IsPowered")) {
			dataProperties!!.putBoolean(DATA_POWERED, true)
		}
		maxHealth = 20
	}

	override val name: String?
		get() = "Creeper"

	override val drops: Array<Item?>
		get() = if (lastDamageCause is EntityDamageByEntityEvent) {
			arrayOf(Item.get(Item.GUNPOWDER, ThreadLocalRandom.current().nextInt(2) + 1))
		} else arrayOfNulls(0)

	companion object {
		const val networkId = 33
		const val DATA_SWELL_DIRECTION = 16
		const val DATA_SWELL = 17
		const val DATA_SWELL_OLD = 18
		const val DATA_POWERED = 19
	}
}