package cn.nukkit.utils

import cn.nukkit.network.protocol.LoginPacket
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * ClientChainData is a container of chain data sent from clients.
 *
 *
 * Device information such as client UUID, xuid and serverAddress, can be
 * read from instances of this object.
 *
 *
 * To get chain data, you can use player.getLoginChainData() or read(loginPacket)
 *
 *
 * ===============
 * author: boybook
 * Nukkit Project
 * ===============
 */
class ClientChainData private constructor(buffer: ByteArray) : LoginChainData {
	companion object {
		private const val MOJANG_PUBLIC_KEY_BASE64 = "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V"
		private var MOJANG_PUBLIC_KEY: PublicKey? = null
		@JvmStatic
        fun of(buffer: ByteArray): ClientChainData {
			return ClientChainData(buffer)
		}

		fun read(pk: LoginPacket): ClientChainData {
			return of(pk.getBuffer())
		}

		const val UI_PROFILE_CLASSIC = 0
		const val UI_PROFILE_POCKET = 1

		@Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
		private fun generateKey(base64: String): PublicKey {
			return KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(base64)))
		}

		init {
			MOJANG_PUBLIC_KEY = try {
				generateKey(MOJANG_PUBLIC_KEY_BASE64)
			} catch (e: InvalidKeySpecException) {
				throw AssertionError(e)
			} catch (e: NoSuchAlgorithmException) {
				throw AssertionError(e)
			}
		}
	}

	override fun getUsername(): String {
		return username!!
	}

	override fun getClientUUID(): UUID {
		return clientUUID!!
	}

	override fun getIdentityPublicKey(): String {
		return identityPublicKey!!
	}

	override fun getClientId(): Long {
		return clientId
	}

	override fun getServerAddress(): String {
		return serverAddress!!
	}

	override fun getDeviceModel(): String {
		return deviceModel!!
	}

	override fun getDeviceOS(): Int {
		return deviceOS
	}

	override fun getDeviceId(): String {
		return deviceId!!
	}

	override fun getGameVersion(): String {
		return gameVersion!!
	}

	override fun getGuiScale(): Int {
		return guiScale
	}

	override fun getLanguageCode(): String {
		return languageCode!!
	}

	override fun getXUID(): String {
		return xuid!!
	}

	private var xboxAuthed = false
	override fun getCurrentInputMode(): Int {
		return currentInputMode
	}

	override fun getDefaultInputMode(): Int {
		return defaultInputMode
	}

	override fun getCapeData(): String {
		return capeData!!
	}

	override fun getUIProfile(): Int {
		return UIProfile
	}

	///////////////////////////////////////////////////////////////////////////
	// Override
	///////////////////////////////////////////////////////////////////////////
	override fun equals(obj: Any?): Boolean {
		return obj is ClientChainData && bs == obj.bs
	}

	override fun hashCode(): Int {
		return bs.hashCode()
	}

	///////////////////////////////////////////////////////////////////////////
	// Internal
	///////////////////////////////////////////////////////////////////////////
	private var username: String? = null
	private var clientUUID: UUID? = null
	private var xuid: String? = null
	private var identityPublicKey: String? = null
	private var clientId: Long = 0
	private var serverAddress: String? = null
	private var deviceModel: String? = null
	private var deviceOS = 0
	private var deviceId: String? = null
	private var gameVersion: String? = null
	private var guiScale = 0
	private var languageCode: String? = null
	private var currentInputMode = 0
	private var defaultInputMode = 0
	private var UIProfile = 0
	private var capeData: String? = null
	private val bs = BinaryStream()
	override fun isXboxAuthed(): Boolean {
		return xboxAuthed
	}

	private fun decodeSkinData() {
		val skinToken = decodeToken(String(bs[bs.lInt])) ?: return
		if (skinToken.has("ClientRandomId")) clientId = skinToken["ClientRandomId"].asLong
		if (skinToken.has("ServerAddress")) serverAddress = skinToken["ServerAddress"].asString
		if (skinToken.has("DeviceModel")) deviceModel = skinToken["DeviceModel"].asString
		if (skinToken.has("DeviceOS")) deviceOS = skinToken["DeviceOS"].asInt
		if (skinToken.has("DeviceId")) deviceId = skinToken["DeviceId"].asString
		if (skinToken.has("GameVersion")) gameVersion = skinToken["GameVersion"].asString
		if (skinToken.has("GuiScale")) guiScale = skinToken["GuiScale"].asInt
		if (skinToken.has("LanguageCode")) languageCode = skinToken["LanguageCode"].asString
		if (skinToken.has("CurrentInputMode")) currentInputMode = skinToken["CurrentInputMode"].asInt
		if (skinToken.has("DefaultInputMode")) defaultInputMode = skinToken["DefaultInputMode"].asInt
		if (skinToken.has("UIProfile")) UIProfile = skinToken["UIProfile"].asInt
		if (skinToken.has("CapeData")) capeData = skinToken["CapeData"].asString
	}

	private fun decodeToken(token: String): JsonObject? {
		val base = token.split("\\.").toTypedArray()
		if (base.size < 2) return null
		val json = String(Base64.getDecoder().decode(base[1]), StandardCharsets.UTF_8)
		//Server.getInstance().getLogger().debug(json);
		return Gson().fromJson(json, JsonObject::class.java)
	}

	private fun decodeChainData() {
		val map = Gson().fromJson<Map<String, List<String>>>(String(bs[bs.lInt], StandardCharsets.UTF_8),
				object : TypeToken<Map<String?, List<String?>?>?>() {}.type)
		if (map.isEmpty() || !map.containsKey("chain") || map["chain"]!!.isEmpty()) return
		val chains = map["chain"]!!

		// Validate keys
		xboxAuthed = try {
			verifyChain(chains)
		} catch (e: Exception) {
			false
		}
		for (c in chains) {
			val chainMap = decodeToken(c) ?: continue
			if (chainMap.has("extraData")) {
				val extra = chainMap["extraData"].asJsonObject
				if (extra.has("displayName")) username = extra["displayName"].asString
				if (extra.has("identity")) clientUUID = UUID.fromString(extra["identity"].asString)
				if (extra.has("XUID")) xuid = extra["XUID"].asString
			}
			if (chainMap.has("identityPublicKey")) identityPublicKey = chainMap["identityPublicKey"].asString
		}
		if (!xboxAuthed) {
			xuid = null
		}
	}

	@Throws(Exception::class)
	private fun verifyChain(chains: List<String>): Boolean {
		var lastKey: PublicKey? = null
		var mojangKeyVerified = false
		for (chain in chains) {
			val jws = JWSObject.parse(chain)
			if (!mojangKeyVerified) {
				// First chain should be signed using Mojang's private key. We'd be in big trouble if it leaked...
				mojangKeyVerified = verify(MOJANG_PUBLIC_KEY, jws)
			}
			if (lastKey != null) {
				if (!verify(lastKey, jws)) {
					throw JOSEException("Unable to verify key in chain.")
				}
			}
			val payload = jws.payload.toJSONObject()
			val base64key = payload.getAsString("identityPublicKey") ?: throw RuntimeException("No key found")
			lastKey = generateKey(base64key)
		}
		return mojangKeyVerified
	}

	@Throws(JOSEException::class)
	private fun verify(key: PublicKey?, `object`: JWSObject): Boolean {
		val verifier = DefaultJWSVerifierFactory().createJWSVerifier(`object`.header, key)
		return `object`.verify(verifier)
	}

	init {
		bs.setBuffer(buffer, 0)
		decodeChainData()
		decodeSkinData()
	}
}