package io.agora.scene.voice.ui.widget.gift;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.agora.scene.voice.R;
import io.agora.scene.voice.model.GiftBean;
import io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.voice.common.utils.LogTools;

public class GiftListAdapter extends RoomBaseRecyclerViewAdapter<GiftBean> {
    private int selectedPosition = -1;

    @Override
    public GiftViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GiftViewHolder(LayoutInflater.from(mContext).inflate(R.layout.voice_item_gift_list_layout, parent, false));
    }

    private class GiftViewHolder extends ViewHolder<GiftBean> {
        private ImageView ivGift;
        private TextView tvGiftName;
        private TextView price;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            ivGift = findViewById(R.id.iv_gift);
            tvGiftName = findViewById(R.id.tv_gift_name);
            price = findViewById(R.id.price);
        }

        @Override
        public void setData(GiftBean item, int position) {
            LogTools.e("GiftListAdapter","setData: " + position);
            ivGift.setImageResource(item.getResource());
            tvGiftName.setText(item.getName());
            price.setText(item.getPrice());

            if(selectedPosition == position) {
                item.setChecked(true);
                itemView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.voice_bg_gift_selected_shape));
            }else {
                item.setChecked(false);
                itemView.setBackground(null);
            }
        }
    }


    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }


}
