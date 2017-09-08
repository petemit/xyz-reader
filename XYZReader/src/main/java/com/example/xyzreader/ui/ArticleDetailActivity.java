package com.example.xyzreader.ui;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>, ArticleDetailFragment.ToolbarImageSetter{

    private Cursor mCursor;
    private long mStartId;
    private long FADEINDURATION=500;
    private long FADEOUTDURATION=300;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;
    private ArticleDetailActivity activity;
    private ImageView toolbarImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        activity=this;
        setContentView(R.layout.activity_article_detail);
        supportPostponeEnterTransition();
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
            }

            @Override
            public void onRejectSharedElements(List<View> rejectedSharedElements) {
                Log.e("taggy","" + rejectedSharedElements.size());
                super.onRejectSharedElements(rejectedSharedElements);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
        toolbarImage=(ImageView)findViewById(R.id.detail_activity_toolbar_image);


        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

//                mUpButton.animate()
//                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
//                        .setDuration(300);


            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(toolbarImage,"alpha",1,0);
                    objectAnimator.setDuration(FADEOUTDURATION);
                    objectAnimator.start();

                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
             //   updateUpButtonPosition();
            }
        });
        Toolbar myToolbar=(Toolbar) findViewById(R.id.detail_activity_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

//        mUpButtonContainer = findViewById(R.id.up_container);
//
//        mUpButton = findViewById(R.id.action_up);
//        mUpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onSupportNavigateUp();
//            }
//        });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//                @Override
//                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
//                    view.onApplyWindowInsets(windowInsets);
//                    mTopInset = windowInsets.getSystemWindowInsetTop();
//                    mUpButtonContainer.setTranslationY(mTopInset);
//                    updateUpButtonPosition();
//                    return windowInsets;
//                }
//            });
//        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
    }

    private void scheduleStartPostponedTransition(final View toolbarImage){
        toolbarImage.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        toolbarImage.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                }
        );
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }



    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();


        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);

                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }




    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

//    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
//        if (itemId == mSelectedItemId) {
//            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
//            updateUpButtonPosition();
//        }
//    }

    private void updateUpButtonPosition() {
        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    @Override
    public void setToolbarImage(Bitmap b, String url) {
        if (toolbarImage!=null){
            toolbarImage.setImageBitmap(b);
            if(url!=null) {
                ViewCompat.setTransitionName(toolbarImage,url);
            }
            scheduleStartPostponedTransition(toolbarImage);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(toolbarImage,"alpha",0,1);
            objectAnimator.setDuration(FADEINDURATION);
            objectAnimator.start();


        }



    }

    private class MyPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
        public MyPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
//                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
//                updateUpButtonPosition();
                Bitmap bitmap =fragment.getToolbarBitmap();
                if(bitmap!=null) {
                    setToolbarImage(bitmap,fragment.getBitmapUri());
                }


            }
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }


        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
