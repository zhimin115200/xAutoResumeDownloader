package com.example.download;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.file.FileUtil;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public abstract class DownloadAsyncTask<T>{

	static final String BASEPATH=Environment.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "MyDownLoad"
			+ File.separator;
	static final String EXTNAME=".tmp";//;

	protected static final int MESSAGE_POST_RESULT = 0;
	protected static final int MESSAGE_POST_PROGRESS = 1;

	//	private final static int UPDATE_START = 1;
	//	private final static int UPDATE_LOADING = 2;
	//	private final static int UPDATE_FAILURE = 3;
	//	private final static int UPDATE_SUCCESS = 4;

	String downloadPath=null;
	String downloadUrl=null;
	DownloadCallback<T> callback=null;
	Executor exec;

	Callable<T> mWorker;
	FutureTask<T> mFuture;
	private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

	State state = State.WAITING;
	public DownloadAsyncTask(Executor exec,String url){
		this.exec=exec;
		downloadUrl=url;
		downloadPath=BASEPATH+FileUtil.getFileName(url)+EXTNAME;
	}
	public DownloadAsyncTask(Executor exec,String url,String path){
		this.exec=exec;
		downloadUrl=url;
		downloadPath=path!=null?path:BASEPATH+FileUtil.getFileName(url)+EXTNAME;
	}
	public DownloadAsyncTask(Executor exec,String url,String path,DownloadCallback<T> callback){
		this.exec=exec;
		downloadUrl=url;
		downloadPath=path!=null?path:BASEPATH+FileUtil.getFileName(url)+EXTNAME;
		this.callback=callback;
	}
	private void init(){
		mWorker=new Callable<T>(){
			@Override
			public T call() throws Exception {
				// TODO Auto-generated method stub
				mTaskInvoked.set(true);
				return downLoad();
			}};
			mFuture = new FutureTask<T>(mWorker) {
				@Override
				protected void done() {
					//post结束结果
					final boolean wasTaskInvoked = mTaskInvoked.get();
					if (!wasTaskInvoked) {
						Message msg;
						try {
							msg = handler.obtainMessage(MESSAGE_POST_RESULT, mFuture.get());
							msg.sendToTarget();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};
	}
	public void startDownload(){
		init();
		this.exec.execute(mFuture);
	}
	public void deleteTmpFile(){
		File file = new File(downloadPath);
		if(file.exists()){
			file.delete();
		}
	}
	/**
	 * 取消任务，断开网络连接，关闭io
	 */
	public void stopDownload(){
		if(mFuture!=null)
			mFuture.cancel(true);
		state=State.CANCELLED;
		if (callback != null) {
			callback.onCancelled();
		}
	}
	/**
	 * 下载成功或修改文件名
	 */
	protected void downloadSuccessed(){
		File file = new File(downloadPath);
		if(file.exists()){
			file.renameTo(new File(downloadPath.substring(0,downloadPath.length()-EXTNAME.length())));
		}
	}
	protected abstract T downLoad() ;
	protected abstract void onProgressUpdate(Object... values) ;
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			AsyncTaskResult<?> result = (AsyncTaskResult<?>) msg.obj;
			switch (msg.what) {
			case MESSAGE_POST_RESULT:
				// There is only one result
				stopDownload();
				break;
			case MESSAGE_POST_PROGRESS:
				//result.mTask.onProgressUpdate(result.mData);
				onProgressUpdate(result.mData);
				break;
			}

		}

	};
	@SuppressWarnings({"RawUseOfParameterizedType"})
	static class AsyncTaskResult<Data> {
		final Data[] mData;

		public AsyncTaskResult( Data... data) {
			mData = data;
		}
	}
	public enum State {
		WAITING(0), STARTED(1), LOADING(2), FAILURE(3), CANCELLED(4), SUCCESS(5);
		private int value = 0;

		State(int value) {
			this.value = value;
		}

		public static State valueOf(int value) {
			switch (value) {
			case 0:
				return WAITING;
			case 1:
				return STARTED;
			case 2:
				return LOADING;
			case 3:
				return FAILURE;
			case 4:
				return CANCELLED;
			case 5:
				return SUCCESS;
			default:
				return FAILURE;
			}
		}

		public int value() {
			return this.value;
		}
	}
}
