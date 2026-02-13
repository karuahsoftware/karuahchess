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


using KaruahChess.Pieces;
using System;
using Windows.ApplicationModel.DataTransfer;
using Windows.Foundation;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Media.Imaging;

namespace PurpleTreeSoftware.Panel
{
    public sealed partial class Tile : UserControl
    {
        
        private static int _id = 0;
        private Piece _pieceCtrl;
        private TilePanel _parentPanel;       

        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="pEntity">The entity to return when the tile is clicked</param>
        /// <param name="pDisplayCtrl">The user control to display on the tile</param>
        
        public Tile(Object pEntity, Piece pPieceCtrl)
        {
            this.InitializeComponent();

            _id++;
            
            // Set the object to return when tile clicked
            tileMain.Entity = pEntity;
            tileMain.Id = _id;
                            
            // Set the object to display on the tile
            if (pPieceCtrl != null) {
                pPieceCtrl.IsHitTestVisible = false;
                tileStack.Children.Add(pPieceCtrl);
                _pieceCtrl = pPieceCtrl;
            }
                       
        }


        /// <summary>
        /// Returns the tile index value
        /// </summary>
        public int Id { get { return tileMain.Id; }  }

        /// <summary>
        /// A class containing styling info for the tile
        /// </summary>
        public TileStyleTemplate StyleTemplate {get; set;}


        /// <summary>
        /// Returns the object associated with the tile
        /// </summary>
        public Object Entity { get { return tileMain.Entity; } }


        /// <summary>
        /// Returns the coordinates of the tile
        /// </summary>
        /// <returns></returns>
        public Point Coordinates()
        {            
            UIElement parentPanel = VisualTreeHelper.GetParent(this) as UIElement;                            
            UIElement parentCanvas = VisualTreeHelper.GetParent(parentPanel) as UIElement;            
            var genTransform = tileStack.TransformToVisual(parentCanvas);
            Rect rect = genTransform.TransformBounds(new Rect(0, 0, this.ActualWidth, this.ActualHeight));
            Point pt = new Point(rect.Left, rect.Top);

            return pt;
        }


        /// <summary>
        /// Set visibility of the user control on the tile
        /// </summary>
        /// <param name="pVisibility"></param>
        public void SetVisibility(Visibility pVisibility)
        {
            _pieceCtrl.Visibility = pVisibility;
        }

        /// <summary>
        /// Set rotation of tile
        /// </summary>
        /// <param name="pRotationZ"></param>
        public void SetRotation(int pRotationZ)
        {
            PlaneProjection proj = new PlaneProjection();
            proj.RotationZ = pRotationZ;
            tileStack.Projection = proj;
           
        }

       
        /// <summary>
        /// Pointer entered event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void tileMain_PointerEntered(object sender, Microsoft.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            VisualStateManager.GoToState(this, TileHover.Name, false);
        }

        /// <summary>
        /// Pointer exited event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void tileMain_PointerExited(object sender, Microsoft.UI.Xaml.Input.PointerRoutedEventArgs e)
        {           
            VisualStateManager.GoToState(this, TileNormal.Name, false);      
           
        }


        /// <summary>
        /// Pointer pressed event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void tileMain_PointerPressed(object sender, Microsoft.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            VisualStateManager.GoToState(this, TilePressed.Name, false);

            
        }

        /// <summary>
        /// Pointer released event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void tileMain_PointerReleased(object sender, Microsoft.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            VisualStateManager.GoToState(this, TileNormal.Name, false);
        }
        
        

        /// <summary>
        /// Drag over event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void tileMain_DragOver(object sender, DragEventArgs e)
        {
            e.AcceptedOperation = DataPackageOperation.Move;
            e.DragUIOverride.IsCaptionVisible = false;
            e.DragUIOverride.IsGlyphVisible = false;           
        }

        /// <summary>
        /// Drop event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void tileMain_Drop(object sender, DragEventArgs e)
        {
            int tileIdDropped = (int)e.DataView.Properties["tileId"];
            
            if(_parentPanel != null)
            {
                _parentPanel.CallMoveAction(tileIdDropped - 1, this.Id - 1);
            }

        }

        /// <summary>
        /// Drag starting event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void tileMain_DragStarting(UIElement sender, DragStartingEventArgs args)
        {
            
            args.Data.Properties.Add("tileId", Id);

            if (_pieceCtrl.ImageData != null) {
                _parentPanel.dragInProgress = true;
                BitmapImage pieceImg = new BitmapImage(_pieceCtrl.ImageData.UriSource);
                pieceImg.DecodePixelWidth = _pieceCtrl.ImageData.DecodePixelWidth;
                pieceImg.DecodePixelHeight = _pieceCtrl.ImageData.DecodePixelHeight;
                args.DragUI.SetContentFromBitmapImage(pieceImg);

                SetVisibility(Visibility.Collapsed);
            }
            else {
                args.Cancel = true;
            }

        }

        /// <summary>
        /// Drop completed event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void tileMain_DropCompleted(UIElement sender, DropCompletedEventArgs args)
        {
            SetVisibility(Visibility.Visible);
            _parentPanel.dragInProgress = false;
        }

        
        /// <summary>
        /// Sets a reference to the parent tile panel
        /// </summary>
        /// <param name="pTilePanel"></param>
        public void SetParentPanel(TilePanel pTilePanel)
        {
            _parentPanel = pTilePanel;
        }

        
    }
}
