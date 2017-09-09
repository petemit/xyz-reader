package com.example.xyzreader.ui;


import android.database.Cursor;
import android.graphics.Bitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private ToolbarSetter toolbarSetter;
    private Bitmap toolbarBitmap;
    private String bitmapUri;
    private String bodyText;
    private String titleText;
    private String bylineText;

    private boolean mIsCard = false;

    private SimpleDateFormat dateFormat;
    // Use default locale format
    private SimpleDateFormat outputFormat;
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
        text_rv = (RecyclerView) mRootView.findViewById(R.id.text_rv);
        dateFormat = new SimpleDateFormat(getString(R.string.simple_date_format));
        outputFormat = new SimpleDateFormat();


        bindViews();

//        updateStatusBar();
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


    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, getString(R.string.getting_today_date));
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }


        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            setDateAndTitle(mCursor);

            bodyText = Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).
                    replaceAll("(\r\n|\n)", "<br />")).toString();
            ArrayList<String> bodyTextList = new ArrayList<String>();
            int index = 0;
            int nextnum = 0;
            while (index < bodyText.length() && nextnum >= 0) {
                nextnum = bodyText.indexOf("\n\n", index);
                if (nextnum == -1) {
                    nextnum = bodyText.length();
                }
                bodyTextList.add(bodyText.substring(index, Math.min(nextnum, bodyText.length())));
                index = nextnum + 1;


            }
            RecyclerViewTextAdapter adapter = new RecyclerViewTextAdapter(bodyTextList);
            text_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            text_rv.setHasFixedSize(true);
            text_rv.setAdapter(adapter);

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {

                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                setToolbarBitmap(bitmap);
                                setBitmapUri(imageContainer.getRequestUrl());
                                if (getUserVisibleHint()) {
                                    toolbarSetter.setToolbar(getToolbarBitmap(),
                                            getBitmapUri(), getTitleText(), getBylineText());

                                }
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
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
                            DateUtils.FORMAT_ABBREV_ALL).toString().concat(getString(R.string.by_string)
                            .concat(cursor.getString(ArticleLoader.Query.AUTHOR))))).toString());


        } else {
            // If date is before 1902, just show the string
            setBylineText((cursor.getString(ArticleLoader.Query.AUTHOR)));

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
            Log.e(TAG, getString(R.string.error_detail_cursor));
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


    private class RecyclerViewTextAdapter extends RecyclerView.Adapter<TextViewHolder> {
        ArrayList<String> data;

        public RecyclerViewTextAdapter(ArrayList<String> data) {
            this.data = data;
        }

        @Override
        public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_list_item, parent, false);
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

    private class TextViewHolder extends RecyclerView.ViewHolder {
        TextView tv;

        public TextViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.rv_tv);
        }
    }
}
