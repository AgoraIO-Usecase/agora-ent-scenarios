package io.agora.scene.voice.ui.widget.primary;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.agora.scene.voice.R;
import io.agora.scene.voice.ui.widget.expression.ExpressionIcon;
import io.agora.scene.voice.ui.widget.expression.ExpressionView;
import io.agora.scene.voice.ui.widget.expression.SmileUtils;

public class ChatPrimaryMenuView extends RelativeLayout implements ExpressionView.ExpressionClickListener {

    protected Activity activity;
    protected InputMethodManager inputManager;
    private LinearLayoutCompat inputLayout;
    private LinearLayoutCompat menuLayout;
    private ArrayList<MenuItemModel> itemModels = new ArrayList<MenuItemModel>();
    private Map<Integer, MenuItemModel> itemMap = new HashMap();
    private MenuItemClickListener clickListener;
    private ConstraintLayout inputView;
    private EditText edContent;
    private ImageView icon;
    private TextView mSend;
    private boolean isShowEmoji;
    private RelativeLayout normalLayout;
    private ExpressionView expressionView;
    private ViewGroup rootView;
    private View view;
    private int softKeyHeight = 0;
    private int mWindowHeight,mExpressionHeight = 0;
    private boolean isSoftShowing;
    private Window window;


    public ChatPrimaryMenuView(Context context) {
        this(context, null);
    }

