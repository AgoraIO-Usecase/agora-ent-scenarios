/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.decorator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;

/**
 * 游戏回调数据缓存
 * Game callback data cache.
 */
public class SudFSMMGCache {

    /**
     * 记录当前队长的用户id
     * Record the user ID of the current team captain.
     */
    private String captainUserId;

    /**
     * 全局游戏状态
     * Global game state.
     */
    private SudMGPMGState.MGCommonGameState mgCommonGameStateModel;

    /**
     * 是否数字炸弹
     * Is it a numeric bomb?
     */
    private boolean isHitBomb = false;

    /**
     * 记录已经加入了游戏的玩家
     * Record the players who have already joined the game.
     */
    private final HashSet<String> playerInSet = new HashSet<>();

    /**
     * 记录已经准备好的游戏玩家
     * Keep track of the game players who are already ready.
     */
    private final HashSet<String> playerReadySet = new HashSet<>();

    /**
     * 记录玩家的游戏状态
     * Record the game state of the player.
     */
    private final HashMap<String, SudMGPMGState.MGCommonPlayerPlaying> playerPlayingMap = new HashMap<>();

    /**
     * 队长状态 处理
     * Captain's status handling.
     */
    public void onPlayerMGCommonPlayerCaptain(String userId, SudMGPMGState.MGCommonPlayerCaptain model) {
        if (model != null) {
            if (model.isCaptain) {
                captainUserId = userId;
            } else {
                if (Objects.equals(captainUserId, userId)) {
                    captainUserId = null;
                }
            }
        }
    }

    /**
     * 游戏状态 处理
     * Game state processing.
     */
    public void onGameMGCommonGameState(SudMGPMGState.MGCommonGameState model) {
        mgCommonGameStateModel = model;
    }

    /**
     * 玩家加入状态处理
     * Player join status processing.
     */
    public void onPlayerMGCommonPlayerIn(String userId, SudMGPMGState.MGCommonPlayerIn model) {
        if (model != null) {
            if (model.isIn) {
                playerInSet.add(userId);
            } else {
                playerInSet.remove(userId);
                playerReadySet.remove(userId);
            }
        }
    }

    /**
     * 玩家准备状态
     * Player readiness status.
     */
    public void onPlayerMGCommonPlayerReady(String userId, SudMGPMGState.MGCommonPlayerReady model) {
        if (model != null) {
            if (model.isReady) {
                playerReadySet.add(userId);
            } else {
                playerReadySet.remove(userId);
            }
        }
    }

    /**
     * 玩家游戏状态
     * Player game status.
     */
    public void onPlayerMGCommonPlayerPlaying(String userId, SudMGPMGState.MGCommonPlayerPlaying model) {
        if (model != null) {
            playerPlayingMap.put(userId, model);
        }
    }

    /**
     * 关键词状态
     * Keyword status.
     */
    public void onGameMGCommonKeyWordToHit(SudMGPMGState.MGCommonKeyWordToHit model) {
        if (model != null) {
            isHitBomb = model.wordType.equals("number");
        }
    }

    /**
     * 返回该玩家是否正在游戏中
     * Return whether the player is currently in-game.
     */
    public boolean playerIsPlaying(String userId) {
        SudMGPMGState.MGCommonPlayerPlaying mgCommonPlayerPlaying = playerPlayingMap.get(userId);
        if (mgCommonPlayerPlaying != null) {
            return mgCommonPlayerPlaying.isPlaying;
        }
        return false;
    }

    /**
     * 返回该玩家是否已准备
     * Return whether the player is ready.
     */
    public boolean playerIsReady(String userId) {
        return playerReadySet.contains(userId);
    }

    /**
     * 返回该玩家是否已加入了游戏
     * Return whether the player has joined the game.
     */
    public boolean playerIsIn(String userId) {
        return playerInSet.contains(userId);
    }

    /**
     * 获取当前游戏中的人数
     * Retrieve the current number of players in the game.
     */
    public int getPlayerInNumber() {
        return playerInSet.size();
    }

    /**
     * 是否数字炸弹
     * Is it a numeric bomb?
     */
    public boolean isHitBomb() {
        return isHitBomb;
    }

    /**
     * 销毁游戏
     * Destroy the game.
     */
    public void destroyMG() {
        captainUserId = null;
        mgCommonGameStateModel = null;
        isHitBomb = false;
        playerInSet.clear();
        playerReadySet.clear();
        playerPlayingMap.clear();
    }

    /**
     * 获取队长userId
     * Get the captain's user ID.
     */
    public String getCaptainUserId() {
        return captainUserId;
    }

    /**
     * 获取当前已加入游戏的玩家集合
     * Retrieve the current set of players who have joined the game.
     */
    public HashSet<String> getPlayerInSet() {
        return new HashSet<>(playerInSet);
    }

    /**
     * 获取当前已准备的玩家集合
     * Retrieve the current set of players who are ready.
     */
    public HashSet<String> getPlayerReadySet() {
        return new HashSet<>(playerReadySet);
    }

    /**
     * 获取玩家游戏状态集合
     * Retrieve the set of player game states.
     */
    public HashMap<String, SudMGPMGState.MGCommonPlayerPlaying> getPlayerPlayingMap() {
        return new HashMap<>(playerPlayingMap);
    }

    /**
     * 返回当前游戏的状态，数值参数{@link SudMGPMGState.MGCommonGameState}
     * Return the current game state, numerical parameter {@link SudMGPMGState.MGCommonGameState}.
     */
    public int getGameState() {
        if (mgCommonGameStateModel != null) {
            return mgCommonGameStateModel.gameState;
        }
        return SudMGPMGState.MGCommonGameState.UNKNOW;
    }

}
