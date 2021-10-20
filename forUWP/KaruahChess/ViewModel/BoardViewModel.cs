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
using KaruahChess.Common;
using PurpleTreeSoftware.Panel;
using KaruahChess.Model;
using Windows.UI.Xaml;
using Windows.System.Threading;
using Windows.UI.Core;
using KaruahChess.Rules;
using static KaruahChess.Pieces.Piece;
using KaruahChess.Model.ParameterObjects;
using System.Collections.Generic;
using KaruahChess.Database;
using Windows.UI.Xaml.Media;
using Windows.UI;
using System.Threading.Tasks;
using System.Threading;
using KaruahChess.Pieces;
using Windows.Storage.Pickers;
using Windows.Storage;
using Windows.UI.Xaml.Controls;
using KaruahChess.CustomControl;
using Windows.Media.SpeechSynthesis;
using KaruahChess.Voice;
using Windows.Globalization;
using Windows.Media.SpeechRecognition;
using Windows.Media.Core;
using KaruahChessEngine;
using static KaruahChess.Rules.Move;
using Windows.Foundation;


namespace KaruahChess.ViewModel
{
    public class BoardViewModel : BaseViewModel
    {
        // variables             
        bool _boardResizeTimerRunning;
        Move _move ;                           
        CancellationTokenSource _ctsComputer;           
        TextMessage _boardTextMessageControl;
        AboutPage _aboutPageControl;        
        ImportPGN _importPGNControl;
        Export _exportControl;
        MoveNavigator _moveNavigatorControl;
        TilePanel _boardTilePanelControl;
        PieceAnimation _pieceAnimationControl;
        Coordinates _coordinatesControl;
         
        VoiceRecognition _voiceRecogniser;
        ChessClock _chessClockControl;
        VoiceHelp _voiceHelpControl;
        EngineSettings _engineSettingsControl;
        SoundColourSettings _soundColourSettingsControl;
        BoardAnimation _boardAnimation;
        PieceEditTool _pieceEditToolControl;
        LevelIndicator _levelIndicatorControl;
        bool _pawnPromotionDialogOpen = false;
        MediaPlayerElement _mediaplayerReadText;
        MediaPlayerElement _mediaplayerPieceMoveSound;

        SemaphoreSlim navThrottler = new SemaphoreSlim(initialCount: 1);

        public enum MoveTypeEnum { None = 0, Normal = 1, EnPassant = 2, Castle = 3, Promotion = 4 }
        // 0  Game ready, 1 CheckMate, 2 Stalemate, 3 Resign
        public enum BoardStatusEnum { Ready = 0, Checkmate = 1, Stalemate = 2, Resigned = 3 }
        public enum PawnPromotionEnum { Knight = 2, Bishop = 3, Rook = 4, Queen = 5 }



        /// <summary>
        /// Options menu items
        /// </summary>
        private ObservableCollectionCustom<Tile> _boardTiles;
        public ObservableCollectionCustom<Tile> BoardTiles
        {
            get { return _boardTiles; }
            set
            {
                if (_boardTiles != value)
                {
                    _boardTiles = value;
                    RaisePropertyChanged(nameof(BoardTiles));
                }
            }
        }

        /// <summary>
        /// Board Width
        /// </summary>
        private double _boardWidth;
        public double BoardWidth
        {
            get { return _boardWidth; }
            set
            {
                _boardWidth = value;                
                RaisePropertyChanged(nameof(BoardWidth));
            }
        }

        /// <summary>
        /// Board margin to apply
        /// </summary>
        private Thickness _boardCoordinateMargin;
        public Thickness BoardCoordinateMargin
        {
            get { return _boardCoordinateMargin; }
            set
            {
                _boardCoordinateMargin = value;
                RaisePropertyChanged(nameof(BoardCoordinateMargin));
            }
        }

        /// <summary>
        /// Board Width
        /// </summary>
        private Thickness _rootPadding;
        public Thickness RootPadding
        {
            get { return _rootPadding; }
            set
            {
                _rootPadding = value;
                RaisePropertyChanged(nameof(RootPadding));
            }
        }


        /// <summary>
        /// Direction indicator
        /// </summary>
        private SolidColorBrush _directionColour;
        public SolidColorBrush DirectionColour
        {
            get { return _directionColour; }
            set
            {
                _directionColour = value;
                RaisePropertyChanged(nameof(DirectionColour));
            }
        }

        /// <summary>
        /// Opposing Direction indicator
        /// </summary>
        private SolidColorBrush _opposingColour;
        public SolidColorBrush OpposingColour
        {
            get { return _opposingColour; }
            set
            {
                _opposingColour = value;
                RaisePropertyChanged(nameof(OpposingColour));
            }
        }

        /// <summary>
        /// Direction indicator
        /// </summary>
        private SolidColorBrush _borderColour;
        public SolidColorBrush BorderColour
        {
            get { return _borderColour; }
            set
            {
                _borderColour = value;
                RaisePropertyChanged(nameof(BorderColour));
            }
        }

        /// <summary>
        /// Move in progress indicator
        /// </summary>
        private bool _computerMoveProcessing;
        public bool ComputerMoveProcessing
        {
            get { return _computerMoveProcessing; }
            set
            {
                _computerMoveProcessing = value;
                RaisePropertyChanged(nameof(ComputerMoveProcessing));
            }
        }

        /// <summary>
        /// Feedback visibility
        /// </summary>
        private Visibility _feedbackVisibility;
        public Visibility FeedbackVisibility
        {
            get { return _feedbackVisibility; }
            set
            {
                _feedbackVisibility = value;
                RaisePropertyChanged(nameof(FeedbackVisibility));
            }
        }


        /// <summary>
        /// Computer Player Enabled
        /// </summary>  
        private ParamComputerPlayer _computerPlayerEnabled;
        public bool ComputerPlayerEnabled
        {
            get {
                var paramComputerPlayerObj = ParameterDataService.instance.Get<ParamComputerPlayer>();
                _computerPlayerEnabled = paramComputerPlayerObj;
                return _computerPlayerEnabled.Enabled;
                 }
            set
            {                
                if (_computerPlayerEnabled != null && _computerPlayerEnabled.Enabled != value)
                {
                    _computerPlayerEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamComputerPlayer>(_computerPlayerEnabled);
                    UpdateBoardIndicators(GameRecordDataService.instance.GetCurrentGame());
                    RaisePropertyChanged(nameof(ComputerPlayerEnabled));
                }
            }
        }

        /// <summary>
        /// Computer Move First
        /// </summary>  
        private ParamComputerMoveFirst _ComputerMoveFirstEnabled;
        public bool ComputerMoveFirstEnabled
        {
            get
            {
                var paramComputerMoveFirstObj = ParameterDataService.instance.Get<ParamComputerMoveFirst>();
                _ComputerMoveFirstEnabled = paramComputerMoveFirstObj;
                return _ComputerMoveFirstEnabled.Enabled;
            }
            set
            {
                if (_ComputerMoveFirstEnabled != null && _ComputerMoveFirstEnabled.Enabled != value)
                {
                    _ComputerMoveFirstEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamComputerMoveFirst>(_ComputerMoveFirstEnabled);
                    UpdateBoardIndicators(GameRecordDataService.instance.GetCurrentGame());
                    RaisePropertyChanged(nameof(ComputerMoveFirstEnabled));
                }
            }
        }


        /// <summary>
        /// Randomise first move enabled
        /// </summary>  
        private ParamRandomiseFirstMove _randomiseFirstMove;
        public bool RandomiseFirstMoveEnabled
        {
            get
            {
                var paramRandomiseFirstMoveObj = ParameterDataService.instance.Get<ParamRandomiseFirstMove>();
                _randomiseFirstMove = paramRandomiseFirstMoveObj;
                return _randomiseFirstMove.Enabled;
            }
            set
            {
                if (_randomiseFirstMove != null && _randomiseFirstMove.Enabled != value)
                {
                    _randomiseFirstMove.Enabled = value;
                    ParameterDataService.instance.Set<ParamRandomiseFirstMove>(_randomiseFirstMove);
                    UpdateBoardIndicators(GameRecordDataService.instance.GetCurrentGame());
                    RaisePropertyChanged(nameof(RandomiseFirstMoveEnabled));
                }
            }
        }

        /// <summary>
        /// Automatic level progression on win
        /// </summary>  
        private ParamLevelAuto _levelAutoEnabled;
        public bool LevelAutoEnabled
        {
            get
            {
                var paramLevelAutoObj = ParameterDataService.instance.Get<ParamLevelAuto>();
                _levelAutoEnabled = paramLevelAutoObj;
                return _levelAutoEnabled.Enabled;
            }
            set
            {
                if (_levelAutoEnabled != null && _levelAutoEnabled.Enabled != value)
                {
                    _levelAutoEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamLevelAuto>(_levelAutoEnabled);
                    UpdateBoardIndicators(GameRecordDataService.instance.GetCurrentGame());
                    RaisePropertyChanged(nameof(LevelAutoEnabled));
                }
            }
        }

        /// <summary>
        /// Limit Engine Strength
        /// </summary>  
        private ParamLimitEngineStrengthELO _limitEngineStrengthELO;
        public int limitEngineStrengthELO
        {
            get
            {
                var paramLimitEngineStrengthELOObj = ParameterDataService.instance.Get<ParamLimitEngineStrengthELO>();
                _limitEngineStrengthELO = paramLimitEngineStrengthELOObj;
                return _limitEngineStrengthELO.eloRating;
            }
            set
            {
                if (_limitEngineStrengthELO != null && _limitEngineStrengthELO.eloRating != value)
                {
                    _limitEngineStrengthELO.eloRating = value;
                    ParameterDataService.instance.Set<ParamLimitEngineStrengthELO>(_limitEngineStrengthELO);                    
                    RaisePropertyChanged(nameof(limitEngineStrengthELO));
                }
            }
        }

        /// <summary>
        /// Computer Move First
        /// </summary>  
        private ParamLimitAdvanced _limitAdvancedEnabled;
        public bool LimitAdvancedEnabled
        {
            get
            {
                var paramLimitAdvancedObj = ParameterDataService.instance.Get<ParamLimitAdvanced>();
                _limitAdvancedEnabled = paramLimitAdvancedObj;
                return _limitAdvancedEnabled.Enabled;
            }
            set
            {
                if (_limitAdvancedEnabled != null && _limitAdvancedEnabled.Enabled != value)
                {
                    _limitAdvancedEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamLimitAdvanced>(_limitAdvancedEnabled);
                    RaisePropertyChanged(nameof(LimitAdvancedEnabled));
                }
            }
        }


        /// <summary>
        /// Limit Search Depth
        /// </summary>  
        private ParamLimitDepth _limitDepth;
        public int limitDepth
        {
            get
            {
                var paramLimitDepthObj = ParameterDataService.instance.Get<ParamLimitDepth>();
                _limitDepth = paramLimitDepthObj;
                return _limitDepth.depth;
            }
            set
            {
                if (_limitDepth != null && _limitDepth.depth != value)
                {
                    _limitDepth.depth = value;
                    ParameterDataService.instance.Set<ParamLimitDepth>(_limitDepth);
                    RaisePropertyChanged(nameof(limitDepth));
                }
            }
        }

