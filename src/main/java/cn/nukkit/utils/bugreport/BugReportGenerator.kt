package cn.nukkit.utils.bugreport

import cn.nukkit.Nukkit
import cn.nukkit.Server
import cn.nukkit.utils.Utils
import com.sun.management.OperatingSystemMXBean
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.management.ManagementFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

/**
 * Project nukkit
 */
class BugReportGenerator internal constructor(private val throwable: Throwable) : Thread() {
	override fun run() {
		val baseLang = Server.instance.language
		try {
			Server.instance.logger.info("[BugReport] " + baseLang.translateString("nukkit.bugreport.create"))
			val path = generate()
			Server.instance.logger.info("[BugReport] " + baseLang.translateString("nukkit.bugreport.archive", path))
		} catch (e: Exception) {
			val stringWriter = StringWriter()
			e.printStackTrace(PrintWriter(stringWriter))
			Server.instance.logger.info("[BugReport] " + baseLang.translateString("nukkit.bugreport.error", stringWriter.toString()))
		}
	}

	@Throws(IOException::class)
	private fun generate(): String {
		val reports = File(Nukkit.DATA_PATH, "logs/bug_reports")
		if (!reports.isDirectory) {
			reports.mkdirs()
		}
		val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmSS")
		val date = simpleDateFormat.format(Date())
		val model = StringBuilder()
		var totalDiskSpace: Long = 0
		var diskNum = 0
		for (root in FileSystems.getDefault().rootDirectories) {
			try {
				val store = Files.getFileStore(root)
				model.append("Disk ").append(diskNum++).append(":(avail=").append(getCount(store.usableSpace, true))
						.append(", total=").append(getCount(store.totalSpace, true))
						.append(") ")
				totalDiskSpace += store.totalSpace
			} catch (e: IOException) {
				//
			}
		}
		val stringWriter = StringWriter()
		throwable.printStackTrace(PrintWriter(stringWriter))
		val stackTrace = throwable.stackTrace
		var pluginError = false
		if (stackTrace.size > 0) {
			pluginError = !throwable.stackTrace[0].className.startsWith("cn.nukkit")
		}
		val mdReport = File(reports, date + "_" + throwable.javaClass.simpleName + ".md")
		mdReport.createNewFile()
		var content = Utils.readFile(this.javaClass.classLoader.getResourceAsStream("report_template.md"))
		val cpuType = System.getenv("PROCESSOR_IDENTIFIER")
		val osMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
		content = content.replace("\${NUKKIT_VERSION}", Nukkit.VERSION)
		content = content.replace("\${JAVA_VERSION}", System.getProperty("java.vm.name") + " (" + System.getProperty("java.runtime.version") + ")")
		content = content.replace("\${HOSTOS}", osMXBean.name + "-" + osMXBean.arch + " [" + osMXBean.version + "]")
		content = content.replace("\${MEMORY}", getCount(osMXBean.totalPhysicalMemorySize, true))
		content = content.replace("\${STORAGE_SIZE}", getCount(totalDiskSpace, true))
		content = content.replace("\${CPU_TYPE}", cpuType ?: "UNKNOWN")
		content = content.replace("\${AVAILABLE_CORE}", osMXBean.availableProcessors.toString())
		content = content.replace("\${STACKTRACE}", stringWriter.toString())
		content = content.replace("\${PLUGIN_ERROR}", java.lang.Boolean.toString(pluginError).toUpperCase())
		content = content.replace("\${STORAGE_TYPE}", model.toString())
		Utils.writeFile(mdReport, content)
		return mdReport.absolutePath
	}

	companion object {
		//Code section from SOF
		fun getCount(bytes: Long, si: Boolean): String {
			val unit = if (si) 1000 else 1024
			if (bytes < unit) return "$bytes B"
			val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
			val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
			return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
		}
	}

}