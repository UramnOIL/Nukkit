package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockRail
import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityMinecartTNT
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.*
import cn.nukkit.utils.Rail

class ItemMinecartTNT @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.MINECART_WITH_TNT, meta, count, "Minecart with TNT") {
	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(level: Level, player: Player, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double): Boolean {
		if (Rail.isRailBlock(target)) {
			val type = (target as BlockRail).orientation
			var adjacent = 0.0
			if (type.isAscending) {
				adjacent = 0.5
			}
			val minecart = Entity.createEntity("MinecartTnt",
					level.getChunk(target.getFloorX() shr 4, target.getFloorZ() shr 4), CompoundTag("")
					.putList(ListTag<Tag>("Pos")
							.add(DoubleTag("", target.getX() + 0.5))
							.add(DoubleTag("", target.getY() + 0.0625 + adjacent))
							.add(DoubleTag("", target.getZ() + 0.5)))
					.putList(ListTag<Tag>("Motion")
							.add(DoubleTag("", 0))
							.add(DoubleTag("", 0))
							.add(DoubleTag("", 0)))
					.putList(ListTag<Tag>("Rotation")
							.add(FloatTag("", 0))
							.add(FloatTag("", 0)))
			) as EntityMinecartTNT?
					?: return false
			if (player.isSurvival) {
				val item = player.getInventory().itemInHand
				item.setCount(item.getCount() - 1)
				player.getInventory().setItemInHand(item)
			}
			minecart.spawnToAll()
			return true
		}
		return false
	}

	override val maxStackSize: Int
		get() = 1
}