        /// <summary>
        /// Limit Search Nodes
        /// </summary>  
        private ParamLimitNodes _limitNodes;
        public int limitNodes
        {
            get
            {
                var paramLimitNodesObj = ParameterDataService.instance.Get<ParamLimitNodes>();
                _limitNodes = paramLimitNodesObj;
                return _limitNodes.nodes;
            }
            set
            {
                if (_limitNodes != null && _limitNodes.nodes != value)
                {
                    _limitNodes.nodes = value;
                    ParameterDataService.instance.Set<ParamLimitNodes>(_limitNodes);
                    RaisePropertyChanged(nameof(limitNodes));
                }
            }
        }

        /// <summary>
        /// Limit Search Time
        /// </summary>  
        private ParamLimitMoveDuration _limitMoveDuration;
        public int limitMoveDuration
        {
            get
            {
                var paramLimitMoveDurationObj = ParameterDataService.instance.Get<ParamLimitMoveDuration>();
                _limitMoveDuration = paramLimitMoveDurationObj;
                return _limitMoveDuration.moveDurationMS;
            }
            set
            {
                if (_limitMoveDuration != null && _limitMoveDuration.moveDurationMS != value)
                {
                    _limitMoveDuration.moveDurationMS = value;
                    ParameterDataService.instance.Set<ParamLimitMoveDuration>(_limitMoveDuration);
                    RaisePropertyChanged(nameof(limitMoveDuration));
                }
            }
        }

        /// <summary>
        /// Limit Search Threads
        /// </summary>  
        private ParamLimitThreads _limitThreads;
        public int limitThreads
        {
            get
            {
                var paramLimitThreadsObj = ParameterDataService.instance.Get<ParamLimitThreads>();
                _limitThreads = paramLimitThreadsObj;
                return _limitThreads.threads;
            }
            set
            {
                if (_limitThreads != null && _limitThreads.threads != value)
                {
                    _limitThreads.threads = value;
                    ParameterDataService.instance.Set<ParamLimitThreads>(_limitThreads);
                    RaisePropertyChanged(nameof(limitThreads));
                }
            }
        }

        /// <summary>
        /// Arrange Board Enabled
        /// </summary>  
        private ParamArrangeBoard _arrangeBoardEnabled;
        public bool ArrangeBoardEnabled
        {
            get
            {
                var paramArrangeBoardEnabledObj = ParameterDataService.instance.Get<ParamArrangeBoard>();
                _arrangeBoardEnabled = paramArrangeBoardEnabledObj;
                return _arrangeBoardEnabled.Enabled;
            }
            set
            {
                if (_arrangeBoardEnabled != null && _arrangeBoardEnabled.Enabled != value)
                {
                    _arrangeBoardEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamArrangeBoard>(_arrangeBoardEnabled);
                    BoardSquare.Shake(value);
                    UpdateBoardIndicators(GameRecordDataService.instance.GetCurrentGame());
                    EndPieceAnimation();
                    RaisePropertyChanged(nameof(ArrangeBoardEnabled));
                    if (value == false && _pieceEditToolControl != null) _pieceEditToolControl.Close();
                }
            }
        }

        /// <summary>
        /// Coordinates Enabled
        /// </summary>  
        private ParamBoardCoord _boardCoordEnabled;
        public bool BoardCoordEnabled
        {
            get
            {
                var paramBoardCoordEnabledObj = ParameterDataService.instance.Get<ParamBoardCoord>();
                _boardCoordEnabled = paramBoardCoordEnabledObj;
                return _boardCoordEnabled.Enabled;
            }
            set
            {
                if (_boardCoordEnabled != null && _boardCoordEnabled.Enabled != value)
                {
                    _boardCoordEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamBoardCoord>(_boardCoordEnabled);
                    ResizeBoard();
                    RaisePropertyChanged(nameof(BoardCoordEnabled));
                }
            }
        }

        /// <summary>
        /// Structure Enabled
        /// </summary>  
        private ParamNavigator _navigatorEnabled;
        public bool NavigatorEnabled
        {
            get
            {
                var paramStructureEnabledObj = ParameterDataService.instance.Get<ParamNavigator>();
                _navigatorEnabled = paramStructureEnabledObj;
                return _navigatorEnabled.Enabled;
            }
            set
            {
                if (_navigatorEnabled != null && _navigatorEnabled.Enabled != value)
                {
                    _navigatorEnabled.Enabled = value;
                    RefreshNavigation(true, true);
                    ParameterDataService.instance.Set<ParamNavigator>(_navigatorEnabled);
                    ResizeBoard();
                    RaisePropertyChanged(nameof(NavigatorEnabled));
                }
            }
        }


        /// <summary>
        /// Move highlight Enabled
        /// </summary>  
        private ParamMoveHighlight _moveHighlightEnabled;
        public bool MoveHighlightEnabled
        {
            get
            {
                var paramMoveHighlightEnabledObj = ParameterDataService.instance.Get<ParamMoveHighlight>();
                _moveHighlightEnabled = paramMoveHighlightEnabledObj;
                return _moveHighlightEnabled.Enabled;
            }
            set
            {
                if (_moveHighlightEnabled != null && _moveHighlightEnabled.Enabled != value)
                {
                    _moveHighlightEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamMoveHighlight>(_moveHighlightEnabled);
                    RaisePropertyChanged(nameof(MoveHighlightEnabled));

                }
            }
        }

        /// <summary>
        /// Move highlight Enabled
        /// </summary>  
        private ParamClock _clockEnabled;
        public bool ClockEnabled
        {
            get
            {
                var paramClockEnabledObj = ParameterDataService.instance.Get<ParamClock>();
                _clockEnabled = paramClockEnabledObj;
                return _clockEnabled.Enabled;
            }
            set
            {
                if (_clockEnabled != null && _clockEnabled.Enabled != value)
                {
                    _clockEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamClock>(_clockEnabled);
                    RaisePropertyChanged(nameof(ClockEnabled));

                }
            }
        }

        /// <summary>
        /// Rotate Board Value
        /// </summary>  
        private ParamRotateBoard _rotateBoardValue;
        public int RotateBoardValue
        {
            get
            {
                var paramRotateBoardValueObj = ParameterDataService.instance.Get<ParamRotateBoard>();
                _rotateBoardValue = paramRotateBoardValueObj;
                return _rotateBoardValue.Value;
            }
            set
            {
                if (_rotateBoardValue != null && _rotateBoardValue.Value != value)
                {
                    _rotateBoardValue.Value = value;
                    ParameterDataService.instance.Set<ParamRotateBoard>(_rotateBoardValue);                   
                    RaisePropertyChanged(nameof(RotateBoardValue));

                }
            }
        }

        /// <summary>
        /// Sounds read enabled
        /// </summary>  
        private ParamSoundRead _soundReadEnabled;
        public bool SoundReadEnabled
        {
            get
            {
                var paramSoundEnabledObj = ParameterDataService.instance.Get<ParamSoundRead>();
                _soundReadEnabled = paramSoundEnabledObj;
                return _soundReadEnabled.Enabled;
            }
            set
            {
                if (_soundReadEnabled != null && _soundReadEnabled.Enabled != value)
                {
                    _soundReadEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamSoundRead>(_soundReadEnabled);
                    RaisePropertyChanged(nameof(SoundReadEnabled));

                }
            }
        }

        /// <summary>
        /// Sounds effects enabled
        /// </summary>  
        private ParamSoundEffect _soundEffectEnabled;
        public bool SoundEffectEnabled
        {
            get
            {
                var paramSoundEnabledObj = ParameterDataService.instance.Get<ParamSoundEffect>();
                _soundEffectEnabled = paramSoundEnabledObj;
                return _soundEffectEnabled.Enabled;
            }
            set
            {
                if (_soundEffectEnabled != null && _soundEffectEnabled.Enabled != value)
                {
                    _soundEffectEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamSoundEffect>(_soundEffectEnabled);
                    RaisePropertyChanged(nameof(SoundEffectEnabled));

                }
            }
        }

        /// <summary>
        /// Board dark square colour
        /// </summary>
        private ParamColourDarkSquares _colourDarkSquares;
        public ColourARGB ColourDarkSquaresARGB
        {
            get
            {
                var paramColourDarkSquaresObj = ParameterDataService.instance.Get<ParamColourDarkSquares>();
                _colourDarkSquares = paramColourDarkSquaresObj;
                return _colourDarkSquares.ARGB();
            }
            set
            {
                if (_colourDarkSquares != null && !_colourDarkSquares.ARGB().Equals(value))
                {
                    _colourDarkSquares.A = value.A;
                    _colourDarkSquares.R = value.R;
                    _colourDarkSquares.G = value.G;
                    _colourDarkSquares.B = value.B;
                   
                    ParameterDataService.instance.Set<ParamColourDarkSquares>(_colourDarkSquares);
                    RaisePropertyChanged(nameof(ColourDarkSquaresARGB));

                }
            }
        }

        /// <summary>
        /// Lock panel flag
        /// </summary>
        private bool _lockPanel;
        public bool LockPanel
        {
            get { return _lockPanel; }
            set
            {
                if (_lockPanel != value)
                {
                    _lockPanel = value;
                    RaisePropertyChanged(nameof(LockPanel));
                }
            }
        }
               

        /// <summary>
        /// Stores a reference to the active banner panel
        /// </summary>
        private SplitViewCustom.Panel _bannerPanelActive;
        public SplitViewCustom.Panel BannerPanelActive
        {
            get { return _bannerPanelActive; }
            set
            {
                if (_bannerPanelActive != value)
                {
                    _bannerPanelActive = value;
                    RaisePropertyChanged(nameof(BannerPanelActive));
                }

            }
        }
               

        /// <summary>
        /// Game record current value
        /// </summary>
        private int _gameRecordCurrentValue;
        public int GameRecordCurrentValue
        {
            get { return _gameRecordCurrentValue; }
            set
            {
                if (GameRecordCurrentValue != value) { 
                    _gameRecordCurrentValue = value;
                    RaisePropertyChanged(nameof(GameRecordCurrentValue));
                }
            }
        }


        /// <summary>
        /// Displays detected speech spoke by the user
        /// </summary>
        private string _listenText;
        public string ListenText
        {
            get { return _listenText; }
            set
            {
                if (ListenText != value)
                {
                    _listenText = value;
                    RaisePropertyChanged(nameof(ListenText));
                }
            }
        }

      

        /// <summary>
        /// Clock White offset
        /// </summary>
        private int _clockWhiteOffset;
        public int ClockWhiteOffset
        {
            get { return _clockWhiteOffset; }
            set
            {
                if (ClockWhiteOffset != value)
                {
                    _clockWhiteOffset = value;
                    RaisePropertyChanged(nameof(ClockWhiteOffset));
                }
            }
        }

