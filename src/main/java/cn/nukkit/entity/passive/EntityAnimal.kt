package cn.nukkit.entity.passive

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityAgeable
import cn.nukkit.entity.EntityCreature
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityAnimal(chunk: FullChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt), EntityAgeable {
	override val isBaby: Boolean
		get() = getDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_BABY)

	open fun isBreedingItem(item: Item): Boolean {
		return item.id == Item.WHEAT //default
	}

	override fun onInteract(player: Player, item: Item, clickedPos: Vector3?): Boolean {
		if (item.id == Item.NAME_TAG) {
			if (item.hasCustomName()) {
				this.nameTag = item.customName
				this.isNameTagVisible = true
				player.inventory.removeItem(item)
				return true
			}
		}
		return false
	}
}