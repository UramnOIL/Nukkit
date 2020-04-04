package cn.nukkit.form.window

import cn.nukkit.form.element.ElementButton
import cn.nukkit.form.response.FormResponseSimple
import java.util.*

class FormWindowSimple @JvmOverloads constructor(title: String, content: String, buttons: MutableList<ElementButton?> = ArrayList()) : FormWindow() {
	private val type = "form" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
	var title = ""
	var content = ""
	private val buttons: MutableList<ElementButton?>
	override var response: FormResponseSimple? = null
		private set

	fun getButtons(): List<ElementButton?> {
		return buttons
	}

	fun addButton(button: ElementButton?) {
		buttons.add(button)
	}

	override fun setResponse(data: String) {
		if (data == "null") {
			closed = true
			return
		}
		val buttonID: Int
		buttonID = try {
			data.toInt()
		} catch (e: Exception) {
			return
		}
		if (buttonID >= buttons.size) {
			response = FormResponseSimple(buttonID, null)
			return
		}
		response = FormResponseSimple(buttonID, buttons[buttonID])
	}

	init {
		this.title = title
		this.content = content
		this.buttons = buttons
	}
}