package com.nexttron.recording;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MainActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
//    private static String fileName = null;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    LineChart dataChart;
    AudioRecord recorder = null;
    RecordThread recordThread;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                int audioSource = MediaRecorder.AudioSource.DEFAULT;
                int samplingRate = 8000;
                int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);

                recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
                EditText editDividers = findViewById(R.id.editDividers);
                double divider = 750;
                if (!editDividers.getText().toString().isEmpty())
                    divider = Double.parseDouble(editDividers.getText().toString());
                recordThread = new RecordThread(recorder,divider);
                new CountDownTimer(15000, 1000) {

                    public void onTick(long millisUntilFinished) {
//                    mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
//                    mTextField.setText("done!");
//                        recorder.stop();
                        recordThread.interrupt();
                    }
                }.start();
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
                    recordThread.start();
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        dataChart = findViewById(R.id.dataChart);
        dataChart.setOnChartValueSelectedListener(this);
        // enable description text
        dataChart.getDescription().setEnabled(true);

        // enable touch gestures
        dataChart.setTouchEnabled(true);

        // enable scaling and dragging
        dataChart.setDragEnabled(true);
        dataChart.setScaleEnabled(false);
        dataChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        dataChart.setPinchZoom(true);

        // set an alternative background color
        dataChart.setBackgroundColor(Color.BLACK);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        dataChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = dataChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
//        l.setTypeface(tfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl = dataChart.getXAxis();
//        xl.setTypeface(tfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = dataChart.getAxisLeft();
//        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.setAxisMaximum(1000);
//        leftAxis.setAxisMinimum(-1000);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = dataChart.getAxisRight();
        rightAxis.setEnabled(false);

//        minBuffSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT);

//        recorder = new AudioRecord.Builder()
//                .setAudioSource(MediaRecorder.AudioSource.DEFAULT)
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
//                        .setSampleRate(8000)
//                        .setChannelMask(AudioFormat.CHANNEL_IN_DEFAULT)
//                        .build())
//                .setBufferSizeInBytes(2 * minBuffSize)
//                .build();


        startButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
                int audioSource = MediaRecorder.AudioSource.MIC;
                int samplingRate = 8000;
                int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
                recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
                EditText editDividers = findViewById(R.id.editDividers);
                double divider = 750;
                if (!editDividers.getText().toString().isEmpty())
                    divider = Double.parseDouble(editDividers.getText().toString());
                recordThread = new RecordThread(recorder, divider);
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    dataChart.getLineData().clearValues();
                    recordThread.start();
                    Log.d(LOG_TAG, "pressed");
                    new CountDownTimer(15000, 1000) {

                        public void onTick(long millisUntilFinished) {
//                    mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
//                    mTextField.setText("done!");
//                            recorder.stop();
                            Log.d(LOG_TAG, "interrupted");
                            recordThread.interrupt();
                        }
                    }.start();
                }

            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            }
//            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
//                recordThread.start();
        });

        stopButton.setOnClickListener(v -> {
            if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                recordThread.interrupt();
        });
    }

    private void addEntry(double value) {

        LineData data = dataChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) value), 0);
