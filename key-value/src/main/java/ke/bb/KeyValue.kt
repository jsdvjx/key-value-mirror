package ke.bb

import android.content.Context

abstract class BaseKeyValue(protected val control: Control)


class KeyValue(context: Context) {
    private val control = Control(context)
    private val map: MutableMap<Class<*>, Any> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <T> getInstance(clazz: Class<T>): T {
        val clazzName = "${clazz.name}Impl"
        return (Class.forName(clazzName).getConstructor(Control::class.java).newInstance(control) as T).apply {
            map[clazz] = this as Any
        }
    }

    inline fun <reified T> get(): T {
        return getInstance(T::class.java)
    }
}
