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
 * A class for populating list view items
 */
public class DrawerListAdapter extends ArrayAdapter<String> {
    private final String[] itemname;
    private final int[] imgid;
    private LayoutInflater layoutInflater;

    public DrawerListAdapter(Activity context, String[] itemname, int[] imgid) {
        super(context, R.layout.drawer_list_item, itemname);
        // TODO Auto-generated constructor stub
        this.itemname=itemname;
        this.imgid=imgid;
        layoutInflater = context.getLayoutInflater();
    }

    public View getView(int position,View view,ViewGroup parent) {
        View rowView=layoutInflater.inflate(R.layout.drawer_list_item, null,true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.drawer_list_title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.drawer_img);

        txtTitle.setText(itemname[position]);
        imageView.setImageResource(imgid[position]);
        return rowView;
    }
}
