package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.Faceable

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * Created by Leonidius20 on 18.08.18.
 */
class BlockObserver @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	override val name: String
		get() = "Observer"

	override val id: Int
		get() = BlockID.Companion.OBSERVER

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (player != null) {
			if (Math.abs(player.floorX - x) <= 1 && Math.abs(player.floorZ - z) <= 1) {
				val y = player.y + player.eyeHeight
				if (y - this.y > 2) {
					this.setDamage(BlockFace.DOWN.index)
				} else if (this.y - y > 0) {
					this.setDamage(BlockFace.UP.index)
				} else {
					this.setDamage(player.horizontalFacing.index)
				}
			} else {
				this.setDamage(player.horizontalFacing.index)
			}
		} else {
			this.setDamage(0)
		}
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 3.5

	override val resistance: Double
		get() = 17.5

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}