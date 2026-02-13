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

using KaruahChess.Model;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Media.Animation;


namespace KaruahChess.Pieces
{
    public sealed partial class PieceAnimation : UserControl
    {

        SemaphoreSlim throttler = new SemaphoreSlim(initialCount: 1);

        /// <summary>
        /// Constructor
        /// </summary>
        public PieceAnimation()
        {
            this.InitializeComponent();
            AnimationCanvas.Visibility = Visibility.Collapsed;
                
        }
                                  

        public int NegativeRotationZ
        {
            get { return (int)GetValue(NegativeRotationZProperty); }
            set { SetValue(NegativeRotationZProperty, value); }
        }

        // Using a DependencyProperty as the backing store for NegativeRotationZ.  
        public static readonly DependencyProperty NegativeRotationZProperty =
            DependencyProperty.Register("NegativeRotationZ", typeof(int), typeof(PieceAnimation), new PropertyMetadata(0));

                        
        /// <summary>
        /// Clears the animation
        /// </summary>
        public void Clear()
        {            
           // Clear animation
           AnimationCanvas.Children.Clear();
           AnimationCanvas.Visibility = Visibility.Collapsed;                      
        }


        /// <summary>
        /// Runs the next animation
        /// </summary>
        /// <returns></returns>
        public async Task RunAnimation(BoardSquareDataService pBoardSquareDS, List<PieceAnimationInstruction> pAnimationList)
        {

            await throttler.WaitAsync();  // Limit animation threads to one

            var taskList = new List<Task>();
            AnimationCanvas.Visibility = Visibility.Visible;
            var storyBoardList = new List<Storyboard>();

            foreach (var instruction in pAnimationList)
            {
                if (instruction.AnimationType == PieceAnimationInstruction.AnimationTypeEnum.Move)
                {
                    // Move a piece from one square to another            
                    var animImg = new Image();
                    var animStoryboard = new Storyboard();
                    SetupMoveAnimation(instruction, animImg, animStoryboard);
                    AnimationCanvas.Children.Add(animImg);
                    storyBoardList.Add(animStoryboard);

                    // Hide board squares to be animated
                    foreach (var index in instruction.HiddenSquareIndexes) pBoardSquareDS.Hide(index);

                    // Begin the storyboard
                    var taskA = BeginStoryboard(animStoryboard);
                    taskList.Add(taskA);
                }
                else if (instruction.AnimationType == PieceAnimationInstruction.AnimationTypeEnum.MoveFade)
                {
                    // Move a piece from one square to another and fade out during the move            
                    var animImg = new Image();
                    var animStoryboard = new Storyboard();
                    SetupMoveFadeAnimation(instruction, animImg, animStoryboard);
                    AnimationCanvas.Children.Add(animImg);
                    storyBoardList.Add(animStoryboard);

                    // Hide board squares to be animated
                    foreach (var index in instruction.HiddenSquareIndexes) pBoardSquareDS.Hide(index);

                    // Begin the storyboard
                    var taskA = BeginStoryboard(animStoryboard);
                    taskList.Add(taskA);
                }
                else if (instruction.AnimationType == PieceAnimationInstruction.AnimationTypeEnum.Take)
                {
                    // Move a piece from one square to another            
                    var animImg = new Image();
                    var animStoryboard = new Storyboard();
                    SetupTakeAnimation(instruction, animImg, animStoryboard);
                    AnimationCanvas.Children.Add(animImg);

                    // Hide board squares to be animated
                    foreach (var index in instruction.HiddenSquareIndexes) pBoardSquareDS.Hide(index);

                    // Begin the storyboard
                    var taskA = BeginStoryboard(animStoryboard);
                    taskList.Add(taskA);
                }
                else if (instruction.AnimationType == PieceAnimationInstruction.AnimationTypeEnum.Put)
                {
                    // Move a piece from one square to another            
                    var animImg = new Image();
                    var animStoryboard = new Storyboard();
                    SetupPutAnimation(instruction, animImg, animStoryboard);
                    AnimationCanvas.Children.Add(animImg);

                    // Hide board squares to be animated
                    foreach (var index in instruction.HiddenSquareIndexes) pBoardSquareDS.Hide(index);

                    // Begin the storyboard
                    var taskA = BeginStoryboard(animStoryboard);
                    taskList.Add(taskA);
                }
                else if (instruction.AnimationType == PieceAnimationInstruction.AnimationTypeEnum.Fall)
                {
                    // Move a piece from one square to another            
                    var animImg = new Image();
                    var animStoryboard = new Storyboard();
                    SetupKingFallAnimation(instruction, animImg, animStoryboard);
                    AnimationCanvas.Children.Add(animImg);

                    // Hide board squares to be animated
                    foreach (var index in instruction.HiddenSquareIndexes) pBoardSquareDS.Hide(index);

                    // Begin the storyboard
                    var taskA = BeginStoryboard(animStoryboard);
                    taskList.Add(taskA);
                }

            }

            // Wait for all tasks to finish
            await Task.WhenAll(taskList);
            throttler.Release(); 
            
        }


