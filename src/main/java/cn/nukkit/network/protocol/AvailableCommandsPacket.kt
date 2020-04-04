package cn.nukkit.network.protocol

import cn.nukkit.command.data.*
import cn.nukkit.utils.BinaryStream
import lombok.ToString
import java.util.*
import java.util.function.ObjIntConsumer
import java.util.function.ToIntFunction
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
class AvailableCommandsPacket : DataPacket() {
	var commands: Map<String?, CommandDataVersions?>? = null
	val softEnums: Map<String?, List<String?>?>? = HashMap()

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	@Override
	override fun decode() {
		commands = HashMap()
		val enumValues: List<String?> = ArrayList()
		val postFixes: List<String?> = ArrayList()
		val enums: List<CommandEnum?> = ArrayList()
		var len = getUnsignedVarInt() as Int
		while (len-- > 0) {
			enumValues.add(getString())
		}
		len = getUnsignedVarInt() as Int
		while (len-- > 0) {
			postFixes.add(getString())
		}
		val indexReader: ToIntFunction<BinaryStream?>
		indexReader = if (enumValues.size() < 256) {
			READ_BYTE
		} else if (enumValues.size() < 65536) {
			READ_SHORT
		} else {
			READ_INT
		}
		len = getUnsignedVarInt() as Int
		while (len-- > 0) {
			val enumName: String = getString()
			var enumLength = getUnsignedVarInt() as Int
			val values: List<String?> = ArrayList()
			while (enumLength-- > 0) {
				val index: Int = indexReader.applyAsInt(this)
				var enumValue: String?
				if (index < 0 || enumValues[index].also { enumValue = it } == null) {
					throw IllegalStateException("Enum value not found for index $index")
				}
				values.add(enumValue)
			}
			enums.add(CommandEnum(enumName, values))
		}
		len = getUnsignedVarInt() as Int
		while (len-- > 0) {
			val name: String = getString()
			val description: String = getString()
			val flags: Int = getByte()
			val permission: Int = getByte()
			var alias: CommandEnum? = null
			val aliasIndex: Int = getLInt()
			if (aliasIndex >= 0) {
				alias = enums[aliasIndex]
			}
			val overloads: Map<String?, CommandOverload?> = HashMap()
			var length = getUnsignedVarInt() as Int
			while (length-- > 0) {
				val overload = CommandOverload()
				val paramLen = getUnsignedVarInt() as Int
				overload.input.parameters = arrayOfNulls<CommandParameter?>(paramLen)
				for (i in 0 until paramLen) {
					val paramName: String = getString()
					val type: Int = getLInt()
					val optional: Boolean = getBoolean()
					val parameter = CommandParameter(paramName, optional)
					if (type and ARG_FLAG_POSTFIX != 0) {
						parameter.postFix = postFixes[type and 0xffff]
					} else if (type and ARG_FLAG_VALID == 0) {
						throw IllegalStateException("Invalid parameter type received")
					} else {
						val index = type and 0xffff
						if (type and ARG_FLAG_ENUM != 0) {
							parameter.enumData = enums[index]
						} else if (type and ARG_FLAG_SOFT_ENUM != 0) {
							// TODO: 22/01/2019 soft enums
						} else {
							throw IllegalStateException("Unknown parameter type!")
						}
					}
					overload.input.parameters.get(i) = parameter
				}
				overloads.put(Integer.toString(length), overload)
			}
			val data = CommandData()
			data.aliases = alias
			data.overloads = overloads
			data.description = description
			data.flags = flags
			data.permission = permission
			val versions = CommandDataVersions()
			versions.versions.add(data)
			commands.put(name, versions)
		}
	}

