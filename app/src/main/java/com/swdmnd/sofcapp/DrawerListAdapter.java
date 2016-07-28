package com.swdmnd.sofcapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Arief on 7/27/2016.
 */
public class DrawerListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] itemname;
    private final Integer[] imgid;

    public DrawerListAdapter(Activity context, String[] itemname, Integer[] imgid) {
        super(context, R.layout.drawer_list_item, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.imgid=imgid;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.drawer_list_item, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.drawer_list_title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.drawer_img);

        txtTitle.setText(itemname[position]);
        imageView.setImageResource(imgid[position]);
        return rowView;
    };
}
