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
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialDialogCenterFragmentContentBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final MaterialTextView mbSubmit;

  @NonNull
  public final MaterialTextView mtContent;

  private VoiceSpatialDialogCenterFragmentContentBinding(@NonNull ConstraintLayout rootView,
      @NonNull MaterialTextView mbSubmit, @NonNull MaterialTextView mtContent) {
    this.rootView = rootView;
    this.mbSubmit = mbSubmit;
    this.mtContent = mtContent;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialDialogCenterFragmentContentBinding inflate(
      @NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialDialogCenterFragmentContentBinding inflate(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_dialog_center_fragment_content, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialDialogCenterFragmentContentBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.mbSubmit;
      MaterialTextView mbSubmit = ViewBindings.findChildViewById(rootView, id);
      if (mbSubmit == null) {
        break missingId;
      }

      id = R.id.mtContent;
      MaterialTextView mtContent = ViewBindings.findChildViewById(rootView, id);
      if (mtContent == null) {
        break missingId;
      }

      return new VoiceSpatialDialogCenterFragmentContentBinding((ConstraintLayout) rootView,
          mbSubmit, mtContent);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
