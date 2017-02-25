package kr.ac.khu.idealab.sjsong.stap;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.VideoView;

public class SecondActivity extends AppCompatActivity {

    VideoView videoView;
    public static final String LOAD_METHOD_ID = "load_method_id";
    public static final int LOAD_METHOD_CODE = 92840;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        videoView = (VideoView) findViewById(R.id.videoView);
        String uri = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video1.wmv";
        Uri videouri = Uri.parse(uri);
        videoView.setVideoURI(videouri);
        videoView.start();




            int code1 = getIntent().getIntExtra(LOAD_METHOD_ID, 0);
            if (code1 == LOAD_METHOD_CODE) {
                videoView.pause();
            }

    }

    public void onClickButton6(View v){
        int currentposition = videoView.getCurrentPosition();
        videoView.seekTo(currentposition-1000);
    }
    public void onClickButton7(View v){
        if(videoView.isPlaying()==true){
            videoView.pause();}
        else{
            videoView.start();
        }
    }
    public void onClickButton8(View v){
        int currentposition = videoView.getCurrentPosition();
        videoView.seekTo(currentposition+2000);
    }



}



