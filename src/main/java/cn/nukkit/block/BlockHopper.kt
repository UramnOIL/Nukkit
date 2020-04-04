package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityHopper
import cn.nukkit.inventory.ContainerInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemHopper
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.Faceable

/**
 * @author CreeperFace
 */
class BlockHopper @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.HOPPER_BLOCK

	override val name: String
		get() = "Hopper Block"

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 24

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		var facing = face.opposite
		if (facing == BlockFace.UP) {
			facing = BlockFace.DOWN
		}
		this.setDamage(facing.index)
		val powered = level.isBlockPowered(this.location)
		if (powered == isEnabled) {
			this.enabled = !powered
		}
		level.setBlock(this, this)
		val nbt = CompoundTag()
				.putList(ListTag("Items"))
				.putString("id", BlockEntity.HOPPER)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
		val hopper = BlockEntity.createBlockEntity(BlockEntity.HOPPER, level.getChunk(this.floorX shr 4, this.floorZ shr 4), nbt) as BlockEntityHopper
		return hopper != null
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		val blockEntity = level.getBlockEntity(this)
		return if (blockEntity is BlockEntityHopper) {
			player!!.addWindow(blockEntity.inventory) != -1
		} else false
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val comparatorInputOverride: Int
		get() {
			val blockEntity = level.getBlockEntity(this)
			return if (blockEntity is BlockEntityHopper) {
				ContainerInventory.calculateRedstone(blockEntity.inventory)
			} else super.getComparatorInputOverride()
		}

	val facing: BlockFace
		get() = BlockFace.fromIndex(this.damage and 7)

	var isEnabled: Boolean
		get() = this.damage and 0x08 != 8
		set(enabled) {
			if (isEnabled != enabled) {
				this.setDamage(this.damage xor 0x08)
			}
		}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val powered = level.isBlockPowered(this.location)
			if (powered == isEnabled) {
				this.enabled = !powered
				level.setBlock(this, this, true, false)
			}
			return type
		}
		return 0
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(toItem())
		} else arrayOfNulls(0)
	}

	override fun toItem(): Item? {
		return ItemHopper()
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}