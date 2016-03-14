package barqsoft.footballscores.widget;

/**
 * Created by GAURAV on 06-03-2016.
 */
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;
import barqsoft.footballscores.Utilies;

public class ScoreWidgetRemoteService extends RemoteViewsService {
    String homeScore = "";
    String awayScore = "";

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL
    };

    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOALS = 3;
    public static final int COL_AWAY_GOALS = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            private RemoteViews parentView;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
                String[] date = new String[1];
                date[0] = Utilies.getFormatDate(0);
                data = getContentResolver().query(dateUri, SCORE_COLUMNS, DatabaseContract.PATH_DATE, date, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews view = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String homeName = data.getString(COL_HOME);
                String awayName = data.getString(COL_AWAY);
                String homeGoal = data.getString(COL_HOME_GOALS);
                String awayGoal = data.getString(COL_AWAY_GOALS);

                view.setTextViewText(R.id.home_name, homeName);
                view.setTextViewText(R.id.away_name, awayName);
                view.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(homeName));
                view.setImageViewResource(R.id.away_crest,Utilies.getTeamCrestByTeamName(awayName));

                if (homeGoal.equals("-1")){
                    homeGoal = "0";
                    awayGoal = "0";
                    //Log.v("ScoreWidgetRemote", "score: " + homeGoal);
                }
                view.setTextViewText(R.id.match_score,String.format("%s-%s",homeGoal,awayGoal));

                final Intent intent = new Intent();

                Uri scoreUri = DatabaseContract.scores_table.buildScoreWithDate();

                intent.setData(scoreUri);
                view.setOnClickFillInIntent(R.id.widget_list_item, intent);
                return view;
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(1);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}