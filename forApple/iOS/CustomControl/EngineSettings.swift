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

import SwiftUI

struct EngineSettings: View {
    @Binding var showMenu: Bool
    @ObservedObject private var engineSettingsVM : EngineSettingsViewModel = EngineSettingsViewModel.instance
    @FocusState private var limitMoveDurationIsFocused : Bool
    
    private var maxThreads: Double = ProcessInfo.processInfo.activeProcessorCount > 1 ? Double(ProcessInfo.processInfo.activeProcessorCount) : Double(1)
    
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
            
            Toggle(isOn: $engineSettingsVM.computerPlayerEnabled) {
                Text("Computer player enabled")
            }
                
            Group {
                
                Section {
                    Toggle(isOn: $engineSettingsVM.computerMovesFirst) {
                        Text("Computer moves first")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    
                    Toggle(isOn: $engineSettingsVM.randomiseFirstMove) {
                        Text("Randomise first computer move")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    
                    Toggle(isOn: $engineSettingsVM.levelAuto) {
                        Text("Increase strength after win")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    
                    Picker(selection: $engineSettingsVM.limitSkillLevel, label: Text("Strength").font(.body).opacity(!$engineSettingsVM.computerPlayerEnabled.wrappedValue ? 0.5 : 1)) {
                        ForEach(0 ..< Constants.strengthList.count, id: \.self) {
                            Text(Constants.strengthList[$0].label).tag($0)
                        }
                    }
                    .pickerStyle(DefaultPickerStyle())
                }
    
                Section {
                    Toggle(isOn: $engineSettingsVM.limitAdvanced) {
                        Text("Advanced search settings")
                            .opacity(getComputerPlayerEnabledOpacity())
                    }
                    .padding(.top)
                    
                    Group {
                        HStack {
                            Text("Depth limit \(getValueZeroOff(pValue: Int($engineSettingsVM.limitDepth.wrappedValue)))")
                            
                            Slider(value: $engineSettingsVM.limitDepth, in: 0...35, step:1) {
                                EmptyView()
                            }
                        }
                        .opacity(getAdvancedSettingsOpacity())
                        
                        
                        HStack(alignment: .top) {
                            Text("Move time limit (ms)")
                            TextField("", value: $engineSettingsVM.limitMoveDuration, formatter: NumberFormatter())
                                .keyboardType(.numberPad)
                                .focused($limitMoveDurationIsFocused)
                                .help(Text("Valid values are 0 to \(formatter.string(from: 600000) ?? ""). Set to 0 for no limit."))
                                .frame(width: 170)
                            
                        }.opacity(getAdvancedSettingsOpacity())
                        
                        if !$engineSettingsVM.limitMoveDurationIsValid.wrappedValue {
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
                                Text("CPU threads: \(getValueZeroOff(pValue: Int($engineSettingsVM.limitThreads.wrappedValue)))")
                                    
                                
                                Slider(value: $engineSettingsVM.limitThreads, in: 1...maxThreads, step:1) {
                                    EmptyView()
                                }
                            }.opacity(getAdvancedSettingsOpacity())
                        }
                        
                    }.disabled(!$engineSettingsVM.limitAdvanced.wrappedValue)
                }  // end section
                
            }.disabled(!$engineSettingsVM.computerPlayerEnabled.wrappedValue)
            
            Button(action: {
                BoardViewModel.instance.stopSearchJob()
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
                limitMoveDurationIsFocused = false
                engineSettingsVM.resetToDefault()
            }){
                Label() {
                    Text("Reset to default")
                } icon: {
                    Image(systemName: "restart.circle")
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
        return !($engineSettingsVM.limitAdvanced.wrappedValue && $engineSettingsVM.computerPlayerEnabled.wrappedValue) ? 0.5 : 1
    }
    
    private func getComputerPlayerEnabledOpacity() -> Double {
        return !($engineSettingsVM.computerPlayerEnabled.wrappedValue && $engineSettingsVM.computerPlayerEnabled.wrappedValue) ? 0.5 : 1
    }
    
    
    
}

