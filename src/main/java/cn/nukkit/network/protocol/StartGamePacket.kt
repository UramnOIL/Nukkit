package cn.nukkit.network.protocol

import cn.nukkit.Server
import cn.nukkit.level.GameRules
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.utils.BinaryStream
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import lombok.ToString
import lombok.extern.log4j.Log4j2
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.util.Collection
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Created on 15-10-13.
 */
@Log4j2
@ToString(exclude = ["blockPalette"])
class StartGamePacket : DataPacket() {
	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.START_GAME_PACKET
		const val GAME_PUBLISH_SETTING_NO_MULTI_PLAY = 0
		const val GAME_PUBLISH_SETTING_INVITE_ONLY = 1
		const val GAME_PUBLISH_SETTING_FRIENDS_ONLY = 2
		const val GAME_PUBLISH_SETTING_FRIENDS_OF_FRIENDS = 3
		const val GAME_PUBLISH_SETTING_PUBLIC = 4
		private val ITEM_DATA_PALETTE: ByteArray?

		init {
			val stream: InputStream = Server::class.java.getClassLoader().getResourceAsStream("runtime_item_ids.json")
			if (cn.nukkit.network.protocol.stream == null) {
				throw AssertionError("Unable to locate RuntimeID table")
			}
			val reader: Reader = InputStreamReader(cn.nukkit.network.protocol.stream, StandardCharsets.UTF_8)
			val gson = Gson()
			val collectionType: Type = object : TypeToken<Collection<ItemData?>?>() {}.getType()
			val entries: Collection<ItemData?> = cn.nukkit.network.protocol.gson.fromJson(cn.nukkit.network.protocol.reader, cn.nukkit.network.protocol.collectionType)
			val paletteBuffer = BinaryStream()
			cn.nukkit.network.protocol.paletteBuffer.putUnsignedVarInt(cn.nukkit.network.protocol.entries.size())
			for (data in cn.nukkit.network.protocol.entries) {
				cn.nukkit.network.protocol.paletteBuffer.putString(cn.nukkit.network.protocol.data.name)
				cn.nukkit.network.protocol.paletteBuffer.putLShort(cn.nukkit.network.protocol.data.id)
			}
			ITEM_DATA_PALETTE = cn.nukkit.network.protocol.paletteBuffer.getBuffer()
		}
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	var entityUniqueId: Long = 0
	var entityRuntimeId: Long = 0
	var playerGamemode = 0
	var x = 0f
	var y = 0f
	var z = 0f
	var yaw = 0f
	var pitch = 0f
	var seed = 0
	var dimension: Byte = 0
	var generator = 1
	var worldGamemode = 0
	var difficulty = 0
	var spawnX = 0
	var spawnY = 0
	var spawnZ = 0
	var hasAchievementsDisabled = true
	var dayCycleStopTime = -1 //-1 = not stopped, any positive value = stopped at that time
	var eduEditionOffer = 0
	var hasEduFeaturesEnabled = false
	var rainLevel = 0f
	var lightningLevel = 0f
	var hasConfirmedPlatformLockedContent = false
	var multiplayerGame = true
	var broadcastToLAN = true
	var xblBroadcastIntent = GAME_PUBLISH_SETTING_PUBLIC
	var platformBroadcastIntent = GAME_PUBLISH_SETTING_PUBLIC
	var commandsEnabled = false
	var isTexturePacksRequired = false
	var gameRules: GameRules? = null
	var bonusChest = false
	var hasStartWithMapEnabled = false
	var permissionLevel = 1
	var serverChunkTickRange = 4
	var hasLockedBehaviorPack = false
	var hasLockedResourcePack = false
	var isFromLockedWorldTemplate = false
	var isUsingMsaGamertagsOnly = false
	var isFromWorldTemplate = false
	var isWorldTemplateOptionLocked = false
	var isOnlySpawningV1Villagers = false
	var vanillaVersion: String? = ProtocolInfo.MINECRAFT_VERSION_NETWORK
	var levelId: String? = "" //base64 string, usually the same as world folder name in vanilla
	var worldName: String? = null
	var premiumWorldTemplateId: String? = ""
	var isTrial = false
	var isMovementServerAuthoritative = false
	var currentTick: Long = 0
	var enchantmentSeed = 0
	var multiplayerCorrelationId: String? = ""

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putEntityUniqueId(entityUniqueId)
		this.putEntityRuntimeId(entityRuntimeId)
		this.putVarInt(playerGamemode)
		this.putVector3f(x, y, z)
		this.putLFloat(yaw)
		this.putLFloat(pitch)
		this.putVarInt(seed)
		this.putVarInt(dimension)
		this.putVarInt(generator)
		this.putVarInt(worldGamemode)
		this.putVarInt(difficulty)
		this.putBlockVector3(spawnX, spawnY, spawnZ)
		this.putBoolean(hasAchievementsDisabled)
		this.putVarInt(dayCycleStopTime)
		this.putVarInt(eduEditionOffer)
		this.putBoolean(hasEduFeaturesEnabled)
		this.putLFloat(rainLevel)
		this.putLFloat(lightningLevel)
		this.putBoolean(hasConfirmedPlatformLockedContent)
		this.putBoolean(multiplayerGame)
		this.putBoolean(broadcastToLAN)
		this.putVarInt(xblBroadcastIntent)
		this.putVarInt(platformBroadcastIntent)
		this.putBoolean(commandsEnabled)
		this.putBoolean(isTexturePacksRequired)
		this.putGameRules(gameRules)
		this.putBoolean(bonusChest)
		this.putBoolean(hasStartWithMapEnabled)
		this.putVarInt(permissionLevel)
		this.putLInt(serverChunkTickRange)
		this.putBoolean(hasLockedBehaviorPack)
		this.putBoolean(hasLockedResourcePack)
		this.putBoolean(isFromLockedWorldTemplate)
		this.putBoolean(isUsingMsaGamertagsOnly)
		this.putBoolean(isFromWorldTemplate)
		this.putBoolean(isWorldTemplateOptionLocked)
		this.putBoolean(isOnlySpawningV1Villagers)
		this.putString(vanillaVersion)
		this.putString(levelId)
		this.putString(worldName)
		this.putString(premiumWorldTemplateId)
		this.putBoolean(isTrial)
		this.putBoolean(isMovementServerAuthoritative)
		this.putLLong(currentTick)
		this.putVarInt(enchantmentSeed)
		this.put(GlobalBlockPalette.BLOCK_PALETTE)
		this.put(ITEM_DATA_PALETTE)
		this.putString(multiplayerCorrelationId)
	}

	private class ItemData {
		private val name: String? = null
		private val id = 0
	}
}