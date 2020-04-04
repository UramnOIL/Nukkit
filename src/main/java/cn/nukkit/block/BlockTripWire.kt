package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.item.ItemString
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace

/**
 * @author CreeperFace
 */
class BlockTripWire @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val id: Int
		get() = BlockID.Companion.TRIPWIRE

	override val name: String
		get() = "Tripwire"

	override fun canPassThrough(): Boolean {
		return true
	}

	override val resistance: Double
		get() = 0

	override val hardness: Double
		get() = 0

	override val boundingBox: AxisAlignedBB?
		get() = null

	override fun toItem(): Item? {
		return ItemString()
	}

	var isPowered: Boolean
		get() = this.damage and 1 > 0
		set(value) {
			if (value xor isPowered) {
				this.setDamage(this.damage xor 0x01)
			}
		}

	var isAttached: Boolean
		get() = this.damage and 4 > 0
		set(value) {
			if (value xor isAttached) {
				this.setDamage(this.damage xor 0x04)
			}
		}

	var isDisarmed: Boolean
		get() = this.damage and 8 > 0
		set(value) {
			if (value xor isDisarmed) {
				this.setDamage(this.damage xor 0x08)
			}
		}

	override fun onEntityCollide(entity: Entity) {
		if (!entity.doesTriggerPressurePlate()) {
			return
		}
		var powered = isPowered
		if (!powered) {
			powered = true
			level.setBlock(this, this, true, false)
			updateHook(false)
			level.scheduleUpdate(this, 10)
		}
	}

	fun updateHook(scheduleUpdate: Boolean) {
		for (side in arrayOf(BlockFace.SOUTH, BlockFace.WEST)) {
			for (i in 1..41) {
				val block = this.getSide(side, i)
				if (block is BlockTripWireHook) {
					val hook = block
					if (hook.facing == side.opposite) {
						hook.calculateState(false, true, i, this)
					}

					/*if(scheduleUpdate) {
                        this.level.scheduleUpdate(hook, 10);
                    }*/break
				}
				if (block.id != BlockID.Companion.TRIPWIRE) {
					break
				}
			}
		}
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			if (!isPowered) {
				return type
			}
			var found = false
			for (entity in level.getCollidingEntities(this.collisionBoundingBox)) {
				if (!entity.doesTriggerPressurePlate()) {
					continue
				}
				found = true
			}
			if (found) {
				level.scheduleUpdate(this, 10)
			} else {
				this.powered = false
				level.setBlock(this, this, true, false)
				updateHook(false)
			}
			return type
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		getLevel().setBlock(this, this, true, true)
		updateHook(false)
		return true
	}

	override fun onBreak(item: Item): Boolean {
		if (item.id == Item.SHEARS) {
			this.disarmed = true
			level.setBlock(this, this, true, false)
			updateHook(false)
			getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		} else {
			this.powered = true
			getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
			updateHook(true)
		}
		return true
	}

	override fun getMaxY(): Double {
		return y + 0.5
	}

	override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
		return this
	}
}