package cn.nukkit.permission

import cn.nukkit.Server
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.ParseException
import java.util.*
import java.text.SimpleDateFormat

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BanEntry(val name: String) {
	companion object {
		val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

		fun fromString(str: String): BanEntry {
			val map = Gson().fromJson<Map<String, String>>(str, object : TypeToken<Map<String, String>>() {}.type)
			val banEntry = BanEntry(map["name"] ?: error("This string does not contain property of name: $str"))
			try {
				banEntry.creationDate = dateFormat.parse(map["creationDate"])
				banEntry.expirationDate = if(map["expireDate"] != ("Forever")) dateFormat.parse(map["expireDate"]) else null
			} catch (e: ParseException) {
				Server.instance!!.logger.logException(e)
			}
			banEntry.source = map["source"].toString()
			banEntry.reason = map["reason"].toString()
			return banEntry
		}


		fun fromMap(map: Map<String, String>): BanEntry {
			val banEntry = BanEntry(map["name"] ?: error("This map does not contain property of name: $map"))
			try {
				banEntry.creationDate = dateFormat.parse(map["creationDate"])
				banEntry.expirationDate = if(!map["expireDate"].equals("Forever")) dateFormat.parse(map["expireDate"]) else null
			} catch (e: ParseException) {
				Server.instance!!.logger.logException(e)
			}
			banEntry.source = map["source"].toString()
			banEntry.reason = map["reason"].toString()
			return banEntry
		}
	}
	var creationDate = Date()
	var source = "(Unknown)"
	var expirationDate: Date? = null
	var reason = "Banned by an operator."

	fun hasExpired(): Boolean {
		val now = Date()
		return expirationDate is Date && expirationDate!!.before(now)
	}

	val map: Map<String, String>
		get() {
			val map: MutableMap<String, String> = mutableMapOf()
			map["name"] = name
			map["creationDate"] = dateFormat.format(creationDate)
			map["source"] = source
			map["expireDate"] = if (expirationDate != null) dateFormat.format(expirationDate) else "Forever"
			map["reason"] = reason
			return map
		}

	override fun toString(): String {
		return Gson().toJson(this.map)
	}
}
