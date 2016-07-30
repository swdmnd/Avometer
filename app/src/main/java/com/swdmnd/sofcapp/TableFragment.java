package com.swdmnd.sofcapp;

import android.app.ProgressDialog;
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

    private TextView tableHeader;
    private TextView[] tableDatas = new TextView[5];
    private TableRow tableRow;
    private TableLayout tableLayout;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ProgressDialog pDialog;
    private OnFragmentInteractionListener mListener;
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
        View fragmentLayout = inflater.inflate(R.layout.fragment_table, container, false);

        tableLayout = (TableLayout) fragmentLayout.findViewById(R.id.main_table);

        Button addRecordButton = (Button) fragmentLayout.findViewById(R.id.btnAddRecord);
        addRecordButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecord();
            }
        });

        fragmentLayout.findViewById(R.id.root_view).setPadding(0, statusBarHeight, 0, 0);

        return fragmentLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        new LoadTable().execute();
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

    class LoadTable extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Sedang memuat...");
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... argumentsOnClassExecute) {
            updateTable();
            return null;
        }

        @Override
        protected void onPostExecute(String stringFromDoInBackground) {
            pDialog.dismiss();
        }
    }

    public void updateTable(){
        tableLayout.removeAllViews();

        List<String> dateLists = new ArrayList<>();
        List<DataRecord> dailyRecords = new ArrayList<DataRecord>();
        dateLists = databaseHelper.listDates();

        for (int i =0; i <5; ++i) {
            tableDatas[i] = new TextView(getActivity());
            tableDatas[i].setBackgroundResource(R.drawable.table_header_cell);
            tableDatas[i].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
            tableDatas[i].setGravity(Gravity.CENTER);
            tableDatas[i].setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
            tableDatas[i].setPadding(4, 4, 4, 4);
            tableDatas[i].setTextColor(Color.WHITE);
            tableDatas[i].setTypeface(null, Typeface.BOLD);
        }

        tableDatas[0].setText("Waktu");
        tableDatas[1].setText("Tegangan (V)");
        tableDatas[2].setText("Arus (A)");
        tableDatas[3].setText("Suhu (C)");
        tableDatas[4].setText("Resistansi");

        tableRow = new TableRow(getActivity());
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        for (int i =0; i <5; ++i) {
            tableRow.addView(tableDatas[i], i);
        }
        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        for (String date : dateLists){
            tableHeader = new TextView(getActivity());
            tableHeader.setBackgroundResource(R.drawable.table_date_cell);
            tableHeader.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
            tableHeader.setGravity(Gravity.CENTER);
            tableHeader.setTypeface(null, Typeface.BOLD);
            tableHeader.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
            tableHeader.setPadding(4,4,4,4);

            tableHeader.setText(date);

            tableRow = new TableRow(getActivity());
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            tableRow.addView(tableHeader, 0);

            tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            dailyRecords =  databaseHelper.getDailyRecord(date);

            int index = 1;
            for (DataRecord dailyRecord : dailyRecords){
                for (int i =0; i <5; ++i) {
                    tableDatas[i] = new TextView(getActivity());
                    if (index%2 == 1) tableDatas[i].setBackgroundResource(R.drawable.table_data_odd);
                    else tableDatas[i].setBackgroundResource(R.drawable.table_data_even);
                    tableDatas[i].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                    tableDatas[i].setGravity(Gravity.CENTER);
                    tableDatas[i].setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
                    tableDatas[i].setPadding(4,4,4,4);
                }

                ++index;

                tableDatas[0].setTypeface(null, Typeface.BOLD);
                tableDatas[0].setText(dailyRecord.getTime());
                tableDatas[1].setText(Double.toString(dailyRecord.getVoltage()));
                tableDatas[2].setText(Double.toString(dailyRecord.getCurrent()));
                tableDatas[3].setText(Double.toString(dailyRecord.getTemperature()));
                tableDatas[4].setText(Double.toString(dailyRecord.getResistance()));

                tableRow = new TableRow(getActivity());
                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                for (int i =0; i <5; ++i) {
                    tableRow.addView(tableDatas[i], i);
                }
                tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    public void addRecord(){
        DataRecord dataRecord = new DataRecord(
                databaseHelper.getLastId()+1,
                "2016-11-12",
                "23:00",
                25.33,
                35.22,
                26.11,
                99.0
        );

        databaseHelper.recordData(dataRecord);
        new LoadTable().execute();
    }
}
