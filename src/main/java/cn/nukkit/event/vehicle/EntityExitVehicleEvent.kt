package cn.nukkit.event.vehicle

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class EntityExitVehicleEvent(val entity: Entity, vehicle: Entity) : VehicleEvent(vehicle), Cancellable {

	val isPlayer: Boolean
		get() = entity is Player

	companion object {
		val handlers = HandlerList()
	}

}