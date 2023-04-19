// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.imageview.ShapeableImageView;
import io.agora.scene.voice.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceItemProfileAvatarBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ShapeableImageView avatar;

  @NonNull
  public final ShapeableImageView avatarBg;

  @NonNull
  public final ImageView iconCheck;

  @NonNull
  public final ConstraintLayout itemLayout;

  private VoiceItemProfileAvatarBinding(@NonNull ConstraintLayout rootView,
      @NonNull ShapeableImageView avatar, @NonNull ShapeableImageView avatarBg,
      @NonNull ImageView iconCheck, @NonNull ConstraintLayout itemLayout) {
    this.rootView = rootView;
    this.avatar = avatar;
    this.avatarBg = avatarBg;
    this.iconCheck = iconCheck;
    this.itemLayout = itemLayout;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceItemProfileAvatarBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceItemProfileAvatarBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_item_profile_avatar, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceItemProfileAvatarBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.avatar;
      ShapeableImageView avatar = ViewBindings.findChildViewById(rootView, id);
      if (avatar == null) {
        break missingId;
      }

      id = R.id.avatar_bg;
      ShapeableImageView avatarBg = ViewBindings.findChildViewById(rootView, id);
      if (avatarBg == null) {
        break missingId;
      }

      id = R.id.icon_check;
      ImageView iconCheck = ViewBindings.findChildViewById(rootView, id);
      if (iconCheck == null) {
        break missingId;
      }

      ConstraintLayout itemLayout = (ConstraintLayout) rootView;

      return new VoiceItemProfileAvatarBinding((ConstraintLayout) rootView, avatar, avatarBg,
          iconCheck, itemLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
