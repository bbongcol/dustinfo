/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package techjun.com.dustinfo.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import techjun.com.dustinfo.R;
import techjun.com.dustinfo.model.Dust;
import techjun.com.dustinfo.model.DustSet;
import techjun.com.dustinfo.service.DustDBService;
import techjun.com.dustinfo.utils.LocationUtil;

/**
 * A sample which shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} within a
 * {@link android.support.v4.app.ListFragment} to add the 'swipe-to-refresh' gesture to a
 * {@link android.widget.ListView}. This is provided through the provided re-usable
 * {@link SwipeRefreshListFragment} class.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item. This item should be displayed in the Action Bar's overflow item.
 *
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 *
 * <p>This sample also provides the functionality to change the colors displayed in the
 * {@link android.support.v4.widget.SwipeRefreshLayout} through the options menu. This is meant to
 * showcase the use of color rather than being something that should be integrated into apps.
 */
public class SwipeRefreshListFragmentFragment extends SwipeRefreshListFragment {

    private static final String TAG = SwipeRefreshListFragmentFragment.class.getSimpleName();

    private static final int LIST_ITEM_COUNT = 24;
    DustDBService mDustDBService;
    boolean mBound = false;
    ArrayList<Dust> curDustArrayList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * Create an ArrayAdapter to contain the data for the ListView. Each item in the ListView
         * uses the system-defined simple_list_item_1 layout that contains one TextView.
         */

        ListAdapter adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                new ArrayList<String>());

        // Set the adapter between the ListView and its backing data.
        setListAdapter(adapter);
        setRefreshing(true);

        // BEGIN_INCLUDE (setup_refreshlistener)
        /**
         * Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */

        doBindService();

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //mDustDBService.requestDustData(LocationUtil.getInstance(getContext()).getCurrentSidoCity());
                curDustArrayList = null;
                initiateRefresh();
            }
        });

        setColorScheme(R.color.color_scheme_1_1, R.color.color_scheme_1_2,
                R.color.color_scheme_1_3, R.color.color_scheme_1_4);
        // END_INCLUDE (setup_refreshlistener)
    }
    // END_INCLUDE (setup_views)

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.main_menu, menu);
    }

    // BEGIN_INCLUDE (setup_refresh_menu_listener)
    /**
     * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout
     * progress bar, then initiate the background task that refreshes the content.
     *
     * <p>A color scheme menu item used for demonstrating the use of SwipeRefreshLayout's color
     * scheme functionality. This kind of menu item should not be incorporated into your app,
     * it just to demonstrate the use of color. Instead you should choose a color scheme based
     * off of your application's branding.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            /*
            case R.id.menu_refresh:

                // We make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
                if (!isRefreshing()) {
                    setRefreshing(true);
                }

                // Start our refresh background task
                initiateRefresh();
                return true;

            case R.id.menu_color_scheme_1:
                item.setChecked(true);

                // Change the colors displayed by the SwipeRefreshLayout by providing it with 4
                // color resource ids
                setColorScheme(R.color.color_scheme_1_1, R.color.color_scheme_1_2,
                        R.color.color_scheme_1_3, R.color.color_scheme_1_4);
                return true;

            case R.id.menu_color_scheme_2:
                item.setChecked(true);

                // Change the colors displayed by the SwipeRefreshLayout by providing it with 4
                // color resource ids
                setColorScheme(R.color.color_scheme_2_1, R.color.color_scheme_2_2,
                        R.color.color_scheme_2_3, R.color.color_scheme_2_4);
                return true;

            case R.id.menu_color_scheme_3:
                item.setChecked(true);

                // Change the colors displayed by the SwipeRefreshLayout by providing it with 4
                // color resource ids
                setColorScheme(R.color.color_scheme_3_1, R.color.color_scheme_3_2,
                        R.color.color_scheme_3_3, R.color.color_scheme_3_4);
                return true;
            */
        }

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE (setup_refresh_menu_listener)

    // BEGIN_INCLUDE (initiate_refresh)
    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DustDBService.LocalBinder binder = (DustDBService.LocalBinder) service;
            mBound = true;
            mDustDBService = binder.getService();
            mDustDBService.registerCallback(new DustDBService.ICurrentDustCallback() {
                @Override
                public void OnCurrentDust(ArrayList<Dust> dustArrayList) {
                    Log.d(TAG,"OnCurrentDust");
                    curDustArrayList = dustArrayList;
                    initiateRefresh();
                }
            });
            initiateRefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    void doBindService() {
        Log.d(TAG, "doBindService()");
        getActivity().bindService(new Intent(getActivity(), DustDBService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        Log.d(TAG, "doUnbindService()");
        if (mBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
        }
    }

    private void initiateRefresh() {

        /**
         * Execute the background task, which uses {@link AsyncTask} to load the data.
         */
        new BackgroundTask().execute();
    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)
    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(List<String> result) {
        Log.d(TAG, "onRefreshComplete()");
        // Remove all items from the ListAdapter, and then replace them with the new items
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        adapter.clear();
        for (String dust : result) {
            adapter.add(dust);
        }

        // Stop the refreshing indicator
        setRefreshing(false);
    }
    // END_INCLUDE (refresh_complete)

    /**
     * Dummy {@link AsyncTask} which simulates a long running task to fetch new cheeses.
     */
    private class BackgroundTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {

            List<String> dustInfoStr = new ArrayList<String>();
            if(curDustArrayList == null) {
                curDustArrayList = mDustDBService.requestDustData(LocationUtil.getInstance(getContext()).getCurrentSidoCity(), true);
            } else {
                for (int i = 0; i < curDustArrayList.size(); i++) {
                    dustInfoStr.add(curDustArrayList.get(i).getmDateTime() + "    PM10 : " + curDustArrayList.get(i).getmPM10() + "    PM2.5 : " + curDustArrayList.get(i).getmPM25());
                }
            }
            return dustInfoStr;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            if(result!=null && result.size() == 24) {
                onRefreshComplete(result);
            }
        }
    }
}
