/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.state;

import java.io.Serializable;
import java.util.List;

/**
 * APP to MG 的通用状态定义
 * 参考文档：https://docs.sud.tech/zh-CN/app/Client/APPFST/
 */
public class SudMGPAPPState implements Serializable {

    // region 通用状态
    /**
     * 1. 加入状态
     * 最低版本: v1.1.30.xx
     */
    public static final String APP_COMMON_SELF_IN = "app_common_self_in";

    /**
     * 1. 加入状态 模型
     * 用户（本人）加入游戏/退出游戏
     * 正确流程：
     * 1.isIn=true: 加入游戏=>准备游戏=>开始游戏;
     * 2.isIn=false: 结束=>取消准备=>退出游戏;
     */
    public static class APPCommonSelfIn implements Serializable {
        // rue 加入游戏，false 退出游戏
        public boolean isIn;

        // 加入的游戏位(座位号) 默认传seatIndex = -1 随机加入，seatIndex 从0开始，不可大于座位数
        public int seatIndex;

        // 默认为ture, 带有游戏位(座位号)的时候，如果游戏位(座位号)已经被占用，是否随机分配一个空位坐下 isSeatRandom=true 随机分配空位坐下，isSeatRandom=false 不随机分配
        public boolean isSeatRandom;

        // 不支持分队的游戏：数值填1；支持分队的游戏：数值填1或2（两支队伍）；
        public int teamId;
    }

    /**
     * 2. 准备状态
     * 最低版本: v1.1.30.xx
     */
    public static final String APP_COMMON_SELF_READY = "app_common_self_ready";

    /**
     * 2. 准备状态 模型
     * 用户（本人）准备/取消准备
     */
    public static class APPCommonSelfReady implements Serializable {
        // true 准备，false 取消准备
        public boolean isReady;
    }

    /**
     * 3. 游戏状态
     * 最低版本: v1.1.30.xx
     */
    public static final String APP_COMMON_SELF_PLAYING = "app_common_self_playing";

    /**
     * 3. 游戏状态 模型
     * 用户游戏状态，如果用户在游戏中，建议：
     * a.空出屏幕中心区：
     * 关闭全屏礼物特效；
     * b.部分强操作类小游戏（spaceMax为true），尽量收缩原生UI，给游戏留出尽量大的操作空间：
     * 收缩公屏；
     * 收缩麦位；
     * 如果不在游戏中，则恢复。
     */
    public static class APPCommonSelfPlaying implements Serializable {
        // true 开始游戏，false 结束游戏
        public boolean isPlaying;

        // string类型，Https服务回调report_game_info参数，最大长度1024字节，超过则截断（2022-01-21）
        public String reportGameInfoExtras;

        // string类型，最大长度64字节，接入方服务端，可以根据这个字段来查询一局游戏的数据
        public String reportGameInfoKey;
    }

    /**
     * 4. 队长状态
     * 最低版本: v1.1.30.xx
     */
    public static final String APP_COMMON_SELF_CAPTAIN = "app_common_self_captain";

    /**
     * 4. 队长状态 模型
     * 用户是否为队长，队长在游戏中会有开始游戏的权利。
     */
    public static class APPCommonSelfCaptain implements Serializable {
        // 必填，指定队长uid
        public String curCaptainUID;
    }

    /**
     * 5. 踢人
     * v1.1.30.xx
     */
    public static final String APP_COMMON_SELF_KICK = "app_common_self_kick";

    /**
     * 5. 踢人 模型
     * 用户（本人，队长）踢其他玩家；
     * 队长才能踢人；
     */
    public static class APPCommonSelfKick implements Serializable {
        // 被踢用户uid
        public String kickedUID;
    }

    /**
     * 6. 结束游戏
     * v1.1.30.xx
     */
    public static final String APP_COMMON_SELF_END = "app_common_self_end";

    /**
     * 6. 结束游戏 模型
     * 用户（本人，队长）结束（本局）游戏
     */
    public static class APPCommonSelfEnd implements Serializable {
        // 当前不需要传参
    }

    /**
     * 7. 房间状态（depreated 已废弃v1.1.30.xx）
     */
    public static final String APP_COMMON_SELF_ROOM = "app_common_self_room";

    /**
     * 8. 麦位状态（depreated 已废弃v1.1.30.xx）
     */
    public static final String APP_COMMON_SELF_SEAT = "app_common_self_seat";

    /**
     * 9. 麦克风状态
     */
    public static final String APP_COMMON_SELF_MICROPHONE = "app_common_self_microphone";

    /**
     * 9. 麦克风状态 模型
     * 用户（本人）麦克风状态，建议：
     * 进入房间后初始通知一次；
     * 每次变更（开麦/闭麦/禁麦/解麦）通知一次；
     */
    public static class APPCommonSelfMicrophone implements Serializable {
        // true 开麦，false 闭麦
        public boolean isOn;

        // true 被禁麦，false 未被禁麦
        public boolean isDisabled;
    }

    /**
     * 10. 文字命中状态
     */
    public static final String APP_COMMON_SELF_TEXT_HIT = "app_common_self_text_hit";

