package cn.nukkit.form.element

import com.google.gson.annotations.SerializedName
import java.util.*

class ElementStepSlider @JvmOverloads constructor(text: String, steps: MutableList<String?> = ArrayList(), defaultStep: Int = 0) : Element() {
	private val type = "step_slider" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
	var text = ""
	private val steps: MutableList<String?>

	@SerializedName("default")
	var defaultStepIndex = 0
		private set

	fun setDefaultOptionIndex(index: Int) {
		if (index >= steps.size) return
		defaultStepIndex = index
	}

	fun getSteps(): List<String?> {
		return steps
	}

	@JvmOverloads
	fun addStep(step: String?, isDefault: Boolean = false) {
		steps.add(step)
		if (isDefault) defaultStepIndex = steps.size - 1
	}

	init {
		this.text = text
		this.steps = steps
		defaultStepIndex = defaultStep
	}
}