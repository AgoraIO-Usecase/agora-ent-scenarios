// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import io.agora.scene.voice.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceFragmentHandsListLayoutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final RecyclerView list;

  @NonNull
  public final SwipeRefreshLayout swipeLayout;

  private VoiceFragmentHandsListLayoutBinding(@NonNull ConstraintLayout rootView,
      @NonNull RecyclerView list, @NonNull SwipeRefreshLayout swipeLayout) {
    this.rootView = rootView;
    this.list = list;
    this.swipeLayout = swipeLayout;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceFragmentHandsListLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceFragmentHandsListLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_fragment_hands_list_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceFragmentHandsListLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.list;
      RecyclerView list = ViewBindings.findChildViewById(rootView, id);
      if (list == null) {
        break missingId;
      }

      id = R.id.swipeLayout;
      SwipeRefreshLayout swipeLayout = ViewBindings.findChildViewById(rootView, id);
      if (swipeLayout == null) {
        break missingId;
      }

      return new VoiceFragmentHandsListLayoutBinding((ConstraintLayout) rootView, list,
          swipeLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