        /// <summary>
        /// Set up animation
        /// </summary>
        /// <param name="pAnimInstruct"></param>
        private void SetupMoveAnimation(PieceAnimationInstruction pAnimInstruct, Image pImage, Storyboard pStoryboard)
        {            
            QuinticEase ease = new QuinticEase();
            ease.EasingMode = EasingMode.EaseOut;

            // Set image and rotation                     
            PlaneProjection proj = new PlaneProjection();
            proj.RotationZ = -NegativeRotationZ;
            pImage.Projection = proj;
            pImage.Source = pAnimInstruct.ImageData;
            pImage.Width = pAnimInstruct.ImageData.DecodePixelHeight;
            pImage.Height = pAnimInstruct.ImageData.DecodePixelWidth;
            pImage.Visibility = Visibility.Visible;

            // Animation on the X axis
            DoubleAnimation animX = new DoubleAnimation();
            animX.From = pAnimInstruct.MoveFrom.X;
            animX.To = pAnimInstruct.MoveTo.X;
            animX.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animX.EasingFunction = ease;

            // Animation on the Y axis
            DoubleAnimation animY = new DoubleAnimation();
            animY.From = pAnimInstruct.MoveFrom.Y;
            animY.To = pAnimInstruct.MoveTo.Y;
            animY.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animY.EasingFunction = ease;

            // Set up the storyboard            
            pStoryboard.Children.Add(animX);
            pStoryboard.Children.Add(animY);
            Storyboard.SetTarget(animX, pImage);
            Storyboard.SetTarget(animY, pImage);
            Storyboard.SetTargetProperty(animX, "(Canvas.Left)");
            Storyboard.SetTargetProperty(animY, "(Canvas.Top)");
            pStoryboard.AutoReverse = false;
                       
        }

