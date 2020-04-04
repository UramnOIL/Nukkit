package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.entity.Entity
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.nbt.tag.ByteTag
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class ItemTool @JvmOverloads constructor(id: Int, meta: Int? = 0, count: Int = 1, name: String = Item.Companion.UNKNOWN_STR) : Item(id, meta, count, name), ItemDurable {
	override val maxStackSize: Int
		get() = 1

	override fun useOn(block: Block): Boolean {
		if (isUnbreakable || isDurable) {
			return true
		}
		if (block.toolType == TYPE_PICKAXE && isPickaxe || block.toolType == TYPE_SHOVEL && isShovel || block.toolType == TYPE_AXE && isAxe || block.toolType == TYPE_SWORD && isSword || block.toolType == ItemID.Companion.SHEARS && isShears) {
			meta++
		} else if (!isShears && block.getBreakTime(this) > 0) {
			meta += 2
		} else if (isHoe) {
			if (block.id == BlockID.GRASS || block.id == BlockID.DIRT) {
				meta++
			}
		} else {
			meta++
		}
		return true
	}

	override fun useOn(entity: Entity?): Boolean {
		if (isUnbreakable || isDurable) {
			return true
		}
		if (entity != null && !isSword) {
			meta += 2
		} else {
			meta++
		}
		return true
	}

	private val isDurable: Boolean
		private get() {
			if (!hasEnchantments()) {
				return false
			}
			val durability = getEnchantment(Enchantment.Companion.ID_DURABILITY)
			return durability != null && durability.level > 0 && 100 / (durability.level + 1) <= Random().nextInt(100)
		}

	override val isUnbreakable: Boolean
		get() {
			val tag = getNamedTagEntry("Unbreakable")
			return tag is ByteTag && tag.data > 0
		}

	override val isPickaxe: Boolean
		get() = false

	override val isAxe: Boolean
		get() = false

	override val isSword: Boolean
		get() = false

	override val isShovel: Boolean
		get() = false

	override val isHoe: Boolean
		get() = false

	override val isShears: Boolean
		get() = id == ItemID.Companion.SHEARS

	override val isTool: Boolean
		get() = id == ItemID.Companion.FLINT_STEEL || id == ItemID.Companion.SHEARS || id == ItemID.Companion.BOW || isPickaxe || isAxe || isShovel || isSword || isHoe

	override val enchantAbility: Int
		get() {
			when (this.tier) {
				TIER_STONE -> return 5
				TIER_WOODEN -> return 15
				TIER_DIAMOND -> return 10
				TIER_GOLD -> return 22
				TIER_IRON -> return 14
			}
			return 0
		}

	companion object {
		const val TIER_WOODEN = 1
		const val TIER_GOLD = 2
		const val TIER_STONE = 3
		const val TIER_IRON = 4
		const val TIER_DIAMOND = 5
		const val TYPE_NONE = 0
		const val TYPE_SWORD = 1
		const val TYPE_SHOVEL = 2
		const val TYPE_PICKAXE = 3
		const val TYPE_AXE = 4
		const val TYPE_SHEARS = 5
		const val DURABILITY_WOODEN = 60
		const val DURABILITY_GOLD = 33
		const val DURABILITY_STONE = 132
		const val DURABILITY_IRON = 251
		const val DURABILITY_DIAMOND = 1562
		const val DURABILITY_FLINT_STEEL = 65
		const val DURABILITY_SHEARS = 239
		const val DURABILITY_BOW = 385
		const val DURABILITY_TRIDENT = 251
		const val DURABILITY_FISHING_ROD = 65
	}
}