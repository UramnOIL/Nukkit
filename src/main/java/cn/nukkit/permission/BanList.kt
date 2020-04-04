package cn.nukkit.permission

import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.Utils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BanList(private val file: String) {
	private var list: MutableMap<String, BanEntry> = mutableMapOf()
	private var isEnable = true

	val entires: Map<String, BanEntry>
		get() {
			removeExpired()
			return list
		}

	fun isBanned(name: String): Boolean {
		return if (!isEnable) {
			false
		} else {
			removeExpired()
			list.containsKey(name.toLowerCase())
		}
	}

	fun add(entry: BanEntry) {
		list[entry.name] = entry
		save()
	}

	fun addBan(target: String): BanEntry {
		return this.addBan(target, null)
	}

	private fun addBan(target: String, reason: String?): BanEntry {
		return this.addBan(target, reason, null)
	}

	private fun addBan(target: String, reason: String?, expireDate: Date?): BanEntry {
		return this.addBan(target, reason, expireDate, null)
	}

	fun addBan(target: String, reason: String?, expireDate: Date?, source: String?): BanEntry {
		val entry = BanEntry(target)
		entry.source = source ?: entry.source
		entry.expirationDate = expireDate
		entry.reason = reason ?: entry.reason
		add(entry)
		return entry
	}

	fun remove(name: String) {
		val name = name.toLowerCase()
		if (list.containsKey(name)) {
			list.remove(name)
			save()
		}
	}

	private fun removeExpired() {
		list.forEach { (t, u) ->
			val entry: BanEntry = u
			if (entry.hasExpired()) {
				list.remove(t)
			}
		}
	}

	fun load() {
		list = mutableMapOf()
		val file = File(file)
		try {
			if (!file.exists()) {
				file.createNewFile()
				save()
			} else {
				val list: List<Map<String, String>> = Gson().fromJson(Utils.readFile(this.file), object : TypeToken<List<Map<String, String>>>() {}.type)
				list.forEach {
					val entry: BanEntry = BanEntry.fromMap(it)
					this.list.put(entry.name, entry)
				}
			}
		} catch (e: IOException) {
			MainLogger.getLogger().error("Could not load ban list: ", e)
		}
	}

	fun save() {
		removeExpired()
		try {
			val file = File(file)
			if (!file.exists()) {
				file.createNewFile()
			}
			val list: MutableList<Map<String, String>> = mutableListOf()
			this.list.values.forEach {
				list.add(it.map)
			}
			Utils.writeFile(this.file, ByteArrayInputStream(GsonBuilder().setPrettyPrinting().create().toJson(list).toByteArray(StandardCharsets.UTF_8)))
		} catch (e: IOException) {
			MainLogger.getLogger().error("Could not save ban list ", e)
		}
	}

}