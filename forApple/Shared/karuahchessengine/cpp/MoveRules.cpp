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

#include "moverules.h"
#include "helper.h"
#include "bitboard.h"
#include <vector>

namespace KaruahChess {
	namespace MoveRules {


		/// <summary>
		/// Moves a piece
		/// </summary>
		bool Move(const int pFromIndex, const int pToIndex, BitBoard& pBoard, const helper::PawnPromotionEnum pPawnPromotionPiece, const bool pValidateEnabled, const bool pCommit)
		{
			bool success = true;

			// Get original values before move
			int origFromSpin = pBoard.GetSpin(pFromIndex);
			int origToSpin = pBoard.GetSpin(pToIndex);

			// Check that from and to indexes fall within the valid range
			if (!(pFromIndex >= 0 && pFromIndex <= 63 && pToIndex >= 0 && pToIndex <= 63)) return false;

			// Validate move turn
			if (pValidateEnabled)
			{
				pBoard.ReturnMessage = "";  // Ensure message is clear

				// Check turn is valid
				int Turn = pBoard.StateActiveColour;
				if ((Turn == helper::WHITEPIECE && origFromSpin < 0) || (Turn == helper::BLACKPIECE && origFromSpin > 0))
				{
					pBoard.ReturnMessage = "Wrong side attempted to move.";
					return false;
				}


				// Validate from move
				if ((pBoard.GetOccupied(pBoard.StateActiveColour) & helper::BITMASK >> pFromIndex) == 0)
				{
					pBoard.ReturnMessage = "Move from " + helper::BoardCoordinateDict.at(pFromIndex) + " to " + helper::BoardCoordinateDict.at(pToIndex) + " is not valid";
					return false;
				}

				// Validate to move
				uint64_t attackPath = pBoard.GetPotentialMove(pFromIndex);
				if (((helper::BITMASK >> pToIndex) & attackPath) == 0)
				{
					pBoard.ReturnMessage = "Move from " + helper::BoardCoordinateDict.at(pFromIndex) + " to " + helper::BoardCoordinateDict.at(pToIndex) + " is not valid";
					return false;
				}


			}


			// Do the move
			pBoard.Update(pFromIndex, 0);
			pBoard.Update(pToIndex, origFromSpin);


			// Check for pawn promotion
			if (origFromSpin == helper::WHITE_PAWN_SPIN && pToIndex >= 0 && pToIndex <= 7)
			{
				int pawnPromotion = (int)pPawnPromotionPiece * pBoard.StateActiveColour;
				pBoard.Update(pToIndex, pawnPromotion);
			}
			else if (origFromSpin == helper::BLACK_PAWN_SPIN && pToIndex >= 56 && pToIndex <= 63)
			{
				int pawnPromotion = (int)pPawnPromotionPiece * pBoard.StateActiveColour;
				pBoard.Update(pToIndex, pawnPromotion);
			}

			// Variables
			bool enPassant = false;
			bool castle = false;
			int rookFromSqIndex = -1;
			int rookToSqIndex = -1;
			int rookSpin = 0;

			// Store original values
			int origEnpassantIndex = pBoard.StateEnpassantIndex;
			int origEnpassantSpin = pBoard.GetSpin(pBoard.StateEnpassantIndex);

			// Detect enpassant
			if ((origFromSpin == helper::WHITE_PAWN_SPIN && (pToIndex + 8) == pBoard.StateEnpassantIndex) || (origFromSpin == helper::BLACK_PAWN_SPIN && (pToIndex - 8) == pBoard.StateEnpassantIndex))
			{
				// Do Enpassant take
				pBoard.Update(pBoard.StateEnpassantIndex, 0);
				enPassant = true;
			}


			// Castle the rook
			if ((origFromSpin == helper::WHITE_KING_SPIN && pFromIndex == 60) || (origFromSpin == helper::BLACK_KING_SPIN && pFromIndex == 4))
			{
				rookFromSqIndex = helper::CastleIndex[pToIndex][0];
				rookToSqIndex = helper::CastleIndex[pToIndex][1];

				if (rookFromSqIndex > -1 && rookToSqIndex > -1)
				{
					// Move rook
					rookSpin = pBoard.GetSpin(rookFromSqIndex);
					pBoard.Update(rookToSqIndex, rookSpin);
					pBoard.Update(rookFromSqIndex, 0);
					castle = true;
				}

			}

			// Reject the move if the king is in check after move
			if (pValidateEnabled && pBoard.IsKingCheck(pBoard.StateActiveColour))
			{
				pBoard.ReturnMessage = "Cannot make this move. King would be in check.";
				success = false;
			}


			// Reverse moves, restore board to original state if error, or commit is false
			if (!success || !pCommit)
			{
				// Reverse the move
				pBoard.Update(pToIndex, origToSpin);
				pBoard.Update(pFromIndex, origFromSpin);

				// Reverse enpassant
				if (enPassant)
				{
					pBoard.Update(origEnpassantIndex, origEnpassantSpin);
					pBoard.StateEnpassantIndex = origEnpassantIndex;
				}

				// Reverse castle
				if (castle)
				{
					pBoard.Update(rookToSqIndex, 0);
					pBoard.Update(rookFromSqIndex, rookSpin);
				}


			}
			else
			{


				// Update EnPassant potential
				int distance = pFromIndex - pToIndex;
				if ((distance == 16 || distance == -16) && (origFromSpin == helper::WHITE_PAWN_SPIN || origFromSpin == helper::BLACK_PAWN_SPIN))
				{
					pBoard.InvalidateAttackPath();
					pBoard.StateEnpassantIndex = pToIndex;
				}
				else
				{
					pBoard.StateEnpassantIndex = -1;
				}

				// Increment the full move count after blacks move
				if (pBoard.StateActiveColour == helper::BLACKPIECE)
				{
					pBoard.StateFullMoveCount += 1;
				}

				// Increment half move count if no piece was captured or a pawn was not advanced. Used for fity move rule.
				if (origToSpin != 0 || origFromSpin == helper::WHITE_PAWN_SPIN || origFromSpin == helper::BLACK_PAWN_SPIN)
				{
					pBoard.StateHalfMoveCount = 0;
				}
				else
				{
					pBoard.StateHalfMoveCount += 1;
				}

				// Change the turn, as this turn is now complete
				pBoard.StateActiveColour *= -1;


				// Update castling availability
				if (origFromSpin == helper::WHITE_ROOK_SPIN && pFromIndex == 63) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111101;
				else if (origFromSpin == helper::WHITE_ROOK_SPIN && pFromIndex == 56) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111110;
				else if (origFromSpin == helper::BLACK_ROOK_SPIN && pFromIndex == 7) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b110111;
				else if (origFromSpin == helper::BLACK_ROOK_SPIN && pFromIndex == 0) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111011;
				else if (origFromSpin == helper::WHITE_KING_SPIN) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111100;
				else if (origFromSpin == helper::BLACK_KING_SPIN) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b110011;

				if (pToIndex == 63) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111101;
				else if (pToIndex == 56) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111110;
				else if (pToIndex == 7) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b110111;
				else if (pToIndex == 0) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111011;


				// Update has castled flag
				if (castle && origFromSpin == helper::BLACK_KING_SPIN) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability | 0b100000;
				else if (castle && origFromSpin == helper::WHITE_KING_SPIN) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability | 0b010000;



				// Update game status
				if (pValidateEnabled)
				{
					// Set return message
					if (castle)
					{
						pBoard.ReturnMessage = "Castle";
					}
					else if (enPassant)
					{
						pBoard.ReturnMessage = "En passant";
					}

					if (IsCheckMate(pBoard))
					{
						pBoard.StateGameStatus = 1;
					}
					else if (IsStaleMate(pBoard))
					{
						pBoard.StateGameStatus = 2;
					}
				}

				// Set move data
				pBoard.MoveData[0] = pFromIndex;
				pBoard.MoveData[1] = pToIndex;
				pBoard.MoveData[2] = origFromSpin;
				pBoard.MoveData[3] = origToSpin;

			}


			return success;
		}


