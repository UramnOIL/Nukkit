package cn.nukkit.block

import cn.nukkit.entity.Entity
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 27.12.2015.
 */
class BlockSoulSand : BlockSolid() {
	override val name: String
		get() = "Soul Sand"

	override val id: Int
		get() = BlockID.Companion.SOUL_SAND

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override fun getMaxY(): Double {
		return y + 1 - 0.125
	}

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override fun onEntityCollide(entity: Entity) {
		entity.motionX *= 0.4
		entity.motionZ *= 0.4
	}

	override val color: BlockColor
		get() = BlockColor.BROWN_BLOCK_COLOR
}