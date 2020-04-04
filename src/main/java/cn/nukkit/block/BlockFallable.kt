package cn.nukkit.block

import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityFallingBlock
import cn.nukkit.level.Level
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag

/**
 * author: rcsuperman
 * Nukkit Project
 */
abstract class BlockFallable protected constructor() : BlockSolid() {
	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val down = this.down()
			if (down.id == BlockID.Companion.AIR || down is BlockLiquid) {
				level.setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
				val nbt = CompoundTag()
						.putList(ListTag<DoubleTag>("Pos")
								.add(DoubleTag("", x + 0.5))
								.add(DoubleTag("", y))
								.add(DoubleTag("", z + 0.5)))
						.putList(ListTag<DoubleTag>("Motion")
								.add(DoubleTag("", 0))
								.add(DoubleTag("", 0))
								.add(DoubleTag("", 0)))
						.putList(ListTag<FloatTag>("Rotation")
								.add(FloatTag("", 0))
								.add(FloatTag("", 0)))
						.putInt("TileID", this.id)
						.putByte("Data", this.damage)
				val fall = Entity.createEntity("FallingSand", getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as EntityFallingBlock
				fall?.spawnToAll()
			}
		}
		return type
	}
}