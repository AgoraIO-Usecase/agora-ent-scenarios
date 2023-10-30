package io.agora.scene.base.manager;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import io.agora.scene.base.Constant;
import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.event.UserCancellationEvent;
import io.agora.scene.base.event.UserInfoChangeEvent;
import io.agora.scene.base.utils.GsonUtil;
import io.agora.scene.base.utils.SPUtil;

public final class UserManager {
    private volatile static UserManager instance;
    private User mUser;

    private UserManager() {
    }

    public User getUser() {
        if (mUser != null) {
            if (TextUtils.isEmpty(ApiManager.token)) {
                ApiManager.token = mUser.token;
            }
            return mUser;
        }
        readingUserInfoFromPrefs();
        return mUser;
    }

    public void saveUserInfo(User user) {
        mUser = user;
        writeUserInfoToPrefs(false);
        EventBus.getDefault().post(new UserInfoChangeEvent());
    }

    public void logout() {
        writeUserInfoToPrefs(true);
    }

    public void cancel() {
        logout();
        EventBus.getDefault().post(new UserCancellationEvent());
    }

    private void writeUserInfoToPrefs(boolean isLogOut) {
        if (isLogOut) {
            mUser = null;
            SPUtil.putString(Constant.CURRENT_USER, "");
        } else {
            SPUtil.putString(Constant.CURRENT_USER, getUserInfoJson());
        }
    }

    private void readingUserInfoFromPrefs() {
        String userInfo = SPUtil.getString(Constant.CURRENT_USER, "");
        if (!TextUtils.isEmpty(userInfo)) {
            mUser = GsonUtil.getInstance().fromJson(userInfo, User.class);
            if (TextUtils.isEmpty(ApiManager.token)) {
                ApiManager.token = mUser.token;
            }
        }
    }

    private String getUserInfoJson() {
        return GsonUtil.getInstance().toJson(mUser);
    }

    public static UserManager getInstance() {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null)
                    instance = new UserManager();
            }
        }
        return instance;
    }

    public boolean isLogin() {
        if (mUser == null) {
            readingUserInfoFromPrefs();
            return mUser != null && !TextUtils.isEmpty(mUser.userNo);
        } else {
            if (TextUtils.isEmpty(ApiManager.token)) {
                ApiManager.token = mUser.token;
            }
        }
        return true;
    }

}
