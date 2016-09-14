package com.swdmnd.avometer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ar.com.daidalos.afiledialog.FileChooserDialog;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PrintFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PrintFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrintFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    int statusBarHeight;
    private ProgressDialog pDialog;
    String statusMsg;
    int resultStatus;
    String targetFolder;

    TextView targetPath;

    private OnFragmentInteractionListener mListener;

    public PrintFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PrintFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PrintFragment newInstance(String param1, String param2) {
        PrintFragment fragment = new PrintFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            statusBarHeight = getArguments().getInt("STATUS_BAR_HEIGHT");
        }
        targetFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_print, container, false);;

        view.findViewById(R.id.root_view).setPadding(0, statusBarHeight, 0, 0);

        ((Button) view.findViewById(R.id.btn_cetak)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPdf();
            }
        });

        ((Button) view.findViewById(R.id.btn_browse)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browseFolder();
            }
        });

        targetPath = (TextView) view.findViewById(R.id.target_path);
        targetPath.setText(targetFolder);

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

    private void createPdf(){
        new PrintReport().execute();
    }

    private void browseFolder(){
        FileChooserDialog dialog = new FileChooserDialog(getActivity());
        dialog.setFolderMode(true);
        dialog.loadFolder(targetFolder);
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener(){
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                targetFolder = file.getAbsolutePath();
                targetPath.setText(targetFolder);
            }
            public void onFileSelected(Dialog source, File folder, String name) {
                source.hide();
            }
        });
        dialog.show();
    }

    private void showResultToast(int status, String msg){
        switch(status){
            case Constants.STATUS_FAILED:
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                break;

            case Constants.STATUS_SUCCESS:
                Toast.makeText(getActivity(), "File pdf telah dicetak.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class PrintReport extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Sedang mencetak laporan ke berkas pdf...");
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            Date date = new Date() ;
            String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH:mm").format(date);

            File myFile = new File(targetFolder + "/Pengukuran_" + timeStamp + ".pdf");

            try{
                OutputStream output = new FileOutputStream(myFile);
                Document document = new Document(PageSize.A4, 20, 20, 20, 20);

                try{
                    PdfWriter.getInstance(document, output);
                    document.open();

                    Paragraph title = new Paragraph("Laporan Pengukuran SOFC", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
                    title.setAlignment(Element.ALIGN_CENTER);
                    document.add(title);

                    title = new Paragraph("Dibuat pada " + DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG, Constants.APP_LOCALE).format(new Date()), new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.add(new Paragraph(" "));
                    title.add(new Paragraph(" "));
                    document.add(title);

                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

                    java.util.List<String> dateLists = new ArrayList<>();
                    java.util.List<DataRecord> dailyRecords = new ArrayList<DataRecord>();
                    dateLists = databaseHelper.listDates();
                    for (String logDate : dateLists) {
                        String mYear = logDate.substring(0, 4);
                        String mMonth = new DateFormatSymbols(new Locale("id")).getMonths()[Integer.parseInt(logDate.substring(5, 7))-1];
                        String mDate = logDate.substring(8);
                        Paragraph p = new Paragraph("Tanggal : " + String.format(getResources().getString(R.string.date),mDate, mMonth, mYear), new Font(Font.FontFamily.HELVETICA, 12, Font.BOLDITALIC));
                        p.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 4)));
                        document.add(p);

                        dailyRecords = databaseHelper.getDailyRecord(logDate);

                        String[] tableDatas = new String[5];

                        PdfPTable table = new PdfPTable(5);
                        table.setTotalWidth(PageSize.A4.getWidth() - 40);
                        table.setLockedWidth(true);


                        Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
                        Font timeStampFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.ITALIC);
                        Font dataFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);

                        PdfPCell cell;
                        tableDatas[0] = "Waktu";
                        tableDatas[1] = "Tegangan (V)";
                        tableDatas[2] = "Arus (mA)";
                        tableDatas[3] = "Suhu (C)";
                        tableDatas[4] = "Resistansi";

                        for (int i = 0; i < 5; ++i) {
                            cell = new PdfPCell(new Phrase(tableDatas[i], headerFont));
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            table.addCell(cell);
                        }

                        table.setHeaderRows(1);

                        for (DataRecord dailyRecord : dailyRecords) {
                            tableDatas[0] = dailyRecord.getTime();
                            tableDatas[1] = Double.toString(dailyRecord.getVoltage());
                            tableDatas[2] = Double.toString(dailyRecord.getCurrent());
                            tableDatas[3] = Double.toString(dailyRecord.getTemperature());
                            tableDatas[4] = Double.toString(dailyRecord.getResistance());

                            for (int i = 0; i < 5; ++i) {
                                if (i == 0) {
                                    cell = new PdfPCell(new Phrase(tableDatas[i], timeStampFont));
                                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                } else {
                                    cell = new PdfPCell(new Phrase(tableDatas[i], dataFont));
                                }
                                table.addCell(cell);
                            }
                        }
                        document.add(table);
                    }
                    document.close();
                } catch (DocumentException e){
                    statusMsg = e.toString();
                    resultStatus = Constants.STATUS_FAILED;
                }

            } catch (FileNotFoundException e){
                statusMsg = e.toString();
                resultStatus = Constants.STATUS_FAILED;
            }
            statusMsg = null;
            resultStatus = Constants.STATUS_SUCCESS;

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            showResultToast(resultStatus, statusMsg);
        }
    }
}
