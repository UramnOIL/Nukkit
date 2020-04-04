package cn.nukkit.lang

import cn.nukkit.Server
import cn.nukkit.utils.Utils
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BaseLang @JvmOverloads constructor(lang: String, path: String? = null, fallback: String = FALLBACK_LANGUAGE) {
	protected val langName: String
	var langMap: Map<String?, String>? = HashMap()
		protected set
	var fallbackLangMap: Map<String?, String>? = HashMap()
		protected set

	val name: String?
		get() = this["language.name"]

	fun getLang(): String {
		return langName
	}

	protected fun loadLang(path: String?): Map<String?, String>? {
		return try {
			val content = Utils.readFile(path)
			val d: MutableMap<String?, String> = HashMap()
			for (line in content.split("\n").toTypedArray()) {
				line = line.trim { it <= ' ' }
				if (line == "" || line[0] == '#') {
					continue
				}
				val t = line.split("=").toTypedArray()
				if (t.size < 2) {
					continue
				}
				val key = t[0]
				var value = ""
				for (i in 1 until t.size - 1) {
					value += t[i] + "="
				}
				value += t[t.size - 1]
				if (value == "") {
					continue
				}
				d[key] = value
			}
			d
		} catch (e: IOException) {
			Server.instance!!.logger.logException(e)
			null
		}
	}

	protected fun loadLang(stream: InputStream?): Map<String?, String>? {
		return try {
			val content = Utils.readFile(stream)
			val d: MutableMap<String?, String> = HashMap()
			for (line in content.split("\n").toTypedArray()) {
				line = line.trim { it <= ' ' }
				if (line == "" || line[0] == '#') {
					continue
				}
				val t = line.split("=").toTypedArray()
				if (t.size < 2) {
					continue
				}
				val key = t[0]
				var value = ""
				for (i in 1 until t.size - 1) {
					value += t[i] + "="
				}
				value += t[t.size - 1]
				if (value == "") {
					continue
				}
				d[key] = value
			}
			d
		} catch (e: IOException) {
			Server.instance!!.logger.logException(e)
			null
		}
	}

	fun translateString(str: String, vararg params: String?): String? {
		return if (params != null) {
			this.translateString(str, params, null)
		} else this.translateString(str, arrayOfNulls(0), null)
	}

	fun translateString(str: String, vararg params: Any?): String? {
		if (params != null) {
			val paramsToString = arrayOfNulls<String>(params.size)
			for (i in 0 until params.size) {
				paramsToString[i] = Objects.toString(params[i])
			}
			return this.translateString(str, paramsToString, null)
		}
		return this.translateString(str, arrayOfNulls(0), null)
	}

	fun translateString(str: String, param: String?, onlyPrefix: String?): String? {
		return this.translateString(str, arrayOf(param), onlyPrefix)
	}

	@JvmOverloads
	fun translateString(str: String, params: Array<String?> = arrayOf(), onlyPrefix: String? = null): String? {
		var baseText = this[str]
		baseText = parseTranslation(if (baseText != null && (onlyPrefix == null || str.indexOf(onlyPrefix) == 0)) baseText else str, onlyPrefix)
		for (i in params.indices) {
			baseText = baseText!!.replace("{%$i}", parseTranslation(params[i].toString()))
		}
		return baseText
	}

	fun translate(c: TextContainer): String? {
		var baseText: String? = parseTranslation(c.getText())
		if (c is TranslationContainer) {
			baseText = internalGet(c.getText())
			baseText = parseTranslation(baseText ?: c.getText())
			for (i in c.parameters.indices) {
				baseText = baseText!!.replace("{%$i}", parseTranslation(c.parameters[i]))
			}
		}
		return baseText
	}

	fun internalGet(id: String?): String? {
		if (langMap!!.containsKey(id)) {
			return langMap!![id]
		} else if (fallbackLangMap!!.containsKey(id)) {
			return fallbackLangMap!![id]
		}
		return null
	}

	operator fun get(id: String?): String? {
		if (langMap!!.containsKey(id)) {
			return langMap!![id]
		} else if (fallbackLangMap!!.containsKey(id)) {
			return fallbackLangMap!![id]
		}
		return id
	}

	protected fun parseTranslation(text: String?, onlyPrefix: String? = null): String {
		var text = text
		var newString = ""
		text = text.toString()
		var replaceString: String? = null
		val len = text.length
		for (i in 0 until len) {
			val c = text[i]
			if (replaceString != null) {
				val ord = c.toInt()
				if (ord >= 0x30 && ord <= 0x39 // 0-9
						|| ord >= 0x41 && ord <= 0x5a // A-Z
						|| ord >= 0x61 && ord <= 0x7a || // a-z
						c == '.' || c == '-') {
					replaceString += c.toString()
				} else {
					val t = internalGet(replaceString.substring(1))
					newString += if (t != null && (onlyPrefix == null || replaceString.indexOf(onlyPrefix) == 1)) {
						t
					} else {
						replaceString
					}
					replaceString = null
					if (c == '%') {
						replaceString = c.toString()
					} else {
						newString += c.toString()
					}
				}
			} else if (c == '%') {
				replaceString = c.toString()
			} else {
				newString += c.toString()
			}
		}
		if (replaceString != null) {
			val t = internalGet(replaceString.substring(1))
			newString += if (t != null && (onlyPrefix == null || replaceString.indexOf(onlyPrefix) == 1)) {
				t
			} else {
				replaceString
			}
		}
		return newString
	}

	companion object {
		const val FALLBACK_LANGUAGE = "eng"
	}

	init {
		var path = path
		langName = lang.toLowerCase()
		val useFallback = lang != fallback
		if (path == null) {
			path = "lang/"
			langMap = this.loadLang(this.javaClass.classLoader.getResourceAsStream(path + langName + "/lang.ini"))
			if (useFallback) fallbackLangMap = this.loadLang(this.javaClass.classLoader.getResourceAsStream("$path$fallback/lang.ini"))
		} else {
			langMap = this.loadLang(path + langName + "/lang.ini")
			if (useFallback) fallbackLangMap = this.loadLang("$path$fallback/lang.ini")
		}
		if (fallbackLangMap == null) fallbackLangMap = langMap
	}
}