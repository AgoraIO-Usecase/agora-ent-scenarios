// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.spatial.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.spatial.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialDialogBottomSheetContentBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final AppCompatImageView ivTitle;

  @NonNull
  public final MaterialTextView mtContent;

  @NonNull
  public final MaterialTextView mtTitle;

  private VoiceSpatialDialogBottomSheetContentBinding(@NonNull ConstraintLayout rootView,
      @NonNull AppCompatImageView ivTitle, @NonNull MaterialTextView mtContent,
      @NonNull MaterialTextView mtTitle) {
    this.rootView = rootView;
    this.ivTitle = ivTitle;
    this.mtContent = mtContent;
    this.mtTitle = mtTitle;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialDialogBottomSheetContentBinding inflate(
      @NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialDialogBottomSheetContentBinding inflate(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_dialog_bottom_sheet_content, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialDialogBottomSheetContentBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.ivTitle;
      AppCompatImageView ivTitle = ViewBindings.findChildViewById(rootView, id);
      if (ivTitle == null) {
        break missingId;
      }

      id = R.id.mtContent;
      MaterialTextView mtContent = ViewBindings.findChildViewById(rootView, id);
      if (mtContent == null) {
        break missingId;
      }

      id = R.id.mtTitle;
      MaterialTextView mtTitle = ViewBindings.findChildViewById(rootView, id);
      if (mtTitle == null) {
        break missingId;
      }

      return new VoiceSpatialDialogBottomSheetContentBinding((ConstraintLayout) rootView, ivTitle,
          mtContent, mtTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
