package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/22 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockGrassPath : BlockGrass() {
	override val id: Int
		get() = BlockID.Companion.GRASS_PATH

	override val name: String
		get() = "Grass Path"

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override fun getMaxY(): Double {
		return y + 0.9375
	}

	override val resistance: Double
		get() = 3.25

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}

	override fun onUpdate(type: Int): Int {
		return 0
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.isHoe) {
			item.useOn(this)
			getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.FARMLAND), true)
			return true
		}
		return false
	}
}