package io.agora.scene.voice.ui.widget.gift;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.voice.R;

public class GiftAdapter extends ArrayAdapter<String> {
    private String[] data;
    private Context context;
    private OnItemClickListener listener;

    public GiftAdapter(@NonNull Context context, int resource, String[] objects) {
        super(context, resource,objects);
        this.data = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String ss = getItem(position);//获得数据的实例
        View view;
        ViewHolder holder;
        if (convertView == null){//反复利用布局
            view = LayoutInflater.from(getContext()).inflate(R.layout.voice_pop_item_gift_layout,null);//加载每个item的布局
            holder = new ViewHolder();
            holder.select_count = view.findViewById(R.id.select_count);//布局中的TextView实例
            view.setTag(holder);
        }else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.select_count.setText(ss);
        holder.select_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.OnItemClick(position,ss);
                }
            }
        });
        return view;
    }

    static class ViewHolder{
        TextView select_count;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void OnItemClick(int position,String count);
    }
}
