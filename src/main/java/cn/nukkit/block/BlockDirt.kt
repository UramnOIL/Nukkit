package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * AMAZING COARSE DIRT added by kvetinac97
 * Nukkit Project
 */
open class BlockDirt @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.DIRT

	override fun canBeActivated(): Boolean {
		return true
	}

	override val resistance: Double
		get() = 2.5

	override val hardness: Double
		get() = 0.5

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override val name: String
		get() = if (this.damage == 0) "Dirt" else "Coarse Dirt"

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.isHoe) {
			item.useOn(this)
			getLevel().setBlock(this, if (this.damage == 0) Block.Companion.get(BlockID.Companion.FARMLAND) else Block.Companion.get(BlockID.Companion.DIRT), true)
			return true
		}
		return false
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(ItemBlock(Block.Companion.get(BlockID.Companion.DIRT)))
	}

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR
}