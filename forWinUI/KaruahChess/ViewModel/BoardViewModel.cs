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

using KaruahChess.Common;
using KaruahChess.CustomControl;
using KaruahChess.Database;
using KaruahChess.Model;
using KaruahChess.Model.ParameterObjects;
using KaruahChess.Pieces;
using KaruahChess.Rules;
using KaruahChess.Voice;
using KaruahChessEngine;
using Microsoft.UI;
using Microsoft.UI.Dispatching;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Documents;
using Microsoft.UI.Xaml.Media;
using PurpleTreeSoftware.Panel;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Globalization;
using Windows.Media.Core;
using Windows.Media.Playback;
using Windows.Media.SpeechRecognition;
using Windows.Media.SpeechSynthesis;
using Windows.Storage;
using Windows.Storage.Pickers;
using Windows.System.Threading;
using Windows.UI;
using static KaruahChess.Pieces.Piece;
using static KaruahChess.Rules.Move;

namespace KaruahChess.ViewModel
{
    public class BoardViewModel : BaseViewModel
    {
        // variables             
        bool _boardResizeTimerRunning;        
        Move _move ;                           
        CancellationTokenSource _ctsComputer;
        CancellationTokenSource _ctsHint;
        TextMessage _boardTextMessageControl;
        MoveNavigator _moveNavigatorControl;
        TilePanel _boardTilePanelControl;
        PieceAnimation _pieceAnimationControl;
        VoiceRecognition _voiceRecogniser;
        BoardAnimation _boardAnimation;        
        bool _pawnPromotionDialogOpen = false;
        MediaPlayer _mediaplayerReadText;
        MediaPlayer _mediaplayerPieceMoveSound;
        KaruahChessEngineClass hintBoard = new KaruahChessEngineClass();
        KaruahChessEngineClass editBoard = new KaruahChessEngineClass();
        ContentDialog boardContentDialog = null;
        int boardContentDialogCount = 0;
        SemaphoreSlim navThrottler = new SemaphoreSlim(initialCount: 1);
        SemaphoreSlim readTextThrottler = new SemaphoreSlim(1, 1);
        SemaphoreSlim pieceMoveSoundThrottler = new SemaphoreSlim(1, 1);
        bool _userMoveProcessing = false;        
        MainWindow mainWindowRef;
        DispatcherQueue mainDispatcherQueue = DispatcherQueue.GetForCurrentThread();
        bool postInitComplete = false;
        public Coordinates coordinatesControl;
        public ClockPanel chessClockControl;
        HashSet<int> editSelection = new HashSet<int>();
        int editLastTapIndex = -1;


        public enum MoveTypeEnum { None = 0, Normal = 1, EnPassant = 2, Castle = 3, Promotion = 4 }
        // 0  Game ready, 1 CheckMate, 2 Stalemate, 3 Resign
        public enum BoardStatusEnum { Ready = 0, Checkmate = 1, Stalemate = 2, Resigned = 3, TimeExpired = 4 }
        public enum PawnPromotionEnum { Knight = 2, Bishop = 3, Rook = 4, Queen = 5 }


        // Error codes
        private static int noCaptureDevices = -1072845856;


        /// <summary>
        /// Options menu 
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
        /// Hint in progress indicator
        /// </summary>
        private bool _computerHintProcessing;
        public bool ComputerHintProcessing
        {
            get { return _computerHintProcessing; }
            set
            {
                _computerHintProcessing = value;
                RaisePropertyChanged(nameof(ComputerHintProcessing));
            }
        }

