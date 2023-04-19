// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.spatial.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import io.agora.scene.voice.spatial.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialWidgetTitleBarLayoutBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final ImageView leftImage;

  @NonNull
  public final RelativeLayout leftLayout;

  @NonNull
  public final ImageView rightImage;

  @NonNull
  public final RelativeLayout rightLayout;

  @NonNull
  public final TextView rightMenu;

  @NonNull
  public final RelativeLayout root;

  @NonNull
  public final TextView title;

  @NonNull
  public final Toolbar toolbar;

  private VoiceSpatialWidgetTitleBarLayoutBinding(@NonNull RelativeLayout rootView,
      @NonNull ImageView leftImage, @NonNull RelativeLayout leftLayout,
      @NonNull ImageView rightImage, @NonNull RelativeLayout rightLayout,
      @NonNull TextView rightMenu, @NonNull RelativeLayout root, @NonNull TextView title,
      @NonNull Toolbar toolbar) {
    this.rootView = rootView;
    this.leftImage = leftImage;
    this.leftLayout = leftLayout;
    this.rightImage = rightImage;
    this.rightLayout = rightLayout;
    this.rightMenu = rightMenu;
    this.root = root;
    this.title = title;
    this.toolbar = toolbar;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialWidgetTitleBarLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialWidgetTitleBarLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_widget_title_bar_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialWidgetTitleBarLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.left_image;
      ImageView leftImage = ViewBindings.findChildViewById(rootView, id);
      if (leftImage == null) {
        break missingId;
      }

      id = R.id.left_layout;
      RelativeLayout leftLayout = ViewBindings.findChildViewById(rootView, id);
      if (leftLayout == null) {
        break missingId;
      }

      id = R.id.right_image;
      ImageView rightImage = ViewBindings.findChildViewById(rootView, id);
      if (rightImage == null) {
        break missingId;
      }

      id = R.id.right_layout;
      RelativeLayout rightLayout = ViewBindings.findChildViewById(rootView, id);
      if (rightLayout == null) {
        break missingId;
      }

      id = R.id.right_menu;
      TextView rightMenu = ViewBindings.findChildViewById(rootView, id);
      if (rightMenu == null) {
        break missingId;
      }

      RelativeLayout root = (RelativeLayout) rootView;

      id = R.id.title;
      TextView title = ViewBindings.findChildViewById(rootView, id);
      if (title == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      return new VoiceSpatialWidgetTitleBarLayoutBinding((RelativeLayout) rootView, leftImage,
          leftLayout, rightImage, rightLayout, rightMenu, root, title, toolbar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