		/// <summary>
		/// Determines if colour is in check mate or stalemate
		/// </summary>
		bool IsCheckMate(BitBoard& pBoard)
		{
			bool moveFound = false;
			int posScanFrom;
			int posScanTo;
			uint64_t potentialScanTo;
			int fromIndex;
			int toIndex;

			bool isKingCheck = pBoard.IsKingCheck(pBoard.StateActiveColour);
			if (!isKingCheck) return false;

			uint64_t originalBoardArray[276];
			pBoard.GetBoardArray(originalBoardArray);

			int originalStateArray[8];
			pBoard.GetStateArray(originalStateArray);

			uint64_t occupiedScanFrom = pBoard.GetOccupied(pBoard.StateActiveColour);
			while (occupiedScanFrom > 0)
			{
				posScanFrom = helper::BitScanForward(occupiedScanFrom);
				occupiedScanFrom ^= 1uLL << posScanFrom;
				fromIndex = (63 - posScanFrom);

				potentialScanTo = pBoard.GetPotentialMove(fromIndex);
				while (potentialScanTo > 0)
				{
					posScanTo = helper::BitScanForward(potentialScanTo);
					potentialScanTo ^= 1uLL << posScanTo;
					toIndex = (63 - posScanTo);

					bool success = Move(fromIndex, toIndex, pBoard, helper::PawnPromotionEnum::Queen, false, true);
					if (success)
					{
						int previousTurn = -pBoard.StateActiveColour;
						// reject the move if the king is still in check
						if (!pBoard.IsKingCheck(previousTurn))
						{
							moveFound = true;
							// Restore original board
							pBoard.SetBoardArray(originalBoardArray);
							pBoard.SetStateArray(originalStateArray);

							break;
						}

						// Restore original board
						pBoard.SetBoardArray(originalBoardArray);
						pBoard.SetStateArray(originalStateArray);
					}

				}

				if (moveFound) break;


			}


			return !moveFound && isKingCheck;

		}

