package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.redstone.RedstoneUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockRedstoneTorch @JvmOverloads constructor(meta: Int = 0) : BlockTorch(meta) {
	override val name: String
		get() = "Redstone Torch"

	override val id: Int
		get() = BlockID.Companion.REDSTONE_TORCH

	override val lightLevel: Int
		get() = 7

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (!super.place(item, block, target, face, fx, fy, fz, player)) {
			return false
		}

//        if (!checkState()) {
//            BlockFace facing = getFacing().getOpposite();
//            Vector3 pos = getLocation();
//
//            for (BlockFace side : BlockFace.values()) {
//                if (facing == side) {
//                    continue;
//                }
//
//                this.level.updateAround(pos.getSide(side));
//            }
//        }
		checkState()
		return true
	}

	override fun getWeakPower(side: BlockFace): Int {
		return if (blockFace != side) 15 else 0
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (side == BlockFace.DOWN) getWeakPower(side) else 0
	}

	override fun onBreak(item: Item): Boolean {
		super.onBreak(item)
		val pos: Vector3 = location
		val face = blockFace.opposite
		for (side in BlockFace.values()) {
			if (side == face) {
				continue
			}
			level.updateAroundRedstone(pos.getSide(side), null)
		}
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (super.onUpdate(type) == 0) {
			if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
				level.scheduleUpdate(this, tickRate())
			} else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
				val ev = RedstoneUpdateEvent(this)
				getLevel().server.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return 0
				}
				if (checkState()) {
					return 1
				}
			}
		}
		return 0
	}

	protected fun checkState(): Boolean {
		if (isPoweredFromSide) {
			val face = blockFace.opposite
			val pos: Vector3 = location
			level.setBlock(pos, Block.Companion.get(BlockID.Companion.UNLIT_REDSTONE_TORCH, damage), false, true)
			for (side in BlockFace.values()) {
				if (side == face) {
					continue
				}
				level.updateAroundRedstone(pos.getSide(side), null)
			}
			return true
		}
		return false
	}

	protected val isPoweredFromSide: Boolean
		protected get() {
			val face = blockFace.opposite
			return level.isSidePowered(this.location.getSide(face), face)
		}

	override fun tickRate(): Int {
		return 2
	}

	override val isPowerSource: Boolean
		get() = true

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR
}