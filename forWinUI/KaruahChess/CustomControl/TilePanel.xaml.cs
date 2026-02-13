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
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using Windows.Foundation;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Input;

namespace PurpleTreeSoftware.Panel
{

    public sealed partial class TilePanel : UserControl
    {
        // variables
        INotifyCollectionChanged _notifyTiles;
        public bool dragInProgress = false;


        /// <summary>
        /// The tiles to display
        /// </summary>
        public IEnumerable<Tile> Tiles
        {
            get { return (IEnumerable<Tile>)GetValue(TilesProperty); }
            set {
                // Set the value
                SetValue(TilesProperty, value);

                // Set collection changed event if value implements INotifyCollectionChanged
                if (value is INotifyCollectionChanged)
                {
                    _notifyTiles = value as INotifyCollectionChanged;
                    _notifyTiles.CollectionChanged += OnTilesCollectionChanged;
                }
            }
        }

        // Using a DependencyProperty as the backing store for Tiles.  
        public static readonly DependencyProperty TilesProperty =
            DependencyProperty.Register("Tiles", typeof(IEnumerable<Tile>), typeof(TilePanel), new PropertyMetadata(Enumerable.Empty<Tile>(), new PropertyChangedCallback(OnTilesPropertyChanged)));


        /// <summary>
        /// Event that fires if the IEnumerable tiles property changes
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private static void OnTilesPropertyChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e)
        {
            var tilePanel = (TilePanel)sender;           
            tilePanel.CreateLayout();
        }