    /**
     * 10. 文字命中状态 模型
     * 用户（本人）聊天信息命中关键词状态，建议：
     * 精确匹配；
     * 首次聊天内容命中关键词之后，后续聊天内容不翻转成未命中；
     * 直至小游戏侧关键词更新，再将状态翻转为未命中；
     */
    public static class APPCommonSelfTextHitState implements Serializable {
        // true 命中，false 未命中
        public boolean isHit;

        // 关键词
        public String keyWord;

        // 聊天原始内容
        public String text;
    }

    /**
     * 11. 打开或关闭背景音乐（2021-12-27新增）
     */
    public static final String APP_COMMON_OPEN_BG_MUSIC = "app_common_open_bg_music";

    /**
     * 11. 打开或关闭背景音乐（2021-12-27新增） 模型
     */
    public static class APPCommonOpenBgMusic implements Serializable {
        // true 打开背景音乐，false 关闭背景音乐
        public boolean isOpen;
    }

    /**
     * 12. 打开或关闭音效（2021-12-27新增）
     */
    public static final String APP_COMMON_OPEN_SOUND = "app_common_open_sound";

    /**
     * 12. 打开或关闭音效（2021-12-27新增） 模型
     */
    public static class APPCommonOpenSound implements Serializable {
        // true 打开音效，false 关闭音效
        public boolean isOpen;
    }

    /**
     * 13. 打开或关闭游戏中的振动效果（2021-12-27新增）
     */
    public static final String APP_COMMON_OPEN_VIBRATE = "app_common_open_vibrate";

    /**
     * 13. 打开或关闭游戏中的振动效果（2021-12-27新增）模型
     */
    public static class APPCommonOpenVibrate implements Serializable {
        // true 打开振动效果，false 关闭振动效果
        public boolean isOpen;
    }

    /**
     * 14. 设置游戏的音量大小（2021-12-31新增）
     */
    public static final String APP_COMMON_GAME_SOUND_VOLUME = "app_common_game_sound_volume";

    /**
     * 14. 设置游戏的音量大小（2021-12-31新增）模型
     */
    public static class APPCommonGameSoundVolume implements Serializable {
        // 音量大小 0 到 100
        public int volume;
    }

    /**
     * 15.  设置游戏玩法选项（2022-05-10新增）
     */
    public static final String APP_COMMON_GAME_SETTING_SELECT_INFO = "app_common_game_setting_select_info";

    /**
     * 15.  设置游戏玩法选项（2022-05-10新增） 模型
     */
    public static class APPCommonGameSettingSelectInfo implements Serializable {
        public Ludo ludo; // 游戏名称
    }

    public static class Ludo implements Serializable {
        public int mode; // mode: 默认赛制，0: 快速, 1: 经典;
        public int chessNum; // chessNum: 默认棋子数量, 2: 对应2颗棋子; 4: 对应4颗棋子;
        public int item; // item: 默认道具, 1: 有道具, 0: 没有道具
    }

    /**
     * 16. 设置游戏中的AI玩家（2022-05-11新增）
     */
    public static final String APP_COMMON_GAME_ADD_AI_PLAYERS = "app_common_game_add_ai_players";

    /**
     * 16. 设置游戏中的AI玩家（2022-05-11新增） 模型
     */
    public static class APPCommonGameAddAIPlayers implements Serializable {
        public List<AIPlayers> aiPlayers; // AI玩家
        public int isReady = 1; // 机器人加入后是否自动准备 1：自动准备，0：不自动准备 默认为1
    }

    public static class AIPlayers implements Serializable {
        public static final int LEVEL_SIMPLE = 1; // 机器人等级：简单
        public static final int LEVEL_MODERATE = 2; // 机器人等级：适中
        public static final int LEVEL_DIFFICULTY = 3; // 机器人等级：困难

        public String userId; // 玩家id
        public String avatar; // 头像url
        public String name; // 名字
        public String gender; // 性别 male：男，female：女
        public int level; // 机器人等级 1:简单 2:适中 3:困难
    }

    /**
     * 17. app在收到游戏断开连接通知后，通知游戏重试连接（2022-06-21新增，暂时支持ludo）
     */
    public static final String APP_COMMON_GAME_RECONNECT = "app_common_game_reconnect";

    /**
     * 17. app在收到游戏断开连接通知后，通知游戏重试连接（2022-06-21新增，暂时支持ludo） 模型
     */
    public static class APPCommonGameReconnect implements Serializable {
    }

    /**
     * 18. app返回玩家当前积分
     */
    public static final String APP_COMMON_GAME_SCORE = "app_common_game_score";

    /**
     * 18. app返回玩家当前积分 模型
     */
    public static class APPCommonGameScore implements Serializable {
        public long score; // 玩家当前积分
    }

    /**
     * 23. app通知游戏创建订单的结果
     */
    public static final String APP_COMMON_GAME_CREATE_ORDER_RESULT = "app_common_game_create_order_result";

