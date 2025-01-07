/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.state;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * MG to APP 的状态定义
 * 参考文档：https://docs.sud.tech/zh-CN/app/Client/MGFSM/
 */
public class SudMGPMGState implements Serializable {

    // region MG状态机-通用状态-游戏
    // 参考文档：https://docs.sud.tech/zh-CN/app/Client/MGFSM/CommonStateGame.html
    /**
     * 1. 公屏消息（已修改）
     * 最低版本: v1.1.30.xx
     */
    public static final String MG_COMMON_PUBLIC_MESSAGE = "mg_common_public_message";

    /**
     * 1. 公屏消息（已修改）
     * 向公屏发送消息，字段含义如下
     * type
     * 0 通知
     * 1 提醒
     * 2 结算
     * 3 其他
     * msg
     * <!-- -->内为转义字段：
     * <!--name:用户昵称|uid:用户UID|color:建议颜色-->
     * 其中name/uid/color均为可选字段，字段为空的情况如下：
     * <!--name:|uid:|color:-->
     * SDK仅会缓存最新一条。
     */
    public static class MGCommonPublicMessage implements Serializable {
        // 0 通知
        // 1 提醒
        // 2 结算
        // 3 其他
        public int type;

        // 消息内容
        public List<MGCommonPublicMessageMsg> msg;

        public static class MGCommonPublicMessageMsg implements Serializable {
            // 词组类型 当phrase=1时，会返回text; 当phrase=2时，会返回user
            public int phrase;
            public MGCommonPublicMessageMsgText text;
            public MGCommonPublicMessageMsgUser user;
        }

        public static class MGCommonPublicMessageMsgText implements Serializable {
            @SerializedName(value = "default")
            public String defaultStr; // 默认文本

            @SerializedName(value = "zh-CN")
            public String zh_CN; // 中文(简体)

            @SerializedName(value = "zh-HK")
            public String zh_HK; // 中文(香港)

            @SerializedName(value = "zh-MO")
            public String zh_MO; // 中文(澳门)

            @SerializedName(value = "zh-SG")
            public String zh_SG; // 中文(新加坡)

            @SerializedName(value = "zh-TW")
            public String zh_TW; // 中文(繁体)

            @SerializedName(value = "en-US")
            public String en_US; // 英语(美国)

            @SerializedName(value = "en-GB")
            public String en_GB; // 英语(英国)

            @SerializedName(value = "ms-BN")
            public String ms_BN; // 马来语(文莱达鲁萨兰)

            @SerializedName(value = "ms-MY")
            public String ms_MY; // 马来语(马来西亚)

            @SerializedName(value = "vi-VN")
            public String vi_VN; // 越南语

            @SerializedName(value = "id-ID")
            public String id_ID; // 印度尼西亚语

            @SerializedName(value = "es-ES")
            public String es_ES; // 西班牙语(传统)

            @SerializedName(value = "ja-JP")
            public String ja_JP; // 日语

            @SerializedName(value = "ko-KR")
            public String ko_KR; // 朝鲜语

            @SerializedName(value = "th-TH")
            public String th_TH; // 泰语

            @SerializedName(value = "ar-SA")
            public String ar_SA; // 阿拉伯语(沙特阿拉伯)

            @SerializedName(value = "ur-PK")
            public String ur_PK; // 乌都语

            @SerializedName(value = "tr-TR")
            public String tr_TR; // 土耳其语

            @SerializedName(value = "pt-PT")
            public String pt_PT; // 葡萄语

            @SerializedName(value = "hi-IN")
            public String hi_IN; // 印地语

            @SerializedName(value = "bn-BD")
            public String bn_BD; // 孟加拉语

            @SerializedName(value = "tl-PH")
            public String tl_PH; // 塔加路语(菲律宾)

            @SerializedName(value = "fa-IR")
            public String fa_IR; // 波斯语(伊朗)

            @SerializedName(value = "ru-RU")
            public String ru_RU; // 俄罗斯语
        }

        public static class MGCommonPublicMessageMsgUser implements Serializable {
            // 默认内容
            public String defaultStr;
            // 用户名称
            public String name;
            // 用户id
            public String uid;
            // 颜色值
            public String color;
        }
    }

    /**
     * 2. 关键词状态
     */
    public static final String MG_COMMON_KEY_WORD_TO_HIT = "mg_common_key_word_to_hit";

    /**
     * 2. 关键词状态
     */
    public static class MGCommonKeyWordToHit implements Serializable {
        // 必填字段；text:文本包含匹配; number:数字等于匹配(必填字段)；默认:text（你画我猜、你说我猜）；数字炸弹填number；
        public String wordType;

        // 单个关键词，兼容老版本。轮到自己猜词时才有值，否则为null
        public String word;

        // 关键词，每一轮都会下发，不区分角色
        public String realWord;

        // 必填字段；关键词列表，可以传送多个关键词。轮到自己猜词时才有值，否则为null
        public List<String> wordList;

        // 必填字段；关键词语言，默认:zh-CN；
        public String wordLanguage;
    }

    /**
     * 3. 游戏结算状态
     */
    public static final String MG_COMMON_GAME_SETTLE = "mg_common_game_settle";

    /**
     * 3. 游戏结算状态
     */
    public static class MGCommonGameSettle implements Serializable {
        // 游戏模式默认为1
        public int gameMode;

        // 本局游戏的id
        public String gameRoundId;

        // 游戏结果玩家列表
        public List<PlayerResult> results;

        /**
         * 游戏结果玩家定义
         */
        public static class PlayerResult implements Serializable {
            public String uid; // 用户id
            public int rank; // 排名 从 1 开始
            public int award; // 奖励
            public int score; // 积分
            public int isEscaped; // 是否逃跑 1：逃跑 0：非逃跑
            public String killerId; // 杀自己的玩家的id
            public int isAI; // 是否是AI玩家，1为AI
        }
    }

    /**
     * 4. 加入游戏按钮点击状态
     */
    public static final String MG_COMMON_SELF_CLICK_JOIN_BTN = "mg_common_self_click_join_btn";

    /**
     * 4. 加入游戏按钮点击状态 模型
     * 用户（本人）点击加入按钮，或者点击头像加入
     */
    public static class MGCommonSelfClickJoinBtn implements Serializable {
        // 点击头像加入游戏对应的座位号，int 类型，从0开始， 如果seatIndex=-1，则是随机加入一个空位，如果seatIndex 大于座位数，则加入不成功
        public int seatIndex;
    }

    /**
     * 5. 取消加入(退出)游戏按钮点击状态
     */
    public static final String MG_COMMON_SELF_CLICK_CANCEL_JOIN_BTN = "mg_common_self_click_cancel_join_btn";

    /**
     * 5. 取消加入(退出)游戏按钮点击状态 模型
     * 用户（本人）点击取消加入按钮
     */
    public static class MGCommonSelfClickCancelJoinBtn implements Serializable {
    }