        /// <summary>
        /// Voice progress indicator
        /// </summary>
        private bool _computerVoiceProcessing;
        public bool ComputerVoiceProcessing
        {
            get { return _computerVoiceProcessing; }
            set
            {
                _computerVoiceProcessing = value;
                RaisePropertyChanged(nameof(ComputerVoiceProcessing));
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
        private ParamLimitSkillLevel _limitSkillLevel;
        public int LimitSkillLevel
        {
            get
            {
                _limitSkillLevel = ParameterDataService.instance.Get<ParamLimitSkillLevel>();
                return _limitSkillLevel.level;
            }
            set
            {
                if (_limitSkillLevel != null && _limitSkillLevel.level != value)
                {
                    _limitSkillLevel.level = value;
                    ParameterDataService.instance.Set<ParamLimitSkillLevel>(_limitSkillLevel);                    
                    RaisePropertyChanged(nameof(LimitSkillLevel));
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
                    if (value == false)
                    {
                        editSelection.Clear();
                        BoardSquare.PieceEditSelectClearAll();

                    }                                        
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
        /// Clock Enabled
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
                    ResizeBoard();
                    // Set clock
                    if (value) {
                        chessClockControl.Show();
                        LoadChessClock(true, GameRecordDataService.instance.GetCurrentGame());
                    }
                    else  {
                        chessClockControl.Hide();
                    }

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
        /// Move speed
        /// </summary>  
        private ParamMoveSpeed _moveSpeed;
        public int moveSpeed
        {
            get
            {
                var paramMoveSpeedObj = ParameterDataService.instance.Get<ParamMoveSpeed>();
                _moveSpeed = paramMoveSpeedObj;
                return _moveSpeed.Speed;
            }
            set
            {
                if (_moveSpeed != null && _moveSpeed.Speed != value)
                {
                    _moveSpeed.Speed = value;
                    ParameterDataService.instance.Set<ParamMoveSpeed>(_moveSpeed);
                    RaisePropertyChanged(nameof(moveSpeed));
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
        /// Automatic pawn promotion
        /// </summary>  
        private ParamPromoteAuto _promoteAutoEnabled;
        public bool PromoteAutoEnabled
        {
            get
            {
                var paramPromoteAutoObj = ParameterDataService.instance.Get<ParamPromoteAuto>();
                _promoteAutoEnabled = paramPromoteAutoObj;
                return _promoteAutoEnabled.Enabled;
            }
            set
            {
                if (_promoteAutoEnabled != null && _promoteAutoEnabled.Enabled != value)
                {
                    _promoteAutoEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamPromoteAuto>(_promoteAutoEnabled);
                    RaisePropertyChanged(nameof(PromoteAutoEnabled));
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

        /// <summary>
        /// Hint button enabled
        /// </summary>  
        private ParamHint _hintEnabled;
        public bool HintEnabled
        {
            get
            {
                var paramHintObj = ParameterDataService.instance.Get<ParamHint>();
                _hintEnabled = paramHintObj;
                return _hintEnabled.Enabled;
            }
            set
            {
                if (_hintEnabled != null && _hintEnabled.Enabled != value)
                {
                    _hintEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamHint>(_hintEnabled);
                    RaisePropertyChanged(nameof(HintEnabled));
                }
            }
        }

        /// <summary>
        /// Hint move piece enabled
        /// </summary>  
        private ParamHintMove _hintMoveEnabled;
        public bool HintMoveEnabled
        {
            get
            {
                var paramHintMoveObj = ParameterDataService.instance.Get<ParamHintMove>();
                _hintMoveEnabled = paramHintMoveObj;
                return _hintMoveEnabled.Enabled;
            }
            set
            {
                if (_hintMoveEnabled != null && _hintMoveEnabled.Enabled != value)
                {
                    _hintMoveEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamHintMove>(_hintMoveEnabled);
                    RaisePropertyChanged(nameof(HintMoveEnabled));
                }
            }
        }

        /// <summary>
        /// Large pawn enabled
        /// </summary>  
        private ParamLargePawn _largePawnEnabled;
        public bool LargePawnEnabled
        {
            get
            {
                var paramLargePawnObj = ParameterDataService.instance.Get<ParamLargePawn>();
                _largePawnEnabled = paramLargePawnObj;
                return _largePawnEnabled.Enabled;
            }
            set
            {
                if (_largePawnEnabled != null && _largePawnEnabled.Enabled != value)
                {
                    _largePawnEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamLargePawn>(_largePawnEnabled);
                    RaisePropertyChanged(nameof(LargePawnEnabled));
                    RefreshPieces();
                }
            }
        }

        /// <summary>
        /// Voice command button enabled
        /// </summary>  
        private ParamVoiceCommand _voiceCommandEnabled;
        public bool VoiceCommandEnabled
        {
            get
            {
                var paramVoiceCommandObj = ParameterDataService.instance.Get<ParamVoiceCommand>();
                _voiceCommandEnabled = paramVoiceCommandObj;
                return _voiceCommandEnabled.Enabled;
            }
            set
            {
                if (_voiceCommandEnabled != null && _voiceCommandEnabled.Enabled != value)
                {
                    _voiceCommandEnabled.Enabled = value;
                    ParameterDataService.instance.Set<ParamVoiceCommand>(_voiceCommandEnabled);
                    RaisePropertyChanged(nameof(VoiceCommandEnabled));
                    
                    if (value == false)
                    {
                        StopVoiceListen();
                    }
                }
            }
        }


        /// <summary>
        /// Main control orientation
        /// </summary>
        private Orientation _mainControlOrientation;
        public Orientation MainControlOrientation
        {
            get { return _mainControlOrientation; }
            set
            {
                _mainControlOrientation = value;
                RaisePropertyChanged(nameof(MainControlOrientation));
            }
        }

        /// <summary>
        /// Voice indicator State
        /// </summary>
        private SpeechRecognizerState _voiceIndicatorState;
        public SpeechRecognizerState VoiceIndicatorState
        {
            get { return _voiceIndicatorState; }
            set
            {
                _voiceIndicatorState = value;
                RaisePropertyChanged(nameof(VoiceIndicatorState));
            }
        }

        /// <summary>
        /// Voice indicator text spoken
        /// </summary>
        private string _voiceIndicatorSpokenText;
        public string VoiceIndicatorSpokenText
        {
            get { return _voiceIndicatorSpokenText; }
            set
            {
                _voiceIndicatorSpokenText = value;
                RaisePropertyChanged(nameof(VoiceIndicatorSpokenText));
            }
        }

       
        // Constructor
        public BoardViewModel(MainWindow pMainWindow)
        {            

            //Load Animation object
            _boardAnimation = new BoardAnimation();

            // Load media player
            _mediaplayerReadText = new MediaPlayer();
            _mediaplayerReadText.AutoPlay = false;
            

            // Load sound effects
            _mediaplayerPieceMoveSound = new MediaPlayer();
            _mediaplayerPieceMoveSound.Source = MediaSource.CreateFromUri(new Uri("ms-appx:///Media/piecesound.wav"));
            _mediaplayerPieceMoveSound.AutoPlay = false;
 

            // Create move object
            _move = new Move(BoardSquareDataService.instance);


            // Set the window size changed event
            mainWindowRef = pMainWindow;
            mainWindowRef.SizeChanged += OnWindowSizeChanged;

        }


        /// <summary>
        /// Post init method
        /// </summary>
        public void PostInit()
        {
            // Load tiles           
            BoardSquareDataService.instance.Load(35, LargePawnEnabled, GameRecordDataService.instance.GetCurrentGame());
            BoardTiles = BoardSquareDataService.instance.BoardTiles;
            
            // Navigate game to latest record
            NavigateMaxRecord();
                        
            // Apply board colour
            ApplyBoardColour();

            // Set clock
            if (ClockEnabled)
            {
                chessClockControl.Show();
                LoadChessClock(true, GameRecordDataService.instance.GetCurrentGame());
            }
            else
            {
                chessClockControl.Hide();
            }

            // Set board shake state
            BoardSquare.Shake(ArrangeBoardEnabled);

            // Set the complete flag
            postInitComplete = true;

            // Set initial board size when postInit is completed   
            ResizeBoard();
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
        /// Sets the chess clock control
        /// </summary>
        /// <param name="pChessClockControl"></param>
        public void SetChessClockControl(ClockPanel pChessClockControl)
        {
            chessClockControl = pChessClockControl;

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
            coordinatesControl = pCoordinatesControl;

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
        /// Undo the last move
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnUndo_Click(object sender, RoutedEventArgs e)
        {
            await Undo();
            
        }

                
        /// <summary>
        /// Switch the board direction
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnSwitchDirection_Click(object sender, RoutedEventArgs e)
        {
            //Cancel any move tasks 
            StopSearchJob();

            // Get current board in view
            KaruahChessEngineClass board = new KaruahChessEngineClass();
            var record = GameRecordDataService.instance.Get(GameRecordCurrentValue);
            board.SetBoardArray(record.BoardArray);
            board.SetStateArray(record.StateArray);
            
            // Flip direction            
            board.SetStateActiveColour(board.GetStateActiveColour() * (-1));
            board.GetStateArray(record.StateArray);
            record.MoveSAN = string.Empty;

            // Save changes
            GameRecordDataService.instance.UpdateGameState(record);
            UpdateBoardIndicators(record);

            CheckChessClock();
            RefreshNavigation(true, false);


        }

        
        /// <summary>
        /// Edit board clicked
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnArrangeBoard_Click(object sender, RoutedEventArgs e)
        {
            //Cancel any move tasks 
            StopSearchJob();

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

            
            await showContentDialog(dialog);
            
                
        }

        /// <summary>
        /// Start a new game
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        async public void btnHint_Click(object sender, RoutedEventArgs e)
        {
            GameRecordArray currentGameRecord = GameRecordDataService.instance.GetCurrentGame();
            await StartHintTask(currentGameRecord);

        }

        /// <summary>
        /// Open voice help screen
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnVoiceCommand_Click(object sender, RoutedEventArgs e)
        {
            await HelpVoiceAction(true);

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
            var hwnd = WinRT.Interop.WindowNative.GetWindowHandle(mainWindowRef);
            WinRT.Interop.InitializeWithWindow.Initialize(fileSavePicker, hwnd);

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
                    ShowBoardMessage("", "Save complete", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);                    
                }

            }
            catch (Exception ex)
            {
                ShowBoardMessage("Error", ex.Message, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);                                
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
            var hwnd = WinRT.Interop.WindowNative.GetWindowHandle(mainWindowRef);
            WinRT.Interop.InitializeWithWindow.Initialize(fileOpenPicker, hwnd);

            fileOpenPicker.SuggestedStartLocation = PickerLocationId.DocumentsLibrary;
            fileOpenPicker.FileTypeFilter.Add(".gz");
            fileOpenPicker.FileTypeFilter.Add(".xml");            

            try
            {
                StorageFile file = await fileOpenPicker.PickSingleFileAsync();

                if (file != null)
                {
                    StopSearchJob();

                    // Do the import
                    await ImportDB.Import(file, ImportDB.ImportType.GameXML);

                    // Change transaction id
                    GameRecordDataService.instance.newTransaction();

                    // Load the latest record on to the board                    
                    NavigateMaxRecord();

                    var gr = GameRecordDataService.instance.Get();
                    BoardSquareDataService.instance.Update(gr, true);

                    LoadChessClock(true, gr);
                                        
                                        
                    ShowBoardMessage("", "Load complete", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                    
                }

            }
            catch (Exception ex)
            {
                ShowBoardMessage("Error", ex.Message, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
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

            await showContentDialog(dialog);
                       
        }

        /// <summary>
        /// About button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnAbout_Click(object sender, RoutedEventArgs e)
        {
            ContentDialog dialog = new AboutDialog().CreateDialog();
            await showContentDialog(dialog);

        }

        

        /// <summary>
        /// Import button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnImportPGN_Click(object sender, RoutedEventArgs e)
        {
            StopSearchJob();
            ContentDialog dialog = new ImportPGNDialog(this).CreateDialog();
            await showContentDialog(dialog);
        }

        /// <summary>
        /// Removed selected pieces from the board
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public void btnEditEraseSelection_Click(object sender, RoutedEventArgs e)
        {
            if (editSelection.Count > 0)
            {                
                foreach (int sqIndex in editSelection)
                {
                  ArrangeUpdate(' ', sqIndex);
                }

                // Clear any edit selections
                editSelection.Clear();
                BoardSquare.PieceEditSelectClearAll();            
            }
            else 
            {                
                 ShowBoardMessage("", "Cannot remove pieces, no squares selected", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
            }
        }

        /// <summary>
        /// Open the dialog that adds pieces to the board
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        public async void btnAddPiecesDialog_Click(object sender, RoutedEventArgs e)
        {
            if (editSelection.Count > 0)
            {
                var pieceEditDialogPage = new PieceEditDialog();
                var pieceEditDialog = pieceEditDialogPage.CreateDialog(this);
                await showContentDialog(pieceEditDialog);
            }
            else
            {
                ShowBoardMessage("", "Cannot add pieces, no squares selected", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
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
            
        if (pEntity != null && !ArrangeBoardEnabled && !LockPanel && !pTilePanel.dragInProgress) {                
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
        else if (ArrangeBoardEnabled && !pTilePanel.dragInProgress)
        {                
            var sq = (BoardSquare)pEntity;  
                

            if (sq.PieceType == TypeEnum.King)
            {
                var record = GameRecordDataService.instance.Get(GameRecordCurrentValue);
                ContentDialog dialog = new CastlingRightsDialog(sq.PieceType, sq.PieceColour, record, this).CreateDialog();
                await showContentDialog(dialog);
            }
            else
            {
                int sqIndex = pTileId - 1;
                if (!editSelection.Contains(sqIndex)) {
                    // Select the tile
                    editSelection.Add(sqIndex);
                    editLastTapIndex = sqIndex;
                }
                else if (sq.PieceType != TypeEnum.Empty && editLastTapIndex == sqIndex && editSelection.Contains(sqIndex)) {
                    // Attempt to select all pieces of the same type
                    var found = false;
                    foreach(Tile boardtile in pTilePanel.Tiles) {
                        BoardSquare boardtilesquare = (BoardSquare)boardtile.Entity;
                        int boardtilesquareindex = boardtile.Id - 1;
                        if (sqIndex != boardtilesquareindex && boardtilesquare.PieceType != TypeEnum.Empty && boardtilesquare.PieceType == sq.PieceType && boardtilesquare.PieceColour == sq.PieceColour && (!editSelection.Contains(boardtilesquareindex))) {
                            editSelection.Add(boardtilesquareindex);
                            found = true;
                        }
                    }

                    // If no similar pieces found just toggle the selection
                    if (!found)
                    {
                        if (editSelection.Contains(sqIndex)) {
                            editSelection.Remove(sqIndex);
                        }
                        else {
                            editSelection.Add(sqIndex);
                        }
                            
                    }
                    editLastTapIndex = -1;
                } 
                else
                {
                    if (editSelection.Contains(sqIndex)) {
                        editSelection.Remove(sqIndex);
                    }
                    else {
                        editSelection.Add(sqIndex);
                    }
                    editLastTapIndex = -1;
                }

                SolidColorBrush rectColour = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 233, 30, 99));
                BoardSquare.PieceEditSelectShow(editSelection, rectColour);
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

            if (!LockPanel && !ArrangeBoardEnabled)
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
        public async void btnEngineSettings_Click(object sender, RoutedEventArgs e)
        {
            ContentDialog dialog = new EngineSettingsDialog(this).CreateDialog();
            await showContentDialog(dialog);
        }

        
               
        /// <summary>
        /// Sound settings button event
        /// </summary>
        public async void btnSoundSettings_Click(object sender, RoutedEventArgs e)
        {
            ContentDialog dialog = new SoundSettingsDialog(this).CreateDialog();
            await showContentDialog(dialog);

        }

        /// <summary>
        /// Board settings button event
        /// </summary>
        public async void btnBoardSettings_Click(object sender, RoutedEventArgs e)
        {
            ContentDialog dialog = new BoardSettingsDialog(this).CreateDialog();
            await showContentDialog(dialog);

        }

        /// <summary>
        /// Piece settings button event
        /// </summary>
        public async void btnPieceSettings_Click(object sender, RoutedEventArgs e)
        {
            ContentDialog dialog = new PieceSettingsDialog(this).CreateDialog();
            await showContentDialog(dialog);

        }

        /// <summary>
        /// Shows the clock settings dialog
        /// </summary>
        public async Task ShowClockSettingsDialog()
        {
            ContentDialog dialog = new ClockSettingsDialog(this).CreateDialog();
            await showContentDialog(dialog);

        }

        /// <summary>
        /// Hint settings button event
        /// </summary>
        public async void btnHintSettings_Click(object sender, RoutedEventArgs e)
        {
            ContentDialog dialog = new HintSettingsDialog(this).CreateDialog();
            await showContentDialog(dialog);

        }

        /// <summary>
        /// Shows the Move Navigator PGN dialog
        /// </summary>
        public async Task ShowMoveNavigatorPGNDialog(string pPGNText)
        {
            ContentDialog dialog = new MoveNavigatorPGNDialog(pPGNText).CreateDialog();
            await showContentDialog(dialog);

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
                editBoard.SetBoardArray(record.BoardArray);
                editBoard.SetStateArray(record.StateArray);
                                
                var mResult = editBoard.ArrangeUpdate(pFen, pToIndex);

                if (mResult.success)
                {
                    editBoard.GetBoardArray(record.BoardArray);
                    editBoard.GetStateArray(record.StateArray);
                    record.MoveSAN = string.Empty;
                    BoardSquareDataService.instance.Update(record, true);
                    GameRecordDataService.instance.UpdateGameState(record);
                    RefreshNavigation(true, false);
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
                editBoard.SetBoardArray(record.BoardArray);
                editBoard.SetStateArray(record.StateArray);
                                
                var mResult = editBoard.Arrange(pFromIndex, pToIndex);

                if (mResult.success)
                {
                    editBoard.GetBoardArray(record.BoardArray);
                    editBoard.GetStateArray(record.StateArray);
                    record.MoveSAN = string.Empty;
                    BoardSquareDataService.instance.Update(record, true);
                    GameRecordDataService.instance.UpdateGameState(record);
                    RefreshNavigation(true, false);
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
                ClearBoardMessage();
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
                _userMoveProcessing = true;
                GameRecordArray boardBeforeMove = GameRecordDataService.instance.GetCurrentGame();                
                
                // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
                int promotionPiece = (int)PawnPromotionEnum.Queen; // default
                if ((!PromoteAutoEnabled) && GameRecordDataService.instance.CurrentGame.IsPawnPromotion(_move.FromIndex, _move.ToIndex))
                {
                    _pawnPromotionDialogOpen = true;
                    var promotionPage = new PawnPromotionDialog(GameRecordDataService.instance.CurrentGame.GetStateActiveColour());
                    var promotionDialog = promotionPage.CreateDialog();
                    await showContentDialog(promotionDialog);
                    promotionPiece = promotionPage.Result;
                    _pawnPromotionDialogOpen = false;
                }

                // Do the move                    
                int gameStatusBeforeMove = GameRecordDataService.instance.CurrentGame.GetStateGameStatus();                    
                var mResult = GameRecordDataService.instance.CurrentGame.Move(_move.FromIndex, _move.ToIndex, promotionPiece, true, true);                    
                               
                if (mResult.success) {
                    // Read Text, show message
                    var soundTasks = new List<Task>();                    
                    
                    // Do animation
                    var boardAfterMove = GameRecordDataService.instance.GetCurrentGame();
                    long transId = GameRecordDataService.instance.transactionId;

                    if (pAnimate) {
                        double duration = Constants.movespeedseconds[Math.Clamp(moveSpeed, 0, Constants.movespeedseconds.Count - 1)];
                        var moveAnimationList = _boardAnimation.CreateAnimationList(boardBeforeMove, boardAfterMove, duration);
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

                        // Record the game
                        RecordCurrentGameState(mResult.moveSAN);

                        // Check the clock
                        CheckChessClock();

                        // Update score if checkmate occurred
                        if (gameStatusBeforeMove == (int)BoardStatusEnum.Ready && GameRecordDataService.instance.CurrentGame.GetStateGameStatus() == (int)BoardStatusEnum.Checkmate)
                        {
                            await afterCheckMate(GameRecordDataService.instance.CurrentGame);

                            // Ensure listening is stopped if using voice commands
                            StopVoiceListen();
                        }
                        
                        await StartComputerMoveTask();
                        
                    }
                }
                else if (!mResult.success)
                {   
                    ShowBoardMessage("", mResult.returnMessage, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                    
                }

                // Clear the move selected
                _move.Clear();
                _userMoveProcessing = false;

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
            
            if (boardStatus == 0 && (!ComputerMoveProcessing) && (!ComputerHintProcessing) && (!ArrangeBoardEnabled) && ComputerPlayerEnabled && computerColour == turnColour) {
                LockPanel = true;

                // Clear the user move since starting computer move
                _move.Clear();

                _ctsComputer = new CancellationTokenSource();
                var token = _ctsComputer.Token;
                token.Register(() => GameRecordDataService.instance.CurrentGame.CancelSearch());
                
                ComputerMoveProcessing = true;
                                
                
                // Start the search      
                SearchOptions options;                
                Strength strengthSetting = Constants.strengthList[Math.Clamp(LimitSkillLevel, 0, Constants.strengthList.Count - 1)];
                options.limitSkillLevel = strengthSetting.SkillLevel;  
                
                if (LimitAdvancedEnabled) { 
                    options.limitDepth = limitDepth;
                    options.limitNodes = limitMoveDuration > 0 ? Constants.NODELIMIT_HIGH : Constants.NODELIMIT_STANDARD;
                    options.limitMoveDuration = limitMoveDuration;
                    options.limitThreads = limitThreads;
                }
                else
                {
                    options.limitDepth = strengthSetting.Depth;
                    options.limitNodes = Constants.NODELIMIT_STANDARD;
                    options.limitMoveDuration = strengthSetting.TimeLimitms;
                    options.limitThreads = Environment.ProcessorCount > 1 ? Environment.ProcessorCount - 1 : 1;
                }

                options.randomiseFirstMove = RandomiseFirstMoveEnabled;

                // Try alternate move if repeat move detected
                options.alternateMove = IsRepeatMove();

                var moveTask = Task.Run(() => GameRecordDataService.instance.CurrentGame.SearchStart(options), token);
                SearchResult topMove = await moveTask;

                if (moveTask.IsFaulted)
                {
                    helper.LogException(moveTask.Exception);
                }

                await DoMoveOnBoard(topMove);
                

                // Clear the cancellation token source
                _ctsComputer = null;

                // Unlock panel
                ComputerMoveProcessing = false;

                
                LockPanel = false;

                
            }
        }
                
        /// <summary>
        /// Start hint task
        /// </summary>
        /// <returns></returns>
        private async Task StartHintTask(GameRecordArray pRecord)
        {
            hintBoard.SetBoardArray(pRecord.BoardArray);
            hintBoard.SetStateArray(pRecord.StateArray);


            int boardStatus = hintBoard.GetStateGameStatus();

            if (boardStatus == 0 && (!(_userMoveProcessing) && !ComputerMoveProcessing) && (!ComputerHintProcessing) && (!ArrangeBoardEnabled))
            {
                LockPanel = true;
                ComputerHintProcessing = true;
                

                // Hint always uses the highest selectable engine strength
                SearchOptions searchOptions = new SearchOptions();                
                searchOptions.limitSkillLevel = Constants.strengthList[^1].SkillLevel;
                searchOptions.limitDepth = Constants.strengthList[^1].Depth;
                searchOptions.limitNodes = Constants.NODELIMIT_STANDARD;
                searchOptions.limitMoveDuration = Constants.strengthList[^1].TimeLimitms;
                searchOptions.limitThreads = Environment.ProcessorCount > 1 ? Environment.ProcessorCount - 1 : 1;
                searchOptions.randomiseFirstMove = false;
                searchOptions.alternateMove = false;

                _ctsHint = new CancellationTokenSource();
                var token = _ctsHint.Token;
                token.Register(() => hintBoard.CancelSearch());
                var moveTask = Task.Run(() => hintBoard.SearchStart(searchOptions), token);
                SearchResult topMove = await moveTask;
                
                if (moveTask.IsFaulted)
                {
                    helper.LogException(moveTask.Exception);
                }

                bool moveSuccess = false;
                if ((!topMove.cancelled) && (topMove.error == 0))
                {
                    if (HintMoveEnabled)
                    {
                        moveSuccess = await DoMoveOnBoard(topMove);
                    }
                    else
                    {
                        HashSet<int> moveIndexes = new HashSet<int>();
                        if (topMove.moveFromIndex != topMove.moveToIndex)
                        {
                            moveIndexes.Add(topMove.moveFromIndex);
                            moveIndexes.Add(topMove.moveToIndex);
                        }

                        int boardColourIndex = Constants.darkSquareColourList.IndexOf(ColourDarkSquaresARGB);
                        if (boardColourIndex > -1)
                        {
                            string fromCoord = helper.BoardCoordinateDict[topMove.moveFromIndex];
                            string toCoord = helper.BoardCoordinateDict[topMove.moveToIndex];
                            BoardSquare.RectangleShow(moveIndexes, Constants.hintColourList[boardColourIndex]);
                            ReadText($"Best move is {fromCoord} to {toCoord}");
                        }
                    }
                }
                else
                {
                    if (topMove.error > 0)
                    {
                        ShowBoardMessage("", topMove.errorMessage + ".", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.Fixed);
                        helper.LogError(topMove.error);
                    }
                    
                }

                ComputerHintProcessing = false;
                LockPanel = false;

                if (moveSuccess)
                {
                    await StartComputerMoveTask();
                }
            }
            else
            {

                if (boardStatus != 0)
                {
                    ShowBoardMessage("", "Unable to show a hint as the game has finished.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                }
                else if (ArrangeBoardEnabled)
                {
                    ShowBoardMessage("", "Unable to show a hint in edit mode.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                }
                else if (ComputerMoveProcessing)
                {
                    ShowBoardMessage("", "Unable to show a hint as processing a move.", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                }

            }
                        
        }

        private async Task<bool> DoMoveOnBoard(SearchResult topMove)
        {
            bool success = false;

            if ((!topMove.cancelled) && (topMove.error == 0))
            {
                GameRecordArray boardBeforeMove = GameRecordDataService.instance.GetCurrentGame();
                int gameStatusBeforeMove = GameRecordDataService.instance.CurrentGame.GetStateGameStatus();

                MoveResult mResult = GameRecordDataService.instance.CurrentGame.Move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, true, true);
                if (mResult.success)
                {
                    
                    // Read Text, show message
                    var soundTasks = new List<Task>();

                    // Do animation
                    var boardAfterMove = GameRecordDataService.instance.GetCurrentGame();
                    double duration = Constants.movespeedseconds[Math.Clamp(moveSpeed, 0, Constants.movespeedseconds.Count - 1)];
                    var moveAnimationList = _boardAnimation.CreateAnimationList(boardBeforeMove, boardAfterMove, duration);
                    BoardSquareDataService.instance.Update(boardAfterMove, true);

                    long transId = GameRecordDataService.instance.transactionId;
                    await StartPieceAnimation(true, moveAnimationList, true);

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

                        RecordCurrentGameState(mResult.moveSAN);
                        CheckChessClock();

                        // Update score if checkmate occurred
                        if (gameStatusBeforeMove == (int)BoardStatusEnum.Ready && GameRecordDataService.instance.CurrentGame.GetStateGameStatus() == (int)BoardStatusEnum.Checkmate)
                        {
                            await afterCheckMate(GameRecordDataService.instance.CurrentGame);

                            // Ensure listening is stopped if using voice commands
                            StopVoiceListen();
                        }

                        success = true;
                    }

                }
                else
                {
                    if (topMove.moveFromIndex > -1 && topMove.moveToIndex > -1)
                    {
                        ShowBoardMessage("", "Engine attempted an invalid move.", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.Fixed);
                    }
                    else
                    {
                        ShowBoardMessage("", "Move not received from Engine.", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.Fixed);

                    }
                }
            }
            else
            {
                if (topMove.error > 0)
                {
                    ShowBoardMessage("", topMove.errorMessage + ".", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.Fixed);
                    helper.LogError(topMove.error);
                }

            }

            return success;
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
            LockPanel = true;
            StopSearchJob();
           
            
            if (ClockEnabled)
            {
                // Initialise a new game            
                int defaultSecondsIndex = ParameterDataService.instance.Get<ParamClockDefault>().Index;
                if (defaultSecondsIndex < Constants.clockResetSeconds.Count)
                {
                    int defaultSeconds = Constants.clockResetSeconds[defaultSecondsIndex];
                    GameRecordDataService.instance.Reset(defaultSeconds, defaultSeconds);
                    chessClockControl.SetClock(defaultSeconds, defaultSeconds);                    
                }
                else
                {
                    GameRecordDataService.instance.Reset(0, 0);
                    chessClockControl.SetClock(0, 0);
                }
            }
            else
            {
                GameRecordDataService.instance.Reset(0, 0);
            }



            // Ensure max record game is on the board  
            NavigateMaxRecord();
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
                StopSearchJob();

                // Set the state of the game to resign                
                GameRecordDataService.instance.CurrentGame.SetStateGameStatus((int)BoardStatusEnum.Resigned);

                // Record current game state
                var turn = GameRecordDataService.instance.CurrentGame.GetStateActiveColour();
                string comment = turn == 1 ? "{White resigns.}" : "{Black resigns.}";
                RecordCurrentGameState(comment);
                CheckChessClock();
                                
                // Do animation, display message
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
        /// Time has expired
        /// </summary>
        public async Task TimeExpired(ColourEnum pWinner)
        {
                // Check that not in edit mode               
            if (ArrangeBoardEnabled) {
                // Cannot expire time if in edit mode
                return;
            }

            // Ensure game record is set to the latest
            NavigateMaxRecord();

            int status = GameRecordDataService.instance.CurrentGame.GetStateGameStatus();

            // Only expire time if game is not already finished
            if (status == (int)BoardStatusEnum.Ready) {

                // Stop task if running
                StopSearchJob();

                // Set the state of the game to time expired
                GameRecordDataService.instance.CurrentGame.SetStateGameStatus((int)BoardStatusEnum.TimeExpired);


                // Record current game state
                var turn = GameRecordDataService.instance.CurrentGame.GetStateActiveColour();
                string comment = turn == 1 ? "{White lost on time.}" : "{Black lost on time.}";
                RecordCurrentGameState(comment);

                // Do animation, display message                
                await doKingFallAnimation(pWinner);

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

            // Increase skill level
            if (LevelAutoEnabled && humanWinAgainstComputer)
            {
                int nextSkillLevel = LimitSkillLevel + 1;                                                
                if (nextSkillLevel >=0 && nextSkillLevel <= (Constants.strengthList.Count - 1))
                {
                    LimitSkillLevel = nextSkillLevel;
                    ShowBoardMessage("", "Congratulations, you have now progressed to the next level. The engine playing strength is now set to " + Constants.strengthList[nextSkillLevel].Label + ".", TextMessage.TypeEnum.Award, TextMessage.AnimationEnum.Fixed);
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
                else if (ClockEnabled && board.GetStateGameStatus() == (int)BoardStatusEnum.TimeExpired && chessClockControl.WhiteClock.RemainingTime().TotalSeconds == 0 && chessClockControl.BlackClock.RemainingTime().TotalSeconds > 0)
                {
                    gameFinished = true;
                    msgTitle = "Black wins! White time has expired.";
                }
                else if (ClockEnabled && board.GetStateGameStatus() == (int)BoardStatusEnum.TimeExpired && chessClockControl.WhiteClock.RemainingTime().TotalSeconds > 0 && chessClockControl.BlackClock.RemainingTime().TotalSeconds == 0)
                {
                    gameFinished = true;
                    msgTitle = "White wins! Black time has expired.";
                }

                

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
        private void OnWindowSizeChanged(object sender, Microsoft.UI.Xaml.WindowSizeChangedEventArgs e)
        {
            
            if (!_boardResizeTimerRunning) {
                // Set flag so only one timer runs at a time
                _boardResizeTimerRunning = true;

                // Create oneshot timer to resize the board
                ThreadPoolTimer DelayTimer = ThreadPoolTimer.CreateTimer((timer) =>
                {
                    mainDispatcherQueue.TryEnqueue(() =>
                    {
                        ResizeBoard();
                    });

                },  TimeSpan.FromMilliseconds(600), (x) => { _boardResizeTimerRunning = false; } );
            }
            
            
        }
                

        /// <summary>
        /// Helper function to resize the board
        /// </summary>
        private void ResizeBoard()
        {
            // Exit if post init not completed
            if (!postInitComplete) return;

            // End any animations
            EndPieceAnimation();

            // Get functionality status
            bool navigatorBarEnabled = NavigatorEnabled;
            bool coordEnabled = BoardCoordEnabled;
            bool clockEnabled = ClockEnabled;

            
            // Get window bounds
            double windowWidth = mainWindowRef.Bounds.Width;
            double windowHeight = mainWindowRef.Bounds.Height;

            // Set root padding
            RootPadding = new Thickness(3, 0, 3, 2);
            
            // Set the board margin taken up by the coordinates                      
            if (coordEnabled) {
                BoardCoordinateMargin = new Thickness(12, 0, 0, 20);                
            }
            else {
                BoardCoordinateMargin = new Thickness(0, 0, 0, 0);
            }

            // Get the board border thickness
            Thickness boardBorderThickness = _boardTilePanelControl.BorderThickness;
                        
            // Calculate area available for board
            double menuBarHeight = 48;
            double navigatorBarHeight;
            if (navigatorBarEnabled) navigatorBarHeight = 50;
            else navigatorBarHeight = 0;

            double clockHeight;
            if (clockEnabled) clockHeight = 40;
            else clockHeight = 0;

            var boardBorderHorizontalThickness = boardBorderThickness.Left + boardBorderThickness.Right;
            var boardBorderVerticalThickness = boardBorderThickness.Top + boardBorderThickness.Bottom;
            var boardHorizontalMargin = BoardCoordinateMargin.Left + BoardCoordinateMargin.Right;
            var boardVerticalMargin = BoardCoordinateMargin.Top + BoardCoordinateMargin.Bottom;
            var rootHorizontalPadding = RootPadding.Left + RootPadding.Right;
            var rootVerticalPadding = RootPadding.Top + RootPadding.Bottom;
            var actionButtonGutterWidth = 0;
            var actionButtonGutterHeight = 0;

            // Main action button control space
            if (windowWidth > windowHeight)
            {
                MainControlOrientation = Orientation.Vertical;
                actionButtonGutterWidth = 42;
            }
            else
            {
                MainControlOrientation = Orientation.Horizontal;
                actionButtonGutterHeight = 42;
            }


            var boardWidthAvailable = windowWidth - boardBorderHorizontalThickness - boardHorizontalMargin - rootHorizontalPadding - actionButtonGutterWidth;
            var boardHeightAvailable = windowHeight - menuBarHeight - navigatorBarHeight - clockHeight - boardBorderVerticalThickness - boardVerticalMargin - rootVerticalPadding - actionButtonGutterHeight;

            // Calculate tilesize
            double tileSize;
            if (boardWidthAvailable < boardHeightAvailable) {
                // Casting to int so that the tile size is rounded towards zero
                tileSize = (double)(int)(boardWidthAvailable / 8);
            }
            else {
                tileSize = (double)(int)(boardHeightAvailable / 8);
            }

            // Set min limit
            if (tileSize < 24) tileSize = 24;

            // Set tile size
            BoardSquareDataService.instance.SquareSize = tileSize;               
            
            foreach (var item in BoardTiles)
            {
                // Set the tile size
                item.StyleTemplate.Width = tileSize;
                item.StyleTemplate.Height = tileSize;

                // Recode the image size
                var p = ((BoardSquare)item.Entity).Piece;
                p.SetType(p.Type, p.Colour, tileSize, tileSize, LargePawnEnabled);
               
            }

            // Set the board width (this includes the margins)
            var tileRowWidth = tileSize * 8;
            BoardWidth = tileRowWidth + boardHorizontalMargin + boardBorderHorizontalThickness + rootHorizontalPadding;

            // Set coordinates
            if (coordinatesControl != null && _boardTilePanelControl != null) {
                coordinatesControl.Show(coordEnabled);
                if (coordEnabled) { 
                    var coordinateWidth = tileRowWidth + boardHorizontalMargin + boardBorderHorizontalThickness;
                    var coordianteHeight = tileRowWidth + boardVerticalMargin + boardBorderVerticalThickness;
                    coordinatesControl.Draw(tileSize, coordinateWidth, coordianteHeight, RotateBoardValue);
                }

            }

            
            // Position board messages
            PositionBoardMessage();           

            
        }

        /// <summary>
        /// Refresh pawns
        /// </summary>
        private void RefreshPieces()
        {
            BoardSquareDataService.instance.LargePawn = LargePawnEnabled;
            foreach (var item in BoardTiles)
            {
                var p = ((BoardSquare)item.Entity).Piece;                
                p.RefreshPiece(LargePawnEnabled);               
            }

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
        /// Shows board message
        /// </summary>        
        public void ShowBoardMessage(String pTitle, String pMessage, TextMessage.TypeEnum pType, TextMessage.AnimationEnum pAnimation)
        {
            // Marshall back to UI thread
            mainDispatcherQueue.TryEnqueue(() =>
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
        public void ClearBoardMessage()
        {
            // Marshall back to UI thread
            mainDispatcherQueue.TryEnqueue(() =>
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
            
        }

        /// <summary>
        ///  Records the current state of the game
        /// </summary>
        private void RecordCurrentGameState(string pMoveSAN)
        {
            
            try {
                int whiteClock = 0;
                int blackClock = 0;
                // Update the clock offset before saving            
                if (chessClockControl != null && ClockEnabled)
                {                                      
                    whiteClock = (int)chessClockControl.WhiteClock.RemainingTime().TotalSeconds;
                    blackClock = (int)chessClockControl.BlackClock.RemainingTime().TotalSeconds;
                }

                // Record current game state                 
                int success =  GameRecordDataService.instance.RecordGameState(whiteClock, blackClock, pMoveSAN);
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
        /// Text to speech
        /// </summary>
        /// <param name="pText"></param>
        private async Task ReadText(string pText)
        {
            if (SoundReadEnabled) {
                // Increment ignore flag in voice recogniser
                VoiceRecogniserIgnoreIncrement();

                // Phonemes
                if (pText == "En passant")
                {
                    pText = "<phoneme alphabet='x-microsoft-ups' ph='S1 lng AO N . P AA S lng AO N'>En passant</phoneme>";
                }

                // Read text
                await readTextThrottler.WaitAsync();
                try
                {
                    
                    using (var speech = new SpeechSynthesizer())
                    {
                        VoiceInformation voiceInfo = SpeechSynthesizer.DefaultVoice;
                        

                        if (voiceInfo != null)
                        {
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
                            _mediaplayerReadText.MediaEnded += mediaEndedHandler;
                            _mediaplayerReadText.PlaybackSession.Position = new TimeSpan(0, 0, 0);
                            _mediaplayerReadText.Play();
                            
                            // Wait here, but timeout after 2 seconds
                            await Task.WhenAny(tcs.Task, Task.Delay(2000));

                            _mediaplayerReadText.MediaEnded -= mediaEndedHandler;
                        }
                    }
                }
                catch (System.IO.FileNotFoundException)
                {
                    SoundReadEnabled = false;
                    ShowBoardMessage("Error", "Speech Synthesizer is unavailable. The read messages out loud option in the sound settings has now been disabled. To use this feature text to speech settings must be enabled on your operating system.", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                }
                finally
                {
                    readTextThrottler.Release();
                }

                // Decrement ignore flag in voice recogniser
                VoiceRecogniserIgnoreDecrement();


            }

        }


        /// <summary>
        /// Start voice recognition
        /// </summary>        
        public async void btnStartVoiceListen_Click(object sender, RoutedEventArgs e)
        {
            
            if ((!ComputerMoveProcessing) && (!ComputerHintProcessing) && (!ComputerVoiceProcessing)) 
            { 
                ComputerVoiceProcessing = true;

                bool initSuccess = await InitVoiceListen();

                ComputerVoiceProcessing = false;
            }
        }

        /// <summary>
        /// Initialise voice listen
        /// </summary>
        private async Task<bool> InitVoiceListen() 
        {
            bool success = false;
            try
            {
                bool permitted = await VoiceRecognition.CheckMicrophonePermission();

                if (permitted)
                {
                    if (_voiceRecogniser == null)
                    {
                        _voiceRecogniser = new VoiceRecognition();
                    }

                    Language speechLanguage = SpeechRecognizer.SystemSpeechLanguage;

                    if (speechLanguage != null)
                    {
                        string langTag = speechLanguage.LanguageTag;
                        if (VoiceRecognition.SupportedLanguages.Contains(langTag))
                        {
                            await _voiceRecogniser.Initialise(speechLanguage, MoveVoiceActionA, MoveVoiceActionB, SetVoiceIndicatorState, SetVoiceIndicatorSpokenText, StartMoveVoiceAction);                            
                            success = true;
                        }
                        else
                        {
                            var msgA = "Voice commands not available. ";
                            var msgB = "Default speech language must be set to English - AU, CA, GB, IN, NZ, or US.";
                            ShowBoardMessage("", msgA + msgB, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                        }
                    }
                    else
                    {
                        var msg = "Voice commands not available. No speech languages are installed on your system.";
                        ShowBoardMessage("", msg, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                    }

                }
                else
                {
                    var msg = "To use voice commands, your system privacy settings must first allow access to the microphone.\r\nStart -> Settings -> Privacy & Security -> Microphone.";
                    ShowBoardMessage("", msg, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);

                }
            }
            catch (Exception ex)
            {

                if (ex.HResult == noCaptureDevices)
                {
                    ShowBoardMessage("Error", "No microphone device appears to be available. Check that your microphone is enabled and that this app has permission to use the microphone.", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                }
                else
                {
                    ShowBoardMessage("Error", ex.Message, TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
                }
            }

            return success;
        }


        /// <summary>
        /// Runs when voice is unchecked
        /// </summary>        
        public void StopVoiceListen()
        {
            if (_voiceRecogniser != null)
            {
                _voiceRecogniser.Stop();
                _voiceRecogniser = null;
            }
        }


        /// <summary>
        /// Executes a move action from voice command
        /// </summary>
        /// <param name="pMoveAction"></param>
        private async Task MoveVoiceActionA(List<int> pMoveList, String pTextSpoken)
        {            

            if (pMoveList != null && !_pawnPromotionDialogOpen) {
                
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
                            ShowBoardMessage("", msg, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                        }
                    }
                }                   
                                
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
                    ShowBoardMessage("", msg, TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
                }

                
            }
                        
        }

        /// <summary>
        /// Executes a move action from voice command
        /// </summary>
        /// <param name="pMoveAction"></param>
        private async Task StartMoveVoiceAction()
        { 
            await StartComputerMoveTask();
        }

       
        /// <summary>
        /// Set the voice indicator state
        /// </summary>        
        private void SetVoiceIndicatorState(SpeechRecognizerState pState)
        {            
            VoiceIndicatorState = pState;
        }

        /// <summary>
        /// Set the voice indicator spoken text
        /// </summary>        
        private void SetVoiceIndicatorSpokenText(string pText)
        {
            VoiceIndicatorSpokenText = pText;

        }

        
        /// <summary>
        /// Increment the ignore flag on the voice recogniser
        /// </summary>
        private void VoiceRecogniserIgnoreIncrement()
        {
            if (_voiceRecogniser != null)
            {
                _voiceRecogniser.Ignore++;
            }
        }

        /// <summary>
        /// Decrement the ignore flag on the voice recogniser
        /// </summary>
        private async Task VoiceRecogniserIgnoreDecrement()
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
        public void CheckChessClock()
        {
            if (chessClockControl != null) {
                if (ArrangeBoardEnabled)
                {
                    chessClockControl.PauseClock();
                }
                else {
                    // Only start the clock if the game has not finished
                    bool gameFinished = GameRecordDataService.instance.CurrentGame.GetStateGameStatus() != (int)BoardStatusEnum.Ready;
                    if (!gameFinished)
                    {
                        int turn = GameRecordDataService.instance.CurrentGame.GetStateActiveColour();
                        chessClockControl.Start(turn);
                    }
                    else
                    {
                        chessClockControl.PauseClock();
                    }
                }

                if ((!chessClockControl.IsPaused()) && GameRecordCurrentValue == GameRecordDataService.instance.GetMaxId())
                {
                    chessClockControl.ShowCurrentTime();
                }                
                
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
                if (GameRecordDataService.instance.RecordCount() == 1)
                {                    
                    int defaultSecondsIndex = ParameterDataService.instance.Get<ParamClockDefault>().Index;
                    if (defaultSecondsIndex < Constants.clockResetSeconds.Count) {
                        int defaultSeconds = Constants.clockResetSeconds[defaultSecondsIndex];
                                                    
                        if (board.GetStateWhiteClockOffset() == 0 || board.GetStateBlackClockOffset() == 0) {
                            InitialiseClockFirstMove(defaultSeconds, defaultSeconds);
                            board.SetStateWhiteClockOffset(defaultSeconds);
                            board.SetStateBlackClockOffset(defaultSeconds);
                        }
                }
            }

                chessClockControl.WhiteClock.SetNewLimit(new TimeSpan(0, 0, board.GetStateWhiteClockOffset()));
                chessClockControl.BlackClock.SetNewLimit(new TimeSpan(0, 0, board.GetStateBlackClockOffset()));
                chessClockControl.ShowCurrentTime();
            }

            // Switches between historical and current
            if (GameRecordCurrentValue == GameRecordDataService.instance.GetMaxId())
            {
                chessClockControl.ShowCurrentTime();
            }
            else
            {
                chessClockControl.ShowHistoricalTime(board.GetStateWhiteClockOffset(), board.GetStateBlackClockOffset());
            }

            
        }

      
        /// <summary>
        /// Update clock for the first move
        /// </summary>
        public void InitialiseClockFirstMove(int pWhiteSecondsRemaining, int pBlackSecondsRemaining)
        {
            // Update the current record state if only one record exists
            if (GameRecordDataService.instance.RecordCount() == 1)
            {
                GameRecordArray record = GameRecordDataService.instance.Get(GameRecordCurrentValue);
                if (record != null)
                {
                    KaruahChessEngineClass bufferBoard = new KaruahChessEngineClass();
                    bufferBoard.SetBoardArray(record.BoardArray);
                    bufferBoard.SetStateArray(record.StateArray);

                    bufferBoard.SetStateWhiteClockOffset(pWhiteSecondsRemaining);
                    bufferBoard.SetStateBlackClockOffset(pBlackSecondsRemaining);

                    bufferBoard.GetBoardArray(record.BoardArray);
                    bufferBoard.GetStateArray(record.StateArray);
                    record.MoveSAN = String.Empty;

                    BoardSquareDataService.instance.Update(record, true);
                    GameRecordDataService.instance.UpdateGameState(record);
                }
            }
        }

        
        /// <summary>
        /// Navigate to game record
        /// </summary>
        /// <param name="pRecId"></param>
        public async Task NavigateGameRecord(int pRecId, bool pAnimate, bool pReloadNav, bool pScrollNav)
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
                        double duration = Constants.movespeedseconds[Math.Clamp(moveSpeed, 0, Constants.movespeedseconds.Count - 1)];
                        var moveAnimationList = _boardAnimation.CreateAnimationList(oldBoard, updatedBoard, duration);
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
            if (pShow) {
                ContentDialog dialog = new VoiceCommandDialog(this).CreateDialog();
                await showContentDialog(dialog);
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
                SolidColorBrush rectColour = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 233, 30, 99));
                BoardSquare.RectangleShow(lastChanges, rectColour);
                ReadText("Last move.");
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
                    _moveNavigatorControl.SyncNavButtons();
                    _moveNavigatorControl.SetSelected(GameRecordCurrentValue);
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
        public void StopSearchJob()
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

            if (_ctsHint != null)
            {
                _ctsHint.Cancel();
            }
        }


        /// <summary>
        /// Play piece move sound effect
        /// </summary>
        private async Task playPieceMoveSoundEffect()
        {
            if (_mediaplayerPieceMoveSound != null && SoundEffectEnabled)
            {
                await pieceMoveSoundThrottler.WaitAsync();

                try
                {
                    var tcs = new TaskCompletionSource<bool>();

                    var mediaEndedHandler = new TypedEventHandler<MediaPlayer, object>((player, resource) =>
                    {
                        tcs.TrySetResult(true);
                    });

                    _mediaplayerPieceMoveSound.MediaEnded += mediaEndedHandler;
                    _mediaplayerPieceMoveSound.PlaybackSession.Position = new TimeSpan(0, 0, 0);
                    _mediaplayerPieceMoveSound.Play();

                    // Wait here, but timeout after 2 seconds
                    await Task.WhenAny(tcs.Task, Task.Delay(2000));

                    _mediaplayerPieceMoveSound.MediaEnded -= mediaEndedHandler;
                }
                finally
                {
                    pieceMoveSoundThrottler.Release();
                }
            }
        }

        /// <summary>        
        /// Updates selected tiles with a given piece type
        /// </summary>
        public void editToolUpdateSelectedTiles(Char pFen)
        {
            bool updated = false;        
            foreach (int sqIndex in editSelection) {
                ArrangeUpdate(pFen, sqIndex);
                updated = true;
            }

            // Clear any edit selections
            editSelection.Clear();
            BoardSquare.PieceEditSelectClearAll();            

            if (!updated) {                
                ShowBoardMessage("", "Cannot update, no squares are selected", TextMessage.TypeEnum.Info, TextMessage.AnimationEnum.FadeOut);
            }
        }

        /// <summary>
        /// Shows a content dialog.
        /// Only one content dialog is allowed to be shown at a time otherwise a crash may occur.
        /// </summary>
        async private Task showContentDialog(ContentDialog pDialog)
        {
            if (boardContentDialogCount > 0)
            {
                // Only allow one content dialog to be shown at a time.
                // Trying to show multiple dialogs will crash
                return;
            }
            
            boardContentDialog = pDialog;

            boardContentDialog.Closed += contentDialogClosed;
            boardContentDialogCount++;

            pDialog.XamlRoot = mainWindowRef.Content.XamlRoot;
            await pDialog.ShowAsync();
            
            boardContentDialog.Hide();
            boardContentDialog = null;

        }

        /// <summary>
        /// Closed event for content dialog
        /// </summary>   
        private void contentDialogClosed(ContentDialog sender, ContentDialogClosedEventArgs args)
        {
            boardContentDialogCount--;
        }

        
        /// <summary>
        /// Indicates if the computer is processing
        /// </summary>        
        public bool IsComputerProcessing(bool pBusyMove, bool pBusyHint, bool pBusyVoice)
        {
            return pBusyMove || pBusyHint || pBusyVoice;
        }


        /// <summary>
        /// Detect a repeat move
        /// </summary>        
        private bool IsRepeatMove()
        {
            bool repeated = false;

            SortedList<int, GameRecordArray> history = GameRecordDataService.instance.GameHistory();
            int endIndex = history.Count - 1;
            if (endIndex >= 4)
            {
                ulong[] currentBoardArray = history.Values[endIndex].BoardArray;

                for (int historyIndex = endIndex - 2; historyIndex >= 0; historyIndex -= 2)
                {                    
                    ulong[] historyBoardArray = history.Values[historyIndex].BoardArray;
                    bool differenceFound = false;
                         
                    if (currentBoardArray.Length == historyBoardArray.Length && historyBoardArray.Length > 0)
                    {                                                
                        // Compare all elements in both arrays and if one of them different,
                        // then they are different boards
                        for (int i = 0; i < currentBoardArray.Length; i++)
                        {
                            if (currentBoardArray[i] != historyBoardArray[i])
                            {
                                differenceFound = true;
                                break;
                            }
                        }

                        // Break if difference not found as no need to search further
                        if (!differenceFound)
                        {
                            repeated = true;
                            break;
                        }
                    }
                }               
            }

            return repeated;

        }

        /// <summary>
        /// Undo last move
        /// </summary>
        private async Task Undo()
        {
            
            LockPanel = true;

            StopSearchJob();

            // Do the undo
            var oldBoard = GameRecordDataService.instance.Get();

            var undo = GameRecordDataService.instance.Undo();
            if (undo)
            {
                // Set the clock
                LoadChessClock(true, GameRecordDataService.instance.GetCurrentGame());

                // Do rollback animation
                double duration = Constants.movespeedseconds[Math.Clamp(moveSpeed, 0, Constants.movespeedseconds.Count - 1)];
                var moveAnimationList = _boardAnimation.CreateAnimationList(oldBoard, GameRecordDataService.instance.Get(), duration);
                BoardSquareDataService.instance.Update(GameRecordDataService.instance.GetCurrentGame(), false);
                await StartPieceAnimation(true, moveAnimationList, true);


                // Set the game record values                                              
                NavigateMaxRecord();


            }

            LockPanel = false;
           
        }
        
        /// <summary>
        /// Gets the visibility of the level indicator
        /// </summary>                
        public Visibility LevelIndicatorVisible(SpeechRecognizerState pVoiceState)
        {            
            if (pVoiceState == SpeechRecognizerState.Capturing || pVoiceState == SpeechRecognizerState.SoundEnded || pVoiceState == SpeechRecognizerState.SoundStarted || pVoiceState == SpeechRecognizerState.SpeechDetected)
            {
                return Visibility.Collapsed;
            }
            else
            {
                return Visibility.Visible;
            }
        }

        /// <summary>
        /// Gets the visibility of the level indicator
        /// </summary>                
        public Visibility ActionButtonVisible(bool pVoiceProcessing)
        {
            if (pVoiceProcessing)
            {
                return Visibility.Visible;
            }
            else
            {
                return Visibility.Visible;
            }
        }


    }


}
