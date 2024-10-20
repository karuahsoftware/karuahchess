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
using System.Collections.Generic;
using KaruahChess.Database;
using KaruahChess.Model.ParameterObjects;
using KaruahChess.Common;
using Microsoft.Data.Sqlite;

namespace KaruahChess.Model
{
    public class ParameterDataService : IParameterDataService
    {

        /// <summary>
        /// List of parameters
        /// </summary>
        private Dictionary<String, Object> _parameters;
        

        // Constructor
        private ParameterDataService()
        {
           
            _parameters = new Dictionary<String, Object>();
            Load();
        }

        /// <summary>
        /// Singleton accessor
        /// </summary>
        public static ParameterDataService instance { get; private set; } = new ParameterDataService();

        /// <summary>
        /// Loads parameters
        /// </summary>
        public void Load()
        {
            
            List<Parameter> ParamList = new List<Parameter>();

            var connection = KaruahChessDB.GetDBConnection();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = $"select * from {KaruahChessDB.ParameterTableName}";
                SqliteDataReader reader = command.ExecuteReader();

                while (reader.Read())
                {
                    Parameter param = new Parameter();
                    param.Name = Convert.ToString(reader["Name"]);
                    param.Value = (byte [])reader["Value"];
                    ParamList.Add(param);
                }
            }

            connection.Close();


            _parameters.Clear();
            foreach (Parameter param in ParamList)
            {
                if (param.Name == typeof(ParamComputerPlayer).Name)                   
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamComputerPlayer>());
                }
                else if (param.Name == typeof(ParamComputerMoveFirst).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamComputerMoveFirst>());
                }
                else if (param.Name == typeof(ParamRandomiseFirstMove).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamRandomiseFirstMove>());
                }
                else if (param.Name == typeof(ParamArrangeBoard).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamArrangeBoard>());
                }               
                else if (param.Name == typeof(ParamRotateBoard).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamRotateBoard>());
                }                
                else if (param.Name == typeof(ParamBoardCoord).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamBoardCoord>());
                }
                else if (param.Name == typeof(ParamMoveHighlight).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamMoveHighlight>());
                }
                else if (param.Name == typeof(ParamSoundRead).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamSoundRead>());
                }
                else if (param.Name == typeof(ParamSoundEffect).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamSoundEffect>());
                }
                else if (param.Name == typeof(ParamClock).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamClock>());
                }
                else if (param.Name == typeof(ParamClockDefault).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamClockDefault>());
                }
                else if (param.Name == typeof(ParamNavigator).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamNavigator>());
                }
                else if (param.Name == typeof(ParamLimitSkillLevel).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamLimitSkillLevel>());
                }
                else if (param.Name == typeof(ParamLimitAdvanced).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamLimitAdvanced>());
                }
                else if (param.Name == typeof(ParamLimitDepth).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamLimitDepth>());
                }
                else if (param.Name == typeof(ParamLimitMoveDuration).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamLimitMoveDuration>());
                }               
                else if (param.Name == typeof(ParamLimitThreads).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamLimitThreads>());
                }
                else if (param.Name == typeof(ParamLevelAuto).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamLevelAuto>());
                }
                else if (param.Name == typeof(ParamColourDarkSquares).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamColourDarkSquares>());
                }
                else if (param.Name == typeof(ParamMoveSpeed).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamMoveSpeed>());
                }
                else if (param.Name == typeof(ParamPromoteAuto).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamPromoteAuto>());
                }
                else if (param.Name == typeof(ParamHint).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamHint>());
                }
                else if (param.Name == typeof(ParamHintMove).Name)
                {
                    _parameters.Add(param.Name, param.Value.Deserialize<ParamHintMove>());
                }
            }


        }


        /// <summary>
        /// Gets a parameter
        /// </summary>
        /// <returns></returns>
        public T Get<T>() where T : class, new()
        {
            T param;

            if (_parameters.ContainsKey(typeof(T).Name))
            {
                param = (T)_parameters[typeof(T).Name];
            }
            else
            {
                // Sets the new parameter and puts it in the database
                param = new T();
                Set<T>(param);
            }

            return param;
        }


        /// <summary>
        /// Sets a parameter
        /// </summary>
        /// <returns></returns>
        public int Set<T>(T pObj) where T : class, new()
        {
            var param = new Parameter();
            param.Name = typeof(T).Name;
            param.Value = pObj.Serialize();

            var result = UpdateOrAdd(param, pObj, false);

            return result;            
           
        }


        /// <summary>
        /// Updates the record if it exists. Otherwise adds a new record.
        /// </summary>
        /// <param name="p_parameter">The serialized parameter to write to the database</param>
        /// <param name="pObj">The object being updated</param>
        /// <param name="pReload">Reloads all instances from the DB</param>
        /// <returns></returns>
        private int UpdateOrAdd(Parameter p_parameter, Object pObj, bool pReload)
        {
            int result = 0;

            if (p_parameter != null)
            {
                var connection = KaruahChessDB.GetDBConnection();

                // Attempt to update record               
                using (var command = connection.CreateCommand())
                {
                    command.CommandText = $"UPDATE {KaruahChessDB.ParameterTableName} SET Value=@Value where Name=@Name;";
                    command.Parameters.Add(new SqliteParameter("@Name", p_parameter.Name));
                    command.Parameters.Add(new SqliteParameter("@Value", p_parameter.Value));
                    result = command.ExecuteNonQuery();
                }

                //If there was no update then insert a new record
                if (result == 0)
                {
                    using (var command = connection.CreateCommand())
                    {
                        command.CommandText = $"INSERT INTO {KaruahChessDB.ParameterTableName} (Name, Value) Values (@Name, @Value);";
                        command.Parameters.Add(new SqliteParameter("@Name", p_parameter.Name));
                        command.Parameters.Add(new SqliteParameter("@Value", p_parameter.Value));
                        result = command.ExecuteNonQuery();
                    }

                }

                connection.Close();

            }

            if (result > 0)
            {
                if (pReload)
                {
                    // Reload all from db
                    Load();
                }
                else
                {
                    // Just update the affected list item
                    if (_parameters.ContainsKey(p_parameter.Name))
                    {
                        _parameters[p_parameter.Name] = pObj;
                    }
                    else
                    {
                        _parameters.Add(p_parameter.Name, pObj);
                    }

                }


            }

            return result;
        }


        

    }
}
