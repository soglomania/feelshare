package io.soglomania.feelshare.feelings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.soglomania.feelshare.R;
import io.soglomania.feelshare.model.Moods;

/**
 * Created by sogloarcadius on 08/03/17.
 */

public class CustomGridAdapter extends BaseAdapter implements Filterable {
    private Context mContext;
    private ValueFilter valueFilter;
    private ArrayList<Moods> _moods = new ArrayList<Moods>();
    private ArrayList<Moods> mStringFilterList;


    public CustomGridAdapter(Context c, ArrayList<Moods> _moods) {

        super();
        this.mContext = c;
        this._moods = _moods;
        mStringFilterList = _moods;
        getFilter();
    }

    @Override
    public int getCount() {
        return _moods.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        if (convertView == null) {
//        grid = new View(mContext);
        grid = inflater.inflate(R.layout.grid_single, null);
        TextView textView = (TextView) grid.findViewById(R.id.grid_text);
        ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);
        textView.setText(_moods.get(position).getName() + " : " + _moods.get(position).getDesc());
        imageView.setImageResource(_moods.get(position).getImg());
//        } else {
//            grid = (View) convertView;
//        }

        return grid;
    }


    @Override
    public Filter getFilter() {
        if (valueFilter == null) {

            valueFilter = new ValueFilter();
        }

        return valueFilter;
    }


    private class ValueFilter extends Filter {

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Moods> filterList = new ArrayList<Moods>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if ((mStringFilterList.get(i).getDesc().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {
                        Moods moods = new Moods();
                        moods.setDesc(mStringFilterList.get(i).getDesc());
                        moods.setName(mStringFilterList.get(i).getName());
                        moods.setImg(mStringFilterList.get(i).getImg());
                        filterList.add(moods);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            _moods = (ArrayList<Moods>) results.values;
            notifyDataSetChanged();
        }
    }
}