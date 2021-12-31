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

import SwiftUI

struct EngineSettings: View {
    @Binding var showMenu: Bool
    @ObservedObject private var engineSettingsVM : EngineSettingsViewModel = EngineSettingsViewModel.instance
    @FocusState private var limitMoveDurationIsFocused : Bool
    @FocusState private var limitNodesIsFocused : Bool
    
    private var maxThreads: Double = ProcessInfo.processInfo.activeProcessorCount > 1 ? Double(ProcessInfo.processInfo.activeProcessorCount - 1) : Double(1)
    
    let formatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        return formatter
    }()
    
    // Initialisation
    init(pShowMenu: Binding<Bool>) {
        _showMenu = pShowMenu
    }
    
    var body: some View {
        Form {
            
            Toggle(isOn: $engineSettingsVM.value.computerPlayerEnabled) {
                Text("Computer player enabled")
            }
                
            Group {
                
                Section {
                    Toggle(isOn: $engineSettingsVM.value.computerMovesFirst) {
                        Text("Computer moves first")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    
                    Toggle(isOn: $engineSettingsVM.value.randomiseFirstMove) {
                        Text("Randomise first computer move")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    
                    Toggle(isOn: $engineSettingsVM.value.levelAuto) {
                        Text("Increase strength after win")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    
                    Picker(selection: $engineSettingsVM.value.limitEngineStrengthELOIndex, label: Text("Computer Strength").opacity(!$engineSettingsVM.value.computerPlayerEnabled.wrappedValue ? 0.5 : 1)) {
                        ForEach(0 ..< Constants.strengthArrayLabel.count) {
                            Text(Constants.strengthArrayLabel[$0])
                        }
                    }
                    .pickerStyle(DefaultPickerStyle())
                }
    
                Section {
                    Toggle(isOn: $engineSettingsVM.value.limitAdvanced) {
                        Text("Advanced search settings")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    .padding(.top)
                    
                    Group {
                        HStack {
                            Text("Depth limit \(getValueZeroOff(pValue: Int($engineSettingsVM.value.limitDepth.wrappedValue)))")
                            
                            Slider(value: $engineSettingsVM.value.limitDepth, in: 0...35, step:1) {
                                EmptyView()
                            }
                        }
                        .opacity(getAdvancedSettingsOpacity())
                        
                        
                        HStack(alignment: .top) {
                            Text("Node limit")
                            TextField("", value: $engineSettingsVM.value.limitNodes, formatter: NumberFormatter())
                                .keyboardType(.numberPad)
                                .focused($limitNodesIsFocused)
                                .help(Text("Valid values are 10 to \(formatter.string(from: 2000000000) ?? "")"))
                                .frame(width: 170)
                        }.opacity(getAdvancedSettingsOpacity())
                        
                        
                        if !$engineSettingsVM.value.limitNodesIsValid.wrappedValue {
                            HStack {
                                Image(systemName: "arrow.turn.left.up")
                                .imageScale(.large)
                                .foregroundColor(Color.red)
                                .padding(.leading)
                                
                                Text("Valid values are 10 to \(formatter.string(from: 2000000000) ?? "") ")
                                .foregroundColor(Color.red)
                                .padding(0)
                            }.opacity(getAdvancedSettingsOpacity())
                        }
                        
                        HStack(alignment: .top) {
                            Text("Move time limit (ms)")
                            TextField("", value: $engineSettingsVM.value.limitMoveDuration, formatter: NumberFormatter())
                                .keyboardType(.numberPad)
                                .focused($limitMoveDurationIsFocused)
                                .help(Text("Valid values are 0 to \(formatter.string(from: 600000) ?? ""). Set to 0 for no limit."))
                                .frame(width: 170)
                            
                        }.opacity(getAdvancedSettingsOpacity())
                        
                        if !$engineSettingsVM.value.limitMoveDurationIsValid.wrappedValue {
                            HStack {
                                Image(systemName: "arrow.turn.left.up")
                                .imageScale(.large)
                                .foregroundColor(Color.red)
                                .padding(.leading)
                                
                                Text("Valid values are 0 to \(formatter.string(from: 600000) ?? "") ")
                                .foregroundColor(Color.red)
                                .padding(0)
                                
                            }.opacity(getAdvancedSettingsOpacity())
                        }
                        
                        if maxThreads > 1 {
                            HStack {
                                Text("CPU threads: \(getValueZeroOff(pValue: Int($engineSettingsVM.value.limitThreads.wrappedValue)))")
                                    
                                
                                Slider(value: $engineSettingsVM.value.limitThreads, in: 1...maxThreads, step:1) {
                                    EmptyView()
                                }
                            }.opacity(getAdvancedSettingsOpacity())
                        }
                        
                    }.disabled(!$engineSettingsVM.value.limitAdvanced.wrappedValue)
                }  // end section
                
            }.disabled(!$engineSettingsVM.value.computerPlayerEnabled.wrappedValue)
            
            Button(action: {
                Task(priority: .userInitiated) {
                    await BoardViewModel.instance.endMoveJob()
                }
                
            }){
                Label() {
                    Text("Stop Search")
                } icon: {
                    Image(systemName: "stop.fill")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .font(Font.system(.headline))
                        .padding(8)
                        .frame(width: 28, height: 28)
                        .background(Color.red)
                        .foregroundColor(Color.white)
                        .cornerRadius(6)
                }
            }
            
            Button(action: {
                limitNodesIsFocused = false
                limitMoveDurationIsFocused = false
                engineSettingsVM.resetToDefault()
            }){
                Label() {
                    Text("Reset to default")
                } icon: {
                    Image(systemName: "wind")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .font(Font.system(.headline))
                        .padding(3)
                        .frame(width: 28, height: 28)
                        .background(Color.gray)
                        .foregroundColor(Color.white)
                        .cornerRadius(6)
                }
            }
            
            Spacer()
                .frame(maxWidth: .infinity)
                    
                
            }
            .padding(0)
            .navigationBarTitle(Text("Engine Settings"), displayMode: .inline)
            .navigationBarItems(leading: ActivityIndicatorView(activityIndicatorVM: BoardViewModel.instance.activityIndicatorVM).frame(width:35, height:35), trailing: Button("Close") {
                self.showMenu = false
            })
            
    
        
    }
    
    
    
    private func getValueZeroOff(pValue: Int) -> String {
        return pValue > 0 ? String(pValue) : "off"
    }
    
    private func getAdvancedSettingsOpacity() -> Double {
        return !($engineSettingsVM.value.limitAdvanced.wrappedValue && $engineSettingsVM.value.computerPlayerEnabled.wrappedValue) ? 0.5 : 1
    }
    
    private func getComputerPlayerEnabledOpacity() -> Double {
        return !($engineSettingsVM.value.computerPlayerEnabled.wrappedValue && $engineSettingsVM.value.computerPlayerEnabled.wrappedValue) ? 0.5 : 1
    }
    
    
    
}

