package Com.mariapublishers.mariaexecutive;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

public class GridAdapter extends ArrayAdapter {
    Context context;
    public GridAdapter(Context context, int resourceId, List<RowItem> rowItems) {
        super(context, resourceId, rowItems);
        this.context = context;
    }
    private class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = (RowItem) getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.grid_list_item, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.textView.setText(rowItem.getName());
        holder.imageView.setImageResource(rowItem.getImage());
        return convertView;
    }
}
