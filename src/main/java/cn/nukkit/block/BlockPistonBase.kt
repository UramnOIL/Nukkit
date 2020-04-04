package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityPistonArm
import cn.nukkit.event.block.BlockPistonChangeEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.utils.Faceable
import java.util.*

/**
 * @author CreeperFace
 */
abstract class BlockPistonBase @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	var sticky = false
	override val resistance: Double
		get() = 2.5

	override val hardness: Double
		get() = 0.5

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (Math.abs(player!!.x - x) < 2 && Math.abs(player.z - z) < 2) {
			val y = player.y + player.eyeHeight
			if (y - this.y > 2) {
				this.setDamage(BlockFace.UP.index)
			} else if (this.y - y > 0) {
				this.setDamage(BlockFace.DOWN.index)
			} else {
				this.setDamage(player.horizontalFacing.index)
			}
		} else {
			this.setDamage(player.horizontalFacing.index)
		}
		level.setBlock(block, this, true, false)
		val nbt = CompoundTag("")
				.putString("id", BlockEntity.PISTON_ARM)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
				.putBoolean("Sticky", sticky)
		val be = BlockEntity.createBlockEntity(BlockEntity.PISTON_ARM, level.getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityPistonArm
				?: return false
		//this.checkState();
		return true
	}

	override fun onBreak(item: Item): Boolean {
		level.setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		val block = this.getSide(facing)
		if (block is BlockPistonHead && block.facing == facing) {
			block.onBreak(item)
		}
		return true
	}

	val isExtended: Boolean
		get() {
			val face = facing
			val block = getSide(face)
			return block is BlockPistonHead && block.facing == face
		}

	override fun onUpdate(type: Int): Int {
		return if (type != 6 && type != 1) {
			0
		} else {
			val blockEntity = level.getBlockEntity(this)
			if (blockEntity is BlockEntityPistonArm) {
				val arm = blockEntity
				val powered = isPowered
				if (arm.powered != powered) {
					level.server.pluginManager.callEvent(BlockPistonChangeEvent(this, if (powered) 0 else 15, if (powered) 15 else 0))
					arm.powered = !arm.powered
					if (arm.chunk != null) {
						arm.chunk.setChanged()
					}
				}
			}
			type
		}
	}

	private fun checkState() {
		val facing = facing
		val isPowered = isPowered
		if (isPowered && !isExtended) {
			if (BlocksCalculator(level, this, facing, true).canMove()) {
				if (!doMove(true)) {
					return
				}
				getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_PISTON_OUT)
			} else {
			}
		} else if (!isPowered && isExtended) {
			//this.level.setBlock() TODO: set piston extension?
			if (sticky) {
				val pos: Vector3 = this.add(facing.xOffset * 2.toDouble(), facing.yOffset * 2.toDouble(), facing.zOffset * 2.toDouble())
				val block = level.getBlock(pos)
				if (block.id == BlockID.Companion.AIR) {
					level.setBlock(this.location.getSide(facing), Block.Companion.get(BlockID.Companion.AIR), true, true)
				}
				if (canPush(block, facing.opposite, false) && (block !is BlockFlowable || block.getId() == BlockID.Companion.PISTON || block.getId() == BlockID.Companion.STICKY_PISTON)) {
					doMove(false)
				}
			} else {
				level.setBlock(location.getSide(facing), Block.Companion.get(BlockID.Companion.AIR), true, false)
			}
			getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_PISTON_IN)
		}
	}

	val facing: BlockFace
		get() = BlockFace.fromIndex(this.damage).opposite

	private val isPowered: Boolean
		private get() {
			val face = facing
			for (side in BlockFace.values()) {
				if (side != face && level.isSidePowered(this.location.getSide(side), side)) {
					return true
				}
			}
			return if (level.isSidePowered(this, BlockFace.DOWN)) {
				true
			} else {
				val pos = this.location.up()
				for (side in BlockFace.values()) {
					if (side != BlockFace.DOWN && level.isSidePowered(pos.getSide(side), side)) {
						return true
					}
				}
				false
			}
		}

	private fun doMove(extending: Boolean): Boolean {
		val pos: Vector3 = this.location
		val direction = facing
		if (!extending) {
			level.setBlock(pos.getSide(direction), Block.Companion.get(BlockID.Companion.AIR), true, false)
		}
		val calculator = BlocksCalculator(level, this, direction, extending)
		return if (!calculator.canMove()) {
			false
		} else {
			val blocks = calculator.blocksToMove
			val newBlocks: List<Block?> = ArrayList(blocks)
			val destroyBlocks = calculator.blocksToDestroy
			val side = if (extending) direction else direction.opposite
			for (i in destroyBlocks.indices.reversed()) {
				val block = destroyBlocks[i]
				level.useBreakOn(block)
			}
			for (i in blocks.indices.reversed()) {
				val block = blocks[i]
				level.setBlock(block, Block.Companion.get(BlockID.Companion.AIR))
				val newPos: Vector3 = block!!.location.getSide(side)

				//TODO: change this to block entity
				level.setBlock(newPos, newBlocks[i])
			}
			val pistonHead = pos.getSide(direction)
			if (extending) {
				//extension block entity
				level.setBlock(pistonHead, Block.Companion.get(BlockID.Companion.PISTON_HEAD, this.damage))
			}
			true
		}
	}

	inner class BlocksCalculator(private val level: Level, pos: Block, facing: BlockFace, extending: Boolean) {
		private val pistonPos: Vector3
		private var blockToMove: Block? = null
		private var moveDirection: BlockFace? = null
		private val toMove: MutableList<Block?> = ArrayList()
		private val toDestroy: MutableList<Block?> = ArrayList()
		fun canMove(): Boolean {
			toMove.clear()
			toDestroy.clear()
			val block = blockToMove
			return if (!canPush(block, moveDirection, false)) {
				if (block is BlockFlowable) {
					toDestroy.add(blockToMove)
					true
				} else {
					false
				}
			} else if (!addBlockLine(blockToMove)) {
				false
			} else {
				for (b in toMove) {
					if (b.getId() == BlockID.Companion.SLIME_BLOCK && !addBranchingBlocks(b)) {
						return false
					}
				}
				true
			}
		}

		private fun addBlockLine(origin: Block?): Boolean {
			var block: Block? = origin!!.clone()
			if (block.getId() == BlockID.Companion.AIR) {
				return true
			} else if (!canPush(origin, moveDirection, false)) {
				return true
			} else if (origin == pistonPos) {
				return true
			} else if (toMove.contains(origin)) {
				return true
			} else {
				var count = 1
				if (count + toMove.size > 12) {
					return false
				} else {
					while (block.getId() == BlockID.Companion.SLIME_BLOCK) {
						block = origin.getSide(moveDirection!!.opposite, count)
						if (block.id == BlockID.Companion.AIR || !canPush(block, moveDirection, false) || block == pistonPos) {
							break
						}
						++count
						if (count + toMove.size > 12) {
							return false
						}
					}
					var blockCount = 0
					for (step in count - 1 downTo 0) {
						toMove.add(block!!.getSide(moveDirection!!.opposite, step))
						++blockCount
					}
					var steps = 1
					while (true) {
						val nextBlock = block!!.getSide(moveDirection!!, steps)
						val index = toMove.indexOf(nextBlock)
						if (index > -1) {
							reorderListAtCollision(blockCount, index)
							for (l in 0..index + blockCount) {
								val b = toMove[l]
								if (b.getId() == BlockID.Companion.SLIME_BLOCK && !addBranchingBlocks(b)) {
									return false
								}
							}
							return true
						}
						if (nextBlock.id == BlockID.Companion.AIR) {
							return true
						}
						if (!canPush(nextBlock, moveDirection, true) || nextBlock == pistonPos) {
							return false
						}
						if (nextBlock is BlockFlowable) {
							toDestroy.add(nextBlock)
							return true
						}
						if (toMove.size >= 12) {
							return false
						}
						toMove.add(nextBlock)
						++blockCount
						++steps
					}
				}
			}
		}

		private fun reorderListAtCollision(count: Int, index: Int) {
			val list: List<Block?> = ArrayList(toMove.subList(0, index))
			val list1: List<Block?> = ArrayList(toMove.subList(toMove.size - count, toMove.size))
			val list2: List<Block?> = ArrayList(toMove.subList(index, toMove.size - count))
			toMove.clear()
			toMove.addAll(list)
			toMove.addAll(list1)
			toMove.addAll(list2)
		}

		private fun addBranchingBlocks(block: Block?): Boolean {
			for (face in BlockFace.values()) {
				if (face.axis != moveDirection!!.axis && !addBlockLine(block!!.getSide(face))) {
					return false
				}
			}
			return true
		}

		val blocksToMove: List<Block?>
			get() = toMove

		val blocksToDestroy: List<Block?>
			get() = toDestroy

		init {
			pistonPos = pos.location
			if (extending) {
				moveDirection = facing
				blockToMove = pos.getSide(facing)
			} else {
				moveDirection = facing.opposite
				blockToMove = pos.getSide(facing, 2)
			}
		}
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}

	companion object {
		fun canPush(block: Block?, face: BlockFace?, destroyBlocks: Boolean): Boolean {
			if (block!!.canBePushed() && block.getY() >= 0 && (face != BlockFace.DOWN || block.getY() != 0.0) && block.getY() <= 255 && (face != BlockFace.UP || block.getY() != 255.0)) {
				if (block !is BlockPistonBase) {
					if (block is BlockFlowable) {
						return destroyBlocks
					}
				} else return !block.isExtended
				return true
			}
			return false
		}
	}
}