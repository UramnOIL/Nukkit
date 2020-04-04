package cn.nukkit.form.response

import java.util.*

class FormResponseCustom(val responses: HashMap<Int?, Any?>, private val dropdownResponses: HashMap<Int, FormResponseData>,
						 private val inputResponses: HashMap<Int, String>, private val sliderResponses: HashMap<Int, Float>,
						 private val stepSliderResponses: HashMap<Int, FormResponseData>,
						 private val toggleResponses: HashMap<Int, Boolean>,
						 private val labelResponses: HashMap<Int, String?>) : FormResponse() {

	fun getResponse(id: Int): Any? {
		return responses[id]
	}

	fun getDropdownResponse(id: Int): FormResponseData? {
		return dropdownResponses[id]
	}

	fun getInputResponse(id: Int): String? {
		return inputResponses[id]
	}

	fun getSliderResponse(id: Int): Float {
		return sliderResponses[id]!!
	}

	fun getStepSliderResponse(id: Int): FormResponseData? {
		return stepSliderResponses[id]
	}

	fun getToggleResponse(id: Int): Boolean {
		return toggleResponses[id]!!
	}

	fun getLabelResponse(id: Int): String? {
		return labelResponses[id]
	}

}