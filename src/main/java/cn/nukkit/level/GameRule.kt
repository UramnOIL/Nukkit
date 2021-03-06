package cn.nukkit.level

enum class GameRule(val ruleName: String) {
	COMMAND_BLOCK_OUTPUT("commandBlockOutput"),
	DO_DAYLIGHT_CYCLE("doDaylightCycle"),
	DO_ENTITY_DROPS("doEntityDrops"),
	DO_FIRE_TICK("doFireTick"),
	DO_IMMEDIATE_RESPAWN("doImmediateRespawn"),
	DO_MOB_LOOT("doMobLoot"),
	DO_MOB_SPAWNING("doMobSpawning"),
	DO_TILE_DROPS("doTileDrops"),
	DO_WEATHER_CYCLE("doWeatherCycle"),
	DROWNING_DAMAGE("drowningDamage"),
	FALL_DAMAGE("fallDamage"),
	FIRE_DAMAGE("fireDamage"),
	KEEP_INVENTORY("keepInventory"),
	MOB_GRIEFING("mobGriefing"),
	NATURAL_REGENERATION("naturalRegeneration"),
	PVP("pvp"),
	RANDOM_TICK_SPEED("randomTickSpeed"),
	SEND_COMMAND_FEEDBACK("sendCommandFeedback"),
	SHOW_COORDINATES("showCoordinates"),
	TNT_EXPLODES("tntExplodes"),
	SHOW_DEATH_MESSAGE("showDeathMessage");

	companion object {
		@JvmStatic
		fun parseString(gameRuleString: String): GameRule? = values().singleOrNull {it.ruleName == gameRuleString}

		val names: Array<String> = arrayOf(*values().map { it.ruleName }.toTypedArray())
	}

}