    /**
     * 6. 准备按钮点击状态
     */
    public static final String MG_COMMON_SELF_CLICK_READY_BTN = "mg_common_self_click_ready_btn";

    /**
     * 6. 准备按钮点击状态 模型
     */
    public static class MGCommonSelfClickReadyBtn implements Serializable {
    }

    /**
     * 7. 取消准备按钮点击状态
     */
    public static final String MG_COMMON_SELF_CLICK_CANCEL_READY_BTN = "mg_common_self_click_cancel_ready_btn";

    /**
     * 7. 取消准备按钮点击状态 模型
     */
    public static class MGCommonSelfClickCancelReadyBtn implements Serializable {
    }

    /**
     * 8. 开始游戏按钮点击状态
     */
    public static final String MG_COMMON_SELF_CLICK_START_BTN = "mg_common_self_click_start_btn";

    /**
     * 8. 开始游戏按钮点击状态 模型
     */
    public static class MGCommonSelfClickStartBtn implements Serializable {
    }

    /**
     * 9. 分享按钮点击状态
     */
    public static final String MG_COMMON_SELF_CLICK_SHARE_BTN = "mg_common_self_click_share_btn";

    /**
     * 9. 分享按钮点击状态 模型
     * 用户（本人）点击分享按钮
     */
    public static class MGCommonSelfClickShareBtn implements Serializable {
    }

    /**
     * 10. 游戏状态
     */
    public static final String MG_COMMON_GAME_STATE = "mg_common_game_state";

    /**
     * 10. 游戏状态 模型
     */
    public static class MGCommonGameState implements Serializable {
        public static final int UNKNOW = -1; // 未知
        public static final int IDLE = 0;
        public static final int LOADING = 1;
        public static final int PLAYING = 2;

        // gameState=0 (idle 状态，游戏未开始，空闲状态）；
        // gameState=1（loading 状态，所有玩家都准备好，队长点击了开始游戏按钮，等待加载游戏场景开始游戏，游戏即将开始提示阶段）；
        // gameState=2（playing状态，游戏进行中状态）
        public int gameState;
    }

    /**
     * 11. 结算界面关闭按钮点击状态（2021-12-27新增）
     */
    public static final String MG_COMMON_SELF_CLICK_GAME_SETTLE_CLOSE_BTN = "mg_common_self_click_game_settle_close_btn";

    /**
     * 11. 结算界面关闭按钮点击状态（2021-12-27新增） 模型
     * 用户（本人）点击结算界面关闭按钮
     */
    public static class MGCommonSelfClickGameSettleCloseBtn implements Serializable {
    }

    /**
     * 12. 结算界面再来一局按钮点击状态（2021-12-27新增）
     */
    public static final String MG_COMMON_SELF_CLICK_GAME_SETTLE_AGAIN_BTN = "mg_common_self_click_game_settle_again_btn";

    /**
     * 12. 结算界面再来一局按钮点击状态（2021-12-27新增）模型
     * 用户（本人）点击结算界面再来一局按钮
     */
    public static class MGCommonSelfClickGameSettleAgainBtn implements Serializable {
    }

    /**
     * 13. 游戏上报游戏中的声音列表（2021-12-30新增，现在只支持碰碰我最强）
     */
    public static final String MG_COMMON_GAME_SOUND_LIST = "mg_common_game_sound_list";

    /**
     * 13. 游戏上报游戏中的声音列表（2021-12-30新增，现在只支持碰碰我最强） 模型
     * 游戏上报本游戏中所有的声音资源列表
     */
    public static class MGCommonGameSoundList implements Serializable {
        // 声音资源列表
        public List<MGCommonGameSound> list;

        public static class MGCommonGameSound implements Serializable {
            // 声音资源的名字
            public String name;
            // 声音资源的URL链接
            public String url;
            // 声音资源类型
            public String type;
        }
    }

    /**
     * 14. 游通知app层播放声音（2021-12-30新增，现在只支持碰碰我最强）
     */
    public static final String MG_COMMON_GAME_SOUND = "mg_common_game_sound";

    /**
     * 14. 游通知app层播放声音（2021-12-30新增，现在只支持碰碰我最强） 模型
     * 游戏通知app层播放背景音乐的开关状态
     */
    public static class MGCommonGameSound implements Serializable {
        // 是否播放 isPlay==true(播放)，isPlay==false(停止)
        public boolean isPlay;
        // 要播放的声音文件名，不带后缀
        public String name;
        // 声音资源类型
        public String type;
        // 播放次数；注：times == 0 为循环播放
        public String times;
        // https://www.xxxx.xx/xxx.mp3"  声音资源的url链接
        public String url;
    }

    /**
     * 15. 游戏通知app层播放背景音乐状态（2022-01-07新增，现在只支持碰碰我最强）
     */
    public static final String MG_COMMON_GAME_BG_MUSIC_STATE = "mg_common_game_bg_music_state";

    /**
     * 15. 游戏通知app层播放背景音乐状态（2022-01-07新增，现在只支持碰碰我最强） 模型
     * 游戏通知app层播放背景音乐的开关状态
     */
    public static class MGCommonGameBgMusicState implements Serializable {
        // 背景音乐的开关状态 true: 开，false: 关
        public boolean state;
    }

    /**
     * 16. 游戏通知app层播放音效的状态（2022-01-07新增，现在只支持碰碰我最强）
     */
    public static final String MG_COMMON_GAME_SOUND_STATE = "mg_common_game_sound_state";

    /**
     * 16. 游戏通知app层播放音效的状态（2022-01-07新增，现在只支持碰碰我最强） 模型
     * 游戏通知app层播放音效的状态
     */
    public static class MGCommonGameSoundState implements Serializable {
        // 背景音乐的开关状态 true: 开，false: 关
        public boolean state;
    }

    /**
     * 17. ASR状态(开启和关闭语音识别状态，v1.1.45.xx 版本新增)
     */
    public static final String MG_COMMON_GAME_ASR = "mg_common_game_asr";

    /**
     * 17. ASR状态(开启和关闭语音识别状态，v1.1.45.xx 版本新增) 模型
     */
    public static class MGCommonGameASR implements Serializable {
        // true:打开语音识别 false:关闭语音识别
        public boolean isOpen;
    }

    /**
     * 18. 麦克风状态（2022-02-08新增）
     */
    public static final String MG_COMMON_SELF_MICROPHONE = "mg_common_self_microphone";

    /**
     * 18. 麦克风状态（2022-02-08新增） 模型
     * 游戏通知app麦克风状态
     */
    public static class MGCommonSelfMicrophone implements Serializable {
        // 麦克风开关状态 true: 开，false: 关
        public boolean isOn;
    }

    /**
     * 19. 耳机（听筒，扬声器）状态（2022-02-08新增）
     */
    public static final String MG_COMMON_SELF_HEADPHONE = "mg_common_self_headphone";

    /**
     * 19. 耳机（听筒，扬声器）状态（2022-02-08新增） 模型
     */
    public static class MGCommonSelfHeadphone implements Serializable {
        // 耳机（听筒，喇叭）开关状态 true: 开，false: 关
        public boolean isOn;
    }

