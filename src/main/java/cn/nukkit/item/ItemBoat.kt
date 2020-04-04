package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockWater
import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityBoat
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag

/**
 * Created by yescallop on 2016/2/13.
 */
class ItemBoat @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.BOAT, meta, count, "Boat") {
	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(level: Level, player: Player, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double): Boolean {
		if (face != BlockFace.UP) return false
		val boat = Entity.createEntity("Boat",
				level.getChunk(block.floorX shr 4, block.floorZ shr 4), CompoundTag("")
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", block.getX() + 0.5))
						.add(DoubleTag("", block.getY() - if (target is BlockWater) 0.0625 else 0))
						.add(DoubleTag("", block.getZ() + 0.5)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", 0))
						.add(DoubleTag("", 0))
						.add(DoubleTag("", 0)))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", ((player.yaw + 90f) % 360).toFloat()))
						.add(FloatTag("", 0)))
				.putByte("woodID", this.damage)
		) as EntityBoat?
				?: return false
		if (player.isSurvival) {
			val item = player.getInventory().itemInHand
			item.setCount(item.getCount() - 1)
			player.getInventory().setItemInHand(item)
		}
		boat.spawnToAll()
		return true
	}

	override val maxStackSize: Int
		get() = 1
}