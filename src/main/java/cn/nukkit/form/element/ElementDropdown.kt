package cn.nukkit.form.element

import com.google.gson.annotations.SerializedName
import java.util.*

class ElementDropdown @JvmOverloads constructor(text: String, options: MutableList<String?> = ArrayList(), defaultOption: Int = 0) : Element() {
	private val type = "dropdown" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
	var text = ""
	private val options: MutableList<String?>

	@SerializedName("default")
	private var defaultOptionIndex = 0
	fun getDefaultOptionIndex(): Int {
		return defaultOptionIndex
	}

	fun setDefaultOptionIndex(index: Int) {
		if (index >= options.size) return
		defaultOptionIndex = index
	}

	fun getOptions(): List<String?> {
		return options
	}

	@JvmOverloads
	fun addOption(option: String?, isDefault: Boolean = false) {
		options.add(option)
		if (isDefault) defaultOptionIndex = options.size - 1
	}

	init {
		this.text = text
		this.options = options
		defaultOptionIndex = defaultOption
	}
}