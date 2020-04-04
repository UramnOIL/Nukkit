package cn.nukkit.event.vehicle

import cn.nukkit.entity.Entity
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Location

class VehicleMoveEvent(vehicle: Entity, val from: Location, val to: Location) : VehicleEvent(vehicle) {

	companion object {
		val handlers = HandlerList()
	}

}