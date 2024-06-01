/*
Karuah Chess is a chess playing program
Copyright (C) 2020-2023 Karuah Software

Karuah Chess is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Karuah Chess is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package purpletreesoftware.karuahchess.common

import android.content.res.Resources
import android.util.TypedValue
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlin.math.roundToLong


inline fun ViewGroup.afterMeasured(crossinline f: ViewGroup.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun Float.spToPx(): Int = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)).toInt()

fun Float.dpToPx(): Int = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)).toInt()

/**
 * Get hours component from seconds
 */
fun Int.hoursFromSeconds() : Int = (this / 3600f).toInt()
fun Long.hoursFromSeconds() : Int = (this / 3600f).toInt()

/**
 * Get minutes component from seconds
 */
fun Int.minutesFromSeconds() : Int = ((this % 3600f) / 60).toInt()
fun Long.minutesFromSeconds() : Int = ((this % 3600f) / 60).toInt()

/**
* Get seconds component from seconds
*/
fun Int.secondsFromSeconds() : Int = ((this % 3600f) % 60 ).toInt()
fun Long.secondsFromSeconds() : Int = ((this % 3600f) % 60 ).toInt()

/**
 * Creates a padded digit from and int. e.g. 1 returns 01
 */
fun Int.paddedDigit() : String = String.format("%02d",this)

fun Long.nanoSecondsToSeconds(): Long = (this / 1000000000.0).roundToLong()
fun Int.secondsToNanoSeconds(): Long = this * 1000000000L