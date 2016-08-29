package com.swdmnd.avometer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
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

import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GetDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GetDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GetDataFragment extends Fragment implements BluetoothSearchDialog.BluetoothSearchDialogListener{

    private OnFragmentInteractionListener mListener;

    boolean autoScroll = true;

    BluetoothConnectionService btService = null;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    TextView logView, statusView;
    ScrollView logScroll;
    private Activity parentActivity;
    int statusBarHeight;
    String[] dataTokens;

    TextView tegangantv;
    TextView arustv;
    TextView tahanantv;
    TextView suhutv;

    Timer timer;

    Resources res;

    DatabaseHelper databaseHelper;

    public interface GetDataListener {
        public void changeBluetoothIcon(int id);
    }

    GetDataListener mCallBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mCallBack = (GetDataListener) getActivity();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement BluetoothSearchDialogListener");
        }
    }

    final Handler btServiceHandler = new Handler() {
        public void handleMessage(Message msg){
            switch(msg.what){
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    String[] tokens = readMessage.split(",");
                    dataTokens = tokens;
                    if(tokens.length == 4){
                        tegangantv.setText("Tegangan : "+tokens[0]);
                        arustv.setText("Arus : " + tokens[1]);
                        tahanantv.setText("Tahanan : " + tokens[2]);
                        suhutv.setText("Suhu : " + tokens[3]);
                    } else if(tokens.length == 6){
                        double[] datas = new double[4];
                        for(int i = 0; i < 4; ++i){
                            try{
                                datas[i] = Double.parseDouble(tokens[i+2]);
                            } catch (Exception e){
                                datas[i] = -99.9;
                            }
                        }
                        DataRecord dataRecord = new DataRecord(
                                databaseHelper.getLastId()+1,
                                tokens[0],
                                tokens[1],
                                datas[0],
                                datas[1],
                                datas[3],
                                datas[2]
                        );
                        databaseHelper.recordData(dataRecord);
                        btService.write(Constants.ASK_ALL_RECORDS);
                    } else if (tokens.length == 5){
                        btService.write(Constants.ASK_ALL_RECORDS);
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

    public static GetDataFragment newInstance() {
        GetDataFragment fragment = new GetDataFragment();
        Bundle args = new Bundle();
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
                mCallBack.changeBluetoothIcon(R.drawable.ic_bluetooth_connected_white_24dp);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)){
                msg = res.getString(R.string.bluetooth_disconnecting);
                statusView.setText(msg);
                updateLog(msg + "\n", Constants.LOG_SYSTEM);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                msg = res.getString(R.string.bluetooth_disconnected);
                statusView.setText(msg);
                updateLog(msg + "\n", Constants.LOG_SYSTEM);
                mCallBack.changeBluetoothIcon(R.drawable.ic_bluetooth_white_24dp);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
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
        databaseHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_data, container, false);
        statusView = (TextView) view.findViewById(R.id.bluetooth_status);
        logView = (TextView) view.findViewById(R.id.log_text);
        logScroll = (ScrollView) view.findViewById(R.id.log_scroll);
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

        CheckBox getRealTimeData = (CheckBox) view.findViewById(R.id.get_real_time_data);
        getRealTimeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox)view).isChecked()){
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {

                        @Override
                        public void run() {
                            if(btService.connected){
                                btService.write(Constants.ASK_REALTIME_DATA);
                            }
                        }

                    }, 0, Constants.MINIMUM_REALTIME_INTERVAL);
                } else {
                    timer.cancel();
                }
            }
        });

        tegangantv = (TextView) view.findViewById(R.id.data_tegangan);
        arustv = (TextView) view.findViewById(R.id.data_arus);
        tahanantv = (TextView) view.findViewById(R.id.data_tahanan);
        suhutv = (TextView) view.findViewById(R.id.data_suhu);

        Button getRecords = (Button) view.findViewById(R.id.get_all_records);
        getRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btService.connected){
                    databaseHelper.refreshDatabase();
                    btService.write(Constants.ASK_ALL_RECORDS);
                }
            }
        });

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

    public void btSearch(){
        BluetoothSearchDialog btSearchDialog = BluetoothSearchDialog.newInstance(mBluetoothAdapter);
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
