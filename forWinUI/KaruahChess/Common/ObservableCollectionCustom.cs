/*
Karuah Chess is a chess playing program
Copyright (C) 2020-2026 Karuah Software

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
using System.Collections.ObjectModel;
using System.Collections.Specialized;

namespace KaruahChess.Common
{
    public class ObservableCollectionCustom<T> : ObservableCollection<T>
    {

        private bool _notificationSupressed = false;
        private bool _supressNotification = false;
        public bool SupressNotification
        {
            get
            {
                return _supressNotification;
            }
            set
            {
                _supressNotification = value;
                if (_supressNotification == false && _notificationSupressed)
                {
                    this.OnCollectionChanged(new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Reset));
                    _notificationSupressed = false;
                }
            }
        }

        protected override void OnCollectionChanged(NotifyCollectionChangedEventArgs e)
        {
            if (SupressNotification)
            {
                _notificationSupressed = true;
                return;
            }
            base.OnCollectionChanged(e);
        }

    }
}
