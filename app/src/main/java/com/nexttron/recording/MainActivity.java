package com.nexttron.recording;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import static android.media.AudioRecord.READ_NON_BLOCKING;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;


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
                int channelConfig = AudioFormat.CHANNEL_IN_DEFAULT;
                int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
                int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);

                recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
                recordThread = new RecordThread(dataChart, recorder);
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
        dataChart.setDrawGridBackground(false);
        dataChart.getDescription().setEnabled(false);
        dataChart.setNoDataText("No chart data available. Use the menu to add entries and data sets!");


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
                int channelConfig = AudioFormat.CHANNEL_IN_DEFAULT;
                int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
                int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
                recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
                recordThread = new RecordThread(dataChart, recorder);
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
                    recordThread.start();
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            }
//            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
//                recordThread.start();
        });

        stopButton.setOnClickListener(v -> {
            recordThread.interrupt();
        });
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    class RecordThread extends Thread {
        public final static String RECORD_THREAD_TAG = "recordThread";
        LineChart dataChart;
        AudioRecord record;
        float[] data;
        int recivedDataNumber;

        RecordThread(LineChart dataChart, AudioRecord record) {
            this.dataChart = dataChart;
            this.record = record;
            data = new float[this.record.getBufferSizeInFrames()];
        }

        public void run() {
            int j=0;
            record.startRecording();
            while (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
//            Log.d(RECORD_THREAD_TAG, String.valueOf((record.getRecordingState()== AudioRecord.RECORDSTATE_RECORDING)));
                recivedDataNumber = record.read(data, 0, data.length, READ_NON_BLOCKING);
                for (int i = 0; i < recivedDataNumber; i++) {
                    j++;
                    Log.d(RECORD_THREAD_TAG, data[i] + ":"+j);
                }

            }
        }

        private LineDataSet createSet() {

            LineDataSet set = new LineDataSet(null, "DataSet 1");
            set.setLineWidth(2.5f);
            set.setCircleRadius(4.5f);
            set.setColor(Color.rgb(240, 99, 99));
            set.setCircleColor(Color.rgb(240, 99, 99));
            set.setHighLightColor(Color.rgb(190, 190, 190));
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setValueTextSize(10f);

            return set;
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
}