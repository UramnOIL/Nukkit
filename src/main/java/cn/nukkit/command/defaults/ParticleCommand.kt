package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.item.Item
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.Position
import cn.nukkit.level.particle.*
import cn.nukkit.math.Vector3
import java.util.*

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class ParticleCommand(name: String) : VanillaCommand(name, "%nukkit.command.particle.description", "%nukkit.command.particle.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size < 4) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		val defaultPosition: Position
		defaultPosition = if (sender is Player) {
			sender.position
		} else {
			Position(0, 0, 0, sender.server.defaultLevel)
		}
		val name = args[0]!!.toLowerCase()
		val x: Double
		val y: Double
		val z: Double
		try {
			x = getDouble(args[1], defaultPosition.getX())
			y = getDouble(args[2], defaultPosition.getY())
			z = getDouble(args[3], defaultPosition.getZ())
		} catch (e: Exception) {
			return false
		}
		val position = Position(x, y, z, defaultPosition.getLevel())
		var count = 1
		if (args.size > 4) {
			try {
				val c = java.lang.Double.valueOf(args[4])
				count = c.toInt()
			} catch (e: Exception) {
				//ignore
			}
		}
		count = Math.max(1, count)
		var data = -1
		if (args.size > 5) {
			try {
				val d = java.lang.Double.valueOf(args[5])
				data = d.toInt()
			} catch (e: Exception) {
				//ignore
			}
		}
		val particle = getParticle(name, position, data)
		if (particle == null) {
			position.level.addParticleEffect(position.asVector3f(), args[0], -1, position.level.dimension)
			return true
		}
		sender.sendMessage(TranslationContainer("commands.particle.success", name, count.toString()))
		val random = Random(System.currentTimeMillis())
		for (i in 0 until count) {
			particle.setComponents(
					position.x + (random.nextFloat() * 2 - 1),
					position.y + (random.nextFloat() * 2 - 1),
					position.z + (random.nextFloat() * 2 - 1)
			)
			position.getLevel().addParticle(particle)
		}
		return true
	}

	private fun getParticle(name: String, pos: Vector3, data: Int): Particle? {
		when (name) {
			"explode" -> return ExplodeParticle(pos)
			"hugeexplosion" -> return HugeExplodeParticle(pos)
			"hugeexplosionseed" -> return HugeExplodeSeedParticle(pos)
			"bubble" -> return BubbleParticle(pos)
			"splash" -> return SplashParticle(pos)
			"wake", "water" -> return WaterParticle(pos)
			"crit" -> return CriticalParticle(pos)
			"smoke" -> return SmokeParticle(pos, if (data != -1) data else 0)
			"spell" -> return EnchantParticle(pos)
			"instantspell" -> return InstantEnchantParticle(pos)
			"dripwater" -> return WaterDripParticle(pos)
			"driplava" -> return LavaDripParticle(pos)
			"townaura", "spore" -> return SporeParticle(pos)
			"portal" -> return PortalParticle(pos)
			"flame" -> return FlameParticle(pos)
			"lava" -> return LavaParticle(pos)
			"reddust" -> return RedstoneParticle(pos, if (data != -1) data else 1)
			"snowballpoof" -> return ItemBreakParticle(pos, Item.get(Item.SNOWBALL))
			"slime" -> return ItemBreakParticle(pos, Item.get(Item.SLIMEBALL))
			"itembreak" -> if (data != -1 && data != 0) {
				return ItemBreakParticle(pos, Item.get(data))
			}
			"terrain" -> if (data != -1 && data != 0) {
				return TerrainParticle(pos, get(data))
			}
			"heart" -> return HeartParticle(pos, if (data != -1) data else 0)
			"ink" -> return InkParticle(pos, if (data != -1) data else 0)
			"droplet" -> return RainSplashParticle(pos)
			"enchantmenttable" -> return EnchantmentTableParticle(pos)
			"happyvillager" -> return HappyVillagerParticle(pos)
			"angryvillager" -> return AngryVillagerParticle(pos)
			"forcefield" -> return BlockForceFieldParticle(pos)
		}
		if (name.startsWith("iconcrack_")) {
			val d = name.split("_").toTypedArray()
			if (d.size == 3) {
				return ItemBreakParticle(pos, Item.get(Integer.valueOf(d[1]), Integer.valueOf(d[2])))
			}
		} else if (name.startsWith("blockcrack_")) {
			val d = name.split("_").toTypedArray()
			if (d.size == 2) {
				return TerrainParticle(pos, get(Integer.valueOf(d[1]) and 0xff, Integer.valueOf(d[1]) shr 12))
			}
		} else if (name.startsWith("blockdust_")) {
			val d = name.split("_").toTypedArray()
			if (d.size >= 4) {
				return DustParticle(pos, Integer.valueOf(d[1]) and 0xff, Integer.valueOf(d[2]) and 0xff, Integer.valueOf(d[3]) and 0xff, if (d.size >= 5) Integer.valueOf(d[4]) and 0xff else 255)
			}
		}
		return null
	}

	companion object {
		private val ENUM_VALUES = arrayOf<String?>("explode", "hugeexplosion", "hugeexplosionseed", "bubble"
				, "splash", "wake", "water", "crit", "smoke", "spell", "instantspell", "dripwater", "driplava", "townaura"
				, "spore", "portal", "flame", "lava", "reddust", "snowballpoof", "slime", "itembreak", "terrain", "heart"
				, "ink", "droplet", "enchantmenttable", "happyvillager", "angryvillager", "forcefield")

		@Throws(Exception::class)
		private fun getDouble(arg: String?, defaultValue: Double): Double {
			if (arg!!.startsWith("~")) {
				val relativePos = arg.substring(1)
				return if (relativePos.isEmpty()) {
					defaultValue
				} else defaultValue + relativePos.toDouble()
			}
			return arg.toDouble()
		}
	}

	init {
		permission = "nukkit.command.particle"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("name", false, ENUM_VALUES),
				CommandParameter("position", CommandParamType.POSITION, false),
				CommandParameter("count", CommandParamType.INT, true),
				CommandParameter("data", true)
		)
	}
}