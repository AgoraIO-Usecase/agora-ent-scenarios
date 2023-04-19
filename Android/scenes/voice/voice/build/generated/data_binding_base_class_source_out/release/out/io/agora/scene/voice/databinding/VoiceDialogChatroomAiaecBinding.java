// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceDialogChatroomAiaecBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final AppCompatCheckBox accbAEC;

  @NonNull
  public final ImageButton btnAfter;

  @NonNull
  public final ImageButton btnBefore;

  @NonNull
  public final ImageView ivAfter;

  @NonNull
  public final ImageView ivBefore;

  @NonNull
  public final AppCompatImageView ivBottomSheetTitle;

  @NonNull
  public final MaterialTextView mtAEC;

  @NonNull
  public final MaterialTextView mtAECIntroduce;

  @NonNull
  public final MaterialTextView mtBottomSheetTitle;

  @NonNull
  public final MaterialTextView tvAfterTitle;

  @NonNull
  public final MaterialTextView tvAuditionTitle;

  @NonNull
  public final MaterialTextView tvBeforeTitle;

  private VoiceDialogChatroomAiaecBinding(@NonNull ConstraintLayout rootView,
      @NonNull AppCompatCheckBox accbAEC, @NonNull ImageButton btnAfter,
      @NonNull ImageButton btnBefore, @NonNull ImageView ivAfter, @NonNull ImageView ivBefore,
      @NonNull AppCompatImageView ivBottomSheetTitle, @NonNull MaterialTextView mtAEC,
      @NonNull MaterialTextView mtAECIntroduce, @NonNull MaterialTextView mtBottomSheetTitle,
      @NonNull MaterialTextView tvAfterTitle, @NonNull MaterialTextView tvAuditionTitle,
      @NonNull MaterialTextView tvBeforeTitle) {
    this.rootView = rootView;
    this.accbAEC = accbAEC;
    this.btnAfter = btnAfter;
    this.btnBefore = btnBefore;
    this.ivAfter = ivAfter;
    this.ivBefore = ivBefore;
    this.ivBottomSheetTitle = ivBottomSheetTitle;
    this.mtAEC = mtAEC;
    this.mtAECIntroduce = mtAECIntroduce;
    this.mtBottomSheetTitle = mtBottomSheetTitle;
    this.tvAfterTitle = tvAfterTitle;
    this.tvAuditionTitle = tvAuditionTitle;
    this.tvBeforeTitle = tvBeforeTitle;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceDialogChatroomAiaecBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceDialogChatroomAiaecBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_dialog_chatroom_aiaec, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceDialogChatroomAiaecBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.accbAEC;
      AppCompatCheckBox accbAEC = ViewBindings.findChildViewById(rootView, id);
      if (accbAEC == null) {
        break missingId;
      }

      id = R.id.btnAfter;
      ImageButton btnAfter = ViewBindings.findChildViewById(rootView, id);
      if (btnAfter == null) {
        break missingId;
      }

      id = R.id.btnBefore;
      ImageButton btnBefore = ViewBindings.findChildViewById(rootView, id);
      if (btnBefore == null) {
        break missingId;
      }

      id = R.id.ivAfter;
      ImageView ivAfter = ViewBindings.findChildViewById(rootView, id);
      if (ivAfter == null) {
        break missingId;
      }

      id = R.id.ivBefore;
      ImageView ivBefore = ViewBindings.findChildViewById(rootView, id);
      if (ivBefore == null) {
        break missingId;
      }

      id = R.id.ivBottomSheetTitle;
      AppCompatImageView ivBottomSheetTitle = ViewBindings.findChildViewById(rootView, id);
      if (ivBottomSheetTitle == null) {
        break missingId;
      }

      id = R.id.mtAEC;
      MaterialTextView mtAEC = ViewBindings.findChildViewById(rootView, id);
      if (mtAEC == null) {
        break missingId;
      }

      id = R.id.mtAECIntroduce;
      MaterialTextView mtAECIntroduce = ViewBindings.findChildViewById(rootView, id);
      if (mtAECIntroduce == null) {
        break missingId;
      }

      id = R.id.mtBottomSheetTitle;
      MaterialTextView mtBottomSheetTitle = ViewBindings.findChildViewById(rootView, id);
      if (mtBottomSheetTitle == null) {
        break missingId;
      }

      id = R.id.tvAfterTitle;
      MaterialTextView tvAfterTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvAfterTitle == null) {
        break missingId;
      }

      id = R.id.tvAuditionTitle;
      MaterialTextView tvAuditionTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvAuditionTitle == null) {
        break missingId;
      }

      id = R.id.tvBeforeTitle;
      MaterialTextView tvBeforeTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvBeforeTitle == null) {
        break missingId;
      }

      return new VoiceDialogChatroomAiaecBinding((ConstraintLayout) rootView, accbAEC, btnAfter,
          btnBefore, ivAfter, ivBefore, ivBottomSheetTitle, mtAEC, mtAECIntroduce,
          mtBottomSheetTitle, tvAfterTitle, tvAuditionTitle, tvBeforeTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
