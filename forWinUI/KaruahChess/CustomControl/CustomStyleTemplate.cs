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
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;

namespace KaruahChess.CustomControl
{
    public class CustomStyleTemplate : DependencyObject
    {
        
        /// <summary>
        /// Corner radius
        /// </summary>
        public CornerRadius CornerRadius
        {
            get { return (CornerRadius)GetValue(CornerRadiusProperty); }
            set { SetValue(CornerRadiusProperty, value); }
        }

        // Using a DependencyProperty as the backing store for CornerRadius. 
        public static readonly DependencyProperty CornerRadiusProperty =
            DependencyProperty.Register("CornerRadius", typeof(CornerRadius), typeof(CustomStyleTemplate), new PropertyMetadata(null));



        /// <summary>
        /// Background colour of Message
        /// </summary>
        public Brush Background
        {
            get { return (Brush)GetValue(BackgroundProperty); }
            set { SetValue(BackgroundProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Background. 
        public static readonly DependencyProperty BackgroundProperty =
            DependencyProperty.Register("Background", typeof(Brush), typeof(CustomStyleTemplate), new PropertyMetadata(null));



        /// <summary>
        /// Minimum width of the message
        /// </summary>
        public double MinWidth
        {
            get { return (double)GetValue(MinWidthProperty); }
            set { SetValue(MinWidthProperty, value); }
        }

        // Using a DependencyProperty as the backing store for MinWidth.  
        public static readonly DependencyProperty MinWidthProperty =
            DependencyProperty.Register("MinWidth", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata((double)100));



        /// <summary>
        /// Minimum height of the message
        /// </summary>
        public double MinHeight
        {
            get { return (double)GetValue(MinHeightProperty); }
            set { SetValue(MinHeightProperty, value); }
        }

        // Using a DependencyProperty as the backing store for MinHeight. 
        public static readonly DependencyProperty MinHeightProperty =
            DependencyProperty.Register("MinHeight", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata((double)100));



        /// <summary>
        /// Maximum height of the message
        /// </summary>
        public double MaxWidth
        {
            get { return (double)GetValue(MaxWidthProperty); }
            set { SetValue(MaxWidthProperty, value); }
        }

        // Using a DependencyProperty as the backing store for MaxWidth.  
        public static readonly DependencyProperty MaxWidthProperty =
            DependencyProperty.Register("MaxWidth", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata(double.PositiveInfinity));




        /// <summary>
        /// Maximum height of the message
        /// </summary>
        public double MaxHeight
        {
            get { return (double)GetValue(MaxHeightProperty); }
            set { SetValue(MaxHeightProperty, value); }
        }

        // Using a DependencyProperty as the backing store for MaxHeight.  
        public static readonly DependencyProperty MaxHeightProperty =
            DependencyProperty.Register("MaxHeight", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata(double.PositiveInfinity));




        /// <summary>
        /// Width of the message
        /// </summary>
        public double Width
        {
            get { return (double)GetValue(WidthProperty); }
            set { SetValue(WidthProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Width.  
        public static readonly DependencyProperty WidthProperty =
            DependencyProperty.Register("Width", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata(double.NaN));



        /// <summary>
        /// Height of the message
        /// </summary>
        public double Height
        {
            get { return (double)GetValue(HeightProperty); }
            set { SetValue(HeightProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Height. 
        public static readonly DependencyProperty HeightProperty =
            DependencyProperty.Register("Height", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata(double.NaN));



        /// <summary>
        /// Margin around the message
        /// </summary>
        public Thickness Margin
        {
            get { return (Thickness)GetValue(MarginProperty); }
            set { SetValue(MarginProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Margin. 
        public static readonly DependencyProperty MarginProperty =
            DependencyProperty.Register("Margin", typeof(Thickness), typeof(CustomStyleTemplate), new PropertyMetadata(new Thickness(0)));



        /// <summary>
        /// Padding within the message
        /// </summary>
        public Thickness Padding
        {
            get { return (Thickness)GetValue(PaddingProperty); }
            set { SetValue(PaddingProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Padding.  
        public static readonly DependencyProperty PaddingProperty =
            DependencyProperty.Register("Padding", typeof(Thickness), typeof(CustomStyleTemplate), new PropertyMetadata(new Thickness(0)));



        /// <summary>
        /// Border thickness of the message
        /// </summary>
        public Thickness BorderThickness
        {
            get { return (Thickness)GetValue(BorderThicknessProperty); }
            set { SetValue(BorderThicknessProperty, value); }
        }

        // Using a DependencyProperty as the backing store for BorderThickness.  
        public static readonly DependencyProperty BorderThicknessProperty =
            DependencyProperty.Register("BorderThickness", typeof(Thickness), typeof(CustomStyleTemplate), new PropertyMetadata(new Thickness(0)));


        /// <summary>
        /// Border brush colour
        /// </summary>
        public Brush BorderBrush
        {
            get { return (Brush)GetValue(BorderBrushProperty); }
            set { SetValue(BorderBrushProperty, value); }
        }

        // Using a DependencyProperty as the backing store for BorderBrush.  
        public static readonly DependencyProperty BorderBrushProperty =
            DependencyProperty.Register("BorderBrush", typeof(Brush), typeof(CustomStyleTemplate), new PropertyMetadata(null));


        


        /// <summary>
        /// Font size of the message icon
        /// </summary>
        public double IconFontSize
        {
            get { return (double)GetValue(IconFontSizeProperty); }
            set { SetValue(IconFontSizeProperty, value); }
        }

        // Using a DependencyProperty as the backing store for IconFontSize.  This enables animation, styling, binding, etc...
        public static readonly DependencyProperty IconFontSizeProperty =
            DependencyProperty.Register("IconFontSize", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata((double)11));



        /// <summary>
        /// Opacity of the control
        /// </summary>
        public double Opacity
        {
            get { return (double)GetValue(OpacityProperty); }
            set { SetValue(OpacityProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Opacity.  
        public static readonly DependencyProperty OpacityProperty =
            DependencyProperty.Register("Opacity", typeof(double), typeof(CustomStyleTemplate), new PropertyMetadata((double)1));



    }
}
