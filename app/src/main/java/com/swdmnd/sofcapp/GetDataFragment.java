package com.swdmnd.sofcapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GetDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GetDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GetDataFragment extends Fragment implements BluetoothSearchDialog.BluetoothSearchDialogListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    boolean autoScroll = true;

    BluetoothConnectionService btService = null;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    TextView logView, statusView;
    ScrollView logScroll;
    Button btSearchButton;
    private Activity parentActivity;
    int statusBarHeight;
    String[] dataTokens;

    TextView tegangantv;
    TextView arustv;
    TextView tahanantv;
    TextView suhutv;

    Resources res;

    final Handler btServiceHandler = new Handler() {
        public void handleMessage(Message msg){
            switch(msg.what){
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    String[] tokens = readMessage.split("\\s+");
                    dataTokens = tokens;
                    if(tokens.length == 4){
                        tegangantv.setText("Tegangan : "+tokens[0]);
                        arustv.setText("Arus : " + tokens[1]);
                        tahanantv.setText("Tahanan : " + tokens[2]);
                        suhutv.setText("Suhu : " + tokens[3]);
                    }

                    updateLog(readMessage, Constants.LOG_OTHERS);
                    break;
            }
        }
    };

    SpannableStringBuilder builder;
    Spannable span_msg;

    private void updateLog(String s, int logType) {
        switch (logType){
            case Constants.LOG_USER_INPUT:
                builder = new SpannableStringBuilder();
                span_msg = new SpannableString(s);
                span_msg.setSpan(new ForegroundColorSpan(Color.BLUE), 2, span_msg.length(), 0);
                builder.append(span_msg);
                logView.append(builder);
                break;

            case Constants.LOG_SYSTEM:
                builder = new SpannableStringBuilder();
                span_msg = new SpannableString(s);
                span_msg.setSpan(new ForegroundColorSpan(Color.RED), 0, span_msg.length(), 0);
                builder.append(span_msg);
                logView.append(builder);
                break;

            case Constants.LOG_OTHERS:
                logView.append(s);
                break;
        }

        if(autoScroll) logScroll.fullScroll(View.FOCUS_DOWN);
    }

    public GetDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GetDataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GetDataFragment newInstance(String param1, String param2) {
        GetDataFragment fragment = new GetDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String msg = "";

            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
                msg = String.format(res.getString(R.string.bluetooth_connected),device.getName(), device.getAddress());
                statusView.setText(msg);
                updateLog(msg + "\n", Constants.LOG_SYSTEM);
                btSearchButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bluetooth_connected_black_24dp, 0);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)){
                msg = res.getString(R.string.bluetooth_disconnecting);
                statusView.setText(msg);
                updateLog(msg + "\n", Constants.LOG_SYSTEM);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                msg = res.getString(R.string.bluetooth_disconnected);
                statusView.setText(msg);
                updateLog(msg + "\n", Constants.LOG_SYSTEM);
                btSearchButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bluetooth_disabled_black_24dp, 0);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            statusBarHeight = getArguments().getInt("STATUS_BAR_HEIGHT");
        }
        btService = new BluetoothConnectionService(getActivity(), btServiceHandler, mBluetoothAdapter);
        btService.start();

        parentActivity = getActivity();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        parentActivity.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        parentActivity.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        parentActivity.registerReceiver(mReceiver, filter);
        res = getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_data, container, false);
        statusView = (TextView) view.findViewById(R.id.bluetooth_status);
        logView = (TextView) view.findViewById(R.id.log_text);
        logScroll = (ScrollView) view.findViewById(R.id.log_scroll);
        btSearchButton = (Button) view.findViewById(R.id.bluetooth_search);
        btSearchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btSearch();
            }
        });
        Button btSettings = (Button) view.findViewById(R.id.bluetooth_setting);
        btSettings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast.makeText(getActivity(), "Settings is not applied yet", Toast.LENGTH_SHORT).show();
            }
        });
        CheckBox autoScrollCheckBox = (CheckBox) view.findViewById(R.id.auto_scroll);
        autoScrollCheckBox.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (((CheckBox)v).isChecked()){
                    autoScroll = true;
                } else {
                    autoScroll = false;
                }
            }
        });

        tegangantv = (TextView) view.findViewById(R.id.data_tegangan);
        arustv = (TextView) view.findViewById(R.id.data_arus);
        tahanantv = (TextView) view.findViewById(R.id.data_tahanan);
        suhutv = (TextView) view.findViewById(R.id.data_suhu);

        view.findViewById(R.id.root_view).setPadding(0, statusBarHeight, 0, 0);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onDestroy(){
        btService.cancel();
        parentActivity.unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    void btSearch(){
        BluetoothSearchDialog btSearchDialog = new BluetoothSearchDialog();
        btSearchDialog.setTargetFragment(this, 1);
        btSearchDialog.show(getFragmentManager().beginTransaction(), null);
    }

    /**
     * Methods implementation of BluetoothSearchDialog.BluetoothSearchDialogListener interface
     */
    public void onReturnValue(String deviceName, String deviceAddress){
        btService.start();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        String msg = String.format(res.getString(R.string.bluetooth_connecting),device.getName(), device.getAddress());
        updateLog(msg + "\n", Constants.LOG_SYSTEM);
        try {
            btService.connect(device);
        } catch (Exception e){
            msg = String.format(res.getString(R.string.bluetooth_connect_fail),device.getName(), device.getAddress());
            updateLog(msg + "\n", Constants.LOG_SYSTEM);
            return;
        }
    }
}
