package com.gabilheri.moviestmdb.ui.details;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.gabilheri.moviestmdb.R;
import com.gabilheri.moviestmdb.dagger.modules.HttpClientModule;
import com.gabilheri.moviestmdb.data.models.Movie;
import com.gabilheri.moviestmdb.ui.base.BaseTvActivity;
import com.gabilheri.moviestmdb.ui.base.GlideBackgroundManager;

import java.util.Objects;

public class MovieDetailsActivity extends BaseTvActivity {

    private GlideBackgroundManager mBackgroundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the movie through the intent
        Movie movie = Objects.requireNonNull(getIntent().getExtras())
                .getParcelable(Movie.class.getSimpleName());

        MovieDetailsFragment detailsFragment = MovieDetailsFragment.newInstance(movie);
        addFragment(detailsFragment);

        // Sets the background of the activity to the backdrop of the movie
        mBackgroundManager = new GlideBackgroundManager(this);
        if (movie != null && movie.getBackdropPath() != null) {
            mBackgroundManager.loadImage(HttpClientModule.BACKDROP_URL + movie.getBackdropPath());
        } else {
            mBackgroundManager.setBackground(ContextCompat.getDrawable(this, R.drawable.material_bg));
        }

    }
}