    /**
     * 23. app通知游戏创建订单的结果 模型
     */
    public static class APPCommonGameCreateOrderResult implements Serializable {
        public int result; // app通知游戏创建订单的结果0：失败 1：成功
    }

    /**
     * 24. app通知游戏设置玩法（只支持 德州pro和teenpattipro）
     */
    public static final String APP_COMMON_GAME_SETTINGS = "app_common_game_settings";

    /**
     * 24. app通知游戏设置玩法（只支持 德州pro和teenpattipro） 模型
     */
    public static class APPCommonGameSettings implements Serializable {
        public int smallBlind; // 1    配置小盲,大盲为小盲的2倍[1,2,5,10,20,50,100,200,500,1000]
        public int ante; // 0    前注
        public int sBuyIn; // 100  带入值/最小带入配置[100,200,100,200,500,1000,2000,5000,100000]
        public int bBuyIn; // 200  最大带入，无限（0）
        public int isAutoStart; // 2    0表示关闭自动开始 [0,2,6,7,8,9]
        public int isStraddle; // 0    0：关闭，1自由，2强制
        public double tableDuration; // 0.05 牌桌时长配置（小时）[0.5,1,2,4,6,8]
        public int thinkTime; // 20   思考时间（秒）[10,15,20]
    }

    /**
     * 25. app通知返回大厅
     */
    public static final String APP_COMMON_GAME_BACK_LOBBY = "app_common_game_back_lobby";

    /**
     * 25. app通知返回大厅 模型
     */
    public static class APPCommonGameBackLobby implements Serializable {
    }

    /**
     * 26. app通知游戏定制UI配置表 (仅支持ludo)
     */
    public static final String APP_COMMON_GAME_UI_CUSTOM_CONFIG = "app_common_game_ui_custom_config";

    /**
     * 26. app通知游戏定制UI配置表 (仅支持ludo) 模型
     */
    public static class APPCommonGameUiCustomConfig implements Serializable {
        public String gameBoard01; // 棋盘底
        public String gameBoard02; // 棋盘
        public String diceBg; // 骰子白底
        public String diceBgGold; // 黄金骰子底
        public String dice01; // 骰子1
        public String dice02; // 骰子2
        public String dice03; // 骰子3
        public String dice04; // 骰子4
        public String dice05; // 骰子5
        public String dice06; // 骰子6
        public String diceCrown; // 骰子皇冠
        public String chessYellow; // 黄色棋子
        public String chessBlue; // 蓝色棋子
        public String chessGreen; // 绿色棋子
        public String chessRed; // 红色棋子
    }

    /**
     * 27. app通知游戏玩家信息列表 (赛车)
     */
    public static final String APP_COMMON_USERS_INFO = "app_common_users_info";

    /**
     * 27. app通知游戏玩家信息列表 (赛车) 模型
     */
    public static class APPCommonUsersInfo implements Serializable {
        public List<UserInfoModel> infos;

        public static class UserInfoModel {
            public String uid; // 玩家id
            public String avatar; // 玩家头像url
            public String name; // 玩家名字
        }
    }

    /**
     * 28. app通知游戏自定义帮助内容 (赛车)
     */
    public static final String APP_COMMON_CUSTOM_HELP_INFO = "app_common_custom_help_info";

    /**
     * 28. app通知游戏自定义帮助内容 (赛车) 模型
     */
    public static class APPCommonCustomHelpInfo implements Serializable {
        public List<String> content;
    }

    /**
     * 29. app主动调起主界面(赛车)
     */
    public static final String APP_COMMON_SHOW_GAME_SCENE = "app_common_show_game_scene";

    /**
     * 29. app主动调起主界面(赛车) 模型
     */
    public static class APPCommonShowGameScene implements Serializable {
    }

    /**
     * 30. app主动隐藏主界面(赛车)
     */
    public static final String APP_COMMON_HIDE_GAME_SCENE = "app_common_hide_game_scene";

    /**
     * 30. app主动隐藏主界面(赛车) 模型
     */
    public static class APPCommonHideGameScene implements Serializable {
    }

    /**
     * 31. app通知游戏爆词内容(谁是卧底)
     */
    public static final String APP_COMMON_GAME_SEND_BURST_WORD = "app_common_game_send_burst_word";

    /**
     * 31. app通知游戏爆词内容(谁是卧底) 模型
     */
    public static class APPCommonGameSendBurstWord implements Serializable {
        public String text; // 爆词内容
    }

    /**
     * 32. app通知游戏玩家所持有的道具卡(大富翁)
     */
    public static final String APP_COMMON_GAME_PLAYER_MONOPOLY_CARDS = "app_common_game_player_monopoly_cards";

    /**
     * 32. app通知游戏玩家所持有的道具卡(大富翁) 模型
     */
    public static class APPCommonGamePlayerMonopolyCards implements Serializable {
        public int reroll_card_count; // 重摇卡的数量
        public int free_rent_card_count; // 免租卡的数量
        public int ctrl_dice_card_count; // 购买指定骰子点数卡的数量
    }

