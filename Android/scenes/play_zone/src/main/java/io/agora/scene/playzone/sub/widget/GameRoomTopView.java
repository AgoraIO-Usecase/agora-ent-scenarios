package io.agora.scene.playzone.sub.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.agora.scene.playzone.R;

/**
 * 房间顶部的View
 */
public class GameRoomTopView extends ConstraintLayout {

    private TextView tvName;
    private TextView tvId;
    private View containerSelectGame;
    private TextView tvSelectGame;
    private ImageView ivMore;
    private LinearLayout endContainer;

    public GameRoomTopView(@NonNull Context context) {
        this(context, null);
    }

    public GameRoomTopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameRoomTopView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.view_game_room_top, this);
        tvName = findViewById(R.id.top_tv_name);
        tvId = findViewById(R.id.top_tv_room_id);
        containerSelectGame = findViewById(R.id.top_container_select_game);
        tvSelectGame = findViewById(R.id.top_tv_select_game);
        ivMore = findViewById(R.id.top_iv_more);
        endContainer = findViewById(R.id.end_container);
    }

    /** 设置房间名称 */
    public void setName(String value) {
        tvName.setText(value);
    }

    /** 设置房号 */
    public void setId(String value) {
        tvId.setText(value);
    }

    /** 设置选择游戏中的文字内容 */
    public void setSelectGameName(String value) {
        tvSelectGame.setText(value);
    }

    /** 设置选择游戏点击监听 */
    public void setSelectGameClickListener(OnClickListener listener) {
        containerSelectGame.setOnClickListener(listener);
    }

    /** 设置更多按钮的点击监听 */
    public void setMoreOnClickListener(OnClickListener listener) {
        ivMore.setOnClickListener(listener);
    }

    /** 设置选择游戏的可见性 */
    public void setSelectGameVisible(boolean isVisible) {
        containerSelectGame.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /** 添加一个自定义的View */
    public void addCustomView(View view, LinearLayout.LayoutParams params) {
        endContainer.addView(view, 0, params);
    }

}
