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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.spatial.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialItemHandsRaisedBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ShapeableImageView ivAudienceAvatar;

  @NonNull
  public final MaterialTextView mtAudienceAction;

  @NonNull
  public final MaterialTextView mtAudienceUsername;

  private VoiceSpatialItemHandsRaisedBinding(@NonNull ConstraintLayout rootView,
      @NonNull ShapeableImageView ivAudienceAvatar, @NonNull MaterialTextView mtAudienceAction,
      @NonNull MaterialTextView mtAudienceUsername) {
    this.rootView = rootView;
    this.ivAudienceAvatar = ivAudienceAvatar;
    this.mtAudienceAction = mtAudienceAction;
    this.mtAudienceUsername = mtAudienceUsername;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialItemHandsRaisedBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialItemHandsRaisedBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_item_hands_raised, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialItemHandsRaisedBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.ivAudienceAvatar;
      ShapeableImageView ivAudienceAvatar = ViewBindings.findChildViewById(rootView, id);
      if (ivAudienceAvatar == null) {
        break missingId;
      }

      id = R.id.mtAudienceAction;
      MaterialTextView mtAudienceAction = ViewBindings.findChildViewById(rootView, id);
      if (mtAudienceAction == null) {
        break missingId;
      }

      id = R.id.mtAudienceUsername;
      MaterialTextView mtAudienceUsername = ViewBindings.findChildViewById(rootView, id);
      if (mtAudienceUsername == null) {
        break missingId;
      }

      return new VoiceSpatialItemHandsRaisedBinding((ConstraintLayout) rootView, ivAudienceAvatar,
          mtAudienceAction, mtAudienceUsername);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
