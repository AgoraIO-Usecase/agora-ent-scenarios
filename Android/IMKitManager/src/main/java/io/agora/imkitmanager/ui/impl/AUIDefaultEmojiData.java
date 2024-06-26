package io.agora.imkitmanager.ui.impl;

import io.agora.imkitmanager.R;
import io.agora.imkitmanager.model.AUIChatBottomBarIcon;
import io.agora.imkitmanager.model.AUIChatBottomBarIconType;
import io.agora.imkitmanager.utils.AUIEmojiUtils;

public class AUIDefaultEmojiData {
    
    private static String[] emojis = new String[]{
        AUIEmojiUtils.ee_1,
        AUIEmojiUtils.ee_2,
        AUIEmojiUtils.ee_3,
        AUIEmojiUtils.ee_4,
        AUIEmojiUtils.ee_5,
        AUIEmojiUtils.ee_6,
        AUIEmojiUtils.ee_7,
        AUIEmojiUtils.ee_8,
        AUIEmojiUtils.ee_9,
        AUIEmojiUtils.ee_10,
        AUIEmojiUtils.ee_11,
        AUIEmojiUtils.ee_12,
        AUIEmojiUtils.ee_13,
        AUIEmojiUtils.ee_14,
        AUIEmojiUtils.ee_15,
        AUIEmojiUtils.ee_16,
        AUIEmojiUtils.ee_17,
        AUIEmojiUtils.ee_18,
        AUIEmojiUtils.ee_19,
        AUIEmojiUtils.ee_20,
        AUIEmojiUtils.ee_21,
        AUIEmojiUtils.ee_22,
        AUIEmojiUtils.ee_23,
        AUIEmojiUtils.ee_24,
        AUIEmojiUtils.ee_25,
        AUIEmojiUtils.ee_26,
        AUIEmojiUtils.ee_27,
        AUIEmojiUtils.ee_28,
        AUIEmojiUtils.ee_29,
        AUIEmojiUtils.ee_30,
        AUIEmojiUtils.ee_31,
        AUIEmojiUtils.ee_32,
        AUIEmojiUtils.ee_33,
        AUIEmojiUtils.ee_34,
        AUIEmojiUtils.ee_35,
        AUIEmojiUtils.ee_36,
        AUIEmojiUtils.ee_37,
        AUIEmojiUtils.ee_38,
        AUIEmojiUtils.ee_39,
        AUIEmojiUtils.ee_40,
        AUIEmojiUtils.ee_41,
        AUIEmojiUtils.ee_42,
        AUIEmojiUtils.ee_43,
        AUIEmojiUtils.ee_44,
        AUIEmojiUtils.ee_45,
        AUIEmojiUtils.ee_46,
        AUIEmojiUtils.ee_47,
        AUIEmojiUtils.ee_48,
        AUIEmojiUtils.ee_49,
        AUIEmojiUtils.ee_50,
        AUIEmojiUtils.ee_51,
        AUIEmojiUtils.ee_52,
    };
    
    private static int[] icons = new int[]{
        R.drawable.voice_ee_1,
        R.drawable.voice_ee_2,
        R.drawable.voice_ee_3,
        R.drawable.voice_ee_4,
        R.drawable.voice_ee_5,
        R.drawable.voice_ee_6,
        R.drawable.voice_ee_7,
        R.drawable.voice_ee_8,
        R.drawable.voice_ee_9,
        R.drawable.voice_ee_10,
        R.drawable.voice_ee_11,
        R.drawable.voice_ee_12,
        R.drawable.voice_ee_13,
        R.drawable.voice_ee_14,
        R.drawable.voice_ee_15,
        R.drawable.voice_ee_16,
        R.drawable.voice_ee_17,
        R.drawable.voice_ee_18,
        R.drawable.voice_ee_19,
        R.drawable.voice_ee_20,
        R.drawable.voice_ee_21,
        R.drawable.voice_ee_22,
        R.drawable.voice_ee_23,
        R.drawable.voice_ee_24,
        R.drawable.voice_ee_25,
        R.drawable.voice_ee_26,
        R.drawable.voice_ee_27,
        R.drawable.voice_ee_28,
        R.drawable.voice_ee_29,
        R.drawable.voice_ee_30,
        R.drawable.voice_ee_31,
        R.drawable.voice_ee_32,
        R.drawable.voice_ee_33,
        R.drawable.voice_ee_34,
        R.drawable.voice_ee_35,
        R.drawable.voice_ee_36,
        R.drawable.voice_ee_37,
        R.drawable.voice_ee_38,
        R.drawable.voice_ee_39,
        R.drawable.voice_ee_40,
        R.drawable.voice_ee_41,
        R.drawable.voice_ee_42,
        R.drawable.voice_ee_43,
        R.drawable.voice_ee_44,
        R.drawable.voice_ee_45,
        R.drawable.voice_ee_46,
        R.drawable.voice_ee_47,
        R.drawable.voice_ee_48,
        R.drawable.voice_ee_49,
        R.drawable.voice_ee_50,
        R.drawable.voice_ee_51,
        R.drawable.voice_ee_52,

    };
    
    
    private static final AUIChatBottomBarIcon[] DATA = createData();
    
    private static AUIChatBottomBarIcon[] createData(){
        AUIChatBottomBarIcon[] datas = new AUIChatBottomBarIcon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new AUIChatBottomBarIcon(icons[i], "", emojis[i], AUIChatBottomBarIconType.NORMAL);
        }
        return datas;
    }
    
    public static AUIChatBottomBarIcon[] getData(){
        return DATA;
    }
}
