package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.Faceable

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockStairs protected constructor(meta: Int) : BlockTransparentMeta(meta), Faceable {
	override fun getMinY(): Double {
		// TODO: this seems wrong
		return y + if (damage and 0x04 > 0) 0.5 else 0
	}

	override fun getMaxY(): Double {
		// TODO: this seems wrong
		return y + if (damage and 0x04 > 0) 1 else 0.5
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val faces = intArrayOf(2, 1, 3, 0)
		this.setDamage(faces[player?.direction?.horizontalIndex ?: 0])
		if (fy > 0.5 && face != BlockFace.UP || face == BlockFace.DOWN) {
			this.setDamage(this.damage or 0x04) //Upside-down stairs
		}
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun toItem(): Item? {
		val item = super.toItem()
		item!!.damage = 0
		return item
	}

	override fun collidesWithBB(bb: AxisAlignedBB): Boolean {
		val damage = this.damage
		val side = damage and 0x03
		var f = 0.0
		var f1 = 0.5
		var f2 = 0.5
		var f3 = 1.0
		if (damage and 0x04 > 0) {
			f = 0.5
			f1 = 1.0
			f2 = 0.0
			f3 = 0.5
		}
		if (bb.intersectsWith(SimpleAxisAlignedBB(
						x,
						y + f,
						z,
						x + 1,
						y + f1,
						z + 1
				))) {
			return true
		}
		if (side == 0) {
			if (bb.intersectsWith(SimpleAxisAlignedBB(
							x + 0.5,
							y + f2,
							z,
							x + 1,
							y + f3,
							z + 1
					))) {
				return true
			}
		} else if (side == 1) {
			if (bb.intersectsWith(SimpleAxisAlignedBB(
							x,
							y + f2,
							z,
							x + 0.5,
							y + f3,
							z + 1
					))) {
				return true
			}
		} else if (side == 2) {
			if (bb.intersectsWith(SimpleAxisAlignedBB(
							x,
							y + f2,
							z + 0.5,
							x + 1,
							y + f3,
							z + 1
					))) {
				return true
			}
		} else if (side == 3) {
			if (bb.intersectsWith(SimpleAxisAlignedBB(
							x,
							y + f2,
							z,
							x + 1,
							y + f3,
							z + 0.5
					))) {
				return true
			}
		}
		return false
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}
}