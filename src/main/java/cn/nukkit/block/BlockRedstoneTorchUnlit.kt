package cn.nukkit.block

import cn.nukkit.event.redstone.RedstoneUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3

/**
 * Created by CreeperFace on 10.4.2017.
 */
class BlockRedstoneTorchUnlit @JvmOverloads constructor(meta: Int = 0) : BlockTorch(meta) {
	override val name: String
		get() = "Unlit Redstone Torch"

	override val id: Int
		get() = BlockID.Companion.UNLIT_REDSTONE_TORCH

	override val lightLevel: Int
		get() = 0

	override fun getWeakPower(side: BlockFace): Int {
		return 0
	}

	override fun getStrongPower(side: BlockFace): Int {
		return 0
	}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.REDSTONE_TORCH))
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
		val face = blockFace.opposite
		val pos: Vector3 = location
		if (!level.isSidePowered(pos.getSide(face), face)) {
			level.setBlock(pos, Block.Companion.get(BlockID.Companion.REDSTONE_TORCH, damage), false, true)
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

	override fun tickRate(): Int {
		return 2
	}
}