package com.nuaily.player;

import io.vov.vitamio.R;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class MediaPlayerActivity extends Activity {
	private VideoView mVideoView;
	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private AudioManager mAudioManager;
	private int mMaxVolume; /* ������� */
	private int mVolume = -1;/* ��ǰ���� */
	private float mBrightness = -1f;/* ��ǰ���� */
	private int mLayout; /* ��ǰ����ģʽ */
	private GestureDetector mGestureDetector;
	private MediaController mMediaController;

	@Override
	public void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.videoview);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Intent intent=getIntent();
		String path=intent.getStringExtra("path");
		mVideoView.setVideoPath(path);
		mMediaController = new MediaController(this);
		mVideoView.setMediaController(mMediaController);
		mVideoView.requestFocus();
		mGestureDetector = new GestureDetector(this, new MyGestureListener());
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;
		/* �������ƽ��� */
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			endGesture();
			break;
		}
		return super.onTouchEvent(event);
	}

	/* �������ƽ��� */
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// ����
		dismissHandler.removeMessages(0);
		dismissHandler.sendEmptyMessageDelayed(0, 500);
	}

	private class MyGestureListener extends SimpleOnGestureListener {
		/* ˫�� */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
				mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			else
				mLayout++;
			if (mVideoView != null)
				mVideoView.setVideoLayout(mLayout, 0);
			return true;
		}

		/* ���� */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			float mOldX = e1.getX(), mOldY = e2.getY();
			int y = (int) e2.getRawY();
			Display disp = getWindowManager().getDefaultDisplay();
			int windowWidth = disp.getWidth();
			int windowHeight = disp.getHeight();

			if (mOldX > windowWidth * 4.0 / 5)// ���һ���
				onVolumeSlide((mOldY - y) / windowHeight);
			else if (mOldX < windowWidth / 5.0)// ���һ���
				onBrightnessSlide((mOldY - y) / windowHeight);
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	/* ���ö�ʱ���� */
	private Handler dismissHandler = new Handler() {
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
	};

	/* �����ı�������С */
	private void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;
			// ��ʾ����
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);// �����仯
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();// �ı����
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width
				* index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}
	
	/*�����ı�����*/
	private void onBrightnessSlide(float percent){
		if(mBrightness<0){
			mBrightness=getWindow().getAttributes().screenBrightness;
			if(mBrightness<=0.00f)
				mBrightness=0.50f;
			if(mBrightness<0.01f)
				mBrightness=0.01f;
			
			//��ʾ
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		WindowManager.LayoutParams lpa=getWindow().getAttributes();
		lpa.screenBrightness=mBrightness+percent;
		if(lpa.screenBrightness>1.0f)
			lpa.screenBrightness=1.0f;
		else if (lpa.screenBrightness<0.01f)
			lpa.screenBrightness=0.01f;
		getWindow().setAttributes(lpa);
		
		ViewGroup.LayoutParams lp=mOperationPercent.getLayoutParams();
		lp.width=(int)(findViewById(R.id.operation_full).getLayoutParams().width*lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		if(mVideoView!=null)
			mVideoView.setVideoLayout(mLayout, 0);
		super.onConfigurationChanged(newConfig);
	}
}
