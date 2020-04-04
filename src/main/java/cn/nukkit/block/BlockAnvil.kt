package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.inventory.AnvilInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Sound
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created by Pub4Game on 27.12.2015.
 */
class BlockAnvil @JvmOverloads constructor(override var damage: Int = 0) : BlockFallable(), Faceable {
	override val fullId: Int
		get() = (id shl 4) + damage

	override val id: Int
		get() = BlockID.Companion.ANVIL

	override fun canBeActivated(): Boolean {
		return true
	}

	override val isTransparent: Boolean
		get() = true

	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 6000

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = NAMES[if (damage > 11) 0 else damage]

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (!target.isTransparent || target.id == BlockID.Companion.SNOW_LAYER) {
			val damage = damage
			val faces = intArrayOf(1, 2, 3, 0)
			this.damage = faces[player?.direction?.horizontalIndex ?: 0]
			if (damage >= 4 && damage <= 7) {
				this.damage = this.damage or 0x04
			} else if (damage >= 8 && damage <= 11) {
				this.damage = this.damage or 0x08
			}
			getLevel().setBlock(block, this, true)
			getLevel().addSound(this, Sound.RANDOM_ANVIL_LAND, 1f, 0.8f)
			return true
		}
		return false
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		player?.addWindow(AnvilInventory(player.uIInventory, this), Player.ANVIL_WINDOW_ID)
		return true
	}

	override fun toItem(): Item? {
		val damage = damage
		return if (damage >= 4 && damage <= 7) {
			ItemBlock(this, this.damage and 0x04)
		} else if (damage >= 8 && damage <= 11) {
			ItemBlock(this, this.damage and 0x08)
		} else {
			ItemBlock(this)
		}
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else arrayOfNulls(0)
	}

	override val color: BlockColor
		get() = BlockColor.IRON_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(damage and 0x7)
	}

	companion object {
		private val NAMES = arrayOf(
				"Anvil",
				"Anvil",
				"Anvil",
				"Anvil",
				"Slighty Damaged Anvil",
				"Slighty Damaged Anvil",
				"Slighty Damaged Anvil",
				"Slighty Damaged Anvil",
				"Very Damaged Anvil",
				"Very Damaged Anvil",
				"Very Damaged Anvil",
				"Very Damaged Anvil"
		)
	}

}