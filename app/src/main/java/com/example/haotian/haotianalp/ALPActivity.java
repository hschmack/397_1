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
import java.io.BufferedReader;
import java.io.FileReader;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class ALPActivity extends Activity implements SensorEventListener{
    protected LockPatternView mPatternView;
    protected PatternGenerator mGenerator;
    protected Button mGenerateButton;
    protected Button mProcessButton;
    protected Button mDesigner;
    protected ToggleButton mPracticeToggle;
    protected ToggleButton mUserToggle;
    private List<Point> mEasterEggPattern;
    protected SharedPreferences mPreferences;
    protected int mGridLength=0;
    protected int mPatternMin=0;
    protected int mPatternMax=0;
    protected String mHighlightMode;
    protected boolean mTactileFeedback;
    private final String[] numArr = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty", "twentyone", "twentytwo", "twentythree", "twentyfour", "twentyfive", "twentysix", "twentyseven", "twentyeight", "twentynine", "thirty", "thirtyone", "thirtytwo", "thirtythree", "thirtyfour", "thirtyfive", "thirtysix", "thirtyseven", "thirtyeight", "thirtynine", "fourty", "fourtyone", "fourtytwo", "fourtythree", "fourtyfour", "fourtyfive", "fourtysix", "fourtyseven", "fourtyeight", "fourtynine", "fifty", "fiftyone", "fiftytwo", "fiftythree", "fiftyfour", "fiftyfive", "fiftysix", "fiftyseven", "fiftyeight", "fiftynine", "sixty", "sixtyone", "sixtytwo", "sixtythree", "sixtyfour", "sixtyfive", "sixtysix", "sixtyseven", "sixtyeight", "sixtynine", "seventy", "seventyone", "seventytwo", "seventythree", "seventyfour", "seventyfive", "seventysix", "seventyseven" ,"seventyeight", "seventynine", "eighty"};

    private static final String TAG = "SensorActivity";
    private static final String TAGmotion = "motionEvent";
    private SensorManager mSensorManager = null;


    public List<Sensor> deviceSensors;
    private  Sensor mAccelerometer, mMagnetometer, mGyroscope, mRotation, mGravity, myLinearAcc;

    private File file;
    public static String[] mLine;
    public BufferedWriter bufferedWriter;
    public BufferedWriter processerWriter;
    public BufferedReader bufferedReader;
    public StringBuilder tempTouchData;
    private VelocityTracker mVelocityTracker = null;
    private int control = 0;
    DateFormat mDateFormat;
    String mTimestamp;
    private int counter=0;
    private String myStr = "";
    private float[] touchData;
    private String userLabel = "Hayden";
    boolean processHeadersWritten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        processHeadersWritten = false;
        mGenerator = new PatternGenerator();

        setContentView(R.layout.activity_alp);
        mPatternView = (LockPatternView) findViewById(R.id.pattern_view);
        mGenerateButton = (Button) findViewById(R.id.generate_button);
        mProcessButton = (Button) findViewById(R.id.process_button);
        touchData = new float[24]; //posX posY velX velY pressure size + all sensor data

        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //
                mPatternView.setPattern(mGenerator.getPattern());
                mPatternView.invalidate();
                //counter++;
                //counter = 0; //reset the attempt at unlocking
            }
        });

        mPracticeToggle = (ToggleButton) findViewById(R.id.practice_toggle);
        mUserToggle = (ToggleButton) findViewById(R.id.user_toggle);


        mPracticeToggle.setOnCheckedChangeListener(
                new ToggleButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        mPatternView.setPracticeMode(isChecked);
                        mGenerateButton.setEnabled(!isChecked);
                        mPatternView.invalidate();

                    }
                });

        mUserToggle.setOnCheckedChangeListener(
                new ToggleButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        userLabel = (!isChecked) ? "Hayden" : "Erwin";
                    }
                });

        //We will use this StringBuilder to temporarily store our touchData
        tempTouchData = new StringBuilder();

        //initialize the bufferedWriter
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File myFile = new File(dcim, "touchdata.csv");
        if (myFile.exists()){
            myFile.delete(); //delete the touch data file if it already exists
        }
        //then create a new one
        file = new File(dcim, "touchdata.csv");
        //set Process Button, declare reader.
        mProcessButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    bufferedReader = new BufferedReader(new FileReader(file));
                    processData();
                } catch (java.io.FileNotFoundException e){
                    Log.d("ERROR", "failed to create bufferedReader");
                    e.printStackTrace();
                }
            }
        });
        try {
            bufferedWriter = new BufferedWriter( new FileWriter(file) );
            String[] headings = { "position_X", "position_Y",
                    "velocity_X", "velocity_Y",
                    "pressure", "size",
                    "TYPE_ACCELEROMETER_X", "TYPE_ACCELEROMETERY", "TYPE_ACCELEROMETER_Z",
                    "TYPE_MAGNETIC_FIELD_X", "TYPE_MAGNETIC_FIELD_Y", "TYPE_MAGNETIC_FIELD_Z",
                    "TYPE_GRYOSCOPE_X", "TYPE_GRYOSCOPE_Y", "TYPE_GRYOSCOPE_Z",
                    "TYPE_ROTATION_VECTOR_X", "TYPE_ROTATION_VECTOR_Y", "TYPE_ROTATION_VECTOR_Z",
                    "TYPE_LINEAR_ACCELERATION_X", "TYPE_LINEAR_ACCELERATION_Y","TYPE_LINEAR_ACCELERATION_Z",
                    "TYPE_GRAVITY_X", "TYPE_GRAVITY_Y", "TYPE_GRAVITY_Z",
                    "mCurrentPattern", "Label","Counter"};

            //initialize the first row of the csv, this row only contains column headings
            StringBuilder sb = new StringBuilder();
            for (String heading : headings) {
                sb.append(heading);
                sb.append(',');
            }
            sb.append("\n");
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();

            File pFile = new File(dcim, "processed_data.csv");
            if (pFile.exists()){
                pFile.delete(); //delete the touch data file if it already exists
            }
            //then create a new one
            File newPFile = new File(dcim, "processed_data.csv"); //fileName

            processerWriter = new BufferedWriter(new FileWriter(newPFile));
            String[] pheadings = {
                    "velocity_X_mean", "velocity_X_std",
                    "velocity_Y_mean", "velocity_Y_std",
                    "pressure_mean", "pressure_std",
                    "size_mean", "size_std",
                    "TYPE_ACCELEROMETER_X_mean", "TYPE_ACCELEROMETER_X_std",
                    "TYPE_ACCELEROMETERY_mean", "TYPE_ACCELEROMETERY_std",
                    "TYPE_ACCELEROMETER_Z_mean", "TYPE_ACCELEROMETER_Z_std",
                    "TYPE_MAGNETIC_FIELD_X_mean", "TYPE_MAGNETIC_FIELD_X_std",
                    "TYPE_MAGNETIC_FIELD_Y_mean", "TYPE_MAGNETIC_FIELD_Y_std",
                    "TYPE_MAGNETIC_FIELD_Z_mean", "TYPE_MAGNETIC_FIELD_Z_std",
                    "TYPE_GRYOSCOPE_X_mean", "TYPE_GRYOSCOPE_X_std",
                    "TYPE_GRYOSCOPE_Y_mean", "TYPE_GRYOSCOPE_Y_std",
                    "TYPE_GRYOSCOPE_Z_mean", "TYPE_GRYOSCOPE_Z_std",
                    "TYPE_ROTATION_VECTOR_X_mean", "TYPE_ROTATION_VECTOR_X_std",
                    "TYPE_ROTATION_VECTOR_Y_mean", "TYPE_ROTATION_VECTOR_Y_std",
                    "TYPE_ROTATION_VECTOR_Z_mean", "TYPE_ROTATION_VECTOR_Z_std",
                    "TYPE_LINEAR_ACCELERATION_X_mean", "TYPE_LINEAR_ACCELERATION_X_std",
                    "TYPE_LINEAR_ACCELERATION_Y_mean", "TYPE_LINEAR_ACCELERATION_Y_std",
                    "TYPE_LINEAR_ACCELERATION_Z_mean", "TYPE_LINEAR_ACCELERATION_Z_std",
                    "TYPE_GRAVITY_X_mean", "TYPE_GRAVITY_X_std",
                    "TYPE_GRAVITY_Y_mean", "TYPE_GRAVITY_Y_std",
                    "TYPE_GRAVITY_Z_mean", "TYPE_GRAVITY_Z_std",
                    "mCurrentPattern",
                    "Label",
                    "Counter"};

                //initialize the first row of the csv, this row only contains column headings
                StringBuilder psb = new StringBuilder();

                for (String pheading : pheadings) {
                    psb.append(pheading);
                    psb.append(',');
                }
                psb.append("\n");
                processerWriter.write(psb.toString());
                processerWriter.flush();

        } catch (java.io.IOException e){
            Log.d("IO", "CANT CREATE BUFFERED OR FILE READER");
        }

        //initialise Sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGravity       = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY            );
        mGyroscope     = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE          );
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER      );
        mMagnetometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD     );
        mRotation      = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR    );
        myLinearAcc    = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL      );
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL     );
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL    );
        mSensorManager.registerListener(this, myLinearAcc, SensorManager.SENSOR_DELAY_NORMAL   );
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mPatternView.setTouchDataListener(new LockPatternView.TouchDataListener() {
            @Override
            public void onGridTouch(MotionEvent event) {
                logTouchEvent((event));
            }
        });
    }


    public void logTouchEvent(MotionEvent event){
        int action = event.getActionMasked();
        VelocityTracker velocity = VelocityTracker.obtain();

        switch(action) {
            case (MotionEvent.ACTION_DOWN):
               // Log.d("DEBUG_TAG", "ACTION DOWN");
                setTouchData(event.getX(), event.getY(), 0, 0, event.getPressure(), event.getSize()); // is there no movement on Action down?
                // WRITE TO CSV
                writeTouchData();
                break;
            case (MotionEvent.ACTION_MOVE) :
                //Log.d("DEBUG_TAG","Action was MOVE");
                velocity.addMovement(event);
                velocity.computeCurrentVelocity(1000);
                float xVelocity = velocity.getXVelocity();
                float yVelocity = velocity.getYVelocity();
                setTouchData(event.getX(), event.getY(), xVelocity, yVelocity, event.getPressure(), event.getSize());
                //WRITE TO CSV
                writeTouchData();
                break;
            case (MotionEvent.ACTION_UP):
                //The motion is finished, write the data if the pattern is correct
                writeToFile();
                break;
            default :
                //
        }
    }

    @Override
    public final void onSensorChanged (SensorEvent event){
        switch(event.sensor.getType()){
            case(Sensor.TYPE_ACCELEROMETER):
                touchData[6] = event.values[0];
                touchData[7] = event.values[1];
                touchData[8] = event.values[2];
            case(Sensor.TYPE_MAGNETIC_FIELD):
                touchData[9] = event.values[0];
                touchData[10] = event.values[1];
                touchData[11] = event.values[2];
            case(Sensor.TYPE_GYROSCOPE):
                touchData[12] = event.values[0];
                touchData[13] = event.values[1];
                touchData[14] = event.values[2];
            case(Sensor.TYPE_ROTATION_VECTOR):
                touchData[15] = event.values[0];
                touchData[16] = event.values[1];
                touchData[17] = event.values[2];
            case(Sensor.TYPE_LINEAR_ACCELERATION):
                touchData[18] = event.values[0];
                touchData[19] = event.values[1];
                touchData[20] = event.values[2];
            case(Sensor.TYPE_GRAVITY):
                touchData[21] = event.values[0];
                touchData[22] = event.values[1];
                touchData[23] = event.values[2];
            default:
                //no relevant sensor data
        }
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
        for (float data : touchData){
            tempTouchData.append(data);
            tempTouchData.append(',');
        }
        //because we made the array float[] instead of String[] and dont want to change 25 lines of code
        tempTouchData.append(convertListToString(mPatternView.getPattern()) + ',');
        tempTouchData.append(userLabel + ',');
        tempTouchData.append(counter);
        tempTouchData.append('\n');
    }

    /**
     * Write to touchdata.csv IF the pattern entered on patternview is correct
     */
    public void writeToFile(){
        if( mPatternView.testResult.equals("true") ){
            try{
                bufferedWriter.write(tempTouchData.toString());
                bufferedWriter.flush();
                counter++;
                Log.d("NUMBER ATTEMPTS", "Solution " + counter);
                //Log.d("SUCCESS", "WROTE TO THE CSV FILE");
            }catch (java.io.IOException e){
                //
            }
        } else {
           // Log.d("FAILURE", "Invalid Pattern: Not Writing TO File");
        }
        //if the test result is false, just dump the data
        //we also want to dump the data after the bufferedwriter writes
        tempTouchData.setLength(0);
    }

    // there HAS to be a way to do this natively
    public String convertListToString(List<Point> pattern){
        StringBuilder sb = new StringBuilder();
        sb.append("\"[");
        for(Point p : pattern){
            sb.append(p.toString());
            sb.append('-');
        }
        sb.append("]\"");
        return sb.toString();
    }
    public void processData(){
        LinkedList<LinkedList<String[]>> rowsWithLabel = new LinkedList<LinkedList<String[]>>(); //im
        try{
            String line = bufferedReader.readLine(); //always skip the first line, we dont care about the labels
            int countIndex = 26;

            while( (line = bufferedReader.readLine()) != null){
                String[] parsedLine = line.split(",");
                while (rowsWithLabel.size() <= Integer.valueOf(parsedLine[countIndex]) ) { rowsWithLabel.add(new LinkedList<String[]>()) ;}
                rowsWithLabel.get(Integer.valueOf( parsedLine[countIndex] )).add(parsedLine);
            }
        } catch (java.io.IOException e ){
            Log.d("ERROR", "error reading from reader");
            e.printStackTrace();
        }
        for (LinkedList<String[]> listOfListofList: rowsWithLabel){
            calculateMeanThenSTD(listOfListofList);
        }
    }

    public void calculateMeanThenSTD (LinkedList<String[]> section){
        ArrayList<Float> means = new ArrayList<Float>(27);
        ArrayList<Float> stds = new ArrayList<Float>(27);

        String[] singleRow = section.getFirst();
        while(means.size() < singleRow.length) { means.add((float)0); stds.add((float)0);}
        String[] metaData = {singleRow[24], singleRow[25], singleRow[26]};


        for(String[] row : section){
            for(int col = 2; col < 24; col++){
                float updated = means.get(col) + Float.valueOf(row[col]);
                means.set(col, updated);
            }
        }
        for (int i = 2; i < 24; i++){
            float avg = means.get(i) / section.size();
            means.set(i, avg);
        }

        //now set the std deviation
        for(String[] row: section){
            for(int col = 2; col <24; col++){
                double updated = Math.pow( (double) (Float.valueOf(row[col]) - means.get(col)), 2);
                stds.set(col, (float) updated);
            }
        }

        for (int i = 2; i < 24; i++){
            double divNsqrt = (double) stds.get(i) / section.size();
            divNsqrt = Math.sqrt(divNsqrt);
            stds.set(i, (float) divNsqrt);
        }

        //BELOW write to file
        try{

            StringBuilder psb = new StringBuilder();
            for(int i=2; i < means.size()-3; i++){
                psb.append(means.get(i)+",");
                psb.append(stds.get(i)+",");
            }

            psb.append(metaData[0] + ",");
            psb.append(metaData[1] + ",");
            psb.append(metaData[2]);
            psb.append("\n");
            processerWriter.write(psb.toString());
            processerWriter.flush();

        }catch (java.io.IOException e){

        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL      );
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL     );
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL    );
        mSensorManager.registerListener(this, myLinearAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

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
        mSensorManager.unregisterListener(this);
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
