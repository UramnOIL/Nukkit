package cn.nukkit.form.window

import cn.nukkit.form.response.FormResponse
import com.google.gson.Gson

abstract class FormWindow {
	protected var closed = false
	val jSONData: String
		get() = GSON.toJson(this)

	abstract fun setResponse(data: String)
	abstract val response: FormResponse?
	fun wasClosed(): Boolean {
		return closed
	}

	companion object {
		private val GSON = Gson()
	}
}