package cn.nukkit.form.element

import com.google.gson.annotations.SerializedName

class ElementToggle @JvmOverloads constructor(var text: String, @field:SerializedName("default") var isDefaultValue: Boolean = false) : Element() {
	private val type = "toggle" //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999

}