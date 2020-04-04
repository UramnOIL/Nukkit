package cn.nukkit.block

import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.item.ItemString
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockCobweb @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(0) {
	override val name: String
		get() = "Cobweb"

	override val id: Int
		get() = BlockID.Companion.COBWEB

	override val hardness: Double
		get() = 4

	override val resistance: Double
		get() = 20

	override val toolType: Int
		get() = ItemTool.TYPE_SWORD

	override fun onEntityCollide(entity: Entity) {
		entity.resetFallDistance()
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isShears || item.isSword) {
			arrayOf(
					ItemString()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.CLOTH_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}