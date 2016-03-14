package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by GAURAV on 06-03-2016.
 */
public class ScoreWidgetService extends IntentService implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String scoreWidgetService_TAG = "ScoreWidgetService";

    private static final String[] FOOTBALL_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL
    };

    public ScoreWidgetService(){
        super("ScoreWidgetService");
        Log.v("ScoreWidgetService", "Service Constructor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ScoreAppWidgetProvider.class));
        Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
        String[] date = new String[1];
        date[0] = Utilies.getFormatDate(0);
        Log.v("ScoreWidgetService", "Date: "+date);
        Cursor data = getContentResolver().query(dateUri,FOOTBALL_COLUMNS,
                DatabaseContract.PATH_DATE,date,null);

        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        String homeTeam = data.getString(data.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
        String awayTeam = data.getString(data.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
        String homeGoals = data.getString(data.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));
        String awayGoals = data.getString(data.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));

        for(int currentWidgetId : appWidgetIds){
            int widgetWidth = getWidgetWidth(appWidgetManager, currentWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_default_width);
            Log.v("ScoreWidgetService", "Id: " + defaultWidth);
            int layoutId;
            if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.score_widget_medium;
            } else {
                layoutId = R.layout.score_widget_small;
            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            Intent clickIntent = new Intent(getApplicationContext(), MainActivity.class);
            //clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0,clickIntent, 0);

            Log.v("ScoreWidgetService","Score: "+homeGoals);
            Log.v("ScoreWidgetService","Score: "+awayGoals);

            views.setImageViewResource(R.id.widget_img,R.drawable.ic_launcher);
            views.setTextViewText(R.id.team1,homeTeam);
            views.setTextViewText(R.id.team2,awayTeam);
            views.setTextViewText(R.id.widget_score,String.format("%s-%s",homeGoals,awayGoals));
            views.setOnClickPendingIntent(R.id.widget_img, pending);
            appWidgetManager.updateAppWidget(currentWidgetId,views);
            Toast.makeText(getApplicationContext(), "widget added", Toast.LENGTH_SHORT).show();
        }
    }


    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_default_width);
    }

    /* Block Comment - replacing Service with IntentService
    @Override
    public void onStart(Intent intent, int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for(int i=0; i<appWidgetIds.length; i++){
            int currentWidgetId = appWidgetIds[i];
            Log.v("ScoreWidgetService", "Id: " + currentWidgetId);
            RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.score_widget_medium);
            Intent clickIntent = new Intent(getApplicationContext(), MainActivity.class);
            //clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0,clickIntent, 0);

            views.setTextViewText(R.id.team1,"Arsenal");
            views.setTextViewText(R.id.team2,"Man Utd");
            views.setOnClickPendingIntent(R.id.team1, pending);
            appWidgetManager.updateAppWidget(currentWidgetId,views);
            Toast.makeText(getApplicationContext(), "widget added", Toast.LENGTH_SHORT).show();
        }
        super.onStart(intent,startId);
    }*/

    /*@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
