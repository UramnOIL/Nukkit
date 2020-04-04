package cn.nukkit.block

import cn.nukkit.entity.Entity
import cn.nukkit.event.block.BlockFromToEvent
import cn.nukkit.event.block.LiquidFlowEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.SmokeParticle
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.LevelSoundEventPacket
import it.unimi.dsi.fastutil.longs.Long2ByteMap
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockLiquid protected constructor(meta: Int) : BlockTransparentMeta(meta) {
	private val CAN_FLOW_DOWN: Byte = 1
	private val CAN_FLOW: Byte = 0
	private val BLOCKED: Byte = -1
	var adjacentSources = 0
	protected var flowVector: Vector3? = null
	private val flowCostVisited: Long2ByteMap = Long2ByteOpenHashMap()
	override fun canBeFlowedInto(): Boolean {
		return true
	}

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return null
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override fun canBeReplaced(): Boolean {
		return true
	}

	override val isSolid: Boolean
		get() = false

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val boundingBox: AxisAlignedBB?
		get() = null

	override fun getMaxY(): Double {
		return y + 1 - fluidHeightPercent
	}

	override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
		return this
	}

	val fluidHeightPercent: Float
		get() {
			var d = this.damage as Float
			if (d >= 8) {
				d = 0f
			}
			return (d + 1) / 9f
		}

	protected fun getFlowDecay(block: Block): Int {
		return if (block.id != this.id) {
			-1
		} else block.damage
	}

	protected fun getEffectiveFlowDecay(block: Block): Int {
		if (block.id != this.id) {
			return -1
		}
		var decay = block.damage
		if (decay >= 8) {
			decay = 0
		}
		return decay
	}

	fun clearCaches() {
		flowVector = null
		flowCostVisited.clear()
	}

	fun getFlowVector(): Vector3? {
		if (flowVector != null) {
			return flowVector
		}
		var vector = Vector3(0, 0, 0)
		val decay = getEffectiveFlowDecay(this)
		for (j in 0..3) {
			var x = x.toInt()
			val y = y.toInt()
			var z = z.toInt()
			when (j) {
				0 -> --x
				1 -> x++
				2 -> z--
				else -> z++
			}
			val sideBlock = level.getBlock(x, y, z)
			var blockDecay = getEffectiveFlowDecay(sideBlock)
			if (blockDecay < 0) {
				if (!sideBlock.canBeFlowedInto()) {
					continue
				}
				blockDecay = getEffectiveFlowDecay(level.getBlock(x, y - 1, z))
				if (blockDecay >= 0) {
					val realDecay = blockDecay - (decay - 8)
					vector.x += (sideBlock.x - this.x) * realDecay
					vector.y += (sideBlock.y - this.y) * realDecay
					vector.z += (sideBlock.z - this.z) * realDecay
				}
			} else {
				val realDecay = blockDecay - decay
				vector.x += (sideBlock.x - this.x) * realDecay
				vector.y += (sideBlock.y - this.y) * realDecay
				vector.z += (sideBlock.z - this.z) * realDecay
			}
		}
		if (this.damage >= 8) {
			if (!canFlowInto(level.getBlock(x.toInt(), y.toInt(), z.toInt() - 1)) ||
					!canFlowInto(level.getBlock(x.toInt(), y.toInt(), z.toInt() + 1)) ||
					!canFlowInto(level.getBlock(x.toInt() - 1, y.toInt(), z.toInt())) ||
					!canFlowInto(level.getBlock(x.toInt() + 1, y.toInt(), z.toInt())) ||
					!canFlowInto(level.getBlock(x.toInt(), y.toInt() + 1, z.toInt() - 1)) ||
					!canFlowInto(level.getBlock(x.toInt(), y.toInt() + 1, z.toInt() + 1)) ||
					!canFlowInto(level.getBlock(x.toInt() - 1, y.toInt() + 1, z.toInt())) ||
					!canFlowInto(level.getBlock(x.toInt() + 1, y.toInt() + 1, z.toInt()))) {
				vector = vector.normalize().add(0.0, -6.0, 0.0)
			}
		}
		return vector.normalize().also { flowVector = it }
	}

	override fun addVelocityToEntity(entity: Entity, vector: Vector3) {
		if (entity.canBeMovedByCurrents()) {
			val flow = getFlowVector()
			vector.x += flow!!.x
			vector.y += flow.y
			vector.z += flow.z
		}
	}

	open val flowDecayPerBlock: Int
		get() = 1

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			checkForHarden()
			level.scheduleUpdate(this, tickRate())
			return 0
		} else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			var decay = getFlowDecay(this)
			val multiplier = flowDecayPerBlock
			if (decay > 0) {
				var smallestFlowDecay = -100
				adjacentSources = 0
				smallestFlowDecay = getSmallestFlowDecay(level.getBlock(x.toInt(), y.toInt(), z.toInt() - 1), smallestFlowDecay)
				smallestFlowDecay = getSmallestFlowDecay(level.getBlock(x.toInt(), y.toInt(), z.toInt() + 1), smallestFlowDecay)
				smallestFlowDecay = getSmallestFlowDecay(level.getBlock(x.toInt() - 1, y.toInt(), z.toInt()), smallestFlowDecay)
				smallestFlowDecay = getSmallestFlowDecay(level.getBlock(x.toInt() + 1, y.toInt(), z.toInt()), smallestFlowDecay)
				var newDecay = smallestFlowDecay + multiplier
				if (newDecay >= 8 || smallestFlowDecay < 0) {
					newDecay = -1
				}
				val topFlowDecay = getFlowDecay(level.getBlock(x.toInt(), y.toInt() + 1, z.toInt()))
				if (topFlowDecay >= 0) {
					newDecay = topFlowDecay or 0x08
				}
				if (adjacentSources >= 2 && this is BlockWater) {
					val bottomBlock = level.getBlock(x.toInt(), y.toInt() - 1, z.toInt())
					if (bottomBlock.isSolid) {
						newDecay = 0
					} else if (bottomBlock is BlockWater && bottomBlock.getDamage() == 0) {
						newDecay = 0
					}
				}
				if (newDecay != decay) {
					decay = newDecay
					val decayed = decay < 0
					val to: Block
					to = if (decayed) {
						Block.Companion.get(BlockID.Companion.AIR)
					} else {
						getBlock(decay)
					}
					val event = BlockFromToEvent(this, to)
					level.server.pluginManager.callEvent(event)
					if (!event.isCancelled) {
						level.setBlock(this, event.to, true, true)
						if (!decayed) {
							level.scheduleUpdate(this, tickRate())
						}
					}
				}
			}
			if (decay >= 0) {
				val bottomBlock = level.getBlock(x.toInt(), y.toInt() - 1, z.toInt())
				flowIntoBlock(bottomBlock, decay or 0x08)
				if (decay == 0 || !bottomBlock.canBeFlowedInto()) {
					val adjacentDecay: Int
					adjacentDecay = if (decay >= 8) {
						1
					} else {
						decay + multiplier
					}
					if (adjacentDecay < 8) {
						val flags = optimalFlowDirections
						if (flags[0]) {
							flowIntoBlock(level.getBlock(x.toInt() - 1, y.toInt(), z.toInt()), adjacentDecay)
						}
						if (flags[1]) {
							flowIntoBlock(level.getBlock(x.toInt() + 1, y.toInt(), z.toInt()), adjacentDecay)
						}
						if (flags[2]) {
							flowIntoBlock(level.getBlock(x.toInt(), y.toInt(), z.toInt() - 1), adjacentDecay)
						}
						if (flags[3]) {
							flowIntoBlock(level.getBlock(x.toInt(), y.toInt(), z.toInt() + 1), adjacentDecay)
						}
					}
				}
				checkForHarden()
			}
		}
		return 0
	}

	protected open fun flowIntoBlock(block: Block, newFlowDecay: Int) {
		if (canFlowInto(block) && block !is BlockLiquid) {
			val event = LiquidFlowEvent(block, this, newFlowDecay)
			level.server.pluginManager.callEvent(event)
			if (!event.isCancelled) {
				if (block.id > 0) {
					level.useBreakOn(block)
				}
				level.setBlock(block, getBlock(newFlowDecay), true, true)
				level.scheduleUpdate(block, tickRate())
			}
		}
	}

	private fun calculateFlowCost(blockX: Int, blockY: Int, blockZ: Int, accumulatedCost: Int, maxCost: Int, originOpposite: Int, lastOpposite: Int): Int {
		var cost = 1000
		for (j in 0..3) {
			if (j == originOpposite || j == lastOpposite) {
				continue
			}
			var x = blockX
			var z = blockZ
			if (j == 0) {
				--x
			} else if (j == 1) {
				++x
			} else if (j == 2) {
				--z
			} else if (j == 3) {
				++z
			}
			val hash = Level.blockHash(x, blockY, z)
			if (!flowCostVisited.containsKey(hash)) {
				val blockSide = level.getBlock(x, blockY, z)
				if (!canFlowInto(blockSide)) {
					flowCostVisited[hash] = BLOCKED
				} else if (level.getBlock(x, blockY - 1, z).canBeFlowedInto()) {
					flowCostVisited[hash] = CAN_FLOW_DOWN
				} else {
					flowCostVisited[hash] = CAN_FLOW
				}
			}
			val status = flowCostVisited[hash]
			if (status == BLOCKED) {
				continue
			} else if (status == CAN_FLOW_DOWN) {
				return accumulatedCost
			}
			if (accumulatedCost >= maxCost) {
				continue
			}
			val realCost = calculateFlowCost(x, blockY, z, accumulatedCost + 1, maxCost, originOpposite, j xor 0x01)
			if (realCost < cost) {
				cost = realCost
			}
		}
		return cost
	}

	override val hardness: Double
		get() = 100.0

	override val resistance: Double
		get() = 500

	private val optimalFlowDirections: BooleanArray
		private get() {
			val flowCost = intArrayOf(
					1000,
					1000,
					1000,
					1000
			)
			var maxCost = 4 / flowDecayPerBlock
			for (j in 0..3) {
				var x = x.toInt()
				val y = y.toInt()
				var z = z.toInt()
				if (j == 0) {
					--x
				} else if (j == 1) {
					++x
				} else if (j == 2) {
					--z
				} else {
					++z
				}
				val block = level.getBlock(x, y, z)
				if (!canFlowInto(block)) {
					flowCostVisited[Level.blockHash(x, y, z)] = BLOCKED
				} else if (level.getBlock(x, y - 1, z).canBeFlowedInto()) {
					flowCostVisited[Level.blockHash(x, y, z)] = CAN_FLOW_DOWN
					maxCost = 0
					flowCost[j] = maxCost
				} else if (maxCost > 0) {
					flowCostVisited[Level.blockHash(x, y, z)] = CAN_FLOW
					flowCost[j] = calculateFlowCost(x, y, z, 1, maxCost, j xor 0x01, j xor 0x01)
					maxCost = Math.min(maxCost, flowCost[j])
				}
			}
			flowCostVisited.clear()
			var minCost = Double.MAX_VALUE
			for (i in 0..3) {
				val d = flowCost[i].toDouble()
				if (d < minCost) {
					minCost = d
				}
			}
			val isOptimalFlowDirection = BooleanArray(4)
			for (i in 0..3) {
				isOptimalFlowDirection[i] = flowCost[i] == minCost
			}
			return isOptimalFlowDirection
		}

	private fun getSmallestFlowDecay(block: Block, decay: Int): Int {
		var blockDecay = getFlowDecay(block)
		if (blockDecay < 0) {
			return decay
		} else if (blockDecay == 0) {
			++adjacentSources
		} else if (blockDecay >= 8) {
			blockDecay = 0
		}
		return if (decay >= 0 && blockDecay >= decay) decay else blockDecay
	}

	protected open fun checkForHarden() {}
	protected fun triggerLavaMixEffects(pos: Vector3) {
		getLevel().addSound(pos.add(0.5, 0.5, 0.5), Sound.RANDOM_FIZZ, 1f, 2.6f + (ThreadLocalRandom.current().nextFloat() - ThreadLocalRandom.current().nextFloat()) * 0.8f)
		for (i in 0..7) {
			getLevel().addParticle(SmokeParticle(pos.add(Math.random(), 1.2, Math.random())))
		}
	}

	abstract fun getBlock(meta: Int): BlockLiquid
	override fun canPassThrough(): Boolean {
		return true
	}

	override fun onEntityCollide(entity: Entity) {
		entity.resetFallDistance()
	}

	fun liquidCollide(cause: Block?, result: Block?): Boolean {
		val event = BlockFromToEvent(this, result)
		level.server.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return false
		}
		level.setBlock(this, event.to, true, true)
		getLevel().addLevelSoundEvent(this.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_FIZZ)
		return true
	}

	protected fun canFlowInto(block: Block): Boolean {
		return block.canBeFlowedInto() && !(block is BlockLiquid && block.getDamage() == 0)
	}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.AIR))
	}
}