    /**
     * 33. app通知游戏获取到道具卡(大富翁)
     */
    public static final String APP_COMMON_GAME_SHOW_MONOPOLY_CARD_EFFECT = "app_common_game_show_monopoly_card_effect";

    /**
     * 33. app通知游戏获取到道具卡(大富翁) 模型
     */
    public static class APPCommonGameShowMonopolyCardEffect implements Serializable {
        public int type; // 1：重摇卡，2：免租卡，3：指定点数卡
        public String fromUid; // 发送的玩家id
        public String toUid; // 接收方玩家id
        public int count; // 数量
    }

    /**
     * 34. app通知游戏侧更新游戏币(概率游戏相关玩法)
     */
    public static final String APP_COMMON_UPDATE_GAME_MONEY = "app_common_update_game_money";

    /**
     * 34. app通知游戏侧更新游戏币(概率游戏相关玩法) 模型
     */
    public static class AppCommonUpdateGameMoney implements Serializable {
    }

    /**
     * 35. app通知游戏玩家所持有的道具卡(只支持飞行棋)
     */
    public static final String APP_COMMON_GAME_PLAYER_PROPS_CARDS = "app_common_game_player_props_cards";

    /**
     * 35. app通知游戏玩家所持有的道具卡(只支持飞行棋) 模型
     */
    public static class AppCommonGamePlayerPropsCards implements Serializable {
        /**
         * 道具卡数量结构的json字符串
         * <p>
         * ludo 返回字符串结构说明
         * 注：返回的是一个json数据的字符串，specify_dice_roll是遥控骰子字段对应的数量
         * {"props": "{"specify_dice_roll":0}"}
         */
        public String props;
    }

    /**
     * 36. app通知游戏播放玩家所获得的道具卡的特效(只支持飞行棋)
     */
    public static final String APP_COMMON_GAME_PLAYER_PROPS_CARDS_EFFECT = "app_common_game_player_props_cards_effect";

    /**
     * 36. app通知游戏播放玩家所获得的道具卡的特效(只支持飞行棋) 模型
     */
    public static class AppCommonGamePlayerPropsCardsEffect implements Serializable {
        /**
         * 获得的道具卡名字
         * <p>
         * 飞行棋（ludo）
         * paid_events_type: "specify_dice_roll" // 控制指定摇出骰子点数的道具
         */
        public String paid_events_type;
        public String fromUid; // 发送的玩家id
        public String toUid; // 接收方玩家id
        public int count; // 数量
    }
    // endregion 通用状态

    // region 元宇宙砂砂舞
    /**
     * 1. 元宇宙砂砂舞相关设置
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/APPFST/CommonStateForDisco.html
     */
    public static final String APP_COMMON_GAME_DISCO_ACTION = "app_common_game_disco_action";

    /**
     * 1. 元宇宙砂砂舞相关设置 模型
     */
    public static class AppCommonGameDiscoAction implements Serializable {
        public int actionId; // 必传的参数，用于指定类型的序号，不同序号用于区分游戏内的不同功能，不传则会判断为无效指令，具体序号代表的功能见下表
        public Integer cooldown; // 持续时间，单位秒，部分功能有持续时间就需要传对应的数值，不传或传错则会按各自功能的默认值处理（见下表）
        public Boolean isTop; // 是否置顶，针对部分功能可排队置顶（false：不置顶；true：置顶；默认为false）
        public String field1; // 额外参数1，针对部分功能有具体的意义
        public String field2; // 额外参数2，针对部分功能有具体的意义
    }
    // endregion 元宇宙砂砂舞

    // region 定制火箭
    /**
     * 1. 礼物配置文件回调
     */
    public static final String APP_CUSTOM_ROCKET_CONFIG = "app_custom_rocket_config";

    /**
     * 1. 礼物配置文件回调 模型
     */
    public static class AppCustomRocketConfig implements Serializable {
        public int maxSeat; // 最大机位
        public double firePrice; // 发射的静态价格
        public int isDynamicPrice; // 发射价格是否动态开关 0:静态 1动态
        public String gameIntroduce; // 玩法介绍
        public String monetaryUnit; // 货币的单位
        public long serverTime; // 服务器时间戳，单位秒
        public List<String> filterModel; // 过滤不显示的模块(默认是为空)
        public List<String> filterLayer; // 过滤不显示的页面(默认是为空)
        public List<ComponentModel> componentList; // 组件列表 1套装，2主仓，3尾翼，4头像，5签名，6颜色
        public List<HeadModel> headList; // 组件列表
        public List<ExtraModel> extraList; // 专属配置

        public static class ComponentModel {
            public String componentId; // 组件的ID
            public int type; // 1套装，2主仓，3尾翼
            public String name; // 显示名称(商城+装配间+购买记录+...)
            public double price; // 价格
            public int isForever; // 永久：0非永久 1永久
            public long validTime; // 有效期：单位是秒
            public String imageId; // 图片ID
            public int isLock; // 锁：0不锁 1锁
            public int isShow; // 展示：0不展示 1展示
        }

