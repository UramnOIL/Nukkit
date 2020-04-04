package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.event.entity.EntityDeathEvent
import cn.nukkit.item.Item
import cn.nukkit.lang.TextContainer

class PlayerDeathEvent(player: Player?, drops: Array<Item?>, var deathMessage: TextContainer, var experience: Int) : EntityDeathEvent(player, drops), Cancellable {
	var keepInventory = false
	var keepExperience = false

	constructor(player: Player?, drops: Array<Item?>, deathMessage: String?, experience: Int) : this(player, drops, TextContainer(deathMessage), experience) {}

	override var entity: Entity?
		get() = super.getEntity() as Player
		set(entity) {
			super.entity = entity
		}

	fun setDeathMessage(deathMessage: String?) {
		this.deathMessage = TextContainer(deathMessage)
	}

	companion object {
		val handlers = HandlerList()
	}

}