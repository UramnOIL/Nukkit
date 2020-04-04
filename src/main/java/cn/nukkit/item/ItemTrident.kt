package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.entity.projectile.EntityThrownTrident
import cn.nukkit.event.entity.EntityShootBowEvent
import cn.nukkit.event.entity.ProjectileLaunchEvent
import cn.nukkit.item.ItemTool
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * Created by PetteriM1
 */
class ItemTrident @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.TRIDENT, meta, count, "Trident") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_TRIDENT

	override val isSword: Boolean
		get() = true

	override val attackDamage: Int
		get() = 9

	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		return true
	}

	override fun onRelease(player: Player, ticksUsed: Int): Boolean {
		this.useOn(player)
		val nbt = CompoundTag()
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", player.x))
						.add(DoubleTag("", player.y + player.eyeHeight))
						.add(DoubleTag("", player.z)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", -Math.sin(player.yaw / 180 * Math.PI) * Math.cos(player.pitch / 180 * Math.PI)))
						.add(DoubleTag("", -Math.sin(player.pitch / 180 * Math.PI)))
						.add(DoubleTag("", Math.cos(player.yaw / 180 * Math.PI) * Math.cos(player.pitch / 180 * Math.PI))))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", (if (player.yaw > 180) 360 else 0) - player.yaw.toFloat()))
						.add(FloatTag("", (-player.pitch).toFloat())))
		val p = ticksUsed.toDouble() / 20
		val f = Math.min((p * p + p * 2) / 3, 1.0) * 2
		val trident = Entity.createEntity("ThrownTrident", player.chunk, nbt, player, f == 2.0) as EntityThrownTrident?
				?: return false
		trident.item = this
		val entityShootBowEvent = EntityShootBowEvent(player, this, trident, f)
		if (f < 0.1 || ticksUsed < 5) {
			entityShootBowEvent.setCancelled()
		}
		Server.instance!!.pluginManager.callEvent(entityShootBowEvent)
		if (entityShootBowEvent.isCancelled) {
			entityShootBowEvent.getProjectile().kill()
		} else {
			entityShootBowEvent.getProjectile().setMotion(entityShootBowEvent.getProjectile().motion.multiply(entityShootBowEvent.force))
			if (entityShootBowEvent.getProjectile() is EntityProjectile) {
				val ev = ProjectileLaunchEvent(entityShootBowEvent.getProjectile())
				Server.instance!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					entityShootBowEvent.getProjectile().kill()
				} else {
					entityShootBowEvent.getProjectile().spawnToAll()
					player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ITEM_TRIDENT_THROW)
					if (!player.isCreative) {
						count--
						player.getInventory().setItemInHand(this)
					}
				}
			}
		}
		return true
	}
}