package androidstack.media;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * 功能清单：
 * 0、绘图:使用三种不同的方式绘制图片
 * 1、录音:录制音频并获取 PCM 数据
 * 2、录屏
 * 3、录像:使用 Camera API 获取预览并获得 NV21 数据流
 * 4、截屏
 * 5、裁剪视频
 * 6、裁剪音频
 * 7、裁剪音视频
 * 8、合成视频
 * 9、合成音频
 * 10、合成音视频
 * 11、替换视频的音频
 * 12、给视频添加水印
 * 13、视频专场动画
 * 14、MP4:解析和封装 MP4 文件
 * 15、OpenGL
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        findViewById(R.id.btn_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioRecordThread audioRecordThread = new AudioRecordThread();
                audioRecordThread.createDefaultAudio();
                audioRecordThread.start();
            }
        });

        findViewById(R.id.btn_buffer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,BufferActivity.class));
            }
        });


        findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CameraActivity.class));
            }
        });
    }
}