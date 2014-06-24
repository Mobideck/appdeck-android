package com.mobideck.android.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
//import android.widget.RelativeLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class SlidingMenuFixed extends SlidingMenu {

	private static final String TAG = "SlidingMenuFixed";	
	
	public SlidingMenuFixed(Context context) {
		super(context);
	}
	
	//@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void manageLayers(float percentOpen) {
		/*
		
		if (Build.VERSION.SDK_INT < 11) return;

		boolean layer = percentOpen > 0.0f && percentOpen < 1.0f;
		final int layerType = layer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

		if (true)
			return;
		
		if (layerType != getContent().getLayerType()) {
			mHandler.post(new Runnable() {
				public void run() {
					Log.v(TAG, "changing layerType. hardware? " + (layerType == View.LAYER_TYPE_HARDWARE));
					getContent().setLayerType(layerType, null);
					getMenu().setLayerType(layerType, null);
					if (getSecondaryMenu() != null) {
						getSecondaryMenu().setLayerType(layerType, null);
					}
				}
			});
		}*/
	}	

	private Handler mHandler = new Handler();	
	
	//@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("InlinedApi")
	public void forceLayerType(final int layerType) {
		if (Build.VERSION.SDK_INT < 11) return;
		
		if (layerType != getContent().getLayerType()) {
			mHandler.post(new Runnable() {
				@SuppressLint("InlinedApi")
				public void run() {
					Log.v(TAG, "changing layerType. hardware? " + (layerType == View.LAYER_TYPE_HARDWARE));
					//getContent().setLayerType(layerType, null);
					getContent().setLayerType(View.LAYER_TYPE_HARDWARE, null);
					
					if (getMenu() != null)
						getMenu().setLayerType(View.LAYER_TYPE_NONE, null);
					if (getSecondaryMenu() != null)
						getSecondaryMenu().setLayerType(View.LAYER_TYPE_NONE, null);
					
					/*getMenu().setLayerType(layerType, null);
					if (getSecondaryMenu() != null) {
						getSecondaryMenu().setLayerType(layerType, null);
					}*/
				}
			});
		}
	}	
	
	
	public void setSideNavigationWidth(int i)
	{
		//RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams)mViewBehind.getLayoutParams());
	}
	
/*
	public void setSecondaryNavigationWidth(int i)
	{
		RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams)mViewBehind.getSecondaryContent()
		params.width = i;
	}
*/
}
