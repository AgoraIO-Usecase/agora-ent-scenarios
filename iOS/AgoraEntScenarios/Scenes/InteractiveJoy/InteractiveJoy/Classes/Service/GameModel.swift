//
//  GameModel.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import Foundation

class GameModel {
    var gameSection: String
    var games: [Game]
    
    init(gameSection: String, games: [Game]) {
        self.gameSection = gameSection
        self.games = games
    }
}

class Game {
    var gameId: Int64
    var gameName: String
    var gamePic: String
    var gameUrl: String
    init(gameId: Int64, gameName: String, gamePic: String, gameUrl: String) {
        self.gameId = gameId
        self.gameName = gameName
        self.gamePic = gamePic
        self.gameUrl = gameUrl
    }
    
    convenience init(gameName: String, gamePic: String, gameUrl: String) {
        self.init(gameId: 0, gameName: gameName, gamePic: gamePic, gameUrl: gameUrl)
    }
    
    convenience init(gameId: Int64, gameName: String, gamePic: String) {
        self.init(gameId: gameId, gameName: gameName, gamePic: gamePic, gameUrl: "")
    }
}

