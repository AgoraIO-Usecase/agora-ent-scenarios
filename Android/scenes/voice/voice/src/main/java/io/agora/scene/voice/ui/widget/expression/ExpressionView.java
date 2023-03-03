package io.agora.scene.voice.ui.widget.expression;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import java.util.Arrays;

import io.agora.voice.common.utils.DeviceTools;
import io.agora.scene.voice.R;

public class ExpressionView extends LinearLayoutCompat {
   private Context context;
   private int mColumns = 7;
   private ExpressionClickListener listener;

   public ExpressionView(@NonNull Context context) {
      super(context);
   }

   public ExpressionView(@NonNull Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
      this.context = context;
   }

   public void init(int Columns){
      this.mColumns = Columns;
      View view = LayoutInflater.from(context).inflate(R.layout.voice_widget_expression_gridview, this);
      GridView gv = (GridView) view.findViewById(R.id.gridview);
      ImageView iv_emoji_delete = view.findViewById(R.id.iv_emoji_delete);

      gv.setVerticalSpacing((int) DeviceTools.dp2px(getContext(), 20));
      gv.setNumColumns(mColumns);
      gv.setVerticalSpacing(40);

      final ExpressionGridAdapter gridAdapter = new ExpressionGridAdapter(context, 1, Arrays.asList(DefaultExpressionData.getData()));
      gv.setAdapter(gridAdapter);

      iv_emoji_delete.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (listener != null)
            listener.onDeleteImageClicked();
         }
      });

      gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ExpressionIcon emojIcon = gridAdapter.getItem(position);
            if(listener != null){
               listener.onExpressionClicked(emojIcon);
            }
         }
      });
   }

   public void setExpressionListener(ExpressionClickListener expressionListener){
      this.listener = expressionListener;
   }

   public interface ExpressionClickListener{
      void onDeleteImageClicked();
      void onExpressionClicked(ExpressionIcon emojicon);
   }

}
