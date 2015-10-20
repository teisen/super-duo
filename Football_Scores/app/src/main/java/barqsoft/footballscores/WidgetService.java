package barqsoft.footballscores;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by teisentraeger on 10/19/2015.
 * Code sample from http://dharmangsoni.blogspot.com/2014/03/collection-widget-with-event-handling.html
 */
@SuppressLint("NewApi")
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new WidgetDataProvider(getApplicationContext());
    }

}
