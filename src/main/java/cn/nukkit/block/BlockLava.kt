package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityPrimedTNT
import cn.nukkit.event.block.BlockIgniteEvent
import cn.nukkit.event.entity.EntityCombustByBlockEvent
import cn.nukkit.event.entity.EntityDamageByBlockEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.Item
import cn.nukkit.level.GameRule
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.potion.Effect
import cn.nukkit.utils.BlockColor
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BlockLava @JvmOverloads constructor(meta: Int = 0) : BlockLiquid(meta) {
	override val id: Int
		get() = BlockID.Companion.LAVA

	override val lightLevel: Int
		get() = 15

	override val name: String
		get() = "Lava"

	override fun onEntityCollide(entity: Entity) {
		entity.highestPosition -= (entity.highestPosition - entity.y) * 0.5

		// Always setting the duration to 15 seconds? TODO
		val ev = EntityCombustByBlockEvent(this, entity, 15)
		Server.instance!!.pluginManager.callEvent(ev)
		if (!ev.isCancelled // Making sure the entity is actually alive and not invulnerable.
				&& entity.isAlive
				&& entity.noDamageTicks == 0) {
			entity.setOnFire(ev.duration)
		}
		if (!entity.hasEffect(Effect.FIRE_RESISTANCE)) {
			entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.LAVA, 4))
		}
		super.onEntityCollide(entity)
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val ret = getLevel().setBlock(this, this, true, false)
		getLevel().scheduleUpdate(this, tickRate())
		return ret
	}

	override fun onUpdate(type: Int): Int {
		val result = super.onUpdate(type)
		if (type == Level.BLOCK_UPDATE_RANDOM && level.gameRules.getBoolean(GameRule.DO_FIRE_TICK)) {
			val random: Random = ThreadLocalRandom.current()
			val i = random.nextInt(3)
			if (i > 0) {
				for (k in 0 until i) {
					val v: Vector3 = this.add(random.nextInt(3) - 1.toDouble(), 1.0, random.nextInt(3) - 1.toDouble())
					val block = getLevel().getBlock(v)
					if (block.id == BlockID.Companion.AIR) {
						if (isSurroundingBlockFlammable(block)) {
							val e = BlockIgniteEvent(block, this, null, BlockIgniteEvent.BlockIgniteCause.LAVA)
							level.server.pluginManager.callEvent(e)
							if (!e.isCancelled) {
								val fire: Block = Block.Companion.get(BlockID.Companion.FIRE)
								getLevel().setBlock(v, fire, true)
								getLevel().scheduleUpdate(fire, fire.tickRate())
								return Level.BLOCK_UPDATE_RANDOM
							}
							return 0
						}
					} else if (block.isSolid) {
						return Level.BLOCK_UPDATE_RANDOM
					}
				}
			} else {
				for (k in 0..2) {
					val v: Vector3 = this.add(random.nextInt(3) - 1.toDouble(), 0.0, random.nextInt(3) - 1.toDouble())
					val block = getLevel().getBlock(v)
					if (block.up().id == BlockID.Companion.AIR && block.burnChance > 0) {
						val e = BlockIgniteEvent(block, this, null, BlockIgniteEvent.BlockIgniteCause.LAVA)
						level.server.pluginManager.callEvent(e)
						if (!e.isCancelled) {
							val fire: Block = Block.Companion.get(BlockID.Companion.FIRE)
							getLevel().setBlock(v, fire, true)
							getLevel().scheduleUpdate(fire, fire.tickRate())
						}
					}
				}
			}
		}
		return result
	}

	protected fun isSurroundingBlockFlammable(block: Block): Boolean {
		for (face in BlockFace.values()) {
			if (block.getSide(face).burnChance > 0) {
				return true
			}
		}
		return false
	}

	override val color: BlockColor
		get() = BlockColor.LAVA_BLOCK_COLOR

	override fun getBlock(meta: Int): BlockLiquid {
		return Block.Companion.get(BlockID.Companion.LAVA, meta) as BlockLiquid
	}

	override fun tickRate(): Int {
		return 30
	}

	override val flowDecayPerBlock: Int
		get() = if (level.dimension == Level.DIMENSION_NETHER) {
			1
		} else 2

	override fun checkForHarden() {
		var colliding: Block? = null
		for (side in 1..5) { //don't check downwards side
			val blockSide = this.getSide(BlockFace.fromIndex(side))
			if (blockSide is BlockWater) {
				colliding = blockSide
				break
			}
		}
		if (colliding != null) {
			if (this.damage == 0) {
				liquidCollide(colliding, Block.Companion.get(BlockID.Companion.OBSIDIAN))
			} else if (this.damage <= 4) {
				liquidCollide(colliding, Block.Companion.get(BlockID.Companion.COBBLESTONE))
			}
		}
	}

	override fun flowIntoBlock(block: Block, newFlowDecay: Int) {
		if (block is BlockWater) {
			(block as BlockLiquid).liquidCollide(this, Block.Companion.get(BlockID.Companion.STONE))
		} else {
			super.flowIntoBlock(block, newFlowDecay)
		}
	}

	override fun addVelocityToEntity(entity: Entity, vector: Vector3) {
		if (entity !is EntityPrimedTNT) {
			super.addVelocityToEntity(entity, vector)
		}
	}
}