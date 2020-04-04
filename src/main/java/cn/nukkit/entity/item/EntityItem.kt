package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.ItemDespawnEvent
import cn.nukkit.event.entity.ItemSpawnEvent
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.AddItemEntityPacket
import cn.nukkit.network.protocol.DataPacket
import cn.nukkit.network.protocol.EntityEventPacket

/**
 * @author MagicDroidX
 */
class EntityItem(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {

	var owner: String? = null
	var thrower: String? = null
	var item: Item? = null
		protected set
	var pickupDelay = 0
	override val width: Float
		get() = 0.25f

	override val length: Float
		get() = 0.25f

	override val height: Float
		get() = 0.25f

	override val gravity: Float
		get() = 0.04f

	override val drag: Float
		get() = 0.02f

	protected override val baseOffset: Float
		protected get() = 0.125f

	override fun canCollide(): Boolean {
		return false
	}

	override fun initEntity() {
		super.initEntity()
		maxHealth = 5
		setHealth(namedTag!!.getShort("Health").toFloat())
		if (namedTag!!.contains("Age")) {
			age = namedTag!!.getShort("Age")
		}
		if (namedTag!!.contains("PickupDelay")) {
			pickupDelay = namedTag!!.getShort("PickupDelay")
		}
		if (namedTag!!.contains("Owner")) {
			owner = namedTag!!.getString("Owner")
		}
		if (namedTag!!.contains("Thrower")) {
			thrower = namedTag!!.getString("Thrower")
		}
		if (!namedTag!!.contains("Item")) {
			close()
			return
		}
		item = NBTIO.getItemHelper(namedTag!!.getCompound("Item"))
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_GRAVITY, true)
		server!!.pluginManager.callEvent(ItemSpawnEvent(this))
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		return (source.cause == DamageCause.VOID || source.cause == DamageCause.CONTACT || source.cause == DamageCause.FIRE_TICK ||
				(source.cause == DamageCause.ENTITY_EXPLOSION ||
						source.cause == DamageCause.BLOCK_EXPLOSION) &&
				!this.isInsideOfWater && (item == null ||
				item!!.id != Item.NETHER_STAR)) && super.attack(source)
	}

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		val tickDiff = currentTick - lastUpdate
		if (tickDiff <= 0 && !justCreated) {
			return true
		}
		lastUpdate = currentTick
		timing!!.startTiming()
		if (age % 60 == 0 && onGround && item != null && this.isAlive) {
			if (item!!.getCount() < item!!.maxStackSize) {
				for (entity in getLevel().getNearbyEntities(getBoundingBox().grow(1.0, 1.0, 1.0), this, false)) {
					if (entity is EntityItem) {
						if (!entity.isAlive()) {
							continue
						}
						val closeItem = entity.item
						if (!closeItem!!.equals(item, true, true)) {
							continue
						}
						if (!entity.isOnGround()) {
							continue
						}
						val newAmount = item!!.getCount() + closeItem.getCount()
						if (newAmount > item!!.maxStackSize) {
							continue
						}
						entity.close()
						item!!.setCount(newAmount)
						val packet = EntityEventPacket()
						packet.eid = getId()
						packet.data = newAmount
						packet.event = EntityEventPacket.MERGE_ITEMS
						broadcastPacket(getLevel().players.values, packet)
					}
				}
			}
		}
		var hasUpdate = this.entityBaseTick(tickDiff)
		if (isInsideOfFire) {
			kill()
		}
		if (this.isAlive) {
			if (pickupDelay > 0 && pickupDelay < 32767) {
				pickupDelay -= tickDiff
				if (pickupDelay < 0) {
					pickupDelay = 0
				}
			} else {
				for (entity in level.getNearbyEntities(boundingBox!!.grow(1.0, 0.5, 1.0), this)) {
					if (entity is Player) {
						if (entity.pickupEntity(this, true)) {
							return true
						}
					}
				}
			}
			if (level.getBlockIdAt(x.toInt(), boundingBox!!.maxY.toInt(), z.toInt()) == 8 || level.getBlockIdAt(x.toInt(), boundingBox!!.maxY.toInt(), z.toInt()) == 9) { //item is fully in water or in still water
				motionY -= gravity * -0.015
			} else if (this.isInsideOfWater) {
				motionY = gravity - 0.06 //item is going up in water, don't let it go back down too fast
			} else {
				motionY -= gravity.toDouble() //item is not in water
			}
			if (checkObstruction(x, y, z)) {
				hasUpdate = true
			}
			move(motionX, motionY, motionZ)
			var friction = 1 - drag.toDouble()
			if (onGround && (Math.abs(motionX) > 0.00001 || Math.abs(motionZ) > 0.00001)) {
				friction *= getLevel().getBlock(temporalVector!!.setComponents(Math.floor(x) as Int.toDouble(), Math.floor(y - 1) as Int.toDouble(), Math.floor(z).toInt() - 1.toDouble())).frictionFactor
			}
			motionX *= friction
			motionY *= 1 - drag.toDouble()
			motionZ *= friction
			if (onGround) {
				motionY *= -0.5
			}
			updateMovement()
			if (age > 6000) {
				val ev = ItemDespawnEvent(this)
				server!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					age = 0
				} else {
					kill()
					hasUpdate = true
				}
			}
		}
		timing!!.stopTiming()
		return hasUpdate || !onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001
	}

	override fun saveNBT() {
		super.saveNBT()
		if (item != null) { // Yes, a item can be null... I don't know what causes this, but it can happen.
			namedTag!!.putCompound("Item", NBTIO.putItemHelper(item, -1))
			namedTag!!.putShort("Health", getHealth().toInt())
			namedTag!!.putShort("Age", age)
			namedTag!!.putShort("PickupDelay", pickupDelay)
			if (owner != null) {
				namedTag!!.putString("Owner", owner)
			}
			if (thrower != null) {
				namedTag!!.putString("Thrower", thrower)
			}
		}
	}

	override val name: String?
		get() = if (hasCustomName()) this.nameTag else if (item!!.hasCustomName()) item!!.customName else item!!.name

	override fun canCollideWith(entity: Entity): Boolean {
		return false
	}

	public override fun createAddEntityPacket(): DataPacket {
		val addEntity = AddItemEntityPacket()
		addEntity.entityUniqueId = getId()
		addEntity.entityRuntimeId = getId()
		addEntity.x = x.toFloat()
		addEntity.y = y.toFloat()
		addEntity.z = z.toFloat()
		addEntity.speedX = motionX.toFloat()
		addEntity.speedY = motionY.toFloat()
		addEntity.speedZ = motionZ.toFloat()
		addEntity.metadata = dataProperties
		addEntity.item = item
		return addEntity
	}

	override fun doesTriggerPressurePlate(): Boolean {
		return true
	}

	companion object {
		const val networkId = 64
		const val DATA_SOURCE_ID = 17
	}
}