	@Override
	override fun encode() {
		this.reset()
		val enumValuesSet: LinkedHashSet<String?> = LinkedHashSet()
		val postFixesSet: LinkedHashSet<String?> = LinkedHashSet()
		val enumsSet: LinkedHashSet<CommandEnum?> = LinkedHashSet()
		commands.forEach { name, data ->
			val cmdData: CommandData = data.versions.get(0)
			if (cmdData.aliases != null) {
				enumsSet.add(cmdData.aliases)
				enumValuesSet.addAll(cmdData.aliases.getValues())
			}
			for (overload in cmdData.overloads.values()) {
				for (parameter in overload.input.parameters) {
					if (parameter.enumData != null) {
						enumsSet.add(parameter.enumData)
						enumValuesSet.addAll(parameter.enumData.getValues())
					}
					if (parameter.postFix != null) {
						postFixesSet.add(parameter.postFix)
					}
				}
			}
		}
		val enumValues: List<String?> = ArrayList(enumValuesSet)
		val enums: List<CommandEnum?> = ArrayList(enumsSet)
		val postFixes: List<String?> = ArrayList(postFixesSet)
		this.putUnsignedVarInt(enumValues.size())
		enumValues.forEach(this::putString)
		this.putUnsignedVarInt(postFixes.size())
		postFixes.forEach(this::putString)
		val indexWriter: ObjIntConsumer<BinaryStream?>
		indexWriter = if (enumValues.size() < 256) {
			WRITE_BYTE
		} else if (enumValues.size() < 65536) {
			WRITE_SHORT
		} else {
			WRITE_INT
		}
		this.putUnsignedVarInt(enums.size())
		enums.forEach { cmdEnum ->
			putString(cmdEnum.getName())
			val values: List<String?> = cmdEnum.getValues()
			putUnsignedVarInt(values.size())
			for (`val` in values) {
				val i = enumValues.indexOf(`val`)
				if (i < 0) {
					throw IllegalStateException("Enum value '$`val`' not found")
				}
				indexWriter.accept(this, i)
			}
		}
		putUnsignedVarInt(commands!!.size())
		commands.forEach { name, cmdData ->
			val data: CommandData = cmdData.versions.get(0)
			putString(name)
			putString(data.description)
			putByte(data.flags as Byte)
			putByte(data.permission as Byte)
			putLInt(if (data.aliases == null) -1 else enums.indexOf(data.aliases))
			putUnsignedVarInt(data.overloads.size())
			for (overload in data.overloads.values()) {
				putUnsignedVarInt(overload.input.parameters.length)
				for (parameter in overload.input.parameters) {
					putString(parameter.name)
					var type = 0
					if (parameter.postFix != null) {
						val i = postFixes.indexOf(parameter.postFix)
						if (i < 0) {
							throw IllegalStateException("Postfix '" + parameter.postFix.toString() + "' isn't in postfix array")
						}
						type = ARG_FLAG_POSTFIX or i
					} else {
						type = type or ARG_FLAG_VALID
						type = if (parameter.enumData != null) {
							type or (ARG_FLAG_ENUM or enums.indexOf(parameter.enumData))
						} else {
							type or parameter.type.getId()
						}
					}
					putLInt(type)
					putBoolean(parameter.optional)
					putByte(parameter.options) // TODO: 19/03/2019 Bit flags. Only first bit is used for GameRules.
				}
			}
		}
		this.putUnsignedVarInt(softEnums!!.size())
		softEnums.forEach { name, values ->
			this.putString(name)
			this.putUnsignedVarInt(values.size())
			values.forEach(this::putString)
		}
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.AVAILABLE_COMMANDS_PACKET
		private val WRITE_BYTE: ObjIntConsumer<BinaryStream?>? = ObjIntConsumer<BinaryStream?> { s, v -> s.putByte(v as Byte) }
		private val WRITE_SHORT: ObjIntConsumer<BinaryStream?>? = BinaryStream::putLShort
		private val WRITE_INT: ObjIntConsumer<BinaryStream?>? = BinaryStream::putLInt
		private val READ_BYTE: ToIntFunction<BinaryStream?>? = BinaryStream::getByte
		private val READ_SHORT: ToIntFunction<BinaryStream?>? = BinaryStream::getLShort
		private val READ_INT: ToIntFunction<BinaryStream?>? = BinaryStream::getLInt
		const val ARG_FLAG_VALID = 0x100000
		const val ARG_FLAG_ENUM = 0x200000
		const val ARG_FLAG_POSTFIX = 0x1000000
		const val ARG_FLAG_SOFT_ENUM = 0x4000000
		const val ARG_TYPE_INT = 1
		const val ARG_TYPE_FLOAT = 2
		const val ARG_TYPE_VALUE = 3
		const val ARG_TYPE_WILDCARD_INT = 4
		const val ARG_TYPE_OPERATOR = 5
		const val ARG_TYPE_TARGET = 6
		const val ARG_TYPE_WILDCARD_TARGET = 7
		const val ARG_TYPE_FILE_PATH = 14
		const val ARG_TYPE_STRING = 29
		const val ARG_TYPE_BLOCK_POSITION = 37
		const val ARG_TYPE_POSITION = 38
		const val ARG_TYPE_MESSAGE = 41
		const val ARG_TYPE_RAWTEXT = 43
		const val ARG_TYPE_JSON = 47
		const val ARG_TYPE_COMMAND = 54
	}
}