package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * @author Nukkit Project Team
 */
class BlockLever @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta), Faceable {
	override val name: String
		get() = "Lever"

	override val id: Int
		get() = BlockID.Companion.LEVER

	override fun canBeActivated(): Boolean {
		return true
	}

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(toItem())
	}

	val isPowerOn: Boolean
		get() = this.damage and 0x08 > 0

	override fun onActivate(item: Item, player: Player?): Boolean {
		level.server.pluginManager.callEvent(BlockRedstoneEvent(this, if (isPowerOn) 15 else 0, if (isPowerOn) 0 else 15))
		this.setDamage(this.damage xor 0x08)
		getLevel().setBlock(this, this, false, true)
		getLevel().addSound(this, Sound.RANDOM_CLICK) //TODO: correct pitch
		val orientation = LeverOrientation.byMetadata(if (isPowerOn) this.damage xor 0x08 else this.damage)
		val face = orientation!!.facing
		//this.level.updateAroundRedstone(this, null);
		level.updateAroundRedstone(this.location.getSide(face.opposite), if (isPowerOn) face else null)
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val face = if (isPowerOn) this.damage xor 0x08 else this.damage
			val faces = LeverOrientation.byMetadata(face)!!.facing.opposite
			if (!this.getSide(faces).isSolid) {
				level.useBreakOn(this)
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (target.isNormalBlock) {
			this.setDamage(LeverOrientation.forFacings(face, player!!.horizontalFacing).metadata)
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun onBreak(item: Item): Boolean {
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		if (isPowerOn) {
			val face = LeverOrientation.byMetadata(if (isPowerOn) this.damage xor 0x08 else this.damage)!!.facing
			level.updateAround(this.location.getSide(face.opposite))
		}
		return true
	}

	override fun getWeakPower(side: BlockFace): Int {
		return if (isPowerOn) 15 else 0
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (!isPowerOn) 0 else if (LeverOrientation.byMetadata(if (isPowerOn) this.damage xor 0x08 else this.damage)!!.facing == side) 15 else 0
	}

	override val isPowerSource: Boolean
		get() = true

	enum class LeverOrientation(val metadata: Int, override val name: String, val facing: BlockFace) {
		DOWN_X(0, "down_x", BlockFace.DOWN), EAST(1, "east", BlockFace.EAST), WEST(2, "west", BlockFace.WEST), SOUTH(3, "south", BlockFace.SOUTH), NORTH(4, "north", BlockFace.NORTH), UP_Z(5, "up_z", BlockFace.UP), UP_X(6, "up_x", BlockFace.UP), DOWN_Z(7, "down_z", BlockFace.DOWN);

		override fun toString(): String {
			return name
		}

		companion object {
			private val META_LOOKUP = arrayOfNulls<LeverOrientation>(values().size)
			fun byMetadata(meta: Int): LeverOrientation? {
				var meta = meta
				if (meta < 0 || meta >= META_LOOKUP.size) {
					meta = 0
				}
				return META_LOOKUP[meta]
			}

			fun forFacings(clickedSide: BlockFace, playerDirection: BlockFace): LeverOrientation {
				return when (clickedSide) {
					BlockFace.DOWN -> when (playerDirection.axis) {
						BlockFace.Axis.X -> DOWN_X
						BlockFace.Axis.Z -> DOWN_Z
						else -> throw IllegalArgumentException("Invalid entityFacing $playerDirection for facing $clickedSide")
					}
					BlockFace.UP -> when (playerDirection.axis) {
						BlockFace.Axis.X -> UP_X
						BlockFace.Axis.Z -> UP_Z
						else -> throw IllegalArgumentException("Invalid entityFacing $playerDirection for facing $clickedSide")
					}
					BlockFace.NORTH -> NORTH
					BlockFace.SOUTH -> SOUTH
					BlockFace.WEST -> WEST
					BlockFace.EAST -> EAST
					else -> throw IllegalArgumentException("Invalid facing: $clickedSide")
				}
			}

			init {
				for (face in values()) {
					META_LOOKUP[cn.nukkit.block.face.getMetadata()] = cn.nukkit.block.face
				}
			}
		}

	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR
}