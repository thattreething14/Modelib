package tree.modelib.utils

import kotlin.math.pow
import kotlin.math.roundToInt

object Round {

    /**
     * Rounds a number to a specified number of decimal places.
     * @param value The number to round.
     * @param places The number of decimal places to round to.
     * @return The rounded number.
     */
    fun decimalPlaces(value: Double, places: Int): Double {
        val number = 10.0.pow(places)
        return (value * number).roundToInt() / number
    }

    /**
     * Rounds a number to 4 decimal places.
     * @param value The number to round.
     * @return The rounded number.
     */
    fun fourDecimalPlaces(value: Double): Double {
        return decimalPlaces(value, 4)
    }

    /**
     * Rounds a number to 2 decimal places.
     *
     * @param value The number to round.
     * @return The rounded number.
     */
    fun twoDecimalPlaces(value: Double): Double {
        return decimalPlaces(value, 2)
    }

    /**
     * Rounds a number to 1 decimal places.
     *
     * @param value The number to round.
     * @return The rounded number.
     */
    fun oneDecimalPlace(value: Double): Double {
        return decimalPlaces(value, 1)
    }

}