    /**
     * 20. App通用状态操作结果错误码（2022-05-10新增）
     */
    public static final String MG_COMMON_APP_COMMON_SELF_X_RESP = "mg_common_app_common_self_x_resp";

    /**
     * 20. App通用状态操作结果错误码（2022-05-10新增） 模型
     */
    public static class MGCommonAPPCommonSelfXResp implements Serializable {
        public String state; // 字段必填, 参考：游戏业务错误 https://docs.sud.tech/zh-CN/app/Client/APPFST/CommonState.html
        public int resultCode; // 字段必填，参考：游戏业务错误 https://docs.sud.tech/zh-CN/app/Server/ErrorCode.html
        public boolean isIn; // 当state=app_common_self_in时，字段必填
        public boolean isReady; // 当state=app_common_self_ready时，字段必填
        public boolean isPlaying; // 当state=app_common_self_playing时，字段必填
        public String reportGameInfoExtras; // 当state=app_common_self_playing时，字段必填
        public String curCaptainUID; // 当state=app_common_self_captain时，字段必填
        public String kickedUID; // 当state=app_common_self_kick时，字段必填
    }

    /**
     * 21. 游戏通知app层添加陪玩机器人是否成功（2022-05-17新增）
     */
    public static final String MG_COMMON_GAME_ADD_AI_PLAYERS = "mg_common_game_add_ai_players";

    /**
     * 21. 游戏通知app层添加陪玩机器人是否成功（2022-05-17新增） 模型
     */
    public static class MGCommonGameAddAIPlayers implements Serializable {
        public int resultCode; // 返回码 0：成功，非0：不成功
        public List<String> userIds; // 加入成功的playerId列表
    }

    /**
     * 22. 游戏通知app层添当前网络连接状态（2022-06-21新增）
     */
    public static final String MG_COMMON_GAME_NETWORK_STATE = "mg_common_game_network_state";

    /**
     * 22. 游戏通知app层添当前网络连接状态（2022-06-21新增） 模型
     */
    public static class MGCommonGameNetworkState implements Serializable {
        public int state; // 0:closed, 1: connected
    }

    /**
     * 23. 游戏通知app获取积分
     */
    public static final String MG_COMMON_GAME_GET_SCORE = "mg_common_game_get_score";

    /**
     * 23. 游戏通知app获取积分 模型
     */
    public static class MGCommonGameGetScore implements Serializable {
    }

    /**
     * 24. 游戏通知app带入积分
     */
    public static final String MG_COMMON_GAME_SET_SCORE = "mg_common_game_set_score";

    /**
     * 24. 游戏通知app带入积分 模型
     */
    public static class MGCommonGameSetScore implements Serializable {
        public String roundId; // 局id
        public long lastRoundScore; // 本人当前积分
        public long incrementalScore; // 充值积分
        public long totalScore; // 充值后总积分
    }

    /**
     * 25. 创建订单
     */
    public static final String MG_COMMON_GAME_CREATE_ORDER = "mg_common_game_create_order";

    /**
     * 25. 创建订单 模型
     */
    public static class MGCommonGameCreateOrder implements Serializable {
        public String cmd; // 触发的行为动作，比如打赏，购买等
        public String fromUid; // 付费用户uid
        public String toUid; // 目标用户uid
        public long value; // 所属的游戏价值
        public String payload; // 扩展数据 json 字符串, 特殊可选
    }

    /**
     * 26. 游戏通知app玩家角色(仅对狼人杀有效)
     */
    public static final String MG_COMMON_PLAYER_ROLE_ID = "mg_common_player_role_id";

    /**
     * 26. 游戏通知app玩家角色(仅对狼人杀有效) 模型
     */
    public static class MGCommonPlayerRoleId implements Serializable {
        public List<MGCommonPlayerModel> playersRoleId; // 列表

        public static class MGCommonPlayerModel implements Serializable {
            public String uid; // 玩家id
            public int roleId; // 角色id
        }
    }

    /**
     * 27. 游戏通知app玩家被扔便便(你画我猜，你说我猜，友尽闯关有效)
     */
    public static final String MG_COMMON_SELF_CLICK_POOP = "mg_common_self_click_poop";

    /**
     * 27. 游戏通知app玩家被扔便便(你画我猜，你说我猜，友尽闯关有效) 模型
     */
    public static class MGCommonSelfClickPoop implements Serializable {
    }

    /**
     * 28. 游戏通知app玩家被点赞(你画我猜，你说我猜，友尽闯关有效)
     */
    public static final String MG_COMMON_SELF_CLICK_GOOD = "mg_common_self_click_good";

    /**
     * 28. 游戏通知app玩家被点赞(你画我猜，你说我猜，友尽闯关有效) 模型
     */
    public static class MGCommonSelfClickGood implements Serializable {
    }

    /**
     * 29. 游戏通知app游戏FPS(仅对碰碰，多米诺骨牌，飞镖达人生效)
     */
    public static final String MG_COMMON_GAME_FPS = "mg_common_game_fps";

    /**
     * 29. 游戏通知app游戏FPS(仅对碰碰，多米诺骨牌，飞镖达人生效) 模型
     */
    public static class MGCommonGameFps implements Serializable {
        public int fps;
    }

    /**
     * 30. 游戏通知app游戏弹框
     */
    public static final String MG_COMMON_ALERT = "mg_common_alert";

    /**
     * 30. 游戏通知app游戏弹框 模型
     */
    public static class MGCommonAlert implements Serializable {
        public String state; // show:显示，close:关闭
    }

    /**
     * 31. 游戏通知app最坑队友（只支持友尽闯关）
     */
    public static final String MG_COMMON_WORST_TEAMMATE = "mg_common_worst_teammate";

    /**
     * 31. 游戏通知app最坑队友（只支持友尽闯关） 模型
     */
    public static class MGCommonWorstTeammate implements Serializable {
        public String uid; // 最坑队友的uid
    }

    /**
     * 32. 游戏通知app因玩家逃跑导致游戏结束（只支持友尽闯关）
     */
    public static final String MG_COMMON_GAME_OVER_TIP = "mg_common_game_over_tip";

    /**
     * 32. 游戏通知app因玩家逃跑导致游戏结束（只支持友尽闯关） 模型
     */
    public static class MGCommonGameOverTip implements Serializable {
        public List<String> uids; // 逃跑玩家的uid数组
    }

    /**
     * 33. 游戏通知app玩家颜色（只支持友尽闯关）
     */
    public static final String MG_COMMON_GAME_PLAYER_COLOR = "mg_common_game_player_color";

    /**
     * 33. 游戏通知app玩家颜色（只支持友尽闯关） 模型
     */
    public static class MGCommonGamePlayerColor implements Serializable {
        public List<PlayerColorModel> players;

        public static class PlayerColorModel {
            public String uid; // 用户id
            public int color; // color:1是粉色，2是紫色，3是绿色，4是蓝色，5是黄色，6是橙色
        }
    }

