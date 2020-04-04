package cn.nukkit.event.vehicle

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class VehicleDamageEvent(vehicle: Entity, val attacker: Entity, var damage: Double) : VehicleEvent(vehicle), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}