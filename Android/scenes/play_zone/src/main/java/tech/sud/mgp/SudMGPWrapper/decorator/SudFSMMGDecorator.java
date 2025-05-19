/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.decorator;

import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;
import tech.sud.mgp.SudMGPWrapper.utils.ISudFSMStateHandleUtils;
import tech.sud.mgp.SudMGPWrapper.utils.SudJsonUtils;
import tech.sud.mgp.core.ISudFSMMG;
import tech.sud.mgp.core.ISudFSMStateHandle;

/**
 * ISudFSMMG 游戏调APP回调装饰类
 * 参考文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG.html
 */
public class SudFSMMGDecorator implements ISudFSMMG {

    // 回调
    private SudFSMMGListener sudFSMMGListener;

    // 数据状态封装
    private final SudFSMMGCache sudFSMMGCache = new SudFSMMGCache();

    /**
     * 设置回调
     *
     * @param listener 监听器
     */
    public void setSudFSMMGListener(SudFSMMGListener listener) {
        sudFSMMGListener = listener;
    }

    /**
     * 游戏日志
     * 最低版本：v1.1.30.xx
     */
    @Override
    public void onGameLog(String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameLog(dataJson);
        }
    }

    /**
     * 游戏加载进度
     *
     * @param stage    阶段：start=1,loading=2,end=3
     * @param retCode  错误码：0成功
     * @param progress 进度：[0, 100]
     */
    @Override
    public void onGameLoadingProgress(int stage, int retCode, int progress) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameLoadingProgress(stage, retCode, progress);
        }
    }

    /**
     * 游戏开始
     * 最低版本：v1.1.30.xx
     */
    @Override
    public void onGameStarted() {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameStarted();
        }
    }

    /**
     * 游戏销毁
     * 最低版本：v1.1.30.xx
     */
    @Override
    public void onGameDestroyed() {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameDestroyed();
        }
    }

    /**
     * Code过期，需要实现
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param dataJson {"code":"value"}
     */
    @Override
    public void onExpireCode(ISudFSMStateHandle handle, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onExpireCode(handle, dataJson);
        }
    }

    /**
     * 获取游戏View信息，需要实现
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param handle   操作
     * @param dataJson {}
     */
    @Override
    public void onGetGameViewInfo(ISudFSMStateHandle handle, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGetGameViewInfo(handle, dataJson);
        }
    }

    /**
     * 获取游戏Config，需要实现
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param handle   操作
     * @param dataJson {}
     *                 最低版本：v1.1.30.xx
     */
    @Override
    public void onGetGameCfg(ISudFSMStateHandle handle, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGetGameCfg(handle, dataJson);
        }
    }

    /**
     * 游戏状态变化
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param handle   操作
     * @param state    状态命令
     * @param dataJson 状态值
     */
    @Override
    public void onGameStateChange(ISudFSMStateHandle handle, String state, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null && listener.onGameStateChange(handle, state, dataJson)) {
            return;
        }
        switch (state) {
            case SudMGPMGState.MG_COMMON_PUBLIC_MESSAGE: // 1. 公屏消息
                SudMGPMGState.MGCommonPublicMessage mgCommonPublicMessage = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPublicMessage.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonPublicMessage(handle, mgCommonPublicMessage);
                }
                break;
            case SudMGPMGState.MG_COMMON_KEY_WORD_TO_HIT: // 2. 关键词状态
                SudMGPMGState.MGCommonKeyWordToHit mgCommonKeyWordToHit = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonKeyWordToHit.class);
                sudFSMMGCache.onGameMGCommonKeyWordToHit(mgCommonKeyWordToHit);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonKeyWordToHit(handle, mgCommonKeyWordToHit);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SETTLE: // 3. 游戏结算状态
                SudMGPMGState.MGCommonGameSettle mgCommonGameSettle = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSettle.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSettle(handle, mgCommonGameSettle);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_JOIN_BTN: // 4. 加入游戏按钮点击状态
                SudMGPMGState.MGCommonSelfClickJoinBtn mgCommonSelfClickJoinBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickJoinBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickJoinBtn(handle, mgCommonSelfClickJoinBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_CANCEL_JOIN_BTN: // 5. 取消加入(退出)游戏按钮点击状态
                SudMGPMGState.MGCommonSelfClickCancelJoinBtn selfClickCancelJoinBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickCancelJoinBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickCancelJoinBtn(handle, selfClickCancelJoinBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_READY_BTN: // 6. 准备按钮点击状态
                SudMGPMGState.MGCommonSelfClickReadyBtn mgCommonSelfClickReadyBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickReadyBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickReadyBtn(handle, mgCommonSelfClickReadyBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_CANCEL_READY_BTN: // 7. 取消准备按钮点击状态
                SudMGPMGState.MGCommonSelfClickCancelReadyBtn mgCommonSelfClickCancelReadyBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickCancelReadyBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickCancelReadyBtn(handle, mgCommonSelfClickCancelReadyBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_START_BTN: // 8. 开始游戏按钮点击状态
                SudMGPMGState.MGCommonSelfClickStartBtn mgCommonSelfClickStartBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickStartBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickStartBtn(handle, mgCommonSelfClickStartBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_SHARE_BTN: // 9. 分享按钮点击状态
                SudMGPMGState.MGCommonSelfClickShareBtn mgCommonSelfClickShareBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickShareBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickShareBtn(handle, mgCommonSelfClickShareBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_STATE: // 10. 游戏状态
                SudMGPMGState.MGCommonGameState mgCommonGameState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameState.class);
                sudFSMMGCache.onGameMGCommonGameState(mgCommonGameState);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameState(handle, mgCommonGameState);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GAME_SETTLE_CLOSE_BTN: // 11. 结算界面关闭按钮点击状态（2021-12-27新增）
                SudMGPMGState.MGCommonSelfClickGameSettleCloseBtn mgCommonSelfClickGameSettleCloseBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGameSettleCloseBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickGameSettleCloseBtn(handle, mgCommonSelfClickGameSettleCloseBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GAME_SETTLE_AGAIN_BTN: // 12. 结算界面再来一局按钮点击状态（2021-12-27新增）
                SudMGPMGState.MGCommonSelfClickGameSettleAgainBtn mgCommonSelfClickGameSettleAgainBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGameSettleAgainBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickGameSettleAgainBtn(handle, mgCommonSelfClickGameSettleAgainBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SOUND_LIST: // 13. 游戏上报游戏中的声音列表（2021-12-30新增，现在只支持碰碰我最强）
                SudMGPMGState.MGCommonGameSoundList mgCommonGameSoundList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSoundList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSoundList(handle, mgCommonGameSoundList);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SOUND: // 14. 游通知app层播放声音（2021-12-30新增，现在只支持碰碰我最强）
                SudMGPMGState.MGCommonGameSound mgCommonGameSound = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSound.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSound(handle, mgCommonGameSound);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_BG_MUSIC_STATE: // 15. 游戏通知app层播放背景音乐状态（2022-01-07新增，现在只支持碰碰我最强）
                SudMGPMGState.MGCommonGameBgMusicState mgCommonGameBgMusicState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameBgMusicState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameBgMusicState(handle, mgCommonGameBgMusicState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SOUND_STATE: // 16. 游戏通知app层播放音效的状态（2022-01-07新增，现在只支持碰碰我最强）
                SudMGPMGState.MGCommonGameSoundState mgCommonGameSoundState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSoundState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSoundState(handle, mgCommonGameSoundState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_ASR: // 17. ASR状态(开启和关闭语音识别状态，v1.1.45.xx 版本新增)
                SudMGPMGState.MGCommonGameASR mgCommonGameASR = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameASR.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameASR(handle, mgCommonGameASR);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_MICROPHONE: // 18. 麦克风状态（2022-02-08新增）
                SudMGPMGState.MGCommonSelfMicrophone mgCommonSelfMicrophone = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfMicrophone.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfMicrophone(handle, mgCommonSelfMicrophone);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_HEADPHONE: // 19. 耳机（听筒，扬声器）状态（2022-02-08新增）
                SudMGPMGState.MGCommonSelfHeadphone mgCommonSelfHeadphone = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfHeadphone.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfHeadphone(handle, mgCommonSelfHeadphone);
                }
                break;
            case SudMGPMGState.MG_COMMON_APP_COMMON_SELF_X_RESP: // 20. App通用状态操作结果错误码（2022-05-10新增）
                SudMGPMGState.MGCommonAPPCommonSelfXResp mgCommonAPPCommonSelfXResp = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonAPPCommonSelfXResp.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonAPPCommonSelfXResp(handle, mgCommonAPPCommonSelfXResp);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_ADD_AI_PLAYERS: // 21. 游戏通知app层添加陪玩机器人是否成功（2022-05-17新增）
                SudMGPMGState.MGCommonGameAddAIPlayers mgCommonGameAddAIPlayers = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameAddAIPlayers.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameAddAIPlayers(handle, mgCommonGameAddAIPlayers);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_NETWORK_STATE: // 22. 游戏通知app层添当前网络连接状态（2022-06-21新增）
                SudMGPMGState.MGCommonGameNetworkState mgCommonGameNetworkState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameNetworkState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameNetworkState(handle, mgCommonGameNetworkState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_GET_SCORE: // 23. 游戏通知app获取积分
                SudMGPMGState.MGCommonGameGetScore mgCommonGameScore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameGetScore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameGetScore(handle, mgCommonGameScore);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SET_SCORE: // 24. 游戏通知app带入积分
                SudMGPMGState.MGCommonGameSetScore mgCommonGameSetScore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSetScore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSetScore(handle, mgCommonGameSetScore);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_CREATE_ORDER: // 25. 创建订单
                SudMGPMGState.MGCommonGameCreateOrder mgCommonGameCreateOrder = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameCreateOrder.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameCreateOrder(handle, mgCommonGameCreateOrder);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_ROLE_ID: // 26. 游戏通知app玩家角色(仅对狼人杀有效)
                SudMGPMGState.MGCommonPlayerRoleId mgCommonPlayerRoleId = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerRoleId.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonPlayerRoleId(handle, mgCommonPlayerRoleId);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_POOP: // 27. 游戏通知app玩家被扔便便(你画我猜，你说我猜，友尽闯关有效)
                SudMGPMGState.MGCommonSelfClickPoop mgCommonSelfClickPoop = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickPoop.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickPoop(handle, mgCommonSelfClickPoop);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GOOD: // 28. 游戏通知app玩家被点赞(你画我猜，你说我猜，友尽闯关有效)
                SudMGPMGState.MGCommonSelfClickGood mgCommonSelfClickGood = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGood.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickGood(handle, mgCommonSelfClickGood);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_FPS: // 29. 游戏通知app游戏FPS(仅对碰碰，多米诺骨牌，飞镖达人生效)
                SudMGPMGState.MGCommonGameFps mgCommonGameFps = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameFps.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameFps(handle, mgCommonGameFps);
                }
                break;
            case SudMGPMGState.MG_COMMON_ALERT: // 30. 游戏通知app游戏弹框
                SudMGPMGState.MGCommonAlert mgCommonAlert = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonAlert.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonAlert(handle, mgCommonAlert);
                }
                break;
            case SudMGPMGState.MG_COMMON_WORST_TEAMMATE: // 31. 游戏通知app最坑队友（只支持友尽闯关）
                SudMGPMGState.MGCommonWorstTeammate mgCommonWorstTeammate = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonWorstTeammate.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonWorstTeammate(handle, mgCommonWorstTeammate);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_OVER_TIP: // 32. 游戏通知app因玩家逃跑导致游戏结束（只支持友尽闯关）
                SudMGPMGState.MGCommonGameOverTip mgCommonGameOverTip = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameOverTip.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameOverTip(handle, mgCommonGameOverTip);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_COLOR: // 33. 游戏通知app玩家颜色（只支持友尽闯关）
                SudMGPMGState.MGCommonGamePlayerColor mgCommonGamePlayerColor = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerColor.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerColor(handle, mgCommonGamePlayerColor);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_ICON_POSITION: // 34. 游戏通知app玩家头像的坐标（只支持ludo）
                SudMGPMGState.MGCommonGamePlayerIconPosition mgCommonGamePlayerIconPosition = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerIconPosition.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerIconPosition(handle, mgCommonGamePlayerIconPosition);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_EXIT_GAME_BTN: // 35. 游戏通知app退出游戏（只支持teenpattipro 与 德州pro）
                SudMGPMGState.MGCommonSelfClickExitGameBtn mgCommonSelfClickExitGameBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickExitGameBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickExitGameBtn(handle, mgCommonSelfClickExitGameBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_IS_APP_CHIP: // 36. 游戏通知app是否要开启带入积分（只支持teenpattipro 与 德州pro）
                SudMGPMGState.MGCommonGameIsAppChip mgCommonGameIsAppChip = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameIsAppChip.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameIsAppChip(handle, mgCommonGameIsAppChip);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_RULE: // 37. 游戏通知app当前游戏的设置信息（只支持德州pro，teenpatti pro）
                SudMGPMGState.MGCommonGameRule mgCommonGameRule = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameRule.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameRule(handle, mgCommonGameRule);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SETTINGS: // 38. 游戏通知app进行玩法设置（只支持德州pro，teenpatti pro）
                SudMGPMGState.MGCommonGameSettings mgCommonGameSettings = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSettings.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSettings(handle, mgCommonGameSettings);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_MONEY_NOT_ENOUGH: // 39. 游戏通知app钱币不足（只支持德州pro，teenpatti pro）
                SudMGPMGState.MGCommonGameMoneyNotEnough mgCommonGameMoneyNotEnough = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameMoneyNotEnough.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameMoneyNotEnough(handle, mgCommonGameMoneyNotEnough);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_UI_CUSTOM_CONFIG: // 40. 游戏通知app下发定制ui配置表（只支持ludo）
                SudMGPMGState.MGCommonGameUiCustomConfig mgCommonGameUiCustomConfig = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameUiCustomConfig.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameUiCustomConfig(handle, mgCommonGameUiCustomConfig);
                }
                break;
            case SudMGPMGState.MG_COMMON_SET_CLICK_RECT: // 41. 设置app提供给游戏可点击区域(赛车)
                SudMGPMGState.MGCommonSetClickRect mgCommonSetClickRect = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSetClickRect.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSetClickRect(handle, mgCommonSetClickRect);
                }
                break;
            case SudMGPMGState.MG_COMMON_USERS_INFO: // 42. 通知app提供对应uids列表玩家的数据(赛车)
                SudMGPMGState.MGCommonUsersInfo mgCommonUsersInfo = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonUsersInfo.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonUsersInfo(handle, mgCommonUsersInfo);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PREPARE_FINISH: // 43. 通知app游戏前期准备完成(赛车)
                SudMGPMGState.MGCommonGamePrepareFinish mgCommonGamePrepareFinish = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePrepareFinish.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePrepareFinish(handle, mgCommonGamePrepareFinish);
                }
                break;
            case SudMGPMGState.MG_COMMON_SHOW_GAME_SCENE: // 44. 通知app游戏主界面已显示(赛车)
                SudMGPMGState.MGCommonShowGameScene mgCommonShowGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonShowGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonShowGameScene(handle, mgCommonShowGameScene);
                }
                break;
            case SudMGPMGState.MG_COMMON_HIDE_GAME_SCENE: // 45. 通知app游戏主界面已隐藏(赛车)
                SudMGPMGState.MGCommonHideGameScene mgCommonHideGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonHideGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonHideGameScene(handle, mgCommonHideGameScene);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GOLD_BTN: // 46. 通知app点击了游戏的金币按钮(赛车)
                SudMGPMGState.MGCommonSelfClickGoldBtn mgCommonSelfClickGoldBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGoldBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickGoldBtn(handle, mgCommonSelfClickGoldBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PIECE_ARRIVE_END: // 47. 通知app棋子到达终点(ludo)
                SudMGPMGState.MGCommonGamePieceArriveEnd mgCommonGamePieceArriveEnd = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePieceArriveEnd.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePieceArriveEnd(handle, mgCommonGamePieceArriveEnd);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_MANAGED_STATE: // 48. 通知app玩家是否托管
                SudMGPMGState.MGCommonGamePlayerManagedState mgCommonGamePlayerManagedState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerManagedState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerManagedState(handle, mgCommonGamePlayerManagedState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SEND_BURST_WORD: // 49. 游戏向app发送爆词
                SudMGPMGState.MGCommonGameSendBurstWord mgCommonGameSendBurstWord = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSendBurstWord.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSendBurstWord(handle, mgCommonGameSendBurstWord);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_RANKS: // 50. 游戏向app发送玩家实时排名（只支持怪物消消乐）
                SudMGPMGState.MGCommonGamePlayerRanks mgCommonGamePlayerRanks = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerRanks.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerRanks(handle, mgCommonGamePlayerRanks);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_PAIR_SINGULAR: // 51. 游戏向app发送玩家即时变化的单双牌（只支持okey101）
                SudMGPMGState.MGCommonGamePlayerPairSingular mgCommonGamePlayerPairSingular = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerPairSingular.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerPairSingular(handle, mgCommonGamePlayerPairSingular);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_MONOPOLY_CARDS: // 52. 游戏向app发送获取玩家持有的道具卡（只支持大富翁）
                SudMGPMGState.MGCommonGamePlayerMonopolyCards mgCommonGamePlayerMonopolyCards = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerMonopolyCards.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerMonopolyCards(handle, mgCommonGamePlayerMonopolyCards);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_SCORES: // 53. 游戏向app发送玩家实时积分（只支持怪物消消乐）
                SudMGPMGState.MGCommonGamePlayerScores mgCommonGamePlayerScores = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerScores.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerScores(handle, mgCommonGamePlayerScores);
                }
                break;
            case SudMGPMGState.MG_COMMON_DESTROY_GAME_SCENE: // 54. 游戏通知app销毁游戏（只支持部分概率类游戏）
                SudMGPMGState.MGCommonDestroyGameScene mgCommonDestroyGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonDestroyGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonDestroyGameScene(handle, mgCommonDestroyGameScene);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_BILLIARDS_HIT_STATE: // 55. 游戏通知app击球状态（只支持桌球）
                SudMGPMGState.MGCommonGameBilliardsHitState mgCommonGameBilliardsHitState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameBilliardsHitState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameBilliardsHitState(handle, mgCommonGameBilliardsHitState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_PROPS_CARDS: // 56. 游戏向app发送获取玩家持有的指定点数道具卡（只支持飞行棋）
                SudMGPMGState.MGCommonGamePlayerPropsCards mgCommonGamePlayerPropsCards = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerPropsCards.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGamePlayerPropsCards(handle, mgCommonGamePlayerPropsCards);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_DISCO_ACTION: // 1. 元宇宙砂砂舞指令回调
                SudMGPMGState.MGCommonGameDiscoAction mgCommonGameDiscoAction = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameDiscoAction.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameDiscoAction(handle, mgCommonGameDiscoAction);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_DISCO_ACTION_END: // 2. 元宇宙砂砂舞指令动作结束通知
                SudMGPMGState.MGCommonGameDiscoActionEnd mgCommonGameDiscoActionEnd = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameDiscoActionEnd.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameDiscoActionEnd(handle, mgCommonGameDiscoActionEnd);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_CONFIG: // 1. 礼物配置文件(火箭)
                SudMGPMGState.MGCustomRocketConfig mgCustomRocketConfig = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketConfig.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketConfig(handle, mgCustomRocketConfig);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_MODEL_LIST: // 2. 拥有模型列表(火箭)
                SudMGPMGState.MGCustomRocketModelList mgCustomRocketModelList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketModelList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketModelList(handle, mgCustomRocketModelList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_COMPONENT_LIST: // 3. 拥有组件列表(火箭)
                SudMGPMGState.MGCustomRocketComponentList mgCustomRocketComponentList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketComponentList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketComponentList(handle, mgCustomRocketComponentList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_USER_INFO: // 4. 获取用户信息(火箭)
                SudMGPMGState.MGCustomRocketUserInfo mgCustomRocketUserInfo = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketUserInfo.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketUserInfo(handle, mgCustomRocketUserInfo);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_ORDER_RECORD_LIST: // 5. 订单记录列表(火箭)
                SudMGPMGState.MGCustomRocketOrderRecordList mgCustomRocketOrderRecordList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketOrderRecordList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketOrderRecordList(handle, mgCustomRocketOrderRecordList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_ROOM_RECORD_LIST: // 6. 展馆内列表(火箭)
                SudMGPMGState.MGCustomRocketRoomRecordList mgCustomRocketRoomRecordList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketRoomRecordList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketRoomRecordList(handle, mgCustomRocketRoomRecordList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_USER_RECORD_LIST: // 7. 展馆内玩家送出记录(火箭)
                SudMGPMGState.MGCustomRocketUserRecordList mgCustomRocketUserRecordList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketUserRecordList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketUserRecordList(handle, mgCustomRocketUserRecordList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_SET_DEFAULT_MODEL: // 8. 设置默认模型(火箭)
                SudMGPMGState.MGCustomRocketSetDefaultModel mgCustomRocketSetDefaultSeat = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketSetDefaultModel.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketSetDefaultModel(handle, mgCustomRocketSetDefaultSeat);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_DYNAMIC_FIRE_PRICE: // 9. 动态计算一键发送价格(火箭)
                SudMGPMGState.MGCustomRocketDynamicFirePrice mgCustomRocketDynamicFirePrice = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketDynamicFirePrice.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketDynamicFirePrice(handle, mgCustomRocketDynamicFirePrice);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_FIRE_MODEL: // 10. 一键发送(火箭)
                SudMGPMGState.MGCustomRocketFireModel mGCustomRocketFireModel = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketFireModel.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketFireModel(handle, mGCustomRocketFireModel);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_CREATE_MODEL: // 11. 新组装模型(火箭)
                SudMGPMGState.MGCustomRocketCreateModel mgCustomRocketCreateModel = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketCreateModel.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketCreateModel(handle, mgCustomRocketCreateModel);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_REPLACE_COMPONENT: // 12. 模型更换组件(火箭)
                SudMGPMGState.MGCustomRocketReplaceComponent mgCustomRocketReplaceComponent = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketReplaceComponent.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketReplaceComponent(handle, mgCustomRocketReplaceComponent);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_BUY_COMPONENT: // 13. 购买组件(火箭)
                SudMGPMGState.MGCustomRocketBuyComponent mgCustomRocketBuyComponent = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketBuyComponent.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketBuyComponent(handle, mgCustomRocketBuyComponent);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_PLAY_EFFECT_START: // 14. 播放效果开始(火箭)
                SudMGPMGState.MGCustomRocketPlayEffectStart mgCustomRocketPlayEffectStart = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketPlayEffectStart.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketPlayEffectStart(handle, mgCustomRocketPlayEffectStart);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_PLAY_EFFECT_FINISH: // 15. 播放效果完成(火箭)
                SudMGPMGState.MGCustomRocketPlayEffectFinish mgCustomRocketPlayEffectFinish = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketPlayEffectFinish.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketPlayEffectFinish(handle, mgCustomRocketPlayEffectFinish);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_VERIFY_SIGN: // 16. 验证签名合规(火箭)
                SudMGPMGState.MGCustomRocketVerifySign mgCustomRocketVerifySign = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketVerifySign.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketVerifySign(handle, mgCustomRocketVerifySign);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_UPLOAD_MODEL_ICON: // 17. 上传icon(火箭)
                SudMGPMGState.MGCustomRocketUploadModelIcon mgCustomRocketUploadModelIcon = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketUploadModelIcon.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketUploadModelIcon(handle, mgCustomRocketUploadModelIcon);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_PREPARE_FINISH: // 18. 前期准备完成(火箭)
                SudMGPMGState.MGCustomRocketPrepareFinish mgCustomRocketPrepareFinish = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketPrepareFinish.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketPrepareFinish(handle, mgCustomRocketPrepareFinish);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_SHOW_GAME_SCENE: // 19. 火箭主界面已显示(火箭)
                SudMGPMGState.MGCustomRocketShowGameScene mgCustomRocketShowGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketShowGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketShowGameScene(handle, mgCustomRocketShowGameScene);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_HIDE_GAME_SCENE: // 20. 火箭主界面已隐藏(火箭)
                SudMGPMGState.MGCustomRocketHideGameScene mgCustomRocketHideGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketHideGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketHideGameScene(handle, mgCustomRocketHideGameScene);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_CLICK_LOCK_COMPONENT: // 21. 点击锁住组件(火箭)
                SudMGPMGState.MGCustomRocketClickLockComponent mgCustomRocketClickLockComponent = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketClickLockComponent.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketClickLockComponent(handle, mgCustomRocketClickLockComponent);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_FLY_CLICK: // 22. 火箭效果飞行点击(火箭)
                SudMGPMGState.MGCustomRocketFlyClick mgCustomRocketFlyClick = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketFlyClick.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketFlyClick(handle, mgCustomRocketFlyClick);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_FLY_END: // 23. 火箭效果飞行结束(火箭)
                SudMGPMGState.MGCustomRocketFlyEnd mgCustomRocketFlyEnd = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketFlyEnd.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketFlyEnd(handle, mgCustomRocketFlyEnd);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_SET_CLICK_RECT: // 24. 设置点击区域(火箭)
                SudMGPMGState.MGCustomRocketSetClickRect mgCustomRocketSetClickRect = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketSetClickRect.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketSetClickRect(handle, mgCustomRocketSetClickRect);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_SAVE_SIGN_COLOR: // 25. 颜色和签名自定义改到装配间的模式，保存颜色或签名 模型
                SudMGPMGState.MGCustomRocketSaveSignColor mgCustomRocketSaveSignColor = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketSaveSignColor.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketSaveSignColor(handle, mgCustomRocketSaveSignColor);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_DEFUALT_STATE: // 1. 设置界面默认状态(棒球)
                SudMGPMGState.MGBaseballDefaultState mgBaseballDefaultState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballDefaultState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballDefaultState(handle, mgBaseballDefaultState);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_PREPARE_FINISH: // 2. 前期准备完成(棒球)
                SudMGPMGState.MGBaseballPrepareFinish mgBaseballPrepareFinish = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballPrepareFinish.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballPrepareFinish(handle, mgBaseballPrepareFinish);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_SHOW_GAME_SCENE: // 3. 主界面已显示(棒球)
                SudMGPMGState.MGBaseballShowGameScene mgBaseballShowGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballShowGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballShowGameScene(handle, mgBaseballShowGameScene);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_HIDE_GAME_SCENE: // 4. 主界面已隐藏(棒球)
                SudMGPMGState.MGBaseballHideGameScene mgBaseballHideGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballHideGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballHideGameScene(handle, mgBaseballHideGameScene);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_RANKING: // 5. 查询排行榜数据(棒球)
                SudMGPMGState.MGBaseballRanking mgBaseballRanking = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballRanking.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballRanking(handle, mgBaseballRanking);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_MY_RANKING: // 6. 查询我的排名(棒球)
                SudMGPMGState.MGBaseballMyRanking mgBaseballMyRanking = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballMyRanking.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballMyRanking(handle, mgBaseballMyRanking);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_RANGE_INFO: // 7. 查询当前距离我的前后玩家数据(棒球)
                SudMGPMGState.MGBaseballRangeInfo mgBaseballRangeInfo = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballRangeInfo.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballRangeInfo(handle, mgBaseballRangeInfo);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_SET_CLICK_RECT: // 8. 设置app提供给游戏可点击区域(棒球)
                SudMGPMGState.MGBaseballSetClickRect mgBaseballSetClickRect = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballSetClickRect.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballSetClickRect(handle, mgBaseballSetClickRect);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_TEXT_CONFIG: // 9. 获取文本配置数据(棒球)
                SudMGPMGState.MGBaseballTextConfig mgBaseballTextConfig = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballTextConfig.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballTextConfig(handle, mgBaseballTextConfig);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_SEND_DISTANCE: // 10. 球落地, 通知距离(棒球)
                SudMGPMGState.MGBaseballSendDistance mgBaseballSendDistance = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballSendDistance.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballSendDistance(handle, mgBaseballSendDistance);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_CR_ROOM_INIT_DATA: // 1. 请求房间数据
                SudMGPMGState.MGCustomCrRoomInitData mgCustomCrRoomInitData = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomCrRoomInitData.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomCrRoomInitData(handle, mgCustomCrRoomInitData);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_CR_CLICK_SEAT: // 2. 点击主播位或老板位通知
                SudMGPMGState.MGCustomCrClickSeat mgCustomCrClickSeat = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomCrClickSeat.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomCrClickSeat(handle, mgCustomCrClickSeat);
                }
                break;
            default:
                ISudFSMStateHandleUtils.handleSuccess(handle);
                break;
        }
    }

    /**
     * 游戏玩家状态变化
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param handle   操作
     * @param userId   用户id
     * @param state    状态命令
     * @param dataJson 状态值
     */
    @Override
    public void onPlayerStateChange(ISudFSMStateHandle handle, String userId, String state, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null && listener.onPlayerStateChange(handle, userId, state, dataJson)) {
            return;
        }
        switch (state) {
            case SudMGPMGState.MG_COMMON_PLAYER_IN: // 1.加入状态（已修改）
                SudMGPMGState.MGCommonPlayerIn mgCommonPlayerIn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerIn.class);
                sudFSMMGCache.onPlayerMGCommonPlayerIn(userId, mgCommonPlayerIn);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerIn(handle, userId, mgCommonPlayerIn);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_READY: // 2.准备状态（已修改）
                SudMGPMGState.MGCommonPlayerReady mgCommonPlayerReady = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerReady.class);
                sudFSMMGCache.onPlayerMGCommonPlayerReady(userId, mgCommonPlayerReady);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerReady(handle, userId, mgCommonPlayerReady);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_CAPTAIN: // 3.队长状态（已修改）
                SudMGPMGState.MGCommonPlayerCaptain mgCommonPlayerCaptain = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerCaptain.class);
                sudFSMMGCache.onPlayerMGCommonPlayerCaptain(userId, mgCommonPlayerCaptain);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerCaptain(handle, userId, mgCommonPlayerCaptain);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_PLAYING: // 4.游戏状态（已修改）
                SudMGPMGState.MGCommonPlayerPlaying mgCommonPlayerPlaying = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerPlaying.class);
                sudFSMMGCache.onPlayerMGCommonPlayerPlaying(userId, mgCommonPlayerPlaying);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerPlaying(handle, userId, mgCommonPlayerPlaying);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_ONLINE: // 5.玩家在线状态
                SudMGPMGState.MGCommonPlayerOnline mgCommonPlayerOnline = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerOnline.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerOnline(handle, userId, mgCommonPlayerOnline);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_CHANGE_SEAT: // 6.玩家换游戏位状态
                SudMGPMGState.MGCommonPlayerChangeSeat mgCommonPlayerChangeSeat = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerChangeSeat.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerChangeSeat(handle, userId, mgCommonPlayerChangeSeat);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GAME_PLAYER_ICON: // 7. 游戏通知app点击玩家头像
                SudMGPMGState.MGCommonSelfClickGamePlayerIcon mgCommonSelfClickGamePlayerIcon = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGamePlayerIcon.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfClickGamePlayerIcon(handle, userId, mgCommonSelfClickGamePlayerIcon);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_DIE_STATUS: // 8. 游戏通知app玩家死亡状态（2022-04-24新增）
                SudMGPMGState.MGCommonSelfDieStatus mgCommonSelfDieStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfDieStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfDieStatus(handle, userId, mgCommonSelfDieStatus);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_TURN_STATUS: // 9. 游戏通知app轮到玩家出手状态（2022-04-24新增）
                SudMGPMGState.MGCommonSelfTurnStatus mgCommonSelfTurnStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfTurnStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfTurnStatus(handle, userId, mgCommonSelfTurnStatus);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_SELECT_STATUS: // 10. 游戏通知app玩家选择状态（2022-04-24新增）
                SudMGPMGState.MGCommonSelfSelectStatus mgCommonSelfSelectStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfSelectStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfSelectStatus(handle, userId, mgCommonSelfSelectStatus);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_COUNTDOWN_TIME: // 11. 游戏通知app层当前游戏剩余时间（2022-05-23新增，目前UMO生效）
                SudMGPMGState.MGCommonGameCountdownTime mgCommonGameCountdownTime = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameCountdownTime.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonGameCountdownTime(handle, userId, mgCommonGameCountdownTime);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_OB_STATUS: // 12. 游戏通知app层当前玩家死亡后变成ob视角（2022-08-23新增，目前狼人杀生效）
                SudMGPMGState.MGCommonSelfObStatus mgCommonSelfObStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfObStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfObStatus(handle, userId, mgCommonSelfObStatus);
                }
                break;
            case SudMGPMGState.MG_DG_SELECTING: // 1. 选词中状态（已修改）
                SudMGPMGState.MGDGSelecting mgdgSelecting = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGSelecting.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGSelecting(handle, userId, mgdgSelecting);
                }
                break;
            case SudMGPMGState.MG_DG_PAINTING: // 2. 作画中状态（已修改）
                SudMGPMGState.MGDGPainting mgdgPainting = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGPainting.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGPainting(handle, userId, mgdgPainting);
                }
                break;
            case SudMGPMGState.MG_DG_ERRORANSWER: // 3. 显示错误答案状态（已修改）
                SudMGPMGState.MGDGErroranswer mgdgErroranswer = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGErroranswer.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGErroranswer(handle, userId, mgdgErroranswer);
                }
                break;
            case SudMGPMGState.MG_DG_TOTALSCORE: // 4. 显示总积分状态（已修改）
                SudMGPMGState.MGDGTotalscore mgdgTotalscore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGTotalscore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGTotalscore(handle, userId, mgdgTotalscore);
                }
                break;
            case SudMGPMGState.MG_DG_SCORE: // 5. 本次获得积分状态（已修改）
                SudMGPMGState.MGDGScore mgdgScore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGScore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGScore(handle, userId, mgdgScore);
                }
                break;
            default:
                ISudFSMStateHandleUtils.handleSuccess(handle);
                break;
        }
    }

    /** 获取队长userId */
    public String getCaptainUserId() {
        return sudFSMMGCache.getCaptainUserId();
    }

    // 返回该玩家是否正在游戏中
    public boolean playerIsPlaying(String userId) {
        return sudFSMMGCache.playerIsPlaying(userId);
    }

    // 返回该玩家是否已准备
    public boolean playerIsReady(String userId) {
        return sudFSMMGCache.playerIsReady(userId);
    }

    // 返回该玩家是否已加入了游戏
    public boolean playerIsIn(String userId) {
        return sudFSMMGCache.playerIsIn(userId);
    }

    // 获取当前游戏中的人数
    public int getPlayerInNumber() {
        return sudFSMMGCache.getPlayerInNumber();
    }

    // 是否数字炸弹
    public boolean isHitBomb() {
        return sudFSMMGCache.isHitBomb();
    }

    // 销毁游戏
    public void destroyMG() {
        sudFSMMGCache.destroyMG();
        sudFSMMGListener = null;
    }

    /**
     * 返回当前游戏的状态，数值参数{@link SudMGPMGState.MGCommonGameState}
     */
    public int getGameState() {
        return sudFSMMGCache.getGameState();
    }

    /** 获取缓存的状态 */
    public SudFSMMGCache getSudFSMMGCache() {
        return sudFSMMGCache;
    }

}
