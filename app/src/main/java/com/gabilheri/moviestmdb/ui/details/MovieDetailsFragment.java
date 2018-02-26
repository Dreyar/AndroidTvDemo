package com.gabilheri.moviestmdb.ui.details;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewLogoPresenter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.gabilheri.moviestmdb.App;
import com.gabilheri.moviestmdb.Config;
import com.gabilheri.moviestmdb.dagger.modules.HttpClientModule;
import com.gabilheri.moviestmdb.data.Api.TheMovieDbAPI;
import com.gabilheri.moviestmdb.data.models.CreditsResponse;
import com.gabilheri.moviestmdb.data.models.Movie;
import com.gabilheri.moviestmdb.data.models.MovieDetails;
import com.gabilheri.moviestmdb.data.models.MovieResponse;
import com.gabilheri.moviestmdb.data.models.PaletteColors;
import com.gabilheri.moviestmdb.ui.movies.MoviePresenter;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends DetailsFragment implements Palette.PaletteAsyncListener {

    public static String TRANSITION_NAME = "poster_transition";

    @Inject
    TheMovieDbAPI mDbAPI;

    private Movie movie;
    private MovieDetails movieDetails;
    private ArrayObjectAdapter mAdapter;
    private FullWidthDetailsOverviewRowPresenter mFullWidthMovieDetailsPresenter;
    private DetailsOverviewRow mDetailsOverviewRow;

    private ArrayObjectAdapter mCastAdapter = new ArrayObjectAdapter(new PersonPresenter());
    private ArrayObjectAdapter mRecommendationsAdapter = new ArrayObjectAdapter(new MoviePresenter());


    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(Movie.class.getSimpleName(), movie);
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.instance().appComponent().inject(this);
        if (getArguments() == null || !getArguments().containsKey(Movie.class.getSimpleName())) {
            throw new RuntimeException("An movie is necessary for MovieDetailsFragment");
        }

        // Retrieves the movie from the arguments
        movie = getArguments().getParcelable(Movie.class.getSimpleName());

        setUpAdapter();

        setUpDetailsOverviewRow();

        setupCastMembers();

        setupRecommendationsRow();

    }

    private void setupRecommendationsRow() {
        mAdapter.add(new ListRow(new HeaderItem(2, "Recommendations"), mRecommendationsAdapter));
        fetchRecommendations();
    }

    private void fetchRecommendations() {
        mDbAPI.getRecommendations(movie.getId(), Config.API_KEY_URL_V3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindRecommendations, e -> {
                    Timber.e(e, "Error fetching recommendations: %s", e.getMessage());
                });
    }

    private void bindRecommendations(MovieResponse response) {
        mRecommendationsAdapter.addAll(0, response.getResults());
    }

    private void setupCastMembers() {
        mAdapter.add(new ListRow(new HeaderItem(0, "Cast"), mCastAdapter));
        fetchCastMembers();
    }

    private void fetchCastMembers() {
        mDbAPI.getCredits(movie.getId(), Config.API_KEY_URL_V3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindCastMembers, e -> {
                    Timber.e(e, "Error fetching data: %s", e.getMessage());
                });
    }

    private void bindCastMembers(CreditsResponse response) {
        mCastAdapter.addAll(0, response.getCast());
    }

    private void setUpDetailsOverviewRow() {
        mDetailsOverviewRow = new DetailsOverviewRow(new MovieDetails());
        mAdapter.add(mDetailsOverviewRow);
        loadImage(HttpClientModule.POSTER_URL + movie.getPosterPath());
        fetchMovieDetails();
    }

    private void setUpAdapter() {

        mFullWidthMovieDetailsPresenter = new CustomMovieDetailsPresenter(
                new MovieDetailsDescriptionPresenter(),
                new DetailsOverviewLogoPresenter());

        // Handle the transition, the Helper is mainly used because the ActivityTransition is being passed from
        // The Activity into the Fragment
        FullWidthDetailsOverviewSharedElementHelper helper = new FullWidthDetailsOverviewSharedElementHelper();
        helper.setSharedElementEnterTransition(getActivity(), TRANSITION_NAME); // the transition name is important
        mFullWidthMovieDetailsPresenter.setListener(helper); // Attach the listener
        // Define if this element is participating in the transition or not
        mFullWidthMovieDetailsPresenter.setParticipatingEntranceTransition(false);

        // Class presenter selector allows the Adapter to render Rows and the details
        // It can be used in any of the Adapters by the Leanback library
        ClassPresenterSelector classPresenterSelector = new ClassPresenterSelector();
        classPresenterSelector.addClassPresenter(DetailsOverviewRow.class, mFullWidthMovieDetailsPresenter);
        classPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mAdapter = new ArrayObjectAdapter(classPresenterSelector);
        // Sets the adapter to the fragment
        setAdapter(mAdapter);

    }

    private void fetchMovieDetails() {
        mDbAPI.getMovieDetails(movie.getId(), Config.API_KEY_URL_V3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindMovieDetails, e ->
                        Timber.e(e, "Error fetching data: %s", e.getMessage()));
    }

    private void bindMovieDetails(MovieDetails movieDetails) {
        this.movieDetails = movieDetails;
        mDetailsOverviewRow.setItem(this.movieDetails);
    }

    private SimpleTarget<GlideDrawable> mGlideDrawableSimpleTarget = new SimpleTarget<GlideDrawable>() {
        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            mDetailsOverviewRow.setImageDrawable(resource);
        }
    };

    private void loadImage(String url) {
        Glide.with(getActivity())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        changePalette(((GlideBitmapDrawable) resource).getBitmap());
                        return false;
                    }
                })
                .into(mGlideDrawableSimpleTarget);

    }

    private void changePalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(this);

    }

    @Override
    public void onGenerated(@NonNull Palette palette) {
        PaletteColors colors = PaletteUtils.getPaletteColors(palette);
        mFullWidthMovieDetailsPresenter.setActionsBackgroundColor(colors.getStatusBarColor());
        mFullWidthMovieDetailsPresenter.setBackgroundColor(colors.getToolbarBackgroundColor());
        if (movieDetails != null) {
            this.movieDetails.setPaletteColors(colors);
        }
        notifyDetailsChanged();
    }

    private void notifyDetailsChanged() {

        mDetailsOverviewRow.setItem(this.movieDetails);
        int index = mAdapter.indexOf(mDetailsOverviewRow);
        mAdapter.notifyArrayItemRangeChanged(index, 1);

    }

}
