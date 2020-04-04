package cn.nukkit.form.window

import cn.nukkit.form.element.*
import cn.nukkit.form.response.FormResponseCustom
import cn.nukkit.form.response.FormResponseData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class FormWindowCustom @JvmOverloads constructor(title: String, contents: MutableList<Element?> = ArrayList(), icon: ElementButtonImageData? = null as ElementButtonImageData?) : FormWindow() {
	private val type = "custom_form" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
	var title = ""
	var icon: ElementButtonImageData?
		private set
	private val content: MutableList<Element?>
	override var response: FormResponseCustom? = null
		private set

	constructor(title: String, contents: MutableList<Element?>, icon: String) : this(title, contents, if (icon.isEmpty()) null else ElementButtonImageData(ElementButtonImageData.Companion.IMAGE_DATA_TYPE_URL, icon)) {}

	val elements: List<Element?>
		get() = content

	fun addElement(element: Element?) {
		content.add(element)
	}

	fun setIcon(icon: String) {
		if (!icon.isEmpty()) this.icon = ElementButtonImageData(ElementButtonImageData.Companion.IMAGE_DATA_TYPE_URL, icon)
	}

	fun setIcon(icon: ElementButtonImageData?) {
		this.icon = icon
	}

	override fun setResponse(data: String) {
		if (data == "null") {
			closed = true
			return
		}
		val elementResponses = Gson().fromJson<List<String>>(data, object : TypeToken<List<String?>?>() {}.type)
		//elementResponses.remove(elementResponses.size() - 1); //submit button //maybe mojang removed that?
		var i = 0
		val dropdownResponses = HashMap<Int, FormResponseData>()
		val inputResponses = HashMap<Int, String>()
		val sliderResponses = HashMap<Int, Float>()
		val stepSliderResponses = HashMap<Int, FormResponseData>()
		val toggleResponses = HashMap<Int, Boolean>()
		val responses = HashMap<Int?, Any?>()
		val labelResponses = HashMap<Int, String?>()
		for (elementData in elementResponses) {
			if (i >= content.size) {
				break
			}
			val e = content[i] ?: break
			if (e is ElementLabel) {
				labelResponses[i] = e.text
				responses[i] = e.text
			} else if (e is ElementDropdown) {
				val answer = e.options[elementData.toInt()]
				dropdownResponses[i] = FormResponseData(elementData.toInt(), answer)
				responses[i] = answer
			} else if (e is ElementInput) {
				inputResponses[i] = elementData
				responses[i] = elementData
			} else if (e is ElementSlider) {
				val answer = elementData.toFloat()
				sliderResponses[i] = answer
				responses[i] = answer
			} else if (e is ElementStepSlider) {
				val answer = e.steps[elementData.toInt()]
				stepSliderResponses[i] = FormResponseData(elementData.toInt(), answer)
				responses[i] = answer
			} else if (e is ElementToggle) {
				val answer = java.lang.Boolean.parseBoolean(elementData)
				toggleResponses[i] = answer
				responses[i] = answer
			}
			i++
		}
		response = FormResponseCustom(responses, dropdownResponses, inputResponses,
				sliderResponses, stepSliderResponses, toggleResponses, labelResponses)
	}

	/**
	 * Set Elements from Response
	 * Used on ServerSettings Form Response. After players set settings, we need to sync these settings to the server.
	 */
	fun setElementsFromResponse() {
		if (response != null) {
			response.getResponses().forEach { (i: Int?, response: Any) ->
				val e = content[i!!]
				if (e != null) {
					if (e is ElementDropdown) {
						e.defaultOptionIndex = e.options.indexOf(response)
					} else if (e is ElementInput) {
						e.defaultText = response
					} else if (e is ElementSlider) {
						e.defaultValue = response
					} else if (e is ElementStepSlider) {
						e.setDefaultOptionIndex(e.steps.indexOf(response))
					} else if (e is ElementToggle) {
						e.isDefaultValue = response
					}
				}
			}
		}
	}

	init {
		this.title = title
		content = contents
		this.icon = icon
	}
}