package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * Created on 2015/11/24 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockCarpet @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	constructor(dyeColor: DyeColor) : this(dyeColor.woolData) {}

	override val id: Int
		get() = BlockID.Companion.CARPET

	override val hardness: Double
		get() = 0.1

	override val resistance: Double
		get() = 0.5

	override val isSolid: Boolean
		get() = true

	override val name: String
		get() = DyeColor.getByWoolData(damage).toString() + " Carpet"

	override fun canPassThrough(): Boolean {
		return false
	}

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return this
	}

	override fun getMaxY(): Double {
		return y + 0.0625
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down.id != Item.AIR) {
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().id == Item.AIR) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override val color: BlockColor
		get() = DyeColor.getByWoolData(damage).color

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(damage)
}