        /// <summary>
        /// Set up animation
        /// </summary>
        /// <param name="pAnimInstruct"></param>
        private void SetupMoveFadeAnimation(PieceAnimationInstruction pAnimInstruct, Image pImage, Storyboard pStoryboard)
        {
            QuinticEase ease = new QuinticEase();
            ease.EasingMode = EasingMode.EaseOut;

            // Set image and rotation                     
            PlaneProjection proj = new PlaneProjection();
            proj.RotationZ = -NegativeRotationZ;
            pImage.Projection = proj;
            pImage.Source = pAnimInstruct.ImageData;
            pImage.Width = pAnimInstruct.ImageData.DecodePixelHeight;
            pImage.Height = pAnimInstruct.ImageData.DecodePixelWidth;
            pImage.Visibility = Visibility.Visible;

            // Animation on the X axis
            DoubleAnimation animX = new DoubleAnimation();
            animX.From = pAnimInstruct.MoveFrom.X;
            animX.To = pAnimInstruct.MoveTo.X;
            animX.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animX.EasingFunction = ease;

            // Animation on the Y axis
            DoubleAnimation animY = new DoubleAnimation();
            animY.From = pAnimInstruct.MoveFrom.Y;
            animY.To = pAnimInstruct.MoveTo.Y;
            animY.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animY.EasingFunction = ease;

            // Animation opacity
            DoubleAnimation animOpacity = new DoubleAnimation();
            animOpacity.From = 1;
            animOpacity.To = 0;
            animOpacity.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animOpacity.EasingFunction = ease;

            // Set up the storyboard            
            pStoryboard.Children.Add(animX);
            pStoryboard.Children.Add(animY);
            pStoryboard.Children.Add(animOpacity);
            Storyboard.SetTarget(animX, pImage);
            Storyboard.SetTarget(animY, pImage);
            Storyboard.SetTarget(animOpacity, pImage);
            Storyboard.SetTargetProperty(animX, "(Canvas.Left)");
            Storyboard.SetTargetProperty(animY, "(Canvas.Top)");
            Storyboard.SetTargetProperty(animOpacity, "Opacity");
            pStoryboard.AutoReverse = false;

        }

        /// <summary>
        /// Set up animation
        /// </summary>
        /// <param name="pAnimInstruct"></param>
        private void SetupTakeAnimation(PieceAnimationInstruction pAnimInstruct, Image pImage, Storyboard pStoryboard)
        {
            QuinticEase ease = new QuinticEase();
            ease.EasingMode = EasingMode.EaseOut;

            // Set image rotation
            PlaneProjection proj = new PlaneProjection();
            proj.RotationZ = -NegativeRotationZ;
            pImage.Projection = proj;
            pImage.Source = pAnimInstruct.ImageData;
            pImage.Width = pAnimInstruct.ImageData.DecodePixelHeight;
            pImage.Height = pAnimInstruct.ImageData.DecodePixelWidth;
            pImage.Visibility = Visibility.Visible;
            pImage.RenderTransform = new CompositeTransform();

            // Set image position on the canvas
            Canvas.SetLeft(pImage, pAnimInstruct.MoveTo.X);
            Canvas.SetTop(pImage, pAnimInstruct.MoveTo.Y);

            // Animation rotate
            DoubleAnimation animRotate = new DoubleAnimation();
            animRotate.From = 0;
            animRotate.To = -20;
            animRotate.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animRotate.EasingFunction = ease;

            // Animation opacity
            DoubleAnimation animOpacity = new DoubleAnimation();
            animOpacity.From = 1;
            animOpacity.To = 0;
            animOpacity.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animOpacity.EasingFunction = ease;

            // Set up the storyboard            
            pStoryboard.Children.Add(animRotate);
            pStoryboard.Children.Add(animOpacity);
            Storyboard.SetTarget(animRotate, pImage);
            Storyboard.SetTarget(animOpacity, pImage);
            Storyboard.SetTargetProperty(animRotate, "(UIElement.RenderTransform).(CompositeTransform.Rotation)");
            Storyboard.SetTargetProperty(animOpacity, "Opacity");
            pStoryboard.AutoReverse = false;

            
        }

