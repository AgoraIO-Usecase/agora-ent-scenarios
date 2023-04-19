// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.spatial.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.opensource.svgaplayer.SVGAImageView;
import io.agora.scene.voice.spatial.R;
import io.agora.scene.voice.spatial.ui.widget.like.LikeLayout;
import io.agora.scene.voice.spatial.ui.widget.mic.Room3DMicLayout;
import io.agora.scene.voice.spatial.ui.widget.primary.ChatPrimaryMenuView;
import io.agora.scene.voice.spatial.ui.widget.top.RoomLiveTopView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialActivityChatroomBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button btnDebug;

  @NonNull
  public final RoomLiveTopView cTopView;

  @NonNull
  public final ChatPrimaryMenuView chatBottom;

  @NonNull
  public final ConstraintLayout clMain;

  @NonNull
  public final Guideline glHorizontal45;

  @NonNull
  public final Guideline glHorizontal72;

  @NonNull
  public final LikeLayout likeView;

  @NonNull
  public final Room3DMicLayout rvChatroom3dMicLayout;

  @NonNull
  public final SVGAImageView svgaView;

  private VoiceSpatialActivityChatroomBinding(@NonNull ConstraintLayout rootView,
      @NonNull Button btnDebug, @NonNull RoomLiveTopView cTopView,
      @NonNull ChatPrimaryMenuView chatBottom, @NonNull ConstraintLayout clMain,
      @NonNull Guideline glHorizontal45, @NonNull Guideline glHorizontal72,
      @NonNull LikeLayout likeView, @NonNull Room3DMicLayout rvChatroom3dMicLayout,
      @NonNull SVGAImageView svgaView) {
    this.rootView = rootView;
    this.btnDebug = btnDebug;
    this.cTopView = cTopView;
    this.chatBottom = chatBottom;
    this.clMain = clMain;
    this.glHorizontal45 = glHorizontal45;
    this.glHorizontal72 = glHorizontal72;
    this.likeView = likeView;
    this.rvChatroom3dMicLayout = rvChatroom3dMicLayout;
    this.svgaView = svgaView;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialActivityChatroomBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialActivityChatroomBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_activity_chatroom, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialActivityChatroomBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnDebug;
      Button btnDebug = ViewBindings.findChildViewById(rootView, id);
      if (btnDebug == null) {
        break missingId;
      }

      id = R.id.cTopView;
      RoomLiveTopView cTopView = ViewBindings.findChildViewById(rootView, id);
      if (cTopView == null) {
        break missingId;
      }

      id = R.id.chat_bottom;
      ChatPrimaryMenuView chatBottom = ViewBindings.findChildViewById(rootView, id);
      if (chatBottom == null) {
        break missingId;
      }

      ConstraintLayout clMain = (ConstraintLayout) rootView;

      id = R.id.glHorizontal45;
      Guideline glHorizontal45 = ViewBindings.findChildViewById(rootView, id);
      if (glHorizontal45 == null) {
        break missingId;
      }

      id = R.id.glHorizontal72;
      Guideline glHorizontal72 = ViewBindings.findChildViewById(rootView, id);
      if (glHorizontal72 == null) {
        break missingId;
      }

      id = R.id.like_view;
      LikeLayout likeView = ViewBindings.findChildViewById(rootView, id);
      if (likeView == null) {
        break missingId;
      }

      id = R.id.rvChatroom3dMicLayout;
      Room3DMicLayout rvChatroom3dMicLayout = ViewBindings.findChildViewById(rootView, id);
      if (rvChatroom3dMicLayout == null) {
        break missingId;
      }

      id = R.id.svga_view;
      SVGAImageView svgaView = ViewBindings.findChildViewById(rootView, id);
      if (svgaView == null) {
        break missingId;
      }

      return new VoiceSpatialActivityChatroomBinding((ConstraintLayout) rootView, btnDebug,
          cTopView, chatBottom, clMain, glHorizontal45, glHorizontal72, likeView,
          rvChatroom3dMicLayout, svgaView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
