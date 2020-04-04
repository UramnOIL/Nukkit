package cn.nukkit.form.element

class ElementLabel(text: String) : Element() {
	private val type = "label" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
	var text = ""

	init {
		this.text = text
	}
}