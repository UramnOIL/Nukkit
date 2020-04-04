package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/23 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockFenceGateBirch @JvmOverloads constructor(meta: Int = 0) : BlockFenceGate(meta) {
	override val id: Int
		get() = BlockID.Companion.FENCE_GATE_BIRCH

	override val name: String
		get() = "Birch Fence Gate"

	override fun toItem(): Item? {
		return Item.get(Item.FENCE_GATE_BIRCH, 0, 1)
	}

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR
}