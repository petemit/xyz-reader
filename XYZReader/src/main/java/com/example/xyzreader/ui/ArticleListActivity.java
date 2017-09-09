package com.example.xyzreader.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;

import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;


import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity {

    private static final String TAG = ArticleListActivity.class.toString();
    private Toolbar mToolbar;
    ArticleListActivity activity;

    int screenWidth;
    int currentOrientation;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        activity = this;
        //  supportPostponeEnterTransition();

        //Determine screen width
        screenWidth = getResources().getConfiguration().smallestScreenWidthDp;
        currentOrientation = getResources().getConfiguration().orientation;

        mToolbar = (Toolbar) findViewById(R.id.toolbar);


    }

    public void callActivityToStartIntent(long position, Bundle bundle, Cursor cursor) {
        Uri uri=null;
        if(position!=0){
            uri=ItemsContract.Items.buildItemUri(position);
        }


        //if in tablet mode
        if (screenWidth >= getResources().getInteger(R.integer.tablet_screen_width) &&
                (currentOrientation ==
                        getResources().getConfiguration().ORIENTATION_LANDSCAPE)){
            getSupportFragmentManager().beginTransaction().replace(R.id.right_fragment_container,
                    ArticleDetailFragment.newInstance(cursor.getLong(ArticleLoader.Query._ID)));
        }
        else{
            startActivity(new Intent(Intent.ACTION_VIEW, uri),bundle);
        }
    }



}
