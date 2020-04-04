package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BlockWater @JvmOverloads constructor(meta: Int = 0) : BlockLiquid(meta) {
	override val id: Int
		get() = BlockID.Companion.WATER

	override val name: String
		get() = "Water"

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val ret = getLevel().setBlock(this, this, true, false)
		getLevel().scheduleUpdate(this, tickRate())
		return ret
	}

	override val color: BlockColor
		get() = BlockColor.WATER_BLOCK_COLOR

	override fun getBlock(meta: Int): BlockLiquid {
		return Block.Companion.get(BlockID.Companion.WATER, meta) as BlockLiquid
	}

	override fun onEntityCollide(entity: Entity) {
		super.onEntityCollide(entity)
		if (entity.fireTicks > 0) {
			entity.extinguish()
		}
	}

	override fun tickRate(): Int {
		return 5
	}
}