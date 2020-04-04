package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.event.redstone.RedstoneUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemRedstone
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.BlockFace.Plane
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor
import java.util.*

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockRedstoneWire @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override var isPowerSource = true
		private set
	private val blocksNeedingUpdate: Set<Vector3> = HashSet()
	override val name: String
		get() = "Redstone Wire"

	override val id: Int
		get() = BlockID.Companion.REDSTONE_WIRE

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (face != BlockFace.UP || !canBePlacedOn(target)) {
			return false
		}
		getLevel().setBlock(block, this, true, false)
		updateSurroundingRedstone(true)
		val pos: Vector3 = location
		for (blockFace in Plane.VERTICAL) {
			level.updateAroundRedstone(pos.getSide(blockFace), blockFace.opposite)
		}
		for (blockFace in Plane.VERTICAL) {
			updateAround(pos.getSide(blockFace), blockFace.opposite)
		}
		for (blockFace in Plane.HORIZONTAL) {
			val v = pos.getSide(blockFace)
			if (level.getBlock(v).isNormalBlock) {
				updateAround(v.up(), BlockFace.DOWN)
			} else {
				updateAround(v.down(), BlockFace.UP)
			}
		}
		return true
	}

	private fun updateAround(pos: Vector3, face: BlockFace) {
		if (level.getBlock(pos).id == BlockID.Companion.REDSTONE_WIRE) {
			level.updateAroundRedstone(pos, face)
			for (side in BlockFace.values()) {
				level.updateAroundRedstone(pos.getSide(side), side.opposite)
			}
		}
	}

	private fun updateSurroundingRedstone(force: Boolean) {
		calculateCurrentChanges(force)
	}

	private fun calculateCurrentChanges(force: Boolean) {
		val pos: Vector3 = this.location
		val meta = this.damage
		var maxStrength = meta
		isPowerSource = false
		val power = indirectPower
		isPowerSource = true
		if (power > 0 && power > maxStrength - 1) {
			maxStrength = power
		}
		var strength = 0
		for (face in Plane.HORIZONTAL) {
			val v = pos.getSide(face)
			if (v.getX() == getX() && v.getZ() == getZ()) {
				continue
			}
			strength = getMaxCurrentStrength(v, strength)
			val vNormal = level.getBlock(v).isNormalBlock
			if (vNormal && !level.getBlock(pos.up()).isNormalBlock) {
				strength = getMaxCurrentStrength(v.up(), strength)
			} else if (!vNormal) {
				strength = getMaxCurrentStrength(v.down(), strength)
			}
		}
		if (strength > maxStrength) {
			maxStrength = strength - 1
		} else if (maxStrength > 0) {
			--maxStrength
		} else {
			maxStrength = 0
		}
		if (power > maxStrength - 1) {
			maxStrength = power
		} else if (power < maxStrength && strength <= maxStrength) {
			maxStrength = Math.max(power, strength - 1)
		}
		if (meta != maxStrength) {
			level.server.pluginManager.callEvent(BlockRedstoneEvent(this, meta, maxStrength))
			this.setDamage(maxStrength)
			level.setBlock(this, this, false, false)
			level.updateAroundRedstone(this, null)
			for (face in BlockFace.values()) {
				level.updateAroundRedstone(pos.getSide(face), face.opposite)
			}
		} else if (force) {
			for (face in BlockFace.values()) {
				level.updateAroundRedstone(pos.getSide(face), face.opposite)
			}
		}
	}

	private fun getMaxCurrentStrength(pos: Vector3, maxStrength: Int): Int {
		return if (level.getBlockIdAt(pos.floorX, pos.floorY, pos.floorZ) != id) {
			maxStrength
		} else {
			val strength = level.getBlockDataAt(pos.floorX, pos.floorY, pos.floorZ)
			if (strength > maxStrength) strength else maxStrength
		}
	}

	override fun onBreak(item: Item): Boolean {
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		val pos: Vector3 = location
		updateSurroundingRedstone(false)
		for (blockFace in BlockFace.values()) {
			level.updateAroundRedstone(pos.getSide(blockFace), null)
		}
		for (blockFace in Plane.HORIZONTAL) {
			val v = pos.getSide(blockFace)
			if (level.getBlock(v).isNormalBlock) {
				updateAround(v.up(), BlockFace.DOWN)
			} else {
				updateAround(v.down(), BlockFace.UP)
			}
		}
		return true
	}

	override fun toItem(): Item? {
		return ItemRedstone()
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	override fun onUpdate(type: Int): Int {
		if (type != Level.BLOCK_UPDATE_NORMAL && type != Level.BLOCK_UPDATE_REDSTONE) {
			return 0
		}
		// Redstone event
		val ev = RedstoneUpdateEvent(this)
		getLevel().server.pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return 0
		}
		if (type == Level.BLOCK_UPDATE_NORMAL && !canBePlacedOn(this.location.down())) {
			getLevel().useBreakOn(this)
			return Level.BLOCK_UPDATE_NORMAL
		}
		updateSurroundingRedstone(false)
		return Level.BLOCK_UPDATE_NORMAL
	}

	fun canBePlacedOn(v: Vector3?): Boolean {
		val b = level.getBlock(v)
		return b.isSolid && !b.isTransparent && b.id != BlockID.Companion.GLOWSTONE
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (!isPowerSource) 0 else getWeakPower(side)
	}

	override fun getWeakPower(side: BlockFace): Int {
		return if (!isPowerSource) {
			0
		} else {
			val power = this.damage
			if (power == 0) {
				0
			} else if (side == BlockFace.UP) {
				power
			} else {
				val enumset = EnumSet.noneOf(BlockFace::class.java)
				for (face in Plane.HORIZONTAL) {
					if (isPowerSourceAt(face)) {
						enumset.add(face)
					}
				}
				if (side.axis.isHorizontal && enumset.isEmpty()) {
					power
				} else if (enumset.contains(side) && !enumset.contains(side.rotateYCCW()) && !enumset.contains(side.rotateY())) {
					power
				} else {
					0
				}
			}
		}
	}

	private fun isPowerSourceAt(side: BlockFace): Boolean {
		val pos: Vector3 = location
		val v = pos.getSide(side)
		val block = level.getBlock(v)
		val flag = block.isNormalBlock
		val flag1 = level.getBlock(pos.up()).isNormalBlock
		return !flag1 && flag && canConnectUpwardsTo(level, v.up()) || canConnectTo(block, side) || !flag && canConnectUpwardsTo(level, block.down())
	}

	private val indirectPower: Int
		private get() {
			var power = 0
			val pos: Vector3 = location
			for (face in BlockFace.values()) {
				val blockPower = getIndirectPower(pos.getSide(face), face)
				if (blockPower >= 15) {
					return 15
				}
				if (blockPower > power) {
					power = blockPower
				}
			}
			return power
		}

	private fun getIndirectPower(pos: Vector3, face: BlockFace): Int {
		val block = level.getBlock(pos)
		if (block.id == BlockID.Companion.REDSTONE_WIRE) {
			return 0
		}
		return if (block.isNormalBlock) getStrongPower(pos.getSide(face), face) else block.getWeakPower(face)
	}

	private fun getStrongPower(pos: Vector3, direction: BlockFace): Int {
		val block = level.getBlock(pos)
		return if (block.id == BlockID.Companion.REDSTONE_WIRE) {
			0
		} else block.getStrongPower(direction)
	}

	companion object {
		protected fun canConnectUpwardsTo(level: Level, pos: Vector3?): Boolean {
			return canConnectUpwardsTo(level.getBlock(pos))
		}

		protected fun canConnectUpwardsTo(block: Block): Boolean {
			return canConnectTo(block, null)
		}

		protected fun canConnectTo(block: Block, side: BlockFace?): Boolean {
			return if (block.id == BlockID.Companion.REDSTONE_WIRE) {
				true
			} else if (BlockRedstoneDiode.Companion.isDiode(block)) {
				val face = (block as BlockRedstoneDiode).facing
				face == side || face!!.opposite == side
			} else {
				block.isPowerSource && side != null
			}
		}
	}
}