		/// <summary>
		/// Determines if board is in stalemate, helper method
		/// </summary>
		bool IsStaleMate(BitBoard& pBoard)
		{
			// Stale mate exists if only kings are left on the board
			bool onlyKingsRemaining = pBoard.OnlyKingsRemain();
			if (onlyKingsRemaining) return true;

			bool isKingCheck = pBoard.IsKingCheck(pBoard.StateActiveColour);

			// Check if valid move found
			bool moveFound = false;
			int posScanFrom;
			int posScanTo;
			uint64_t potentialScanTo;
			int fromIndex;
			int toIndex;

			uint64_t originalBoardArray[276];
			pBoard.GetBoardArray(originalBoardArray);

			int originalStateArray[8];
			pBoard.GetStateArray(originalStateArray);

			uint64_t occupiedScanFrom = pBoard.GetOccupied(pBoard.StateActiveColour);
			while (occupiedScanFrom > 0)
			{
				posScanFrom = helper::BitScanForward(occupiedScanFrom);
				occupiedScanFrom ^= 1uLL << posScanFrom;
				fromIndex = (63 - posScanFrom);

				potentialScanTo = pBoard.GetPotentialMove(fromIndex);
				while (potentialScanTo > 0)
				{
					posScanTo = helper::BitScanForward(potentialScanTo);
					potentialScanTo ^= 1uLL << posScanTo;
					toIndex = (63 - posScanTo);

					bool success = Move(fromIndex, toIndex, pBoard, helper::PawnPromotionEnum::Queen, false, true);
					if (success)
					{
						// Reject the move if the king is still in check
						if (!pBoard.IsKingCheck(-pBoard.StateActiveColour))
						{
							moveFound = true;
							// Restore original board
							pBoard.SetBoardArray(originalBoardArray);
							pBoard.SetStateArray(originalStateArray);
							break;
						}

						// Restore original board
						pBoard.SetBoardArray(originalBoardArray);
						pBoard.SetStateArray(originalStateArray);
					}
				}

				if (moveFound) break;

			}

			bool cannotMoveNotInCheck = (moveFound == false && !isKingCheck);
			bool stalemate = onlyKingsRemaining || (!onlyKingsRemaining && cannotMoveNotInCheck);

			return stalemate;

		}

		/// <summary>
		/// Used for arranging pieces on the board.
		/// </summary>
		bool Arrange(const int pFromIndex, const int pToIndex, BitBoard& pBoard)
		{
			bool status = true;

			// Exit straight away if from and to indexes are the same
			if (pFromIndex == pToIndex) return status;

			// Get original values before move
			int origFromSpin = pBoard.GetSpin(pFromIndex);
			int origToSpin = pBoard.GetSpin(pToIndex);

			// Check that the square being moved contains a piece
			if (origFromSpin == 0) {
				pBoard.ReturnMessage = helper::BoardCoordinateDict.at(pFromIndex) + " does not contain a piece to move";
				status = false;
				return status;
			}

			// Do the move
			pBoard.Update(pFromIndex, 0);
			pBoard.Update(pToIndex, origFromSpin);

			// Check that the king still exists
			int whiteKingIndex = pBoard.KingIndex<helper::WHITEPIECE>();
			int blackKingIndex = pBoard.KingIndex<helper::BLACKPIECE>();


			// Check if the king is still on the board
			if (whiteKingIndex == -1 || blackKingIndex == -1)
			{
				// If king is missing reverse move
				pBoard.Update(pToIndex, origToSpin);
				pBoard.Update(pFromIndex, origFromSpin);
				pBoard.ReturnMessage = "Invalid as King must remain on the board";

				status = false;
				return status;
			}

			// Update castling availability
			if (origFromSpin == helper::WHITE_ROOK_SPIN && pFromIndex == 63) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111101;
			else if (origFromSpin == helper::WHITE_ROOK_SPIN && pFromIndex == 56) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111110;
			else if (origFromSpin == helper::BLACK_ROOK_SPIN && pFromIndex == 7) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b110111;
			else if (origFromSpin == helper::BLACK_ROOK_SPIN && pFromIndex == 0) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111011;
			else if (origFromSpin == helper::WHITE_KING_SPIN) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111100;
			else if (origFromSpin == helper::BLACK_KING_SPIN) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b110011;

			if (pToIndex == 63) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111101;
			else if (pToIndex == 56) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111110;
			else if (pToIndex == 7) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b110111;
			else if (pToIndex == 0) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111011;

			return status;
		}

