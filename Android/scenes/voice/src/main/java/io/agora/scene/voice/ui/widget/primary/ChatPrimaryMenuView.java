package io.agora.scene.voice.ui.widget.primary;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.agora.scene.voice.R;
import io.agora.scene.voice.ui.widget.expression.ExpressionIcon;
import io.agora.scene.voice.ui.widget.expression.ExpressionView;
import io.agora.scene.voice.ui.widget.expression.SmileUtils;
import io.agora.voice.common.utils.DeviceTools;
import io.agora.voice.common.utils.LogTools;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ChatPrimaryMenuView extends RelativeLayout implements ExpressionView.ExpressionClickListener {

    protected FragmentActivity activity;
    protected InputMethodManager inputManager;
    private LinearLayoutCompat inputLayout;
    private LinearLayoutCompat menuLayout;
    private ArrayList<MenuItemModel> itemModels = new ArrayList<MenuItemModel>();
    private Map<Integer, MenuItemModel> itemMap = new HashMap();
    private MenuItemClickListener clickListener;
    private ConstraintLayout inputView;
    private AppCompatEditText edContent;
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
    private View mKeyboardBg;


    public ChatPrimaryMenuView(Context context) {
        this(context, null);
    }

    public ChatPrimaryMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatPrimaryMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = (FragmentActivity) context;
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
        mKeyboardBg = findViewById(R.id.vKeyboardBg);
        mSend.setText(R.string.voice_room_send_tip);
        if (window != null)
            rootView = window.getDecorView().findViewById(android.R.id.content);

        expressionView.setExpressionListener(this);
        expressionView.init(7);

        edContent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    LogTools.d("focus", "focused");
                } else {
                    LogTools.d("focus", "focus lost");
                    if (!isShowEmoji)
                        inputView.setVisibility(View.GONE);
                }
            }
        });

        edContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        // done stuff
                        sendMessage();
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        // next stuff
                        break;
                }
                return false;
            }
        });

        inputLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputView.setVisibility(View.VISIBLE);
                edContent.requestFocus();
                showInputMethod(edContent);
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
                sendMessage();
            }
        });

        new KeyboardStatusWatcher(activity, activity, new Function2<Boolean, Integer, Unit>() {
            @Override
            public Unit invoke(Boolean isKeyboardShowed, Integer keyboardHeight) {
                LogTools.d("KeyboardStatusWatcher"," isKeyboardShowed: " + isKeyboardShowed + " keyboardHeight: "+ keyboardHeight);
                ViewGroup.LayoutParams lp = mKeyboardBg.getLayoutParams();
                if (isKeyboardShowed){
                    lp.height = keyboardHeight;
                    softKeyHeight = keyboardHeight;
                }else {
                    if (!isShowEmoji){
                        lp.height = DeviceTools.dp2px(activity,55);
                        showNormalLayout();
                    }
                }
                mKeyboardBg.setLayoutParams(lp);
                return null;
            }
        });
    }

    public static void setViewLayoutParams(View view,int width,int height){
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = width;
        lp.height = height;
        view.setLayoutParams(lp);
    }

    public void SoftShowing(boolean isShowEmoji){
        LogTools.d("MenuView","SoftShowing: " + isShowEmoji);
        if (isShowEmoji){
            LogTools.d("KeyboardStatusWatcher","SoftShowing softKeyHeight: " + softKeyHeight);
            setViewLayoutParams(expressionView, ViewGroup.LayoutParams.MATCH_PARENT,softKeyHeight);
            setViewLayoutParams(mKeyboardBg, ViewGroup.LayoutParams.MATCH_PARENT,softKeyHeight);
        }else {
            setViewLayoutParams(expressionView, ViewGroup.LayoutParams.MATCH_PARENT,DeviceTools.dp2px(activity,55));
        }
    }

    public void addMenu( int drawableRes, int itemId){
        registerMenuItem(drawableRes,itemId);
        if(!itemMap.containsKey(itemId)) {
            ImageView imageView = new ImageView(activity);
            imageView.setLayoutParams(new LayoutParams(DeviceTools.dp2px(activity,38), DeviceTools.dp2px(activity,38)));
            imageView.setPadding(DeviceTools.dp2px(activity,7),DeviceTools.dp2px(activity,7)
                    ,DeviceTools.dp2px(activity,7),DeviceTools.dp2px(activity,7));
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
        LogTools.d("initMenu","roomType: " + roomType);
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
            LinearLayoutCompat.LayoutParams marginLayoutParams = new LinearLayoutCompat.LayoutParams(DeviceTools.dp2px(activity,38), DeviceTools.dp2px(activity,38));
            marginLayoutParams.leftMargin = DeviceTools.dp2px(activity,5);
            marginLayoutParams.setMarginStart(DeviceTools.dp2px(activity,5));
            imageView.setPadding(DeviceTools.dp2px(activity,4),DeviceTools.dp2px(activity,7)
                    ,DeviceTools.dp2px(activity,5),DeviceTools.dp2px(activity,7));
            imageView.setImageResource(itemModel.image);
            imageView.setBackgroundResource(R.drawable.voice_bg_primary_menu_item_icon);
            imageView.setId(itemModel.id);

            if (itemModel.id == R.id.voice_extend_item_gift){
                marginLayoutParams.setMarginEnd(DeviceTools.dp2px(activity,0));
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
                relativeLayout.setLayoutParams(new LayoutParams(DeviceTools.dp2px(activity,42), DeviceTools.dp2px(activity,38)));

                ImageView status = new ImageView(activity);
                status.setId(R.id.voice_extend_item_hand_up_status);
                status.setImageResource(R.drawable.voice_bg_primary_hand_status);
                status.setVisibility(GONE);

                RelativeLayout.LayoutParams imgLayout = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
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

    public void setEnableMic(boolean isOn){
        post(()-> {
            ImageView mic = menuLayout.findViewById(R.id.voice_extend_item_mic);
            if (isOn){
                mic.setImageResource(R.drawable.voice_icon_mic);
            }else {
                mic.setImageResource(R.drawable.voice_icon_close_mic);
            }
        });
    }

    public void showMicVisible(boolean isVisible, boolean isOn){
        post(()-> {
            ImageView mic = menuLayout.findViewById(R.id.voice_extend_item_mic);
            if (isOn){
                mic.setImageResource(R.drawable.voice_icon_mic);
            }else {
                mic.setImageResource(R.drawable.voice_icon_close_mic);
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
     * background of item
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
            LogTools.d("MenuView","hideKeyboard" + activity.getCurrentFocus());
            if (activity.getCurrentFocus() != null){
                LogTools.d("MenuView","hideKeyboard" + activity.getCurrentFocus());
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
            setViewLayoutParams(mKeyboardBg, ViewGroup.LayoutParams.MATCH_PARENT,DeviceTools.dp2px(activity,55));
        }
    }

    public void showInput(){
        inputView.setVisibility(View.GONE);
        inputLayout.setVisibility(VISIBLE);
        inputLayout.setEnabled(true);
    }

    public void sendMessage(){
        if (null != clickListener)
            clickListener.onSendMessage(edContent.getText().toString().trim());
        hideKeyboard();
        showNormalLayout();
    }

    public boolean showNormalLayout(){
        if(inputLayout.getVisibility() != View.VISIBLE){
            edContent.setText("");
            showInput();
            normalLayout.setVisibility(View.VISIBLE);
            menuLayout.setVisibility(View.VISIBLE);
            hideExpressionView(false);
            return true;
        }
        return false;
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

}