    /**
     * 34. 游戏通知app玩家头像的坐标（只支持ludo）
     */
    public static final String MG_COMMON_GAME_PLAYER_ICON_POSITION = "mg_common_game_player_icon_position";

    /**
     * 34. 游戏通知app玩家头像的坐标（只支持ludo） 模型
     */
    public static class MGCommonGamePlayerIconPosition implements Serializable {
        public String uid;
        public PlayerIconPositionModel position;

        public static class PlayerIconPositionModel {
            // 头像坐标和宽高，坐标为头像中心
            public double x;
            public double y;
            public double width;
            public double height;
        }
    }

    /**
     * 35. 游戏通知app退出游戏（只支持teenpattipro 与 德州pro）
     */
    public static final String MG_COMMON_SELF_CLICK_EXIT_GAME_BTN = "mg_common_self_click_exit_game_btn";

    /**
     * 35. 游戏通知app退出游戏（只支持teenpattipro 与 德州pro） 模型
     */
    public static class MGCommonSelfClickExitGameBtn implements Serializable {
    }

    /**
     * 36. 游戏通知app是否要开启带入积分（只支持teenpattipro 与 德州pro）
     */
    public static final String MG_COMMON_GAME_IS_APP_CHIP = "mg_common_game_is_app_chip";

    /**
     * 36. 游戏通知app是否要开启带入积分（只支持teenpattipro 与 德州pro） 模型
     */
    public static class MGCommonGameIsAppChip implements Serializable {
        public int isAppChip; // 0:不开启，1：开启
    }

    /**
     * 37. 游戏通知app当前游戏的设置信息（只支持德州pro，teenpatti pro）
     */
    public static final String MG_COMMON_GAME_RULE = "mg_common_game_rule";

    /**
     * 37. 游戏通知app当前游戏的设置信息（只支持德州pro，teenpatti pro） 模型
     */
    public static class MGCommonGameRule implements Serializable {
        public GameRuleModel gameMode;

        // 德州与teenpatti的结构融合在一起
        public static class GameRuleModel {
            public Integer smallBlind; // 小盲
            public Integer ante; // 前注
            public Integer isStraddle; // 0：关闭，1自由，2强制
            public Integer sBuyIn; // 带入值/最小带入配置
            public Integer bBuyIn; // 最大带入，无限（0）
            public Integer isAutoStart; // 是否自动开始
            public Double tableDuration; // 牌桌时长配置（小时）
            public Integer thinkTime; // 思考时间（秒）
            public Integer darkCard; // 暗牌回合
            public Integer potLimit; // 最大带入
            public Integer round; // 最大回合
            public Integer singleLimit; // 单注限
        }
    }

    /**
     * 38. 游戏通知app进行玩法设置（只支持德州pro，teenpatti pro）
     */
    public static final String MG_COMMON_GAME_SETTINGS = "mg_common_game_settings";

    /**
     * 38. 游戏通知app进行玩法设置（只支持德州pro，teenpatti pro） 模型
     */
    public static class MGCommonGameSettings implements Serializable {
    }

    /**
     * 39. 游戏通知app钱币不足（只支持德州pro，teenpatti pro）
     */
    public static final String MG_COMMON_GAME_MONEY_NOT_ENOUGH = "mg_common_game_money_not_enough";

    /**
     * 39. 游戏通知app钱币不足（只支持德州pro，teenpatti pro） 模型
     */
    public static class MGCommonGameMoneyNotEnough implements Serializable {
    }

    /**
     * 40. 游戏通知app下发定制ui配置表（只支持ludo）
     */
    public static final String MG_COMMON_GAME_UI_CUSTOM_CONFIG = "mg_common_game_ui_custom_config";

    /**
     * 40. 游戏通知app下发定制ui配置表（只支持ludo） 模型
     */
    public static class MGCommonGameUiCustomConfig implements Serializable {
    }

    /**
     * 41. 设置app提供给游戏可点击区域(赛车)
     */
    public static final String MG_COMMON_SET_CLICK_RECT = "mg_common_set_click_rect";

    /**
     * 41. 设置app提供给游戏可点击区域(赛车) 模型
     */
    public static class MGCommonSetClickRect implements Serializable {
        public List<InteractionClickRect> list; // 游戏的点击区域
    }

    /**
     * 42. 通知app提供对应uids列表玩家的数据(赛车)
     */
    public static final String MG_COMMON_USERS_INFO = "mg_common_users_info";

    /**
     * 42. 通知app提供对应uids列表玩家的数据(赛车) 模型
     */
    public static class MGCommonUsersInfo implements Serializable {
        public List<String> uids;
    }

    /**
     * 43. 通知app游戏前期准备完成(赛车)
     */
    public static final String MG_COMMON_GAME_PREPARE_FINISH = "mg_common_game_prepare_finish";

    /**
     * 43. 通知app游戏前期准备完成(赛车) 模型
     */
    public static class MGCommonGamePrepareFinish implements Serializable {
    }

    /**
     * 44. 通知app游戏主界面已显示(赛车)
     */
    public static final String MG_COMMON_SHOW_GAME_SCENE = "mg_common_show_game_scene";

    /**
     * 44. 通知app游戏主界面已显示(赛车) 模型
     */
    public static class MGCommonShowGameScene implements Serializable {
    }

    /**
     * 45. 通知app游戏主界面已隐藏(赛车)
     */
    public static final String MG_COMMON_HIDE_GAME_SCENE = "mg_common_hide_game_scene";

    /**
     * 45. 通知app游戏主界面已隐藏(赛车) 模型
     */
    public static class MGCommonHideGameScene implements Serializable {
    }

    /**
     * 46. 通知app点击了游戏的金币按钮(赛车)
     */
    public static final String MG_COMMON_SELF_CLICK_GOLD_BTN = "mg_common_self_click_gold_btn";

    /**
     * 46. 通知app点击了游戏的金币按钮(赛车) 模型
     */
    public static class MGCommonSelfClickGoldBtn implements Serializable {
    }

    /**
     * 47. 通知app棋子到达终点(ludo)
     */
    public static final String MG_COMMON_GAME_PIECE_ARRIVE_END = "mg_common_game_piece_arrive_end";

    /**
     * 47. 通知app棋子到达终点(ludo) 模型
     */
    public static class MGCommonGamePieceArriveEnd implements Serializable {
        public String uid; // 玩家id
        public int pieceIndex; // 棋子编号 0 ~ 3
    }

    /**
     * 48. 通知app玩家是否托管
     */
    public static final String MG_COMMON_GAME_PLAYER_MANAGED_STATE = "mg_common_game_player_managed_state";

    /**
     * 48. 通知app玩家是否托管 模型
     */
    public static class MGCommonGamePlayerManagedState implements Serializable {
        public String uid; // 玩家id
        public int managed; // 0: 未托管 1：托管
    }

