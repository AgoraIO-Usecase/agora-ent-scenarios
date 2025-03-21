package io.agora.scene.playzone.live.sub;

import android.app.Activity;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo;
import io.agora.scene.base.api.SecureOkHttpClient;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.playzone.BuildConfig;
import io.agora.scene.playzone.PlayCenter;
import io.agora.scene.playzone.service.PlayRobotInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGListener;
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSTAPPDecorator;
import tech.sud.mgp.SudMGPWrapper.model.GameConfigModel;
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel;
import tech.sud.mgp.SudMGPWrapper.state.MGStateResponse;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPAPPState;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;
import tech.sud.mgp.SudMGPWrapper.utils.GameCommonStateUtils;
import tech.sud.mgp.core.ISudFSMMG;
import tech.sud.mgp.core.ISudFSMStateHandle;

/**
 * 游戏业务逻辑
 * 1.自定义ViewModel继承此类，实现对应方法。(注意：onAddGameView()与onRemoveGameView()与页面有交互)
 * 2.外部调用switchGame(activity,gameRoomId,gameId)方法启动游戏，参数定义可查看方法注释。
 * 3.页面销毁时调用onDestroy()
 * <p>
 * Game business logic
 * 1. Define a custom ViewModel that extends this class and implement the corresponding methods. (Note: onAddGameView() and onRemoveGameView() interact with the page)
 * 2. Externally call the switchGame(activity, gameRoomId, gameId) method to start the game. Refer to method comments for parameter definitions.
 * 3. Call onDestroy() when the page is destroyed.
 */
public class QuickStartGameViewModel extends BaseGameViewModel {

    // TODO: Sud平台申请的appId
    // TODO: The appId obtained from Sud platform application.
    public static String SudMGP_APP_ID = BuildConfig.SUB_APP_ID;

    // TODO: Sud平台申请的appKey
    // TODO: The appKey obtained from Sud platform application.
    public static String SudMGP_APP_KEY = BuildConfig.SUB_APP_KEY;

    // TODO: true 加载游戏时为测试环境 false 加载游戏时为生产环境
    // TODO: true for loading the game in the testing environment, false for loading the game in the production environment.
    public static final boolean GAME_IS_TEST_ENV = true;

    // TODO: 使用的UserId。这里随机生成作演示，开发者将其修改为业务使用的唯一userId
    // TODO: Used UserId. Here it is randomly generated for demonstration purposes. Developers should modify it to the unique userId used for the business.
    public static String userId = UserManager.getInstance().getUser().id.toString();

    // 游戏自定义安全操作区域
    // Customized security operation zone for the game.
    public GameViewInfoModel.GameViewRectModel gameViewRectModel;

    // TODO: 游戏的语言代码，参考文档：https://docs.sud.tech/zh-CN/app/Client/Languages/
    // TODO: Language code for the game. Reference documentation: https://docs.sud.tech/en-US/app/Client/Languages/
    public String languageCode = "zh-CN";

    // 游戏View回调
    // Game View callback.
    public final MutableLiveData<View> gameViewLiveData = new MutableLiveData<>();

    // 游戏是否
    public final MutableLiveData<Boolean> gameStartedLiveData = new MutableLiveData<>();

    // 当前玩家是否加入游戏
    public final MutableLiveData<Boolean> gameLocalPlayerInLiveData = new MutableLiveData<>();

    // 队长回调
    public final MutableLiveData<Pair<String, Boolean>> captainIdLiveData = new MutableLiveData<>();

    // 游戏消息
    public final MutableLiveData<String> gameMessageLiveData = new MutableLiveData<>(); // 游戏消息

    // 机器人列表
    public List<PlayRobotInfo> robotInfoList = new ArrayList<>();

    // 玩家列表
    public Set<String> playerSet = new ArraySet<>();

    // 已添加机器人玩家
    public List<SudMGPAPPState.AIPlayers> aiPlayers = new ArrayList<>();

