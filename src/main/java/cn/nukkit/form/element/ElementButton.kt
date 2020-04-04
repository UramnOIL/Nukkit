package cn.nukkit.form.element

class ElementButton {
	var text = ""
	var image: ElementButtonImageData? = null
		private set

	constructor(text: String) {
		this.text = text
	}

	constructor(text: String, image: ElementButtonImageData) {
		this.text = text
		if (!image.data.isEmpty() && !image.type.isEmpty()) this.image = image
	}

	fun addImage(image: ElementButtonImageData) {
		if (!image.data.isEmpty() && !image.type.isEmpty()) this.image = image
	}
}