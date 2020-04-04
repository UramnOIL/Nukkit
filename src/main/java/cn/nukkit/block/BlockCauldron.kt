package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityCauldron
import cn.nukkit.event.player.PlayerBucketEmptyEvent
import cn.nukkit.event.player.PlayerBucketFillEvent
import cn.nukkit.item.*
import cn.nukkit.level.Sound
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * author: CreeperFace
 * Nukkit Project
 */
class BlockCauldron : BlockSolidMeta {
	constructor() : super(0) {}
	constructor(meta: Int) : super(meta) {}

	override val id: Int
		get() = BlockID.Companion.CAULDRON_BLOCK

	override val name: String
		get() = "Cauldron Block"

	override val resistance: Double
		get() = 10

	override val hardness: Double
		get() = 2

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun canBeActivated(): Boolean {
		return true
	}

	val isFull: Boolean
		get() = this.damage == 0x06

	val isEmpty: Boolean
		get() = this.damage == 0x00

	override fun onActivate(item: Item, player: Player?): Boolean {
		val be = level.getBlockEntity(this) as? BlockEntityCauldron ?: return false
		val cauldron = be
		when (item.id) {
			Item.BUCKET -> if (item.damage == 0) { //empty bucket
				if (!isFull || cauldron.isCustomColor || cauldron.hasPotion()) {
					break
				}
				val bucket = item.clone() as ItemBucket
				bucket.setCount(1)
				bucket.setDamage(8) //water bucket
				val ev = PlayerBucketFillEvent(player, this, null, item, bucket)
				level.server.pluginManager.callEvent(ev)
				if (!ev.isCancelled) {
					replaceBucket(item, player, ev.item)
					this.setDamage(0) //empty
					level.setBlock(this, this, true)
					cauldron.clearCustomColor()
					getLevel().addSound(this.add(0.5, 1.0, 0.5), Sound.CAULDRON_TAKEWATER)
				}
			} else if (item.damage == 8) { //water bucket
				if (isFull && !cauldron.isCustomColor && !cauldron.hasPotion()) {
					break
				}
				val bucket = item.clone() as ItemBucket
				bucket.setCount(1)
				bucket.setDamage(0) //empty bucket
				val ev = PlayerBucketEmptyEvent(player, this, null, item, bucket)
				level.server.pluginManager.callEvent(ev)
				if (!ev.isCancelled) {
					replaceBucket(item, player, ev.item)
					if (cauldron.hasPotion()) { //if has potion
						this.setDamage(0) //empty
						cauldron.potionId = 0xffff //reset potion
						cauldron.isSplashPotion = false
						cauldron.clearCustomColor()
						level.setBlock(this, this, true)
						level.addSound(this.add(0.5, 0.0, 0.5), Sound.CAULDRON_EXPLODE)
					} else {
						this.setDamage(6) //fill
						cauldron.clearCustomColor()
						level.setBlock(this, this, true)
						getLevel().addLevelSoundEvent(this.add(0.5, 1.0, 0.5), LevelSoundEventPacket.SOUND_BUCKET_FILL_WATER)
					}
					//this.update();
				}
			}
			Item.DYE -> {
			}
			Item.LEATHER_CAP, Item.LEATHER_TUNIC, Item.LEATHER_PANTS, Item.LEATHER_BOOTS -> {
			}
			Item.POTION -> {
				if (isFull) {
					break
				}
				this.setDamage(this.damage + 1)
				if (this.damage > 0x06) this.setDamage(0x06)
				if (item.getCount() == 1) {
					player!!.inventory.itemInHand = ItemBlock(Block.Companion.get(BlockID.Companion.AIR))
				} else if (item.getCount() > 1) {
					item.setCount(item.getCount() - 1)
					player!!.inventory.itemInHand = item
					val bottle: Item = ItemGlassBottle()
					if (player.inventory.canAddItem(bottle)) {
						player.inventory.addItem(bottle)
					} else {
						player.level.dropItem(player.add(0.0, 1.3, 0.0), bottle, player.directionVector.multiply(0.4))
					}
				}
				level.addSound(this.add(0.5, 0.5, 0.5), Sound.CAULDRON_FILLPOTION)
			}
			Item.GLASS_BOTTLE -> {
				if (isEmpty) {
					break
				}
				this.setDamage(this.damage - 1)
				if (this.damage < 0x00) this.setDamage(0x00)
				if (item.getCount() == 1) {
					player!!.inventory.itemInHand = ItemPotion()
				} else if (item.getCount() > 1) {
					item.setCount(item.getCount() - 1)
					player!!.inventory.itemInHand = item
					val potion: Item = ItemPotion()
					if (player.inventory.canAddItem(potion)) {
						player.inventory.addItem(potion)
					} else {
						player.level.dropItem(player.add(0.0, 1.3, 0.0), potion, player.directionVector.multiply(0.4))
					}
				}
				level.addSound(this.add(0.5, 0.5, 0.5), Sound.CAULDRON_TAKEPOTION)
			}
			else -> return true
		}
		level.updateComparatorOutputLevel(this)
		return true
	}

	protected fun replaceBucket(oldBucket: Item, player: Player?, newBucket: Item?) {
		if (player!!.isSurvival || player.isAdventure) {
			if (oldBucket.getCount() == 1) {
				player.inventory.itemInHand = newBucket
			} else {
				oldBucket.setCount(oldBucket.getCount() - 1)
				if (player.inventory.canAddItem(newBucket)) {
					player.inventory.addItem(newBucket)
				} else {
					player.level.dropItem(player.add(0.0, 1.3, 0.0), newBucket, player.directionVector.multiply(0.4))
				}
			}
		}
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val nbt = CompoundTag("")
				.putString("id", BlockEntity.CAULDRON)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
				.putShort("PotionId", 0xffff)
				.putByte("SplashPotion", 0)
		if (item.hasCustomBlockData()) {
			val customData = item.customBlockData.tags
			for ((key, value) in customData) {
				nbt.put(key, value)
			}
		}
		val cauldron = BlockEntity.createBlockEntity(BlockEntity.CAULDRON, level.getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityCauldron
				?: return false
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(ItemCauldron())
		} else arrayOfNulls(0)
	}

	override fun toItem(): Item? {
		return ItemCauldron()
	}

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val comparatorInputOverride: Int
		get() = this.damage

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}