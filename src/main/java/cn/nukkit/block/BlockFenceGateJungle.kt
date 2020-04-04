package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/23 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockFenceGateJungle @JvmOverloads constructor(meta: Int = 0) : BlockFenceGate(meta) {
	override val id: Int
		get() = BlockID.Companion.FENCE_GATE_JUNGLE

	override val name: String
		get() = "Jungle Fence Gate"

	override fun toItem(): Item? {
		return Item.get(Item.FENCE_GATE_JUNGLE, 0, 1)
	}

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR
}