    public ChatPrimaryMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatPrimaryMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = (Activity) context;
        if (activity !=null) window = activity.getWindow();
        view = LayoutInflater.from(context).inflate(R.layout.voice_widget_primary_menu_layout, this);
        inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initViews();
    }

    private void initViews() {
        menuLayout = findViewById(R.id.menu_layout);
        inputLayout = findViewById(R.id.input_layout);
        inputView = findViewById(R.id.input_view);
        edContent = findViewById(R.id.input_edit_view);
        icon = findViewById(R.id.icon_emoji);
        mSend = findViewById(R.id.input_send);
        normalLayout = findViewById(R.id.normal_layout);
        expressionView = findViewById(R.id.expression_view);
        if (window != null)
        rootView = window.getDecorView().findViewById(android.R.id.content);

        expressionView.setExpressionListener(this);
        expressionView.init(7);

        edContent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d("focus", "focused");
                } else {
                    Log.d("focus", "focus lost");
                    inputView.setVisibility(View.GONE);
                }
            }
        });
        inputLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputView.setVisibility(View.VISIBLE);
                edContent.requestFocus();
                showInputMethod(edContent);
                SoftShowing(true);
                expressionView.setVisibility(View.INVISIBLE);
                inputLayout.setVisibility(GONE);
                inputLayout.setEnabled(false);
                hindViewChangeIcon();
                isShowEmoji = false;
                if (null != clickListener)
                    clickListener.onInputLayoutClick();
            }
        });
        icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowEmoji = !isShowEmoji;
                SoftShowing(isShowEmoji);
                checkShowExpression(isShowEmoji);
            }
        });
        mSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickListener)
                    clickListener.onSendMessage(edContent.getText().toString().trim());
                edContent.setText("");
                hideKeyboard();
                inputView.setVisibility(View.GONE);
                normalLayout.setVisibility(View.VISIBLE);
                menuLayout.setVisibility(View.VISIBLE);
                inputLayout.setVisibility(View.VISIBLE);
                inputLayout.setEnabled(true);
                hideExpressionView(false);

            }
        });

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //获取当前窗口实际的可见区域
                rootView.getWindowVisibleDisplayFrame(r);
                int height = r.height();
                int rootWidth = r.width();
                if (mWindowHeight == 0) {
                    //一般情况下，这是原始的窗口高度
                    mWindowHeight = height;
                    setViewLayoutParams(expressionView,rootWidth,0);
//                    System.out.println("SoftKeyboard height0 = " + 0);
                } else {
                    if (softKeyHeight == 0){
                        softKeyHeight = 765;
                    }
//                    Log.e("onGlobalLayout","softKeyHeight: " + softKeyHeight);
                    if (mWindowHeight != height) {
                        Log.e("ChatPrimaryMenuView","checkDeviceHasNavigationBar: " +  checkDeviceHasNavigationBar(activity));
                        Log.e("ChatPrimaryMenuView","getNavigationBarHeight: " + getNavigationBarHeight(activity));
                        if (checkDeviceHasNavigationBar(activity)){
                            if (getNavigationBarHeight(activity) > 100){
                                //两次窗口高度相减，就是软键盘高度
                                softKeyHeight = mWindowHeight - height - getNavigationBarHeight(activity);
                            }else {
                                //两次窗口高度相减，就是软键盘高度
                                softKeyHeight = mWindowHeight - height;
                            }
                            isSoftShowing = true;
                        }else {
                            softKeyHeight = 765;
                        }
//                        System.out.println("SoftKeyboard height1 = " + softKeyHeight);
                        setViewLayoutParams(expressionView,rootWidth,softKeyHeight);
                    }
                    else {
                        isSoftShowing = false;
                        if (!isShowEmoji){
                            setViewLayoutParams(expressionView,rootWidth,0);
                        }else {
                            setViewLayoutParams(expressionView,rootWidth,softKeyHeight);
                        }
//                        System.out.println("SoftKeyboard height2 = " + 0);
                    }
                }
            }
        });
    }

    public static void setViewLayoutParams(View view,int width,int height){
        ViewGroup.LayoutParams lp = view.getLayoutParams();
//        Log.e("setViewLayoutParams","\n lp.height: " + lp.height +  "\n height: "+ height + "\n lp.width" +lp.width + "\n width:" +width );
        if (lp.height != height || lp.width != width){
            lp.width = width;
            lp.height = height;
            view.setLayoutParams(lp);
        }
    }

    public void SoftShowing(boolean isShowEmoji){
        Log.e("MenuView","SoftShowing: " + isShowEmoji);
        if (isShowEmoji){
            setViewLayoutParams(expressionView, ViewGroup.LayoutParams.MATCH_PARENT,softKeyHeight);
        }else {
            setViewLayoutParams(expressionView, ViewGroup.LayoutParams.MATCH_PARENT,0);
        }
    }

    public void addMenu( int drawableRes, int itemId){
        registerMenuItem(drawableRes,itemId);
        if(!itemMap.containsKey(itemId)) {
            ImageView imageView = new ImageView(activity);
            imageView.setLayoutParams(new LayoutParams(dp2px(activity,38), dp2px(activity,38)));
            imageView.setPadding(dp2px(activity,7),dp2px(activity,7)
                    ,dp2px(activity,7),dp2px(activity,7));
            imageView.setImageResource(drawableRes);
            imageView.setBackgroundResource(R.drawable.voice_bg_primary_menu_item_icon);
            imageView.setId(itemId);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != clickListener)
                    clickListener.onChatExtendMenuItemClick(v.getId(),v);
                }
            });
            menuLayout.addView(imageView);
        }
    }

    public void initMenu(int roomType) {
        Log.e("initMenu","roomType: " + roomType);
        if (roomType == 0){
            normalLayout.setVisibility(VISIBLE);
            registerMenuItem(R.drawable.voice_icon_close_mic,R.id.voice_extend_item_mic);
            registerMenuItem(R.drawable.voice_icon_handuphard,R.id.voice_extend_item_hand_up);
            registerMenuItem(R.drawable.voice_icon_eq,R.id.voice_extend_item_eq);
            registerMenuItem(R.drawable.voice_icon_gift,R.id.voice_extend_item_gift);
            addView();
        }else if (roomType == 1){
            normalLayout.setVisibility(VISIBLE);
            inputLayout.setVisibility(INVISIBLE);
            menuLayout.setVisibility(VISIBLE);
            registerMenuItem(R.drawable.voice_icon_close_mic,R.id.voice_extend_item_mic);
            registerMenuItem(R.drawable.voice_icon_handuphard,R.id.voice_extend_item_hand_up);
            registerMenuItem(R.drawable.voice_icon_eq,R.id.voice_extend_item_eq);
            addView();
        }
    }

    private void addView(){
        for (MenuItemModel itemModel : itemModels) {
            ImageView imageView = new ImageView(activity);
            LinearLayoutCompat.LayoutParams marginLayoutParams = new LinearLayoutCompat.LayoutParams(dp2px(activity,38), dp2px(activity,38));
            marginLayoutParams.leftMargin = dp2px(activity,5);
            marginLayoutParams.setMarginStart(dp2px(activity,5));
            imageView.setPadding(dp2px(activity,4),dp2px(activity,7)
                    ,dp2px(activity,5),dp2px(activity,7));
            imageView.setImageResource(itemModel.image);
            imageView.setBackgroundResource(R.drawable.voice_bg_primary_menu_item_icon);
            imageView.setId(itemModel.id);

            if (itemModel.id == R.id.voice_extend_item_gift){
                marginLayoutParams.setMarginEnd(dp2px(activity,0));
            }
            imageView.setLayoutParams(marginLayoutParams);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != clickListener)
                        clickListener.onChatExtendMenuItemClick(v.getId(),v);
                }
            });
            if (itemModel.id == R.id.voice_extend_item_hand_up){
                RelativeLayout relativeLayout = new RelativeLayout(activity);
                relativeLayout.setLayoutParams(new LayoutParams(dp2px(activity,42), dp2px(activity,38)));

                ImageView status = new ImageView(activity);
                status.setId(R.id.voice_extend_item_hand_up_status);
                status.setImageResource(R.drawable.voice_bg_primary_hand_status);
                status.setVisibility(GONE);

                LayoutParams imgLayout = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                imgLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP | ALIGN_PARENT_RIGHT);
                imgLayout.setMargins(0,18,18,0);
                relativeLayout.addView(imageView, marginLayoutParams);
                relativeLayout.addView(status, imgLayout);
                menuLayout.addView(relativeLayout);
                continue;
            }
            menuLayout.addView(imageView);
        }
    }

    public void setShowHandStatus(boolean isOwner,boolean isShowHandStatus){
        post(()-> {
            if (isOwner){
                ImageView handStatus = menuLayout.findViewById(R.id.voice_extend_item_hand_up_status);
                if (isShowHandStatus){
                    handStatus.setVisibility(VISIBLE);
                }else {
                    handStatus.setVisibility(GONE);
                }
            }else {
                ImageView hand = menuLayout.findViewById(R.id.voice_extend_item_hand_up);
                if (isShowHandStatus){
                    hand.setImageResource(R.drawable.voice_icon_handup_dot);
                }else {
                    hand.setImageResource(R.drawable.voice_icon_handuphard);
                }
            }
        });
    }

    public void setEnableHand(boolean isEnable){
        post(()-> {
            ImageView hand = menuLayout.findViewById(R.id.voice_extend_item_hand_up);
            if (isEnable){
                hand.setImageResource(R.drawable.voice_icon_vector);
                hand.setEnabled(false);
            }else {
                hand.setImageResource(R.drawable.voice_icon_handuphard);
                hand.setEnabled(true);
            }
        });
    }

    public void setEnableMic(boolean isEnable){
        post(()-> {
            ImageView mic = menuLayout.findViewById(R.id.voice_extend_item_mic);
            if (isEnable){
                mic.setImageResource(R.drawable.voice_icon_mic);
            }else {
                mic.setImageResource(R.drawable.voice_icon_close_mic);
            }
        });
    }

    public void showMicVisible(boolean muteLocal,boolean isVisible){
        post(()-> {
            ImageView mic = menuLayout.findViewById(R.id.voice_extend_item_mic);
            if (muteLocal) {
                mic.setImageResource(R.drawable.voice_icon_close_mic);
            } else {
                mic.setImageResource(R.drawable.voice_icon_mic);
            }
            if (isVisible){
                mic.setVisibility(VISIBLE);
            }else {
                mic.setVisibility(GONE);
            }
        });
    }

    /**
         * register menu item
         *
         * @param drawableRes
         *            background of item
         * @param itemId
         *             id
         */
    public void registerMenuItem( int drawableRes, int itemId) {
        if(!itemMap.containsKey(itemId)) {
            MenuItemModel item = new MenuItemModel();
            item.image = drawableRes;
            item.id = itemId;
            itemModels.add(item);
        }
    }

    public void checkShowExpression(boolean isShow){
        isShowEmoji = isShow;
        if (isShowEmoji){
            icon.setImageResource(R.drawable.voice_icon_key);
            expressionView.setVisibility(VISIBLE);
            hideKeyboard();
        }else {
            icon.setImageResource(R.drawable.voice_icon_face);
            expressionView.setVisibility(INVISIBLE);
            showInputMethod(edContent);
        }
    }

    public void hindViewChangeIcon(){
        icon.setImageResource(R.drawable.voice_icon_face);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm!=null && activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            Log.e("MenuView","hideKeyboard" + activity.getCurrentFocus());
            if (activity.getCurrentFocus() != null){
                Log.e("MenuView","hideKeyboard" + activity.getCurrentFocus());
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public void showInputMethod(EditText editText){
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onDeleteImageClicked() {
        if (!TextUtils.isEmpty(edContent.getText())) {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            edContent.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onExpressionClicked(ExpressionIcon expressionIcon) {
        if(expressionIcon != null) {
            edContent.append(SmileUtils.getSmiledText(getContext(),expressionIcon.getLabelString()));
        }
    }

    public void hideExpressionView(Boolean isShowEx){
        if (isShowEx){
            expressionView.setVisibility(View.VISIBLE);
        }else {
            expressionView.setVisibility(View.GONE);
        }
    }

    public void showInput(){
        inputView.setVisibility(View.GONE);
        inputLayout.setVisibility(VISIBLE);
        inputLayout.setEnabled(true);
    }

    public static class MenuItemModel{
        public int image;
        public int id;
    }

    public void setMenuItemOnClickListener(MenuItemClickListener listener){
        this.clickListener = listener;
    }


    public EditText getEdContent(){
        return edContent;
    }

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @SuppressWarnings("unused")
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;

        Resources rs = context.getResources();

        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");

        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);

        }

        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");

            Method m = systemPropertiesClass.getMethod("get", String.class);

            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");

            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;

            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;

            }

        } catch (Exception e) {
        }

        return hasNavigationBar;

    }

    // 获取NavigationBar高度
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();

        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");

        int height = resources.getDimensionPixelSize(resourceId);

        return height;

    }

}
