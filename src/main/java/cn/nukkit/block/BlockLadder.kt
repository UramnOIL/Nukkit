package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created on 2015/12/8 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockLadder @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val name: String
		get() = "Ladder"

	override val id: Int
		get() = BlockID.Companion.LADDER

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override fun canBeClimbed(): Boolean {
		return true
	}

	override val isSolid: Boolean
		get() = false

	override val hardness: Double
		get() = 0.4

	override val resistance: Double
		get() = 2

	private var offMinX = 0.0
	private var offMinZ = 0.0
	private var offMaxX = 0.0
	private var offMaxZ = 0.0
	private fun calculateOffsets() {
		val f = 0.1875
		when (getDamage()) {
			2 -> {
				offMinX = 0.0
				offMinZ = 1 - f
				offMaxX = 1.0
				offMaxZ = 1.0
			}
			3 -> {
				offMinX = 0.0
				offMinZ = 0.0
				offMaxX = 1.0
				offMaxZ = f
			}
			4 -> {
				offMinX = 1 - f
				offMinZ = 0.0
				offMaxX = 1.0
				offMaxZ = 1.0
			}
			5 -> {
				offMinX = 0.0
				offMinZ = 0.0
				offMaxX = f
				offMaxZ = 1.0
			}
			else -> {
				offMinX = 0.0
				offMinZ = 1.0
				offMaxX = 1.0
				offMaxZ = 1.0
			}
		}
	}

	override var damage: Int
		get() = super.damage
		set(meta) {
			super.setDamage(meta)
			calculateOffsets()
		}

	override fun getMinX(): Double {
		return x + offMinX
	}

	override fun getMinZ(): Double {
		return z + offMinZ
	}

	override fun getMaxX(): Double {
		return x + offMaxX
	}

	override fun getMaxZ(): Double {
		return z + offMaxZ
	}

	override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
		return super.recalculateBoundingBox()
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (!target.isTransparent) {
			if (face.index >= 2 && face.index <= 5) {
				damage = face.index
				getLevel().setBlock(block, this, true, true)
				return true
			}
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val faces = intArrayOf(
					0,  //never use
					1,  //never use
					3,
					2,
					5,
					4
			)
			if (!this.getSide(BlockFace.fromIndex(faces[getDamage()])).isSolid) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				Item.get(Item.LADDER, 0, 1)
		)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(getDamage() and 0x07)
	}

	init {
		calculateOffsets()
	}
}