    /**
     * 49. 游戏向app发送爆词
     */
    public static final String MG_COMMON_GAME_SEND_BURST_WORD = "mg_common_game_send_burst_word";

    /**
     * 49. 游戏向app发送爆词 模型
     */
    public static class MGCommonGameSendBurstWord implements Serializable {
        public String text; // 爆词
    }

    /**
     * 50. 游戏向app发送玩家实时排名（只支持怪物消消乐）
     */
    public static final String MG_COMMON_GAME_PLAYER_RANKS = "mg_common_game_player_ranks";

    /**
     * 50. 游戏向app发送玩家实时排名（只支持怪物消消乐） 模型
     */
    public static class MGCommonGamePlayerRanks implements Serializable {
        public List<RanksModel> ranks; // 所有玩家排名变化推送

        public static class RanksModel implements Serializable {
            public String uid; // 用户id
            public int rank; // 排名
        }
    }

    /**
     * 51. 游戏向app发送玩家即时变化的单双牌（只支持okey101）
     */
    public static final String MG_COMMON_GAME_PLAYER_PAIR_SINGULAR = "mg_common_game_player_pair_singular";

    /**
     * 51. 游戏向app发送玩家即时变化的单双牌（只支持okey101） 模型
     */
    public static class MGCommonGamePlayerPairSingular implements Serializable {
        public List<SingularModel> pairs; // 玩家单双牌推送

        public static class SingularModel implements Serializable {
            public String uid; // 用户id
            public int pair; // pair: 1 双，0 单
        }
    }

    /**
     * 52. 游戏向app发送获取玩家持有的道具卡（只支持大富翁）
     */
    public static final String MG_COMMON_GAME_PLAYER_MONOPOLY_CARDS = "mg_common_game_player_monopoly_cards";

    /**
     * 52. 游戏向app发送获取玩家持有的道具卡（只支持大富翁） 模型
     */
    public static class MGCommonGamePlayerMonopolyCards implements Serializable {
    }

    /**
     * 53. 游戏向app发送玩家实时积分（只支持怪物消消乐）
     */
    public static final String MG_COMMON_GAME_PLAYER_SCORES = "mg_common_game_player_scores";

    /**
     * 53. 游戏向app发送玩家实时积分（只支持怪物消消乐） 模型
     */
    public static class MGCommonGamePlayerScores implements Serializable {
        public List<ScoresModel> scores; // 所有玩家积分变化推送

        public static class ScoresModel implements Serializable {
            public String uid; // 用户id
            public int score; // 积分
        }
    }

    /**
     * 54. 游戏通知app销毁游戏（只支持部分概率类游戏）
     */
    public static final String MG_COMMON_DESTROY_GAME_SCENE = "mg_common_destroy_game_scene";

    /**
     * 54. 游戏通知app销毁游戏（只支持部分概率类游戏） 模型
     */
    public static class MGCommonDestroyGameScene implements Serializable {
    }

    /**
     * 55. 游戏通知app击球状态（只支持桌球）
     */
    public static final String MG_COMMON_GAME_BILLIARDS_HIT_STATE = "mg_common_game_billiards_hit_state";

    /**
     * 55. 游戏通知app击球状态（只支持桌球） 模型
     */
    public static class MGCommonGameBilliardsHitState implements Serializable {
        public String uid; // 操作玩家的id
        /**
         * state状态说明:
         * 0: 母球击空或者第一击没击中目标球
         * 2-7: 连杆
         * 8: 白球进洞
         * 9: 没有足够的撞库数量
         * 10: 开球时进了黑八
         * 11: 提前进了黑八
         * 12: 没有有效进球
         * 13: 有有效进球
         * 14: 超时
         */
        public int state;
    }

    /**
     * 56. 游戏向app发送获取玩家持有的指定点数道具卡（只支持飞行棋）
     */
    public static final String MG_COMMON_GAME_PLAYER_PROPS_CARDS = "mg_common_game_player_props_cards";

    /**
     * 56. 游戏向app发送获取玩家持有的指定点数道具卡（只支持飞行棋） 模型
     */
    public static class MGCommonGamePlayerPropsCards implements Serializable {
    }

    // endregion 通用状态-游戏

    // region MG状态机-通用状态-玩家
    // 参考：https://docs.sud.tech/zh-CN/app/Client/MGFSM/CommonStatePlayer.html

    /**
     * 1.加入状态（已修改）
     * 最低版本: v1.1.30.xx
     */
    public static final String MG_COMMON_PLAYER_IN = "mg_common_player_in";

    /**
     * 1.加入状态（已修改） 模型
     * 用户是否加入游戏；
     * 游戏开始后，未加入的用户为OB视角。
     */
    public static class MGCommonPlayerIn implements Serializable {
        // true 已加入，false 未加入
        public boolean isIn;

        // 加入哪支队伍
        public int teamId;

        // 当isIn==false时有效；0 主动退出，1 被踢;（reason默认-1，无意义便于处理）
        public int reason;

        // 当reason==1时有效；kickUID为踢人的用户uid；判断被踢的人是本人条件(onPlayerStateChange(userId==kickedUID == selfUID)；（kickUID默认""，无意义便于处理）
        public String kickUID;
    }

    /**
     * 2.准备状态（已修改）
     * 最低版本: v1.1.30.xx
     */
    public static final String MG_COMMON_PLAYER_READY = "mg_common_player_ready";

    /**
     * 2.准备状态（已修改） 模型
     * 用户是否为队长，队长在游戏中会有开始游戏的权利。
     */
    public static class MGCommonPlayerReady implements Serializable {
        // 当retCode==0时有效；true 已准备，false 未准备
        public boolean isReady;
    }

    /**
     * 3.队长状态（已修改）
     * 最低版本: v1.1.30.xx
     */
    public static final String MG_COMMON_PLAYER_CAPTAIN = "mg_common_player_captain";

    /**
     * 3.队长状态（已修改） 模型
     * 用户是否为队长，队长在游戏中会有开始游戏的权利。
     */
    public static class MGCommonPlayerCaptain implements Serializable {
        // true 是队长，false 不是队长；
        public boolean isCaptain;
    }

    /**
     * 4.游戏状态（已修改）
     * 最低版本: v1.1.30.xx
     */
    public static final String MG_COMMON_PLAYER_PLAYING = "mg_common_player_playing";

    /**
     * 4.游戏状态（已修改）模型
     * 用户游戏状态，如果用户在游戏中，建议：
     * a.空出屏幕中心区：
     * 关闭全屏礼物特效；
     * b.部分强操作类小游戏（spaceMax为true），尽量收缩原生UI，给游戏留出尽量大的操作空间：
     * 收缩公屏；
     * 收缩麦位；
     * 如果不在游戏中，则恢复。
     */
    public static class MGCommonPlayerPlaying implements Serializable {
        // true 游戏中，false 未在游戏中；
        public boolean isPlaying;
        // 本轮游戏id，当isPlaying==true时有效
        public String gameRoundId;
        // 当isPlaying==false时有效；isPlaying=false, 0:正常结束 1:提前结束（自己不玩了）2:无真人可以提前结束（无真人，只有机器人） 3:所有人都提前结束；（reason默认-1，无意义便于处理）
        public int reason;
        // true 建议尽量收缩原生UI，给游戏留出尽量大的操作空间 false 初始状态；
        public Boolean spaceMax;
    }

