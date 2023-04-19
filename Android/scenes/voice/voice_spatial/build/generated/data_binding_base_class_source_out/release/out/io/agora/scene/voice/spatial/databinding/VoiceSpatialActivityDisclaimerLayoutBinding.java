// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.spatial.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.spatial.R;
import io.agora.scene.voice.spatial.ui.widget.titlebar.RoomTitleBar;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialActivityDisclaimerLayoutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final MaterialTextView content;

  @NonNull
  public final MaterialTextView end;

  @NonNull
  public final RoomTitleBar titleBar;

  private VoiceSpatialActivityDisclaimerLayoutBinding(@NonNull ConstraintLayout rootView,
      @NonNull MaterialTextView content, @NonNull MaterialTextView end,
      @NonNull RoomTitleBar titleBar) {
    this.rootView = rootView;
    this.content = content;
    this.end = end;
    this.titleBar = titleBar;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialActivityDisclaimerLayoutBinding inflate(
      @NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialActivityDisclaimerLayoutBinding inflate(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_activity_disclaimer_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialActivityDisclaimerLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.content;
      MaterialTextView content = ViewBindings.findChildViewById(rootView, id);
      if (content == null) {
        break missingId;
      }

      id = R.id.end;
      MaterialTextView end = ViewBindings.findChildViewById(rootView, id);
      if (end == null) {
        break missingId;
      }

      id = R.id.title_bar;
      RoomTitleBar titleBar = ViewBindings.findChildViewById(rootView, id);
      if (titleBar == null) {
        break missingId;
      }

      return new VoiceSpatialActivityDisclaimerLayoutBinding((ConstraintLayout) rootView, content,
          end, titleBar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
