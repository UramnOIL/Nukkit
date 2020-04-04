package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.redstone.RedstoneUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * @author CreeperFace
 */
abstract class BlockRedstoneDiode @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta), Faceable {
	protected var isPowered = false
	override fun onBreak(item: Item): Boolean {
		val pos: Vector3 = location
		level.setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		for (face in BlockFace.values()) {
			level.updateAroundRedstone(pos.getSide(face), null)
		}
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (block.getSide(BlockFace.DOWN).isTransparent) {
			return false
		}
		this.setDamage(player?.direction?.opposite?.horizontalIndex ?: 0)
		level.setBlock(block, this, true, true)
		if (shouldBePowered()) {
			level.scheduleUpdate(this, 1)
		}
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			if (!isLocked) {
				val pos: Vector3 = location
				val shouldBePowered = shouldBePowered()
				if (isPowered && !shouldBePowered) {
					level.setBlock(pos, unpowered, true, true)
					level.updateAroundRedstone(this.location.getSide(facing.opposite), null)
				} else if (!isPowered) {
					level.setBlock(pos, getPowered(), true, true)
					level.updateAroundRedstone(this.location.getSide(facing.opposite), null)
					if (!shouldBePowered) {
//                        System.out.println("schedule update 2");
						level.scheduleUpdate(getPowered(), this, delay)
					}
				}
			}
		} else if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
			// Redstone event
			val ev = RedstoneUpdateEvent(this)
			getLevel().server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return 0
			}
			return if (type == Level.BLOCK_UPDATE_NORMAL && this.getSide(BlockFace.DOWN).isTransparent) {
				level.useBreakOn(this)
				Level.BLOCK_UPDATE_NORMAL
			} else {
				updateState()
				Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	open fun updateState() {
		if (!isLocked) {
			val shouldPowered = shouldBePowered()
			if ((isPowered && !shouldPowered || !isPowered && shouldPowered) && !level.isBlockTickPending(this, this)) {
				/*int priority = -1;

                if (this.isFacingTowardsRepeater()) {
                    priority = -3;
                } else if (this.isPowered) {
                    priority = -2;
                }*/
				level.scheduleUpdate(this, this, delay)
			}
		}
	}

	open val isLocked: Boolean
		get() = false

	protected open fun calculateInputStrength(): Int {
		val face = facing
		val pos: Vector3 = this.location.getSide(face)
		val power = level.getRedstonePower(pos, face)
		return if (power >= 15) {
			power
		} else {
			val block = level.getBlock(pos)
			Math.max(power, if (block.id == BlockID.Companion.REDSTONE_WIRE) block.damage else 0)
		}
	}

	protected val powerOnSides: Int
		protected get() {
			val pos: Vector3 = location
			val face = facing
			val face1 = face.rotateY()
			val face2 = face.rotateYCCW()
			return Math.max(getPowerOnSide(pos.getSide(face1), face1), getPowerOnSide(pos.getSide(face2), face2))
		}

	protected fun getPowerOnSide(pos: Vector3?, side: BlockFace?): Int {
		val block = level.getBlock(pos)
		return if (isAlternateInput(block)) if (block.id == BlockID.Companion.REDSTONE_BLOCK) 15 else if (block.id == BlockID.Companion.REDSTONE_WIRE) block.damage else level.getStrongPower(pos, side) else 0
	}

	override val isPowerSource: Boolean
		get() = true

	protected open fun shouldBePowered(): Boolean {
		return calculateInputStrength() > 0
	}

	abstract val facing: BlockFace
	protected abstract val delay: Int
	protected abstract val unpowered: Block
	protected abstract fun getPowered(): Block
	override fun getMaxY(): Double {
		return y + 0.125
	}

	override fun canPassThrough(): Boolean {
		return false
	}

	protected open fun isAlternateInput(block: Block): Boolean {
		return block.isPowerSource
	}

	protected open val redstoneSignal: Int
		protected get() = 15

	override fun getStrongPower(side: BlockFace): Int {
		return getWeakPower(side)
	}

	override fun getWeakPower(side: BlockFace): Int {
		return if (!isPowered()) 0 else if (facing == side) redstoneSignal else 0
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	open fun isPowered(): Boolean {
		return isPowered
	}

	val isFacingTowardsRepeater: Boolean
		get() {
			val side = facing.opposite
			val block = this.getSide(side)
			return block is BlockRedstoneDiode && block.facing != side
		}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	companion object {
		@kotlin.jvm.JvmStatic
		fun isDiode(block: Block?): Boolean {
			return block is BlockRedstoneDiode
		}
	}
}