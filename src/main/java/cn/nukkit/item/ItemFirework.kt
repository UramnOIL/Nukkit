package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityFirework
import cn.nukkit.item.ItemFirework.FireworkExplosion.ExplosionType
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.DyeColor
import java.util.*

/**
 * @author CreeperFace
 */
class ItemFirework @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.FIREWORKS, meta, count, "Fireworks") {
	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(level: Level, player: Player, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double): Boolean {
		if (block.canPassThrough()) {
			spawnFirework(level, block)
			if (!player.isCreative) {
				player.getInventory().decreaseCount(player.getInventory().heldItemIndex)
			}
			return true
		}
		return false
	}

	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		if (player.getInventory().chestplate is ItemElytra && player.isGliding) {
			spawnFirework(player.level, player)
			player.setMotion(Vector3(
					-Math.sin(Math.toRadians(player.yaw)) * Math.cos(Math.toRadians(player.pitch)) * 2,
					-Math.sin(Math.toRadians(player.pitch)) * 2,
					Math.cos(Math.toRadians(player.yaw)) * Math.cos(Math.toRadians(player.pitch)) * 2))
			if (!player.isCreative) {
				count--
			}
			return true
		}
		return false
	}

	fun addExplosion(explosion: FireworkExplosion) {
		val colors = explosion.getColors()
		val fades = explosion.getFades()
		if (colors.isEmpty()) {
			return
		}
		val clrs = ByteArray(colors.size)
		for (i in clrs.indices) {
			clrs[i] = colors[i].dyeData.toByte()
		}
		val fds = ByteArray(fades.size)
		for (i in fds.indices) {
			fds[i] = fades[i].dyeData.toByte()
		}
		val explosions = this.namedTag.getCompound("Fireworks").getList("Explosions", CompoundTag::class.java)
		val tag = CompoundTag()
				.putByteArray("FireworkColor", clrs)
				.putByteArray("FireworkFade", fds)
				.putBoolean("FireworkFlicker", explosion.flicker)
				.putBoolean("FireworkTrail", explosion.trail)
				.putByte("FireworkType", explosion.type.ordinal)
		explosions.add(tag)
	}

	fun clearExplosions() {
		this.namedTag.getCompound("Fireworks").putList(ListTag<CompoundTag>("Explosions"))
	}

	private fun spawnFirework(level: Level, pos: Vector3) {
		val nbt = CompoundTag()
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", pos.x + 0.5))
						.add(DoubleTag("", pos.y + 0.5))
						.add(DoubleTag("", pos.z + 0.5)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", 0))
						.add(DoubleTag("", 0))
						.add(DoubleTag("", 0)))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", 0))
						.add(FloatTag("", 0)))
				.putCompound("FireworkItem", NBTIO.putItemHelper(this))
		val entity = Entity.createEntity("Firework", level.getChunk(pos.floorX shr 4, pos.floorZ shr 4), nbt) as EntityFirework?
		entity?.spawnToAll()
	}

	class FireworkExplosion {
		private val colors: MutableList<DyeColor> = ArrayList()
		private val fades: MutableList<DyeColor> = ArrayList()
		var flicker = false
		var trail = false
		var type = ExplosionType.CREEPER_SHAPED
			private set

		fun getColors(): List<DyeColor> {
			return colors
		}

		fun getFades(): List<DyeColor> {
			return fades
		}

		fun hasFlicker(): Boolean {
			return flicker
		}

		fun hasTrail(): Boolean {
			return trail
		}

		fun setFlicker(flicker: Boolean): FireworkExplosion {
			this.flicker = flicker
			return this
		}

		fun setTrail(trail: Boolean): FireworkExplosion {
			this.trail = trail
			return this
		}

		fun type(type: ExplosionType): FireworkExplosion {
			this.type = type
			return this
		}

		fun addColor(color: DyeColor): FireworkExplosion {
			colors.add(color)
			return this
		}

		fun addFade(fade: DyeColor): FireworkExplosion {
			fades.add(fade)
			return this
		}

		enum class ExplosionType {
			SMALL_BALL, LARGE_BALL, STAR_SHAPED, CREEPER_SHAPED, BURST
		}
	}

	init {
		if (!hasCompoundTag() || !this.namedTag.contains("Fireworks")) {
			var tag = namedTag
			if (tag == null) {
				tag = CompoundTag()
				val ex = CompoundTag()
						.putByteArray("FireworkColor", byteArrayOf(DyeColor.BLACK.dyeData.toByte()))
						.putByteArray("FireworkFade", byteArrayOf())
						.putBoolean("FireworkFlicker", false)
						.putBoolean("FireworkTrail", false)
						.putByte("FireworkType", ExplosionType.CREEPER_SHAPED.ordinal)
				tag.putCompound("Fireworks", CompoundTag("Fireworks")
						.putList(ListTag<CompoundTag>("Explosions").add(ex))
						.putByte("Flight", 1)
				)
				this.namedTag = tag
			}
		}
	}
}