/**
 * 
 */
/**
 * @author Yuan
 *
 */
package com.nuaily.player;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class player extends Activity {
	protected static final int SEARCH_VIDEO_SUCCESS = 0;
	private SeekBar seekBar;
	private ListView listView;
	private TextView tv_currTime, tv_totalTime, tv_showName;
	private List<String> list;
	private ProgressDialog pd;// 进度条对话框
	private VideoAdapter ma;
	private MediaPlayer mp;
	private int currIndex = 0;// 表示当前播放的索引
	private boolean flag = true;// 控制进度条线程标记

	// 定义播放状态
	private static final int IDLE = 0;
	private static final int PAUSE = 1;
	private static final int START = 2;
	private static final int CURR_TIME_VALUE = 1;

	private int currState = IDLE;// 表示当前播放器状态
	// 同时只能有一个线程运行
	ExecutorService es = Executors.newSingleThreadExecutor();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.player);
		list = new ArrayList<String>();
		// mp = new MediaPlayer();
		// mp.setOnCompletionListener(this);
		// mp.setOnErrorListener(this);
		initView();
		initList();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			// ListView监听器
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
		        Intent intent = new Intent(player.this,MediaPlayerActivity.class);
		        intent.putExtra("path", list.get(position));
		        startActivity(intent);
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (mp != null) {
			mp.stop();
			flag = false;
			mp.release();
		}
		super.onDestroy();
	}

	/**
	 * 初始化UI*
	 */
	private void initView() {
		// seekBar=(SeekBar)findViewById(R.id.seekBar1);
		// seekBar.setOnSeekBarChangeListener(this);
		listView = (ListView) findViewById(R.id.list);
		// tv_currTime = (TextView) findViewById(R.id.textView1_curr_time);
		// tv_totalTime = (TextView) findViewById(R.id.textView1_total_time);
		tv_showName = (TextView) findViewById(R.id.video_name);
	}

	private Handler hander = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SEARCH_VIDEO_SUCCESS:
				ma = new VideoAdapter();
				listView.setAdapter(ma);
				pd.dismiss();
				break;
			case CURR_TIME_VALUE:
				tv_currTime.setText(msg.obj.toString());
				break;
			default:
				break;
			}
		}
	};

	private void initList() {
		list.clear();
		// 是否有外部存储设备
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			pd = ProgressDialog.show(this, "", "正在搜索视频文件...", true);
			new Thread(new Runnable() {
				String[] ext = { ".mp4", ".flv", ".mkv", ".mov", ".mov" };
				File file = Environment.getExternalStorageDirectory();

				public void run() {
					search(file, ext);
					hander.sendEmptyMessage(SEARCH_VIDEO_SUCCESS);
				}
			}).start();

		} else {
			Toast.makeText(this, "请插入外部存储设备..", Toast.LENGTH_LONG).show();
		}
	}

	// 搜索音乐文件
	private void search(File file, String[] ext) {
		if (file != null) {
			if (file.isDirectory()) {
				File[] listFile = file.listFiles();
				if (listFile != null) {
					for (int i = 0; i < listFile.length; i++) {
						search(listFile[i], ext);
					}
				}
			} else {
				String filename = file.getAbsolutePath();
				for (int i = 0; i < ext.length; i++) {
					if (filename.endsWith(ext[i])) {
						list.add(filename);
						break;
					}
				}
			}
		}
	}

	class VideoAdapter extends BaseAdapter {

		public int getCount() {
			return list.size();
		}

		public Object getItem(int position) {
			return list.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.video_item,
						null);
			}
			TextView tv_Video_name = (TextView) convertView
					.findViewById(R.id.video_name);
			tv_Video_name.setText(getname(list.get(position)));
			return convertView;
		}
	}

	protected String getname(String string) {
		String[] temp = string.split("/");
		return temp[temp.length - 1];
	}

	/*
	 * private void play() { switch (currState) { case IDLE: start(); break;
	 * case PAUSE: mp.pause();
	 * btnPlay.setImageResource(R.drawable.ic_media_play); currState = START;
	 * break; case START: mp.start();
	 * btnPlay.setImageResource(R.drawable.ic_media_pause); currState = PAUSE; }
	 * }
	 * 
	 * //上一首 private void previous() { if((currIndex-1)>=0){ currIndex--;
	 * start(); }else{ Toast.makeText(this, "当前已经是第一首歌曲了",
	 * Toast.LENGTH_SHORT).show(); } }
	 * 
	 * //下一自首 private void next() { if(currIndex+1<list.size()){ currIndex++;
	 * start(); }else{ Toast.makeText(this, "当前已经是最后一首歌曲了",
	 * Toast.LENGTH_SHORT).show(); } }
	 * 
	 * //开始播放 private void start() { if (list.size() > 0 && currIndex <
	 * list.size()) { String SongPath = list.get(currIndex); mp.reset(); try {
	 * mp.setDataSource(SongPath); mp.prepare(); mp.start(); initSeekBar();
	 * es.execute(this); tv_showName.setText(list.get(currIndex));
	 * btnPlay.setImageResource(R.drawable.ic_media_pause); currState = PAUSE; }
	 * catch (IOException e) { e.printStackTrace(); } }else{
	 * Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show(); } }
	 * 
	 * //播放按钮 public void play(View v){ play(); }
	 * 
	 * //上一首按钮 public void previous(View v){ previous(); }
	 * 
	 * //下一首按钮 public void next(View v){ next(); }
	 * 
	 * //监听器，当当前歌曲播放完时触发，播放下一首 public void onCompletion(MediaPlayer mp) {
	 * if(list.size()>0){ next(); }else{ Toast.makeText(this, "播放列表为空",
	 * Toast.LENGTH_SHORT).show(); } }
	 * 
	 * //当播放异常时触发 public boolean onError(MediaPlayer mp, int what, int extra) {
	 * mp.reset(); return false; }
	 * 
	 * //初始化SeekBar private void initSeekBar(){
	 * seekBar.setMax(mp.getDuration()); seekBar.setProgress(0);
	 * tv_totalTime.setText(toTime(mp.getDuration())); }
	 */

}
