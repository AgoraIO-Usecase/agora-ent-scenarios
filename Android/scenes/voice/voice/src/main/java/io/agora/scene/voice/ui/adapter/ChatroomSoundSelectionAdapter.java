package io.agora.scene.voice.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import io.agora.scene.voice.model.CustomerUsageBean;
import io.agora.scene.voice.model.SoundSelectionBean;
import io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.voice.common.utils.DeviceTools;
import io.agora.voice.common.utils.ResourcesTools;
import io.agora.scene.voice.R;

public class ChatroomSoundSelectionAdapter extends RoomBaseRecyclerViewAdapter<SoundSelectionBean> {
    private static int selectedPosition = -1;
    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener listener;

    public ChatroomSoundSelectionAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder<SoundSelectionBean> getViewHolder(ViewGroup parent, int viewType) {
        return new soundViewHold(LayoutInflater.from(parent.getContext()).inflate(R.layout.voice_item_sound_selection, parent, false));
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public class soundViewHold extends ViewHolder<SoundSelectionBean> {
        private ConstraintLayout item;
        private MaterialTextView sound_name;
        private MaterialTextView sound_desc;
        private LinearLayout layout;
        private AppCompatImageView icon;
        private Context context;
        private MaterialCardView cardView;

        public soundViewHold(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            item = itemView.findViewById(R.id.item);
            sound_name = itemView.findViewById(R.id.sound_name);
            sound_desc = itemView.findViewById(R.id.sound_desc);
            layout = itemView.findViewById(R.id.llSoundCustomerUsage);
            icon = itemView.findViewById(R.id.ivSoundSelected);
            cardView = itemView.findViewById(R.id.mcvSoundSelectionContent);
        }

        @Override
        public void setData(SoundSelectionBean bean, int position) {
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.OnItemClick(position, bean);
                    }
                }
            });
            sound_name.setText(bean.getSoundName());
            sound_desc.setText(bean.getSoundIntroduce());
            for (CustomerUsageBean customerUsageBean : Objects.requireNonNull(bean.getCustomer())) {
                ImageView imageView = new ImageView(context);
                LinearLayoutCompat.LayoutParams marginLayoutParams = new LinearLayoutCompat.LayoutParams(DeviceTools.dp2px(context, 20), DeviceTools.dp2px(context, 20));
                marginLayoutParams.rightMargin = DeviceTools.dp2px(context, 10);
                imageView.setImageResource(customerUsageBean.getAvatar());
                imageView.setLayoutParams(marginLayoutParams);
                if (layout.getChildCount() < bean.getCustomer().size()) {
                    layout.addView(imageView);
                }
            }
            if (selectedPosition == position) {
                icon.setVisibility(View.VISIBLE);
                cardView.setStrokeColor(ResourcesTools.getColor(mContext.getResources(), R.color.voice_color_009fff, null));
            } else {
                icon.setVisibility(View.GONE);
                cardView.setStrokeColor(ResourcesTools.getColor(mContext.getResources(), R.color.voice_color_d8d8d8, null));
            }
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int position, SoundSelectionBean bean);
    }

    public void SetOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
