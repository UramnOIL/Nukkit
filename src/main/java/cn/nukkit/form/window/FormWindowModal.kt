package cn.nukkit.form.window

import cn.nukkit.form.response.FormResponseModal

class FormWindowModal(title: String, content: String, trueButtonText: String, falseButtonText: String) : FormWindow() {
	private val type = "modal" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
	var title = ""
	var content = ""
	var button1 = ""
	var button2 = ""
	override var response: FormResponseModal? = null
		private set

	override fun setResponse(data: String) {
		if (data == "null") {
			closed = true
			return
		}
		response = if (data == "true") FormResponseModal(0, button1) else FormResponseModal(1, button2)
	}

	init {
		this.title = title
		this.content = content
		button1 = trueButtonText
		button2 = falseButtonText
	}
}