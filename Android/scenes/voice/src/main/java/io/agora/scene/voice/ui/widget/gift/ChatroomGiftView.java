package io.agora.scene.voice.ui.widget.gift;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.utils.KtExtendKt;
import io.agora.scene.voice.R;
import io.agora.scene.voice.VoiceLogger;
import io.agora.scene.voice.imkit.bean.ChatMessageData;
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper;
import io.agora.scene.voice.imkit.manager.ChatroomIMManager;
import io.agora.scene.voice.model.GiftBean;

public class ChatroomGiftView extends LinearLayout {
    private RecyclerView recyclerView;
    private String chatroomId;
    private GiftListAdapter adapter;
    private Context mContext;
    private Handler handler = new Handler();
    private Runnable task;
    private int delay = 3000;

    private void startTask() {
        stopTask();
        handler.postDelayed(task = new Runnable() {
            @Override
            public void run() {
                if (adapter.messages.size() > 0) {
                    adapter.removeAll();
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void stopTask() {
        if (task != null) {
            handler.removeCallbacks(task);
            task = null;
        }
    }

    public void clear() {
        if (task != null) {
            removeCallbacks(task);
        }
    }


    public ChatroomGiftView(Context context) {
        super(context);
        init(context, null);
    }

    public ChatroomGiftView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.voice_widget_gift_layout, this);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    public void init(String chatroomId) {
        this.chatroomId = chatroomId;
        adapter = new GiftListAdapter(mContext, CustomMsgHelper.getInstance().getGiftData(chatroomId));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);


        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize(0, (int)  KtExtendKt.getDp(  6));
        itemDecoration.setDrawable(drawable);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    default:
                        break;
                }
                return true;
            }
        });

        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(500);
        defaultItemAnimator.setRemoveDuration(500);
        recyclerView.setItemAnimator(defaultItemAnimator);

    }

    public void refresh() {
        adapter.refresh();
        if (adapter.getItemCount() > 0){
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
        clearTiming();
    }

    private void clearTiming() {
        if (getChildCount() > 0) {
            startTask();
        }
    }

    private void removeAllGiftView() {
        adapter.removeAll();
    }

    private class GiftListAdapter extends RecyclerView.Adapter<GiftViewHolder> {
        private Context context;
        ArrayList<ChatMessageData> messages;

        GiftListAdapter(Context context, ArrayList<ChatMessageData> dataArrayList) {
            this.context = context;
            messages = dataArrayList;
        }

        @Override
        public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GiftViewHolder(LayoutInflater.from(context).inflate(R.layout.voice_widget_gift_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
            final ChatMessageData message = messages.get(position);
            show(holder.avatar, holder.icon, holder.name, message);
            if (mContext != null) {
                Typeface fromAsset = Typeface.createFromAsset(mContext.getAssets(), "fonts/RobotoNembersVF.ttf");
                holder.icon_count.setTypeface(fromAsset);
            }
            holder.icon_count.setText("x" + CustomMsgHelper.getInstance().getMsgGiftNum(message));
        }

        public void show(ShapeableImageView avatar, ImageView icon, TextView name, ChatMessageData message) {
            int resId = 0;
            String gift_id = CustomMsgHelper.getInstance().getMsgGiftId(message);
            String userName = ChatroomIMManager.getInstance().getUserName(message);
            String userPortrait = ChatroomIMManager.getInstance().getUserPortrait(message);
            GiftBean giftBean = GiftRepository.getGiftById(context, gift_id);
            try {
                resId = context.getResources().getIdentifier(userPortrait, "drawable", context.getPackageName());
            } catch (Exception ignored) {
                VoiceLogger.e("getResources()", ignored.getMessage());
            }
            if (resId != 0) {
                avatar.setImageResource(resId);
            }else {
                GlideApp.with(avatar)
                        .load(userPortrait)
                        .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(avatar);
            }
            StringBuilder builder = new StringBuilder();
            if (null != giftBean) {
                builder.append(!TextUtils.isEmpty(userName) ? userName : message.getFrom()).append(":").append("\n").append(context.getString(R.string.voice_gift_sent)).append(" ").append(giftBean.getName());
                icon.setImageResource(giftBean.getResource());
            }
            SpannableString span = new SpannableString(builder.toString());
            name.setText(span);
        }

        @Override
        public int getItemCount() {
            if (messages != null && messages.size()>0){
                VoiceLogger.d("gift_view", "messages.size()" + messages.size());
                return messages.size();
            }else {
                return 0;
            }
        }


        public void removeAll() {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved(0, messages.size());
                    messages.clear();
                }
            });
        }

        public void refresh() {
            int positionStart = messages.size();
            VoiceLogger.d("room_refresh", "positionStart1 " + positionStart);
            messages.addAll(CustomMsgHelper.getInstance().getGiftData(chatroomId));
            VoiceLogger.d("room_refresh", messages.size() + " positionStart: " + positionStart);
            if (messages.size() > 0) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRangeInserted(positionStart, messages.size());
                    }
                });
            }
        }
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        MaterialTextView icon_count;
        ShapeableImageView avatar;

        public GiftViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.nick_name);
            icon = itemView.findViewById(R.id.icon);
            icon_count = itemView.findViewById(R.id.gift_count);
        }
    }
}


