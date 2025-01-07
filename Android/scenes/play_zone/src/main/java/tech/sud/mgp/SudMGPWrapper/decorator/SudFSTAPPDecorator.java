/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.decorator;

import java.nio.ByteBuffer;
import java.util.List;

import tech.sud.mgp.SudMGPWrapper.state.SudMGPAPPState;
import tech.sud.mgp.SudMGPWrapper.utils.SudJsonUtils;
import tech.sud.mgp.core.ISudFSTAPP;
import tech.sud.mgp.core.ISudListenerNotifyStateChange;

/**
 * ISudFSTAPP的装饰类，接近于业务
 * 参考文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSTAPP.html
 * 注意：
 * 1，向游戏侧发送状态之后，不能立即调用destroyMG()方法，也不能立即finish Activity。例如：{@link SudFSTAPPDecorator#notifyAPPCommonSelfEnd()}
 */
public class SudFSTAPPDecorator {

    /**
     * APP调用游戏的接口
     */
    private ISudFSTAPP iSudFSTAPP;
    private OnNotifyStateChangeListener onNotifyStateChangeListener;

    /**
     * 设置app调用sdk的对象
     *
     * @param iSudFSTAPP
     */
    public void setISudFSTAPP(ISudFSTAPP iSudFSTAPP) {
        this.iSudFSTAPP = iSudFSTAPP;
    }

    // region 状态通知，ISudFSTAPP.notifyStateChange

