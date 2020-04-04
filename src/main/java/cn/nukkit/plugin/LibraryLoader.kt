package cn.nukkit.plugin

import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.logging.Logger

/**
 * Created on 15-12-13.
 */
object LibraryLoader {
	private val BASE_FOLDER = File("./libraries")
	private val LOGGER = Logger.getLogger("LibraryLoader")
	private const val SUFFIX = ".jar"
	fun load(library: String) {
		val split = library.split(":").toTypedArray()
		require(split.size == 3) { library }
		load(object : Library {
			override fun getGroupId(): String {
				return split[0]
			}

			override fun getArtifactId(): String {
				return split[1]
			}

			override fun getVersion(): String {
				return split[2]
			}
		})
	}

	fun load(library: Library) {
		val filePath = library.groupId.replace('.', '/') + '/' + library.artifactId + '/' + library.version
		val fileName = library.artifactId + '-' + library.version + SUFFIX
		val folder = File(BASE_FOLDER, filePath)
		if (folder.mkdirs()) {
			LOGGER.info("Created " + folder.path + '.')
		}
		val file = File(folder, fileName)
		if (!file.isFile) try {
			val url = URL("https://repo1.maven.org/maven2/$filePath/$fileName")
			LOGGER.info("Get library from $url.")
			Files.copy(url.openStream(), file.toPath())
			LOGGER.info("Get library $fileName done!")
		} catch (e: IOException) {
			throw LibraryLoadException(library)
		}
		try {
			val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
			val accessible = method.isAccessible
			if (!accessible) {
				method.isAccessible = true
			}
			val classLoader = Thread.currentThread().contextClassLoader as URLClassLoader
			val url = file.toURI().toURL()
			method.invoke(classLoader, url)
			method.isAccessible = accessible
		} catch (e: NoSuchMethodException) {
			throw LibraryLoadException(library)
		} catch (e: MalformedURLException) {
			throw LibraryLoadException(library)
		} catch (e: IllegalAccessException) {
			throw LibraryLoadException(library)
		} catch (e: InvocationTargetException) {
			throw LibraryLoadException(library)
		}
		LOGGER.info("Load library $fileName done!")
	}

	fun getBaseFolder(): File {
		return BASE_FOLDER
	}

	init {
		if (BASE_FOLDER.mkdir()) {
			LOGGER.info("Created libraries folder.")
		}
	}
}