// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.spatial.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import io.agora.scene.voice.spatial.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialCreatePageItemLayoutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ConstraintLayout itemLayout;

  @NonNull
  public final TextView itemText;

  @NonNull
  public final TextView itemTitle;

  private VoiceSpatialCreatePageItemLayoutBinding(@NonNull ConstraintLayout rootView,
      @NonNull ConstraintLayout itemLayout, @NonNull TextView itemText,
      @NonNull TextView itemTitle) {
    this.rootView = rootView;
    this.itemLayout = itemLayout;
    this.itemText = itemText;
    this.itemTitle = itemTitle;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialCreatePageItemLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialCreatePageItemLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_create_page_item_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialCreatePageItemLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.item_layout;
      ConstraintLayout itemLayout = ViewBindings.findChildViewById(rootView, id);
      if (itemLayout == null) {
        break missingId;
      }

      id = R.id.item_text;
      TextView itemText = ViewBindings.findChildViewById(rootView, id);
      if (itemText == null) {
        break missingId;
      }

      id = R.id.item_title;
      TextView itemTitle = ViewBindings.findChildViewById(rootView, id);
      if (itemTitle == null) {
        break missingId;
      }

      return new VoiceSpatialCreatePageItemLayoutBinding((ConstraintLayout) rootView, itemLayout,
          itemText, itemTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
