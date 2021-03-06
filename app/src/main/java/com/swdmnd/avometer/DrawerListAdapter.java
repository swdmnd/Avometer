package com.swdmnd.avometer;

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
    private final String[] itemName;
    private final int[] imgId;
    private final int menuGroup;
    private LayoutInflater layoutInflater;

    static class ViewHolder{
        TextView titleText;
        ImageView imageView;
    }

    public DrawerListAdapter(Activity context, String[] itemName, int[] imgId, int menuGroup) {
        super(context, R.layout.drawer_list_item, itemName);

        this.menuGroup = menuGroup;
        this.itemName=itemName;
        this.imgId=imgId;
        layoutInflater = context.getLayoutInflater();
    }

    public View getView(int position,View view,ViewGroup parent) {
        ViewHolder mViewHolder;
        if(view == null){
            mViewHolder = new ViewHolder();
            view = layoutInflater.inflate(R.layout.drawer_list_item, null,true);
            mViewHolder.titleText = (TextView) view.findViewById(R.id.drawer_list_title);
            mViewHolder.imageView = (ImageView) view.findViewById(R.id.drawer_img);
            view.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) view.getTag();
        }

        if(menuGroup == Constants.DRAWER_POSITION_HOME_MENU)
            view.setBackground(getContext().getResources().getDrawable(R.drawable.drawer_background_alternate));
        mViewHolder.titleText.setText(itemName[position]);
        mViewHolder.imageView.setImageResource(imgId[position]);
        return view;
    }
}
