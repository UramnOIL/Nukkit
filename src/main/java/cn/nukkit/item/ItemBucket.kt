package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.block.*
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.event.player.PlayerBucketEmptyEvent
import cn.nukkit.event.player.PlayerBucketFillEvent
import cn.nukkit.event.player.PlayerItemConsumeEvent
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.BlockFace.Plane
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.network.protocol.UpdateBlockPacket

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBucket @JvmOverloads constructor(meta: Int = 0, count: Int = 1) : Item(ItemID.Companion.BUCKET, meta, count, getName(meta)) {
	override val maxStackSize: Int
		get() = if (meta == 0) 16 else 1

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(level: Level, player: Player, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double): Boolean {
		val targetBlock = Block[getDamageByTarget(meta)]
		if (targetBlock is BlockAir) {
			if (target is BlockLiquid && target.damage == 0) {
				val result = get(ItemID.Companion.BUCKET, getDamageByTarget(target.id), 1)
				var ev: PlayerBucketFillEvent
				player.getServer().pluginManager.callEvent(PlayerBucketFillEvent(player, block, face, this, result!!).also { ev = it })
				if (!ev.isCancelled) {
					player.level.setBlock(target, Block[BlockID.AIR], true, true)

					// When water is removed ensure any adjacent still water is
					// replaced with water that can flow.
					for (side in Plane.HORIZONTAL) {
						val b = target.getSide(side!!)
						if (b.id == BlockID.STILL_WATER) {
							level.setBlock(b, Block[BlockID.WATER])
						}
					}
					if (player.isSurvival) {
						val clone = clone()
						clone.setCount(getCount() - 1)
						player.getInventory().setItemInHand(clone)
						player.getInventory().addItem(ev.item)
					}
					if (target is BlockLava) {
						level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_BUCKET_FILL_LAVA)
					} else {
						level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_BUCKET_FILL_WATER)
					}
					return true
				} else {
					player.getInventory().sendContents(player)
				}
			}
		} else if (targetBlock is BlockLiquid) {
			val result = get(ItemID.Companion.BUCKET, 0, 1)
			val ev = PlayerBucketEmptyEvent(player, block, face, this, result!!)
			ev.isCancelled = !block.canBeFlowedInto()
			if (player.level.dimension == Level.DIMENSION_NETHER && this.damage != 10) {
				ev.isCancelled = true
			}
			player.getServer().pluginManager.callEvent(ev)
			if (!ev.isCancelled) {
				player.level.setBlock(block, targetBlock, true, true)
				if (player.isSurvival) {
					val clone = clone()
					clone.setCount(getCount() - 1)
					player.getInventory().setItemInHand(clone)
					player.getInventory().addItem(ev.item)
				}
				if (this.damage == 10) {
					level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_BUCKET_EMPTY_LAVA)
				} else {
					level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_BUCKET_EMPTY_WATER)
				}
				return true
			} else {
				player.level.sendBlocks(arrayOf(player), arrayOf(get(Block.AIR, 0, block)), UpdateBlockPacket.FLAG_ALL_PRIORITY, 1)
				player.getInventory().sendContents(player)
			}
		}
		return false
	}

	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		return this.damage == 1 // Milk
	}

	override fun onUse(player: Player, ticksUsed: Int): Boolean {
		val consumeEvent = PlayerItemConsumeEvent(player, this)
		player.getServer().pluginManager.callEvent(consumeEvent)
		if (consumeEvent.isCancelled) {
			player.getInventory().sendContents(player)
			return false
		}
		if (player.isSurvival) {
			count--
			player.getInventory().setItemInHand(this)
			player.getInventory().addItem(ItemBucket())
		}
		player.removeAllEffects()
		return true
	}

	companion object {
		protected fun getName(meta: Int): String {
			return when (meta) {
				1 -> "Milk"
				2 -> "Bucket of Cod"
				3 -> "Bucket of Salmon"
				4 -> "Bucket of Tropical Fish"
				5 -> "Bucket of Pufferfish"
				8 -> "Water Bucket"
				10 -> "Lava Bucket"
				else -> "Bucket"
			}
		}

		@kotlin.jvm.JvmStatic
		fun getDamageByTarget(target: Int): Int {
			return when (target) {
				2, 3, 4, 5, 8, 9 -> 8
				10, 11 -> 10
				else -> 0
			}
		}
	}
}