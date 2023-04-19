// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceItemRoomAinsAuditionBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final MaterialTextView mtChatroomAins;

  @NonNull
  public final MaterialTextView mtChatroomAinsName;

  @NonNull
  public final MaterialTextView mtChatroomAinsNone;

  @NonNull
  public final MaterialTextView mtChatroomAinsSubName;

  @NonNull
  public final ConstraintLayout superLayout;

  private VoiceItemRoomAinsAuditionBinding(@NonNull ConstraintLayout rootView,
      @NonNull MaterialTextView mtChatroomAins, @NonNull MaterialTextView mtChatroomAinsName,
      @NonNull MaterialTextView mtChatroomAinsNone, @NonNull MaterialTextView mtChatroomAinsSubName,
      @NonNull ConstraintLayout superLayout) {
    this.rootView = rootView;
    this.mtChatroomAins = mtChatroomAins;
    this.mtChatroomAinsName = mtChatroomAinsName;
    this.mtChatroomAinsNone = mtChatroomAinsNone;
    this.mtChatroomAinsSubName = mtChatroomAinsSubName;
    this.superLayout = superLayout;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceItemRoomAinsAuditionBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceItemRoomAinsAuditionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_item_room_ains_audition, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceItemRoomAinsAuditionBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.mtChatroomAins;
      MaterialTextView mtChatroomAins = ViewBindings.findChildViewById(rootView, id);
      if (mtChatroomAins == null) {
        break missingId;
      }

      id = R.id.mtChatroomAinsName;
      MaterialTextView mtChatroomAinsName = ViewBindings.findChildViewById(rootView, id);
      if (mtChatroomAinsName == null) {
        break missingId;
      }

      id = R.id.mtChatroomAinsNone;
      MaterialTextView mtChatroomAinsNone = ViewBindings.findChildViewById(rootView, id);
      if (mtChatroomAinsNone == null) {
        break missingId;
      }

      id = R.id.mtChatroomAinsSubName;
      MaterialTextView mtChatroomAinsSubName = ViewBindings.findChildViewById(rootView, id);
      if (mtChatroomAinsSubName == null) {
        break missingId;
      }

      ConstraintLayout superLayout = (ConstraintLayout) rootView;

      return new VoiceItemRoomAinsAuditionBinding((ConstraintLayout) rootView, mtChatroomAins,
          mtChatroomAinsName, mtChatroomAinsNone, mtChatroomAinsSubName, superLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