    /**
     * 向接入方服务器获取code
     * Retrieve the code from the partner's server.
     */
    @Override
    protected void getCode(Activity activity, String userId, String appId, GameGetCodeListener listener) {
        // TODO: 2022/6/10 注意，这里是演示使用OkHttpClient请求hello-sud服务
        // TODO: 2022/6/10 开发者在与后端联调时需将其改成自己的网络请求方式向自己的服务器获取code
        // TODO: 2023/10/26 每次回调此方法都去自己的后端拿最新的code，不要缓存code

        // TODO: 2022/6/10 Note that this is a demonstration using OkHttpClient to request the hello-sud service.
        // TODO: 2022/6/10 Developers should modify this to their own network request method to retrieve the code from their own server during backend integration.
        // TODO: 2023/10/26 Retrieve the latest code from your own backend every time this method is called, and avoid caching the code.
        OkHttpClient client = SecureOkHttpClient.createWithSeconds(30)
                .build();
        String req;
        try {
            JSONObject reqJsonObj = new JSONObject();
            reqJsonObj.put("user_id", userId);
            reqJsonObj.put("app_id", getAppId());
            req = reqJsonObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
            req = "";
        }

        RequestBody body = RequestBody.create(req, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://mgp-hello.sudden.ltd/login/v3")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailed();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String dataJson = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(dataJson);
                    int ret_code = jsonObject.getInt("ret_code");
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    String code = dataObject.getString("code");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (ret_code == MGStateResponse.SUCCESS) {
                                listener.onSuccess(code);
                            } else {
                                listener.onFailed();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed();
                        }
                    });
                }
            }
        });
    }

    /**
     * 设置当前用户id(接入方定义)
     * Set the current user ID (defined by the partner).
     */
    @Override
    protected String getUserId() {
        return userId;
    }

    /**
     * 设置Sud平台申请的appId
     * Set the appId obtained from the Sud platform.
     */
    @Override
    protected String getAppId() {
        return SudMGP_APP_ID;
    }

    /**
     * 设置Sud平台申请的appKey
     * Set the appKey obtained from the Sud platform.
     */
    @Override
    protected String getAppKey() {
        return SudMGP_APP_KEY;
    }

    /**
     * 设置游戏的语言代码
     * Set the language code for the game.
     */
    @Override
    protected String getLanguageCode() {
        return languageCode;
    }

    /**
     * 设置游戏的安全操作区域，{@link ISudFSMMG}.onGetGameViewInfo()的实现。
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG/onGetGameViewInfo.html
     * <p>
     * Set the secure operation area for the game, implementation of {@link ISudFSMMG}.onGetGameViewInfo().
     * Reference documentation: https://docs.sud.tech/en-US/app/Client/API/ISudFSMMG/onGetGameViewInfo.html
     *
     * @param gameViewInfoModel 游戏视图模型
     *                          Game view model
     */
    @Override
    protected void getGameRect(GameViewInfoModel gameViewInfoModel) {
        // 相对于view_size（左、上、右、下）边框偏移（单位像素）
        // 开发者可自定义gameViewRectModel来控制安全区域

        // Border offset relative to view_size (left, top, right, bottom) in pixels
        // Developers can customize the gameViewRectModel to control the safe area
        if (gameViewRectModel != null) {
            gameViewInfoModel.view_game_rect = gameViewRectModel;
        }
    }

    /**
     * 获取游戏配置对象，{@link ISudFSMMG}.onGetGameCfg()的实现。
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG/onGetGameCfg.html
     * 开发者拿到此对象之后，可修改自己需要的配置
     * 注意：在加载游戏之前配置才有效
     * <p>
     * Get the game configuration object, implementation of {@link ISudFSMMG}.onGetGameCfg().
     * Reference documentation: https://docs.sud.tech/en-US/app/Client/API/ISudFSMMG/onGetGameCfg.html
     * Once developers obtain this object, they can modify the desired configurations.
     * Note: The configurations are only effective before loading the game.
     *
     * @return 游戏配置对象
     * Game configuration object
     */
    public GameConfigModel getGameConfigModel() {
        return gameConfigModel;
    }

    /**
     * true 加载游戏时为测试环境
     * false 加载游戏时为生产环境
     * <p>
     * true for loading the game in the testing environment
     * false for loading the game in the production environment.
     */
    @Override
    protected boolean isTestEnv() {
        return GAME_IS_TEST_ENV;
    }

    /**
     * 将游戏View添加到页面中
     * Add the game View to the page.
     */
    @Override
    protected void onAddGameView(View gameView) {
        gameViewLiveData.setValue(gameView);
    }

    /**
     * 将页面中的游戏View移除
     * Remove the game View from the page.
     */
    @Override
    protected void onRemoveGameView() {
        gameViewLiveData.setValue(null);
    }

    // ************ 上面是基础能力以及必要配置，下面讲解状态交互
    // ************ 主要有：1.App向游戏发送状态；2.游戏向App回调状态

    // ************ Above are the basic capabilities and necessary configurations, below we explain state interactions
    // ************ Mainly include: 1. App sending states to the game; 2. Game callback states to the App

    /**
     * 1.App向游戏发送状态
     * 这里演示的是发送：1. 加入状态；
     * 开发者可自由定义方法，能发送的状态都封装在{@link SudFSTAPPDecorator}
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/APPFST/
     * 注意：
     * 1，App向游戏发送状态，因为需要走网络，所以向游戏发送状态之后，不能马上销毁游戏或者finish Activity，否则状态无法发送成功。
     * 2，要保证状态能到达，可以发送之后，delay 500ms再销毁游戏或者finish Activity。
     *
     * <p> English
     * App sending states to the game
     * Here, we demonstrate sending the "join" state.
     * Developers can freely define methods to send different states, all encapsulated in {@link SudFSTAPPDecorator}.
     * Reference documentation: https://docs.sud.tech/en-US/app/Client/APPFST/
     * Note:
     * When sending states from the App to the game, as it involves network communication, the game should not be immediately destroyed or the Activity finished after sending the state; otherwise, the state may not be successfully sent.
     * To ensure the state reaches the game, it is recommended to delay the destruction of the game or finishing the Activity by 500ms after sending the state.
     */
    public void notifyAPPCommonSelfIn(boolean isIn, int seatIndex, boolean isSeatRandom, int teamId) {
        sudFSTAPPDecorator.notifyAPPCommonSelfIn(isIn, seatIndex, isSeatRandom, teamId);
    }

    // 指定队长
    public void notifyAPPCommonSelfCaptain(@NonNull String captainId) {
        sudFSTAPPDecorator.notifyAPPCommonSelfCaptain(captainId);
    }

    /**
     * 2.游戏向App回调状态
     * 这里演示的是接收游戏回调状态：10. 游戏状态 mg_common_game_state
     * 游戏回调的每个状态都对应着一个方法，方法定义在：{@link SudFSMMGListener}
     * 在文档(https://docs.sud.tech/zh-CN/app/Client/MGFSM/)里面定义的都是游戏向APP回调状态的定义
     * 其中又细分为通过两个不同接口进行回调，分别是：onGameStateChange和onPlayerStateChange，但是都已封装好，只需关心SudFSMMGListener对应回调即可
     *
     * <p> English
     * 2. Game callback states to the App
     * Here, we demonstrate receiving the game callback state: 10. Game state mg_common_game_state
     * Each callback state from the game corresponds to a method defined in {@link SudFSMMGListener}.
     * The definitions for game callback states are provided in the documentation (https://docs.sud.tech/en-US/app/Client/MGFSM/).
     * These states are further divided into two different interfaces for callbacks: onGameStateChange and onPlayerStateChange. However, they are already encapsulated, and you only need to focus on the corresponding callbacks in SudFSMMGListener.
     */
    @Override
    public void onGameMGCommonGameState(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameState model) {
        super.onGameMGCommonGameState(handle, model);
    }

    @Override
    public void onGameStarted() {
        super.onGameStarted();
        gameStartedLiveData.postValue(true);
    }

    @Override
    public void onGameDestroyed() {
        super.onGameDestroyed();
    }

    @Override
    public void onGameLoadingProgress(int stage, int retCode, int progress) {
        super.onGameLoadingProgress(stage, retCode, progress);
    }

    // 1.加入状态（
    @Override
    public void onPlayerMGCommonPlayerIn(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerIn model) {
        super.onPlayerMGCommonPlayerIn(handle, userId, model);
        if (Objects.equals(userId, PlayCenter.INSTANCE.getMUser().id.toString())) {
            gameLocalPlayerInLiveData.postValue(model.isIn);
        }
        if (model.isIn) {
            playerSet.add(userId);
        } else {
            playerSet.remove(userId);
        }
    }

    // 2.准备状态
    @Override
    public void onPlayerMGCommonPlayerReady(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerReady model) {
        super.onPlayerMGCommonPlayerReady(handle, userId, model);
    }

    // 3.队长状态
    @Override
    public void onPlayerMGCommonPlayerCaptain(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerCaptain model) {
        super.onPlayerMGCommonPlayerCaptain(handle, userId, model);
        captainIdLiveData.postValue(new Pair<>(userId, model.isCaptain));
    }

    // 游戏公屏消息
    @Override
    public void onGameMGCommonPublicMessage(ISudFSMStateHandle handle, SudMGPMGState.MGCommonPublicMessage model) {
        String message = GameCommonStateUtils.parseMGCommonPublicMessage(model, "zh-CN");
        if (!TextUtils.isEmpty(message)) {
            gameMessageLiveData.postValue(message);
        }
        super.onGameMGCommonPublicMessage(handle, model);
    }

    // 添加机器人
    public void addRobot() {
        PlayRobotInfo playRobotInfo = findAvailableRobotInfo();
        if (playRobotInfo != null) {
            List<SudMGPAPPState.AIPlayers> aiPlayerList = new ArrayList<>();
            SudMGPAPPState.AIPlayers aiPlayers = new SudMGPAPPState.AIPlayers();
            aiPlayers.level = playRobotInfo.getLevel();
            AUIUserThumbnailInfo owner = playRobotInfo.getOwner();
            if (owner != null) {
                aiPlayers.userId = owner.userId;
                aiPlayers.name = owner.userName;
                aiPlayers.avatar = owner.userAvatar;
            }
            aiPlayers.gender = playRobotInfo.getGender();
            aiPlayerList.add(aiPlayers);
            sudFSTAPPDecorator.notifyAPPCommonGameAddAIPlayers(aiPlayerList, 1);
        }
    }

    private PlayRobotInfo findAvailableRobotInfo() {
        for (int i = 0; i < robotInfoList.size(); i++) {
            PlayRobotInfo playRobotInfo = robotInfoList.get(i);
            if (!playerSet.contains(playRobotInfo.getOwner().userId)) {
                return playRobotInfo;
            }
        }
        return null;
    }

    public boolean supportRobots(long gameId) {
        // TeenPatti, 德州扑克, 友尽闯关 暂时不支持机器人
       if (gameId == 1557194487352053761L || gameId ==1557194155570024449L || gameId == 1490944230389182466L) {
           return false;
       }
       return true;
    }
}
