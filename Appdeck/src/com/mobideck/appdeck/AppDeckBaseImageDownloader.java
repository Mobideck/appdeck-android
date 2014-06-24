package com.mobideck.appdeck;

import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

public class AppDeckBaseImageDownloader extends BaseImageDownloader {

	public AppDeckBaseImageDownloader(Context context) {
		super(context);
	}
	public AppDeckBaseImageDownloader(Context context, int connectTimeout, int readTimeout) {
		super(context, connectTimeout, readTimeout);
	}

	@Override
	public InputStream getStream(String imageUri, Object extra) throws IOException {

		// try from resources first
		InputStream stream = AppDeck.getInstance().cache.getEmbedResourceStream(imageUri);
		if (stream != null)
			return stream;
		
		return super.getStream(imageUri, extra);
	}
}
