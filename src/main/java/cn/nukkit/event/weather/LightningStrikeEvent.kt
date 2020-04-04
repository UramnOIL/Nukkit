package cn.nukkit.event.weather

import cn.nukkit.entity.weather.EntityLightningStrike
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.event.level.WeatherEvent
import cn.nukkit.level.Level

/**
 * author: funcraft
 * Nukkit Project
 */
class LightningStrikeEvent(level: Level,
						   /**
							* Gets the bolt which is striking the earth.
							* @return lightning entity
							*/
						   val lightning: EntityLightningStrike) : WeatherEvent(level), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}