    /**
     * 5.玩家在线状态
     */
    public static final String MG_COMMON_PLAYER_ONLINE = "mg_common_player_online";

    /**
     * 5.玩家在线状态 模型
     */
    public static class MGCommonPlayerOnline implements Serializable {
        // true：在线，false： 离线
        public boolean isOnline;
    }

    /**
     * 6.玩家换游戏位状态
     */
    public static final String MG_COMMON_PLAYER_CHANGE_SEAT = "mg_common_player_change_seat";

    /**
     * 6.玩家换游戏位状态 模型
     */
    public static class MGCommonPlayerChangeSeat implements Serializable {
        // 换位前的游戏位(座位号)
        public int preSeatIndex;
        // 换位成功后的游戏位(座位号)
        public int currentSeatIndex;
    }

    /**
     * 7. 游戏通知app点击玩家头像
     */
    public static final String MG_COMMON_SELF_CLICK_GAME_PLAYER_ICON = "mg_common_self_click_game_player_icon";

    /**
     * 7. 游戏通知app点击玩家头像 模型
     */
    public static class MGCommonSelfClickGamePlayerIcon implements Serializable {
        // 被点击头像的用户id
        public String uid;
    }

    /**
     * 8. 游戏通知app玩家死亡状态（2022-04-24新增）
     */
    public static final String MG_COMMON_SELF_DIE_STATUS = "mg_common_self_die_status";

    /**
     * 8. 游戏通知app玩家死亡状态（2022-04-24新增）模型
     */
    public static class MGCommonSelfDieStatus implements Serializable {
        public String uid; // 用户id
        public boolean isDeath; // 玩家是否死亡 true:死亡, false: 未死亡；默认 false
    }

    /**
     * 9. 游戏通知app轮到玩家出手状态（2022-04-24新增）
     */
    public static final String MG_COMMON_SELF_TURN_STATUS = "mg_common_self_turn_status";

    /**
     * 9. 游戏通知app轮到玩家出手状态（2022-04-24新增）模型
     */
    public static class MGCommonSelfTurnStatus implements Serializable {
        public String uid; // 用户id
        public boolean isTurn; // 是否轮到玩家出手 true:是上面uid玩家的出手回合, false: 不是上面uid玩家的出手回合；默认false
    }

    /**
     * 10. 游戏通知app玩家选择状态（2022-04-24新增）
     */
    public static final String MG_COMMON_SELF_SELECT_STATUS = "mg_common_self_select_status";

    /**
     * 10. 游戏通知app玩家选择状态（2022-04-24新增）模型
     */
    public static class MGCommonSelfSelectStatus implements Serializable {
        public String uid; // 用户id
        public boolean isSelected; // 玩家是否选择 true:选择, false: 未选择； 默认false
    }

    /**
     * 11. 游戏通知app层当前游戏剩余时间（2022-05-23新增，目前UMO生效）
     */
    public static final String MG_COMMON_GAME_COUNTDOWN_TIME = "mg_common_game_countdown_time";

    /**
     * 11. 游戏通知app层当前游戏剩余时间（2022-05-23新增，目前UMO生效）模型
     */
    public static class MGCommonGameCountdownTime implements Serializable {
        public int countdown;// 剩余时间，单位为秒
    }

    /**
     * 12. 游戏通知app层当前玩家死亡后变成ob视角（2022-08-23新增，目前狼人杀生效）
     */
    public static final String MG_COMMON_SELF_OB_STATUS = "mg_common_self_ob_status";

    /**
     * 12. 游戏通知app层当前玩家死亡后变成ob视角（2022-08-23新增，目前狼人杀生效）模型
     */
    public static class MGCommonSelfObStatus implements Serializable {
        public boolean isOb;// 是否成为ob视角
    }

    // endregion 通用状态-玩家


    // region 碰碰我最强
    // endregion 碰碰我最强

    // region 飞刀达人
    // endregion 飞刀达人

    // region 你画我猜
    // 参考文档：https://docs.sud.tech/zh-CN/app/Client/MGFSM/DrawGuess.html

    /**
     * 1. 选词中状态（已修改）
     */
    public static final String MG_DG_SELECTING = "mg_dg_selecting";

    /**
     * 1. 选词中状态（已修改） 模型
     * 选词中，头像正下方
     */
    public static class MGDGSelecting implements Serializable {
        // bool 类型 true：正在选词中，false: 不在选词中
        public boolean isSelecting;
    }

    /**
     * 2. 作画中状态（已修改）
     */
    public static final String MG_DG_PAINTING = "mg_dg_painting";

    /**
     * 2. 作画中状态（已修改） 模型
     * 作画中，头像正下方
     */
    public static class MGDGPainting implements Serializable {
        // true: 绘画中，false: 取消绘画
        public boolean isPainting;
    }

    /**
     * 3. 显示错误答案状态（已修改）
     */
    public static final String MG_DG_ERRORANSWER = "mg_dg_erroranswer";

    /**
     * 3. 显示错误答案状态（已修改） 模型
     * 错误的答案，最多6中文，头像正下方
     */
    public static class MGDGErroranswer implements Serializable {
        // 字符串类型，展示错误答案
        public String msg;
    }

    /**
     * 4. 显示总积分状态（已修改）
     */
    public static final String MG_DG_TOTALSCORE = "mg_dg_totalscore";

    /**
     * 4. 显示总积分状态（已修改） 模型
     * 总积分，位于头像右上角
     */
    public static class MGDGTotalscore implements Serializable {
        // 字符串类型 总积分
        public String msg;
    }

    /**
     * 5. 本次获得积分状态（已修改）
     */
    public static final String MG_DG_SCORE = "mg_dg_score";

    /**
     * 5. 本次获得积分状态（已修改） 模型
     * 本次积分，头像正下方
     */
    public static final class MGDGScore implements Serializable {
        // string类型，展示本次获得积分
        public String msg;
    }

    // endregion 你画我猜

    // region 元宇宙砂砂舞
    /**
     * 1. 元宇宙砂砂舞指令回调
     */
    public static final String MG_COMMON_GAME_DISCO_ACTION = "mg_common_game_disco_action";

    /**
     * 1. 元宇宙砂砂舞指令回调 模型
     * app指令请求游戏客户端成功与否的回调
     */
    public static final class MGCommonGameDiscoAction implements Serializable {
        public int actionId; // 指令序号类型
        public boolean isSuccess; // true 指令成功，false 指令失败
    }

    /**
     * 2. 元宇宙砂砂舞指令动作结束通知
     */
    public static final String MG_COMMON_GAME_DISCO_ACTION_END = "mg_common_game_disco_action_end";

