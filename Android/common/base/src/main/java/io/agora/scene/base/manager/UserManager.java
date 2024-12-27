package io.agora.scene.base.manager;

import android.text.TextUtils;

import io.agora.scene.base.Constant;
import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.utils.GsonTools;
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

    public void saveUserInfo(User user, boolean isLogin) {
        if (isLogin){
            mUser = user;
        }else {
            if (user.token==null || user.token.isEmpty()){
                if (mUser != null) {
                    user.token = mUser.token;
                }
                mUser = user;
            }
        }
        writeUserInfoToPrefs(false);
    }

    public void logout() {
        SPUtil.putBoolean(Constant.IS_AGREE, false);
        writeUserInfoToPrefs(true);
    }

    public void logoff() {
        SPUtil.putBoolean(Constant.IS_AGREE, false);
        writeUserInfoToPrefs(true);
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
            mUser = GsonTools.toBean(userInfo, User.class);
            if (TextUtils.isEmpty(ApiManager.token)) {
                ApiManager.token = mUser.token;
            }
        }
    }

    private String getUserInfoJson() {
        return GsonTools.beanToString(mUser);
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

    public boolean isRealNameAuth() {
        User user = getUser();
        return user!=null && user.realNameVerifyStatus == 1;
    }
}
