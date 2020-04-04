package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockSlab(meta: Int, protected val doubleSlab: Int) : BlockTransparentMeta(meta) {
	override fun getMinY(): Double {
		return if (this.damage and 0x08 > 0) y + 0.5 else y
	}

	override fun getMaxY(): Double {
		return if (this.damage and 0x08 > 0) y + 1 else y + 0.5
	}

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = (if (toolType < ItemTool.TYPE_AXE) 30 else 15).toDouble()

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		this.setDamage(this.damage and 0x07)
		if (face == BlockFace.DOWN) {
			if (target is BlockSlab && target.getDamage() and 0x08 == 0x08 && target.getDamage() and 0x07 == this.damage and 0x07) {
				getLevel().setBlock(target, Block.Companion.get(doubleSlab, this.damage), true)
				return true
			} else if (block is BlockSlab && block.getDamage() and 0x07 == this.damage and 0x07) {
				getLevel().setBlock(block, Block.Companion.get(doubleSlab, this.damage), true)
				return true
			} else {
				this.setDamage(this.damage or 0x08)
			}
		} else if (face == BlockFace.UP) {
			if (target is BlockSlab && target.getDamage() and 0x08 == 0 && target.getDamage() and 0x07 == this.damage and 0x07) {
				getLevel().setBlock(target, Block.Companion.get(doubleSlab, this.damage), true)
				return true
			} else if (block is BlockSlab && block.getDamage() and 0x07 == this.damage and 0x07) {
				getLevel().setBlock(block, Block.Companion.get(doubleSlab, this.damage), true)
				return true
			}
			//TODO: check for collision
		} else {
			if (block is BlockSlab) {
				if (block.getDamage() and 0x07 == this.damage and 0x07) {
					getLevel().setBlock(block, Block.Companion.get(doubleSlab, this.damage), true)
					return true
				}
				return false
			} else {
				if (fy > 0.5) {
					this.setDamage(this.damage or 0x08)
				}
			}
		}
		if (block is BlockSlab && target.damage and 0x07 != this.damage and 0x07) {
			return false
		}
		getLevel().setBlock(block, this, true, true)
		return true
	}

}