    /**
     * 2. 元宇宙砂砂舞指令动作结束通知 模型
     * 游戏客户端通知APP指令动作结束
     */
    public static final class MGCommonGameDiscoActionEnd implements Serializable {
        public int actionId; // 指令序号类型
        public String playerId; // // 玩家ID string 类型
    }
    // endregion 元宇宙砂砂舞

    // region 定制火箭
    /**
     * 1. 礼物配置文件(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_CONFIG = "mg_custom_rocket_config";

    /**
     * 1. 礼物配置文件(火箭) 模型
     */
    public static final class MGCustomRocketConfig implements Serializable {
    }

    /**
     * 2. 拥有模型列表(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_MODEL_LIST = "mg_custom_rocket_model_list";

    /**
     * 2. 拥有模型列表(火箭) 模型
     */
    public static final class MGCustomRocketModelList implements Serializable {
    }

    /**
     * 3. 拥有组件列表(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_COMPONENT_LIST = "mg_custom_rocket_component_list";

    /**
     * 3. 拥有组件列表(火箭) 模型
     */
    public static final class MGCustomRocketComponentList implements Serializable {
    }

    /**
     * 4. 获取用户信息(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_USER_INFO = "mg_custom_rocket_user_info";

    /**
     * 4. 获取用户信息 模型
     */
    public static final class MGCustomRocketUserInfo implements Serializable {
        public List<String> userIdList;
    }

    /**
     * 5. 订单记录列表(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_ORDER_RECORD_LIST = "mg_custom_rocket_order_record_list";

    /**
     * 5. 订单记录列表(火箭) 模型
     */
    public static final class MGCustomRocketOrderRecordList implements Serializable {
        public int pageIndex; // 第几页
        public int pageSize; // 每页多少条数据
    }

    /**
     * 6. 展馆内列表(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_ROOM_RECORD_LIST = "mg_custom_rocket_room_record_list";

    /**
     * 6. 展馆内列表(火箭) 模型
     */
    public static final class MGCustomRocketRoomRecordList implements Serializable {
        public int pageIndex; // 第几页
        public int pageSize; // 每页多少条数据
    }

    /**
     * 7. 展馆内玩家送出记录(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_USER_RECORD_LIST = "mg_custom_rocket_user_record_list";

    /**
     * 7. 展馆内玩家送出记录(火箭) 模型
     */
    public static final class MGCustomRocketUserRecordList implements Serializable {
        public String userId; // 用户id
        public int pageIndex; // 第几页
        public int pageSize; // 每页多少条数据
    }

    /**
     * 8. 设置默认模型(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_SET_DEFAULT_MODEL = "mg_custom_rocket_set_default_model";

    /**
     * 8. 设置默认模型(火箭) 模型
     */
    public static final class MGCustomRocketSetDefaultModel implements Serializable {
        public String modelId; // 默认模型
    }

    /**
     * 9. 动态计算一键发送价格(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_DYNAMIC_FIRE_PRICE = "mg_custom_rocket_dynamic_fire_price";

    /**
     * 9. 动态计算一键发送价格(火箭) 模型
     */
    public static final class MGCustomRocketDynamicFirePrice implements Serializable {
        public List<ComponentModel> componentList; // 组件列表

        public static class ComponentModel {
            public String itemId; // 已购买的唯一标识
        }
    }

    /**
     * 10. 一键发送(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_FIRE_MODEL = "mg_custom_rocket_fire_model";

    /**
     * 10. 一键发送(火箭) 模型
     */
    public static final class MGCustomRocketFireModel implements Serializable {
        public List<ComponentModel> componentList; // 组件列表

        public static class ComponentModel {
            public int type; // 类型
            public String itemId; // 已购买的唯一标识
        }
    }

    /**
     * 11. 新组装模型(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_CREATE_MODEL = "mg_custom_rocket_create_model";

    /**
     * 11. 新组装模型(火箭) 模型
     */
    public static final class MGCustomRocketCreateModel implements Serializable {
        public List<ComponentModel> componentList; // 组件列表

        public static class ComponentModel {
            public String itemId; // 模型Id
        }
    }

    /**
     * 12. 模型更换组件(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_REPLACE_COMPONENT = "mg_custom_rocket_replace_component";

    /**
     * 12. 模型更换组件(火箭) 模型
     */
    public static final class MGCustomRocketReplaceComponent implements Serializable {
        public String modelId; // 模型ID
        public List<ComponentModel> componentList; // 组件列表

        public static class ComponentModel {
            public String itemId; // 已购买的唯一标识
        }
    }

    /**
     * 13. 购买组件(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_BUY_COMPONENT = "mg_custom_rocket_buy_component";

    /**
     * 13. 购买组件(火箭) 模型
     */
    public static final class MGCustomRocketBuyComponent implements Serializable {
        public List<ComponentModel> componentList; // 组件列表

        public static class ComponentModel {
            public String componentId; // 已购买的唯一标识
            public String value; // 值
        }
    }

    /**
     * 14. 播放效果开始(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_PLAY_EFFECT_START = "mg_custom_rocket_play_effect_start";

    /**
     * 14. 播放效果开始(火箭) 模型
     */
    public static final class MGCustomRocketPlayEffectStart implements Serializable {
    }

    /**
     * 15. 播放效果完成(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_PLAY_EFFECT_FINISH = "mg_custom_rocket_play_effect_finish";

    /**
     * 15. 播放效果完成(火箭) 模型
     */
    public static final class MGCustomRocketPlayEffectFinish implements Serializable {
    }

    /**
     * 16. 验证签名合规(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_VERIFY_SIGN = "mg_custom_rocket_verify_sign";

    /**
     * 16. 验证签名合规(火箭) 模型
     */
    public static final class MGCustomRocketVerifySign implements Serializable {
        public String sign; // 验证的内容
    }

    /**
     * 17. 上传icon(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_UPLOAD_MODEL_ICON = "mg_custom_rocket_upload_model_icon";

    /**
     * 17. 上传icon(火箭) 模型
     */
    public static final class MGCustomRocketUploadModelIcon implements Serializable {
        public String data; // 图片base64数据
    }

    /**
     * 18. 前期准备完成(火箭)
     * 表示app此时可以向火箭发出指令了
     */
    public static final String MG_CUSTOM_ROCKET_PREPARE_FINISH = "mg_custom_rocket_prepare_finish";

    /**
     * 18. 前期准备完成(火箭) 模型
     */
    public static final class MGCustomRocketPrepareFinish implements Serializable {
    }

    /**
     * 19. 火箭主界面已显示(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_SHOW_GAME_SCENE = "mg_custom_rocket_show_game_scene";

    /**
     * 19. 火箭主界面已显示(火箭) 模型
     */
    public static final class MGCustomRocketShowGameScene implements Serializable {
    }

    /**
     * 20. 火箭主界面已隐藏(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_HIDE_GAME_SCENE = "mg_custom_rocket_hide_game_scene";

    /**
     * 20. 火箭主界面已隐藏(火箭) 模型
     */
    public static final class MGCustomRocketHideGameScene implements Serializable {
    }

