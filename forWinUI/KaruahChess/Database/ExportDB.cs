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
using Microsoft.Data.Sqlite;
using System.IO;
using System.Xml;
using System.IO.Compression;

namespace KaruahChess.Database
{
    public static class ExportDB
    {


        public enum ExportType { GameXML };

        /// <summary>
        /// Export funciton
        /// </summary>
        /// <param name="pExportType"></param>
        public static async Task<StorageFile> Export(ExportType pExportType)
        {
            StorageFile file = null;

            switch (pExportType)
            {      
                case ExportType.GameXML:


                    // Create memory stream and xml writer
                    MemoryStream memoryStream = new MemoryStream();
                    XmlTextWriter xmlWriter = new XmlTextWriter(memoryStream, System.Text.Encoding.UTF8);
                    xmlWriter.Formatting = Formatting.Indented;

                    // Start the xml document
                    xmlWriter.WriteStartDocument(true);
                    xmlWriter.WriteStartElement("Records");

                    // Write the records
                    using (var connection = KaruahChessDB.GetDBConnection())
                    {
                        string exportFileName = "KaruahChessGame.gz";
                                                                        
                        using (var command = connection.CreateCommand())
                        {
                            command.CommandText = $"select * from {KaruahChessDB.GameRecordTableName} order by id";
                            SqliteDataReader reader = command.ExecuteReader();

                            while (reader.Read())
                            {
                                // Start the GameRecord element.
                                xmlWriter.WriteStartElement("GameRecord");

                                // Write the Id
                                xmlWriter.WriteStartElement("Id");
                                xmlWriter.WriteString(Convert.ToString(reader["Id"]));
                                xmlWriter.WriteEndElement();

                                // Write the BoardSquareStr
                                xmlWriter.WriteStartElement("BoardSquareStr");
                                xmlWriter.WriteString(Convert.ToString(reader["BoardSquareStr"]));
                                xmlWriter.WriteEndElement();

                                // Write the GameStateStr
                                xmlWriter.WriteStartElement("GameStateStr");
                                xmlWriter.WriteString(Convert.ToString(reader["GameStateStr"]));
                                xmlWriter.WriteEndElement();

                                xmlWriter.WriteEndElement();  // GameRecord

                            }

                        }
                                                
                        // End the records node 
                        xmlWriter.WriteEndElement();   // Records

                        // End the document.
                        xmlWriter.WriteEndDocument();
                        xmlWriter.Flush();

                        
                        // Write the bytes out to a file
                        byte[] dataBuffer = memoryStream.ToArray();                                                
                        file = await WriteFile(exportFileName, dataBuffer);

                        // Close the connection
                        connection.Close();
                    }

                    break;

                default:
                    break;
            }

            return file;

        }

        /// <summary>
        /// Writes bytes data to a file in the local application data folder
        /// </summary>
        /// <param name="pFileName">Filename to write to</param>
        /// <param name="pData">The data to write</param>
        /// <returns></returns>
        private static async Task<StorageFile> WriteFile(string pFileName, byte[] pData)
        {

            //Compress data
            var compressedStream = new MemoryStream();
            using (GZipStream gzip = new GZipStream(compressedStream, CompressionMode.Compress))
            {
                gzip.Write(pData, 0, pData.Length);
            }


            // Write to file
            StorageFolder storageFolder = ApplicationData.Current.LocalFolder;
            StorageFile exportFile = await storageFolder.CreateFileAsync(pFileName, CreationCollisionOption.ReplaceExisting);

            var stream = await exportFile.OpenAsync(FileAccessMode.ReadWrite);
            using (var outputStream = stream.GetOutputStreamAt(0))
            {
                
                using (var dataWriter = new Windows.Storage.Streams.DataWriter(outputStream))
                {
                    dataWriter.UnicodeEncoding = Windows.Storage.Streams.UnicodeEncoding.Utf16LE;                    
                    dataWriter.WriteBytes(compressedStream.ToArray());

                    await dataWriter.StoreAsync();
                    await outputStream.FlushAsync();
                }
            }
            stream.Dispose();

            return exportFile;
        }




       
    }
}