        /// <summary>
        /// Set up animation
        /// </summary>
        /// <param name="pAnimInstruct"></param>
        private void SetupPutAnimation(PieceAnimationInstruction pAnimInstruct, Image pImage, Storyboard pStoryboard)
        {
            QuinticEase ease = new QuinticEase();
            ease.EasingMode = EasingMode.EaseOut;

            // Set image rotation
            PlaneProjection proj = new PlaneProjection();
            proj.RotationZ = -NegativeRotationZ;
            pImage.Projection = proj;
            pImage.Source = pAnimInstruct.ImageData;
            pImage.Width = pAnimInstruct.ImageData.DecodePixelHeight;
            pImage.Height = pAnimInstruct.ImageData.DecodePixelWidth;
            pImage.Visibility = Visibility.Visible;
            pImage.RenderTransform = new CompositeTransform();

            // Set image position on the canvas
            Canvas.SetLeft(pImage, pAnimInstruct.MoveTo.X);
            Canvas.SetTop(pImage, pAnimInstruct.MoveTo.Y);

            // Animation rotate
            DoubleAnimation animRotate = new DoubleAnimation();
            animRotate.From = -20;
            animRotate.To = -0;
            animRotate.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animRotate.EasingFunction = ease;

            // Animation opacity
            DoubleAnimation animOpacity = new DoubleAnimation();
            animOpacity.From = 0;
            animOpacity.To = 1;
            animOpacity.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animOpacity.EasingFunction = ease;

            // Set up the storyboard            
            pStoryboard.Children.Add(animRotate);
            pStoryboard.Children.Add(animOpacity);
            Storyboard.SetTarget(animRotate, pImage);
            Storyboard.SetTarget(animOpacity, pImage);
            Storyboard.SetTargetProperty(animRotate, "(UIElement.RenderTransform).(CompositeTransform.Rotation)");
            Storyboard.SetTargetProperty(animOpacity, "Opacity");
            pStoryboard.AutoReverse = false;
        }


        /// <summary>
        /// Set up animation
        /// </summary>
        /// <param name="pAnimInstruct"></param>
        private void SetupKingFallAnimation(PieceAnimationInstruction pAnimInstruct, Image pImage, Storyboard pStoryboard)
        {
            BounceEase bounce = new BounceEase();
            bounce.EasingMode = EasingMode.EaseOut;

            // Set image rotation
            PlaneProjection proj = new PlaneProjection();
            proj.RotationZ = -NegativeRotationZ;
            pImage.Projection = proj;
            pImage.Source = pAnimInstruct.ImageData;
            pImage.Width = pAnimInstruct.ImageData.DecodePixelHeight;
            pImage.Height = pAnimInstruct.ImageData.DecodePixelWidth;
            pImage.Visibility = Visibility.Visible;
            pImage.RenderTransform = new CompositeTransform();
            pImage.RenderTransformOrigin = new Windows.Foundation.Point(0.5,0.7);

            // Set image position on the canvas
            Canvas.SetLeft(pImage, pAnimInstruct.MoveTo.X);
            Canvas.SetTop(pImage, pAnimInstruct.MoveTo.Y);

            // Animation rotate
            DoubleAnimation animRotate = new DoubleAnimation();
            animRotate.From = 0;
            animRotate.To = 90;
            animRotate.Duration = new Duration(TimeSpan.FromSeconds(pAnimInstruct.Duration));
            animRotate.EasingFunction = bounce;

            
            // Set up the storyboard            
            pStoryboard.Children.Add(animRotate);            
            Storyboard.SetTarget(animRotate, pImage);            
            Storyboard.SetTargetProperty(animRotate, "(UIElement.RenderTransform).(CompositeTransform.Rotation)");            
            pStoryboard.AutoReverse = false;


        }

        /// <summary>
        /// Runs storyboard as a task
        /// </summary>
        /// <param name="storyboard"></param>
        /// <returns></returns>
        private Task BeginStoryboard(Storyboard pStoryboard)
        {
            System.Threading.Tasks.TaskCompletionSource<bool> tcs = new TaskCompletionSource<bool>();
            if (pStoryboard == null)
                tcs.SetException(new ArgumentNullException());
            else
            {
                EventHandler<object> onComplete = null;
                onComplete = (s, e) => {
                    pStoryboard.Completed -= onComplete;
                    tcs.SetResult(true);
                };
                pStoryboard.Completed += onComplete;
                pStoryboard.Begin();
            }
            return tcs.Task;
        }
    }
}
