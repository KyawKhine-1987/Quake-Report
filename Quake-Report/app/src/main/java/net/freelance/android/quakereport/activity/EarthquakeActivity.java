package net.freelance.android.quakereport.activity;


import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.freelance.android.quakereport.adapter.EarthquakeAdapter;
import net.freelance.android.quakereport.utility.EarthquakeLoader;
import net.freelance.android.quakereport.R;
import net.freelance.android.quakereport.model.Earthquake;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    private static final String LOG_TAG = EarthquakeActivity.class.getName();

    /*private EarthquakeRVListAdapter adapter;
    private RecyclerView earthquakeRecyclerView;*/
    /**
     * URL for earthquake data from the USGS dataset
     */
    private static final String USGS_REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query";
 /*?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10
   private static final String USGS_REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=5&limit=10";
*/
    /**
     * Adapter for the list of earthquakes
     */
    private EarthquakeAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "TEST: EarthquakeActivity onCreate() called...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.rvListItems);

        // Create a new adapter that takes the list of earthquakes as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(mAdapter);

        /**
         * Initial State of appear screen
         * setEmptyView
         */
        mEmptyStateTextView = (TextView) findViewById(R.id.tvEmpty);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getmUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        //Checked the Internet or Network Connection on built-in device.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Set empty state text to display "No earthquakes found."
            mEmptyStateTextView.setText(R.string.empty_textview);

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            Log.i(LOG_TAG, "TEST: Calling initLoader() ...");
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            // Set no network or internet connection state text to display "No Internet Connection."
            mEmptyStateTextView.setText(R.string.noIC_textview);

            // Hide loading indicator because the data has been loaded
            // You can use View.GONE or INVISIBLE whatever you wished
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
       /* Log.i(LOG_TAG, "TEST: onCreateLoader() called ...");
        return new EarthquakeLoader(this, USGS_REQUEST_URL);*/
        Log.i(LOG_TAG, "TEST: onCreateLoader() called ...");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new EarthquakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        Log.i(LOG_TAG, "TEST: onLoadFinished() called ...");

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.empty_textview);

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (earthquakes != null && !earthquakes.isEmpty()) {
            mAdapter.addAll(earthquakes);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        Log.i(LOG_TAG, "TEST: onLoaderReset() called ...");
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

/*public class EarthquakeActivity extends AppCompatActivity {

    private static final String LOG_TAG = EarthquakeActivity.class.getName();

    *//*private EarthquakeRVListAdapter adapter;
    private RecyclerView earthquakeRecyclerView;*//*

    *//**
 * URL for earthquake data from the USGS dataset
 * <p>
 * Adapter for the list of earthquakes
 * <p>
 * {@link AsyncTask} to perform the network request on a background thread, and then
 * update the UI with the list of earthquakes in the response.
 * <p>
 * AsyncTask has three generic parameters: the input type, a type used for progress updates, and
 * an output type. Our task will take a String URL, and return an Earthquake. We won't do
 * progress updates, so the second generic is just Void.
 * <p>
 * We'll only override two of the methods of AsyncTask: doInBackground() and onPostExecute().
 * The doInBackground() method runs on a background thread, so it can run long-running code
 * (like network activity), without interfering with the responsiveness of the app.
 * Then onPostExecute() is passed the result of doInBackground() method, but runs on the
 * UI thread, so it can use the produced data to update the UI.
 * <p>
 * MAIN THREAD
 * <p>
 * This method runs on a background thread and performs the network request.
 * We should not update the UI from a background thread, so we return a list of
 * {@link Earthquake}s as the result.
 * <p>
 * This method runs on the main UI thread after the background work has been
 * completed. This method receives as input, the return value from the doInBackground()
 * method. First we clear out the adapter, to get rid of earthquake data from a previous
 * query to USGS. Then we update the adapter with the new list of earthquakes,
 * which will trigger the ListView to re-populate its list items.
 * <p>
 * Adapter for the list of earthquakes
 * <p>
 * {@link AsyncTask} to perform the network request on a background thread, and then
 * update the UI with the list of earthquakes in the response.
 * <p>
 * AsyncTask has three generic parameters: the input type, a type used for progress updates, and
 * an output type. Our task will take a String URL, and return an Earthquake. We won't do
 * progress updates, so the second generic is just Void.
 * <p>
 * We'll only override two of the methods of AsyncTask: doInBackground() and onPostExecute().
 * The doInBackground() method runs on a background thread, so it can run long-running code
 * (like network activity), without interfering with the responsiveness of the app.
 * Then onPostExecute() is passed the result of doInBackground() method, but runs on the
 * UI thread, so it can use the produced data to update the UI.
 * <p>
 * MAIN THREAD
 * <p>
 * This method runs on a background thread and performs the network request.
 * We should not update the UI from a background thread, so we return a list of
 * {@link Earthquake}s as the result.
 * <p>
 * This method runs on the main UI thread after the background work has been
 * completed. This method receives as input, the return value from the doInBackground()
 * method. First we clear out the adapter, to get rid of earthquake data from a previous
 * query to USGS. Then we update the adapter with the new list of earthquakes,
 * which will trigger the ListView to re-populate its list items.
 *//*
    *//*private static final String USGS_REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10";*//*
    private static final String USGS_REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=5&limit=10";

    *//**
 * Adapter for the list of earthquakes
 *//*
    private EarthquakeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Get the list of earthquakes from {@link QueryUtils}
       *//* ArrayList<Earthquake> earthquakes = QueryUtils.extractEarthquakes();*//*

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.rvListItems);

        // Create a new adapter that takes the list of earthquakes as input
        *//*final EarthquakeAdapter adapter = new EarthquakeAdapter(this, earthquakes);*//*
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        *//*earthquakeListView.setAdapter(adapter);*//*
        earthquakeListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
       *//* earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
               *//**//* Earthquake currentEarthquake = adapter.getItem(position);*//**//*
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getmUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });*//*

        // Start the AsyncTask to fetch the earthquake data
        *//*EarthquakeAsyncTask task = new EarthquakeAsyncTask();
        task.execute(USGS_REQUEST_URL);*//*
        new EarthquakeAsyncTask().execute(USGS_REQUEST_URL);

    }

    *//**
 * {@link AsyncTask} to perform the network request on a background thread, and then
 * update the UI with the list of earthquakes in the response.
 * <p/>
 * AsyncTask has three generic parameters: the input type, a type used for progress updates, and
 * an output type. Our task will take a String URL, and return an Earthquake. We won't do
 * progress updates, so the second generic is just Void.
 * <p/>
 * We'll only override two of the methods of AsyncTask: doInBackground() and onPostExecute().
 * The doInBackground() method runs on a background thread, so it can run long-running code
 * (like network activity), without interfering with the responsiveness of the app.
 * Then onPostExecute() is passed the result of doInBackground() method, but runs on the
 * UI thread, so it can use the produced data to update the UI.
 *//*
    private class EarthquakeAsyncTask extends AsyncTask<String, Void, List<Earthquake>> {
        *//**
 * MAIN THREAD
 *//*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(EarthquakeActivity.this, "Earthquake JSON Data is\n downloading right now!", Toast.LENGTH_LONG).show();
        }

        *//**
 * This method runs on a background thread and performs the network request.
 * We should not update the UI from a background thread, so we return a list of
 * {@link Earthquake}s as the result.
 *//*
        @Override
        protected List<Earthquake> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            List<Earthquake> result = QueryUtils.fetchEarthquakeData(urls[0]);
            return result;
        }

        *//**
 * This method runs on the main UI thread after the background work has been
 * completed. This method receives as input, the return value from the doInBackground()
 * method. First we clear out the adapter, to get rid of earthquake data from a previous
 * query to USGS. Then we update the adapter with the new list of earthquakes,
 * which will trigger the ListView to re-populate its list items.
 *//*
        @Override
        protected void onPostExecute(List<Earthquake> data) {
            // Clear the adapter of previous earthquake data
            mAdapter.clear();

            // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                mAdapter.addAll(data);
            }
        }
    }
}*/

/*earthquakeRecyclerView = (RecyclerView) findViewById(R.id.rvListItems);

        LinearLayoutManager verticalLinearManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        earthquakeRecyclerView.setLayoutManager(verticalLinearManager);

        List<String> cityLists = Arrays.asList("San Francisco", "London", "Tokyo", "Mexico City", "Moscow", "Rio de Janerio", "Paris");

        adapter = new EarthquakeRVListAdapter(cityLists);

        earthquakeRecyclerView.setAdapter(adapter);*/

/*    ArrayList<Earthquake> earthquakes = new ArrayList<>();
earthquakes.add(new Earthquake("7.2", "San Francisco", "Feb 2, 2016"));
        earthquakes.add(new Earthquake("6.1", "London", "July 20, 2015"));
        earthquakes.add(new Earthquake("3.9", "Tokyo", "Nov 10, 2014"));
        earthquakes.add(new Earthquake("5.4", "Mexico City", "May 3, 2014"));
        earthquakes.add(new Earthquake("2.8", "Moscow", "Jan 31, 2013"));
        earthquakes.add(new Earthquake("4.9", "Rio de Janerio", "Aug 19, 2012"));
        earthquakes.add(new Earthquake("1.6", "Paris", "Oct 30, 2011"));

        ListView listView = (ListView) findViewById(R.id.rvListItems);

        EarthquakeAdapter earthquakeAdapter = new EarthquakeAdapter(this, earthquakes);

        listView.setAdapter(earthquakeAdapter);*/

/*  ArrayList<String> earthquakes = new ArrayList<>();
        earthquakes.add("San Francisco");
        earthquakes.add("London");
        earthquakes.add("Tokyo");
        earthquakes.add("Mexico City");
        earthquakes.add("Moscow");
        earthquakes.add("Rio de Janerio");
        earthquakes.add("Paris");

        ListView earthquakeListView = (ListView) findViewById(R.id.rvListItems);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, earthquakes);
        earthquakeListView.setAdapter(adapter);*/