        /// <summary>
        /// Event that fires if the IEnumerable implements INotifyCollectionChanged and the tiles collection changes
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        void OnTilesCollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            CreateLayout();

        }

        

        /// <summary>
        /// The depth of the grid to create
        /// </summary>
        public int Depth
        {
            get { return (int)GetValue(DepthProperty); }
            set {
                // ensure value is always greater than zero
                if (value > 0) {
                    SetValue(DepthProperty, value);
                }
                else {
                    SetValue(DepthProperty, 1);
                }

            }
        }

        // Using a DependencyProperty as the backing store for Depth.  
        public static readonly DependencyProperty DepthProperty =
            DependencyProperty.Register("Depth", typeof(int), typeof(TilePanel), new PropertyMetadata(1));


        /// <summary>
        /// Locks the panel and stops click events
        /// </summary>
        public bool Lock
        {
            get { return (bool)GetValue(LockProperty); }
            set { SetValue(LockProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Lock.  
        public static readonly DependencyProperty LockProperty =
            DependencyProperty.Register("Lock", typeof(bool), typeof(TilePanel), new PropertyMetadata(false));

        
       
        // Using a DependencyProperty as the backing store for ShowBackgroundText. 
        public static readonly DependencyProperty BackgroundTextEnabledProperty =
            DependencyProperty.Register("BackgroundTextMode", typeof(int), typeof(TilePanel), new PropertyMetadata(false));
                       

        /// <summary>
        /// Property to hold orientation enum
        /// </summary>
        public OrientationEnum Orientation
        {
            get { return (OrientationEnum)GetValue(OrientationProperty); }
            set { SetValue(OrientationProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Orientation. 
        public static readonly DependencyProperty OrientationProperty =
            DependencyProperty.Register("Orientation", typeof(OrientationEnum), typeof(TilePanel), new PropertyMetadata(OrientationEnum.Vertical));



        /// <summary>
        /// Enum to represent the layout orientation
        /// </summary>
        public enum OrientationEnum { Vertical, Horizontal };
        
       

        /// <summary>
        /// Style template to use to format the tile
        /// </summary>
        public TileStyleTemplate StyleTemplate
        {
            get { return (TileStyleTemplate)GetValue(StyleTemplateProperty); }
            set { SetValue(StyleTemplateProperty, value); }
        }
        
        // Using a DependencyProperty as the backing store for StyleTemplate.  
        public static readonly DependencyProperty StyleTemplateProperty =
            DependencyProperty.Register("StyleTemplate", typeof(TileStyleTemplate), typeof(TilePanel), new PropertyMetadata(null));
        

        /// <summary>
        /// Rotation value for the board
        /// </summary>
        public int RotationZ
        {
            get { return (int)GetValue(RotationZProperty); }
            set {
                var origValue = (int)GetValue(RotationZProperty);
                if (origValue != value) {                     
                    SetValue(RotationZProperty, value);                    
                    SetTileRotation(-value);
                }
            }
        }

        // Using a DependencyProperty as the backing store for RotationZ.
        public static readonly DependencyProperty RotationZProperty =
            DependencyProperty.Register("RotationZ", typeof(int), typeof(TilePanel), new PropertyMetadata(0));

        


        /// <summary>
        /// Action to raise when a tile click is detected
        /// </summary>
        public event Action<TilePanel, object, int> TileClicked;


        /// <summary>
        /// Move from square Id to Square Id
        /// </summary>
        public event Action<int, int> MoveAction;



        /// <summary>
        /// Constructor
        /// </summary>        
        public TilePanel()
        {           
            this.InitializeComponent();

            


            // Loads a default style template if one has not been set
            if (StyleTemplate == null)
            {
                StyleTemplate = (TileStyleTemplate)TilesStyleDefaultResourceDictionary["TileStyleTemplateDefaultObject"];
            }
            
           
        }

        

        /// <summary>
        /// Creates the layout
        /// </summary>
        private void CreateLayout()
        {            
            // Set formatting configuration
            var layoutTiles = Tiles;
            
            if (layoutTiles != null) { 

                SetTileFormat(ref layoutTiles);

                // Ensure all children are cleared
                ClearChildren();

                // Add tiles to the relative panel
                CreateRelativePanelItems(TilesRelativePanel, layoutTiles, Depth);

                // Set the Rotation
                SetTileRotation(-RotationZ);

                                              
            }
        }

        /// <summary>
        /// Creates the relative panel items and relationships
        /// </summary>
        /// <param name="pPanel">The relative panel to add children to</param>
        /// <param name="pUIElement">The list of UI elements to add</param>
        private void CreateRelativePanelItems(RelativePanel pPanel, IEnumerable<Tile> pTileList, int pDepth)
        {

            // Initialise variables
            UIElement[] lastColumnAdded = new UIElement[pDepth];                 
            int x = 0;            
           
          
            // Loop through the collection
            foreach (Tile currentTile in pTileList)
            {
                // Set a reference to the parent panel
                currentTile.SetParentPanel(this);

                // Add the UI element to the panel               
                pPanel.Children.Add(currentTile);

                // Create relationships based on orientation
                if (Orientation == OrientationEnum.Horizontal) { 
                    if (lastColumnAdded[x] != null)
                    {
                        RelativePanel.SetRightOf(currentTile, lastColumnAdded[x]);                        
                    }
                
                    if (x > 0 && lastColumnAdded[x - 1] != null)
                    {
                        RelativePanel.SetBelow(currentTile, lastColumnAdded[x-1]);
                    }
                }
                else if (Orientation == OrientationEnum.Vertical)
                {
                    if (lastColumnAdded[x] != null)
                    {
                        RelativePanel.SetBelow(currentTile, lastColumnAdded[x]);
                        
                    }


                    if (x > 0 && lastColumnAdded[x - 1] != null)
                    {
                        RelativePanel.SetRightOf(currentTile, lastColumnAdded[x - 1]);
                    }
                }

                // Record the last tile added
                lastColumnAdded[x] = currentTile;

                // Increment the counter
                x++;

                // Set counter back to zero when depth exceeded
                if (x > pDepth - 1)
                {
                    x = 0;
                }
        
            }
        }

        /// <summary>
        /// Sets formatting config on the tile
        /// </summary>
        /// <param name="pTiles"></param>
        private void SetTileFormat(ref IEnumerable<Tile> pTiles)
        {

            foreach (Tile currentTile in pTiles)
            {                

                // Pass through the tile style template if one has not been set on the tile
                if (currentTile.StyleTemplate == null)
                {
                    currentTile.StyleTemplate = StyleTemplate;
                }

            }
        }

       

        
        /// <summary>
        /// Event that fires when the tile is clicked. Routed event processed here. If an object is found in the tag property an action event is called. 
        /// The tag object is supplied as a parameter in the action event and returned to the subscribers of the event.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void rootTile_PointerReleased(object sender, PointerRoutedEventArgs e)
        {
            if (Lock == false && e.OriginalSource != null) {  
                if (e.OriginalSource.GetType() == typeof(CanvasCustom)) { 
                    var tileStack = (CanvasCustom)e.OriginalSource;                 
                    if (tileStack.Entity != null && TileClicked != null) { 
                        TileClicked(this, tileStack.Entity, tileStack.Id);                        
                    }
                                    
                }
            }            
        }


        /// <summary>
        /// Removes all children from the relative panel control
        /// </summary>
        private void ClearChildren()
        {   

            // Clears the children in the current control'
            foreach (var item in TilesRelativePanel.Children)
            {
                RelativePanel.SetRightOf(item, null);
                RelativePanel.SetBelow(item, null);
            }

            TilesRelativePanel.Children.Clear();


        }

        

        /// <summary>
        /// Sets tile rotation
        /// </summary>
        /// <param name="pRotationZ"></param>
        private void SetTileRotation(int pRotationZ)
        {

            foreach (Tile currentTile in Tiles)
            {
                // Set font size
                currentTile.SetRotation(pRotationZ);
                
            }
        }

        
        /// <summary>
        /// Call the move action event
        /// </summary>
        /// <param name="pFromId"></param>
        /// <param name="pToId"></param>
        public void CallMoveAction(int pFromId, int pToId)
        {
            if(MoveAction != null)
            {
                MoveAction(pFromId, pToId);
            }
        }

        /// <summary>
        /// Get tile coordinates for a specified tile
        /// </summary>
        /// <param name="pTileId"></param>
        /// <returns></returns>
        public Point GetTileCoordinates(int pTileId)
        {
            Point coord = new Point(0,0);
            foreach(Tile tile in Tiles) {
                if (pTileId == tile.Id) coord = tile.Coordinates();
            }

            return coord;
        }

    }
}