        /// <summary>
        /// Clock black offset
        /// </summary>
        private int _clockBlackOffset;
        public int ClockBlackOffset
        {
            get { return _clockBlackOffset; }
            set
            {
                if (ClockBlackOffset != value)
                {
                    _clockBlackOffset = value;
                    RaisePropertyChanged(nameof(ClockBlackOffset));
                }
            }
        }

        
        // Constructor
        public BoardViewModel()
        {            

            //Load Animation object
            _boardAnimation = new BoardAnimation();

            // Load media player
            _mediaplayerReadText = new MediaPlayerElement();
            _mediaplayerReadText.AutoPlay = false;
            

            // Load sound effects
            _mediaplayerPieceMoveSound = new MediaPlayerElement();
            _mediaplayerPieceMoveSound.Source = MediaSource.CreateFromUri(new Uri("ms-appx:///Media/piecesound.wav"));
            _mediaplayerPieceMoveSound.AutoPlay = false;
 

            // Create move object
            _move = new Move(BoardSquareDataService.instance);
                        
            
            // Set the window size changed event                        
            Window.Current.SizeChanged += OnWindowSizeChanged;
                                   
            
            // Set feedback button visibility
            SetFeedbackVisibility();

        }


        /// <summary>
        /// Post init method
        /// </summary>
        public void PostInit()
        {
            // Load tiles           
            BoardSquareDataService.instance.Load(35, GameRecordDataService.instance.GetCurrentGame());
            BoardTiles = BoardSquareDataService.instance.BoardTiles;
            
            // Navigate game to latest record
            NavigateMaxRecord();

            // Set initial board size   
            ResizeBoard();

            // Apply board colour
            ApplyBoardColour();

            // Set clock offset
            LoadChessClock(true, GameRecordDataService.instance.GetCurrentGame());

            // Set board shake state
            BoardSquare.Shake(ArrangeBoardEnabled);
                        
                        
        }


        
       
        /// <summary>
        /// Sets board text control
        /// </summary>
        /// <param name="pTextControl"></param>
        public void SetBoardTextMessageControl(TextMessage pTextControl)
        {
            _boardTextMessageControl = pTextControl;
        }


        /// <summary>
        ///  Sets the board tile panel control
        /// </summary>
        /// <param name="pTilePanelControl"></param>
        public void SetBoardTilePanelControl(TilePanel pTilePanelControl)
        {
            _boardTilePanelControl = pTilePanelControl;
        }

                

        /// <summary>
        /// Sets the piece animation control
        /// </summary>
        /// <param name="pPieceAnimationControl"></param>
        public void SetPieceAnimationControl(PieceAnimation pPieceAnimationControl)
        {
            _pieceAnimationControl = pPieceAnimationControl;
           
        }

        
        /// <summary>
        /// Sets the aboutPage control
        /// </summary>
        /// <param name="paboutPageControl"></param>
        public void SetAboutPageControl(AboutPage pAboutPageControl)
        {
            _aboutPageControl = pAboutPageControl;

        }
        
        /// <summary>
        /// Sets the chess clock control
        /// </summary>
        /// <param name="pChessClockControl"></param>
        public void SetChessClockControl(ChessClock pChessClockControl)
        {
            _chessClockControl = pChessClockControl;

        }

        /// <summary>
        /// Sets the import PGN control
        /// </summary>
        /// <param name="pImportPGNControl"></param>
        public void SetImportPGNControl(ImportPGN pImportPGNControl)
        {
            _importPGNControl = pImportPGNControl;

        }

        /// <summary>
        /// Sets the export control
        /// </summary>
        /// <param name="pExportControl"></param>
        public void SetExportControl(Export pExportControl)
        {
            _exportControl = pExportControl;

        }

        /// <summary>
        /// Sets the move navigator control
        /// </summary>
        /// <param name="pMoveNavigatorControl"></param>
        public void SetMoveNavigatorControl(MoveNavigator pMoveNavigatorControl)
        {
            _moveNavigatorControl = pMoveNavigatorControl;

        }

        /// <summary>
        /// Sets the coordiantes control
        /// </summary>
        /// <param name="pCoordinatesControl"></param>
        public void SetCoordinatesControl(Coordinates pCoordinatesControl)
        {
            _coordinatesControl = pCoordinatesControl;

        }

        

        /// <summary>
        /// Sets the voice help control
        /// </summary>
        /// <param name="pVoiceHelpControl"></param>
        public void SetVoiceHelpControl(VoiceHelp pVoiceHelpControl)
        {
            _voiceHelpControl = pVoiceHelpControl;

        }

        /// <summary>
        /// Sets the piece type select control
        /// </summary>        
        public void SetPieceTypeSelectControl(PieceEditTool pPieceEditToolControl)
        {
            _pieceEditToolControl = pPieceEditToolControl;

        }

        /// <summary>
        /// Sets the engine settings control
        /// </summary>
        /// <param name="pEngineSettingsControl"></param>
        public void SetEngineSettingsControl(EngineSettings pEngineSettingsControl)
        {
            _engineSettingsControl = pEngineSettingsControl;

        }

        
        /// <summary>
        /// Sets the sound settings control
        /// </summary>
        /// <param name="pSoundSettingsControl"></param>
        public void SetSoundColourSettingsControl(SoundColourSettings pSoundColourSettingsControl)
        {
            _soundColourSettingsControl = pSoundColourSettingsControl;

        }

        /// <summary>
        /// Sets the level indicator control
        /// </summary>
        /// <param name="pLevelIndicatorControl"></param>
        public void SetLevelIndicatorControl(LevelIndicator pLevelIndicatorControl)
        {
            _levelIndicatorControl = pLevelIndicatorControl;

        }

        /// <summary>
        /// Highlights the last move
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnLastMove_Click(object sender, RoutedEventArgs e)
        {            
            HighlightLastMove();
        }

     

        /// <summary>
        /// Rotate the board
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnRotate_Click(object sender, RoutedEventArgs e)
        {
            var currentRotate = RotateBoardValue;
            int newRotate = (currentRotate - 90);

            if (newRotate > 270 || newRotate < -270) {
                newRotate = 0;
            }

            RotateBoardValue = newRotate;

            if (_coordinatesControl != null) {
                _coordinatesControl.SetCoordLabels(newRotate);
            }

        }

        /// <summary>
        /// Undo the last move
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnUndo_Click(object sender, RoutedEventArgs e)
        {
            LockPanel = true;

            // Clear any moves selected
            _move.Clear();

            // Ensure animations are cleared
            EndPieceAnimation();

            // Cancel any computer move tasks
            if (_ctsComputer != null) {
                _ctsComputer.Cancel();
            }

            // Do the undo
            var oldBoard = GameRecordDataService.instance.Get();
                        
            var undo = GameRecordDataService.instance.Undo();            
            if (undo)
            {
                // Set the clock
                LoadChessClock(true, GameRecordDataService.instance.GetCurrentGame());

                // Do rollback animation
                var moveAnimationList = _boardAnimation.CreateAnimationList(oldBoard, GameRecordDataService.instance.Get(), 1);
                BoardSquareDataService.instance.Update(GameRecordDataService.instance.GetCurrentGame(), false);
                await StartPieceAnimation(true, moveAnimationList, true);


                // Set the game record values                                              
                NavigateMaxRecord();
                               
                
            }

            LockPanel = false;
        }

                
        /// <summary>
        /// Switch the board direction
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnSwitchDirection_Click(object sender, RoutedEventArgs e)
        {
            //Cancel any move tasks 
            if (_ctsComputer != null) {
                _ctsComputer.Cancel();
            }

            // Get current board in view
            KaruahChessEngineClass board = new KaruahChessEngineClass();
            var record = GameRecordDataService.instance.Get(GameRecordCurrentValue);
            board.SetBoardArray(record.BoardArray);
            board.SetStateArray(record.StateArray);

            // Flip direction            
            board.SetStateActiveColour(board.GetStateActiveColour() * (-1));
            board.GetStateArray(record.StateArray);

            // Save changes
            GameRecordDataService.instance.UpdateGameState(record);
            UpdateBoardIndicators(record);

            CheckChessClock();
        }

        
        /// <summary>
        /// Edit board tapped
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnArrangeBoard_Click(object sender, RoutedEventArgs e)
        {
            //Cancel any move tasks 
            if (_ctsComputer != null)
            {
                _ctsComputer.Cancel();
            }

            CheckChessClock();
           

        }

        /// <summary>
        /// Start a new game
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        async public void btnNewGame_Click(object sender, RoutedEventArgs e)
        {
            var dialog = new ContentDialog
            {
                Title = "Start a new game?",
                Content = "",
                CloseButtonText = "Cancel",
                PrimaryButtonText = "Ok",
                PrimaryButtonCommand = new RelayCommand(NewGame)
            };

            await dialog.ShowAsync();
        }


        /// <summary>
        /// Open voice help screen
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnVoiceHelp_Click(object sender, RoutedEventArgs e)
        {
            await HelpVoiceAction(true);

        }

        /// <summary>
        /// Feedback button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnFeedback_Click(object sender, RoutedEventArgs e)
        {
            var launcher = Microsoft.Services.Store.Engagement.StoreServicesFeedbackLauncher.GetDefault();
            await launcher.LaunchAsync();

        }


        /// <summary>
        /// Save game
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnSaveGameRecordFile_Click(object sender, RoutedEventArgs e)
        {
           
            string fileName = "KaruahChess-Game-" + System.DateTime.Now.ToString("dd-MMM-yyy HHmm");

            var fileSavePicker = new FileSavePicker();
            fileSavePicker.SuggestedStartLocation = PickerLocationId.DocumentsLibrary;

            fileSavePicker.FileTypeChoices.Add("GZIP", new[] { ".gz" });
            fileSavePicker.SuggestedFileName = fileName;

            try
            {
                StorageFile exportFile = await ExportDB.Export(ExportDB.ExportType.GameXML);
                StorageFile localfile = await fileSavePicker.PickSaveFileAsync();

                if (exportFile != null && localfile != null)
                {
                    await exportFile.CopyAndReplaceAsync(localfile);                    
                    await ShowBoardMessage("", "Save complete", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);                    
                }

            }
            catch (Exception ex)
            {
                await ShowBoardMessage("Error", ex.Message, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);                                
            }

        }

