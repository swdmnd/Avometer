package com.swdmnd.sofcapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GetDataFragment.GetDataListener{

    private String[] mDrawerHomeMenus;
    private int[] mDrawerHomeMenuImages;

    private String[] mDrawerMainMenus;
    private int[] mDrawerImages;

    private String[] mDrawerSettingMenus;
    private int[] mDrawerSettingMenuImages;

    private SparseArrayCompat<Integer> actionButtonIcon = new SparseArrayCompat<>();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ListView mDrawerSettingMenuList;
    private ListView mDrawerHomeMenuList;
    private LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private int statusBarHeight = 0;

    private int drawerMainMenuLastPosition=0, drawerSettingMenuLastPosition=0, drawerHomeMenuLastPosition;
    private int drawerCurrentPosition, drawerLastPosition;
    private Activity parentActivity;
    private ActionBar actionBar;

    private int[] assignIconsIds(TypedArray ar){
        int len = ar.length();
        int[] target = new int[len];
        for (int i = 0; i < len; i++)
            target[i] = ar.getResourceId(i, 0);
        ar.recycle();
        return target;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();

        actionButtonIcon.put(Constants.KEY_BLUETOOTH_ICON, R.drawable.ic_bluetooth_white_24dp);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // Inflate the "decor.xml"
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DrawerLayout drawer = (DrawerLayout) inflater.inflate(R.layout.decor, null); // "null" is important.
        drawer.findViewById(R.id.left_drawer).setPadding(0, statusBarHeight, 0, 0);

        // HACK: "steal" the first child of decor view
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        FrameLayout container = (FrameLayout) drawer.findViewById(R.id.content_frame); // This is the container we defined just now.
        container.addView(child);

        // Make the drawer replace the first child
        decor.addView(drawer);

        parentActivity = this;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerList = (ListView) findViewById(R.id.drawer_main_menu_list);
        mDrawerHomeMenuList = (ListView) findViewById(R.id.drawer_home_menu_list);
        mDrawerSettingMenuList = (ListView) findViewById(R.id.drawer_setting_menu_list);
        mDrawer = (LinearLayout) findViewById(R.id.left_drawer);

        mDrawerMainMenus = getResources().getStringArray(R.array.drawer_main_menus);
        mDrawerSettingMenus = getResources().getStringArray(R.array.drawer_setting_menus);
        mDrawerHomeMenus = getResources().getStringArray(R.array.drawer_home_menus);

        mDrawerImages = assignIconsIds(getResources().obtainTypedArray(R.array.drawer_main_icons));
        mDrawerSettingMenuImages = assignIconsIds(getResources().obtainTypedArray(R.array.drawer_setting_icons));
        mDrawerHomeMenuImages = assignIconsIds(getResources().obtainTypedArray(R.array.drawer_home_icons));

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerListAdapter(this, mDrawerMainMenus, mDrawerImages));
        mDrawerSettingMenuList.setAdapter(new DrawerListAdapter(this, mDrawerSettingMenus, mDrawerSettingMenuImages));
        mDrawerHomeMenuList.setAdapter(new DrawerListAdapter(this, mDrawerHomeMenus, mDrawerHomeMenuImages));

        // Set the list's click listener
        mDrawerHomeMenuList.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                drawerCurrentPosition = Constants.DRAWER_POSITION_HOME_MENU;
                selectItem(position);
            }
        });

        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                drawerCurrentPosition = Constants.DRAWER_POSITION_MAIN_MENU;
                selectItem(position);
            }
        });

        mDrawerSettingMenuList.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                drawerCurrentPosition = Constants.DRAWER_POSITION_SETTING_MENU;
                selectItem(position);
            }
        });

        mTitle =  getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }

        drawerCurrentPosition = drawerLastPosition = Constants.DRAWER_POSITION_HOME_MENU;
        selectItem(0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if(actionBar != null){
            actionBar.setTitle(mTitle);
        }
    }

    public void changeBluetoothIcon(int id){
        actionButtonIcon.put(Constants.KEY_BLUETOOTH_ICON, id);
        invalidateOptionsMenu();
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem settingsItem;
        switch (drawerCurrentPosition){
            case Constants.DRAWER_POSITION_MAIN_MENU:
                switch (drawerMainMenuLastPosition){
                    case 2:
                        settingsItem = menu.findItem(R.id.action_bluetooth_search);
                        // set your desired icon here based on a flag if you like
                        settingsItem.setIcon(ContextCompat.getDrawable(this, actionButtonIcon.get(Constants.KEY_BLUETOOTH_ICON)));
                        break;
                }
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        Fragment fragment = null;
        switch(drawerCurrentPosition){
            case Constants.DRAWER_POSITION_HOME_MENU:
                switch(position){
                    case 0:
                        fragment = new HomeFragment();
                        break;
                }

                /**** Pass parameters to fragment ****/
                if(fragment != null){
                    Bundle args = new Bundle();
                    args.putInt("STATUS_BAR_HEIGHT", statusBarHeight);
                    fragment.setArguments(args);

                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();
                }

                // Highlight the selected item, update the title, and close the drawer
                mDrawerHomeMenuList.setItemChecked(position, true);
                setTitle(mDrawerHomeMenus[position]);
                drawerHomeMenuLastPosition = position;
                mDrawerSettingMenuList.setItemChecked(drawerSettingMenuLastPosition, false);
                mDrawerList.setItemChecked(drawerMainMenuLastPosition, false);
                mDrawerLayout.closeDrawer(mDrawer);
                break;

            case Constants.DRAWER_POSITION_MAIN_MENU:
                switch(position){
                    case 0:
                        fragment = new TableFragment();
                        break;

                    case 1:
                        fragment = new ChartFragment();
                        break;

                    case 2:
                        fragment = new GetDataFragment();
                        break;

                    case 3:
                        fragment = new PrintFragment();
                        break;
                }

                /**** Pass parameters to fragment ****/
                if(fragment != null){
                    Bundle args = new Bundle();
                    args.putInt("STATUS_BAR_HEIGHT", statusBarHeight);
                    fragment.setArguments(args);

                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();
                }

                // Highlight the selected item, update the title, and close the drawer
                mDrawerList.setItemChecked(position, true);
                setTitle(mDrawerMainMenus[position]);
                drawerMainMenuLastPosition = position;
                mDrawerSettingMenuList.setItemChecked(drawerSettingMenuLastPosition, false);
                mDrawerHomeMenuList.setItemChecked(drawerHomeMenuLastPosition, false);
                mDrawerLayout.closeDrawer(mDrawer);
                break;

            case Constants.DRAWER_POSITION_SETTING_MENU:
                switch(position){
                    case 0:
                        fragment = new SettingsFragment();
                        break;

                    case 1:
                        new AlertDialog.Builder(parentActivity)
                                .setTitle(R.string.about_us)
                                .setMessage(R.string.about_us_desc)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setIcon(R.drawable.ic_info_outline_black_24dp)
                                .show();
                        break;

                    case 2:
                        finish();
                        break;
                }

                /**** Pass parameters to fragment ****/
                if(fragment != null){
                    Bundle args = new Bundle();
                    args.putInt("STATUS_BAR_HEIGHT", statusBarHeight);
                    fragment.setArguments(args);

                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();
                }

                if(position<1){
                    // Highlight the selected item, update the title, and close the drawer
                    mDrawerSettingMenuList.setItemChecked(position, true);
                    setTitle(mDrawerSettingMenus[position]);
                    drawerSettingMenuLastPosition = position;
                    mDrawerList.setItemChecked(drawerMainMenuLastPosition, false);
                    mDrawerHomeMenuList.setItemChecked(drawerHomeMenuLastPosition, false);
                } else {
                    if(drawerLastPosition == Constants.DRAWER_POSITION_SETTING_MENU){
                        mDrawerSettingMenuList.setItemChecked(drawerSettingMenuLastPosition, true);
                    } else {
                        drawerCurrentPosition = drawerLastPosition;
                        mDrawerSettingMenuList.setItemChecked(position, false);
                    }
                }
                mDrawerLayout.closeDrawer(mDrawer);
                break;
        }
        drawerLastPosition = drawerCurrentPosition;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch (drawerCurrentPosition){
            case Constants.DRAWER_POSITION_MAIN_MENU:
                switch (drawerMainMenuLastPosition){
                    case 0:
                    case 1:
                        getMenuInflater().inflate(R.menu.menu_data_utilities, menu);
                        break;

                    case 2:
                        getMenuInflater().inflate(R.menu.menu_get_data, menu);
                        break;
                }
                break;
        }
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_bluetooth_search:
                GetDataFragment getDataFragment = (GetDataFragment)
                        getSupportFragmentManager().findFragmentById(R.id.content_frame);
                getDataFragment.btSearch();
                //createPdf();
                return true;

            case R.id.action_get_data:
                drawerCurrentPosition = Constants.DRAWER_POSITION_MAIN_MENU;
                selectItem(2);
        }

        return super.onOptionsItemSelected(item);
    }
}
