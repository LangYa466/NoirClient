package cn.langya.module.value

/**
 * @author LangYa466
 * @date 10/11/2025
 */
open class Value<T>(val name: String, value: T) {
    var value: T = value
        set(value) {
            field = value
            onChange(value)
        }

    open fun onChange(newValue: T) {}

    override fun toString(): String {
        return "Value(name='$name', value=$value)"
    }
}

class NumberValue(
    name: String,
    value: Float,
    val min: Float,
    val max: Float,
    val increment: Float
) : Value<Float>(name, value) {

    override fun onChange(newValue: Float) {
        var v = newValue.toDouble()

        if (v < min.toDouble()) {
            v = min.toDouble()
        }
        if (v > max.toDouble()) {
            v = max.toDouble()
        }

        val remainder = v % increment.toDouble()
        if (remainder != 0.0) {
            v -= remainder
            if (v < min.toDouble()) {
                v = min.toDouble()
            } else if (v > max.toDouble()) {
                v = max.toDouble()
            }
        }

        super.onChange(v.toFloat())
    }
}

class BooleanValue(name: String, value: Boolean) : Value<Boolean>(name, value)

class StringValue(name: String, value: String) : Value<String>(name, value)

class EnumValue<T : Enum<T>>(name: String, value: T) : Value<T>(name, value) {
    val enumClass: Class<T> = value.javaClass

    fun next() {
        val constants = enumClass.enumConstants
        val currentIndex = constants.indexOf(value)
        val nextIndex = (currentIndex + 1) % constants.size
        value = constants[nextIndex]
    }

    fun previous() {
        val constants = enumClass.enumConstants
        val currentIndex = constants.indexOf(value)
        val previousIndex = if (currentIndex - 1 < 0) constants.size - 1 else currentIndex - 1
        value = constants[previousIndex]
    }
}