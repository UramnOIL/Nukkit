package cn.nukkit.plugin

/**
 * Created on 15-12-13.
 */
interface Library {
	fun getGroupId(): String
	fun getArtifactId(): String
	fun getVersion(): String
}