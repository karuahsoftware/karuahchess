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
    
    @ObservedObject private var engineSettingsVM : EngineSettingsViewModel = EngineSettingsViewModel.instance
    private let menuSheet : MenuSheet
    @FocusState private var limitMoveDurationIsFocused : Bool
    
    private var maxThreads: Double = ProcessInfo.processInfo.activeProcessorCount > 1 ? Double(ProcessInfo.processInfo.activeProcessorCount) : Double(1)
    
    let formatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        return formatter
    }()
    
    // Initialisation
    init(pMenuSheet: MenuSheet) {
        menuSheet = pMenuSheet
    }
    
    var body: some View {
        VStack(alignment: .leading) {
            ScrollView([.vertical]) {
                VStack(alignment: .leading) {
                    HStack {
                        Image(systemName: "gear")
                        .imageScale(.large)
                        Text("Engine Settings").font(.headline)
                        ActivityIndicatorView(activityIndicatorVM: BoardViewModel.instance.activityIndicatorVM).frame(width:35, height:35)
                    }
                    
                    Toggle(isOn: $engineSettingsVM.computerPlayerEnabled) {
                        Text("Computer player enabled")
                            .font(.body)
                    }.padding(.bottom, 7)
                        
                    Group {
                        
                        Picker(selection: $engineSettingsVM.limitSkillLevel, label: Text("Computer strength").font(.body).opacity(!$engineSettingsVM.computerPlayerEnabled.wrappedValue ? 0.5 : 1)) {
                            ForEach(0 ..< Constants.strengthList.count, id: \.self) {
                                Text(Constants.strengthList[$0].label).tag($0)
                            }
                        }
                        .pickerStyle(DefaultPickerStyle())
                        .fixedSize(horizontal: true, vertical: true)
                        
                        Toggle(isOn: $engineSettingsVM.levelAuto) {
                            Text("Auto increase strength")
                                .font(.body)
                        }.padding(.bottom)
                                  
                        
                        Button(action: {
                            engineSettingsVM.computerMovesFirstToggle()
                               }){
                                   VStack(spacing: 0) {
                                       Text("Computer colour").font(.body)
                                       if $engineSettingsVM.computerMovesFirst.wrappedValue {
                                           Image("WhitePawnLarge")
                                               .resizable()
                                               .aspectRatio(contentMode: .fit)
                                               .frame(width:60, height: 60)
                                               .padding(.bottom)
                                               .opacity(!$engineSettingsVM.computerPlayerEnabled.wrappedValue ? 0.5 : 1)
                                       }
                                       else {
                                           Image("BlackPawnLarge")
                                               .resizable()
                                               .aspectRatio(contentMode: .fit)
                                               .frame(width:60, height: 60)
                                               .padding(.bottom)
                                               .opacity(!$engineSettingsVM.computerPlayerEnabled.wrappedValue ? 0.5 : 1)
                                       }
                                   }
                                      
                               }.padding(.bottom)
                        
                        Toggle(isOn: $engineSettingsVM.randomiseFirstMove) {
                            Text("Make the first computer move more random")
                                .font(.body)
                        }
                                                
                        
                        Toggle(isOn: $engineSettingsVM.limitAdvanced) {
                            Text("Advanced search settings")
                                .font(.body)
                        }
                        .padding(.top)
                        
                        Group {
                            Slider(value: $engineSettingsVM.limitDepth, in: 0...35, step:1) {
                                Text("Depth limit \(getValueZeroOff(pValue: Int($engineSettingsVM.limitDepth.wrappedValue)))")
                                    .font(.body)
                                    .opacity(getAdvancedSettingsOpacity())
                            }
                            
                            
                            HStack(alignment: .top) {
                                Text("Move time limit (ms)").opacity(getAdvancedSettingsOpacity())
                                TextField("", value: $engineSettingsVM.limitMoveDuration, formatter: NumberFormatter())
                                    .focused($limitMoveDurationIsFocused)
                                    .help(Text("Valid values are 0 to \(formatter.string(from: 600000) ?? ""). Set to 0 for no limit."))
                                    .frame(width: 100)
                                
                            }
                            
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
                                Slider(value: $engineSettingsVM.limitThreads, in: 1...maxThreads, step:1) {
                                    Text("CPU threads \(getValueZeroOff(pValue: Int($engineSettingsVM.limitThreads.wrappedValue)))")
                                        .font(.body)
                                        .opacity(getAdvancedSettingsOpacity())
                                }
                            }
                            
                        }.disabled(!$engineSettingsVM.limitAdvanced.wrappedValue)
                        
                    }.disabled(!$engineSettingsVM.computerPlayerEnabled.wrappedValue)
                    
                }.frame(minWidth: 50, maxWidth: .infinity, minHeight: 50, maxHeight: .infinity)
                
            } // Scrollview
            
            Divider().padding(.top)
            
            HStack {
                Button(action: {
                    menuSheet.active = nil
                }){
                    Text("Close")
                }
                
                Button(action: {
                    BoardViewModel.instance.stopSearchJob()
                }){
                    Text("Stop search")
                }.buttonStyle(.bordered)
                
                Button(action: {
                    limitMoveDurationIsFocused = false
                    engineSettingsVM.resetToDefault()
                }){
                    Text("Reset to default")
                }.buttonStyle(.bordered)
            }
               
        }
        
    }
    
    
    private func getValueZeroOff(pValue: Int) -> String {
        return pValue > 0 ? String(pValue) : "off"
    }
    
    private func getAdvancedSettingsOpacity() -> Double {
        return !($engineSettingsVM.limitAdvanced.wrappedValue && $engineSettingsVM.computerPlayerEnabled.wrappedValue) ? 0.5 : 1
    }
    
    
    
}

