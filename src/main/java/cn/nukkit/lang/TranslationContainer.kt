package cn.nukkit.lang

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class TranslationContainer : TextContainer, Cloneable {
	var parameters: Array<String?>

	constructor(text: String, params: String?) : super(text) {
		parameters = arrayOf(params)
	}

	@JvmOverloads
	constructor(text: String?, vararg params: String? = *arrayOf<kotlin.String?>()) : super(text)
	{
		parameters = parameters
	}

	fun getParameter(i: Int): String? {
		return if (i >= 0 && i < parameters.size) parameters[i] else null
	}

	fun setParameter(i: Int, str: String?) {
		if (i >= 0 && i < parameters.size) {
			parameters[i] = str
		}
	}

	override fun clone(): TranslationContainer {
		return TranslationContainer(text, *parameters.clone())
	}
}