    /**
     * 发送
     * 1. 加入状态
     *
     * @param isIn         true 加入游戏，false 退出游戏
     * @param seatIndex    加入的游戏位(座位号) 默认传seatIndex = -1 随机加入，seatIndex 从0开始，不可大于座位数
     * @param isSeatRandom 默认为ture, 带有游戏位(座位号)的时候，如果游戏位(座位号)已经被占用，是否随机分配一个空位坐下 isSeatRandom=true 随机分配空位坐下，isSeatRandom=false 不随机分配
     * @param teamId       不支持分队的游戏：数值填1；支持分队的游戏：数值填1或2（两支队伍）；
     */
    public void notifyAPPCommonSelfIn(boolean isIn, int seatIndex, boolean isSeatRandom, int teamId) {
        SudMGPAPPState.APPCommonSelfIn state = new SudMGPAPPState.APPCommonSelfIn();
        state.isIn = isIn;
        state.seatIndex = seatIndex;
        state.isSeatRandom = isSeatRandom;
        state.teamId = teamId;
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_IN, state);
    }

    /**
     * 发送
     * 2. 准备状态
     * 用户（本人）准备/取消准备
     *
     * @param isReady true 准备，false 取消准备
     */
    public void notifyAPPCommonSelfReady(boolean isReady) {
        SudMGPAPPState.APPCommonSelfReady state = new SudMGPAPPState.APPCommonSelfReady();
        state.isReady = isReady;
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_READY, state);
    }

    /**
     * 发送
     * 3. 游戏状态 模型
     * 用户游戏状态，如果用户在游戏中，建议：
     * a.空出屏幕中心区：
     * 关闭全屏礼物特效；
     * b.部分强操作类小游戏（spaceMax为true），尽量收缩原生UI，给游戏留出尽量大的操作空间：
     * 收缩公屏；
     * 收缩麦位；
     * 如果不在游戏中，则恢复。
     *
     * @param isPlaying            true 开始游戏，false 结束游戏
     * @param reportGameInfoExtras string类型，Https服务回调report_game_info参数，最大长度1024字节，超过则截断（2022-01-21）
     */
    public void notifyAPPCommonSelfPlaying(boolean isPlaying, String reportGameInfoExtras, String reportGameInfoKey) {
        SudMGPAPPState.APPCommonSelfPlaying state = new SudMGPAPPState.APPCommonSelfPlaying();
        state.isPlaying = isPlaying;
        state.reportGameInfoExtras = reportGameInfoExtras;
        state.reportGameInfoKey = reportGameInfoKey;
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_PLAYING, state);
    }

    /**
     * 发送
     * 4. 队长状态
     * 用户是否为队长，队长在游戏中会有开始游戏的权利。
     * 发送此状态后，会把队长身份转移到另一名用户身上。
     *
     * @param curCaptainUID 必填，指定队长uid
     */
    public void notifyAPPCommonSelfCaptain(String curCaptainUID) {
        SudMGPAPPState.APPCommonSelfCaptain state = new SudMGPAPPState.APPCommonSelfCaptain();
        state.curCaptainUID = curCaptainUID;
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_CAPTAIN, state);
    }

    /**
     * 发送
     * 5. 踢人
     * 用户（本人，队长）踢其他玩家；
     * 队长才能踢人；
     *
     * @param kickedUID 被踢用户uid
     */
    public void notifyAPPCommonSelfKick(String kickedUID) {
        SudMGPAPPState.APPCommonSelfKick state = new SudMGPAPPState.APPCommonSelfKick();
        state.kickedUID = kickedUID;
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_KICK, state);
    }

    /**
     * 发送
     * 6. 结束游戏
     * 用户（本人，队长）结束（本局）游戏
     * 注意：必须是队长发送才有效果。可通过{@link SudFSMMGDecorator#getCaptainUserId()}拿到当前队长id
     */
    public void notifyAPPCommonSelfEnd() {
        SudMGPAPPState.APPCommonSelfEnd state = new SudMGPAPPState.APPCommonSelfEnd();
        // 使用iSudFSTAPP.notifyStateChange方法向游戏侧发送状态时，因为大部分状态都需要通过网络向后端发送状态指令
        // 所以如果发送状态后，马上就销毁游戏或者Activity，那么状态指令大概率会不生效
        // *** 如果要确保指令能到达后端，那么发送指令后不要立即destroyMG()或finish Activity，可在发送后delay一定时间(如300 or 500 ms)再销毁
        // *** 如果不在乎指令是否能成功到达，可忽略delay
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_END, state);
    }

    /**
     * 发送
     * 9. 麦克风状态
     * 用户（本人）麦克风状态，建议：
     * 进入房间后初始通知一次；
     * 每次变更（开麦/闭麦/禁麦/解麦）通知一次；
     *
     * @param isOn       true 开麦，false 闭麦
     * @param isDisabled true 被禁麦，false 未被禁麦
     */
    public void notifyAPPCommonSelfMicrophone(boolean isOn, boolean isDisabled) {
        SudMGPAPPState.APPCommonSelfMicrophone state = new SudMGPAPPState.APPCommonSelfMicrophone();
        state.isOn = isOn;
        state.isDisabled = isDisabled;
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_MICROPHONE, state);
    }

    /**
     * 发送
     * 10. 文字命中状态
     * 用户（本人）聊天信息命中关键词状态，建议：
     * 精确匹配；
     * 首次聊天内容命中关键词之后，后续聊天内容不翻转成未命中；
     * 直至小游戏侧关键词更新，再将状态翻转为未命中；
     *
     * @param isHit   true 命中，false 未命中
     * @param keyWord 关键词
     * @param text    聊天原始内容
     */
    public void notifyAPPCommonSelfTextHitState(boolean isHit, String keyWord, String text) {
        SudMGPAPPState.APPCommonSelfTextHitState state = new SudMGPAPPState.APPCommonSelfTextHitState();
        state.isHit = isHit;
        state.keyWord = keyWord;
        state.text = text;
        notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_TEXT_HIT, state);
    }

    /**
     * 发送
     * 11. 打开或关闭背景音乐（2021-12-27新增）
     *
     * @param isOpen true 打开背景音乐，false 关闭背景音乐
     */
    public void notifyAPPCommonOpenBgMusic(boolean isOpen) {
        SudMGPAPPState.APPCommonOpenBgMusic state = new SudMGPAPPState.APPCommonOpenBgMusic();
        state.isOpen = isOpen;
        notifyStateChange(SudMGPAPPState.APP_COMMON_OPEN_BG_MUSIC, state);
    }

    /**
     * 发送
     * 12. 打开或关闭音效（2021-12-27新增）
     *
     * @param isOpen true 打开音效，false 关闭音效
     */
    public void notifyAPPCommonOpenSound(boolean isOpen) {
        SudMGPAPPState.APPCommonOpenSound state = new SudMGPAPPState.APPCommonOpenSound();
        state.isOpen = isOpen;
        notifyStateChange(SudMGPAPPState.APP_COMMON_OPEN_SOUND, state);
    }

    /**
     * 发送
     * 13. 打开或关闭游戏中的振动效果（2021-12-27新增）
     *
     * @param isOpen 打开振动效果，false 关闭振动效果
     */
    public void notifyAPPCommonOpenVibrate(boolean isOpen) {
        SudMGPAPPState.APPCommonOpenVibrate state = new SudMGPAPPState.APPCommonOpenVibrate();
        state.isOpen = isOpen;
        notifyStateChange(SudMGPAPPState.APP_COMMON_OPEN_VIBRATE, state);
    }

    /**
     * 发送
     * 14. 设置游戏的音量大小（2021-12-31新增）
     *
     * @param volume 音量大小 0 到 100
     */
    public void notifyAPPCommonGameSoundVolume(int volume) {
        SudMGPAPPState.APPCommonGameSoundVolume state = new SudMGPAPPState.APPCommonGameSoundVolume();
        state.volume = volume;
        notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_SOUND_VOLUME, state);
    }

    /**
     * 发送
     * 15.  设置游戏玩法选项（2022-05-10新增）
     *
     * @param ludo ludo游戏
     */
    public void notifyAPPCommonGameSettingSelectInfo(SudMGPAPPState.Ludo ludo) {
        SudMGPAPPState.APPCommonGameSettingSelectInfo state = new SudMGPAPPState.APPCommonGameSettingSelectInfo();
        state.ludo = ludo;
        notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_SETTING_SELECT_INFO, state);
    }

    /**
     * 发送
     * 16. 设置游戏中的AI玩家（2022-05-11新增）
     *
     * @param aiPlayers AI玩家
     * @param isReady   机器人加入后是否自动准备 1：自动准备，0：不自动准备 默认为1
     */
    public void notifyAPPCommonGameAddAIPlayers(List<SudMGPAPPState.AIPlayers> aiPlayers, int isReady) {
        SudMGPAPPState.APPCommonGameAddAIPlayers state = new SudMGPAPPState.APPCommonGameAddAIPlayers();
        state.aiPlayers = aiPlayers;
        state.isReady = isReady;
        notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_ADD_AI_PLAYERS, state);
    }

    /**
     * 发送
     * 17. app在收到游戏断开连接通知后，通知游戏重试连接（2022-06-21新增，暂时支持ludo）
     */
    public void notifyAPPCommonGameReconnect() {
        SudMGPAPPState.APPCommonGameReconnect state = new SudMGPAPPState.APPCommonGameReconnect();
        notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_RECONNECT, state);
    }

    /**
     * 发送
     * 18. app返回玩家当前积分
     */
    public void notifyAPPCommonGameScore(long score) {
        SudMGPAPPState.APPCommonGameScore state = new SudMGPAPPState.APPCommonGameScore();
        state.score = score;
        notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_SCORE, state);
    }
    // endregion 状态通知，ISudFSTAPP.notifyStateChange

    // region 生命周期
    public void startMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.startMG();
        }
    }

    public void pauseMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.pauseMG();
        }
    }

    public void playMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.playMG();
        }
    }

    public void stopMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.stopMG();
        }
    }

    public void destroyMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.destroyMG();
            this.iSudFSTAPP = null;
        }
    }

    // endregion 生命周期

    /**
     * 更新code
     *
     * @param code
     * @param listener
     */
    public void updateCode(String code, ISudListenerNotifyStateChange listener) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.updateCode(code, listener);
        }
    }

    /**
     * 音频流数据
     */
    public void pushAudio(ByteBuffer buffer, int bufferLength) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.pushAudio(buffer, bufferLength);
        }
    }

    // region 元宇宙砂砂舞

    /**
     * 发送
     * 1. 元宇宙砂砂舞相关设置
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/APPFST/CommonStateForDisco.html
     *
     * @param actionId 必传的参数，用于指定类型的序号，不同序号用于区分游戏内的不同功能，不传则会判断为无效指令，具体序号代表的功能见下表
     * @param cooldown 持续时间，单位秒，部分功能有持续时间就需要传对应的数值，不传或传错则会按各自功能的默认值处理（见下表）
     * @param isTop    是否置顶，针对部分功能可排队置顶（false：不置顶；true：置顶；默认为false）
     * @param field1   额外参数1，针对部分功能有具体的意义
     * @param field2   额外参数2，针对部分功能有具体的意义
     */
    public void notifyAppCommonGameDiscoAction(int actionId, Integer cooldown, Boolean isTop, String field1, String field2) {
        SudMGPAPPState.AppCommonGameDiscoAction state = new SudMGPAPPState.AppCommonGameDiscoAction();
        state.actionId = actionId;
        state.cooldown = cooldown;
        state.isTop = isTop;
        state.field1 = field1;
        state.field2 = field2;
        notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_DISCO_ACTION, state);
    }
    // endregion 元宇宙砂砂舞

    /**
     * APP状态通知给小游戏
     *
     * @param state    状态标识
     * @param dataJson 数据
     * @param listener 回调监听
     */
    public void notifyStateChange(String state, String dataJson, ISudListenerNotifyStateChange listener) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.notifyStateChange(state, dataJson, listener);
            if (onNotifyStateChangeListener != null) {
                onNotifyStateChangeListener.onNotifyStateChange(state, dataJson);
            }
        }
    }

    /**
     * APP状态通知给小游戏
     *
     * @param state    状态标识
     * @param dataJson 数据
     */
    public void notifyStateChange(String state, String dataJson) {
        notifyStateChange(state, dataJson, null);
    }

    /**
     * APP状态通知给小游戏
     *
     * @param state 状态标识
     * @param obj   数据
     */
    public void notifyStateChange(String state, Object obj) {
        notifyStateChange(state, SudJsonUtils.toJson(obj), null);
    }

    /**
     * 自定义进度条
     * 在游戏加载失败时，调用此方法可重新加载游戏
     */
    public void reloadMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.reloadMG();
        }
    }

    public void setOnNotifyStateChangeListener(OnNotifyStateChangeListener onNotifyStateChangeListener) {
        this.onNotifyStateChangeListener = onNotifyStateChangeListener;
    }

    /**
     * 此接口是监听{iSudFSTAPP.notifyStateChange}的接口调用
     */
    public interface OnNotifyStateChangeListener {
        /** 此接口回调用于监控app向游戏发送的消息，可将其打印到控制台，尽量不要在此做其他业务性的操作 */
        void onNotifyStateChange(String state, String dataJson);
    }

}
