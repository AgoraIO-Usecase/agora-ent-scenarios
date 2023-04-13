package io.agora.scene.voice.ui.widget.expression;

import io.agora.scene.voice.R;

public class DefaultExpressionData {
    
    private static String[] emojis = new String[]{
        SmileUtils.ee_1,
        SmileUtils.ee_2,
        SmileUtils.ee_3,
        SmileUtils.ee_4,
        SmileUtils.ee_5,
        SmileUtils.ee_6,
        SmileUtils.ee_7,
        SmileUtils.ee_8,
        SmileUtils.ee_9,
        SmileUtils.ee_10,
        SmileUtils.ee_11,
        SmileUtils.ee_12,
        SmileUtils.ee_13,
        SmileUtils.ee_14,
        SmileUtils.ee_15,
        SmileUtils.ee_16,
        SmileUtils.ee_17,
        SmileUtils.ee_18,
        SmileUtils.ee_19,
        SmileUtils.ee_20,
        SmileUtils.ee_21,
        SmileUtils.ee_22,
        SmileUtils.ee_23,
        SmileUtils.ee_24,
        SmileUtils.ee_25,
        SmileUtils.ee_26,
        SmileUtils.ee_27,
        SmileUtils.ee_28,
        SmileUtils.ee_29,
        SmileUtils.ee_30,
        SmileUtils.ee_31,
        SmileUtils.ee_32,
        SmileUtils.ee_33,
        SmileUtils.ee_34,
        SmileUtils.ee_35,
        SmileUtils.ee_36,
        SmileUtils.ee_37,
        SmileUtils.ee_38,
        SmileUtils.ee_39,
        SmileUtils.ee_40,
        SmileUtils.ee_41,
        SmileUtils.ee_42,
        SmileUtils.ee_43,
        SmileUtils.ee_44,
        SmileUtils.ee_45,
        SmileUtils.ee_46,
        SmileUtils.ee_47,
        SmileUtils.ee_48,
        SmileUtils.ee_49,
        SmileUtils.ee_50,
        SmileUtils.ee_51,
        SmileUtils.ee_52,
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
    
    
    private static final ExpressionIcon[] DATA = createData();
    
    private static ExpressionIcon[] createData(){
        ExpressionIcon[] datas = new ExpressionIcon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new ExpressionIcon(icons[i], emojis[i], ExpressionIcon.Type.NORMAL);
        }
        return datas;
    }
    
    public static ExpressionIcon[] getData(){
        return DATA;
    }
}
