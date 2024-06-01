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

using System;
using System.Diagnostics;

namespace KaruahChess.CustomControl
{
    public class Clock
    {
        Stopwatch timeStopwatch = new Stopwatch();
        TimeSpan timeLimit = new TimeSpan(0, 0, 0);

       
        /// <summary>
        /// Starts the timer
        /// </summary>
        public void Start()
        {
            timeStopwatch.Start();
        }

        /// <summary>
        /// Pause the timer
        /// </summary>
        public void Pause()
        {
            if (timeStopwatch.IsRunning)
            {
                timeStopwatch.Stop();
            }
        }


        /// <summary>
        /// Set a new time limit
        /// </summary>
        public void SetNewLimit(TimeSpan pTimeLimit)
        {
            timeStopwatch.Stop();
            timeStopwatch.Reset();
            timeLimit = pTimeLimit;            
        }


        /// <summary>
        /// Calculates remaining time
        /// </summary>
        public TimeSpan RemainingTime() {
            
            if(TimeSpan.Compare(timeLimit,timeStopwatch.Elapsed) <= 0)
            {
                return new TimeSpan(0, 0, 0);
            }
            else
            {
                return timeLimit.Subtract(timeStopwatch.Elapsed);
            }
        }

        
        /// <summary>
        /// Returns true if the clock is paused
        /// </summary>
        public bool IsPaused() { 
            return !timeStopwatch.IsRunning;
        }

    }
}