    /**
     * 21. 点击锁住组件(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_CLICK_LOCK_COMPONENT = "mg_custom_rocket_click_lock_component";

    /**
     * 21. 点击锁住组件(火箭) 模型
     */
    public static final class MGCustomRocketClickLockComponent implements Serializable {
        public int type; // 组件类型
        public String componentId; // 组件ID
    }

    /**
     * 22. 火箭效果飞行点击(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_FLY_CLICK = "mg_custom_rocket_fly_click";

    /**
     * 22. 火箭效果飞行点击(火箭) 模型
     */
    public static final class MGCustomRocketFlyClick implements Serializable {
    }

    /**
     * 23. 火箭效果飞行结束(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_FLY_END = "mg_custom_rocket_fly_end";

    /**
     * 23. 火箭效果飞行结束(火箭) 模型
     */
    public static final class MGCustomRocketFlyEnd implements Serializable {
        public long clickNumber; // 点击多少次
        public long flyNumber; // 飞行多远
    }

    /**
     * 24. 设置点击区域(火箭)
     */
    public static final String MG_CUSTOM_ROCKET_SET_CLICK_RECT = "mg_custom_rocket_set_click_rect";

    /**
     * 24. 设置点击区域(火箭) 模型
     */
    public static final class MGCustomRocketSetClickRect implements Serializable {
        public List<InteractionClickRect> list; // 游戏的点击区域
    }

    /**
     * 25. 颜色和签名自定义改到装配间的模式，保存颜色或签名
     */
    public static final String MG_CUSTOM_ROCKET_SAVE_SIGN_COLOR = "mg_custom_rocket_save_sign_color";

    /**
     * 25. 颜色和签名自定义改到装配间的模式，保存颜色或签名 模型
     */
    public static final class MGCustomRocketSaveSignColor implements Serializable {
        public List<ComponentModel> componentList;

        public static class ComponentModel {
            public String componentId; // 组件的ID
            public String value; // 颜色值,采用十六进制
            public String modelId; // 模型id (更新模型时使用)
        }
    }
    // endregion 定制火箭

    // region 棒球
    /**
     * 1. 设置界面默认状态(棒球)
     */
    public static final String MG_BASEBALL_DEFUALT_STATE = "mg_baseball_defualt_state";

    /**
     * 1. 设置界面默认状态(棒球) 模型
     */
    public static final class MGBaseballDefaultState implements Serializable {
    }

    /**
     * 2. 前期准备完成(棒球)
     * 游戏客户端通知APP指令动作前期准备完成
     */
    public static final String MG_BASEBALL_PREPARE_FINISH = "mg_baseball_prepare_finish";

    /**
     * 2. 前期准备完成(棒球) 模型
     */
    public static final class MGBaseballPrepareFinish implements Serializable {
    }

    /**
     * 3. 主界面已显示(棒球)
     * 游戏客户端通知APP指令动作主界面已显示
     */
    public static final String MG_BASEBALL_SHOW_GAME_SCENE = "mg_baseball_show_game_scene";

    /**
     * 3. 主界面已显示(棒球) 模型
     */
    public static final class MGBaseballShowGameScene implements Serializable {
    }

    /**
     * 4. 主界面已隐藏(棒球)
     * 游戏客户端通知APP指令动作主界面已隐藏
     */
    public static final String MG_BASEBALL_HIDE_GAME_SCENE = "mg_baseball_hide_game_scene";

    /**
     * 4. 主界面已隐藏(棒球) 模型
     */
    public static final class MGBaseballHideGameScene implements Serializable {
    }

    /**
     * 5. 查询排行榜数据(棒球)
     * 游戏客户端通知APP查询排行榜数据
     */
    public static final String MG_BASEBALL_RANKING = "mg_baseball_ranking";

    /**
     * 5. 查询排行榜数据(棒球) 模型
     */
    public static final class MGBaseballRanking implements Serializable {
        public int page; // 页数
        public int size; // 每页显示的数量
    }

    /**
     * 6. 查询我的排名(棒球)
     * 游戏客户端通知APP查询我的排名
     */
    public static final String MG_BASEBALL_MY_RANKING = "mg_baseball_my_ranking";

    /**
     * 6. 查询我的排名(棒球) 模型
     */
    public static final class MGBaseballMyRanking implements Serializable {
    }

    /**
     * 7. 查询当前距离我的前后玩家数据(棒球)
     * 游戏客户端通知APP查询当前距离我的前后玩家数据（需要排除自己）
     */
    public static final String MG_BASEBALL_RANGE_INFO = "mg_baseball_range_info";

    /**
     * 7. 查询当前距离我的前后玩家数据(棒球) 模型
     */
    public static final class MGBaseballRangeInfo implements Serializable {
        public long distance; // 自己当前的距离
    }

    /**
     * 8. 设置app提供给游戏可点击区域(棒球)
     * 游戏客户端通知APP指令动作设置点击区域
     */
    public static final String MG_BASEBALL_SET_CLICK_RECT = "mg_baseball_set_click_rect";

    /**
     * 8. 设置app提供给游戏可点击区域(棒球) 模型
     */
    public static final class MGBaseballSetClickRect implements Serializable {
        public List<InteractionClickRect> list;
    }

    /**
     * 9. 获取文本配置数据(棒球)
     */
    public static final String MG_BASEBALL_TEXT_CONFIG = "mg_baseball_text_config";

    /**
     * 9. 获取文本配置数据(棒球) 模型
     */
    public static final class MGBaseballTextConfig implements Serializable {
    }

    /**
     * 10. 球落地, 通知距离(棒球)
     */
    public static final String MG_BASEBALL_SEND_DISTANCE = "mg_baseball_send_distance";

    /**
     * 10. 球落地, 通知距离(棒球) 模型
     */
    public static final class MGBaseballSendDistance implements Serializable {
        public int[] distances;
    }
    // endregion 棒球

    /** 点击区域定义 */
    public static class InteractionClickRect {
        public float x; // 区域的x
        public float y; // 区域的y
        public float width; // 区域的width
        public float height; // 区域的height
    }

    // region 3D语聊房
    /**
     * 1. 请求房间数据
     */
    public static final String MG_CUSTOM_CR_ROOM_INIT_DATA = "mg_custom_cr_room_init_data";

    /**
     * 1. 请求房间数据 模型
     */
    public static final class MGCustomCrRoomInitData implements Serializable {
    }

    /**
     * 2. 点击主播位或老板位通知
     */
    public static final String MG_CUSTOM_CR_CLICK_SEAT = "mg_custom_cr_click_seat";

    /**
     * 2. 点击主播位或老板位通知 模型
     */
    public static final class MGCustomCrClickSeat implements Serializable {
        public int seatIndex; // 0~4一共5个麦位，0为老板位，1~4为四个面主播位
    }
    // endregion 3D语聊房

}
