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

#import <Foundation/Foundation.h>
#import "CompressionC.h"
#import <zlib.h>





@implementation CompressionC



/// Turn the data in to gzipped compressed data
- (NSData * _Nonnull) gZip:(const NSData * _Nonnull) pData {
    if (pData.length == 0 || [self isGZip:pData]) {
        return [[NSData alloc]init];
    }
    
    z_stream datain;
    datain.zalloc = Z_NULL;
    datain.zfree = Z_NULL;
    datain.opaque = Z_NULL;
    datain.avail_in = (uint)pData.length;
    datain.next_in = (Bytef *)(void *)pData.bytes;
    datain.total_out = 0;
    datain.avail_out = 0;
    
    static const NSUInteger chunksize = 16384;
    NSMutableData *dataout = nil;
    
    if (deflateInit2(&datain, Z_DEFAULT_COMPRESSION, Z_DEFLATED, 31, 8, Z_DEFAULT_STRATEGY) == Z_OK) {
        dataout = [NSMutableData dataWithLength:chunksize];
        while (datain.avail_out == 0) {
            if (datain.total_out >= dataout.length) {
                dataout.length += chunksize;
            }
            datain.next_out = (uint8_t *)dataout.mutableBytes + datain.total_out;
            datain.avail_out = (uInt)(dataout.length - datain.total_out);
            deflate(&datain, Z_FINISH);
        }
        deflateEnd(&datain);
        dataout.length = datain.total_out;
    }
    
    return dataout;
    
}

/// Unzip the compressed data
- (NSData * _Nonnull) gUnZip:(const NSData * _Nonnull) pData {
    if (pData.length == 0 || ![self isGZip:pData]) {
        return [[NSData alloc]init];
    }
    
    z_stream datain;
    datain.zalloc = Z_NULL;
    datain.zfree = Z_NULL;
    datain.avail_in = (uint)pData.length;
    datain.next_in = (Bytef *)pData.bytes;
    datain.total_out = 0;
    datain.avail_out = 0;
    
    NSMutableData *dataout = nil;
    if (inflateInit2(&datain, 47) == Z_OK) {
        int status = Z_OK;
        dataout = [NSMutableData dataWithCapacity:pData.length * 2];
        while (status == Z_OK) {
            if (datain.total_out >= dataout.length) {
                dataout.length += pData.length / 2;
            }
            datain.next_out = (uint8_t *)dataout.mutableBytes + datain.total_out;
            datain.avail_out = (uInt)(dataout.length - datain.total_out);
            status = inflate(&datain, Z_SYNC_FLUSH);
        }
        if (inflateEnd(&datain) == Z_OK) {
            if (status == Z_STREAM_END) {
                dataout.length = datain.total_out;
            }
        }
    }
    
    return dataout;
    
}

/// Test if the data is a GZip file
- (bool) isGZip:(const NSData * _Nonnull) pData {
    const UInt8 *bytes = (const UInt8 *)pData.bytes;
    return pData.length >= 2 && bytes[0] == 0x1f && bytes[1] == 0x8b;
    
}


@end
