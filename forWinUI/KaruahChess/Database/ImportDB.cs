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


using System;
using System.Threading.Tasks;
using Windows.Storage;
using System.IO;
using KaruahChess.Model;
using System.Xml.Linq;
using Microsoft.Data.Sqlite;
using System.IO.Compression;

namespace KaruahChess.Database
{
    public static class ImportDB
    {
        
        public enum ImportType { GameXML };

        /// <summary>
        /// Import funciton
        /// </summary>
        /// <param name="pImportType"></param>
        public static async Task<int> Import(StorageFile pfile, ImportType pImportType)
        {
            int rec = 0;

            switch (pImportType)
            {
               case ImportType.GameXML:

                    using (var connection = KaruahChessDB.GetDBConnection())
                    {
                        XDocument dataXML = await ReadFile(pfile);
                        var records = dataXML.Descendants("GameRecord");
                        
                        using (var tran = connection.BeginTransaction())
                        {
                            try
                            {
                 
                                using (var command = connection.CreateCommand())
                                {
                                    command.CommandText = $"Delete from {KaruahChessDB.GameRecordTableName};";
                                    command.ExecuteNonQuery();
                                }

                                foreach (var gr in records)
                                {
                                    using (var command = connection.CreateCommand())
                                    {
                                        command.CommandText = $"INSERT INTO {KaruahChessDB.GameRecordTableName} (Id, BoardSquareStr, GameStateStr) Values (@Id, @BoardSquareStr, @GameStateStr);";
                                        command.Parameters.Add(new SqliteParameter("@Id", gr.Element("Id").Value));
                                        command.Parameters.Add(new SqliteParameter("@BoardSquareStr", gr.Element("BoardSquareStr").Value));
                                        command.Parameters.Add(new SqliteParameter("@GameStateStr", gr.Element("GameStateStr").Value));                                        
                                        command.ExecuteNonQuery();
                                    }

                                }


                                tran.Commit();
                            }
                            catch (Exception ex)
                            {
                                tran.Rollback();
                                throw ex;
                            }
                        }

                        GameRecordDataService.instance.Load();
                    }

                    break;

            }

            return rec;

        }


        /// <summary>
        /// Reads a file
        /// </summary>
        /// <param name="pFile">Filename to read</param>        
        /// <returns></returns>
        private static async Task<XDocument> ReadFile(StorageFile pFile)
        {
            XDocument xml = null;

            using (Stream inStream = await pFile.OpenStreamForReadAsync())
            {
                // Uncompress the data if compressed
                Byte[] headerBytes = new byte[2];
                inStream.Read(headerBytes, 0, 2);
                inStream.Position = 0;

                var uncompressedStream = new MemoryStream();
                if(IsGZipped(headerBytes)) {
                    using (GZipStream gzip = new GZipStream(inStream, CompressionMode.Decompress))  {
                        gzip.CopyTo(uncompressedStream);                        
                    }
                }
                else {                    
                   inStream.CopyTo(uncompressedStream);
                }

                uncompressedStream.Position = 0;
                xml = XDocument.Load(uncompressedStream);                
            }


            return xml;

        }


        /// <summary>
        /// Checks if data is compressed
        /// </summary>
        private static bool IsGZipped(byte[] pHeaderBytes) {
            
            if (pHeaderBytes == null || pHeaderBytes.Length < 2) {
                return false;
            }
            else {
                return pHeaderBytes[0] == 0x1f && pHeaderBytes[1] == 0x8b;                
            }
        }

    }
}