//            Log.d("addEntry", set.getEntryCount() + "");
            data.notifyDataChanged();

            // let the chart know it's data has changed
            dataChart.notifyDataSetChanged();

            // limit the number of visible entries
            dataChart.setVisibleXRangeMaximum(70);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            dataChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    class FilterThread extends Thread {
        public final static String FILTER_THREAD_TAG = "FilterThread";
        List<Short> filteredList = new ArrayList<>();
        short sum;
        List<Short> sampleList;

        public FilterThread(List<Short> sampleList) {
            sum = 0;
            this.sampleList = sampleList;
        }

        @Override
        public void run() {
            super.run();
            short filterSample = (short) 0;
            for (int i = 0; i < sampleList.size(); i++) {
                filterSample += (short) (Constants.filterArray[i] / 500 * sampleList.get(i));
                Log.d(FILTER_THREAD_TAG, filterSample + "");

//                if (i % 480 == 0) {
//
//                    sum = 0f;
//                }

            }
//            filteredList.add(filterSample);
            addEntry(filterSample);
//            if (filteredList.size() == 480) {
//                sum = 0;
//                for (int i = 0; i < filteredList.size(); i++)
//                    sum += filterSample;
//                addEntry(sum);
//                filteredList = new LinkedList<>();
//            }
        }
    }

    class RecordThread extends Thread {
        public final static String RECORD_THREAD_TAG = "recordThread";
        AudioRecord record;
        short[] data;
        int recivedDataNumber;
        List<Double> sampleList = new ArrayList<>();
        List<Double> xcorrList = new ArrayList<>();
        TypedArray orgSignal;
        List<Double> orgSignalList;
        private final double divider;
//        FIR filter;


        RecordThread(AudioRecord record, double divider) {
            this.record = record;
            data = new short[this.record.getBufferSizeInFrames()];
//            filter= new FIR(Constants.filterArray);
            Resources res = getResources();
            orgSignal = res.obtainTypedArray(R.array.orgSignal);
            orgSignalList = new ArrayList<Double>();
            for (int i = 0; i < orgSignal.length(); i++) {
                orgSignalList.add((double) orgSignal.getFloat(i, 0));
            }
            this.divider=divider;
        }

        public void run() {

            record.startRecording();


            while (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                recivedDataNumber = record.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
//                Log.d(RECORD_THREAD_TAG, data.getClass() + "");
                for (int i = 0; i < data.length; i++) {
                    sampleList.add(data[i] / (pow(2, 2 * 8)));
//                    Log.d(LOG_TAG,data[i]/(pow(2,2*8))+"");
                }
//                short filterSample = (short) 0;
//                short mean =0;
//                for (int i = 0; i < recivedDataNumber; i++) {
//                    filterSample =filter.getOutputSample((short) (data[i]*10));
//                    Log.d(RECORD_THREAD_TAG, filterSample + "");
//                    mean+=filterSample;
//                    if (i % 500 == 0) {
//                        addEntry(mean/500);
//                        mean=0;
//                    }
//                    sum+=data[i];
//                    sampleList.add(data[i]);
//                    if (sampleList.size() == Constants.filterArray.length)
//                        sampleList.remove(0);
//                    short filterSample = (short) 0;
//                    for (int j = 0; j < sampleList.size(); j++) {
//                        filterSample += (short) (Constants.filterArray[j] * sampleList.get(j));
//                    }
////                    Log.d(RECORD_THREAD_TAG, filterSample + "");
//                    if (i % 500 == 0)
//                        addEntry(filterSample);

            }

//                new FilterThread(sampleList).start();
//                short filterSample = (short) 0;
//                for (int i = 0; i < sampleList.size(); i++) {
//                    filterSample += (short) (Constants.filterArray[i]/1000 * sampleList.get(i));
//                    Log.d(RECORD_THREAD_TAG, filterSample + "");
//                }
//                addEntry(filterSample);

//                addEntry(sum);
//                int i =0;
//                while (data[i]>0){
//                    addEntry(data[i]);
//                    i++;
//                }
        }


        @Override
        public void interrupt() {
            if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop();
                record.release();
                double corr = 0;
                double energySample = 0;
                double energyOrgSignal = 0;
                double meanSample = 0;
                double meanOrgSignal = 0;
                for (int i = 0; i < sampleList.size(); i += (orgSignalList.size() - 1) / divider) {
                    if (i + orgSignalList.size() <= sampleList.size()) {
                        corr = 0;
                        energySample = 0;
                        energyOrgSignal = 0;
                        meanSample = 0;
                        meanOrgSignal = 0;
                        for (int j = 0; j < orgSignalList.size(); j++) {
                            corr += orgSignalList.get(j) * sampleList.get(i + j);
                            energySample += (sampleList.get(i + j) * sampleList.get(i + j));
                            energyOrgSignal += (orgSignalList.get(j) * orgSignalList.get(j));
                            meanSample = sampleList.get(i + j);
                            meanOrgSignal = orgSignalList.get(j);
                        }
                        corr = (corr / (sqrt(energySample * energyOrgSignal))) * 100;
                        energySample = 0;
                        energyOrgSignal = 0;
                        meanSample = meanSample / orgSignalList.size();
                        meanOrgSignal = meanOrgSignal / orgSignalList.size();
//                        Log.d(LOG_TAG+"corr", corr + "");
                        addEntry(corr);
                        xcorrList.add(corr);
//                        if (corr > 12) {
//                            Log.d(LOG_TAG, corr + "");
//                            break;
//                        }
                    } else {
                        corr = 0;
                        energySample = 0;
                        energyOrgSignal = 0;
                        int ii = sampleList.size() - orgSignalList.size() - 1;
                        for (int j = 0; j < orgSignal.length(); j++) {
                            corr += orgSignalList.get(j) * sampleList.get(ii + j);
                            energySample += (sampleList.get(ii + j) * sampleList.get(ii + j));
                            energyOrgSignal += (orgSignalList.get(j) * orgSignalList.get(j));
                        }
                        corr = (corr / (sqrt(energySample * energyOrgSignal))) * 100;
                        energySample = 0;
                        energyOrgSignal = 0;
                        meanSample = meanSample / orgSignalList.size();
                        meanOrgSignal = meanOrgSignal / orgSignalList.size();
                        addEntry(corr);
                        xcorrList.add(corr);
                        Log.d(LOG_TAG, "Broke");
//                        break;
                    }
                }
//                short value = (short) orgSignal.getFloat(0, 0);
//                Log.d(RECORD_THREAD_TAG, String.valueOf((record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)));
//                Log.d(RECORD_THREAD_TAG, orgSignal.length() + "");

                super.interrupt();
            }
        }


    }

//    class FIR {
//        private int length;
//        private short[] delayLine;
//        private short[] impulseResponse;
//        private int count = 0;
//
//        FIR(short[] coefs) {
//            length = coefs.length;
//            impulseResponse = coefs;
//            delayLine = new short[length];
//        }
//
//        short getOutputSample(short inputSample) {
//            delayLine[count] = inputSample;
//            short result = (short) 0.0;
//            int index = count;
//            for (int i = 0; i < length; i++) {
//                result += impulseResponse[i] * delayLine[index--];
//                if (index < 0) index = length - 1;
//            }
//            if (++count >= length) count = 0;
//            return result;
//        }
//    }
}

