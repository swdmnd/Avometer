package com.swdmnd.sofcapp;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothSearchDialog extends DialogFragment {

    private ArrayList<String> mDeviceList;
    private String btDeviceAddress = "30:14:12:08:09:32";
    private String btDeviceName = "";
    private ArrayAdapter<String> mDeviceListAdapter;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ListView listView;

    public interface BluetoothSearchDialogListener{
        void onReturnValue(String deviceName, String deviceAddress);
    }

    BluetoothSearchDialogListener mCallBack;

    private Activity parentActivity;

    TextView emptyTextView;
    char firstTime;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mCallBack = (BluetoothSearchDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement BluetoothSearchDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedStateInstance){
        super.onCreate(savedStateInstance);

        mDeviceList = new ArrayList<>();
        parentActivity = getTargetFragment().getActivity();

        mDeviceListAdapter = new ArrayAdapter<>(getTargetFragment().getActivity(), R.layout.list, mDeviceList);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        parentActivity.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        parentActivity.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        parentActivity.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        parentActivity.registerReceiver(mReceiver, filter);

        if (mBluetoothAdapter == null){
            Toast.makeText(parentActivity, getResources().getString(R.string.no_bluetooth_device), Toast.LENGTH_SHORT).show();
            dismiss();
        } else if (!mBluetoothAdapter.isEnabled()){
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, Constants.REQUEST_ENABLE_BT);
        } else {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if(mBluetoothAdapter!=null){ mBluetoothAdapter.cancelDiscovery(); }
        parentActivity.unregisterReceiver(mReceiver);

        super.onDismiss(dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Use builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.list_dialog, null);
        listView = (ListView) dialogView.findViewById(R.id.device_list);
        listView.setAdapter((ListAdapter) mDeviceListAdapter);
        emptyTextView = (TextView) dialogView.findViewById(R.id.bluetooth_empty);
        emptyTextView.setVisibility(View.VISIBLE);
        firstTime=1;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String btDeviceString = ((TextView) view.findViewById(R.id.device_item)).getText().toString();
                int truncateIndex = btDeviceString.length() - Constants.MAC_ADDRESS_LENGTH;
                btDeviceAddress = btDeviceString.substring(truncateIndex, btDeviceString.length());
                btDeviceName = btDeviceString.substring(0, truncateIndex - 1);
                mCallBack.onReturnValue(btDeviceName, btDeviceAddress);
                dismiss();
            }
        });

        Button refreshButton = (Button) dialogView.findViewById(R.id.btRefresh);
        refreshButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v){
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothAdapter.startDiscovery();

                mDeviceList = new ArrayList<>();
                mDeviceListAdapter = new ArrayAdapter<>(getTargetFragment().getActivity(), R.layout.list, mDeviceList);
                listView.setAdapter((ListAdapter) mDeviceListAdapter);
                emptyTextView.setVisibility(View.VISIBLE);
                firstTime=1;
            }
        });

        builder.setView(dialogView);

        return builder.create();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, getResources().getString(R.string.bluetooth_turned_off), Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(context, getResources().getString(R.string.bluetooth_turning_off), Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(context, getResources().getString(R.string.bluetooth_turned_on), Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(context, getResources().getString(R.string.bluetooth_turning_on), Toast.LENGTH_SHORT).show();
                        break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)){
                if(firstTime==1){
                    firstTime=0;
                    emptyTextView.setVisibility(View.GONE);
                }
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
                mDeviceListAdapter.notifyDataSetChanged();
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                mDeviceList.clear();
                Toast.makeText(context, getResources().getString(R.string.bluetooth_starts_searching), Toast.LENGTH_SHORT).show();
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                mBluetoothAdapter.cancelDiscovery();
                Toast.makeText(context, getResources().getString(R.string.bluetooth_stops_searching), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void  onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == Constants.REQUEST_ENABLE_BT){
            if(resultCode == parentActivity.RESULT_OK){
                //If bluetooth is enabled successfully
                mBluetoothAdapter.startDiscovery();
            } else if (resultCode == parentActivity.RESULT_CANCELED){
                Toast.makeText(parentActivity, getResources().getString(R.string.bluetooth_enable_fail), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
