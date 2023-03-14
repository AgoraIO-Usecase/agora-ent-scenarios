package io.agora.scene.voice.ui.widget.expression;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import io.agora.scene.voice.R;

public class ExpressionGridAdapter extends ArrayAdapter<ExpressionIcon> {

    public ExpressionGridAdapter(Context context, int textViewResourceId, List<ExpressionIcon> objects) {
        super(context, textViewResourceId, objects);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
           convertView = View.inflate(getContext(), R.layout.voice_widget_row_expression, null);
        }
        ShapeableImageView imageView = convertView.findViewById(R.id.iv_expression);
        ExpressionIcon emojIcon = getItem(position);
        if(emojIcon.getIcon() != 0){
            imageView.setImageResource(emojIcon.getIcon());
        }else if(emojIcon.getIconPath() != null){
            Glide.with(getContext()).load(emojIcon.getIconPath())
                    .apply(RequestOptions.placeholderOf(R.drawable.voice_icon_default_expression))
                    .into(imageView);
        }
        return convertView;
    }
    
}
