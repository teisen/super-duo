package barqsoft.footballscores;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import barqsoft.footballscores.service.myFetchService;

/**
 * Created by teisentraeger on 10/19/2015.
 * Code sample from http://dharmangsoni.blogspot.com/2014/03/collection-widget-with-event-handling.html
 */
public class WidgetProvider extends AppWidgetProvider {

    private static final String LOG_TAG = "WidgetProvider";
    public static final String ACTION_UPDATE = "barqsoft.footballscores.ACTION_UPDATE";
    private static Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Log.v(LOG_TAG, "onReceive " );
        mContext = context;
        if (intent.getAction().equals(ACTION_UPDATE)) {
            Toast.makeText(mContext, "Updating Data...", Toast.LENGTH_LONG).show();
            update_scores();
        }
        super.onReceive(mContext, intent);
    }

    private void update_scores()
    {
        // Log.v(LOG_TAG, "update_scores " + mContext );
        Intent service_start = new Intent(mContext, myFetchService.class);
        mContext.startService(service_start);
    }

    @SuppressLint("NewApi")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // Log.v(LOG_TAG, "onUpdate ");
        update_scores();

        for (int widgetId : appWidgetIds) {
            RemoteViews mView = initViews(context, appWidgetManager, widgetId);

            // Adding collection list item handler
            final Intent onItemClick = new Intent(context, WidgetProvider.class);
            onItemClick.setAction(ACTION_UPDATE);
            onItemClick.setData(Uri.parse(onItemClick
                    .toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent
                    .getBroadcast(context, 0, onItemClick,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            mView.setPendingIntentTemplate(R.id.widgetCollectionList,
                    onClickPendingIntent);

            appWidgetManager.updateAppWidget(widgetId, mView);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private RemoteViews initViews(Context context, AppWidgetManager widgetManager, int widgetId) {
        // Log.v(LOG_TAG, "initViews(" + widgetId +")");
        RemoteViews mView = new RemoteViews(context.getPackageName(),
                R.layout.widget_provider_layout);

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        mView.setRemoteAdapter(widgetId, R.id.widgetCollectionList, intent);

        return mView;
    }
}
