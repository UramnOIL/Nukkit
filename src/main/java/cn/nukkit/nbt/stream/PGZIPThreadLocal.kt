package cn.nukkit.nbt.stream

class PGZIPThreadLocal(parent: PGZIPOutputStream) : ThreadLocal<PGZIPState?>() {
	private val parent: PGZIPOutputStream

	@Override
	protected fun initialValue(): PGZIPState {
		return PGZIPState(parent)
	}

	init {
		this.parent = parent
	}
}