package cn.nukkit.block

import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.event.block.BlockBurnEvent
import cn.nukkit.event.block.BlockFadeEvent
import cn.nukkit.event.block.BlockIgniteEvent
import cn.nukkit.event.entity.EntityCombustByBlockEvent
import cn.nukkit.event.entity.EntityDamageByBlockEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.GameRule
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
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
class BlockFire @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val id: Int
		get() = BlockID.Companion.FIRE

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override val name: String
		get() = "Fire Block"

	override val lightLevel: Int
		get() = 15

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override fun canBeReplaced(): Boolean {
		return true
	}

	override fun onEntityCollide(entity: Entity) {
		if (!entity.hasEffect(Effect.FIRE_RESISTANCE)) {
			entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.FIRE, 1))
		}
		val ev = EntityCombustByBlockEvent(this, entity, 8)
		if (entity is EntityArrow) {
			ev.setCancelled()
		}
		Server.instance!!.pluginManager.callEvent(ev)
		if (!ev.isCancelled && entity.isAlive && entity.noDamageTicks == 0) {
			entity.setOnFire(ev.duration)
		}
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_RANDOM) {
			if (!isBlockTopFacingSurfaceSolid(this.down()) && !canNeighborBurn()) {
				val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.AIR))
				level.server.pluginManager.callEvent(event)
				if (!event.isCancelled) {
					level.setBlock(this, event.newState, true)
				}
			}
			return Level.BLOCK_UPDATE_NORMAL
		} else if (type == Level.BLOCK_UPDATE_SCHEDULED && level.gameRules.getBoolean(GameRule.DO_FIRE_TICK)) {
			val forever = this.down().id == BlockID.Companion.NETHERRACK || this.down().id == BlockID.Companion.MAGMA
			val random = ThreadLocalRandom.current()

			//TODO: END
			if (!isBlockTopFacingSurfaceSolid(this.down()) && !canNeighborBurn()) {
				val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.AIR))
				level.server.pluginManager.callEvent(event)
				if (!event.isCancelled) {
					level.setBlock(this, event.newState, true)
				}
			}
			if (!forever && getLevel().isRaining &&
					(getLevel().canBlockSeeSky(this) ||
							getLevel().canBlockSeeSky(this.east()) ||
							getLevel().canBlockSeeSky(this.west()) ||
							getLevel().canBlockSeeSky(this.south()) ||
							getLevel().canBlockSeeSky(this.north()))) {
				val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.AIR))
				level.server.pluginManager.callEvent(event)
				if (!event.isCancelled) {
					level.setBlock(this, event.newState, true)
				}
			} else {
				val meta = this.damage
				if (meta < 15) {
					val newMeta = meta + random.nextInt(3)
					this.setDamage(Math.min(newMeta, 15))
					getLevel().setBlock(this, this, true)
				}
				getLevel().scheduleUpdate(this, tickRate() + random.nextInt(10))
				if (!forever && !canNeighborBurn()) {
					if (!isBlockTopFacingSurfaceSolid(this.down()) || meta > 3) {
						val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.AIR))
						level.server.pluginManager.callEvent(event)
						if (!event.isCancelled) {
							level.setBlock(this, event.newState, true)
						}
					}
				} else if (!forever && this.down().burnAbility <= 0 && meta == 15 && random.nextInt(4) == 0) {
					val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.AIR))
					level.server.pluginManager.callEvent(event)
					if (!event.isCancelled) {
						level.setBlock(this, event.newState, true)
					}
				} else {
					val o = 0

					//TODO: decrease the o if the rainfall values are high
					tryToCatchBlockOnFire(this.east(), 300 + o, meta)
					tryToCatchBlockOnFire(this.west(), 300 + o, meta)
					tryToCatchBlockOnFire(this.down(), 250 + o, meta)
					tryToCatchBlockOnFire(this.up(), 250 + o, meta)
					tryToCatchBlockOnFire(this.south(), 300 + o, meta)
					tryToCatchBlockOnFire(this.north(), 300 + o, meta)
					for (x in (x - 1).toInt()..(x + 1).toInt()) {
						for (z in (z - 1).toInt()..(z + 1).toInt()) {
							for (y in (y - 1).toInt()..(y + 4).toInt()) {
								if (x != this.x.toInt() || y != this.y.toInt() || z != this.z.toInt()) {
									var k = 100
									if (y > this.y + 1) {
										(k += (y - (this.y + 1)) * 100).toInt()
									}
									val block = getLevel().getBlock(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
									val chance = getChanceOfNeighborsEncouragingFire(block)
									if (chance > 0) {
										val t = (chance + 40 + getLevel().server.getDifficulty() * 7) / (meta + 30)

										//TODO: decrease the t if the rainfall values are high
										if (t > 0 && random.nextInt(k) <= t) {
											var damage = meta + random.nextInt(5) / 4
											if (damage > 15) {
												damage = 15
											}
											val e = BlockIgniteEvent(block, this, null, BlockIgniteEvent.BlockIgniteCause.SPREAD)
											level.server.pluginManager.callEvent(e)
											if (!e.isCancelled) {
												getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.FIRE, damage), true)
												getLevel().scheduleUpdate(block, tickRate())
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return 0
	}

	private fun tryToCatchBlockOnFire(block: Block?, bound: Int, damage: Int) {
		val burnAbility = block.getBurnAbility()
		val random: Random = ThreadLocalRandom.current()
		if (random.nextInt(bound) < burnAbility) {
			if (random.nextInt(damage + 10) < 5) {
				var meta = damage + random.nextInt(5) / 4
				if (meta > 15) {
					meta = 15
				}
				val e = BlockIgniteEvent(block, this, null, BlockIgniteEvent.BlockIgniteCause.SPREAD)
				level.server.pluginManager.callEvent(e)
				if (!e.isCancelled) {
					getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.FIRE, meta), true)
					getLevel().scheduleUpdate(block, tickRate())
				}
			} else {
				val ev = BlockBurnEvent(block)
				getLevel().server.pluginManager.callEvent(ev)
				if (!ev.isCancelled) {
					getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.AIR), true)
				}
			}
			if (block is BlockTNT) {
				block.prime()
			}
		}
	}

	private fun getChanceOfNeighborsEncouragingFire(block: Block): Int {
		return if (block.id != BlockID.Companion.AIR) {
			0
		} else {
			var chance = 0
			chance = Math.max(chance, block.east().burnChance)
			chance = Math.max(chance, block.west().burnChance)
			chance = Math.max(chance, block.down().burnChance)
			chance = Math.max(chance, block.up().burnChance)
			chance = Math.max(chance, block.south().burnChance)
			chance = Math.max(chance, block.north().burnChance)
			chance
		}
	}

	fun canNeighborBurn(): Boolean {
		for (face in BlockFace.values()) {
			if (this.getSide(face).burnChance > 0) {
				return true
			}
		}
		return false
	}

	fun isBlockTopFacingSurfaceSolid(block: Block?): Boolean {
		if (block != null) {
			if (block.isSolid) {
				return true
			} else {
				if (block is BlockStairs &&
						block.getDamage() and 4 == 4) {
					return true
				} else if (block is BlockSlab &&
						block.getDamage() and 8 == 8) {
					return true
				} else if (block is BlockSnowLayer &&
						block.getDamage() and 7 == 7) {
					return true
				}
			}
		}
		return false
	}

	override fun tickRate(): Int {
		return 30
	}

	override val color: BlockColor
		get() = BlockColor.LAVA_BLOCK_COLOR

	override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
		return this
	}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.AIR))
	}
}