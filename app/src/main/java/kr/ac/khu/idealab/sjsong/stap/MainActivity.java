package kr.ac.khu.idealab.sjsong.stap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.Math.*;

public class MainActivity extends AppCompatActivity {
    int posX = 0;
    int posY = 0;
    int angle = 0;
    int maxClass = -1;
    int D=64; // feature dimension
    int n=5;  //
    int N=n*2; // support vectors
    int nT=5; // number of training data
    int C=12;
    double threshold = 0.3;

    double[][] WC = new double[2][7];
    double[][] WF1 = new double[256][32];
    double[][] WF2 = new double[32][12];
    double[] BF1 = new double[32];
    double[] BF2 = new double[12];
    double[] BN = new double[2];
    double[][] sample1 = new double[2][4096];


    public static final String LOAD_METHOD_ID = "load_method_id";
    public static final int LOAD_METHOD_CODE = 92840;

    Bitmap myImg = null;
    ImageView imageView1;
    EditText editText1;
    ThreadRec thread1 = null;
    Thread thread2 = null;
    Handler handler = new Handler();

    TextView textView1;
    TextView textView2;

    Switch switch1;
    Button button1;

    WebView webView;
    Intent videointent;
    ImageView imageView2;


    boolean training = false;
    boolean iswebopen = false;
    boolean isvideoopen = false;
    boolean ismapopen = false;
    boolean upordown = false;
    boolean istouched = false;

