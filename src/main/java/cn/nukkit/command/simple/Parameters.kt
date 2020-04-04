package cn.nukkit.command.simple

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @author nilsbrychzy
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
annotation class Parameters(val name: String, val parameters: Array<Parameter>)