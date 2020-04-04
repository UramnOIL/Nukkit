package cn.nukkit.form.element

class ElementButtonImageData(type: String, data: String) {
	var type: String
	var data: String

	companion object {
		const val IMAGE_DATA_TYPE_PATH = "path"
		const val IMAGE_DATA_TYPE_URL = "url"
	}

	init {
		if (type != IMAGE_DATA_TYPE_URL && type != IMAGE_DATA_TYPE_PATH) return
		this.type = type
		this.data = data
	}
}