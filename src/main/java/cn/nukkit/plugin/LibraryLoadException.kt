package cn.nukkit.plugin

/**
 * Created on 15-12-13.
 */
class LibraryLoadException(library: Library) : RuntimeException("Load library " + library.artifactId + " failed!")