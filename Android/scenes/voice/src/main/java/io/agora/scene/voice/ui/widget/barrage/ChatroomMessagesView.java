package io.agora.scene.voice.ui.widget.barrage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agora.scene.voice.R;
import io.agora.scene.voice.imkit.bean.ChatMessageData;
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper;
import io.agora.scene.voice.imkit.custorm.CustomMsgType;
import io.agora.scene.voice.imkit.manager.ChatroomIMManager;
import io.agora.scene.voice.ui.widget.expression.SmileUtils;
import io.agora.voice.common.utils.DeviceTools;
import io.agora.voice.common.utils.LogTools;
import io.agora.voice.common.utils.ThreadManager;

/**
 * MessagesView
 */
public class ChatroomMessagesView extends RelativeLayout{
    private ListAdapter adapter;
    private RecyclerView listview;
    private static final int ITEM_DEFAULT_TYPE = 0;
    private static final int ITEM_SYSTEM_TYPE = 1;
    private String chatroomId;
    private String ownerUid;
    private Context mContext;
    private boolean isScrollBottom;

    public ChatroomMessagesView(Context context) {
        super(context);
        init(context, null);
    }

    public ChatroomMessagesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatroomMessagesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.voice_widget_room_messages, this);
        listview = (RecyclerView) findViewById(R.id.listview);
    }

    public void init(String chatroomId,String ownerUid){
        this.chatroomId = chatroomId;
        this.ownerUid = ownerUid;
        adapter = new ListAdapter(getContext(), ChatroomIMManager.getInstance().getMessageData(chatroomId));
        ScrollSpeedLinearLayoutManger scrollSpeedLinearLayoutManger = new ScrollSpeedLinearLayoutManger(getContext());
        //设置item滑动速度
        scrollSpeedLinearLayoutManger.setSpeedSlow();
        listview.setLayoutManager(scrollSpeedLinearLayoutManger);
        //设置item 间距
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize(0, (int) DeviceTools.dp2px(getContext(), 6));
        itemDecoration.setDrawable(drawable);
        listview.addItemDecoration(itemDecoration);
        //设置item动画
//        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
//        defaultItemAnimator.setAddDuration(1000);
//        defaultItemAnimator.setRemoveDuration(1000);
//        defaultItemAnimator.setChangeDuration(1000);
//        listview.setItemAnimator(defaultItemAnimator);
        listview.setAdapter(adapter);

        listview.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (messageViewListener != null)
                    messageViewListener.onListClickListener();
                return false;
            }
        });

        listview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                int totalCount = lm.getItemCount();
                LogTools.d("onScrolled","lastVisibleItemPosition: " + lastVisibleItemPosition + " totalCount: " + (totalCount - 1));
                if (lastVisibleItemPosition == totalCount - 1 ) {
                    int findLastVisibleItemPosition = lm.findLastVisibleItemPosition();
                    if (findLastVisibleItemPosition == lm.getItemCount() - 1) {
                        ThreadManager.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                isScrollBottom = true;
                            }
                        });
                    }
                }else {
                    isScrollBottom = false;
                }
            }
        });
    }

    private MessageViewListener messageViewListener;

    public interface MessageViewListener{
        void onItemClickListener(ChatMessageData message);
        void onListClickListener();
    }

    public void setMessageViewListener(MessageViewListener messageViewListener){
        this.messageViewListener = messageViewListener;
    }

    public void refresh(){
        if(adapter != null){
            LogTools.d("refresh","isScrollBottom: " + isScrollBottom);
            if (isScrollBottom){
                refreshSelectLast();
            }else {
                adapter.refresh();
            }
        }
    }

    public void refreshSelectLast(){
        if (adapter != null) {
            adapter.refresh();
            if (adapter.getItemCount() > 0) {
                listview.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        }
    }


    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private final Context context;
        ArrayList<ChatMessageData> messages;


        public ListAdapter(Context context, ArrayList<ChatMessageData> dataArrayList){
            this.context = context;
            messages = dataArrayList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ITEM_DEFAULT_TYPE){
                return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.voice_widget_barrage_msgs_item, parent, false));
            }else if (viewType == ITEM_SYSTEM_TYPE){
                return new SystemViewHolder(LayoutInflater.from(context).inflate(R.layout.voice_widget_barrage_system_msgs_item, parent, false));
            }
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.voice_widget_barrage_msgs_item, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            final ChatMessageData message = messages.get(position);
            if(!TextUtils.isEmpty(message.getEvent()) && message.getEvent().equals(CustomMsgType.CHATROOM_SYSTEM.getName())) {
                return ITEM_SYSTEM_TYPE;
            }
            return ITEM_DEFAULT_TYPE;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            String from = "";
            final ChatMessageData message = messages.get(position);
            from = ChatroomIMManager.getInstance().getUserName(message);
            String s = message.getContent();
            if (holder instanceof MyViewHolder){
                if (TextUtils.isEmpty(from)){
                    from = message.getFrom();
                }
                showText(message.getFrom().equals(ownerUid),((MyViewHolder) holder).content, from, s);
            }else if (holder instanceof SystemViewHolder){
                from = ChatroomIMManager.getInstance().getSystemUserName(message);
                showSystemMsg(((SystemViewHolder) holder).name ,from,"");
            }

            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if (messageViewListener != null) {
                        messageViewListener.onItemClickListener(message);
                    }
                }
            });
        }

        private void showSystemMsg(TextView name,String nickName,String type) {
            StringBuilder builder = new StringBuilder();
            builder.append(nickName).append(" ").append(context.getString(R.string.voice_room_system_msg_member_add));
            SpannableString span = new SpannableString(builder.toString());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_color_8bb3ff)), 0, nickName.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_color_fcf0b3)),
                    nickName.length() + 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            name.setText(span);
        }

        private void showText(boolean isOwner,TextView con, String nickName,String content) {
            StringBuilder builder = new StringBuilder();
            if (isOwner){
                builder.append("O").append(nickName).append(" : ").append(content);
            }else {
                builder.append(nickName).append(" : ").append(content);
            }
            if (!TextUtils.isEmpty(builder.toString()) && SmileUtils.containsKey(builder.toString())){
                Spannable span1 = SmileUtils.getSmiledText(context, builder.toString());
                if (isOwner){
                    span1.setSpan(new CenteredImageSpan(mContext, R.drawable.voice_icon_owner,0,10),0,1,0);
                    span1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_color_8bb3ff)),
                            0, nickName.length()+4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    span1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_white)),
                            nickName.length() + 4, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    span1.setSpan(new StyleSpan(Typeface.BOLD),0,nickName.length()+4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }else {
                    span1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_color_8bb3ff)),
                            0, nickName.length()+3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    span1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_white)),
                            nickName.length() + 3, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    span1.setSpan(new StyleSpan(Typeface.BOLD),0,nickName.length()+3,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                con.setText(span1,TextView.BufferType.SPANNABLE);
                return;
            }
            SpannableString span = new SpannableString(builder.toString());
            if (isOwner){
                span.setSpan(new CenteredImageSpan(mContext, R.drawable.voice_icon_owner,0,10),0,1,0);
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_color_8bb3ff)),
                        0, nickName.length()+4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD),0,nickName.length()+4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_white)),
                        nickName.length() + 4, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else {
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_color_8bb3ff)),
                        0, nickName.length()+3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD),0,nickName.length()+3,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.voice_white)),
                        nickName.length() + 3, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            con.setText(span, TextView.BufferType.SPANNABLE);
        }

        @Override
        public int getItemCount() {
            if (messages != null && messages.size()>0){
                return messages.size();
            }else {
                return 0;
            }
        }

        public void refresh(){
            int startPosition = messages.size();
            messages.addAll(startPosition,CustomMsgHelper.getInstance().getNormalData(chatroomId));
            LogTools.d("room_refresh",messages.size()+" startPosition" + startPosition);

            if (messages.size() > 0){
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRangeInserted(startPosition,messages.size());
                    }
                });
            }
        }

    }


    private static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView content;
        public MyViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.content);
        }
    }

    private static class SystemViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView content;
        ImageView icon;
        public SystemViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            content = (TextView) itemView.findViewById(R.id.content);
            icon = itemView.findViewById(R.id.icon_system);
        }
    }

    /**
     * 控制滑动速度的LinearLayoutManager
     */
    public static class ScrollSpeedLinearLayoutManger extends LinearLayoutManager {
        private float MILLISECONDS_PER_INCH = 0.03f;
        private Context context;

        public ScrollSpeedLinearLayoutManger(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return ScrollSpeedLinearLayoutManger.this
                                    .computeScrollVectorForPosition(targetPosition);
                        }

                        //This returns the milliseconds it takes to
                        //scroll one pixel.
                        @Override
                        protected float calculateSpeedPerPixel
                        (DisplayMetrics displayMetrics) {
                            return MILLISECONDS_PER_INCH / displayMetrics.density;
                            //返回滑动一个pixel需要多少毫秒
                        }

                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }


        public void setSpeedSlow() {
            //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
            //0.3f是自己估摸的一个值，可以根据不同需求自己修改
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.3f;
        }

        public void setSpeedFast() {
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.03f;
        }
    }

    public static class CenteredImageSpan extends ImageSpan {
        private int mMarginLeft;
        private int mMarginRight;

        public CenteredImageSpan(Context context, final int drawableRes) {
            super(context, drawableRes);
        }

        public CenteredImageSpan(Context context,final int drawableRes, int marginLeft,int marginRight) {
            super(context,drawableRes);
            mMarginLeft = marginLeft;
            mMarginRight = marginRight;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {
            // image to draw
            Drawable b = getDrawable();
            // font metrics of text to be replaced
            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int transY = (y + fm.descent + y + fm.ascent) / 2
                    - b.getBounds().bottom / 2;

            canvas.save();
            canvas.translate(x-2, transY);
            b.draw(canvas);
            canvas.restore();
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
            return mMarginLeft + super.getSize(paint, text, start, end, fm) + mMarginRight;
        }
    }
}
