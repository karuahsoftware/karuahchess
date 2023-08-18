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
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace PurpleTreeSoftware.Panel
{
    public class CanvasCustom : Canvas
    {
        public Object Entity
        {
            get { return (Object)GetValue(EntityProperty); }
            set { SetValue(EntityProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Entity.
        public static readonly DependencyProperty EntityProperty =
            DependencyProperty.Register("Entity", typeof(Object), typeof(CanvasCustom), new PropertyMetadata(null));




        public int Id
        {
            get { return (int)GetValue(IndexProperty); }
            set { SetValue(IndexProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Index. 
        public static readonly DependencyProperty IndexProperty =
            DependencyProperty.Register("Id", typeof(int), typeof(CanvasCustom), new PropertyMetadata(0));

    }
}
