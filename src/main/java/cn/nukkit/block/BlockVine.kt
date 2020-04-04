package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 15.01.2016.
 */
class BlockVine @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val name: String
		get() = "Vines"

	override val id: Int
		get() = BlockID.Companion.VINE

	override val hardness: Double
		get() = 0.2

	override val resistance: Double
		get() = 1

	override fun canPassThrough(): Boolean {
		return true
	}

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override fun canBeReplaced(): Boolean {
		return true
	}

	override fun canBeClimbed(): Boolean {
		return true
	}

	override fun onEntityCollide(entity: Entity) {
		entity.resetFallDistance()
		entity.onGround = true
	}

	override val isSolid: Boolean
		get() = false

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		var f1 = 1.0
		var f2 = 1.0
		var f3 = 1.0
		var f4 = 0.0
		var f5 = 0.0
		var f6 = 0.0
		var flag = this.damage > 0
		if (this.damage and 0x02 > 0) {
			f4 = Math.max(f4, 0.0625)
			f1 = 0.0
			f2 = 0.0
			f5 = 1.0
			f3 = 0.0
			f6 = 1.0
			flag = true
		}
		if (this.damage and 0x08 > 0) {
			f1 = Math.min(f1, 0.9375)
			f4 = 1.0
			f2 = 0.0
			f5 = 1.0
			f3 = 0.0
			f6 = 1.0
			flag = true
		}
		if (this.damage and 0x01 > 0) {
			f3 = Math.min(f3, 0.9375)
			f6 = 1.0
			f1 = 0.0
			f4 = 1.0
			f2 = 0.0
			f5 = 1.0
			flag = true
		}
		if (!flag && this.up().isSolid) {
			f2 = Math.min(f2, 0.9375)
			f5 = 1.0
			f1 = 0.0
			f4 = 1.0
			f3 = 0.0
			f6 = 1.0
		}
		return SimpleAxisAlignedBB(
				x + f1,
				y + f2,
				z + f3,
				x + f4,
				y + f5,
				z + f6
		)
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (target.isSolid && face.horizontalIndex != -1) {
			this.setDamage(getMetaFromFace(face.opposite))
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isShears) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (!this.getSide(face).isSolid) {
				val up = this.up()
				if (up.id != id || up.damage != this.damage) {
					getLevel().useBreakOn(this, null, null, true)
					return Level.BLOCK_UPDATE_NORMAL
				}
			}
		}
		return 0
	}

	private val face: BlockFace
		private get() {
			val meta = this.damage
			if (meta and 1 > 0) {
				return BlockFace.SOUTH
			} else if (meta and 2 > 0) {
				return BlockFace.WEST
			} else if (meta and 4 > 0) {
				return BlockFace.NORTH
			} else if (meta and 8 > 0) {
				return BlockFace.EAST
			}
			return BlockFace.SOUTH
		}

	private fun getMetaFromFace(face: BlockFace): Int {
		return when (face) {
			BlockFace.SOUTH -> 0x01
			BlockFace.WEST -> 0x02
			BlockFace.NORTH -> 0x04
			BlockFace.EAST -> 0x08
			else -> 0x01
		}
	}

	override val toolType: Int
		get() = ItemTool.TYPE_SHEARS

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR
}