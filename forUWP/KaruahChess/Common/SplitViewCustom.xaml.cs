/*
Karuah Chess is a chess playing program
Copyright (C) 2020 Karuah Software

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
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;


namespace KaruahChess.Common
{
    public sealed partial class SplitViewCustom : UserControl
    {
        /// <summary>
        /// Panel A space
        /// </summary>
        public object PanelA
        {
            get { return (object)GetValue(PanelAProperty); }
            set { SetValue(PanelAProperty, value); }
        }

        // Using a DependencyProperty as the backing store for PanelA.  
        public static readonly DependencyProperty PanelAProperty =
            DependencyProperty.Register("PanelA", typeof(object), typeof(SplitViewCustom), new PropertyMetadata(null));



        /// <summary>
        /// Panel B space
        /// </summary>
        public object PanelB
        {
            get { return (object)GetValue(PanelBProperty); }
            set { SetValue(PanelBProperty, value); }
        }

        // Using a DependencyProperty as the backing store for PanelB.  
        public static readonly DependencyProperty PanelBProperty =
            DependencyProperty.Register("PanelB", typeof(object), typeof(SplitViewCustom), new PropertyMetadata(null));




        /// <summary>
        /// Panel C space
        /// </summary>
        public object PanelC
        {
            get { return (object)GetValue(PanelCProperty); }
            set { SetValue(PanelCProperty, value); }
        }

        // Using a DependencyProperty as the backing store for PanelC.  
        public static readonly DependencyProperty PanelCProperty =
            DependencyProperty.Register("PanelC", typeof(object), typeof(SplitViewCustom), new PropertyMetadata(null));


        /// <summary>
        /// Panel D space
        /// </summary>
        public object PanelD
        {
            get { return (object)GetValue(PanelDProperty); }
            set { SetValue(PanelDProperty, value); }
        }

        // Using a DependencyProperty as the backing store for PanelD.  
        public static readonly DependencyProperty PanelDProperty =
            DependencyProperty.Register("PanelD", typeof(object), typeof(SplitViewCustom), new PropertyMetadata(null));



        /// <summary>
        /// Panel E space
        /// </summary>
        public object PanelE
        {
            get { return (object)GetValue(PanelEProperty); }
            set { SetValue(PanelEProperty, value); }
        }

        // Using a DependencyProperty as the backing store for PanelE.  
        public static readonly DependencyProperty PanelEProperty =
            DependencyProperty.Register("PanelE", typeof(object), typeof(SplitViewCustom), new PropertyMetadata(null));

        /// <summary>
        /// The panel to show, based on the enum
        /// </summary>
        public Panel ShowPanel
        {
            get { return (Panel)GetValue(ShowPanelProperty); }
            set { SetValue(ShowPanelProperty, value); }
        }

        // Using a DependencyProperty as the backing store for ShowPanel. 
        public static readonly DependencyProperty ShowPanelProperty =
            DependencyProperty.Register("ShowPanel", typeof(Panel), typeof(SplitViewCustom), new PropertyMetadata(0, OnShowPanelChanged));


        /// <summary>
        /// An enum to define the panel types
        /// </summary>
        public enum Panel { A, B, C, D, E };


        /// <summary>
        /// Event that fires when the ShowPanel property changes
        /// </summary>
        /// <param name="pObject"></param>
        /// <param name="pEventArgs"></param>
        private static void OnShowPanelChanged(DependencyObject pObject, DependencyPropertyChangedEventArgs pEventArgs)
        {
            var viewObj = (SplitViewCustom)pObject;

            if (viewObj.ShowPanel == Panel.A)
            {
                viewObj.PanelAContent.Visibility = Visibility.Visible;
                viewObj.PanelBContent.Visibility = Visibility.Collapsed;
                viewObj.PanelCContent.Visibility = Visibility.Collapsed;
                viewObj.PanelDContent.Visibility = Visibility.Collapsed;
                viewObj.PanelEContent.Visibility = Visibility.Collapsed;
            }
            else if (viewObj.ShowPanel == Panel.B)
            {
                viewObj.PanelAContent.Visibility = Visibility.Collapsed;
                viewObj.PanelBContent.Visibility = Visibility.Visible;
                viewObj.PanelCContent.Visibility = Visibility.Collapsed;
                viewObj.PanelDContent.Visibility = Visibility.Collapsed;
                viewObj.PanelEContent.Visibility = Visibility.Collapsed;
            }
            else if (viewObj.ShowPanel == Panel.C)
            {
                viewObj.PanelAContent.Visibility = Visibility.Collapsed;
                viewObj.PanelBContent.Visibility = Visibility.Collapsed;
                viewObj.PanelCContent.Visibility = Visibility.Visible;
                viewObj.PanelDContent.Visibility = Visibility.Collapsed;
                viewObj.PanelEContent.Visibility = Visibility.Collapsed;
            }
            else if (viewObj.ShowPanel == Panel.D)
            {
                viewObj.PanelAContent.Visibility = Visibility.Collapsed;
                viewObj.PanelBContent.Visibility = Visibility.Collapsed;
                viewObj.PanelCContent.Visibility = Visibility.Collapsed;
                viewObj.PanelDContent.Visibility = Visibility.Visible;
                viewObj.PanelEContent.Visibility = Visibility.Collapsed;
            }
            else if (viewObj.ShowPanel == Panel.E)
            {
                viewObj.PanelAContent.Visibility = Visibility.Collapsed;
                viewObj.PanelBContent.Visibility = Visibility.Collapsed;
                viewObj.PanelCContent.Visibility = Visibility.Collapsed;
                viewObj.PanelDContent.Visibility = Visibility.Collapsed;
                viewObj.PanelEContent.Visibility = Visibility.Visible;
            }
            else
            {
                viewObj.PanelAContent.Visibility = Visibility.Collapsed;
                viewObj.PanelBContent.Visibility = Visibility.Collapsed;
                viewObj.PanelCContent.Visibility = Visibility.Collapsed;
                viewObj.PanelDContent.Visibility = Visibility.Collapsed;
            }
        }


        public SplitViewCustom()
        {
            this.InitializeComponent();
        }
    }
}
