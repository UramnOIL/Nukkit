package cn.nukkit.entity.mob

import cn.nukkit.Player
import cn.nukkit.entity.EntityCreature
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityMob(chunk: FullChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt) {
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