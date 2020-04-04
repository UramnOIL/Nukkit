package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityEnderPearl
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.ProjectileLaunchEvent
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * @author CreeperFace
 */
abstract class ProjectileItem(id: Int, meta: Int?, count: Int, name: String) : Item(id, meta, count, name) {
	abstract val projectileEntityType: String
	abstract val throwForce: Float
	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		val nbt = CompoundTag()
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", player.x))
						.add(DoubleTag("", player.y + player.eyeHeight - 0.30000000149011612))
						.add(DoubleTag("", player.z)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", directionVector.x))
						.add(DoubleTag("", directionVector.y))
						.add(DoubleTag("", directionVector.z)))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", player.yaw.toFloat()))
						.add(FloatTag("", player.pitch.toFloat())))
		correctNBT(nbt)
		val projectile = Entity.createEntity(projectileEntityType, player.level.getChunk(player.floorX shr 4, player.floorZ shr 4), nbt, player)
		if (projectile != null) {
			if (projectile is EntityEnderPearl) {
				if (player.getServer().tick - player.lastEnderPearlThrowingTick < 20) {
					projectile.kill()
					return false
				}
			}
			projectile.setMotion(projectile.motion.multiply(throwForce.toDouble()))
			if (projectile is EntityProjectile) {
				val ev = ProjectileLaunchEvent(projectile as EntityProjectile?)
				player.getServer().pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					projectile.kill()
				} else {
					if (!player.isCreative) {
						count--
					}
					if (projectile is EntityEnderPearl) {
						player.onThrowEnderPearl()
					}
					projectile.spawnToAll()
					player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BOW)
				}
			}
		} else {
			return false
		}
		return true
	}

	protected open fun correctNBT(nbt: CompoundTag) {}
}