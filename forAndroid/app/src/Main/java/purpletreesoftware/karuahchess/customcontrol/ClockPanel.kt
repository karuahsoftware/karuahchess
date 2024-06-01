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

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.coroutines.*
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.common.*
import purpletreesoftware.karuahchess.databinding.ClockpanelBinding
import java.lang.Runnable


@ExperimentalUnsignedTypes
class ClockPanel : LinearLayout {
    private var _binding: ClockpanelBinding? = null
    private val binding get() = _binding!!
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockRunnable: Runnable
    val whiteClock: Clock = Clock()
    val blackClock: Clock = Clock()
    private val mainActivity = context as MainActivity
    private var clockIsTicking: Boolean = false
    private var whiteHistoricalSeconds: Long = 0
    private var blackHistoricalSeconds: Long = 0
    private var displayHistorical: Boolean = false
    private var clockEnabled = false


    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initialize(context)

    }

    init {
        clockRunnable = Runnable {
            refreshClockDisplay()
            runClockTick()
            checkTimeExpired()
        }

    }

    private fun initialize(context: Context) {
        // Inflate view
        _binding = ClockpanelBinding.inflate(LayoutInflater.from(context), this, true)

        // Pause clock button
        binding.clockStartPauseButton.setOnClickListener {
            if (isPaused()) {
                mainActivity.checkChessClock()
            }
            else {
                pauseClock()
            }
        }

        // Edit button
        binding.clockEditButton.setOnClickListener {
            mainActivity.showClockSettingsDialog()
        }

    }



    /**
     * Sets the orientation of the control
     */
    fun setOrientation(pIsPortrait: Boolean) {
        // Set to vertical or horizontal layout depending on screen rotation
        if(!pIsPortrait) {
            binding.mainClockLayout.orientation = LinearLayout.VERTICAL
        }
        else {
            binding.mainClockLayout.orientation = LinearLayout.HORIZONTAL
        }
    }


    /**
     * Start the clock
     */
    fun start(pPieceColour: Int) {
        if (!clockEnabled) return

        if(pPieceColour == Constants.WHITEPIECE) {
            blackClock.pause()
            whiteClock.start()
        } else {
            whiteClock.pause()
            blackClock.start()
        }

        if (!clockIsTicking) {
            refreshClockDisplay()
            runClockTick()
        }

        setEditButton()
    }

    /**
     * Pause the clock
     */
    fun pauseClock() {
        if (!clockEnabled) return

        whiteClock.pause()
        blackClock.pause()
        stopClockTick()
        refreshClockDisplay()
        setEditButton()
    }

    /**
     * Determines if both clocks are paused
     */
    fun isPaused(): Boolean {
        return whiteClock.isPaused() && blackClock.isPaused()
    }

    /**
     * Clock tick
     */
    private fun runClockTick() {
        clockIsTicking = true
        clockHandler.postDelayed(clockRunnable, 1000)
    }

    /**
     * Stop the clock ticking. The clock timer may still be running, this just stops
     * the clock display from being updated.
     */
    private fun stopClockTick() {
        clockHandler.removeCallbacks(clockRunnable)
        clockIsTicking = false
    }

    /**
     * Refresh the clock display
     */
    private fun refreshClockDisplay() {
        if (!displayHistorical) {
            binding.clockWhiteText.text = "${getHHMMSS(whiteClock.remainingNano().nanoSecondsToSeconds())} W"
            binding.clockBlackText.text = "${getHHMMSS(blackClock.remainingNano().nanoSecondsToSeconds())} B"
        }
        else {
            binding.clockWhiteText.text = "${getHHMMSS(whiteHistoricalSeconds)} W"
            binding.clockBlackText.text = "${getHHMMSS(blackHistoricalSeconds)} B"
        }
    }

    /**
     * Convert a time in seconds to a hh:mm:ss formatted string
     */
    private fun getHHMMSS(pSeconds: Long) : String {
        return if (pSeconds in 0 until 3600)
            "${pSeconds.minutesFromSeconds().paddedDigit()}:${pSeconds.secondsFromSeconds().paddedDigit()}"
        else {
            "${pSeconds.hoursFromSeconds().paddedDigit()}:${pSeconds.minutesFromSeconds().paddedDigit()}:${pSeconds.secondsFromSeconds().paddedDigit()}"
        }
    }

    /**
     * Show the control
     */
    fun show() {
        this.visibility = View.VISIBLE

        // Set orientation
        setEditButton()

        clockEnabled = true
    }

    /**
     * Initialise the clocks and display
     */
    fun setClock(pWhiteClockSeconds: Int, pBlackClockSeconds: Int) {
        if (!clockEnabled) return

        whiteClock.setNewLimitNano(pWhiteClockSeconds.secondsToNanoSeconds())
        blackClock.setNewLimitNano(pBlackClockSeconds.secondsToNanoSeconds())
        showCurrentTime()
        refreshClockDisplay()
        setEditButton()
    }

    /**
     * Set the display to show a historical time rather than a ticking clock
     */
    fun showHistoricalTime(pWhiteClockSeconds: Int, pBlackClockSeconds: Int) {
        if (!clockEnabled) return

        whiteHistoricalSeconds = pWhiteClockSeconds.toLong()
        blackHistoricalSeconds = pBlackClockSeconds.toLong()
        displayHistorical = true
        refreshClockDisplay()
    }

    /**
     * Display the current clock
     */
    fun showCurrentTime() {
        if (!clockEnabled) return

        displayHistorical = false
        refreshClockDisplay()
    }

    /**
     * Hide the control
     */
    fun hide() {
        pauseClock()
        setClock(0,0)
        this.visibility = View.GONE
        clockEnabled = false
    }

    /**
     * Displays the edit button if the clock is paused
     */
    private fun setEditButton() {
        if (isPaused()) {
            binding.clockEditButton.visibility = VISIBLE
            binding.clockStartPauseButton.setImageResource(R.drawable.ic_play)
        }
        else {
            binding.clockEditButton.visibility = GONE
            binding.clockStartPauseButton.setImageResource(R.drawable.ic_alarm)
        }
    }

    /**
     * Check if time has expired
     */
    private fun checkTimeExpired() {
        if (whiteClock.remainingNano() == 0L && blackClock.remainingNano() > 0L) {
            mainActivity.uiScope.launch(Dispatchers.Main) {
                mainActivity.timeExpired(Constants.WHITEPIECE)
            }
            pauseClock()
        }
        else if (whiteClock.remainingNano() > 0L && blackClock.remainingNano() == 0L){
            mainActivity.uiScope.launch(Dispatchers.Main) {
                mainActivity.timeExpired(Constants.BLACKPIECE)
            }
            pauseClock()
        }
    }




}