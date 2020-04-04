package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.utils.Faceable

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * Created by CreeperFace on 15.4.2017.
 */
class BlockDispenser @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val name: String
		get() = "Dispenser"

	override val id: Int
		get() = BlockID.Companion.DISPENSER

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	/*BlockEntity blockEntity = this.level.getBlockEntity(this);

        if(blockEntity instanceof BlockEntityDispenser) {
            //return ContainerInventory.calculateRedstone(((BlockEntityDispenser) blockEntity).getInventory()); TODO: dispenser
        }*/
	override val comparatorInputOverride: Int
		get() =/*BlockEntity blockEntity = this.level.getBlockEntity(this);

        if(blockEntity instanceof BlockEntityDispenser) {
            //return ContainerInventory.calculateRedstone(((BlockEntityDispenser) blockEntity).getInventory()); TODO: dispenser
        }*/
			super.getComparatorInputOverride()

	val facing: BlockFace
		get() = BlockFace.fromIndex(this.damage and 7)

	var isTriggered: Boolean
		get() = this.damage and 8 > 0
		set(value) {
			var i = 0
			i = i or facing.index
			if (value) {
				i = i or 8
			}
			this.setDamage(i)
		}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	val dispensePosition: Vector3
		get() {
			val facing = facing
			val x = getX() + 0.7 * facing.xOffset
			val y = getY() + 0.7 * facing.yOffset
			val z = getZ() + 0.7 * facing.zOffset
			return Vector3(x, y, z)
		}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}