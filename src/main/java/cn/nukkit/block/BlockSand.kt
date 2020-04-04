package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockSand @JvmOverloads constructor(override var damage: Int = 0) : BlockFallable() {
	override val fullId: Int
		get() = (id shl 4) + damage

	override val id: Int
		get() = BlockID.Companion.SAND

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override val name: String
		get() = if (damage == 0x01) {
			"Red Sand"
		} else "Sand"

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR

	companion object {
		const val DEFAULT = 0
		const val RED = 1
	}

}