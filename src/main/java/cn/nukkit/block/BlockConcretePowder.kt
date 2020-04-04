package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace

/**
 * Created by CreeperFace on 2.6.2017.
 */
class BlockConcretePowder @JvmOverloads constructor(override var damage: Int = 0) : BlockFallable() {
	override val fullId: Int
		get() = (id shl 4) + damage

	override val id: Int
		get() = BlockID.Companion.CONCRETE_POWDER

	override val name: String
		get() = "Concrete Powder"

	override val resistance: Double
		get() = 2.5

	override val hardness: Double
		get() = 0.5

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			super.onUpdate(Level.BLOCK_UPDATE_NORMAL)
			for (side in 1..5) {
				val block = this.getSide(BlockFace.fromIndex(side))
				if (block.id == BlockID.Companion.WATER || block.id == BlockID.Companion.STILL_WATER || block.id == BlockID.Companion.LAVA || block.id == BlockID.Companion.STILL_LAVA) {
					level.setBlock(this, Block.Companion.get(BlockID.Companion.CONCRETE, damage), true, true)
				}
			}
			return Level.BLOCK_UPDATE_NORMAL
		}
		return 0
	}

	override fun place(item: Item, b: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		var concrete = false
		for (side in 1..5) {
			val block = this.getSide(BlockFace.fromIndex(side))
			if (block.id == BlockID.Companion.WATER || block.id == BlockID.Companion.STILL_WATER || block.id == BlockID.Companion.LAVA || block.id == BlockID.Companion.STILL_LAVA) {
				concrete = true
				break
			}
		}
		if (concrete) {
			level.setBlock(this, Block.Companion.get(BlockID.Companion.CONCRETE, damage), true, true)
		} else {
			level.setBlock(this, this, true, true)
		}
		return true
	}

}