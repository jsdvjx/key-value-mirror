package ke.bb

import android.content.Context

abstract class BaseKeyValue(protected val control: Control)


class KeyValue(private val context: Context) {
    private val control = Control(context)
    fun <T> getInstance(clazz: Class<T>): T {
        val clazzName = "${clazz.name}Impl"
        return Class.forName(clazzName).getConstructor(Control::class.java).newInstance(control) as T
    }
}