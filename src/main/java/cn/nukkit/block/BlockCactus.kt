package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.event.block.BlockGrowEvent
import cn.nukkit.event.entity.EntityDamageByBlockEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockCactus @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.CACTUS

	override val hardness: Double
		get() = 0.4

	override val resistance: Double
		get() = 2

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override fun getMinX(): Double {
		return x + 0.0625
	}

	override fun getMinY(): Double {
		return y
	}

	override fun getMinZ(): Double {
		return z + 0.0625
	}

	override fun getMaxX(): Double {
		return x + 0.9375
	}

	override fun getMaxY(): Double {
		return y + 0.9375
	}

	override fun getMaxZ(): Double {
		return z + 0.9375
	}

	override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
		return SimpleAxisAlignedBB(x, y, z, x + 1, y + 1, z + 1)
	}

	override fun onEntityCollide(entity: Entity) {
		entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.CONTACT, 1))
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val down = down()
			if (down.id != BlockID.Companion.SAND && down.id != BlockID.Companion.CACTUS) {
				getLevel().useBreakOn(this)
			} else {
				for (side in 2..5) {
					val block = getSide(BlockFace.fromIndex(side))
					if (!block!!.canBeFlowedInto()) {
						getLevel().useBreakOn(this)
					}
				}
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (down().id != BlockID.Companion.CACTUS) {
				if (this.damage == 0x0F) {
					for (y in 1..2) {
						val b = getLevel().getBlock(Vector3(x, this.y + y, z))
						if (b.id == BlockID.Companion.AIR) {
							val event = BlockGrowEvent(b, Block.Companion.get(BlockID.Companion.CACTUS))
							Server.instance!!.pluginManager.callEvent(event)
							if (!event.isCancelled) {
								getLevel().setBlock(b, event.newState, true)
							}
						}
					}
					this.setDamage(0)
					getLevel().setBlock(this, this)
				} else {
					this.setDamage(this.damage + 1)
					getLevel().setBlock(this, this)
				}
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down.id == BlockID.Companion.SAND || down.id == BlockID.Companion.CACTUS) {
			val block0 = north()
			val block1 = south()
			val block2 = west()
			val block3 = east()
			if (block0!!.canBeFlowedInto() && block1!!.canBeFlowedInto() && block2!!.canBeFlowedInto() && block3!!.canBeFlowedInto()) {
				getLevel().setBlock(this, this, true)
				return true
			}
		}
		return false
	}

	override val name: String
		get() = "Cactus"

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				Item.get(Item.CACTUS, 0, 1)
		)
	}
}