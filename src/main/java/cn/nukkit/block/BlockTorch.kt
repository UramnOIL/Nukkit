package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockTorch @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta), Faceable {
	override val name: String
		get() = "Torch"

	override val id: Int
		get() = BlockID.Companion.TORCH

	override val lightLevel: Int
		get() = 14

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val below = this.down()
			val side = this.damage
			val faces = intArrayOf(
					0,  //0
					4,  //1
					5,  //2
					2,  //3
					3,  //4
					0,  //5
					0 //6
			)
			if (this.getSide(BlockFace.fromIndex(faces[side])).isTransparent && !(side == 0 && (below is BlockFence || below.id == BlockID.Companion.COBBLE_WALL))) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val below = this.down()
		if (!target.isTransparent && face != BlockFace.DOWN) {
			val faces = intArrayOf(
					0,  //0, nerver used
					5,  //1
					4,  //2
					3,  //3
					2,  //4
					1)
			this.setDamage(faces[face.index])
			getLevel().setBlock(block, this, true, true)
			return true
		} else if (!below!!.isTransparent || below is BlockFence || below.id == BlockID.Companion.COBBLE_WALL) {
			this.setDamage(0)
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	override fun getBlockFace(): BlockFace {
		return getBlockFace(this.damage and 0x07)
	}

	fun getBlockFace(meta: Int): BlockFace {
		return when (meta) {
			1 -> BlockFace.EAST
			2 -> BlockFace.WEST
			3 -> BlockFace.SOUTH
			4 -> BlockFace.NORTH
			else -> BlockFace.UP
		}
	}
}