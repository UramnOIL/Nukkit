package cn.nukkit.level

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.block.BlockTNT
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.entity.item.EntityXPOrb
import cn.nukkit.event.block.BlockUpdateEvent
import cn.nukkit.event.entity.EntityDamageByBlockEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityExplodeEvent
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.particle.HugeExplodeSeedParticle
import cn.nukkit.math.*
import cn.nukkit.math.NukkitMath.ceilDouble
import cn.nukkit.math.NukkitMath.floorDouble
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.utils.Hash
import it.unimi.dsi.fastutil.longs.LongArraySet
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * author: Angelic47
 * Nukkit Project
 */
class Explosion(center: Position, size: Double, what: Entity) {
	private val rays = 16 //Rays
	private val level: Level
	private val source: Position
	private val size: Double
	private var affectedBlocks: List<Block> = ArrayList()
	private val stepLen = 0.3
	private val what: Any

	/**
	 * @return bool
	 */
	@Deprecated("")
	fun explode(): Boolean {
		return if (explodeA()) {
			explodeB()
		} else false
	}

	/**
	 * @return bool
	 */
	fun explodeA(): Boolean {
		if (size < 0.1) {
			return false
		}
		val vector = Vector3(0, 0, 0)
		val vBlock = Vector3(0, 0, 0)
		val mRays = rays - 1
		for (i in 0 until rays) {
			for (j in 0 until rays) {
				for (k in 0 until rays) {
					if (i == 0 || i == mRays || j == 0 || j == mRays || k == 0 || k == mRays) {
						vector.setComponents(i.toDouble() / mRays.toDouble() * 2.0 - 1, j.toDouble() / mRays.toDouble() * 2.0 - 1, k.toDouble() / mRays.toDouble() * 2.0 - 1)
						val len = vector.length()
						vector.setComponents(vector.x / len * stepLen, vector.y / len * stepLen, vector.z / len * stepLen)
						var pointerX: Double = source.x
						var pointerY: Double = source.y
						var pointerZ: Double = source.z
						var blastForce = size * ThreadLocalRandom.current().nextInt(700, 1301) / 1000.0
						while (blastForce > 0) {
							val x = pointerX.toInt()
							val y = pointerY.toInt()
							val z = pointerZ.toInt()
							vBlock.x = if (pointerX >= x) x else x - 1
							vBlock.y = if (pointerY >= y) y else y - 1
							vBlock.z = if (pointerZ >= z) z else z - 1
							if (vBlock.y < 0 || vBlock.y > 255) {
								break
							}
							val block = level.getBlock(vBlock)
							if (block.id != 0) {
								blastForce -= (block.resistance / 5 + 0.3) * stepLen
								if (blastForce > 0) {
									if (!affectedBlocks.contains(block)) {
										affectedBlocks.add(block)
									}
								}
							}
							pointerX += vector.x
							pointerY += vector.y
							pointerZ += vector.z
							blastForce -= stepLen * 0.75
						}
					}
				}
			}
		}
		return true
	}

	fun explodeB(): Boolean {
		val updateBlocks = LongArraySet()
		val send: MutableList<Vector3> = ArrayList()
		val source = Vector3(source.x, source.y, source.z).floor()
		var yield = 1.0 / size * 100.0
		if (what is Entity) {
			val ev = EntityExplodeEvent(what, this.source, affectedBlocks, yield)
			level.server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return false
			} else {
				yield = ev.yield
				affectedBlocks = ev.blockList
			}
		}
		val explosionSize = size * 2.0
		val minX = floorDouble(this.source.x - explosionSize - 1).toDouble()
		val maxX = ceilDouble(this.source.x + explosionSize + 1).toDouble()
		val minY = floorDouble(this.source.y - explosionSize - 1).toDouble()
		val maxY = ceilDouble(this.source.y + explosionSize + 1).toDouble()
		val minZ = floorDouble(this.source.z - explosionSize - 1).toDouble()
		val maxZ = ceilDouble(this.source.z + explosionSize + 1).toDouble()
		val explosionBB: AxisAlignedBB = SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
		val list = level.getNearbyEntities(explosionBB, if (what is Entity) what else null)
		for (entity in list) {
			val distance = entity.distance(this.source) / explosionSize
			if (distance <= 1) {
				val motion = entity.subtract(this.source)!!.normalize()
				val exposure = 1
				val impact = (1 - distance) * exposure
				val damage = ((impact * impact + impact) / 2 * 8 * explosionSize + 1).toInt()
				if (what is Entity) {
					entity.attack(EntityDamageByEntityEvent(what, entity, DamageCause.ENTITY_EXPLOSION, damage))
				} else if (what is Block) {
					entity.attack(EntityDamageByBlockEvent(what, entity, DamageCause.BLOCK_EXPLOSION, damage.toFloat()))
				} else {
					entity.attack(EntityDamageEvent(entity, DamageCause.BLOCK_EXPLOSION, damage))
				}
				if (!(entity is EntityItem || entity is EntityXPOrb)) {
					entity.setMotion(motion.multiply(impact)!!)
				}
			}
		}
		val air = ItemBlock(get(BlockID.AIR))

		//Iterator iter = this.affectedBlocks.entrySet().iterator();
		for (block in affectedBlocks) {
			//Block block = (Block) ((HashMap.Entry) iter.next()).getValue();
			if (block.id == Block.TNT) {
				(block as BlockTNT).prime(NukkitRandom().nextRange(10, 30), if (what is Entity) what else null)
			} else if (block.id == Block.CHEST || block.id == Block.TRAPPED_CHEST) {
				val chest = block.getLevel().getBlockEntity(block)
				if (chest != null) {
					for (drop in (chest as BlockEntityChest).getInventory().getContents()!!.values) {
						level.dropItem(block.add(0.5, 0.5, 0.5), drop)
					}
					chest.getInventory().clearAll()
				}
			} else if (Math.random() * 100 < yield) {
				for (drop in block.getDrops(air)) {
					level.dropItem(block.add(0.5, 0.5, 0.5), drop)
				}
			}
			level.setBlockAt(block.x as Int, block.y as Int, block.z as Int, BlockID.AIR)
			val pos = Vector3(block.x, block.y, block.z)
			for (side in BlockFace.values()) {
				val sideBlock = pos.getSide(side)
				val index = Hash.hashBlock(sideBlock.x as Int, sideBlock.y as Int, sideBlock.z as Int)
				if (!affectedBlocks.contains(sideBlock) && !updateBlocks.contains(index)) {
					val ev = BlockUpdateEvent(level.getBlock(sideBlock))
					level.server.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						ev.block!!.onUpdate(Level.BLOCK_UPDATE_NORMAL)
					}
					updateBlocks.add(index)
				}
			}
			send.add(Vector3(block.x - source.x, block.y - source.y, block.z - source.z))
		}
		level.addParticle(HugeExplodeSeedParticle(this.source))
		level.addLevelSoundEvent(source, LevelSoundEventPacket.SOUND_EXPLODE)
		return true
	}

	init {
		level = center.getLevel()
		source = center
		this.size = Math.max(size, 0.0)
		this.what = what
	}
}