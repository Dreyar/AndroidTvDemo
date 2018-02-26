package com.gabilheri.moviestmdb.ui.details;

import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gabilheri.moviestmdb.R;
import com.gabilheri.moviestmdb.data.models.MovieDetails;

/**
 * Created by Pampoukidis on 26/2/2018.
 */
public class MovieDetailsDescriptionPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vh_movie_details, parent, false);
        return new MovieDetailsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        MovieDetails movie = (MovieDetails) item;
        MovieDetailsViewHolder holder = (MovieDetailsViewHolder) viewHolder;
        holder.bindTo(movie);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}