        /// <summary>
        /// Loads a game record from a file
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnLoadGameRecordFile_Click(object sender, RoutedEventArgs e)
        {
         
            var fileOpenPicker = new Windows.Storage.Pickers.FileOpenPicker();
            fileOpenPicker.SuggestedStartLocation = PickerLocationId.DocumentsLibrary;
            fileOpenPicker.FileTypeFilter.Add(".gz");
            fileOpenPicker.FileTypeFilter.Add(".xml");            

            try
            {
                StorageFile file = await fileOpenPicker.PickSingleFileAsync();

                if (file != null)
                {
                    //Cancel any move tasks 
                    if (_ctsComputer != null)
                    {
                        _ctsComputer.Cancel();
                    }
                    EndPieceAnimation();

                    // Do the import
                    await ImportDB.Import(file, ImportDB.ImportType.GameXML);

                    // Change transaction id
                    GameRecordDataService.instance.newTransaction();

                    // Load the latest record on to the board                    
                    NavigateMaxRecord();

                    var gr = GameRecordDataService.instance.Get();
                    BoardSquareDataService.instance.Update(gr, true);

                    LoadChessClock(true, gr);
                                        
                                        
                    await ShowBoardMessage("", "Load complete", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                    
                }

            }
            catch (Exception ex)
            {
                await ShowBoardMessage("Error", ex.Message, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
            }

        }

        /// <summary>
        /// Resign button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnResign_Click(object sender, RoutedEventArgs e)
        {         
            
            var dialog = new ContentDialog
            {
                Title = "Resign from current game?",
                Content = "",
                CloseButtonText = "Cancel",
                PrimaryButtonText = "Ok",
                PrimaryButtonCommand = new RelayCommand(ResignGame)
            };

            await dialog.ShowAsync();
        }

        /// <summary>
        /// About button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnAbout_Click(object sender, RoutedEventArgs e)
        {
            if (_aboutPageControl != null) {
                _aboutPageControl.Show();
                PositionBoardMessage();                                
            }
        }

        

        /// <summary>
        /// About button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnImportPGN_Click(object sender, RoutedEventArgs e)
        {
            if (_importPGNControl != null)
            {
                _importPGNControl.Show();
                PositionBoardMessage();

            }

        }

        /// <summary>
        /// About button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnExport_Click(object sender, RoutedEventArgs e)
        {
            if (_exportControl != null)
            {
                // Gets the array record of the board currently displayed
                // and sets the export board control to the same.
                GameRecordArray recordArray = GameRecordDataService.instance.Get(GameRecordCurrentValue);
                _exportControl.SetBoard(recordArray);
                _exportControl.Show();
                PositionBoardMessage();

            }

        }


        /// <summary>
        /// Runs when a tile is clicked on the board
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void BoardTilePanel_TileClicked(TilePanel pTilePanel, object pEntity, int pTileId)
        {
            bool gameFinished = !(GameRecordDataService.instance.CurrentGame.GetStateGameStatus() == (int)BoardStatusEnum.Ready);
            
            if (pEntity != null && !ArrangeBoardEnabled) {
                             
                if (gameFinished == false)
                {
                    var sq = (BoardSquare)pEntity;
                    await UserMoveAdd(sq, true);
                }
                else
                {                 
                   int maxRecId = GameRecordDataService.instance.GetMaxId();
                   NavigateGameRecord(maxRecId, false, true, true);                               
                }
               
            }
            else if (_pieceEditToolControl != null && ArrangeBoardEnabled)
            {
                int sqIndex = pTileId - 1;
                if (_pieceEditToolControl.IsOpen())
                {
                    _pieceEditToolControl.Close();
                    BoardSquare.EllipseClearAll();
                }
                else
                {
                    var sq = (BoardSquare)pEntity;

                    if (sq.PieceType == TypeEnum.King)
                    {
                        var record = GameRecordDataService.instance.Get(GameRecordCurrentValue);
                        CastlingRightsDialog castlingRights = new CastlingRightsDialog(sq.PieceType, sq.PieceColour, record, this);
                        castlingRights.ShowAsync();
                    }
                    else
                    {
                        if (sq.PieceColour == ColourEnum.White) _pieceEditToolControl.EditPieceColour = Common.Constants.WHITEPIECE;
                        else if (sq.PieceColour == ColourEnum.Black) _pieceEditToolControl.EditPieceColour = Common.Constants.BLACKPIECE;

                        Point tileCoord = pTilePanel.GetTileCoordinates(pTileId);
                        _pieceEditToolControl.Show(sqIndex, tileCoord, BoardSquareDataService.instance.SquareSize);

                        BoardSquare.EllipseClearAll();
                        if (_pieceEditToolControl.IsOpen())
                        {
                            var sqMark = new HashSet<int>() { sqIndex };
                            SolidColorBrush colour = new SolidColorBrush(Colors.DarkGreen);
                            BoardSquare.EllipseShow(sqMark, colour, true);
                        }
                    }
                }
                
            }

        }

        /// <summary>
        /// Runs when a tile is dragged from one square to another
        /// </summary>
        /// <param name="pFromId"></param>
        /// <param name="pToId"></param>
        public async void BoardTilePanel_MoveAction(int pFromIndex, int pToIndex)
        {
            // Clear move
            _move.Clear();

            // Add the moves
            if (!ArrangeBoardEnabled)
            {
                await UserMoveAdd(BoardSquareDataService.instance.Get(pFromIndex), false);
                await UserMoveAdd(BoardSquareDataService.instance.Get(pToIndex), false);
            }
            else
            {
                ArrangeUpdate(pFromIndex, pToIndex);
            }
        }

        
        /// <summary>
        /// Engine settings button event
        /// </summary>
        public void btnEngineSettings_Click(object sender, RoutedEventArgs e)
        {
            showEngineSettingsDialog();
        }

        /// <summary>
        /// Shows the engine settings dialog
        /// </summary>
        public void showEngineSettingsDialog()
        {
            if (_engineSettingsControl != null)
            {
                _engineSettingsControl.Show();
            }
        }

               
        /// <summary>
        /// Sound settings button event
        /// </summary>
        public void btnSoundColourSettings_Click(object sender, RoutedEventArgs e)
        {
            if (_soundColourSettingsControl != null)
            {
                _soundColourSettingsControl.Show();
            }
        }


        /// <summary>
        /// Starts the animation
        /// </summary>
        /// <param name="pMoveMessage"></param>
        private async Task StartPieceAnimation(bool pLockPanel, List<PieceAnimationInstruction> pAnimationList, bool pEndClear)
        {
            if (pLockPanel) { 
                LockPanel = true;
            }
         
            
            await _pieceAnimationControl.RunAnimation(BoardSquareDataService.instance,pAnimationList);
            if (pEndClear) { 
               EndPieceAnimation();
            }                
          
            if (pLockPanel) {
                LockPanel = false;
            }
        }
        
        /// <summary>
        /// Ends any piece animation
        /// </summary>
        private void EndPieceAnimation()
        {
            
            if (_pieceAnimationControl != null) { 
                _pieceAnimationControl.Clear();
            }

            if (BoardSquareDataService.instance != null)
            {
                BoardSquareDataService.instance.ShowAllHidden();
            }

        }

        /// <summary>
        /// Updates the piece type on a square. Used for editing the board.
        /// </summary>
        /// <param name="pSpin"></param>
        /// <param name="pToIndex"></param>
        public void ArrangeUpdate(char pFen, int pToIndex)
        {
            if (ArrangeBoardEnabled)
            {
                GameRecordArray record = GameRecordDataService.instance.Get(GameRecordCurrentValue);
                _pieceEditToolControl.BufferBoard.SetBoardArray(record.BoardArray);
                _pieceEditToolControl.BufferBoard.SetStateArray(record.StateArray);
                                
                var mResult = _pieceEditToolControl.BufferBoard.ArrangeUpdate(pFen, pToIndex);

                if (mResult.success)
                {
                    _pieceEditToolControl.BufferBoard.GetBoardArray(record.BoardArray);
                    _pieceEditToolControl.BufferBoard.GetStateArray(record.StateArray);                    
                    BoardSquareDataService.instance.Update(record, true);
                    GameRecordDataService.instance.UpdateGameState(record);
                }
                else {
                    ShowBoardMessage("", mResult.returnMessage, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                                        
                }

                // Clear the move selected
                _move.Clear();
            }
        }

        /// <summary>
        /// Moves a piece from one square to another
        /// </summary>
        /// <param name="pSpin"></param>
        /// <param name="pToIndex"></param>
        public void ArrangeUpdate(int pFromIndex, int pToIndex)
        {
            if (ArrangeBoardEnabled)
            {
                GameRecordArray record = GameRecordDataService.instance.Get(GameRecordCurrentValue);
                _pieceEditToolControl.BufferBoard.SetBoardArray(record.BoardArray);
                _pieceEditToolControl.BufferBoard.SetStateArray(record.StateArray);
                                
                var mResult = _pieceEditToolControl.BufferBoard.Arrange(pFromIndex, pToIndex);

                if (mResult.success)
                {
                    _pieceEditToolControl.BufferBoard.GetBoardArray(record.BoardArray);
                    _pieceEditToolControl.BufferBoard.GetStateArray(record.StateArray);
                    BoardSquareDataService.instance.Update(record, true);
                    GameRecordDataService.instance.UpdateGameState(record);
                }
                else
                {
                    ShowBoardMessage("", mResult.returnMessage, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);                   
                }

                // Clear the move selected
                _move.Clear();
            }
        }

        /// <summary>
        /// Start user move.
        /// </summary>
        /// <returns></returns>
        private async Task UserMoveAdd(BoardSquare pBoardSquare, bool pAnimate)
        {            

            // exit function if null or the pawn promotion dialog is open
            if (pBoardSquare == null) {
                return;
            }

            // Ensure game record is set to the latest
            int maxRecId = GameRecordDataService.instance.GetMaxId();
            if (GameRecordCurrentValue != maxRecId) {
                NavigateGameRecord(maxRecId, false, true, true);
                _move.Clear();
                return;
            }
                        
                  
            // Clear message if panel is not locked
            if (!LockPanel){
                await ClearBoardMessage();
            }

            // Check the clock
            CheckChessClock();

            // Select highlight mode
            HighlightEnum highlight;
            if (MoveHighlightEnabled) highlight = HighlightEnum.MovePath;
            else highlight = HighlightEnum.Select;

            // Create proposed move   
            bool moveSelected =_move.Add(pBoardSquare.Index, GameRecordDataService.instance.CurrentGame, highlight);
                        
            // Restart the computer move (if required)
            await StartComputerMoveTask();

            if (moveSelected)
            {
                GameRecordArray boardBeforeMove = GameRecordDataService.instance.GetCurrentGame();                
                
                // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
                int promotionPiece = (int)PawnPromotionEnum.Queen; // default
                if (GameRecordDataService.instance.CurrentGame.IsPawnPromotion(_move.FromIndex, _move.ToIndex))
                {
                    _pawnPromotionDialogOpen = true;
                    var promotionDialog = new PawnPromotionDialog();
                    promotionDialog.CreateContent(GameRecordDataService.instance.CurrentGame.GetStateActiveColour());
                    await promotionDialog.ShowAsync();
                    promotionPiece = promotionDialog.Result;
                    _pawnPromotionDialogOpen = false;
                }

                // Do the move                    
                int gameStatusBeforeMove = GameRecordDataService.instance.CurrentGame.GetStateGameStatus();                    
                var mResult = GameRecordDataService.instance.CurrentGame.Move(_move.FromIndex, _move.ToIndex, promotionPiece, true, true);                    
                               
                if (mResult.success)
                {
                    // Read Text, show message
                    var soundTasks = new List<Task>();
                    if (mResult.returnMessage != String.Empty)
                    {
                        await ShowBoardMessage("", mResult.returnMessage, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                    }
                    else
                    {                        
                        var ssml = GetBoardSquareSSML(mResult.moveDataStr);
                        soundTasks.Add(ReadText(ssml));
                    }

                    // Do animation
                    var boardAfterMove = GameRecordDataService.instance.GetCurrentGame();
                    long transId = GameRecordDataService.instance.transactionId;

                    if (pAnimate) {
                        var moveAnimationList = _boardAnimation.CreateAnimationList(boardBeforeMove, boardAfterMove, 2);
                        BoardSquareDataService.instance.Update(boardAfterMove, false);
                        await StartPieceAnimation(true, moveAnimationList, true);                       
                    }
                    else  {
                        BoardSquareDataService.instance.Update(boardAfterMove, true);
                    }

                    // Piece move sound effect
                    soundTasks.Add(playPieceMoveSoundEffect());

                    // Wait for sound tasks to finish
                    if (soundTasks.Count > 0)
                    {
                        await Task.WhenAll(soundTasks);
                    }

                    // Continue if nothing changed during the animation
                    if (transId == GameRecordDataService.instance.transactionId)
                    {

                        // Check the clock
                        CheckChessClock();

                        // Record the game
                        RecordCurrentGameState();

                        // Update score if checkmate occurred
                        if (gameStatusBeforeMove == (int)BoardStatusEnum.Ready && GameRecordDataService.instance.CurrentGame.GetStateGameStatus() == (int)BoardStatusEnum.Checkmate)
                        {
                            await afterCheckMate(GameRecordDataService.instance.CurrentGame);
                        }

                        // Start computer move if enabled
                        await StartComputerMoveTask();

                    }
                }
                else if (!mResult.success)
                {   
                    await ShowBoardMessage("", mResult.returnMessage, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                    
                }

                // Clear the move selected
                _move.Clear();

            }


        }
        
       

        /// <summary>
        /// Works out if current turn is the computer
        /// </summary>
        /// <returns>True if computer is current turn</returns>
        private bool IsComputerTurn()
        {
            int computerColour = ComputerMoveFirstEnabled ? (int)ColourEnum.White : (int)ColourEnum.Black;
            var turnColour = GameRecordDataService.instance.CurrentGame.GetStateActiveColour();

            if (computerColour == turnColour && ComputerPlayerEnabled) return true;
            else return false;
        }


        /// <summary>
        /// Start Computer Move task
        /// </summary>
        /// <returns></returns>
        private async Task StartComputerMoveTask()
        {

            int computerColour = ComputerMoveFirstEnabled ? (int)ColourEnum.White : (int)ColourEnum.Black;
            var turnColour = GameRecordDataService.instance.CurrentGame.GetStateActiveColour();
            var boardStatus = GameRecordDataService.instance.CurrentGame.GetStateGameStatus();
            
            if (boardStatus == 0 && (!ComputerMoveProcessing) && (!ArrangeBoardEnabled) && ComputerPlayerEnabled && computerColour == turnColour) {
                LockPanel = true;

                // Clear the user move since starting computer move
                _move.Clear();

                _ctsComputer = new CancellationTokenSource();
                var token = _ctsComputer.Token;
                token.Register(() => GameRecordDataService.instance.CurrentGame.CancelSearch());
                
                ComputerMoveProcessing = true;
                                
                GameRecordArray boardBeforeMove = GameRecordDataService.instance.GetCurrentGame();
                int gameStatusBeforeMove = GameRecordDataService.instance.CurrentGame.GetStateGameStatus();

                // Start the search      
                SearchOptions options;
                options.randomiseFirstMove = RandomiseFirstMoveEnabled;
                options.limitStrengthELO = limitEngineStrengthELO;  
                
                if (LimitAdvancedEnabled) { 
                    options.limitDepth = limitDepth;
                    options.limitNodes = limitNodes;
                    options.limitMoveDuration = limitMoveDuration;
                    options.limitThreads = limitThreads;
                }
                else
                {
                    options.limitDepth = 10;
                    options.limitNodes = 500000000;
                    options.limitMoveDuration = 0;
                    options.limitThreads = Environment.ProcessorCount > 1 ? Environment.ProcessorCount - 1 : 1;
                }


                var moveTask = Task.Run(() => GameRecordDataService.instance.CurrentGame.SearchStart(options), token);
                SearchResult topMove = await moveTask;
                 
                if((!topMove.cancelled) && (topMove.error == 0)) { 
                    MoveResult mResult = GameRecordDataService.instance.CurrentGame.Move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, true, true);
                    if (mResult.success) {

                        // Read Text, show message
                        var soundTasks = new List<Task>();
                        var rtnMessage = mResult.returnMessage;
                        if (rtnMessage != String.Empty) {
                            await ShowBoardMessage("", rtnMessage, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);                            
                        }
                        else {
                            soundTasks.Add(ReadText(GetBoardSquareSSML(mResult.moveDataStr)));
                        }

                        // Do animation
                        var boardAfterMove = GameRecordDataService.instance.GetCurrentGame();
                        var moveAnimationList = _boardAnimation.CreateAnimationList(boardBeforeMove, boardAfterMove, 2);
                        BoardSquareDataService.instance.Update(boardAfterMove, true);

                        long transId = GameRecordDataService.instance.transactionId;
                        await StartPieceAnimation(true, moveAnimationList, true);

                        // Piece move sound effect
                        soundTasks.Add(playPieceMoveSoundEffect());

                        // Wait for sound tasks to finish
                        if (soundTasks.Count > 0) {
                            await Task.WhenAll(soundTasks);
                        }

                        // Continue if nothing changed during the animation
                        if (transId == GameRecordDataService.instance.transactionId)
                        {
                            CheckChessClock();
                            RecordCurrentGameState();
                                                        
                            // Update score if checkmate occurred
                            if (gameStatusBeforeMove == (int)BoardStatusEnum.Ready && GameRecordDataService.instance.CurrentGame.GetStateGameStatus() == (int)BoardStatusEnum.Checkmate)
                            {
                                await afterCheckMate(GameRecordDataService.instance.CurrentGame);
                            }
                        }

                    }
                    else if (!mResult.success)
                    {
                        if (topMove.moveFromIndex > -1 && topMove.moveToIndex > -1)
                        {                            
                            await ShowBoardMessage("", "Engine attempted an invalid move.", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.Fixed);                            
                        }
                        else
                        {                            
                            await ShowBoardMessage("", "Move not received from Engine.", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.Fixed);
                            
                        }
                    }
                }
                else
                {
                    if (topMove.error > 0)
                    {
                        await ShowBoardMessage("", "Invalid board configuration. " + topMove.errorMessage + "." , TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.Fixed);
                    }
                }

                // Clear the cancellation token source
                _ctsComputer = null;

                // Unlock panel
                ComputerMoveProcessing = false;

                
                LockPanel = false;

                
            }
        }

        /// <summary>
        /// Converts int to colour
        /// </summary>
        /// <param name="pIntColour"></param>
        /// <returns></returns>
        private ColourEnum GetColour(int pIntColour)
        {
            if (pIntColour == (int)ColourEnum.White)
            {
                return ColourEnum.White;
            }
            else if (pIntColour == (int)ColourEnum.Black) {
                return ColourEnum.Black;
            }
            else
            {
                return ColourEnum.None;
            }
        }

        

        /// <summary>
        /// Starts a new game
        /// </summary>
        private void NewGame()
        {

            // Stop task if running
            stopMoveJob();
           

            // Initialise a new game
            LockPanel = true;
            GameRecordDataService.instance.Reset();
            BoardSquareDataService.instance.Update(GameRecordDataService.instance.GetCurrentGame(), true);


            // Ensure max record game is on the board  
            NavigateMaxRecord();


            // Set edit to false if true
            if (ArrangeBoardEnabled) {
                ArrangeBoardEnabled = false;
            }
            
            // Clear the clock
            ResetChessClock();

            // Update board indicators
            UpdateBoardIndicators(GameRecordDataService.instance.GetCurrentGame());

            
            LockPanel = false;

            // Show start move message
            if (ComputerPlayerEnabled && ComputerMoveFirstEnabled)
            {                
                ShowBoardMessage("", "Tap the board to start the game", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);                
                
            }

        }

        /// <summary>
        /// Resigns the current game
        /// </summary>
        private async void ResignGame()
        {
            if (ArrangeBoardEnabled)
            {
                ShowBoardMessage("", "Cannot resign in edit mode.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                return;
            }

            if (ComputerPlayerEnabled && IsComputerTurn())
            {
                ShowBoardMessage("", "Cannot resign as it is not your turn.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                return;
            }

            // Ensure game record is set to the latest
            NavigateMaxRecord();

            var status = GameRecordDataService.instance.CurrentGame.GetStateGameStatus(); 
           
            // Only resign if game is not already finished
            if (status == (int)BoardStatusEnum.Ready && GameRecordDataService.instance.CurrentGame.GetStateFullMoveCount() > 0) {
                stopMoveJob();

                // Set the state of the game to resign
                GameRecordDataService.instance.CurrentGame.SetStateGameStatus((int)BoardStatusEnum.Resigned);

                // Record current game state                    
                RecordCurrentGameState();

                var turn = GameRecordDataService.instance.CurrentGame.GetStateActiveColour();
                // Do animation, update score and display message
                if (turn == -1)
                {
                    await doKingFallAnimation(ColourEnum.White);
                }
                else if (turn == 1)
                {
                    await doKingFallAnimation(ColourEnum.Black);
                }

                UpdateBoardIndicators(GameRecordDataService.instance.GetCurrentGame());
            } 
            else
            {
                ShowBoardMessage("", "Resigning not available at this stage of the game.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
            }
        }


        /// <summary>
        /// Updates score after checkmate
        /// </summary>
        /// <param name="pBoard"></param>
        /// <returns></returns>
        private async Task afterCheckMate(KaruahChessEngineClass pBoard)
        {
            bool humanWinAgainstComputer = false;
            if (pBoard.GetStateActiveColour() == (int)ColourEnum.Black)
            {
                if (ComputerPlayerEnabled && !ComputerMoveFirstEnabled) humanWinAgainstComputer = true;

                // White wins
                await doKingFallAnimation(ColourEnum.White);
            }
            else if (pBoard.GetStateActiveColour() == (int)ColourEnum.White)
            {
                if (ComputerPlayerEnabled && ComputerMoveFirstEnabled) humanWinAgainstComputer = true;

                // Black wins
                await doKingFallAnimation(ColourEnum.Black);
            }

            // Increase strength level
            if (LevelAutoEnabled && humanWinAgainstComputer)
            {
                int nextElo = limitEngineStrengthELO + 75;
                int eloIndex = Constants.eloList.IndexOf(nextElo);
                if (eloIndex > -1)
                {
                    limitEngineStrengthELO = nextElo;
                    ShowBoardMessage("", "Congratulations, you have now progressed to the next level. The engine playing strength is now set to " + Constants.strengthLabelList[eloIndex] + ".", TextMessage.TypeEnum.Award, TextMessage.AnimationEnum.Fixed);
                }

            }

        }

        /// <summary>
        /// Upates all the board indicators
        /// </summary>        
        internal void UpdateBoardIndicators(GameRecordArray pBoard)
        {
            UpdateGameMessage(pBoard);
            UpdatetDirectionIndicator(pBoard);
            UpdateCheckIndicator(pBoard);
            
        }

        /// <summary>
        /// Shows game messages
        /// </summary>
        /// <returns></returns>
        private bool UpdateGameMessage(GameRecordArray pRecord)
        {
            String msgTitle = "";
            String msg = "";
            bool gameFinished = false;
            
            KaruahChessEngineClass board = new KaruahChessEngineClass();
            board.SetBoardArray(pRecord.BoardArray);
            board.SetStateArray(pRecord.StateArray);
                     

            if (board != null)
            {

                // 0  Game ready, game not started, 1 game commenced, 2 CheckMate, 3 Stalemate, 4 Resign         
                if (board.GetStateGameStatus() == (int)BoardStatusEnum.Checkmate && board.GetStateActiveColour() == (int)ColourEnum.Black)
                {
                    gameFinished = true;
                    msgTitle = "Checkmate! White wins.";
                    
                }
                else if (board.GetStateGameStatus() == (int)BoardStatusEnum.Checkmate && board.GetStateActiveColour() == (int)ColourEnum.White)
                {
                    gameFinished = true;
                    msgTitle = "Checkmate! Black wins.";
                }
                else if (board.GetStateGameStatus() == (int)BoardStatusEnum.Stalemate)
                {
                    gameFinished = true;
                    msgTitle = "Stalemate, no winners";                    
                }
                else if (board.GetStateGameStatus() == (int)BoardStatusEnum.Resigned && board.GetStateActiveColour() == (int)ColourEnum.White)
                {
                    gameFinished = true;
                    msgTitle = "White resigned. Black wins.";
                    
                }
                else if (board.GetStateGameStatus() == (int)BoardStatusEnum.Resigned && board.GetStateActiveColour() == (int)ColourEnum.Black)
                {
                    gameFinished = true;
                    msgTitle = "Black resigned. White wins.";                    
                }                
                

                if (gameFinished == true && _chessClockControl != null) _chessClockControl.StopAll();

                ShowBoardMessage(msgTitle, msg, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.Fixed);
               
            }

            return gameFinished;
        }

        

        /// <summary>
        /// Set the direction indicator
        /// </summary>
        private ColourEnum UpdatetDirectionIndicator(GameRecordArray pRecord)
        {
            KaruahChessEngineClass board = new KaruahChessEngineClass();
            board.SetBoardArray(pRecord.BoardArray);
            board.SetStateArray(pRecord.StateArray);

            ColourEnum rtnValue;           

            if (board.GetStateActiveColour() == (int)ColourEnum.Black)
            {
                DirectionColour = new SolidColorBrush(Colors.Black);
                OpposingColour = new SolidColorBrush(Colors.White);
                rtnValue = ColourEnum.Black;
            }
            else if (board.GetStateActiveColour() == (int)ColourEnum.White)
            {
                DirectionColour = new SolidColorBrush(Colors.White);
                OpposingColour = new SolidColorBrush(Colors.Black);
                rtnValue = ColourEnum.White;
            }
            else
            {
                DirectionColour = new SolidColorBrush(Colors.BurlyWood);
                OpposingColour = new SolidColorBrush(Colors.BurlyWood);
                rtnValue = ColourEnum.None;
            }

            


            return rtnValue;
        }

        /// <summary>
        /// Sets the check indicator
        /// </summary>
        private void UpdateCheckIndicator(GameRecordArray pRecord)
        {
            KaruahChessEngineClass board = new KaruahChessEngineClass();
            board.SetBoardArray(pRecord.BoardArray);
            board.SetStateArray(pRecord.StateArray);

            // Clear check indicator
            BoardSquare.EllipseUnderAttackClearAll();                        
            var turn = board.GetStateActiveColour();
            
            // Circles the king if in check
            if (board.GetStateActiveColour() != 0) {                 
                var kingCheck = board.IsKingCheck(turn);
               
                if (kingCheck)
                {
                    int kingIndex = board.GetKingIndex(turn);
                    if (kingIndex >= 0)
                    {
                        var kingHash = new HashSet<int>();
                        kingHash.Add(kingIndex);
                        SolidColorBrush underAttackColour = new SolidColorBrush(Colors.Red);
                        BoardSquare.EllipseUnderAttackShow(kingHash, underAttackColour);

                        if (board.GetStateGameStatus() == (int)BoardStatusEnum.Ready) { 
                            Task t = ReadText("Check");
                        }
                    }
                }
            }
        }

        /// <summary>
        /// Resize the board when the window size is changed
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnWindowSizeChanged(object sender, Windows.UI.Core.WindowSizeChangedEventArgs e)
        {
           
            if (!_boardResizeTimerRunning) {
                // Set flag so only one timer runs at a time
                _boardResizeTimerRunning = true;

                // Create oneshot timer to resize the board
                ThreadPoolTimer DelayTimer = ThreadPoolTimer.CreateTimer(async (timer) => {
                    await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () => { ResizeBoard();});                
                },  TimeSpan.FromMilliseconds(600), (x) => { _boardResizeTimerRunning = false; } );
            }

            
        }
                

        /// <summary>
        /// Helper function to resize the board
        /// </summary>
        private void ResizeBoard()
        {
            EndPieceAnimation();

            // Get functionality status
            bool navigatorBarEnabled = NavigatorEnabled;
            bool coordEnabled = BoardCoordEnabled;

            // Get window bounds
            var windowWidth = Window.Current.Bounds.Width;
            var windowHeight = Window.Current.Bounds.Height;

            // Set root padding
            var smallestDimension = windowWidth < windowHeight ? windowWidth : windowHeight;
            if (smallestDimension > 400) {
                RootPadding = new Thickness(5, 0, 5, 5);
            }
            else {
                RootPadding = new Thickness(0, 0, 0, 0);
            }
                        
            // Set the board margin taken up by the coordinates                      
            if (coordEnabled) {
                BoardCoordinateMargin = new Thickness(12, 0, 0, 18);                
            }
            else {
                BoardCoordinateMargin = new Thickness(0, 0, 0, 0);
            }

            // Get the board border thickness
            var boardBorderThickness = _boardTilePanelControl.BorderThickness;

            // Note: Default command bar height is 40px in latest uwp update. Previously it was 48px.
            // Calculate area available for board
            double menuBarHeight = 43;
            double navigatorBarHeight;
            if (navigatorBarEnabled) navigatorBarHeight = 34;
            else navigatorBarHeight = 0;


            var boardBorderHorizontalThickness = boardBorderThickness.Left + boardBorderThickness.Right;
            var boardBorderVerticalThickness = boardBorderThickness.Top + boardBorderThickness.Bottom;
            var boardHorizontalMargin = BoardCoordinateMargin.Left + BoardCoordinateMargin.Right;
            var boardVerticalMargin = BoardCoordinateMargin.Top + BoardCoordinateMargin.Bottom;
            var rootHorizontalPadding = RootPadding.Left + RootPadding.Right;
            var rootVerticalPadding = RootPadding.Top + RootPadding.Bottom;

            var boardWidthAvailable = windowWidth - boardBorderHorizontalThickness - boardHorizontalMargin - rootHorizontalPadding;
            var boardHeightAvailable = windowHeight - menuBarHeight - navigatorBarHeight - boardBorderVerticalThickness - boardVerticalMargin - rootVerticalPadding;

            // Calculate tilesize
            double tileSize;
            if (boardWidthAvailable < boardHeightAvailable) {
                tileSize = boardWidthAvailable / 8;
            }
            else {
                tileSize = boardHeightAvailable / 8;
            }

            // Set tile size
            BoardSquareDataService.instance.SquareSize = tileSize;               
            
            foreach (var item in BoardTiles)
            {
                // Set the tile size
                item.StyleTemplate.Width = tileSize;
                item.StyleTemplate.Height = tileSize;

                // Recode the image size
                var p = ((BoardSquare)item.Entity).Piece;
                p.SetType(p.Type, p.Colour, tileSize, tileSize);
               
            }

            // Set the board width (this includes the margins)
            var tileRowWidth = tileSize * 8;
            BoardWidth = tileRowWidth + boardHorizontalMargin + boardBorderHorizontalThickness + rootHorizontalPadding;

            // Set coordinates
            if (_coordinatesControl != null && _boardTilePanelControl != null) {
                _coordinatesControl.Show(coordEnabled);
                if (coordEnabled) { 
                    var coordinateWidth = tileRowWidth + boardHorizontalMargin + boardBorderHorizontalThickness;
                    var coordianteHeight = tileRowWidth + boardVerticalMargin + boardBorderVerticalThickness;
                    _coordinatesControl.Draw(tileSize, coordinateWidth, coordianteHeight, RotateBoardValue);
                }

            }

            
            // Position board messages
            PositionBoardMessage();

            // Ensure board editor menu is closed
            _pieceEditToolControl.Close();

            
        }

        /// <summary>
        /// Apply board colour to the board
        /// </summary>
        public void ApplyBoardColour()
        {
            ColourARGB argb = ColourDarkSquaresARGB;
            Color darkSquareColour = new Color
            {
                A = argb.A,
                R = argb.R,
                G = argb.G,
                B = argb.B
            };

            // Tiles
            foreach (Tile tile in BoardTiles)
            {
                BoardSquare sq = tile.Entity as BoardSquare;
                if (sq.Colour == BoardSquare.ColourEnum.Black)
                {
                    tile.StyleTemplate.Background = new SolidColorBrush(darkSquareColour);
                    
                }
            }

            // Board border
            BorderColour = new SolidColorBrush(darkSquareColour);
                        
        }

        
        // Do the endgame animation
        public async Task doKingFallAnimation(ColourEnum pWinner)
        {
            if (pWinner == ColourEnum.White)
            {
                
                await KingFallAnimation(-1);

            }
            else if (pWinner == ColourEnum.Black)
            {
                
                await KingFallAnimation(1);
                               
            }
        }

        /// <summary>
        /// Initiates the king fall animation
        /// </summary>
        /// <param name="pTurn"></param>
        private async Task KingFallAnimation(int pTurn)
        {
            int kingIndex = GameRecordDataService.instance.CurrentGame.GetKingIndex(pTurn);
            if (kingIndex >= 0)
            {
                var kingTile = BoardSquareDataService.instance.BoardTiles[kingIndex];
                var kingTilePoint = kingTile.Coordinates();

                PieceAnimationInstruction instruction = new PieceAnimationInstruction();
                instruction.AnimationType = PieceAnimationInstruction.AnimationTypeEnum.Fall;
                instruction.ImageData = BoardSquareDataService.instance.Get(kingIndex).Piece.ImageData;
                instruction.MoveFrom = kingTilePoint;
                instruction.MoveTo = kingTilePoint;

                List<PieceAnimationInstruction> animationList = new List<PieceAnimationInstruction>(1);
                animationList.Add(instruction);
                                
                BoardSquareDataService.instance.Hide(kingIndex);
                await StartPieceAnimation(false, animationList, false);
            }
                        
        }
        
        /// <summary>
        /// Sets the visibility of the feedback button
        /// </summary>
        private void SetFeedbackVisibility()
        {
            if (Microsoft.Services.Store.Engagement.StoreServicesFeedbackLauncher.IsSupported())
            {
                FeedbackVisibility = Visibility.Visible;
            }
            else
            {
                FeedbackVisibility = Visibility.Collapsed;
            }
        }

        
        /// <summary>
        /// Shows board message
        /// </summary>        
        public async Task ShowBoardMessage(String pTitle, String pMessage, TextMessage.TypeEnum pType, TextMessage.AnimationEnum pAnimation)
        {
            // Marshall back to UI thread
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {

                // Show message
                if (_boardTextMessageControl != null) {
                    if (!(pMessage == string.Empty && pTitle == string.Empty))
                    {
                        PositionBoardMessage();
                        _boardTextMessageControl.Show(pTitle, pMessage, pType, pAnimation);

                        if (pTitle != String.Empty)
                        {
                            Task t = ReadText(pTitle);
                        }
                        else
                        {
                            Task t = ReadText(pMessage);
                        }
                    }
                    else 
                    {
                        _boardTextMessageControl.Clear();
                    }

                }

            });
            
        }


        /// <summary>
        /// Clear board message
        /// </summary>        
        public async Task ClearBoardMessage()
        {
            // Marshall back to UI thread
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {

                // Show message
                if (_boardTextMessageControl != null)
                {
                    _boardTextMessageControl.Clear();
                }


            });

        }


        /// <summary>
        /// Positions board message
        /// </summary>
        private void PositionBoardMessage() 
        {
            // Set board message position
            if (_boardTextMessageControl != null)
            {
                _boardTextMessageControl.SetPosition(BoardWidth);
            }
                       
            if (_aboutPageControl != null)
            {
                _aboutPageControl.SetPosition(BoardWidth);
            }

            if (_importPGNControl != null)
            {
                _importPGNControl.SetPosition(BoardWidth);
            }

            if (_exportControl != null)
            {
                _exportControl.SetPosition(BoardWidth);
            }

            if (_voiceHelpControl != null)
            {
                _voiceHelpControl.SetPosition(BoardWidth);
            }

            if (_engineSettingsControl != null)
            {
                _engineSettingsControl.SetPosition(BoardWidth);
            }
                        
            if (_soundColourSettingsControl != null)
            {
                _soundColourSettingsControl.SetPosition(BoardWidth);
            }

        }

        /// <summary>
        ///  Records the current state of the game
        /// </summary>
        private void RecordCurrentGameState()
        {
            
            try {
                int whiteClock = 0;
                int blackClock = 0;
                // Update the clock offset before saving            
                if (_chessClockControl != null && _chessClockControl.ClockEnabled)
                {
                    _chessClockControl.UpdateCurrentTime(); // Ensure time is up to date before saving                    
                    whiteClock = (int)Math.Round(_chessClockControl.WhiteTotalTime);
                    blackClock = (int)Math.Round(_chessClockControl.BlackTotalTime);                   
                }

                // Record current game state                 
                int success =  GameRecordDataService.instance.RecordGameState(whiteClock, blackClock);
                if (success > 0)
                {
                    // Ensure game record position is set to max value
                    NavigateMaxRecord();                   
                }
            }
            catch (Exception ex)  {
                ShowBoardMessage("Error", ex.Message, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
            }           

        }


        /// <summary>
        /// Get SSML for board square id
        /// </summary>
        /// <param name="pSqId"></param>
        /// <returns></returns>
        private string GetBoardSquareSSML(string pMoveDataStr)
        {
            String[] moveDataStrArray = pMoveDataStr.Split('|');


            // Check move data array is the correct size
            if (moveDataStrArray == null || moveDataStrArray.Length != 4)
            {
                return string.Empty;
            }

            int fromIndex;
            int toIndex;
            int origFromSpin;
            int origToSpin;

            int.TryParse(moveDataStrArray[0], out fromIndex);
            int.TryParse(moveDataStrArray[1], out toIndex);
            int.TryParse(moveDataStrArray[2], out origFromSpin);
            int.TryParse(moveDataStrArray[3], out origToSpin);
            

            // Check from and to indexes are in the correct range
            if(!(fromIndex >=0 && fromIndex <= 63 && toIndex >=0 && toIndex <= 63))
            {
                return string.Empty;
            }



            string ssml = string.Empty;
                   
            
            if (origToSpin != 0)
            {
                ssml = GetPieceSpinSSML(origFromSpin) + " takes " + GetPieceSpinSSML(origToSpin);
            }
            else
            {
                ssml = GetPieceSpinSSML(origFromSpin) + " to " + "<say-as interpret-as='characters'>" + helper.BoardCoordinateDict[toIndex] + "</say-as>";
            }
                             

            return ssml;
        }

        /// <summary>
        /// Get ssml for piece spin
        /// </summary>
        /// <param name="pSpinTaken"></param>
        /// <returns></returns>
        private string GetPieceSpinSSML(int pPieceSpin)
        {            
            int spin = Math.Abs(pPieceSpin);
            string ssml = string.Empty;

            if (spin >= 1 && spin <= 6 ) {
                var pieceColour = pPieceSpin < 0 ? Piece.ColourEnum.Black : Piece.ColourEnum.White;
                var pieceType = (Piece.TypeEnum)spin;               
                ssml = Enum.GetName(typeof(Pieces.Piece.ColourEnum), pieceColour);
                ssml = string.Join(" ", ssml, Enum.GetName(typeof(Pieces.Piece.TypeEnum), pieceType));
            }

            return ssml;
        }

        /// <summary>
        /// Text to speech
        /// </summary>
        /// <param name="pText"></param>
        private async Task ReadText(string pText)
        {
            if (SoundReadEnabled) {
                // Pause recogniser
                VoiceRecogniserIgnoreIncrement();

                // Phonemes
                if (pText == "En passant")
                {
                    pText = "<phoneme alphabet='x-microsoft-ups' ph='S1 lng AO N . P AA S lng AO N'>En passant</phoneme>";
                }

                // Read text
                using (var speech = new SpeechSynthesizer())
                {                
                    VoiceInformation voiceInfo = SpeechSynthesizer.DefaultVoice;
                
                    if (voiceInfo != null) {                        
                        var tcs = new TaskCompletionSource<bool>();
                        string langTag = voiceInfo.Language;                                
                        string ssml = @"<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='" + langTag + "'>" + pText + "</speak>";                                            
                        SpeechSynthesisStream stream = await speech.SynthesizeSsmlToStreamAsync(ssml);
                        var source = MediaSource.CreateFromStream(stream, stream.ContentType);
                                                
                        var mediaEndedHandler = new TypedEventHandler<Windows.Media.Playback.MediaPlayer, object>((player, resource) =>
                        {
                            tcs.TrySetResult(true);
                        });
                        _mediaplayerReadText.Source = source;
                        _mediaplayerReadText.MediaPlayer.MediaEnded += mediaEndedHandler;
                        _mediaplayerReadText.MediaPlayer.PlaybackSession.Position = new TimeSpan(0, 0, 0);
                        _mediaplayerReadText.MediaPlayer.Play();

                        // Wait here, but timeout after 2 seconds
                        await Task.WhenAny(tcs.Task, Task.Delay(2000));

                        _mediaplayerReadText.MediaPlayer.MediaEnded -= mediaEndedHandler;
                    }
                }

                VoiceRecogniserIgnoreDecrement();

            }
            
        }


        /// <summary>
        /// Start voice recognition
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnStartVoiceListen_Checked(object sender, RoutedEventArgs e)
        {
            AppBarToggleButton btn = (AppBarToggleButton)sender;

            ListenText = string.Empty;
            bool permitted = await VoiceRecognition.CheckMicrophonePermission();
                      
            if (permitted)
            {
                if (_voiceRecogniser == null) { 
                    _voiceRecogniser = new VoiceRecognition();
                }
                                
                Language speechLanguage = SpeechRecognizer.SystemSpeechLanguage;
                                
                if (speechLanguage != null) { 
                    string langTag = speechLanguage.LanguageTag;
                    if (VoiceRecognition.SupportedLanguages.Contains(langTag)) {
                        try { 
                            await _voiceRecogniser.Initialise(speechLanguage, MoveVoiceActionA, MoveVoiceActionB, PieceFindVoiceAction, HelpVoiceAction);
                            await ShowBoardMessage("", "Voice command activated. Say 'Help' for voice commands.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                            
                        }
                        catch (Exception ex)
                        {
                            btn.IsChecked = false;
                            await ShowBoardMessage("Error", ex.Message, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);                            
                        }
                    }
                    else
                    {
                        btn.IsChecked = false;
                        var msgA = "Voice commands not available. ";
                        var msgB = "Default speech language must be set to English - AU, CA, GB, IN, NZ, or US.";                        
                        await ShowBoardMessage("", msgA + msgB, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);                                               
                    }
                }
                else
                {
                    btn.IsChecked = false;
                    var msg = "Voice commands not available. No speech languages are installed on your system.";
                    await ShowBoardMessage("", msg, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);                                       
                }

            }
            else
            {
                btn.IsChecked = false;
                var msg = "To use voice commands, your system privacy settings must first allow access to the microphone.";
                await ShowBoardMessage("", msg, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);                
                
            }
            
        }


        /// <summary>
        /// Runs when voice is unchecked
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnStartVoiceListen_Unchecked(object sender, RoutedEventArgs e)
        {
            ListenText = string.Empty;

            if (_voiceRecogniser != null)
            {
                _voiceRecogniser.Stop();
            }
        }


        /// <summary>
        /// Executes a move action from voice command
        /// </summary>
        /// <param name="pMoveAction"></param>
        private async Task MoveVoiceActionA(List<int> pMoveList, String pTextSpoken)
        {            

            if (pMoveList != null && !_pawnPromotionDialogOpen) { 
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, async () => {
                                       
                    // Attempt to add the move
                    if (pMoveList.Count == 2 && !LockPanel) {
                        
                        // Clear move
                        _move.Clear();
                                                
                        if (ArrangeBoardEnabled)
                        {
                            ArrangeUpdate(pMoveList[0], pMoveList[1]);                            
                        }
                        else
                        {                            
                            int maxRecId = GameRecordDataService.instance.GetMaxId();
                            if (GameRecordCurrentValue == maxRecId)
                            {   // Add the moves
                                await UserMoveAdd(BoardSquareDataService.instance.Get(pMoveList[0]), true);
                                await UserMoveAdd(BoardSquareDataService.instance.Get(pMoveList[1]), true);
                            }
                            else
                            {
                                // Show and or read message to user
                                var msg = "Cannot move, board is not on the latest move.";
                                await ShowBoardMessage("", msg, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                            }
                        }
                    }                   

                });
            }
        }


        /// <summary>
        /// Executes a move action of type B from voice command
        /// </summary>
        /// <param name="pMoveAction"></param>
        private async Task MoveVoiceActionB(List<string> pMoveCommand, String pTextSpoken)
        {
            if (pMoveCommand != null && !_pawnPromotionDialogOpen)
            {
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, async () => {

                    var msg = string.Empty;


                    // Ensure max is displayed
                    int maxRecId = GameRecordDataService.instance.GetMaxId();

                   
                    // Attempt to add the move
                    if (ArrangeBoardEnabled)
                    {
                        msg = "Use coordinates to move pieces when in edit mode.";
                    }
                    else if (GameRecordCurrentValue != maxRecId)
                    {
                        msg = "Cannot move, board is not on the latest move.";
                    }
                    else if (IsComputerTurn() && !ArrangeBoardEnabled)
                    {
                        msg = "Cannot move, as it is not your turn. Tap the board for the computer to move.";
                    }
                    else
                    {
                        if (pMoveCommand.Count == 3 && !LockPanel)
                        {                            
                            // If no colour was specified, substitute in the active colour
                            if (pMoveCommand[0] == String.Empty && ArrangeBoardEnabled == false)
                            {
                                if (GameRecordDataService.instance.CurrentGame.GetStateActiveColour() == Constants.WHITEPIECE) pMoveCommand[0] = "White";
                                else if (GameRecordDataService.instance.CurrentGame.GetStateActiveColour() == Constants.BLACKPIECE) pMoveCommand[0] = "Black";
                            }

                            var pieceName = (pMoveCommand[0] + " " + pMoveCommand[1]).Trim();
                            var fromSpin = GameRecordDataService.instance.CurrentGame.GetSpinFromPieceName(pieceName);

                            // Get to index
                            int toIndex = -1;
                            if (helper.BoardCoordinateReverseDict.ContainsKey(pMoveCommand[2]))
                            {
                                toIndex = helper.BoardCoordinateReverseDict[pMoveCommand[2]];
                            }
                            
                            var fromIndex = GameRecordDataService.instance.CurrentGame.FindFromIndex(toIndex, fromSpin, null);
                                                        
                            if (fromIndex == -1) msg = pTextSpoken + " is not a valid move.";
                            else if (fromIndex == -2) msg = pTextSpoken + " is ambiguous. Try using coordinates instead.";
                            else
                            {                                
                                // Clear move
                                _move.Clear();


                                // Do the move
                                await UserMoveAdd(BoardSquareDataService.instance.Get(fromIndex), true);
                                await UserMoveAdd(BoardSquareDataService.instance.Get(toIndex), true);
                            }                           
                        }
                    }

                    // Show and or read message to user
                    if (msg != string.Empty)
                    {
                        await ShowBoardMessage("", msg, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                    }

                });
            }
        }


        /// <summary>
        /// Executes a piece action from voice command
        /// </summary>
        /// <param name="pActionList"></param>
        /// <param name="pTextSpoken"></param>
        /// <returns></returns>
        private async Task PieceFindVoiceAction(List<char> pActionList, String pTextSpoken)
        {
            if (pActionList != null && !_pawnPromotionDialogOpen)
            {
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
                {
                    // Attempt to add the move
                    if (pActionList.Count > 0)
                    {
                        string msg = string.Empty;
                        string msgSSML = string.Empty;
                        bool pieceFound = false;

                        foreach (var fenStr in pActionList)
                        {
                            var coordList = BoardSquareDataService.instance.LocatePiece(fenStr);
                            var coordCount = coordList.Count;
                            if (coordList.Count > 0)
                            {
                                pieceFound = true;
                                var title = GameRecordDataService.instance.CurrentGame.GetPieceNameFromChar(fenStr);
                                msg += title + "; ";
                                msgSSML += title + ". <prosody rate='0.7'>";
                                for (int i = 0; i < coordCount; i++)
                                {
                                    msg += coordList[i];
                                    msgSSML += "<say-as interpret-as='spell-out'>" + coordList[i] + "</say-as>";
                                    if (i < (coordCount - 1))
                                    {
                                        msg += ", ";
                                        msgSSML += ", ";
                                    }
                                    else
                                    {
                                        msg += ". ";
                                        msgSSML += ". ";
                                    }
                                }
                                msgSSML += "</prosody>";
                            }
                        }


                        if (!pieceFound)
                        {
                            msg = pTextSpoken + ". Not on the board.";                            
                        }


                        // Show and or read message to user
                        ShowBoardMessage("", msg, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                        
                    }


                });
            }

        }

        /// <summary>
        /// Increment the ignore flag on the voice recogniser
        /// </summary>
        private async void VoiceRecogniserIgnoreIncrement()
        {
            if (_voiceRecogniser != null)
            {                
                _voiceRecogniser.Ignore++;
            }
        }

        /// <summary>
        /// Decrement the ignore flag on the voice recogniser
        /// </summary>
        private async void VoiceRecogniserIgnoreDecrement()
        {
            // 1 second delay to drop any synthesiser voice that may have been captured
            if (_voiceRecogniser != null)
            {
                await Task.Delay(1000);
                _voiceRecogniser.Ignore--;
            }
        }


        /// <summary>
        /// Starts the chess clock based on current turn
        /// </summary>
        private void CheckChessClock()
        {
            if (_chessClockControl != null) {
                if (ArrangeBoardEnabled)
                {
                    _chessClockControl.StopAll();
                }
                else {
                    var turn = GameRecordDataService.instance.CurrentGame.GetStateActiveColour();
                    _chessClockControl.Start(turn);
                }

                if (GameRecordCurrentValue == GameRecordDataService.instance.GetMaxId()) _chessClockControl.ShowCurrentTime();
                
            }

        }

        /// <summary>
        /// Loads the chess clock values from the game state
        /// </summary>
        private void LoadChessClock(bool pLoadInitialOffset, GameRecordArray pRecord)
        {
            var board = new KaruahChessEngineClass();
            board.SetBoardArray(pRecord.BoardArray);
            board.SetStateArray(pRecord.StateArray);

            // Loads the initial offset
            if (pLoadInitialOffset)
            {
                if (_chessClockControl != null) _chessClockControl.ResetAll();
                ClockWhiteOffset = board.GetStateWhiteClockOffset();
                ClockBlackOffset = board.GetStateBlackClockOffset();
            }

            // Switches between historical and current
            if (_chessClockControl != null)
            {                
                if (GameRecordCurrentValue == GameRecordDataService.instance.GetMaxId()) _chessClockControl.ShowCurrentTime();                                    
                else _chessClockControl.ShowHistoricalTime(board.GetStateWhiteClockOffset(),
                                                           board.GetStateBlackClockOffset());
                
            }
        }

        /// <summary>
        /// Reset the chess clock
        /// </summary>
        public void ResetChessClock()
        {
            ClockBlackOffset = 0;
            ClockWhiteOffset = 0;

            if (_chessClockControl != null)
            {
                _chessClockControl.ResetAll();
                _chessClockControl.UpdateCurrentTime(); // Ensure time is up to date before saving                     
            }
        }

        /// <summary>
        /// Navigate to game record
        /// </summary>
        /// <param name="pRecId"></param>
        public async void NavigateGameRecord(int pRecId, bool pAnimate, bool pReloadNav, bool pScrollNav)
        {           

            if (pRecId > 0)
            {
                await navThrottler.WaitAsync();

                GameRecordArray oldBoard = GameRecordDataService.instance.Get(GameRecordCurrentValue);
                GameRecordArray updatedBoard = GameRecordDataService.instance.Get(pRecId);

                // Update board displayed with requested record
                if (updatedBoard != null)
                {

                    // Do animation
                    if (pAnimate) { 
                        var moveAnimationList = _boardAnimation.CreateAnimationList(oldBoard, updatedBoard, 0.5);
                        BoardSquareDataService.instance.Update(updatedBoard, false);
                        GameRecordCurrentValue = pRecId;
                        UpdateBoardIndicators(updatedBoard);
                        LoadChessClock(false, updatedBoard);
                        await StartPieceAnimation(true, moveAnimationList, true);
                    }
                    else {
                        // End any animations
                        EndPieceAnimation();

                        BoardSquareDataService.instance.Update(updatedBoard, true);
                        GameRecordCurrentValue = pRecId;
                        UpdateBoardIndicators(updatedBoard);
                        LoadChessClock(false, updatedBoard);
                    }
                                        
                    if (_move != null)
                    {
                        _move.Clear();
                    }

                    // Refresh the navigation
                    RefreshNavigation(pReloadNav, pScrollNav);
                }

                navThrottler.Release();

            }
        }

        /// <summary>
        /// Set the board to the max record
        /// </summary>
        public void NavigateMaxRecord()
        {
            int maxId = GameRecordDataService.instance.GetMaxId();
            NavigateGameRecord(maxId, false, true, true);
        }

        /// <summary>
        /// Opens the voice help screen
        /// </summary>
        private async Task HelpVoiceAction(bool pShow)
        {
            if (_voiceHelpControl != null)
            {
                if (pShow) { 
                    _voiceHelpControl.Show();
                    PositionBoardMessage();
                }
            }
            
        }

        /// <summary>
        /// Highlight last move
        /// </summary>
        private void HighlightLastMove()
        {
            GameRecordArray currentBoard = GameRecordDataService.instance.Get((int)GameRecordCurrentValue);
            GameRecordArray previousBoard = GameRecordDataService.instance.Get((int)GameRecordCurrentValue - 1);
            var lastChanges = GameRecordDataService.instance.GetBoardSquareChanges(currentBoard, previousBoard);

            if (lastChanges.Count > 0)
            {
                SolidColorBrush rectColour = new SolidColorBrush(Color.FromArgb(255, 233, 30, 99));
                BoardSquare.RectangleShow(lastChanges, rectColour);
            }
            else
            {                
                ShowBoardMessage("", "No last move to show.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                
            }
        }
          

        /// <summary>
        /// Refresh the navigation control
        /// </summary>
        public void RefreshNavigation(bool pReload, bool pScroll)
        {
            // Exit immediately if navigator is null
            if (_moveNavigatorControl == null) return;

            // Refresh the navigator control
            if (NavigatorEnabled)
            {
                _moveNavigatorControl.Show();

                if (pReload)
                {
                    _moveNavigatorControl.Load(GameRecordDataService.instance.GetAllRecordIDList(), GameRecordCurrentValue);
                }
                else
                {
                    _moveNavigatorControl.SetSelected(GameRecordCurrentValue);
                }

                if (pScroll)
                {
                    _moveNavigatorControl.ScrollToSelected();
                }
            }
            else
            {
                _moveNavigatorControl.Hide();
            }
        }

        /// <summary>
        /// Stops a running move job
        /// </summary>
        public void stopMoveJob()
        {
            // Clear any moves selected
            _move.Clear();

            // Ensure animations are cleared
            EndPieceAnimation();

            // Cancel any computer move tasks
            if (_ctsComputer != null)
            {
                _ctsComputer.Cancel();
            }
        }


        /// <summary>
        /// Play piece move sound effect
        /// </summary>
        private async Task playPieceMoveSoundEffect()
        {
            if (_mediaplayerPieceMoveSound != null && SoundEffectEnabled)
            {
                var tcs = new TaskCompletionSource<bool>();
                
                var mediaEndedHandler = new TypedEventHandler<Windows.Media.Playback.MediaPlayer, object>((player, resource) => 
                { 
                    tcs.TrySetResult(true); 
                });
                
                _mediaplayerPieceMoveSound.MediaPlayer.MediaEnded += mediaEndedHandler;
                _mediaplayerPieceMoveSound.MediaPlayer.PlaybackSession.Position = new TimeSpan(0, 0, 0);
                _mediaplayerPieceMoveSound.MediaPlayer.Play();
                
                // Wait here, but timeout after 2 seconds
                await Task.WhenAny(tcs.Task, Task.Delay(2000));

                _mediaplayerPieceMoveSound.MediaPlayer.MediaEnded -= mediaEndedHandler;
            }
        }

        
        
    }


}
