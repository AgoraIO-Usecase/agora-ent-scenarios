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

public final class VoiceSpatialDialogBottomSheetSingleBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final AppCompatImageView ivTitle;

  @NonNull
  public final MaterialTextView mbCancel;

  private VoiceSpatialDialogBottomSheetSingleBinding(@NonNull ConstraintLayout rootView,
      @NonNull AppCompatImageView ivTitle, @NonNull MaterialTextView mbCancel) {
    this.rootView = rootView;
    this.ivTitle = ivTitle;
    this.mbCancel = mbCancel;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialDialogBottomSheetSingleBinding inflate(
      @NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialDialogBottomSheetSingleBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_dialog_bottom_sheet_single, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialDialogBottomSheetSingleBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.ivTitle;
      AppCompatImageView ivTitle = ViewBindings.findChildViewById(rootView, id);
      if (ivTitle == null) {
        break missingId;
      }

      id = R.id.mbCancel;
      MaterialTextView mbCancel = ViewBindings.findChildViewById(rootView, id);
      if (mbCancel == null) {
        break missingId;
      }

      return new VoiceSpatialDialogBottomSheetSingleBinding((ConstraintLayout) rootView, ivTitle,
          mbCancel);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