		/// <summary>
		/// Updates a piece on the board directly.
		/// </summary>
		bool ArrangeUpdate(const char pFen, const int pToIndex, BitBoard& pBoard)
		{
			bool status = true;

			// Get original values before move
			int origToSpin = pBoard.GetSpin(pToIndex);

			// Add the piece
			int spin = helper::GetSpinFromChar(pFen);

			// Exit if there is no change
			if (origToSpin == spin) {
				return status;
			}


			// Do the update
			pBoard.Update(pToIndex, spin);

			// Check that the king still exists
			int whiteKingIndex = pBoard.KingIndex<helper::WHITEPIECE>();
			int blackKingIndex = pBoard.KingIndex<helper::BLACKPIECE>();


			// Check if the king is still on the board
			if (whiteKingIndex == -1 || blackKingIndex == -1)
			{
				// If king is missing reverse move
				pBoard.Update(pToIndex, origToSpin);
				pBoard.ReturnMessage = "Invalid as King must remain on the board";

				status = false;
				return status;
			}

			// Update castling availability
			if (pToIndex == 63) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111101;
			else if (pToIndex == 56) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111110;
			else if (pToIndex == 7) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b110111;
			else if (pToIndex == 0) pBoard.StateCastlingAvailability = pBoard.StateCastlingAvailability & 0b111011;
			// Kings cannot be removed or changed to another piece so not updating castling availability for king changes here

			return status;
		}

		/// <summary>
		/// Searches for a possible from move index from the supplied spin values
		/// </summary>
		/// <returns>-2 if ambiguous move or -1 if move not found, otherwise returns move index</returns>
		int FindFromIndex(BitBoard& pBoard, const int pToIndex, const int pSpin, const std::vector<int> pValidFromIndexes)
		{
			uint64_t occupied = pBoard.GetOccupiedBySpin(pSpin);
			std::vector<int> possibleList;


			for (int fromIndex = 0; fromIndex < 64; fromIndex++)
			{
				if (((helper::BITMASK >> fromIndex) & occupied) > 0)
				{
					uint64_t potSq = pBoard.GetPotentialMove(fromIndex);
					for (int j = 0; j < 64; j++)
					{
						if (((helper::BITMASK >> j) & potSq) > 0 && j == pToIndex)
						{
							// Test the move before adding
							if (Move(fromIndex, pToIndex, pBoard, helper::PawnPromotionEnum::Queen, true, false))
							{
								possibleList.push_back(fromIndex);
								break;
							}
						}
					}
				}
			}


			// Filter out anything not in valid list
			int rtnfromIndex = -1;
			if (pValidFromIndexes.size() > 0)
			{
				for (int const& possibleFromIndex : possibleList) {

					if (std::count(pValidFromIndexes.begin(), pValidFromIndexes.end(), possibleFromIndex))
					{
						if (rtnfromIndex != -1) return -2;
						rtnfromIndex = possibleFromIndex;
					}
				}
			}
			else
			{
				// If no valid index array supplied then there should only be one possibility found.
				if (possibleList.size() == 1)
				{
					rtnfromIndex = possibleList[0];
				}
				else if (possibleList.size() == 0)
				{
					return -1;
				}
				else
				{
					return -2;
				}
			}

			return rtnfromIndex;
		}


		/// <summary>
		/// Checks if the move is a pawn promoting move
		/// </summary>
		bool IsPawnPromotion(const int pFromIndex, const int pToIndex, BitBoard& pBoard)
		{
			int origFromSpin = pBoard.GetSpin(pFromIndex);
			// Check for pawn promotion
			if ((origFromSpin == helper::WHITE_PAWN_SPIN && pToIndex >= 0 && pToIndex <= 7) ||
				(origFromSpin == helper::BLACK_PAWN_SPIN && pToIndex >= 56 && pToIndex <= 63))
			{
				// test the move but don't commit it
				return Move(pFromIndex, pToIndex, pBoard, helper::PawnPromotionEnum::Queen, true, false);
			}

			return false;
		}

	}

}