package com.example.download;

/**
 * 下载回调
 * @author zhimin115200
 *
 * @param <T>
 */
public interface DownloadCallback<T> {
	public void onStart();
	public void onDownloading(Long total,Long current);
	public void onSuccessed(T file);
	public void onFailed(Exception e);
	public void onCancelled();
}
