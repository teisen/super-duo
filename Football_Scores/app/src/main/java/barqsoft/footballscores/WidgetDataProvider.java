package barqsoft.footballscores;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

/**
 * Created by teisentraeger on 10/19/2015.
 * Code sample from http://dharmangsoni.blogspot.com/2014/03/collection-widget-with-event-handling.html
 */
@SuppressLint("NewApi")
public class WidgetDataProvider implements RemoteViewsFactory {

    private static final String LOG_TAG = "WidgetDataProvider";
    private List mCollections = new ArrayList();

    private Context mContext = null;

    public WidgetDataProvider(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCollections.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                android.R.layout.simple_list_item_1);
        mView.setTextViewText(android.R.id.text1, (CharSequence) mCollections.get(position));
        mView.setTextColor(android.R.id.text1, Color.BLACK);

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(WidgetProvider.ACTION_UPDATE);
        final Bundle bundle = new Bundle();
        //bundle.putString(WidgetProvider.EXTRA_STRING, (String) mCollections.get(position));
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(android.R.id.text1, fillInIntent);
        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        mCollections.clear();
        //for (int i = 1; i <= 10; i++) {
        //    mCollections.add("ListView item " + i);
        //}

        // params
        Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
        String[] fragmentdate = new String[1];
        Date fragmentd = new Date(System.currentTimeMillis()-(1 * 86400000)); //day offset on the right side if needed -(1 * 86400000)
        @SuppressLint("SimpleDateFormat") SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        fragmentdate[0] = mformat.format(fragmentd);

        Log.v(LOG_TAG, "URI: " + uri.toString());
        Log.v(LOG_TAG, "fragmentdate[0]: " + fragmentdate[0]);

        Cursor cursor = null;
        try {
            cursor = new ScoresDBHelper(mContext).getReadableDatabase().query(
                    //DatabaseContract.SCORES_TABLE, null, DatabaseContract.scores_table.DATE_COL + " LIKE ?" , fragmentdate, null, null, null);
                    DatabaseContract.SCORES_TABLE, null, null , null, null, null, DatabaseContract.scores_table.DATE_COL + " DESC");

            int i = 0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                i++;

                String score = Utilies.getScores(cursor.getInt(scoresAdapter.COL_HOME_GOALS), cursor.getInt(scoresAdapter.COL_AWAY_GOALS));
                String homename = cursor.getString(scoresAdapter.COL_HOME);
                String awayname = cursor.getString(scoresAdapter.COL_AWAY);
                //String matchdateUTC = cursor.getString(scoresAdapter.COL_DATE);
                String matchtimeUTC = cursor.getString(scoresAdapter.COL_MATCHTIME);

                Log.v(LOG_TAG, i + " " + homename + score + awayname + " @ " + matchtimeUTC);

                String displayString = homename + score + awayname ;
                if(score.equalsIgnoreCase(" - ")) {
                    displayString = displayString + " @ " + matchtimeUTC;
                }

                mCollections.add(displayString);

                cursor.moveToNext();
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
    }

    @Override
    public void onDestroy() {

    }

}