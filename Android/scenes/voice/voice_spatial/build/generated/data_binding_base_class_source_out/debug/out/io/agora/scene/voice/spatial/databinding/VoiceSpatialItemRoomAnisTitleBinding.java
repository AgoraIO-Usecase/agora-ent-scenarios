// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.spatial.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.spatial.R;
import java.lang.NullPointerException;
import java.lang.Override;

public final class VoiceSpatialItemRoomAnisTitleBinding implements ViewBinding {
  @NonNull
  private final MaterialTextView rootView;

  @NonNull
  public final MaterialTextView mtChatroomAinsTitle;

  private VoiceSpatialItemRoomAnisTitleBinding(@NonNull MaterialTextView rootView,
      @NonNull MaterialTextView mtChatroomAinsTitle) {
    this.rootView = rootView;
    this.mtChatroomAinsTitle = mtChatroomAinsTitle;
  }

  @Override
  @NonNull
  public MaterialTextView getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialItemRoomAnisTitleBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialItemRoomAnisTitleBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_item_room_anis_title, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialItemRoomAnisTitleBinding bind(@NonNull View rootView) {
    if (rootView == null) {
      throw new NullPointerException("rootView");
    }

    MaterialTextView mtChatroomAinsTitle = (MaterialTextView) rootView;

    return new VoiceSpatialItemRoomAnisTitleBinding((MaterialTextView) rootView,
        mtChatroomAinsTitle);
  }
}
