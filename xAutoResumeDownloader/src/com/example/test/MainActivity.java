package com.example.test;

import java.io.File;

import com.example.download.DownloadCallback;
import com.example.download.DownloadManager;
import com.example.downloader.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	ProgressBar progressBar;
	TextView currentTv,totalTv,textView3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressBar=(ProgressBar)this.findViewById(R.id.progressBar1);
		progressBar.setMax(100);
		currentTv=(TextView)this.findViewById(R.id.current);
		totalTv=(TextView)this.findViewById(R.id.total);
		textView3=(TextView)this.findViewById(R.id.textView3);
	}

	String url = "http://www.baidu.com/img/baidu_sylogo1.gif";
	String url2="http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk";
	public void startDownload(View view){
		DownloadManager.getDownloadManager().startDownload(
				url,
				null,
				null);
		DownloadManager.getDownloadManager().startDownload(
				url2,
				null,
				new DownloadCallback<File>(){

					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						Toast.makeText(MainActivity.this, "下载开始", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onDownloading(Long total, Long current) {
						// TODO Auto-generated method stub
						currentTv.setText(String.valueOf(current));
						totalTv.setText(String.valueOf(total));
						textView3.setText(String.valueOf(current * 100 / total));
						progressBar.setProgress((int) (current * 100 / total));
					}

					@Override
					public void onSuccessed(File file) {
						// TODO Auto-generated method stub
						Toast.makeText(MainActivity.this, "下载完成"+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
						progressBar.setProgress(0);
					}

					@Override
					public void onFailed(Exception e) {
						// TODO Auto-generated method stub
						Toast.makeText(MainActivity.this, "下载失败"+e.getMessage(), Toast.LENGTH_LONG).show();
					}

					@Override
					public void onCancelled() {
						// TODO Auto-generated method stub
						Toast.makeText(MainActivity.this, "下载取消", Toast.LENGTH_LONG).show();
					}

				});
	}
	public void stopDownload(View view){
		DownloadManager.getDownloadManager().stopDownload(
				url2);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
