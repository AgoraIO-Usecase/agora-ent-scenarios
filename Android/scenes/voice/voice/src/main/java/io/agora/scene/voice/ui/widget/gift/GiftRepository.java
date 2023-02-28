package io.agora.scene.voice.ui.widget.gift;

import android.content.Context;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.List;

import io.agora.scene.voice.model.GiftBean;

/**
 * 用于获取本地礼物信息
 */
public class GiftRepository {
    static int SIZE = 9;
    public static String [] Price = {"1","5","10","20","50","100","500","1000","1500"};

    public static List<GiftBean> getDefaultGifts(Context context) {
        List<GiftBean> gifts = new ArrayList<>();
        GiftBean bean;
        for(int i = 1; i <= SIZE; i++){
            bean = new GiftBean();
            String name = "voice_icon_gift_"+i;
            int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            int nameId = context.getResources().getIdentifier("voice_gift_default_name_" + i, "string", context.getPackageName());
            bean.setResource(resId);
            bean.setName(context.getString(nameId));
            bean.setId("VoiceRoomGift"+i);
            bean.setPrice(Price[i-1]);
            gifts.add(bean);
        }
        return gifts;
    }

    public static List<GiftBean> getGiftsByPage(Context context,int page){
        int base = 4;
        int index = page * base;
        List<GiftBean> gifts = new ArrayList<>();
        List<GiftBean> data =  getDefaultGifts(context);
        for (int i = 1; i <= data.size(); i++) {
            if (index < i && i <= base+(page*base)){
                gifts.add(data.get(i-1));
            }
        }
        return gifts;
    }

    /**
     * 获取GiftBean
     * @param giftId
     * @return
     */
    public static GiftBean getGiftById(Context context,String giftId) {
        List<GiftBean> gifts = getDefaultGifts(context);
        for (GiftBean bean : gifts) {
            if(TextUtils.equals(bean.getId(), giftId)) {
                return bean;
            }
        }
        return null;
    }
}
