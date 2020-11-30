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
import java.util.LinkedList;
import java.util.List;

import static android.media.AudioRecord.READ_NON_BLOCKING;

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
                recordThread = new RecordThread(recorder);
                new CountDownTimer(15000, 1000) {

                    public void onTick(long millisUntilFinished) {
//                    mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
//                    mTextField.setText("done!");
                        recorder.stop();
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
                int audioSource = MediaRecorder.AudioSource.DEFAULT;
                int samplingRate = 8000;
                int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
                recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
                recordThread = new RecordThread(recorder);
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    recordThread.start();
                    new CountDownTimer(15000, 1000) {

                        public void onTick(long millisUntilFinished) {
//                    mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
//                    mTextField.setText("done!");
                            recorder.stop();
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

    private void addEntry(float value) {

        LineData data = dataChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), value), 0);
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
        List<Short> sampleList = new ArrayList<>();
        FIR filter;


        RecordThread(AudioRecord record) {
            this.record = record;
            data = new short[this.record.getBufferSizeInFrames()];
//            filter= new FIR(Constants.filterArray);
            Resources res = getResources();
            TypedArray orgSignal = res.obtainTypedArray(R.array.orgSignal);

        }

        public void run() {

            record.startRecording();


            while (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                recivedDataNumber = record.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
                Log.d(RECORD_THREAD_TAG, data.getClass() + "");
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
                Log.d(RECORD_THREAD_TAG, String.valueOf((record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)));
                super.interrupt();
            }
        }


    }

    class FIR {
        private int length;
        private short[] delayLine;
        private short[] impulseResponse;
        private int count = 0;

        FIR(short[] coefs) {
            length = coefs.length;
            impulseResponse = coefs;
            delayLine = new short[length];
        }

        short getOutputSample(short inputSample) {
            delayLine[count] = inputSample;
            short result = (short) 0.0;
            int index = count;
            for (int i = 0; i < length; i++) {
                result += impulseResponse[i] * delayLine[index--];
                if (index < 0) index = length - 1;
            }
            if (++count >= length) count = 0;
            return result;
        }
    }
}

