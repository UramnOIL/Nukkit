package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/1 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockWaterLily @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(0) {
	override val name: String
		get() = "Lily Pad"

	override val id: Int
		get() = BlockID.Companion.WATER_LILY

	override fun getMinX(): Double {
		return x + 0.0625
	}

	override fun getMinZ(): Double {
		return z + 0.0625
	}

	override fun getMaxX(): Double {
		return x + 0.9375
	}

	override fun getMaxY(): Double {
		return y + 0.015625
	}

	override fun getMaxZ(): Double {
		return z + 0.9375
	}

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return this
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (target is BlockWater) {
			val up = target.up()
			if (up.id == BlockID.Companion.AIR) {
				getLevel().setBlock(up, this, true, true)
				return true
			}
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down() !is BlockWater) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR

	override fun canPassThrough(): Boolean {
		return false
	}

	override val fullId: Int
		get() = id shl 4

	override var damage: Int
		get() = super.damage
		set(meta) {}
}