package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityPainting
import cn.nukkit.entity.item.EntityPainting.Motive
import cn.nukkit.level.Level
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemPainting @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.PAINTING, 0, count, "Painting") {
	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(level: Level, player: Player, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double): Boolean {
		val chunk: FullChunk? = level.getChunk(block.getX().toInt() shr 4, block.getZ().toInt() shr 4)
		if (chunk == null || target.isTransparent || face.horizontalIndex == -1 || block.isSolid) {
			return false
		}
		val validMotives: MutableList<Motive> = ArrayList()
		for (motive in EntityPainting.motives) {
			var valid = true
			var x = 0
			while (x < motive.width && valid) {
				var z = 0
				while (z < motive.height && valid) {
					if (target.getSide(BlockFace.fromIndex(RIGHT[face.index - 2]), x).isTransparent ||
							target.up(z).isTransparent ||
							block.getSide(BlockFace.fromIndex(RIGHT[face.index - 2]), x).isSolid ||
							block.up(z).isSolid) {
						valid = false
					}
					z++
				}
				x++
			}
			if (valid) {
				validMotives.add(motive)
			}
		}
		val direction = DIRECTION[face.index - 2]
		val motive = validMotives[ThreadLocalRandom.current().nextInt(validMotives.size)]
		val position = Vector3(target.x + 0.5, target.y + 0.5, target.z + 0.5)
		val widthOffset = offset(motive.width)
		when (face.horizontalIndex) {
			0 -> {
				position.x += widthOffset
				position.z += OFFSET
			}
			1 -> {
				position.x -= OFFSET
				position.z += widthOffset
			}
			2 -> {
				position.x -= widthOffset
				position.z -= OFFSET
			}
			3 -> {
				position.x += OFFSET
				position.z -= widthOffset
			}
		}
		position.y += offset(motive.height)
		val nbt = CompoundTag()
				.putByte("Direction", direction)
				.putString("Motive", motive.title)
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("0", position.x))
						.add(DoubleTag("1", position.y))
						.add(DoubleTag("2", position.z)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("0", 0))
						.add(DoubleTag("1", 0))
						.add(DoubleTag("2", 0)))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("0", (direction * 90).toFloat()))
						.add(FloatTag("1", 0)))
		val entity = Entity.createEntity("Painting", chunk, nbt) as EntityPainting? ?: return false
		if (player.isSurvival) {
			val item = player.getInventory().itemInHand
			item.setCount(item.getCount() - 1)
			player.getInventory().setItemInHand(item)
		}
		entity.spawnToAll()
		return true
	}

	companion object {
		private val DIRECTION = intArrayOf(2, 3, 4, 5)
		private val RIGHT = intArrayOf(4, 5, 3, 2)
		private const val OFFSET = 0.53125
		private fun offset(value: Int): Double {
			return if (value > 1) 0.5 else 0
		}
	}
}