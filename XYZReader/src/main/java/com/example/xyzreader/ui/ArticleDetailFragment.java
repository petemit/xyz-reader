package com.example.xyzreader.ui;


import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import org.w3c.dom.Text;


/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;
    private ToolbarSetter toolbarSetter;
    private Bitmap toolbarBitmap;
    private String bitmapUri;
    private TextView bodyView;
    private String bodyText;
    private String titleText;
    private String bylineText;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private RecyclerView text_rv;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setUserVisibleHint(false);
        if (getActivity() instanceof ToolbarSetter) {
            toolbarSetter = (ToolbarSetter) getActivity();
        }
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        text_rv=(RecyclerView)mRootView.findViewById(R.id.text_rv);
//        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
//                mRootView.findViewById(R.id.draw_insets_frame_layout);
//        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
//            @Override
//            public void onInsetsChanged(Rect insets) {
//                mTopInset = insets.top;
//            }
//        });
//
//        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
//        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
//            @Override
//            public void onScrollChanged() {
//                mScrollY = mScrollView.getScrollY();
//              //  getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
////                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
//                updateStatusBar();
//            }
//        });

//        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
//        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);




        bindViews();

        updateStatusBar();
        return mRootView;

    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getBylineText() {
        return bylineText;
    }

    public void setBylineText(String bylineText) {
        this.bylineText = bylineText;
    }

    private class loadText extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            //I really don't like doing this, but it's out of scope for this project to implement
            //textview pagination.
            //This hack makes the transition smooth without interrupting the UI.
            //The user will experience some jankiness when scrolling, but that happened
            //even IF I didn't put in a wait.
            SystemClock.sleep(1000);
            publishProgress();


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            setBigText();
        }
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
//        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

//        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
//        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
   //     bylineView.setMovementMethod(new LinkMovementMethod());
//        bodyView = (TextView) mRootView.findViewById(R.id.article_body);


        //  bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            setDateAndTitle(mCursor);

            bodyText = Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")).toString();
            ArrayList<String> bodyTextList = new ArrayList<String>();
            int index=0;
            int span=500;
            int nextnum=0;
            while (index < bodyText.length()&&nextnum >=0){
                nextnum=bodyText.indexOf("\n\n",index);
                if(nextnum==-1){
                    nextnum=bodyText.length();
                }
                bodyTextList.add(bodyText.substring(index,Math.min(nextnum,bodyText.length())));
                index=nextnum+1;


            }
            RecyclerViewTextAdapter adapter = new RecyclerViewTextAdapter(bodyTextList);
            text_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            text_rv.setHasFixedSize(true);
            text_rv.setAdapter(adapter);


            //Spanned spanned=Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />"));
            //new loadText().execute(bigText);
          //  bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).substring(0, 1000).replaceAll("(\r\n|\n)", "<br />")));
          //  new loadText().execute(bodyText);
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {

                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
//                                Palette p = Palette.generate(bitmap, 12);
//                                mMutedColor = p.getDarkMutedColor(0xFF333333);
//                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
//                                mRootView.findViewById(R.id.meta_bar)
//                                        .setBackgroundColor(mMutedColor);
//                                updateStatusBar();
                                setToolbarBitmap(bitmap);
                                setBitmapUri(imageContainer.getRequestUrl());
                                if (getUserVisibleHint()) {
                                    toolbarSetter.setToolbar(getToolbarBitmap(), getBitmapUri(),getTitleText(),getBylineText());

                                }
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
//            titleView.setText("N/A");
//            bylineView.setText("N/A");
//            bodyView.setText("N/A");
        }


    }

    private void setDateAndTitle(Cursor cursor) {
        setTitleText((cursor.getString(ArticleLoader.Query.TITLE)));
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            setBylineText((Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)).toString()));


        } else {
            // If date is before 1902, just show the string
            setBylineText((cursor.getString(ArticleLoader.Query.AUTHOR)));

        }

//        titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
//        Date publishedDate = parsePublishedDate();
//        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
//            bylineView.setText(Html.fromHtml(
//                    DateUtils.getRelativeTimeSpanString(
//                            publishedDate.getTime(),
//                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
//                            DateUtils.FORMAT_ABBREV_ALL).toString()
//                            + " by "
//                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
//
//
//        } else {
//            // If date is before 1902, just show the string
//            bylineView.setText(mCursor.getString(ArticleLoader.Query.AUTHOR));
//
//        }
    }

    public void setBigText() {
        if (bodyText != null) {
            bodyView.append(Html.fromHtml(bodyText.substring(1000, bodyText.length()).replaceAll("(\r\n|\n)", "<br />")));

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }

    public Bitmap getToolbarBitmap() {
        return toolbarBitmap;
    }

    public void setToolbarBitmap(Bitmap toolbarBitmap) {
        this.toolbarBitmap = toolbarBitmap;
    }

    public String getBitmapUri() {
        return bitmapUri;
    }

    public void setBitmapUri(String bitmapUri) {
        this.bitmapUri = bitmapUri;
    }

    public interface ToolbarSetter {
        void setToolbar(Bitmap b, String url, String title, String byline);
    }


    private class RecyclerViewTextAdapter extends RecyclerView.Adapter<TextViewHolder>{
        ArrayList<String> data;

        public RecyclerViewTextAdapter(ArrayList<String> data){
            this.data=data;
        }

        @Override
        public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_list_item,parent,false);
            return new TextViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TextViewHolder holder, int position) {
            holder.tv.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class TextViewHolder extends RecyclerView.ViewHolder{
        TextView tv;
        public TextViewHolder(View itemView) {
            super(itemView);
            tv= (TextView)itemView.findViewById(R.id.rv_tv);
        }
    }
}
