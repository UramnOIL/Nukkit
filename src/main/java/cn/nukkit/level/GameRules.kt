package cn.nukkit.level

import cn.nukkit.level.GameRule.Companion.parseString
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.BinaryStream
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableMap
import java.lang.Exception
import java.util.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class GameRules private constructor() {
	private val gameRules = EnumMap<GameRule, Value<*>>(GameRule::class.java)
	var isStale = false
		private set

	fun getGameRules(): Map<GameRule, Value<*>> {
		return ImmutableMap.copyOf(gameRules)
	}

	fun refresh() {
		isStale = false
	}

	fun setGameRule(gameRule: GameRule, value: Boolean) {
		require(gameRules.containsKey(gameRule)) { "Gamerule does not exist" }
		gameRules[gameRule]!!.setSafeValue(value)
		isStale = true
	}

	fun setGameRule(gameRule: GameRule, value: Int) {
		require(gameRules.containsKey(gameRule)) { "Gamerule does not exist" }
		gameRules[gameRule]!!.setSafeValue(value)
		isStale = true
	}

	fun setGameRule(gameRule: GameRule, value: Float) {
		require(gameRules.containsKey(gameRule)) { "Gamerule does not exist" }
		gameRules[gameRule]!!.setSafeValue(value)
		isStale = true
	}

	@Throws(IllegalArgumentException::class)
	fun setGameRules(gameRule: GameRule, value: String) {
		Preconditions.checkNotNull(gameRule, "gameRule")
		Preconditions.checkNotNull(value, "value")
		when (getGameRuleType(gameRule)) {
			Type.BOOLEAN -> when {
				value.equals("true", ignoreCase = true) -> {
					setGameRule(gameRule, true)
				}
				value.equals("false", ignoreCase = true) -> {
					setGameRule(gameRule, false)
				}
				else -> {
					throw IllegalArgumentException("Was not a boolean")
				}
			}
			Type.INTEGER -> setGameRule(gameRule, value.toInt())
			Type.FLOAT -> setGameRule(gameRule, value.toFloat())
			else -> {}
		}
	}

	fun getBoolean(gameRule: GameRule): Boolean {
		return gameRules[gameRule]!!.valueAsBoolean
	}

	fun getInteger(gameRule: GameRule): Int {
		Preconditions.checkNotNull(gameRule, "gameRule")
		return gameRules[gameRule]!!.valueAsInteger
	}

	fun getFloat(gameRule: GameRule): Float {
		Preconditions.checkNotNull(gameRule, "gameRule")
		return gameRules[gameRule]!!.valueAsFloat
	}

	fun getString(gameRule: GameRule): String {
		Preconditions.checkNotNull(gameRule, "gameRule")
		return gameRules[gameRule]!!.value.toString()
	}

	fun getGameRuleType(gameRule: GameRule): Type {
		Preconditions.checkNotNull(gameRule, "gameRule")
		return gameRules[gameRule]!!.type
	}

	fun hasRule(gameRule: GameRule): Boolean {
		return gameRules.containsKey(gameRule)
	}

	val rules: Array<GameRule> = gameRules.keys.toTypedArray()

	// TODO: This needs to be moved out since there is not a separate compound tag in the LevelDB format for Game Rules.
	fun writeNBT(): CompoundTag {
		val nbt = CompoundTag()
		for ((key, value) in gameRules) {
			nbt.putString(key.ruleName, value.value.toString())
		}
		return nbt
	}

	fun readNBT(nbt: CompoundTag) {
		Preconditions.checkNotNull(nbt)
		nbt.tags.keys.forEach{ key ->
			val gameRule = parseString(key)
			if(gameRule !is GameRule) return
			if (!GameRule::class.java.enumConstants.any { it.name == gameRule.name }) {
				return
			}
			setGameRules(gameRule, nbt.getString(key))
		}
	}

	enum class Type(var type: KClass<*>) {
		UNKNOWN(Nothing::class) {
			override fun write(pk: BinaryStream, value: Value<*>) {}
		},
		BOOLEAN(Boolean::class) {
			override fun write(pk: BinaryStream, value: Value<*>) {
				pk.putBoolean(value.valueAsBoolean)
			}
		},
		INTEGER(Int::class) {
			override fun write(pk: BinaryStream, value: Value<*>) {
				pk.putUnsignedVarInt(value.valueAsInteger.toLong())
			}
		},
		FLOAT(Float::class) {
			override fun write(pk: BinaryStream, value: Value<*>) {
				pk.putLFloat(value.valueAsFloat)
			}
		};

		abstract fun write(pk: BinaryStream, value: Value<*>)
	}

	class Value<T>( val type: Type, var value: T) {
		init {
			if (value!!::class != type.type::class) throw Exception("Value not of type " + type::class.simpleName)
		}

		inline fun <reified G> setSafeValue(value: G) {
			if (this::class == G::class) {
				throw UnsupportedOperationException("Rule not of type " + G::class.simpleName!!.toLowerCase())
			}
			@Suppress("UNCHECKED_CAST")
			this.value = value as T
		}

		internal val valueAsBoolean: Boolean
			get() {
				if (value!!::class != Boolean::class) {
					throw UnsupportedOperationException("Rule not of type boolean")
				}
				return value as Boolean
			}

		internal val valueAsInteger: Int
			get() {
				if (value!!::class !== Int::class) {
					throw UnsupportedOperationException("Rule not of type integer")
				}
				return value as Int
			}

		internal val valueAsFloat: Float
			get() {
				if (value!!::class !== Float::class) {
					throw UnsupportedOperationException("Rule not of type float")
				}
				return value as Float
			}

		fun write(pk: BinaryStream) {
			pk.putUnsignedVarInt(type.ordinal.toLong())
			type.write(pk, this)
		}

	}

	companion object {
		@JvmStatic
        val default: GameRules
			get() {
				val gameRules = GameRules()
				gameRules.gameRules[GameRule.COMMAND_BLOCK_OUTPUT] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DO_DAYLIGHT_CYCLE] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DO_ENTITY_DROPS] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DO_FIRE_TICK] = Value<Any?>(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DO_IMMEDIATE_RESPAWN] = Value<Any?>(Type.BOOLEAN, false)
				gameRules.gameRules[GameRule.DO_MOB_LOOT] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DO_MOB_SPAWNING] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DO_TILE_DROPS] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DO_WEATHER_CYCLE] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.DROWNING_DAMAGE] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.FALL_DAMAGE] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.FIRE_DAMAGE] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.KEEP_INVENTORY] = Value(Type.BOOLEAN, false)
				gameRules.gameRules[GameRule.MOB_GRIEFING] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.NATURAL_REGENERATION] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.PVP] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.RANDOM_TICK_SPEED] = Value(Type.INTEGER, 3)
				gameRules.gameRules[GameRule.SEND_COMMAND_FEEDBACK] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.SHOW_COORDINATES] = Value(Type.BOOLEAN, false)
				gameRules.gameRules[GameRule.TNT_EXPLODES] = Value(Type.BOOLEAN, true)
				gameRules.gameRules[GameRule.SHOW_DEATH_MESSAGE] = Value(Type.BOOLEAN, true)
				return gameRules
			}
	}
}