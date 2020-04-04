package cn.nukkit.level

import cn.nukkit.Server
import cn.nukkit.level.generator.Generator

enum class EnumLevel {
	OVERWORLD, NETHER;

	//THE_END
	var level: Level? = null

	companion object {
		fun initLevels() {
			OVERWORLD.level = Server.instance!!.defaultLevel

			// attempt to load the nether world if it is allowed in server properties
			if (Server.instance!!.isNetherAllowed && !Server.instance!!.loadLevel("nether")) {

				// Nether is allowed, and not found, create the default nether world
				Server.instance!!.logger.info("No level called \"nether\" found, creating default nether level.")

				// Generate seed for nether and get nether generator
				val seed = System.currentTimeMillis()
				val generator = Generator.getGenerator("nether")

				// Generate the nether world
				Server.instance!!.generateLevel("nether", seed, generator)

				// Finally, load the level if not already loaded and set the level
				if (!Server.instance!!.isLevelLoaded("nether")) {
					Server.instance!!.loadLevel("nether")
				}
			}
			NETHER.level = Server.instance!!.getLevelByName("nether")
			if (NETHER.level == null) {
				// Nether is not found or disabled
				Server.instance!!.logger.alert("No level called \"nether\" found or nether is disabled in server properties! Nether functionality will be disabled.")
			}
		}

		fun getOtherNetherPair(current: Level): Level? {
			return if (current === OVERWORLD.level) {
				NETHER.level
			} else if (current === NETHER.level) {
				OVERWORLD.level
			} else {
				throw IllegalArgumentException("Neither overworld nor nether given!")
			}
		}

		fun moveToNether(current: Position): Position? {
			return if (NETHER.level == null) {
				null
			} else {
				if (current.level === OVERWORLD.level) {
					Position(mRound(current.floorX shr 3, 128).toDouble(), mRound(current.floorY, 32).toDouble(), mRound(current.floorZ shr 3, 128).toDouble(), NETHER.level)
				} else if (current.level === NETHER.level) {
					Position(mRound(current.floorX shl 3, 1024).toDouble(), mRound(current.floorY, 32).toDouble(), mRound(current.floorZ shl 3, 1024).toDouble(), OVERWORLD.level)
				} else {
					throw IllegalArgumentException("Neither overworld nor nether given!")
				}
			}
		}

		private fun mRound(value: Int, factor: Int): Int {
			return Math.round(value.toFloat() / factor) * factor
		}
	}
}