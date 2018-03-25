// Generated code from Butter Knife. Do not modify!
package com.snh.popularmovies;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class Detail_ViewBinding implements Unbinder {
  private Detail target;

  @UiThread
  public Detail_ViewBinding(Detail target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public Detail_ViewBinding(Detail target, View source) {
    this.target = target;

    target.imageView = Utils.findRequiredViewAsType(source, R.id.image1, "field 'imageView'", ImageView.class);
    target.fab = Utils.findRequiredViewAsType(source, R.id.fab, "field 'fab'", FloatingActionButton.class);
    target.id = Utils.findRequiredViewAsType(source, R.id.id, "field 'id'", TextView.class);
    target.title = Utils.findRequiredViewAsType(source, R.id.title, "field 'title'", TextView.class);
    target.releaseDate = Utils.findRequiredViewAsType(source, R.id.releaseDate, "field 'releaseDate'", TextView.class);
    target.voteAverage = Utils.findRequiredViewAsType(source, R.id.voteAverage, "field 'voteAverage'", TextView.class);
    target.overview = Utils.findRequiredViewAsType(source, R.id.overview, "field 'overview'", TextView.class);
    target.reviewsView = Utils.findRequiredViewAsType(source, R.id.reviewsRecycler, "field 'reviewsView'", RecyclerView.class);
    target.trailersView = Utils.findRequiredViewAsType(source, R.id.trailersRecycler, "field 'trailersView'", RecyclerView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    Detail target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.imageView = null;
    target.fab = null;
    target.id = null;
    target.title = null;
    target.releaseDate = null;
    target.voteAverage = null;
    target.overview = null;
    target.reviewsView = null;
    target.trailersView = null;
  }
}