    double[] Y = {1,1,1,1,1,-1,-1,-1,-1,-1};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        editText1 = (EditText) findViewById(R.id.editText1);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);

        button1 = (Button) findViewById(R.id.button1);
        myImg = BitmapFactory.decodeResource(getResources(), R.drawable.arrow1);



        switch1 = (Switch)findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    training = true;
                }
                else
                    training = false;
            }
        });

    }

    public void onClickButton1(View v){
        imageView1.setImageResource(R.drawable.arrow1);

        thread1 = new ThreadRec();
        thread2 = new Thread(thread1);
        thread2.setPriority(Thread.MAX_PRIORITY);
        thread2.start();
        maxClass = 0;
        arrow_Position(0);
    }



    public void onClickButton3(View v){
        if(thread2 != null){
            thread2.interrupt();
        }
        maxClass = 0;
        arrow_Position(1000);
    }

    public void onClickButton4(View v){
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("http://spectrum.ieee.org");
        iswebopen = true;
    }

    public void onClickButton5(View v){

        videointent = new Intent(getApplicationContext(), SecondActivity.class);
        startActivity(videointent);
        isvideoopen = true;
    }

    public void onClickButton9(View v){
        imageView2 = (ImageView) findViewById(R.id.imageView2);

        imageView2.setImageResource(R.drawable.map);

        ismapopen = true;
    }

    public boolean onTouchEvent (MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();



        if(ismapopen) {
            if (y < 640) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    upordown = true;
                    istouched= true;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    istouched= false;
                }


            } else {
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    upordown = false;
                    istouched = true;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    istouched = false;
                }

            }
        }



        return super.onTouchEvent(event);
    }

    public void onClickButton2(View v){ // OPEN FILES
        String inWC = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WC";
        String inWF1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WF1";
        String inWF2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WF2";
        String inBF1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BF1";
        String inBF2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BF2";
        String inSample = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sample";

        File fileWC = new File(inWC);
        File fileWF1 = new File(inWF1);
        File fileWF2 = new File(inWF2);
        File fileBF1 = new File(inBF1);
        File fileBF2 = new File(inBF2);
        File fileSample = new File(inSample);


        double[] WC_t = new double[2*7];
        double[] WF1_t = new double[256*32];
        double[] WF2_t = new double[32*12];
        double[] Sample_t = new double[8192];




        try {
            FileInputStream fWC = new FileInputStream(fileWC);
            FileInputStream fWF1 = new FileInputStream(fileWF1);
            FileInputStream fWF2 = new FileInputStream(fileWF2);
            FileInputStream fBF1 = new FileInputStream(fileBF1);
            FileInputStream fBF2 = new FileInputStream(fileBF2);
            FileInputStream fSample = new FileInputStream(fileSample);

            BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(fWC));

            String temp = "";
            int num = 0;

            while((temp=bufferedReader1.readLine()) != null){
                WC_t[num] = Double.parseDouble(temp);
                num += 1;
            }

            bufferedReader1.close();

            for(int i=0;i<2;i++) {
                for (int q = 0; q < 7; q++) {
                    WC[i][q]=WC_t[i*7+q];
                }
            }

            num = 0;
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(fWF1));

            while((temp=bufferedReader2.readLine()) != null){
                WF1_t[num] = Double.parseDouble(temp);
                num += 1;
            }
            bufferedReader2.close();

            for(int q=0;q<256;q++) {
                for (int p = 0; p < 32; p++) {
                    WF1[q][p]=WF1_t[p*256 + q];
                }
            }

            num = 0;
            BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(fWF2));

            while((temp=bufferedReader3.readLine()) != null){
                WF2_t[num] = Double.parseDouble(temp);
                num += 1;
            }
            bufferedReader3.close();

            for(int q=0;q<32;q++) {
                for (int p = 0; p < 12; p++) {
                    WF2[q][p]=WF2_t[p*32 + q];
                }
            }

            num = 0;
            BufferedReader bufferedReader4 = new BufferedReader(new InputStreamReader(fBF1));

            while((temp=bufferedReader4.readLine()) != null){
                BF1[num] = Double.parseDouble(temp);
                num += 1;
            }
            bufferedReader4.close();


            num = 0;
            BufferedReader bufferedReader5 = new BufferedReader(new InputStreamReader(fBF2));

            while((temp=bufferedReader5.readLine()) != null){
                BF2[num] = Double.parseDouble(temp);
                num += 1;
            }
            bufferedReader5.close();


            num = 0;
            BufferedReader bufferedReader7 = new BufferedReader(new InputStreamReader(fSample));

            while((temp=bufferedReader7.readLine()) != null){
                Sample_t[num] = Double.parseDouble(temp);
                num += 1;
            }
            bufferedReader7.close();

            for(int q=0;q<2;q++) {
                for (int p = 0; p < 4096; p++) {
                    sample1[q][p]=Sample_t[q*4096 + p];
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        int WC_length = WC[0].length;
        int WF1_length = WF1.length;
        int WF2_length = WF2[0].length;
        Toast.makeText(getApplicationContext(),"WC="+WC_length+" WF1="+WF1_length+" WF2="+WF2_length,Toast.LENGTH_SHORT).show();


        double maxsig = 0;
        double max_can = 0;
        for (int a=0;a<4096;a++){
            max_can = Math.max(Math.abs(sample1[0][a]),Math.abs(sample1[1][a]));
            maxsig = Math.max(Math.abs(maxsig),Math.abs(max_can));
        }
        for (int a=0;a<4096;a++){
            sample1[0][a]=sample1[0][a]/maxsig;
            sample1[1][a]=sample1[1][a]/maxsig;
        }

        int aaa=Network(sample1);
        Toast.makeText(getApplicationContext(),String.valueOf(aaa),Toast.LENGTH_SHORT).show();

    }



    class ThreadRec implements Runnable{
        public void run() {
            if (training == true) {
                String fileName = editText1.getText().toString();
                String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

                File dir = new File(dirName);

                if (!dir.exists()) {
                    dir.mkdirs();
                    handler.post(new Runnable() {
                        public void run() {
                            textView1.setText("Directory is Created");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            textView1.setText("Directory already exists");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                File file1 = new File(dirName + fileName);

                boolean isCreated = false;
                boolean isDeleted = true;

                if (file1.exists()) {
                    isDeleted = file1.delete();
                }

                if (!isDeleted) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView1.setText("Failed to Delete a File");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    isCreated = file1.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!isCreated) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView1.setText("Failed to create a file");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView1.setText("Successed to create a file");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView1.setText("Start Recording");
                    }
                });

                try {
                    FileOutputStream fileStream = new FileOutputStream(file1);
                    AudioRecord sRec;
                    final int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                    sRec = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                    sRec.startRecording();

                    int index = 63;
                    int[] bufStapL = new int[4096];
                    int[] bufStapR = new int[4096];



                    while (maxClass <= nT * C -1) {
                        short[] buffer = new short[bufferSize];
                        final int bufferReadResults = sRec.read(buffer, 0, bufferSize);

                        if (bufferReadResults > 0) {
                            for (int i = 0; i < bufferReadResults / 2; i++) {
                                int dataSTAPL = (buffer[2 * i] < 0) ? -1 * buffer[2 * i] : buffer[2 * i];
                                int dataSTAPR = (buffer[2 * i + 1] < 0) ? -1 * buffer[2 * i + 1] : buffer[2 * i + 1];

                                if (dataSTAPL < 8000 && dataSTAPR < 8000 && index == 63) {
                                    for (int j = 0; j < index; j++) {
                                        bufStapL[j] = bufStapL[j + 1];
                                        bufStapR[j] = bufStapR[j + 1];
                                    }
                                } else {
                                    index += 1;
                                }

                                bufStapL[index] = buffer[2 * i];
                                bufStapR[index] = buffer[2 * i + 1];

                                if (index == 4095) {
                                    index = 63;
                                    final String noSamp = String.valueOf(++maxClass);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView2.setText(noSamp);
                                            arrow_Position(maxClass);
                                        }
                                    });

/*
                                    double[] topSig1 = new double[2048];
                                    double[] topSig2 = new double[2048];
                                    double[] botSig1 = new double[2048];
                                    double[] botSig2 = new double[2048];

                                    for(int k=0; k<2048; k++){
                                        topSig1[k] = bufStapL[2*k];
                                        topSig2[k] = bufStapL[2*k+1];
                                        botSig1[k] = bufStapR[2*k];
                                        botSig2[k] = bufStapR[2*k+1];
                                    }

                                    double[][] fftSig1 = new double[2048][2];
                                    double[][] fftSig2 = new double[2048][2];

                                    for(int k=0;k<2048;k++){
                                        fftSig1[k][0] = (double) (topSig1[k]-topSig2[k]);
                                        fftSig2[k][0] = (double) (botSig1[k]-botSig2[k]);
                                    }

                                    fft(fftSig1);
                                    fft(fftSig2);

                                    double[] magSig1 = new double[1024];//D];
                                    double[] magSig2 = new double[1024];//D];


                                    //for (int k=0;k<D/2;k++){
                                    //    int a=(1024/D/2)*(k+1);
                                    //    magSig1[k] = Math.sqrt(fftSig1[a][0] * fftSig1[a][0] + fftSig1[a][1] * fftSig1[a][1]);
                                    //    magSig2[k] = Math.sqrt(fftSig2[a][0] * fftSig2[a][0] + fftSig2[a][1] * fftSig2[a][1]);
                                    //}
                                    for (int k=0;k<1024;k++){
                                        magSig1[k] = Math.sqrt(fftSig1[k][0]*fftSig1[k][0] + fftSig1[k][1]*fftSig1[k][1]);
                                        magSig2[k] = Math.sqrt(fftSig2[k][0]*fftSig2[k][0] + fftSig2[k][1]*fftSig2[k][1]);
                                    }


                                    double[] magSig = new double[2048];//D];
                                    //for(int k=0;k<D/2;k++){
                                    //    magSig[k]=magSig1[k];
                                    //    magSig[k+D/2]=magSig2[k];
                                    //}
                                    for (int k=0;k<1024;k++){
                                        magSig[k] = magSig1[k];
                                        magSig[k+1024]=magSig2[k];
                                    }

                                    //double maxSig=-10;
//
                                    //for(int k=0;k<D;k++){
                                    //    if(magSig[k]>maxSig){
                                    //        maxSig=magSig[k];
                                    //    }
                                    //}
                                    //for(int k=0;k<D;k++){
                                    //    magSig[k] = magSig[k]/maxSig;
                                    //}

*/
                                    for (int k = 0; k < 4096; k++) {
                                        // String tempStr =  String.valueOf(magSig[k]) + "\n";//String.valueOf(bufStapL[k]) + " "+String.valueOf(bufStapR[k]) +" "+String.valueOf(fftSig[k][0])+" "+String.valueOf(fftSig[k][1])+"\n";
                                        String tempStr = String.valueOf(bufStapL[k]) + " " + String.valueOf(bufStapR[k]) + "\n";
                                        //String tempStr = String.valueOf(magSig[k]) + "\n";
                                        try {
                                            fileStream.write(tempStr.getBytes());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    fileStream.flush();
                                }
                            }
                        }
                    }
                    fileStream.close();


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            textView1.setText("Sound recoreded in File");
                            textView2.setText("Training is finished");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sRec.stop();
                    sRec.release();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else{

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView1.setText("Start Test");
                    }
                });

                try{
                    AudioRecord sRec;
                    final int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                    sRec = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                    sRec.startRecording();

                    int index = 63;
                    int[] bufStapL = new int[4096];
                    int[] bufStapR = new int[4096];


                    while (!Thread.currentThread().isInterrupted()) {
                        short[] buffer = new short[bufferSize];
                        final int bufferReadResults = sRec.read(buffer, 0, bufferSize);

                        if (bufferReadResults > 0) {
                            for (int i = 0; i < bufferReadResults / 2; i++) {
                                int dataSTAPL = (buffer[2 * i] < 0) ? -1 * buffer[2 * i] : buffer[2 * i];
                                int dataSTAPR = (buffer[2 * i + 1] < 0) ? -1 * buffer[2 * i + 1] : buffer[2 * i + 1];

                                if (dataSTAPL < 8000 && dataSTAPR < 8000 && index == 63) {
                                    for (int j = 0; j < index; j++) {
                                        bufStapL[j] = bufStapL[j + 1];
                                        bufStapR[j] = bufStapR[j + 1];
                                    }
                                } else {
                                    index += 1;
                                }

                                bufStapL[index] = buffer[2 * i];
                                bufStapR[index] = buffer[2 * i + 1];

                                if (index == 4095) {
                                    index = 63;

                                    double[][] sig = new double[2][4096];
                                    for (int k=0;k<4096;k++){
                                        sig[0][k] = (double) bufStapL[k];
                                        sig[1][k] = (double) bufStapR[k];
                                    }

                                    double maxsig = 0;
                                    double max_can = 0;
                                    for (int a=0;a<4096;a++){
                                        max_can = Math.max(Math.abs(sig[0][a]),Math.abs(sig[1][a]));
                                        maxsig = Math.max(Math.abs(maxsig),Math.abs(max_can));
                                    }
                                    for (int a=0;a<4096;a++){
                                        sig[0][a]=sig[0][a]/maxsig;
                                        sig[1][a]=sig[1][a]/maxsig;
                                    }


                                    maxClass = Network(sig);

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            arrow_Position(maxClass);
                                            textView2.setText(String.valueOf(maxClass));

                                            if(iswebopen) {
                                                textView1.setText("web control");
                                                if (maxClass == 1) {
                                                    webView.pageUp(true);
                                                } else if (maxClass == 7) {
                                                    webView.pageDown(true);
                                                } else if (maxClass == 2) {
                                                    webView.pageUp(false);
                                                } else if (maxClass == 6) {
                                                    webView.pageDown(false);
                                                }
                                            }

                                            else if(isvideoopen) {
                                                textView1.setText("video control");
                                                videointent.putExtra(LOAD_METHOD_ID, LOAD_METHOD_CODE);
                                            }

                                            else if(ismapopen){
                                                if(istouched) {
                                                    if (upordown) {
                                                        if (maxClass <3 && maxClass >0) {
                                                            imageView2.setImageResource(R.drawable.mapziup);
                                                            textView1.setText("Zomm In up");
                                                        } else if (maxClass >5) {
                                                            imageView2.setImageResource(R.drawable.mapzoup);
                                                            textView1.setText("Zoom Out up");
                                                        } else if (maxClass == 4){
                                                            imageView2.setImageResource(R.drawable.map);
                                                        } else if (maxClass == 3){
                                                            imageView2.setImageResource(R.drawable.maprotright);
                                                        } else if (maxClass == 5){
                                                            imageView2.setImageResource(R.drawable.maprotleft);
                                                        }

                                                    } else {
                                                        if (maxClass <3 && maxClass >0) {
                                                            imageView2.setImageResource(R.drawable.mapzidown);
                                                            textView1.setText("Zoom In down");
                                                        } else if (maxClass>5) {
                                                            imageView2.setImageResource(R.drawable.mapzodown);
                                                            textView1.setText("Zoom Out down");
                                                        } else if (maxClass == 4){
                                                            imageView2.setImageResource(R.drawable.map);
                                                        } else if (maxClass == 3){
                                                            imageView2.setImageResource(R.drawable.maprotright);
                                                        } else if (maxClass == 5){
                                                            imageView2.setImageResource(R.drawable.maprotleft);
                                                        }
                                                    }
                                                }else
                                                    textView1.setText("not pressed");
                                            }
                                        }
                                    });

                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            arrow_Position(1000);
                                            textView2.setText("ready");

                                        }
                                    });
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sRec.stop();
                    sRec.release();
                }catch (Exception e) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error 01", Toast.LENGTH_SHORT).show();
                        }});
                    e.printStackTrace();
                }
            }
        }
    }



    public static int bitReverse(int n, int bits) {
        int reversedN = n;
        int count = bits - 1;

        n >>= 1;
        while (n > 0) {
            reversedN = (reversedN << 1) | (n & 1);
            count--;
            n >>= 1;
        }

        return ((reversedN << count) & ((1 << bits) - 1));
    }

    public void fft(double[][] buffer) {

        int bits = (int) (log(buffer.length) / log(2));

        double[][] temp = new double[buffer.length][2];

        for (int j = 0; j < buffer.length; j++) {
            temp[j][0] = buffer[j][0];
            temp[j][1] = buffer[j][1];
        }

        for (int j = 0; j < buffer.length; j++) {
            int swapPos = bitReverse(j, bits);
            buffer[j][0] = temp[swapPos][0];
            buffer[j][1] = temp[swapPos][1];
        }

        for (int N = 2; N <= buffer.length; N <<= 1) {
            for (int i = 0; i < buffer.length; i += N) {
                for (int k = 0; k < N / 2; k++) {

                    int evenIndex = i + k;
                    int oddIndex = i + k + (N / 2);
                    double[] even = new double[2];
                    double[] odd = new double[2];
                    even[0] = buffer[evenIndex][0];
                    even[1] = buffer[evenIndex][1];
                    odd[0] = buffer[oddIndex][0];
                    odd[1] = buffer[oddIndex][1];

                    double term = (-2 * PI * k) / (double) N;
                    double[] exp = new double[2];
                    exp[0] = odd[0]*cos(term)-odd[1]*sin(term);
                    exp[1] = odd[1]*cos(term)+odd[0]*sin(term);

                    buffer[evenIndex][0] = even[0]+exp[0];
                    buffer[evenIndex][1] = even[1]+exp[1];
                    buffer[oddIndex][0] = even[0]-exp[0];
                    buffer[oddIndex][1] = even[1]-exp[1];
                }
            }
        }
    }

    public void arrow_Position(int cnt){
        if(training==false && iswebopen==false){
            if(cnt==1){//cnt>=0 && cnt<=4){
                posX = 0;     posY = 0;       angle = 0;}
            else if(cnt==2){//cnt>=5 && cnt<=9){
                posX = 200;   posY = 0;       angle = 30;}
            else if(cnt==3){//cnt>=10 && cnt<=14){
                posX = 200;   posY = 80;     angle = 90;}
            else if(cnt==4){//cnt>=15 && cnt<=19){
                posX = 200;   posY = 350;     angle = 90;}
            else if(cnt==5){//cnt>=20 && cnt<=24){
                posX = 200;   posY = 700;     angle = 90;}
            else if(cnt==6){//cnt>=25 && cnt<=29){
                posX = 200;   posY = 800;    angle = 150;}
            else if(cnt==7){//cnt>=30 && cnt<=34){
                posX = 0;     posY = 800;    angle = 180;}
            else if(cnt==8){//cnt>=35 && cnt<=39){
                posX = -200;  posY = 800;    angle = 210;}
            else if(cnt==9){//cnt>=40 && cnt<=44){
                posX = -200;  posY = 700;     angle = 270;}
            else if(cnt==10){//cnt>=45 && cnt<=49){
                posX = -200;  posY = 350;     angle = 270;}
            else if(cnt==11){//cnt>=50 && cnt<=54){
                posX = -200;  posY = 80;     angle = 270;}
            else if(cnt==12){//cnt>=55 && cnt<=59){
                posX = -200;  posY = 0;       angle = 330;}
            else{
                posX = 1000; posY=-1000;}
        } else if(training==true && iswebopen==false) {
            if(cnt>=0 && cnt<=nT-1){
                posX = 0;     posY = 0;       angle = 0;}
            else if(cnt>=nT && cnt<=2*nT-1){
                posX = 200;   posY = 0;       angle = 30;}
            else if(cnt>=2*nT && cnt<=3*nT-1){
                posX = 200;   posY = 80;     angle = 90;}
            else if(cnt>=3*nT && cnt<=4*nT-1){
                posX = 200;   posY = 350;     angle = 90;}
            else if(cnt>=4*nT && cnt<=5*nT-1){
                posX = 200;   posY = 700;     angle = 90;}
            else if(cnt>=5*nT && cnt<=6*nT-1){
                posX = 200;   posY = 800;    angle = 150;}
            else if(cnt>=6*nT && cnt<=7*nT-1){
                posX = 0;     posY = 800;    angle = 180;}
            else if(cnt>=7*nT && cnt<=8*nT-1){
                posX = -200;  posY = 800;    angle = 210;}
            else if(cnt>=8*nT && cnt<=9*nT-1){
                posX = -200;  posY = 700;     angle = 270;}
            else if(cnt>=9*nT && cnt<=10*nT-1){
                posX = -200;  posY = 350;     angle = 270;}
            else if(cnt>=10*nT && cnt<=11*nT-1){
                posX = -200;  posY = 80;     angle = 270;}
            else if(cnt>=11*nT && cnt<=12*nT-1){
                posX = -200;  posY = 0;       angle = 330;}
            else{
                posX = 1000; posY=-1000;}
        }



        imageView1.setTranslationX(posX);
        imageView1.setTranslationY(posY);
        imageView1.setRotation(angle);
    }

    public double svmoutput(double[] tstX, double[][] TRset, double[] alpha, double b){
        double[] H = new double[N];
        double C = 0;
        double out=0;

        for (int i = 0; i < N; i++) {
            double ker = 0;
            for (int j = 0; j < D; j++) {
                ker = ker +  (TRset[i][j] * tstX[j]);//(TRset[i][j] - tstX[j])*(TRset[i][j] - tstX[j]); //
            }
            H[i] = Y[i] *  (ker +1);//exp(-ker/4); //
        }
        for (int i = 0; i < N; i++) {
            C = C + H[i] * alpha[i];
        }
        C = C + b;

        //if(C>0){out=1;}
        //else {out = -1;}

        out=C;
        return out;
    }
