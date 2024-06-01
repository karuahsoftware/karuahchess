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

#pragma once
#import <Foundation/Foundation.h>


@interface CompressionC : NSObject

- (NSData * _Nonnull) gZip:(const NSData * _Nonnull) pData;
- (NSData * _Nonnull) gUnZip:(const NSData * _Nonnull) pData;
- (bool) isGZip:(const NSData * _Nonnull) pData;


@end


