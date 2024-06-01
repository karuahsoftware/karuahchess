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

package purpletreesoftware.karuahchess.customcontrol

import java.lang.Long.max


class Clock() {

    private var timePoint: Long = 0L
    private var timeElapsedNano: Long = 0L
    private var timeLimitNano: Long = 0L

    /**
     * Starts the timer
     */
    fun start() {
        if (timePoint == 0L) {
            timePoint = System.nanoTime()
        }
    }

    /**
     * Pause the timer
     */
    fun pause() {
        if (timePoint > 0L){
            timeElapsedNano = timeElapsedNano + System.nanoTime() - timePoint
            timePoint = 0L
        }
    }


    /**
     * Set a new time limit
     */
    fun setNewLimitNano(pNanoSeconds: Long) {
        timeElapsedNano = 0L
        timePoint = 0L
        timeLimitNano = pNanoSeconds
    }

    /**
     * Calculates remaining nano seconds
     */
    fun remainingNano() : Long {

        val elapsedNano: Long =  if (timePoint > 0L) {
            timeElapsedNano + System.nanoTime() - timePoint
        } else {
            timeElapsedNano
        }

        return max(timeLimitNano - elapsedNano, 0)
    }

    /**
     * Returns true if the clock is paused
     */
    fun isPaused(): Boolean {
        return timePoint == 0L
    }


}