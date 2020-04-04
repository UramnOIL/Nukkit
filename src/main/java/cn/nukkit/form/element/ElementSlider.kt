package cn.nukkit.form.element

import com.google.gson.annotations.SerializedName

class ElementSlider @JvmOverloads constructor(text: String, min: Float, max: Float, step: Int = -1, defaultValue: Float = -1f) : Element() {
	private val type = "slider" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
	var text = ""
	var min = 0f
	var max = 100f
	var step = 0

	@SerializedName("default")
	var defaultValue = 0f

	init {
		this.text = text
		this.min = if (min < 0f) 0f else min
		this.max = if (max > this.min) max else this.min
		if (step.toFloat() != -1f && step > 0) this.step = step
		if (defaultValue != -1f) this.defaultValue = defaultValue
	}
}