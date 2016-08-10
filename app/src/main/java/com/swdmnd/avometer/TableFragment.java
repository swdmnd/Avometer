package com.swdmnd.avometer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    DatabaseHelper databaseHelper;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ProgressDialog pDialog;

    private OnFragmentInteractionListener mListener;
    View fragmentLayout;
    TextView noDataIndicator;

    int statusBarHeight = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TableFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TableFragment newInstance(String param1, String param2) {
        TableFragment fragment = new TableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            statusBarHeight = getArguments().getInt("STATUS_BAR_HEIGHT");
        }

        databaseHelper = new DatabaseHelper(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentLayout = inflater.inflate(R.layout.fragment_table, container, false);

        Button addRecordButton = (Button) fragmentLayout.findViewById(R.id.btnAddRecord);
        addRecordButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecord();
            }
        });

        Button clearRecord = (Button) fragmentLayout.findViewById(R.id.btnClearRecords);
        clearRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseHelper.refreshDatabase();
                new LoadTableData().execute();
            }
        });

        fragmentLayout.findViewById(R.id.root_view).setPadding(0, statusBarHeight, 0, 0);

        noDataIndicator = (TextView) fragmentLayout.findViewById(R.id.database_empty);

        new LoadTableData().execute();

        return fragmentLayout;
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
        public void onFragmentInteraction(Uri uri);
    }

    private class LoadTableData extends AsyncTask<String, Object, Object>{
        TextView tableCell;
        List<String> dateLists;
        List<DataRecord> dailyRecords;
        TableRow tableRow;
        private ProgressDialog pDialog;
        TableLayout tableLayout;

        int backgroundProcessStatus;
        private final int WAITING_UI_THREAD = 0;
        private final int UI_THREAD_READY = 1;

        int tableRowPosition;
        private final int TABLE_HEADER_ROW = 2;
        private final int TABLE_DATE_ROW = 3;
        private final int TABLE_DATA_ROW = 4;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            tableLayout = new TableLayout(getActivity());
            dateLists = new ArrayList<>();
            dailyRecords = new ArrayList<>();

            backgroundProcessStatus = UI_THREAD_READY;
            tableRowPosition = TABLE_HEADER_ROW;

            noDataIndicator.setVisibility(View.VISIBLE);
            final AsyncTask<String,Object, Object> mTask = this;

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Sedang memuat...");
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    mTask.cancel(true);
                }
            });
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected void onProgressUpdate(Object... args){
            tableRow = new TableRow(getActivity());
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            int index = 0;
            switch(tableRowPosition){
                case TABLE_HEADER_ROW:
                    String[] headerTexts = {
                            "Waktu", "Tegangan (V)", "Arus (mA)", "Suhu (C)", "Resistansi"
                    };
                    for(String headerText : headerTexts){
                        tableCell = new TextView(getActivity());
                        tableCell.setBackgroundResource(R.drawable.table_header_cell);
                        tableCell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                        tableCell.setGravity(Gravity.CENTER);
                        tableCell.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
                        tableCell.setPadding(4, 4, 4, 4);
                        tableCell.setTextColor(Color.WHITE);
                        tableCell.setTypeface(null, Typeface.BOLD);
                        tableCell.setText(headerText);

                        tableRow.addView(tableCell, index++);
                    }
                    tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    backgroundProcessStatus = UI_THREAD_READY;
                    break;

                case TABLE_DATE_ROW:
                    String arg = (String) args[0];
                    String mYear = arg.substring(0, 4);
                    String mMonth = new DateFormatSymbols(Constants.APP_LOCALE).getMonths()[Integer.parseInt(arg.substring(5, 7))-1];
                    String mDate = arg.substring(8);
                    tableCell = new TextView(getActivity());
                    tableCell.setBackgroundResource(R.drawable.table_date_cell);
                    tableCell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                    tableCell.setGravity(Gravity.CENTER);
                    tableCell.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
                    tableCell.setPadding(4,4,4,4);
                    tableCell.setTypeface(null, Typeface.BOLD_ITALIC);
                    tableCell.setText(String.format(getResources().getString(R.string.date),mDate, mMonth, mYear));

                    tableRow.addView(tableCell, index);
                    tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    backgroundProcessStatus = UI_THREAD_READY;
                    break;

                case TABLE_DATA_ROW:
                    int row_index = 1;
                    for (DataRecord dailyRecord : dailyRecords){
                        index = 0;
                        String[] cellTexts = {
                                dailyRecord.getTime(),
                                Double.toString(dailyRecord.getVoltage()),
                                Double.toString(dailyRecord.getCurrent()),
                                Double.toString(dailyRecord.getTemperature()),
                                Double.toString(dailyRecord.getResistance())
                        };
                        tableRow = new TableRow(getActivity());
                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        for (String cellText : cellTexts) {
                            tableCell = new TextView(getActivity());
                            if (row_index%2 == 1) tableCell.setBackgroundResource(R.drawable.table_data_odd);
                            else tableCell.setBackgroundResource(R.drawable.table_data_even);
                            tableCell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                            tableCell.setGravity(Gravity.CENTER);
                            tableCell.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
                            tableCell.setPadding(4,4,4,4);
                            if(index==0) tableCell.setTypeface(null, Typeface.BOLD);
                            tableCell.setText(cellText);

                            tableRow.addView(tableCell, index++);
                        }

                        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                        ++row_index;
                    }
                    backgroundProcessStatus = UI_THREAD_READY;
                    break;
            }
        }

        @Override
        protected Object doInBackground(String... args){
            dateLists = databaseHelper.listDates();
            if (dateLists!=null){
                backgroundProcessStatus = WAITING_UI_THREAD;
                publishProgress();
                while(backgroundProcessStatus == WAITING_UI_THREAD){
                    if(isCancelled()) return null;
                }
                
                for (String date : dateLists){
                    tableRowPosition = TABLE_DATE_ROW;
                    backgroundProcessStatus = WAITING_UI_THREAD;
                    publishProgress(date);
                    while(backgroundProcessStatus == WAITING_UI_THREAD){
                        if(isCancelled()) return null;
                    }

                    dailyRecords =  databaseHelper.getDailyRecord(date);
                    tableRowPosition = TABLE_DATA_ROW;
                    backgroundProcessStatus = WAITING_UI_THREAD;
                    publishProgress(dailyRecords);
                    while(backgroundProcessStatus == WAITING_UI_THREAD){
                        if(isCancelled()) return null;
                    }
                }
            } else {
                return 1;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object arg){
            if(arg == null){
                View tableView = fragmentLayout.findViewById(R.id.main_table);
                ViewGroup parent = (ViewGroup) tableView.getParent();
                int index = parent.indexOfChild(tableView);
                parent.removeView(tableView);
                tableLayout.setId(R.id.main_table);
                parent.addView(tableLayout, index);
                noDataIndicator.setVisibility(View.GONE);
            }
            pDialog.dismiss();
        }
    }

    public void addRecord(){
        DataRecord dataRecord = new DataRecord(
                databaseHelper.getLastId()+1,
                "2016-01-25",
                "12:00",
                25.33,
                35.22,
                26.11,
                99.0
        );

        databaseHelper.recordData(dataRecord);
        new LoadTableData().execute();
    }
}
