package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageByBlockEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.potion.Effect
import cn.nukkit.utils.BlockColor

class BlockMagma : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.MAGMA

	override val name: String
		get() = "Magma Block"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 30

	override val lightLevel: Int
		get() = 3

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun onEntityCollide(entity: Entity) {
		if (!entity.hasEffect(Effect.FIRE_RESISTANCE)) {
			if (entity is Player) {
				val p = entity
				if (!p.isCreative && !p.isSpectator && !p.isSneaking) {
					entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.LAVA, 1))
				}
			} else {
				entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.LAVA, 1))
			}
		}
	}

	override val color: BlockColor
		get() = BlockColor.NETHERRACK_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}