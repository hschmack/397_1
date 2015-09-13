package com.example.haotian.haotianalp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ALPActivity extends Activity implements SensorEventListener{
    protected LockPatternView mPatternView;
    protected PatternGenerator mGenerator;
    protected Button mGenerateButton;
    protected Button mDesigner;
    protected ToggleButton mPracticeToggle;
    private List<Point> mEasterEggPattern;
    protected SharedPreferences mPreferences;
    protected int mGridLength=0;
    protected int mPatternMin=0;
    protected int mPatternMax=0;
    protected String mHighlightMode;
    protected boolean mTactileFeedback;

    private static final String TAG = "SensorActivity";
    private static final String TAGmotion = "motionEvent";
    private SensorManager mSensorManager = null;


    public List<Sensor> deviceSensors;
    private  Sensor mAccelerometer, mMagnetometer, mGyroscope, mRotation, mGravity, myLinearAcc;

    private File file;
    public static String[] mLine;
    public BufferedWriter bufferedWriter;
    private VelocityTracker mVelocityTracker = null;
    private int control = 0;
    DateFormat mDateFormat;
    String mTimestamp;
    private int counter=0;
    private String myStr = "";
    private float[] touchData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mGenerator = new PatternGenerator();

        setContentView(R.layout.activity_alp);
        mPatternView = (LockPatternView) findViewById(R.id.pattern_view);
        mGenerateButton = (Button) findViewById(R.id.generate_button);
        touchData = new float[6]; //posX posY velX velY pressure size

        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //
                mPatternView.setPattern(mGenerator.getPattern());
                mPatternView.invalidate();
            }
        });

        mPracticeToggle = (ToggleButton) findViewById(R.id.practice_toggle);


        mPracticeToggle.setOnCheckedChangeListener(
                new ToggleButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        mPatternView.setPracticeMode(isChecked);
                        mGenerateButton.setEnabled(!isChecked);
                        mPatternView.invalidate();

                    }
                });

        //initialize the bufferedWriter
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File myFile = new File(dcim, "touchdata.csv");
        if (myFile.exists()){
            myFile.delete(); //delete the touch data file if it already exists
        }
        //then create a new one
        file = new File(dcim, "touchdata.csv"); //fileName
        try {
            bufferedWriter = new BufferedWriter( new FileWriter(file) );
            String[] headings = {"position_X", "position_Y", "velocity_X", "velocity_Y", "pressure", "size", "timestamp",
                    "TYPE_ACCELEROMETER_X", "TYPE_ACCELEROMETERY", "TYPE_ACCELEROMETER_Z",
                    "TYPE_MAGNETIC_FIELD_X", "TYPE_MAGNETIC_FIELD_Y", "TYPE_MAGNETIC_FIELD_Z", "TYPE_GRYOSCOPE_X",
                    "TYPE_GRYOSCOPE_Y", "TYPE_GRYOSCOPE_Z", "TYPE_ROTATION_VECTOR_X", "TYPE_ROTATION_VECTOR_Y",
                    "TYPE_ROTATION_VECTOR_Z", "TYPE_LINEAR_ACCELERATION_X", "TYPE_LINEAR_ACCELERATION_Y",
                    "TYPE_LINEAR_ACCELERATION_Z","TYPE_GRAVITY_X", "TYPE_GRAVITY_Y", "TYPE_GRAVITY_Z"
            };

            //initialize the first row of the csv, this row only contains column headings
            for (String heading : headings) {
                StringBuilder sb = new StringBuilder();
                sb.append(heading);
                sb.append(',');
                sb.append("\n");
                bufferedWriter.write(sb.toString());
                bufferedWriter.flush();
            }
        } catch (java.io.IOException e){
            Log.d("IO", "CANT CREATE BUFFERED OR FILE READER");
        }

        //initialise Sensors
        mSensorManager = (SensorManager) getSystemService((Context.SENSOR_SERVICE));

        mGravity       = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY            );
        mGyroscope     = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE          );
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER      );
        mMagnetometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD     );
        mRotation      = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR    );
        myLinearAcc    = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

    }

    public boolean onTouchEvent(MotionEvent event){

        int action = event.getActionMasked();
        VelocityTracker velocity = VelocityTracker.obtain();

        switch(action) {
            case (MotionEvent.ACTION_DOWN):
               // Log.d("DEBUG_TAG", "Action was DOWN " + String.valueOf(event.getX()) + ", " + String.valueOf(event.getY()));
                event.getX();
                event.getY();
                event.getPressure();
                setTouchData(event.getX(), event.getY(), 0, 0, event.getPressure(), event.getSize()); // is there no movement on Action down?
                // WRITE TO CSV
                writeTouchData();
                return true;
            case (MotionEvent.ACTION_MOVE) :
              //  Log.d("DEBUG_TAG","Action was MOVE");
                velocity.addMovement(event);
                velocity.computeCurrentVelocity(1000);
                float xVelocity = velocity.getXVelocity();
                float yVelocity = velocity.getYVelocity();
                setTouchData(event.getX(), event.getY(), xVelocity, yVelocity, event.getPressure(), event.getSize());
                //WRITE TO CSV
                writeTouchData();
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }

    @Override
    public final void onSensorChanged (SensorEvent event){
        //array order is: Timestamp, AccelX, AccelY, AccelZ, MagnetX, MagnetY, MagnetZ,
        // GyroscopeX, GyroscopeY, GyroscopeZ, RotationVecX, RoationVecY, RotationVecZ
        // LinAccX, LinAccY, LinAccZ, GravX, GravY, gravZ
        touchData[6] = event.timestamp;

        switch(event.sensor.getType()){ //TIMESTAMP IS ALWAYS 6
            case(Sensor.TYPE_ACCELEROMETER): //indices 7,8,9
                touchData[7] = event.values[0];
                touchData[8] = event.values[1];
                touchData[9] = event.values[2];
            case(Sensor.TYPE_MAGNETIC_FIELD): //indices 10,11,12
                touchData[10] = event.values[0];
                touchData[11] = event.values[1];
                touchData[12] = event.values[2];
            case(Sensor.TYPE_GYROSCOPE): //indices 13,14,15
                touchData[13] = event.values[0];
                touchData[14] = event.values[1];
                touchData[15] = event.values[2];
            case(Sensor.TYPE_ROTATION_VECTOR): //indices 16,17,18
                touchData[16] = event.values[0];
                touchData[17] = event.values[1];
                touchData[18] = event.values[2];
            case(Sensor.TYPE_LINEAR_ACCELERATION): //indices 19,20,21
                touchData[19] = event.values[0];
                touchData[20] = event.values[1];
                touchData[21] = event.values[2];
            case(Sensor.TYPE_GRAVITY): //indices 22,23,24
                touchData[22] = event.values[0];
                touchData[23] = event.values[1];
                touchData[24] = event.values[2];
            default:
                //no relevant sensor data
        }
        //Do we write to the bufferedWriter here? Only one sensor is being recorded at a time here?
        //maybe write to the bufferedWriter when every element of the array is nonnull?
        if (readyToWriteSensor()){
            writeTouchData();
            resetTouchData();
        }
    }

    /**
     * Resets touchData array buy reverting everything to 0,0
     */
    public void resetTouchData(){
        for(float f : touchData){
            f = 0;
        }
    }

    /**
     * @return true if all the sensor data elements of touchData are nonnull
     */
    public boolean readyToWriteSensor(){
        for ( int i=6; i< touchData.length-1; i++){
            if(touchData[i] == 0){
                return false; // This can technically be wrong, but i doubt we will ever get 0.0 sensor measurement
            }
        }
        return true;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy){
        // Do something here if sensor accuracy changes;
    }

    public void setTouchData(float x, float y, float velX, float velY, float pressure, float size){
        touchData[0] = x;
        touchData[1] = y;
        touchData[2] = velX;
        touchData[3] = velY;
        touchData[4] = pressure;
        touchData[5] = size;
    }

    public void writeTouchData(){
        StringBuilder sb = new StringBuilder();
        try {
            for (float data : touchData){
                sb.append(data);
                sb.append(',');
            }
            sb.append('\n');
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
            Log.d("IO", "WROTE to the csv file: "+ sb.toString());
        } catch (java.io.IOException e){
            Log.d("IO", "Failed to write to the csv file");
        }
    }

    public void writeTouchData2(){
        String FILENAME = "touch_data.csv";
        String entry = String.valueOf(touchData[0]) + "," +
                String.valueOf(touchData[1]) + "," +
                String.valueOf(touchData[2]) + "," +
                String.valueOf(touchData[3]) + "," +
                String.valueOf(touchData[4]) + "," +
                String.valueOf(touchData[5]) + "\n";
        try{
            FileOutputStream out = openFileOutput(FILENAME, Context.MODE_APPEND);
            out.write(entry.getBytes());
            out.flush();
            Log.d("File Entry", entry);
            out.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();


        updateFromPrefs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_al, menu);
        return true;
    }

    @Override
    protected void onPause() {

        super.onPause();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateFromPrefs()
    {
        int gridLength =
                mPreferences.getInt("grid_length", Defaults.GRID_LENGTH);
        int patternMin =
                mPreferences.getInt("pattern_min", Defaults.PATTERN_MIN);
        int patternMax =
                mPreferences.getInt("pattern_max", Defaults.PATTERN_MAX);
        String highlightMode =
                mPreferences.getString("highlight_mode", Defaults.HIGHLIGHT_MODE);
        boolean tactileFeedback = mPreferences.getBoolean("tactile_feedback",
                Defaults.TACTILE_FEEDBACK);

        // sanity checking
        if(gridLength < 1)
        {
            gridLength = 1;
        }
        if(patternMin < 1)
        {
            patternMin = 1;
        }
        if(patternMax < 1)
        {
            patternMax = 1;
        }
        int nodeCount = (int) Math.pow(gridLength, 2);
        if(patternMin > nodeCount)
        {
            patternMin = nodeCount;
        }
        if(patternMax > nodeCount)
        {
            patternMax = nodeCount;
        }
        if(patternMin > patternMax)
        {
            patternMin = patternMax;
        }

        // only update values that differ
        if(gridLength != mGridLength)
        {
            setGridLength(gridLength);
        }
        if(patternMax != mPatternMax)
        {
            setPatternMax(patternMax);
        }
        if(patternMin != mPatternMin)
        {
            setPatternMin(patternMin);
        }
        if(!highlightMode.equals(mHighlightMode))
        {
            setHighlightMode(highlightMode);
        }
        if(tactileFeedback ^ mTactileFeedback)
        {
            setTactileFeedback(tactileFeedback);
        }
    }

    private void setGridLength(int length)
    {
        mGridLength = length;
        mGenerator.setGridLength(length);
        mPatternView.setGridLength(length);
    }
    private void setPatternMin(int nodes)
    {
        mPatternMin = nodes;
        mGenerator.setMinNodes(nodes);
    }
    private void setPatternMax(int nodes)
    {
        mPatternMax = nodes;
        mGenerator.setMaxNodes(nodes);
    }
    private void setHighlightMode(String mode)
    {
        if("no".equals(mode))
        {
            mPatternView.setHighlightMode(new LockPatternView.NoHighlight());
        }
        else if("first".equals(mode))
        {
            mPatternView.setHighlightMode(new LockPatternView.FirstHighlight());
        }
        else if("rainbow".equals(mode))
        {
            mPatternView.setHighlightMode(
                    new LockPatternView.RainbowHighlight());
        }

        mHighlightMode = mode;
    }
    private void setTactileFeedback(boolean enabled)
    {
        mTactileFeedback = enabled;
        mPatternView.setTactileFeedbackEnabled(enabled);
    }

}