        public static class HeadModel {
            public String componentId; // 组件的ID
            public int type; // 4头像(商城+装配间+购买记录+...)
            public String name; // 显示名称
            public double price; // 价格 暂时不考虑小数
            public int isForever; // 永久：0非永久 1永久
            public long validTime; // 有效期：单位是秒
            public String userId; // 用户的userId
            public String nickname; // 昵称
            public int sex; // 性别 0:男 1:女
            public String url; // 头像URL
        }

        public static class ExtraModel {
            public String componentId; // 组件的ID
            public int type; // 5签名，6颜色
            public String name; // 显示名称(商城+装配间+购买记录+...)
            public double price; // 价格
            public int isForever; // 永久：0非永久 1永久
            public long validTime; // 有效期：单位是秒
            public String desc; // 描述
        }
    }

    /**
     * 2. 拥有模型列表回调(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_MODEL_LIST = "app_custom_rocket_model_list";

    /**
     * 2. 拥有模型列表回调(火箭) 模型
     */
    public static class AppCustomRocketModelList implements Serializable {
        public String defaultModelId; // 默认模型
        public int isScreenshot; // 截图：0不截图 1截图(app上传失败或者过期时,被动截图)
        public List<Model> list;

        public static class Model {
            public String modelId; // 模型Id
            public int isAvatar; // 可以换装：0不可以 1可以
            public String serviceFlag; // 服务标识
            public List<ComponentModel> componentList; // 列表

            public static class ComponentModel {
                public String itemId; // 唯一标识
                public int type; // 类型
                public String value; // 值
                public int isForever; // 永久：0非永久 1永久
                public long validTime; // 有效期时间戳：单位是秒
            }
        }
    }

    /**
     * 3. 拥有组件列表回调(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_COMPONENT_LIST = "app_custom_rocket_component_list";

    /**
     * 3. 拥有组件列表回调(火箭) 模型
     */
    public static class AppCustomRocketComponentList implements Serializable {
        public List<ComponentModel> defaultList; // 默认组件列表
        public List<ComponentModel> list; // 组件列表

        public static class ComponentModel {
            public String itemId; // 唯一标识
            public int type; // 类型
            public String value; // 值
            public int isForever; // 永久：0非永久 1永久
            public long validTime; // 有效期时间戳：单位是秒
            public long date; // 购买时间：1970年1月1日开始。时间戳：单位是秒
            public String extra; // (可选择) 字段存在显示内容，字段不存在显示时间或者永久
        }
    }

