package net.freelance.android.quakereport.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.freelance.android.quakereport.R;

import java.util.List;


/**
 * Created by Kyaw Khine on 12/16/2016.
 */
public class EarthquakeRVListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> cityListItems;

    public EarthquakeRVListAdapter(List<String> cityListItems) {
        this.cityListItems = cityListItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_city_list, parent, false);
        return new EarthquakeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String string = cityListItems.get(position);
        EarthquakeViewHolder earthquakeViewHolder = (EarthquakeViewHolder) holder;
        earthquakeViewHolder.tvCity.setText(string);
    }

    @Override
    public int getItemCount() {
        return cityListItems.size();
    }

    public class EarthquakeViewHolder extends RecyclerView.ViewHolder {
        TextView tvCity;

        public EarthquakeViewHolder(View itemView) {
            super(itemView);
            tvCity = (TextView) itemView.findViewById(R.id.tvLocationOffset);
        }
    }
}