/*
    public int SVMBU(double[] tstX){
        double[] C1={0,0,0};
        double[] C2={0,0};
        double C3=0;
        int out=0;

        C1[0]=svmoutput(tstX,SVs[0], alphas[0],bias[0]);
        C1[1]=svmoutput(tstX,SVs[11], alphas[11],bias[11]);
        C1[2]=svmoutput(tstX,SVs[18], alphas[18],bias[18]);

        if(C1[0]>0 && C1[1]>0){
            C2[0]=svmoutput(tstX,SVs[1], alphas[1],bias[1]);
        } else if(C1[0]>0 && C1[1]<0){
            C2[0]=svmoutput(tstX,SVs[2], alphas[2],bias[2]);
        } else if(C1[0]<0 && C1[1]>0){
            C2[0]=svmoutput(tstX,SVs[6], alphas[6],bias[6]);
        } else if(C1[0]<0 && C1[1]<0) {
            C2[0]=svmoutput(tstX,SVs[7], alphas[7],bias[7]);
        }

        if(C1[2]>0){
            C2[1]=svmoutput(tstX,SVs[19], alphas[19],bias[19]);
        } else if(C1[2]<0){
            C2[1]=svmoutput(tstX,SVs[20], alphas[20],bias[20]);
        }

        if(C1[0]>0 && C1[2]>0 && C2[0]>0 && C2[1]>0){           //1vs5
            C3 = svmoutput(tstX, SVs[3], alphas[3],bias[3]);
            if(C3>threshold){out=1;}else if(C3<-threshold){out=5;}else{out=0;}
        } else if(C1[0]>0 && C1[2]<0 && C2[0]>0 && C2[1]>0){   //1vs6
            C3 = svmoutput(tstX, SVs[4], alphas[4],bias[4]);
            if(C3>threshold){out=1;}else if(C3<-threshold){out=6;}else{out=0;}
        } else if(C1[0]>0 && C2[0]>0 && C2[1]<0) {              //1vs7
            C3 = svmoutput(tstX, SVs[5], alphas[5], bias[5]);
            if(C3>threshold){out=1;}else if(C3<-threshold){out=7;}else{out=0;}
        } else if(C1[0]<0 && C1[2]>0 && C2[0]>0 && C2[1]>0) {  //2vs5
            C3 = svmoutput(tstX, SVs[8], alphas[8], bias[8]);
            if(C3>threshold){out=2;}else if(C3<-threshold){out=5;}else{out=0;}
        } else if(C1[0]<0 && C1[2]<0 && C2[0]>0 && C2[1]>0) { //2vs6
            C3 = svmoutput(tstX, SVs[9], alphas[9], bias[9]);
            if(C3>threshold){out=2;}else if(C3<-threshold){out=6;}else{out=0;}
        } else if(C1[0]<0 && C2[0]>0 && C2[1]<0) {             //2vs7
            C3 = svmoutput(tstX, SVs[10], alphas[10], bias[10]);
            if(C3>threshold){out=2;}else if(C3<-threshold){out=7;}else{out=0;}
        } else if(C1[1]>0 && C1[2]>0 && C2[0]<0 && C2[1]>0) {  //3vs5
            C3 = svmoutput(tstX, SVs[12], alphas[12], bias[12]);
            if(C3>threshold){out=3;}else if(C3<-threshold){out=5;}else{out=0;}
        } else if(C1[1]>0 && C1[2]<0 && C2[0]<0 && C2[1]>0) { //3vs6
            C3 = svmoutput(tstX, SVs[13], alphas[13], bias[13]);
            if(C3>threshold){out=3;}else if(C3<-threshold){out=6;}else{out=0;}
        } else if(C1[1]>0 && C2[0]<0 && C2[1]<0) {             //3vs7
            C3 = svmoutput(tstX, SVs[14], alphas[14], bias[14]);
            if(C3>threshold){out=3;}else if(C3<-threshold){out=7;}else{out=0;}
        } else if(C1[1]<0 && C1[2]>0 && C2[0]<0 && C2[1]>0) { //4vs5
            C3 = svmoutput(tstX, SVs[15], alphas[15], bias[15]);
            if(C3>threshold){out=4;}else if(C3<-threshold){out=5;}else{out=0;}
        } else if(C1[1]<0 && C1[2]<0 && C2[0]<0 && C2[1]>0) {//4vs6
            C3 = svmoutput(tstX, SVs[16], alphas[16], bias[16]);
            if(C3>threshold){out=4;}else if(C3<-threshold){out=6;}else{out=0;}
        } else if(C1[1]<0 && C2[0]<0 && C2[1]<0) {            //4vs7
            C3 = svmoutput(tstX, SVs[17], alphas[17], bias[17]);
            if(C3>threshold){out=4;}else if(C3<-threshold){out=7;}else{out=0;}
        }
        return out;
    }
*/
    public double[] Conv(double[] input1, double[] input2, double[][]WC){
        int size = input1.length;
        double[] conv_out = new double[size];
        
        for (int k = 0; k < size; k++) {
            if (k == 0) {
                conv_out[k] = input1[0] * WC[0][3] + input1[1] * WC[0][4] + input1[2] * WC[0][5] + input1[3] * WC[0][6] +
                        input2[0] * WC[1][3] + input2[1] * WC[1][4] + input2[2] * WC[1][5] + input2[3] * WC[1][6];
            } else if (k == 1) {
                conv_out[k] = input1[0] * WC[0][2] + input1[1] * WC[0][3] + input1[2] * WC[0][4] + input1[3] * WC[0][5] + input1[4] * WC[0][6] +
                        input2[0] * WC[1][2] + input2[1] * WC[1][3] + input2[2] * WC[1][4] + input2[3] * WC[1][5] + input2[4] * WC[1][6];
            } else if (k == 2) {
                conv_out[k] = input1[0] * WC[0][1] + input1[1] * WC[0][2] + input1[2] * WC[0][3] + input1[3] * WC[0][4] + input1[4] * WC[0][5] + input1[5] * WC[0][6] +
                        input2[0] * WC[1][1] + input2[1] * WC[1][2] + input2[2] * WC[1][3] + input2[3] * WC[1][4] + input2[4] * WC[1][5] + input2[5] * WC[1][6];
            } else if (k == size-3) {
                conv_out[k] = input1[size-6] * WC[0][0] + input1[size-5] * WC[0][1] + input1[size-4] * WC[0][2] + input1[size-3] * WC[0][3] + input1[size-2] * WC[0][4] + input1[size-1] * WC[0][5] +
                        input2[size-6] * WC[1][0] + input2[size-5] * WC[1][1] + input2[size-4] * WC[1][2] + input2[size-3] * WC[1][3] + input2[size-2] * WC[1][4] + input2[size-1] * WC[1][5];
            } else if (k == size-2) {
                conv_out[k] = input1[size-5] * WC[0][0] + input1[size-4] * WC[0][1] + input1[size-3] * WC[0][2] + input1[size-2] * WC[0][3] + input1[size-1] * WC[0][4] +
                        input2[size-5] * WC[1][0] + input2[size-4] * WC[1][1] + input2[size-3] * WC[1][2] + input2[size-2] * WC[1][3] + input2[size-1] * WC[1][4];
            } else if (k == size-1) {
                conv_out[k] = input1[size-4] * WC[0][0] + input1[size-3] * WC[0][1] + input1[size-2] * WC[0][2] + input1[size-1] * WC[0][3] +
                        input2[size-4] * WC[1][0] + input2[size-3] * WC[1][1] + input2[size-2] * WC[1][2] + input2[size-1] * WC[1][3];
            } else {
                conv_out[k] = input1[k - 3] * WC[0][0] + input1[k - 2] * WC[0][1] + input1[k - 1] * WC[0][2] + input1[k] * WC[0][3] + input1[k + 1] * WC[0][4] + input1[k + 2] * WC[0][5] + input1[k + 3] * WC[0][6] +
                        input2[k - 3] * WC[1][0] + input2[k - 2] * WC[1][1] + input2[k - 1] * WC[1][2] + input2[k] * WC[1][3] + input2[k + 1] * WC[1][4] + input2[k + 2] * WC[1][5] + input2[k + 3] * WC[1][6];
            }
        }
        return conv_out;
    }

    public double[] Max_Pool(double[] input, int stride){

        int size = input.length;
        double[] out = new double[size/stride];

        for (int j=0; j< size/stride; j++){

            double tmpout = 0;
            for (int i=0; i<stride; i++) {
                if (input[i+j*stride] >= tmpout) {
                    tmpout = input[i+j*stride];
                }
            }
            out[j]=tmpout;
        }

        return out;
    }

    public double[] Avg_Pool(double[] input, int stride){

        int size = input.length;
        double[] out = new double[size/stride];

        for (int j=0; j< size/stride; j++){

            double tmpout = 0;
            for (int i=0; i<stride; i++) {
                tmpout = tmpout + input[j*stride + i];
            }
            out[j]=tmpout/stride;
        }

        return out;
    }

    public double[] Batch_Normalize(double[] input, double mean, double var){
        int size = input.length;
        double[] out = new double[size];
        for (int i=0;i<size; i++){
            if(var==0){
                out[i] = (input[i]-mean)/0.0001;
            }
            else {
                out[i] = (input[i] - mean) /Math.sqrt(var);
            }
        }
        return out;
    }

    public double[] reLU(double[] input){
        int size = input.length;
        double[] out = new double[size];
        for (int i=0;i<size;i++){
            out[i] = max(input[i],0);
        }
        return out;
    }



    public double[] Fully_Connected(double[] input, double[][] W, double[] B, int outsize){
        int size = input.length;
        double[] out = new double[outsize];

        for (int i=0;i<outsize;i++){
            out[i] = 0;
            for (int j=0;j<size;j++){

                out[i]=out[i]+(input[j]*W[j][i]);
            }
            out[i]=out[i]+B[i];
        }
        return out;
    }

    public static double[] Soft_Max(double[] input){
        int size = input.length;
        double[] out = new double[size];
        double sum = 0;
        double min =0;
        for (int i=0;i<size;i++){
            if(input[i]<min){
                min=input[i];
            }
        }
        if(min<0){
            min=-min;
        }
        else{
            min=0;
        }
        for (int i=0;i<size;i++){
            sum = sum + input[i] + min;
        }

        for (int i=0;i<size;i++){
            out[i]=(input[i]+min)/sum;
        }
        return out;

    }

    public int Network(double[][] input){
        double[] Conv_out = new double[1024];
        double[] Apool_out1 = new double[1024];
        double[] Apool_out2 = new double[1024];
        double[] BN_out = new double[1024];
        double[] Act_out = new double[1024];
        double[] Mpool_out = new double[256];
        double[] FC1_out = new double[32];
        double[] FC2_out = new double[12];
        double[] SM_out = new double[12];
        double BNT = 3337025.75;


        Apool_out1 = Avg_Pool(input[0], 4);
        Apool_out2 = Avg_Pool(input[1], 4);
        Conv_out = Conv(Apool_out1, Apool_out2, WC);
        //BN_out = Batch_Normalize(Conv_out, BN[0],BNT);
        Act_out = reLU(Conv_out);
        Mpool_out = Max_Pool(Act_out, 4);
        FC1_out = Fully_Connected(Mpool_out, WF1, BF1, 32);
        FC1_out = reLU(FC1_out);
        FC2_out = Fully_Connected(FC1_out, WF2, BF2, 12);
        SM_out = Soft_Max(FC2_out);

        double maxSig=0.15;


        maxClass = -1;

        for(int k=0;k<12;k++){
            if(SM_out[k]>maxSig){
                maxSig=SM_out[k];
                maxClass=k+1;
            }
        }
        return maxClass;
    }
}



