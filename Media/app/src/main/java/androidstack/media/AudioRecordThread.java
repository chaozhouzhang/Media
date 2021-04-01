package androidstack.media;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecordThread extends Thread {

    /**
     * 音频采集的来源
     * 一般是麦克风
     */
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /**
     * 采样率
     * 单位：赫兹
     * 每秒钟音频采样点个数(8000/44100Hz)，模拟信号数字化的过程，用0101来表示的数字信号
     */
    private static final int SAMPLE_RATE_IN_HZ = 44100;
    /**
     * 声道
     * AudioFormat.CHANNEL_IN_MONO 单声道，一个声道进行采样
     * AudioFormat.CHANNEL_IN_STEREO 双声道，两个声道进行采样
     */
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 音频采样精度
     * 指定采样的数据的格式和每次采样的大小。
     * 数据返回格式为 PCM 格式
     * 每次采样的位宽为 16bit
     * 一般都采用这个 AudioFormat.ENCODING_PCM_16BIT(官方文档表示，该采样精度保证所有设备都支持)
     */
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;//数据返回格式为 PCM 格式，每次采样的位宽为 16bit，官方文档表示，该采样精度保证所有设备都支持

    /**
     * 缓冲区byte字节数组大小
     */
    private int mBufferSizeInBytes = 0;

    /**
     * 回调的byte字节数组大小
     */
    private static final int CALLBACK_BUFFER_SIZE_IN_BYTES = 48000 * 20 * 2 / 1000; //20ms


    /**
     *
     */
    public static long mRecordBuffSize;

    /**
     * 录音对象
     */
    private AudioRecord mAudioRecord = null;

    /**
     * 录音状态。默认未准备。
     */
    private AudioRecordStatus mAudioRecordStatus = AudioRecordStatus.STATUS_NO_READY;

    /**
     *
     */

    private DataBuffer mRecordDataBuf = null;
    /**
     * 创建录音对象
     */
    public void createAudio(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        mRecordBuffSize = 0;
        // release the object
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        mBufferSizeInBytes = Math.max(mBufferSizeInBytes, (CALLBACK_BUFFER_SIZE_IN_BYTES * 5));
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mBufferSizeInBytes);
        //已初始化，准备好录制
        mAudioRecordStatus = AudioRecordStatus.STATUS_READY;
    }

    /**
     * 创建默认设置的录音对象
     */
    public void createDefaultAudio() {
        createAudio(AUDIO_SOURCE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
    }

    /**
     * 进行录制
     */
    @Override
    public void run() {
        super.run();
        if (mAudioRecord == null) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (mAudioRecordStatus == AudioRecordStatus.STATUS_NO_READY) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (mAudioRecordStatus == AudioRecordStatus.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        mAudioRecord.startRecording();
        byte[] recBuf = new byte[CALLBACK_BUFFER_SIZE_IN_BYTES];
        //将录音状态设置成正在录音状态
        mAudioRecordStatus = AudioRecordStatus.STATUS_START;
        long startTime = 0;
        long numSample = 0;
        while (mAudioRecordStatus == AudioRecordStatus.STATUS_START) {
            int numOfRead = mAudioRecord.read(recBuf, 0, CALLBACK_BUFFER_SIZE_IN_BYTES);
            // ERROR_INVALID_OPERATION if the object wasn't properly initialized
            if (numOfRead <= 0) {
                break;
            }
            mRecordBuffSize += numOfRead;
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            } else {
                numSample += numOfRead;
            }

            if (mRecordDataBuf.getLeft() <= recBuf.length) {
                mRecordDataBuf.erase(recBuf.length);
            }
            mRecordDataBuf.pushData(recBuf, 0, numOfRead);
        }
        stopRecord();
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        if (mAudioRecordStatus == AudioRecordStatus.STATUS_NO_READY || mAudioRecordStatus == AudioRecordStatus.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            mAudioRecord.stop();
            mAudioRecordStatus = AudioRecordStatus.STATUS_STOP;
            release();
        }
    }

    /**
     * 释放资源
     */
    private void release() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        mAudioRecordStatus = AudioRecordStatus.STATUS_NO_READY;
    }


    /**
     * 录音对象的状态
     */
    public enum AudioRecordStatus {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }
}
