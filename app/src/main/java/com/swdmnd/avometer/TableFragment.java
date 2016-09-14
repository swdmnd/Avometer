package com.swdmnd.avometer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment implements DatePickerFragment.CallbackListener{
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
    TextSwitcher setDate;

    int currentDate = 0;
    int currentMonth = 0;
    int currentYear = 0;
    String mYear;
    String mMonth;
    String mDate;

    Animation inLeft;
    Animation outRight;
    Animation inRight;
    Animation outLeft;

    int statusBarHeight = 0;

    int tableViewIndex = 0;
    ViewGroup parentViewGroup;

    LineChart lineChart;
    ArrayList<Entry> values;
    LineDataSet lineDataSet;
    ArrayList<String> labels;
    LineData lineData;

    boolean showTable = true;

    private static int CHART_STATE_VOLTAGE = 0;
    private static int CHART_STATE_CURRENT = 1;
    private static int CHART_STATE_TEMPERATURE = 2;
    private static int CHART_STATE_RESISTANCE = 3;

    int chartState = CHART_STATE_VOLTAGE;

    RadioGroup radioGroup;

    public void toggleView(){
        showTable = (!showTable);
        new LoadTableData().execute();
    }

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

    public void onReturnValue(int year, int month, int date){
        mYear = String.valueOf(currentYear=year);
        mMonth = new DateFormatSymbols(Constants.APP_LOCALE).getMonths()[currentMonth=month];
        mDate = String.valueOf(currentDate=date);
        setDate.setText(String.format(getResources().getString(R.string.date),mDate, mMonth, mYear));

        new LoadTableData().execute();
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

    private void shiftDate(int offset){
        Calendar c = Calendar.getInstance();
        c.set(currentYear,currentMonth,currentDate);
        c.add(Calendar.DAY_OF_MONTH, offset);
        if(offset>0){
            setDate.setInAnimation(inRight);
            setDate.setOutAnimation(outLeft);
        } else {
            setDate.setInAnimation(inLeft);
            setDate.setOutAnimation(outRight);
        }
        currentMonth = c.get(Calendar.MONTH);
        currentDate = c.get(Calendar.DAY_OF_MONTH);
        currentYear = c.get(Calendar.YEAR);

        mYear = String.valueOf(currentYear);
        mMonth = new DateFormatSymbols(Constants.APP_LOCALE).getMonths()[currentMonth];
        mDate = String.valueOf(currentDate);
        setDate.setText(String.format(getResources().getString(R.string.date),mDate, mMonth, mYear));

        new LoadTableData().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentLayout = inflater.inflate(R.layout.fragment_table, container, false);

        /*Button clearRecord = (Button) fragmentLayout.findViewById(R.id.btnClearRecords);
        clearRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseHelper.refreshDatabase();
                new LoadTableData().execute();
            }
        });*/

        final Calendar c = Calendar.getInstance();

        mYear = String.valueOf(currentYear = c.get(Calendar.YEAR));
        mMonth = new DateFormatSymbols(Constants.APP_LOCALE).getMonths()[currentMonth = c.get(Calendar.MONTH)];
        mDate = String.valueOf(currentDate = c.get(Calendar.DAY_OF_MONTH));

        fragmentLayout.findViewById(R.id.root_view).setPadding(0, statusBarHeight, 0, 0);

        noDataIndicator = (TextView) fragmentLayout.findViewById(R.id.database_empty);

        fragmentLayout.findViewById(R.id.buttonTomorrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shiftDate(1);
            }
        });

        fragmentLayout.findViewById(R.id.buttonYesterday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shiftDate(-1);
            }
        });

        // Declare the in and out animations and initialize them
        inLeft = AnimationUtils.loadAnimation(getActivity(),android.R.anim.slide_in_left);
        outRight = AnimationUtils.loadAnimation(getActivity(),android.R.anim.slide_out_right);
        inRight = AnimationUtils.loadAnimation(getActivity(),R.anim.slide_in_right);
        outLeft = AnimationUtils.loadAnimation(getActivity(),R.anim.slide_out_left);

        setDate = (TextSwitcher) fragmentLayout.findViewById(R.id.buttonSetDate);

        setDate.setInAnimation(inLeft);
        setDate.setOutAnimation(outRight);

        setDate.setText(String.format(getResources().getString(R.string.date),mDate, mMonth, mYear));
        setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        fragmentLayout.findViewById(R.id.tableScrollView).setOnTouchListener(new OnSwipeTouchListener(this.getActivity()) {
            @Override
            public void onSwipeLeft() {
                shiftDate(1);
            }

            @Override
            public void onSwipeRight(){
                shiftDate(-1);
            }
        });

        View tableView = fragmentLayout.findViewById(R.id.main_table);
        parentViewGroup = (ViewGroup) tableView.getParent();
        tableViewIndex = parentViewGroup.indexOfChild(tableView);

        lineChart = (LineChart) fragmentLayout.findViewById(R.id.chart);

        RadioButton radioButton = (RadioButton) fragmentLayout.findViewById(R.id.radioTegangan);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRadioButtonClicked(view);
            }
        });
        radioButton = (RadioButton) fragmentLayout.findViewById(R.id.radioArus);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRadioButtonClicked(view);
            }
        });
        radioButton = (RadioButton) fragmentLayout.findViewById(R.id.radioSuhu);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRadioButtonClicked(view);
            }
        });
        radioButton = (RadioButton) fragmentLayout.findViewById(R.id.radioTahanan);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRadioButtonClicked(view);
            }
        });

        radioGroup = (RadioGroup) fragmentLayout.findViewById(R.id.radio_group);

        new LoadTableData().execute();

        return fragmentLayout;
    }

    public void showDatePicker(){
        DatePickerFragment datePicker = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("date", currentDate);
        args.putInt("year", currentYear);
        args.putInt("month", currentMonth);
        datePicker.setArguments(args);
        datePicker.setTargetFragment(this,1);
        datePicker.show(getFragmentManager().beginTransaction(), null);
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

        float maxVal=0, minVal=0;

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

            DecimalFormat formatter = new DecimalFormat("00");
            String date = currentYear + "-" + formatter.format(currentMonth+1) + "-" + formatter.format(currentDate);
            dailyRecords =  databaseHelper.getDailyRecord(date);
            if(dailyRecords == null) {
                cancel(true);
                lineChart.clear();
                try{
                    View tableView = fragmentLayout.findViewById(R.id.main_table);
                    ViewGroup parent = (ViewGroup) tableView.getParent();
                    parent.removeView(tableView);
                } catch(Exception e){

                }
            }
            else pDialog.show();
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
                        tableCell.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
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
                    tableCell.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
                    tableCell.setPadding(4,4,4,4);
                    tableCell.setTypeface(null, Typeface.BOLD_ITALIC);
                    tableCell.setText(String.format(getResources().getString(R.string.date),mDate, mMonth, mYear));

                    tableRow.addView(tableCell, index);
                    tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    backgroundProcessStatus = UI_THREAD_READY;
                    break;

                case TABLE_DATA_ROW:
                    int row_index = 1;
                    int lineChartIndex = 0;
                    values = new ArrayList<>();
                    labels = new ArrayList<>();
                    for (DataRecord dailyRecord : dailyRecords){
                        index = 0;
                        String[] cellTexts = {
                                dailyRecord.getTime(),
                                Double.toString(dailyRecord.getVoltage()),
                                Double.toString(dailyRecord.getCurrent()),
                                Double.toString(dailyRecord.getTemperature()),
                                Double.toString(dailyRecord.getResistance())
                        };
                        float tempVal = 0f;
                        if(chartState == CHART_STATE_VOLTAGE){
                            tempVal = dailyRecord.getVoltage().floatValue();
                        } else if (chartState == CHART_STATE_CURRENT){
                            tempVal = dailyRecord.getCurrent().floatValue();
                        } else if (chartState == CHART_STATE_TEMPERATURE){
                            tempVal = dailyRecord.getTemperature().floatValue();
                        } else if (chartState == CHART_STATE_RESISTANCE){
                            tempVal = dailyRecord.getResistance().floatValue();
                        }
                        if(tempVal > maxVal) maxVal = dailyRecord.getVoltage().floatValue();
                        if(tempVal < minVal) minVal = dailyRecord.getVoltage().floatValue();
                        values.add(new Entry(tempVal,lineChartIndex++));
                        labels.add(dailyRecord.getTime());
                        tableRow = new TableRow(getActivity());
                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        for (String cellText : cellTexts) {
                            tableCell = new TextView(getActivity());
                            if (row_index%2 == 1) tableCell.setBackgroundResource(R.drawable.table_data_odd);
                            else tableCell.setBackgroundResource(R.drawable.table_data_even);
                            tableCell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                            tableCell.setGravity(Gravity.CENTER);
                            tableCell.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
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
            if(isCancelled()) return null;
            dateLists = databaseHelper.listDates();
            if (dateLists!=null){
                backgroundProcessStatus = WAITING_UI_THREAD;
                publishProgress();
                while(backgroundProcessStatus == WAITING_UI_THREAD){
                    if(isCancelled()) return null;
                }

                //DecimalFormat formatter = new DecimalFormat("00");
                //String date = currentYear + "-" + formatter.format(currentMonth+1) + "-" + formatter.format(currentDate);
                //dailyRecords =  databaseHelper.getDailyRecord(date);
                /*
                tableRowPosition = TABLE_DATE_ROW;
                backgroundProcessStatus = WAITING_UI_THREAD;
                publishProgress(date);
                while(backgroundProcessStatus == WAITING_UI_THREAD){
                    if(isCancelled()) return null;
                }
                */
                //dailyRecords =  databaseHelper.getDailyRecord(date);
                //if(dailyRecords == null) return null;
                tableRowPosition = TABLE_DATA_ROW;
                backgroundProcessStatus = WAITING_UI_THREAD;
                publishProgress(dailyRecords);
                while(backgroundProcessStatus == WAITING_UI_THREAD){
                    if(isCancelled()) return null;
                }

            } else {
                return null;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Object arg){
            if(arg != null){
                try{
                    View tableView = fragmentLayout.findViewById(R.id.main_table);
                    ViewGroup parent = (ViewGroup) tableView.getParent();
                    parent.removeView(tableView);
                } catch(Exception e){

                }finally {
                    if(showTable){
                        tableLayout.setId(R.id.main_table);
                        parentViewGroup.addView(tableLayout, tableViewIndex);
                        tableLayout.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        radioGroup.setVisibility(View.GONE);
                    } else {
                        if(chartState == CHART_STATE_VOLTAGE){
                            lineDataSet = new LineDataSet(values, "Tegangan (V)");
                        } else if (chartState == CHART_STATE_CURRENT){
                            lineDataSet = new LineDataSet(values, "Arus (mA)");
                        } else if (chartState == CHART_STATE_TEMPERATURE){
                            lineDataSet = new LineDataSet(values, "Suhu (C)");
                        } else if (chartState == CHART_STATE_RESISTANCE){
                            lineDataSet = new LineDataSet(values, "Tahanan (Ohm)");
                        }
                        //lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                        lineData = new LineData(labels, lineDataSet);

                        lineChart.setData(lineData);
                        lineChart.setDescription("Terhadap waktu");
                        lineChart.setVisibleXRange(minVal, maxVal);
                        tableLayout.setVisibility(View.GONE);
                        lineChart.setVisibility(View.VISIBLE);
                        radioGroup.setVisibility(View.VISIBLE);
                    }

                    noDataIndicator.setVisibility(View.GONE);
                }
            }
            pDialog.dismiss();

            lineChart.invalidate();
        }
    }

    public void onRadioButtonClicked(View v){
        Boolean checked = ((RadioButton) v).isChecked();

        switch (v.getId()){
            case R.id.radioTegangan:
                if(checked){
                    chartState = CHART_STATE_VOLTAGE;
                }
                break;

            case R.id.radioArus:
                if(checked){
                    chartState = CHART_STATE_CURRENT;
                }
                break;

            case R.id.radioSuhu:
                if(checked){
                    chartState = CHART_STATE_TEMPERATURE;
                }
                break;

            case R.id.radioTahanan:
                if(checked){
                    chartState = CHART_STATE_RESISTANCE;
                }
                break;
        }
        new LoadTableData().execute();
    }
}
