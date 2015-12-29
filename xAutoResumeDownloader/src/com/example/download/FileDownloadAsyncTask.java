package com.example.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.io.SocketInputBuffer;

import com.example.download.DownloadAsyncTask.State;

import android.os.Environment;
import android.os.Message;

public class FileDownloadAsyncTask extends DownloadAsyncTask<File>{

	//	BufferedInputStream bis = null;
	//	BufferedOutputStream bos = null;
	//FileOutputStream fos = null;
	RandomAccessFile os=null;
	BufferedInputStream bis=null;
	//InputStream is=null;
	//HttpClient httpClient=null;
	public FileDownloadAsyncTask(Executor exec,String url) {
		super(exec,url);
		// TODO Auto-generated constructor stub
	}

	public FileDownloadAsyncTask(Executor exec,String url, String path) {
		super(exec,url, path);
		// TODO Auto-generated constructor stub
	}
	public FileDownloadAsyncTask(Executor exec,String url, String path,
			DownloadCallback<File> callback) {
		super(exec,url, path, callback);
		// TODO Auto-generated constructor stub
	}

	@Override 
	protected File downLoad() {
		// TODO Auto-generated method stub
		//检查当前路径下的文件大小
		//设置http头断点下载，更新进度
		File targetFile = new File(this.downloadPath);
		try {
			if (!targetFile.exists()) {
				File dir = targetFile.getParentFile();
				if (!dir.exists()) { 
					if(dir.mkdirs())
						targetFile.createNewFile();
				}
				File basefile=new File(BASEPATH);
				if (basefile.exists() || basefile.mkdirs()) {
					final File file = new File(BASEPATH, downloadPath.substring(BASEPATH.length(), downloadPath.length()));
					if(!file.exists()){
						file.createNewFile() ;
					}
				}

			}
			long current = 0;
			long total=0;

			HttpClient	httpClient = new DefaultHttpClient();
			httpClient.getParams().setIntParameter("http.socket.timeout", 5000);

			final HttpGet httpGet = new HttpGet(downloadUrl);
			if (true) {//默认支持断点下载
				current = targetFile.length();
				os = new RandomAccessFile(targetFile, "rw");
				os.seek(current);
				httpGet.addHeader("Range", "bytes=" + current + "-");
			}

			final HttpResponse response = httpClient.execute(httpGet);
			final int code = response.getStatusLine().getStatusCode();
			final HttpEntity entity = response.getEntity();
			System.out.println(code);

			if (entity != null && code < 400) {
				//is = entity.getContent();
				total=entity.getContentLength()+current;
				//is.skip(current);

				 bis = new BufferedInputStream(entity.getContent());
				byte[] tmp = new byte[4096];
				int len;
				while ((len = bis.read(tmp)) != -1) {
					
					
					//bos.write(tmp, 0, len);
					os.write(tmp, 0, len);
					
					current += len;
					postResult(UPDATE_LOADING,total, current);
					if(state==State.CANCELLED){
						return targetFile;
					}
				}
				postResult(UPDATE_SUCCESS,targetFile);
				downloadSuccessed();
			}else{

				postResult(UPDATE_FAILURE,new Exception("请求失败"));
			}
			//bos.flush();

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			postResult(UPDATE_FAILURE,e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			postResult(UPDATE_FAILURE,e);
		}
		
		finally {

			try {
				os.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if(bis!=null)
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//httpClient.getConnectionManager().shutdown();
		}
		return targetFile;
	}
	private final static int UPDATE_START = 1;
	private final static int UPDATE_LOADING = 2;
	private final static int UPDATE_FAILURE = 3;
	private final static int UPDATE_SUCCESS = 4;

	private void postResult(Object... object){
		Message msg=handler.obtainMessage(this.MESSAGE_POST_PROGRESS, new AsyncTaskResult<Object>(object));
		msg.sendToTarget();
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		// TODO Auto-generated method stub
		switch(Integer.valueOf(values[0].toString())){
		case UPDATE_START:
			this.state=State.STARTED;
			if(callback!=null)this.callback.onStart();
			break;
		case UPDATE_LOADING:
			this.state=State.LOADING;
			if(callback!=null)this.callback.onDownloading(
					(Long)values[1], 
					(Long)values[2]);
			break;
		case UPDATE_FAILURE:
			this.state=State.FAILURE;
			if(callback!=null)this.callback.onFailed((Exception)(values[1]));
			break;
		case UPDATE_SUCCESS:
			this.state=State.SUCCESS;
			if(callback!=null)this.callback.onSuccessed((File)values[1]);
			break;
		}
	}

}
