package io.agora.scene.base.api.model;

import android.util.Log;

public class User {
    public String headUrl;
    public String mobile;
    public String name;
    public String sex;
    public int status;
    public String userNo;
    public String token;
    public Long id;
    public Integer realNameVerifyStatus = 1;

    public void setRealNameVerifyStatus(Integer realNameVerifyStatus) {
        if (realNameVerifyStatus == null) {
            this.realNameVerifyStatus = 0;
            Log.d("setRealNameVerifyStatus","realNameVerifyStatus==null");
        } else {
            this.realNameVerifyStatus = realNameVerifyStatus;
        }
    }
}
