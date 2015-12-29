package com.example.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DownloadManager {
	
	Map<String,DownloadAsyncTask<?>>tasks=new HashMap<String,DownloadAsyncTask<?>>();
	private static final int DEFAULT_POOL_SIZE = 3;
	private final static Executor EXECUTOR = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	
	private static DownloadManager DOWNLOAD_MANAGER;
    public static DownloadManager getDownloadManager() {
        if (DOWNLOAD_MANAGER == null) {
            DOWNLOAD_MANAGER = new DownloadManager();
        }
        return DOWNLOAD_MANAGER;
    }
    /**
     * 开始下载
     * @param url
     * @param path
     * @param callback
     */
	public void startDownload(String url,String path,DownloadCallback<File> callback){
		//stopDownload(url);
		if(tasks.get(url)!=null){
			tasks.get(url).startDownload();
		}else{
			DownloadAsyncTask<?> newTask=new FileDownloadAsyncTask(EXECUTOR,url,path,callback);
			newTask.startDownload();
			tasks.put(url, newTask);
		}
	}
	/**
	 * 暂停下载
	 * @param url
	 */
	public void stopDownload(String url){
		if(tasks.get(url)!=null){
			tasks.get(url).stopDownload();
		}
	}
	/**
	 * 移除任务
	 * @param url
	 */
	public void removeDownload(String url){
		if(tasks.get(url)!=null){
			tasks.get(url).stopDownload();
			tasks.remove(url);
		}
	}
}