    /**
     * 4. 获取用户信息回调(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_USER_INFO = "app_custom_rocket_user_info";

    /**
     * 4. 获取用户信息回调(火箭) 模型
     */
    public static class AppCustomRocketUserInfo implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public List<CustomRocketUserInfoModel> userList; // 用户信息列表
    }

    /**
     * 5. 订单记录列表回调
     */
    public static final String APP_CUSTOM_ROCKET_ORDER_RECORD_LIST = "app_custom_rocket_order_record_list";

    /**
     * 5. 订单记录列表回调 模型
     */
    public static class AppCustomRocketOrderRecordList implements Serializable {
        public int pageIndex; // 第几页
        public int pageCount; // 总页数
        public List<ComponentModel> list; // 列表

        /**
         * 定制火箭，订单组件 模型
         */
        public static class ComponentModel {
            public int type; // 类型
            public String value; // 值
            public int isForever; // 永久：0非永久 1永久
            public long validTime; // 有效期时间戳：单位是秒
            public long date; // 有效期时间戳：单位是秒
        }
    }

    /**
     * 6. 展馆内列表回调
     */
    public static final String APP_CUSTOM_ROCKET_ROOM_RECORD_LIST = "app_custom_rocket_room_record_list";

    /**
     * 6. 展馆内列表回调 模型
     */
    public static class AppCustomRocketRoomRecordList implements Serializable {
        public int pageIndex; // 第几页
        public int pageCount; // 总页数
        public List<RoomRecordModel> list; // 列表

        public static class RoomRecordModel {
            public CustomRocketUserInfoModel fromUser; // 送礼人
            public int number; // 火箭数量
        }
    }

    /**
     * 7. 展馆内玩家送出记录回调
     */
    public static final String APP_CUSTOM_ROCKET_USER_RECORD_LIST = "app_custom_rocket_user_record_list";

    /**
     * 7. 展馆内玩家送出记录回调 模型
     */
    public static class AppCustomRocketUserRecordList implements Serializable {
        public int pageIndex; // 第几页
        public int pageCount; // 总页数
        public CustomRocketUserInfoModel fromUser; // 送礼人
        public List<UserRecordModel> list; // 列表

        public static class UserRecordModel {
            public long date; // 订单时间戳: 单位是秒
            public int number; // 个数
            public CustomRocketUserInfoModel toUser; // 收礼人
            public List<ComponentModel> componentList; // 列表

            public static class ComponentModel {
                public int type; // 类型
                public String value; // 值
                public int isForever; // 永久：0非永久 1永久
                public long validTime; // 有效期时间戳：单位是秒
            }
        }
    }

    /**
     * 8. 设置默认模型(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_SET_DEFAULT_MODEL = "app_custom_rocket_set_default_model";

    /**
     * 8. 设置默认模型(火箭) 模型
     */
    public static class AppCustomRocketSetDefaultModel implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public Data data; // 数据

        public static class Data {
            public String modelId; // 默认模型
        }
    }

    /**
     * 9. 动态计算一键发送价格回调
     */
    public static final String APP_CUSTOM_ROCKET_DYNAMIC_FIRE_PRICE = "app_custom_rocket_dynamic_fire_price";

    /**
     * 9. 动态计算一键发送价格回调 模型
     */
    public static class AppCustomRocketDynamicFirePrice implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public Data data; // 数据

        public static class Data {
            public double price; // 发送的价格
        }
    }

    /**
     * 10. 一键发送回调
     */
    public static final String APP_CUSTOM_ROCKET_FIRE_MODEL = "app_custom_rocket_fire_model";

    /**
     * 10. 一键发送回调 模型
     */
    public static class AppCustomRocketFireModel implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
    }

    /**
     * 11. 新组装模型 回调
     */
    public static final String APP_CUSTOM_ROCKET_CREATE_MODEL = "app_custom_rocket_create_model";

    /**
     * 11. 新组装模型 回调 模型
     */
    public static class AppCustomRocketCreateModel implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public Data data;

        public static class Data {
            public String modelId; // 模型Id
            public int isAvatar; // 可以换装：0不可以 1可以
            public String serviceFlag; // 服务标识
            public List<ComponentModel> componentList;

            public static class ComponentModel {
                public String itemId; // 模型Id
            }
        }
    }

    /**
     * 12. 更换组件 回调
     */
    public static final String APP_CUSTOM_ROCKET_REPLACE_COMPONENT = "app_custom_rocket_replace_component";

    /**
     * 12. 更换组件 回调 模型
     */
    public static class AppCustomRocketReplaceComponent implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public Data data;

        public static class Data {
            public String modelId; // 模型Id
            public List<ComponentModel> componentList;

            public static class ComponentModel {
                public String itemId;
            }
        }
    }

    /**
     * 13. 购买组件 回调
     */
    public static final String APP_CUSTOM_ROCKET_BUY_COMPONENT = "app_custom_rocket_buy_component";

    /**
     * 13. 购买组件 回调 模型
     */
    public static class AppCustomRocketBuyComponent implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public Data data;

        public static class Data {
            public List<ComponentModel> componentList;

            public static class ComponentModel {
                public String itemId; // 唯一标识
                public int type; // 类型
                public String value; // 值
                public int isForever; // 永久：0非永久 1永久
                public long validTime; // 有效期时间戳：单位是秒
                public long date; // 有效期时间戳：单位是秒
            }
        }
    }

    /**
     * 14. app播放火箭发射动效(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_PLAY_MODEL_LIST = "app_custom_rocket_play_model_list";

    /**
     * 14. app播放火箭发射动效(火箭) 模型
     */
    public static class AppCustomRocketPlayModelList implements Serializable {
        public String orderId; // 订单号
        public InteractConfigModel interactConfig; // 可选配置
        public List<ComponentModel> componentList; // 组件列表

        public static class ComponentModel {
            public int type; // 类型
            public String value; // 值
        }

        public static class InteractConfigModel {
            public int interactivePlay; // 互动玩法默认状态，1是关闭，0是打开，默认打开；
            public List<Integer> gear; // 每个档位需要点击的次数；
            public int nicknameTips; // 昵称飘字是否显示，1是隐藏，0是显示，默认0；
            public int uiSwitche; // 左上角UI信息是否显示，1是隐藏，0是显示，默认0；
            public int guide; // 新手引导是否显示，1是隐藏，0是显示，默认0；
        }
    }

    /**
     * 15. app推送主播信息(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_NEW_USER_INFO = "app_custom_rocket_new_user_info";

    /**
     * 15. app推送主播信息(火箭) 模型
     */
    public static class AppCustomRocketNewUserInfo implements Serializable {
        public List<CustomRocketUserInfoModel> userList; // 用户信息列表
    }

    /**
     * 16. 验证签名合规(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_VERIFY_SIGN = "app_custom_rocket_verify_sign";

    /**
     * 16. 验证签名合规(火箭) 回调  模型
     */
    public static class AppCustomRocketVerifySign implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public Data data;

        public static class Data {
            public String sign; // 验证的签名
        }
    }

    /**
     * 17. app主动调起火箭主界面(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_SHOW_GAME_SCENE = "app_custom_rocket_show_game_scene";

    /**
     * 17. app主动调起火箭主界面(火箭) 模型
     */
    public static class AppCustomRocketShowGameScene implements Serializable {
    }

    /**
     * 18. app主动隐藏火箭主界面(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_HIDE_GAME_SCENE = "app_custom_rocket_hide_game_scene";

    /**
     * 18. app主动隐藏火箭主界面(火箭)  模型
     */
    public static class AppCustomRocketHideGameScene implements Serializable {
    }

    /**
     * 19. app推送解锁组件(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_UNLOCK_COMPONENT = "app_custom_rocket_unlock_component";

    /**
     * 19. app推送解锁组件(火箭)  模型
     */
    public static class AppCustomRocketUnlockComponent implements Serializable {
        public int type; // 组件类型
        public String componentId; // 组件ID
    }

    /**
     * 20. app推送火箭效果飞行点击(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_FLY_CLICK = "app_custom_rocket_fly_click";

    /**
     * 20. app推送火箭效果飞行点击(火箭)  模型
     */
    public static class AppCustomRocketFlyClick implements Serializable {
    }

    /**
     * 21. app推送关闭火箭播放效果(火箭)
     */
    public static final String APP_CUSTOM_ROCKET_CLOSE_PLAY_EFFECT = "app_custom_rocket_close_play_effect";

    /**
     * 21. app推送关闭火箭播放效果(火箭)  模型
     */
    public static class AppCustomRocketClosePlayEffect implements Serializable {
    }

    /**
     * 22. 颜色和签名自定义改到装配间的模式，保存颜色或签名回调
     */
    public static final String APP_CUSTOM_ROCKET_SAVE_SIGN_COLOR = "app_custom_rocket_save_sign_color";

    /**
     * 22. 颜色和签名自定义改到装配间的模式，保存颜色或签名回调 模型
     */
    public static final class AppCustomRocketSaveSignColor implements Serializable {
        public int resultCode; // 0: 请求成功，1：请求失败
        public String error; // 错误描述
        public Data data; // 数据

        public static class Data {
            public List<ComponentModel> componentList;
        }

        public static class ComponentModel {
            public String itemId; // 唯一标识
            public int type; // 签名
            public String value; // 签名的值
            public int isForever; // 永久：0非永久 1永久
            public long validTime; // 有效期时间戳：单位是秒
        }
    }

    /**
     * 定制火箭，用户信息 模型
     */
    public static class CustomRocketUserInfoModel {
        public String userId;   // 用户的userId
        public String nickname; // 昵称
        public int sex; // 性别 0:男 1:女
        public String url; // 头像URL
    }
    // endregion 定制火箭

    // region 棒球
    /**
     * 1. 下发游戏客户端查询排行榜数据(棒球)
     */
    public static final String APP_BASEBALL_RANKING = "app_baseball_ranking";

    /**
     * 1. 下发游戏客户端查询排行榜数据(棒球) 模型
     */
    public static class AppBaseballRanking implements Serializable {
        public List<AppBaseballPlayerInfo> data;
    }

    /**
     * 2. 下发游戏客户端查询我的排名数据(棒球)
     */
    public static final String APP_BASEBALL_MY_RANKING = "app_baseball_my_ranking";

    /**
     * 2. 下发游戏客户端查询我的排名数据(棒球) 模型
     */
    public static class AppBaseballMyRanking implements Serializable {
        public AppBaseballPlayerInfo data;
    }

    /**
     * 3. 下发游戏客户端查询排在自己前后的玩家数据(棒球)
     */
    public static final String APP_BASEBALL_RANGE_INFO = "app_baseball_range_info";

    /**
     * 3. 下发游戏客户端查询排在自己前后的玩家数据(棒球) 模型
     */
    public static class AppBaseballRangeInfo implements Serializable {
        public AppBaseballPlayerInfo before; // 前一名
        public AppBaseballPlayerInfo after; // 后一名
    }

    /**
     * 4. app主动调起主界面(棒球)
     */
    public static final String APP_BASEBALL_SHOW_GAME_SCENE = "app_baseball_show_game_scene";

    /**
     * 4. app主动调起主界面(棒球) 模型
     */
    public static class AppBaseballShowGameScene implements Serializable {
    }

    /**
     * 5. app主动隐藏主界面(棒球)
     */
    public static final String APP_BASEBALL_HIDE_GAME_SCENE = "app_baseball_hide_game_scene";

    /**
     * 5. app主动隐藏主界面(棒球) 模型
     */
    public static class AppBaseballHideGameScene implements Serializable {
    }

    /**
     * 6. app推送需要的文本数据(棒球)
     */
    public static final String APP_BASEBALL_TEXT_CONFIG = "app_baseball_text_config";

    /**
     * 6. app推送需要的文本数据(棒球) 模型
     */
    public static class AppBaseballTextConfig implements Serializable {
        public String mode1;
        public String mode2;
        public String mode3;
    }

    public static class AppBaseballPlayerInfo implements Serializable {
        public String playerId; // 玩家Id
        public String name; // 玩家昵称
        public String avatar; // 头像
        public long distance; // 距离
        public int rank; // 排名
    }
    // endregion 棒球

    // region 3D语聊房
    /**
     * 1. 设置房间配置
     * 收到游戏发过来的mg_custom_cr_room_init_data状态后
     * App把房间配置以及主播位数据发送给游戏
     */
    public static final String APP_CUSTOM_CR_SET_ROOM_CONFIG = "app_custom_cr_set_room_config";

    /**
     * 1. 设置房间配置 模型
     */
    public static class AppCustomCrSetRoomConfig implements Serializable {
        public int platformRotate; // 立方体是否自转  0:不旋转 1：旋转
        public int rotateDir; // 立方体自转方向 0:从右往左转  1:从左往右转
        public int rotateSpeed; // 立方体自转速度（整形类型）0:使用默认速度每秒6度  x>0:每秒旋转x度
        public int gameMusic; // 音乐控制  0:关  1:开
        public int gameSound; // 音效控制  0:关  1:开
        public int flashVFX; // 是否开启爆灯边框效果  0:关  1:开
        public int micphoneWave; // 是否开启麦浪边框效果  0:关  1:开
        public int showGiftValue; // 是否显示心动值  0:隐藏  1:显示
    }

    /**
     * 2. 设置主播位数据
     * 收到游戏发过来的mg_custom_cr_room_init_data状态后
     * App把房间配置以及主播位数据发送给游戏
     * <p>
     * 初始化时，需要发送5个主播位的全量数据
     * 后续如果某个主播位有变化，可只传一个或多个主播位的数据(需要该主播位的全量数据)
     */
    public static final String APP_CUSTOM_CR_SET_SEATS = "app_custom_cr_set_seats";

    /**
     * 2. 设置主播位数据 模型
     */
    public static class AppCustomCrSetSeats implements Serializable {
        public List<CrSeatModel> seats; // 主播位数据

        /**
         * 3D语聊房主播位 模型
         */
        public static class CrSeatModel implements Serializable {
            public static final int MICRO_STATE_SOMEONE = 1;
            public static final int MICRO_STATE_NO_ONE = 2;
            public static final int MICRO_STATE_LOCKED = 3;

            public int seatIndex; // 0~4一共5个麦位，0为老板位，1~4为四个面主播位
            public int level; // 四个面场景等级 0~2
            public int microState;  // 麦位状态  1:有人  2:空位  3:麦位被锁
            public String userId; // 当前麦位用户id（如果有）
            public int gender; // 性别  0:男  1:女
            public String name; // 名字
            public String photoUrl; // 头像链接
            public int micphoneState; // 麦克风状态  -1:禁麦  0:闭麦  1:开麦
            public int giftValue; // 心动值
        }
    }

    /**
     * 3. 播放收礼效果
     */
    public static final String APP_CUSTOM_CR_PLAY_GIFT_EFFECT = "app_custom_cr_play_gift_effect";

    /**
     * 3. 播放收礼效果 模型
     */
    public static class AppCustomCrPlayGiftEffect implements Serializable {
        public String giverUserId; // 送礼人的userId
        public boolean isAllSeat; // 标识是否是全麦
        public List<CrGiftModel> giftList; // 礼物列表，可送给一个或者多个主播

        public static class CrGiftModel implements Serializable {
            public int seatIndex; // 0~4一共5个麦位，0为老板位，1~4为四个面主播位
            public int level; // 礼物档位，1 ~ 30
            public int count; // 礼物数量
        }
    }

    /**
     * 4. 通知播放爆灯特效
     */
    public static final String APP_CUSTOM_CR_SET_LIGHT_FLASH = "app_custom_cr_set_light_flash";

    /**
     * 4. 通知播放爆灯特效 模型
     */
    public static class AppCustomCrSetLightFlash implements Serializable {
        public int seatIndex; // 主播位index
    }

    /**
     * 5. 通知主播播放指定动作
     */
    public static final String APP_CUSTOM_CR_PLAY_ANIM = "app_custom_cr_play_anim";

    /**
     * 5. 通知主播播放指定动作 模型
     */
    public static class AppCustomCrPlayAnim implements Serializable {
        public int seatIndex; // 主播位index
        public int animId; // 动作id 1:跳舞 2:飞吻 3:感谢 4:鼓掌 5:害羞 6:欢呼 7:伤心 8:生气
    }

    /**
     * 6. 通知麦浪值变化
     */
    public static final String APP_CUSTOM_CR_MICPHONE_VALUE_SEAT = "app_custom_cr_micphone_value_seat";

    /**
     * 6. 通知麦浪值变化 模型
     */
    public static class AppCustomCrMicphoneValueSeat implements Serializable {
        public int seatIndex; // 主播位index
        public int value; // 麦浪值，请映射到区间0~100
    }

    /**
     * 7. 通知暂停或恢复立方体自转
     */
    public static final String APP_CUSTOM_CR_PAUSE_ROTATE = "app_custom_cr_pause_rotate";

    /**
     * 7. 通知暂停或恢复立方体自转 模型
     */
    public static class AppCustomCrPauseRotate implements Serializable {
        /**
         * 0:恢复自转（若旋转配置启用自转，则收到0时恢复自转，若旋转配置不启用自转，则无效果）
         * 1:暂停自转（若旋转配置启用自转，则收到1时暂定自转，若旋转配置不启用自转，则无效果）
         */
        public int pause;
    }
    // endregion 3D语聊房

}
