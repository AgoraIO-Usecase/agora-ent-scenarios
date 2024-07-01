package io.agora.imkitmanager.ui.basic;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AUISpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int spaceHorizontal;
    private final int spaceVertical;

    public AUISpaceItemDecoration(int spaceHorizontal, int spaceVertical) {
        this.spaceHorizontal = spaceHorizontal;
        this.spaceVertical = spaceVertical;
    }

    public int getSpaceHorizontal() {
        return spaceHorizontal;
    }

    public int getSpaceVertical() {
        return spaceVertical;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        int position = parent.getChildAdapterPosition(view);
        int size = adapter.getItemCount();

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int orientation = ((GridLayoutManager) layoutManager).getOrientation();
            if (orientation == LinearLayoutManager.VERTICAL) {
                int columnCount = ((GridLayoutManager) layoutManager).getSpanCount();
                int columnIndex = position % columnCount;
                int rowCount = size / columnCount;
                if(rowCount == 0){
                    return;
                }
                if (size % columnCount > 0) {
                    rowCount++;
                }
                int rowIndex = position / columnCount;

                outRect.left = columnIndex * spaceHorizontal / columnCount;
                outRect.right = spaceHorizontal - (columnIndex + 1) * spaceHorizontal / columnCount;
                outRect.top = rowIndex * spaceVertical / rowCount;
                outRect.bottom = spaceVertical - (rowIndex + 1) * spaceVertical / rowCount;
            } else {
                int rowCount = ((GridLayoutManager) layoutManager).getSpanCount();
                int rowIndex = position / rowCount;
                int columnCount = size / rowCount;
                if(columnCount == 0){
                    return;
                }
                if (size % rowCount > 0) {
                    columnCount++;
                }
                int columnIndex = position % columnCount;

                outRect.left = columnIndex * spaceHorizontal / columnCount;
                outRect.right = spaceHorizontal - (columnIndex + 1) * spaceHorizontal / columnCount;
                outRect.top = rowIndex * spaceVertical / rowCount;
                outRect.bottom = spaceVertical - (rowIndex + 1) * spaceVertical / rowCount;
            }
        } else if (layoutManager instanceof LinearLayoutManager) {
            int orientation = ((LinearLayoutManager) layoutManager).getOrientation();
            if (orientation == LinearLayoutManager.VERTICAL) {
                if (position != size - 1) {
                    outRect.bottom = spaceHorizontal;
                }
            } else {
                if (position != size - 1) {
                    outRect.right = spaceHorizontal;
                }
            }
        }


    }
}
