package com.mobideck.appdeck;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.mobideck.android.support.DatePickerDialogCustom;
import com.mobideck.appdeck.CacheManager.CacheResult;

/*import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorListenerAdapter;
import com.actionbarsherlock.internal.nineoldandroids.view.animation.AnimatorProxy;*/
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;

import com.mobideck.appdeck.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
//import com.nineoldandroids.animation.*;
import com.nineoldandroids.animation.ObjectAnimator;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/*
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
*/

public class PageFragmentSwap extends AppDeckFragment /*implements OnRefreshListener*/ {
	
	public static final String TAG = "PageFragmentSwap";	
	
	public PageSwipe pageSwipe;
	
//	private SmartWebView pageWebView;
///	private SmartWebView pageWebViewAlt;

	private XSmartWebView pageWebView;
	private XSmartWebView pageWebViewAlt;
	
//	private PullToRefreshLayout mPullToRefreshLayout;
	private SwipeRefreshLayout swipeView;
	private SwipeRefreshLayout swipeViewAlt;
	
	private long lastUrlLoad = 0;
	
	private FrameLayout wv_container;
	
	View adview;
	
	//public String url;
	public URI uri;
	
	private boolean shouldAutoReloadInbackground;
	
	public static PageFragmentSwap newInstance(String absoluteURL)
	{
		PageFragmentSwap fragment = new PageFragmentSwap();

		Bundle args = new Bundle();
	    args.putString("absoluteURL", absoluteURL);
	    fragment.setArguments(args);
	    fragment.currentPageUrl = absoluteURL;
	    //fragment.setRetainInstance(true);
	    
	    return fragment;
	}	
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		previousPageUrl = currentPageUrl = nextPageUrl = "";
		this.loader = (Loader)activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		this.appDeck = this.loader.appDeck;
    	currentPageUrl = getArguments().getString("absoluteURL");
    	this.screenConfiguration = this.appDeck.config.getConfiguration(currentPageUrl);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        rootView = (FrameLayout)inflater.inflate(R.layout.page_fragment_swap, container, false);
        rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
		//pageWebView = new SmartWebView(this);
		pageWebView = new XSmartWebView(this);

    	//pageWebViewAlt = new SmartWebView(this);
    	pageWebViewAlt = new XSmartWebView(this);
    	
		/*FrameLayout.LayoutParams webviewParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		webviewParams.gravity = Gravity.TOP | Gravity.CENTER; 
		webviewParams.weight = 1;*/		
		//rootView.addView(pageWebView);//, webviewParams);
        
		//currentPageUrl = "";
		
		mAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
		
		/*
		
		  // Now find the PullToRefreshLayout and set it up
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        
        mPullToRefreshLayout.addView(pageWebView);//, webviewParams);
    	mPullToRefreshLayout.addView(pageWebViewAlt);
        
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    	{
	        ActionBarPullToRefresh.from(this.loader)
	                .allChildrenArePullable()
	                .listener(this)
	                .setup(mPullToRefreshLayout);		
    	}
		*/
		
        swipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipeView.setColorScheme(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        	
        	@Override
            public void onRefresh() {
                swipeViewAlt.setRefreshing(true);
                swipeView.setRefreshing(true);
                reloadInBackground();
            }
        });
        swipeView.addView(pageWebView);
        
        swipeViewAlt = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeAlt);
        swipeViewAlt.setColorScheme(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        swipeViewAlt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeViewAlt.setRefreshing(true);
                swipeView.setRefreshing(true);
                reloadInBackground();
            }
        });
        swipeViewAlt.addView(pageWebViewAlt);
        swipeViewAlt.setVisibility(View.GONE);
        
        rootView.bringChildToFront(swipeView);
        //swipeView.addView(wv_container);
        		
        if (savedInstanceState != null)
        {
        	Log.i(TAG, "onCreateView with State");
        	loader = (Loader)getActivity();
    		try {
    			uri = new URI(currentPageUrl);
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		}    		
    		loadURLConfiguration(currentPageUrl);
    		menuItems = screenConfiguration.getDefaultPageMenuItems(uri, this);
    		this.loader.setMenuItems(menuItems);
        	pageWebView.restoreState(savedInstanceState);
        } else {
        	loadPage(currentPageUrl);
        }
		
        return rootView;
    }    
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    }

    @Override
    public void onResume() {
    	super.onResume();
    	CookieSyncManager.getInstance().stopSync();
    	pageWebView.resume();
    	pageWebViewAlt.resume();
    	/*if (adview != null)
    		adview.resume();*/

    	long now = System.currentTimeMillis();
    	if (screenConfiguration != null && screenConfiguration.ttl > 0 && lastUrlLoad != 0)
    	{
			if (screenConfiguration.ttl > ((now - lastUrlLoad) / 1000))
			{
				Log.v(TAG, "Should NOT AutoRealod SCREEN:["+screenConfiguration.title+"] ttl: "+screenConfiguration.ttl + " cache ttl: "+lastUrlLoad + " now: " + now + " diff: " + (now - lastUrlLoad)/1000);				
			} else {
				Log.v(TAG, "Should AutoRealod SCREEN:["+screenConfiguration.title+"] ttl: "+screenConfiguration.ttl + " cache ttl: "+lastUrlLoad + " now: " + now + "diff: " + (now - lastUrlLoad)/1000);
				reloadInBackground();
			}
		} else {
			Log.v(TAG, "AutoRealod DISABLED :["+screenConfiguration.title+"] ttl: "+screenConfiguration.ttl + " cache ttl: "+lastUrlLoad + " now: " + now + "diff: " + (now - lastUrlLoad)/1000);
		}
    	
    };
    
    @Override
    public void onPause() {
    	super.onPause();
    	CookieSyncManager.getInstance().sync();
    	pageWebView.pause();
    	pageWebViewAlt.pause();
    	/*if (adview != null)
    		adview.pause();*/
    };

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	if (pageWebView != null)
    		pageWebView.saveState(outState);
    }
    
    
    
    @Override
    public void onDestroyView()
    {
    	super.onDestroyView();
    }
    
    @Override
    public void onDestroy()
    {
    	if (pageWebView != null)
    		pageWebView.clean();
    	if (pageWebViewAlt != null)
    		pageWebViewAlt.clean();
    	super.onDestroy();
    }
    
    @Override
    public void onDetach ()
    {
    	super.onDetach();
    }

    /*
    @Override
    public void onRefreshStarted(View view) {
          // TODO Auto-generated method stub

          //////This method will Automatically call when
          /////pull to refresh event occurs

    	reloadInBackground();
    } */   
    
    public void loadUrl(String absoluteURL)
    {
		if (absoluteURL.startsWith("javascript:"))
		{
			pageWebView.loadUrl(absoluteURL);
			return;
		}
		
		if (absoluteURL.startsWith("appdeckapi:refresh"))
		{
			reloadInBackground();
			return;
		}
		if (screenConfiguration.isRelated(absoluteURL))
    	{
			loader.replacePage(absoluteURL);
			return;
    	}		
		super.loadUrl(absoluteURL);
    }
    
	public void loadPage(String absoluteUrl)
	{		
		/*
		Cursor mCur = getActivity().managedQuery(Browser.BOOKMARKS_URI,
                Browser.HISTORY_PROJECTION, null, null, null);
        if (mCur.moveToFirst()) {
            while (mCur.isAfterLast() == false) {
                Log.v("titleIdx", mCur
                        .getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
                Log.v("urlIdx", mCur
                        .getString(Browser.HISTORY_PROJECTION_URL_INDEX));
                mCur.moveToNext();
            }
        }		
		
		*/
		
		currentPageUrl = absoluteUrl;
		try {
			uri = new URI(currentPageUrl);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		loadURLConfiguration(absoluteUrl);
		
		menuItems = screenConfiguration.getDefaultPageMenuItems(uri, this);			
		
		Log.v(TAG, "SCREEN: "+screenConfiguration.title+" TTL: "+screenConfiguration.ttl);
		
		// BEGIN TEST
		
/*		if (true)
		{
			pageWebView.setForceCache(true);			
			shouldAutoReloadInbackground = true;
			pageWebView.loadUrl(absoluteUrl);
			loader.invalidateOptionsMenu();
			return;
		}		*/
		
		progressStart(pageWebView);
		
		// does page is in cache ?
		CacheManager.CacheResult cacheResult = appDeck.cache.isInCache(currentPageUrl);		
		
		boolean loadFromCache = false;
		boolean reloadInBackground = false;
		
		if (cacheResult.isInCache)
		{
			long now = System.currentTimeMillis();					
			
			if (screenConfiguration.ttl > ((now - cacheResult.lastModified) / 1000))
			{
				Log.v(TAG, "Cache HIT SCREEN:["+screenConfiguration.title+"] ttl: "+screenConfiguration.ttl + " cache ttl: "+cacheResult.lastModified + " now: " + now + " diff: " + (now - cacheResult.lastModified)/1000);				
				//pageWebView.setForceCache(true);
				loadFromCache = true;
			} else {
				Log.v(TAG, "Cache HIT DEPRECATED SCREEN:["+screenConfiguration.title+"] ttl: "+screenConfiguration.ttl + " cache ttl: "+cacheResult.lastModified + " now: " + now + "diff: " + (now - cacheResult.lastModified)/1000);
				loadFromCache = true;
				reloadInBackground = true;
			}
		} else {
			Log.v("CACHE", "Cache MISS SCREEN:["+screenConfiguration.title+"] ttl: "+screenConfiguration.ttl + " page IS NOT IN CACHE");
		}
		
		/*
		if (cacheResult.isInCache && !appDeck.isLowSystem)
		{
			if (cacheResult.lastModified + screenConfiguration.ttl < System.currentTimeMillis())
				reloadInBackground = true;
		}*/
		
		if (loadFromCache)
		{
			pageWebView.setForceCache(true);			
			if (reloadInBackground /*&& false*/)
				shouldAutoReloadInbackground = true;
			//String data = appDeck.cache.getCachedData(absoluteUrl);
			//pageWebView.loadDataWithBaseURL(absoluteUrl, data, "text/html", "UTF-8", null);
			//pageWebView.loadUrl(absoluteUrl);
		}
		pageWebView.loadUrl(absoluteUrl);
		lastUrlLoad = System.currentTimeMillis();
		loader.invalidateOptionsMenu();
	}
	
	private boolean reloadInProgress = false;
    public void reloadInBackground()
    {    	
    	if (reloadInProgress)
    		return;
    	if (appDeck.isLowSystem)
    	{
    		loadPage(currentPageUrl);
    		return;
    	}
    	reloadInProgress = true;

    	pageWebView.stopLoading();
    	pageWebViewAlt.stopLoading();
    	pageWebViewAlt.setForceCache(false);
    	
    	rootView.bringChildToFront(swipeView);
    	if (adview != null)
    		rootView.bringChildToFront(adview);
//    	swipeViewAlt.setVisibility(View.VISIBLE);
    	
    	//page_layout_alt.removeAllViews();
    	//etSupportProgressBarIndeterminateVisibility(true);
    	//pageWebViewAlt.stopLoading();
    	progressStart(pageWebViewAlt);
    	pageWebViewAlt.loadUrl(currentPageUrl);
		lastUrlLoad = System.currentTimeMillis();
    }
    
	@Override
	public void reload()
	{
		super.reload();
		reloadInBackground();		
	}    
	
    private int mAnimationDuration;
    private boolean swapInProgress = false;
    
    public void swapWebView()
    {
    	if (swapInProgress)
    		return;

/*		swipeView.removeView(pageWebView);
		rootView.addView(pageWebView);
		rootView.removeView(pageWebViewAlt);
    	swipeView.addView(pageWebViewAlt);*/    	
    	
    	swapInProgress = true;

    	pageWebView.touchDisabled = true;
    	pageWebViewAlt.touchDisabled = true;
    	pageWebViewAlt.setVerticalScrollBarEnabled(false);
    	
    	pageWebView.copyScrollTo(pageWebViewAlt);
    	swipeViewAlt.setAlpha(0f);
    	swipeViewAlt.setVisibility(View.VISIBLE);
    	rootView.bringChildToFront(swipeViewAlt);
    	if (adview != null)
    		rootView.bringChildToFront(adview);
    	
    	final Runnable r = new Runnable()
    	{
    	    public void run() 
    	    {
    	        // Animate the content view to 100% opacity, and clear any animation
    	        // listener set on the view.
    	    	animate(swipeViewAlt)
    	                .alpha(1f)
    	                .setDuration(250)
    	                .setListener(new AnimatorListenerAdapter() {
    	                    @Override
    	                    public void onAnimationEnd(Animator animation) {
    	                    	swipeView.setVisibility(View.GONE);
    	            			pageWebView.stopLoading();
    	            			
    	            			pageWebView.touchDisabled = false;
    	            	    	pageWebViewAlt.touchDisabled = false;
    	            	    	pageWebViewAlt.setVerticalScrollBarEnabled(true);
    	            	    	
    	            	    	// swap webview and layout
    	            	    	//SmartWebView tmpWebView = pageWebView;
    	            	    	/*XSmartWebView tmpWebView = pageWebView;
    	            	    	pageWebView = pageWebViewAlt;
    	            	    	pageWebViewAlt = tmpWebView;*/
    	            	    	
    	            	    	SwipeRefreshLayout tmp = swipeView;
    	            	    	swipeView = swipeViewAlt;
    	            	    	swipeViewAlt = tmp;
    	            	    	
    	            	    	rootView.bringChildToFront(swipeView);
    	            	    	if (adview != null)
    	            	    		rootView.bringChildToFront(adview);
    	            	    	    	            	    	
    	            	    	swapInProgress = false;
    	            	    	reloadInProgress = false;    	            	   
    	                    }
    	                });    	    	
    	    }
    	};

    	
    	new Handler().postDelayed(r, 250);
    	
/*    	loader.getWindow()
    	.getDecorView()
    	.getHandler()
    	.postDelayed(r, 250);*/    	
    	
    }   
    
    public void progressStart(View origin)
    {
    	super.progressStart(origin);
    }
    
    public void progressSet(View origin, int percent)
    {
    	super.progressSet(origin, percent);
    	if (percent == 100)
    	{
//    		Log.i(TAG, "progress is 101");
    		if (origin == pageWebViewAlt)
    			swapWebView();
    	}
//    	else
//    	{
    		//if (shouldAutoReloadInbackground == false)
    			
//    	}
    }
    
    public void progressStop(View origin)
    {
    	super.progressStop(origin);
    	swipeView.setRefreshing(false);
    	swipeViewAlt.setRefreshing(false);
		if (origin == pageWebView && shouldAutoReloadInbackground == true)
		{
			Log.i(TAG, "+++ Reload In Background +++");
			shouldAutoReloadInbackground = false;
			reloadInBackground();
		} else {

//			Activity activity = getActivity();
//			if (activity != null)
//			{				
//				LinearLayout.LayoutParams adParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//				//LinearLayout.LayoutParams adParams = new LinearLayout.LayoutParams(320, 50);
//				adParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
//				adParams.weight = 0;
//				//adParams.addRule(LinearLayout.  ALIGN_PARENT_BOTTOM);
//				//adParams.addRule(LinearLayout.ALIGN_PARENT_CENTER);
//				adview = new MobclixMMABannerXLAdView(activity);
//				rootView.addView(adview, adParams);
//			}
					
		}
		
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    	{
			 // If the PullToRefreshAttacher is refreshing, make it as complete
	        //if (mPullToRefreshLayout != null && mPullToRefreshLayout.isRefreshing())
	        //{
	        //	mPullToRefreshLayout.setRefreshComplete();
	        //}
        }		
		
    }
    /*
    public void setProgress(View origin, boolean indeterminate, int percent)
    {
    	super.setProgress(origin, indeterminate, percent);
    	if (percent == 101 && origin == pageWebViewAlt)
    	{
    		Log.i("setProgress", "**101**");
    		swapWebView();
    	}
    }   */ 
    
	public boolean apiCall(final AppDeckApiCall call)
	{
		if (call.command.equalsIgnoreCase("load"))
		{
			Log.i("API", uri.getPath()+" **LOAD**");
						
			return true;
		}
		
		if (call.command.equalsIgnoreCase("ready"))
		{
			Log.i("API", uri.getPath()+" **READY**");
			
	    	if (call.smartWebView == pageWebViewAlt)
	        {	    		
	    		//swapWebView();
	        }
			return true;
		}
		 
		if (call.command.equalsIgnoreCase("inhistory"))
		{
			Log.i("API", uri.getPath()+" **IN HISTORY**");
			
			boolean isInCache = false;
			String relativeURL = call.input.getString("param");
			URI url = this.uri.resolve(relativeURL);
			if (url != null)
			{
				String absoluteURL = url.toString();
				CacheResult value = this.appDeck.cache.isInCache(absoluteURL);
				isInCache = value.isInCache;
			}
			Boolean result = new Boolean(isInCache);
			
			call.setResult(result);
			
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("menu"))
		{
			Log.i("API", uri.getPath()+" **MENU**");
			
			// menu entries
			AppDeckJsonArray entries = call.input.getArray("param");
			if (entries.length() > 0)
			{
				PageMenuItem defaultMenu[] = screenConfiguration.getDefaultPageMenuItems(uri, this); 
				menuItems = new PageMenuItem[entries.length() + defaultMenu.length];
				
				int i;
				for (i = 0; i < entries.length(); i++)
				{
					AppDeckJsonNode entry = entries.getNode(i);
					String title = entry.getString("title");
					String content = entry.getString("content");
					String icon = entry.getString("icon");
					String type = entry.getString("type");
					
			        //UIImage *iconImage = self.child.loader.conf.icon_action.image;
					PageMenuItem item = new PageMenuItem(title, icon, type, content, uri, this);
					menuItems[i] = item;
				}
				for (int j = 0; j < defaultMenu.length; j++, i++) {
					menuItems[i] = defaultMenu[j];
				}
				//appDeck.loader.invalidateOptionsMenu();
				if (isCurrentAppDeckPage())
					loader.setMenuItems(menuItems);
			} else {
				menuItems = screenConfiguration.getDefaultPageMenuItems(uri, this);
				loader.setMenuItems(menuItems);
			}
			
			return true;
		}
		
		if (call.command.equalsIgnoreCase("previousnext"))
		{
			Log.i("API", uri.getPath()+" **PREVIOUSNEXT**");
			
			previousPageUrl = call.param.getString("previous_page");
			if (previousPageUrl.isEmpty() == false)
				previousPageUrl = uri.resolve(previousPageUrl).toString();
			nextPageUrl = call.param.getString("next_page");
			if (nextPageUrl.isEmpty() == false)
				nextPageUrl = uri.resolve(nextPageUrl).toString();			
			if (pageSwipe != null)
				pageSwipe.updatePreviousNext(this);
			
			return true;
		}
		
		if (call.command.equalsIgnoreCase("popover"))
		{
			Log.i("API", uri.getPath()+" **POPOVER**");

			String url = call.param.getString("url");
			
			if (url != null && !url.isEmpty())
			{
				loader.showPopOver(this, call);
			}
			
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("popup"))
		{
			Log.i("API", uri.getPath()+" **POPUP**");
			
			loader.showPopUp(this, call.input.getString("param"));
			
			return true;
		}
		
		if (call.command.equalsIgnoreCase("select"))
		{
			Log.i("API", uri.getPath()+" **SELECT**");
			
			call.postponeResult();
			
			String title = call.param.getString("title");
			AppDeckJsonArray values = call.param.getArray("values");
        	CharSequence[] items = new CharSequence[values.length()];
        	for (int i = 0; i < values.length(); i++) {
				items[i] = values.getString(i);
			}

        	AlertDialog.Builder builder = new AlertDialog.Builder(loader);
        	if (title != null && !title.isEmpty())
        		builder.setTitle(title);
        	builder.setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                        	call.sendPostponeResult(false);
                        }
                    });
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)
            	builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
				@Override
				public void onDismiss(DialogInterface dialog) {
					call.sendPostponeResult(false);
				}
			});        	
        	builder.setItems(items, new customDialogOnClickListener(call, items));
        	AlertDialog alert = builder.create();        	
        	//The above line didn't show the dialog i added this line:
        	alert.show();			
		}
		
		if (call.command.equalsIgnoreCase("selectdate"))
		{
			Log.i("API", uri.getPath()+" **SELECT DATE**");
			
			String title = call.param.getString("title");
			String year = call.param.getString("year");
			String month = call.param.getString("month");
			String day = call.param.getString("day");
			
			call.postponeResult();
			
		    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
		    	
		        @Override
		        public void onDateSet(DatePicker view, final int year, final int monthOfYear,
		                final int dayOfMonth) {
		        	
		        	Log.d("Date", "selected");
		        	 HashMap<String,String> result = new HashMap<String, String>() {
		        	     {
		        	      put("year", String.valueOf(year));
		        	      put("month", String.valueOf(monthOfYear + 1));
		        	      put("day", String.valueOf(dayOfMonth));
		        	     }
		        	 };
					call.setResult(result);
					call.sendPostponeResult(true);
		        }
		    };			
			
		    int yearValue = call.param.getInt("year");
		    int monthValue = call.param.getInt("month");
		    int dayValue = call.param.getInt("day");
		    Calendar cal = GregorianCalendar.getInstance();
		    cal.set(yearValue, monthValue - 1, dayValue);
		    //if (yearValue == 0)
		    	yearValue = cal.get(Calendar.YEAR);
		    //if (monthValue == 0)
		    	monthValue = cal.get(Calendar.MONTH);
		    //if (dayValue == 0)
		    	dayValue = cal.get(Calendar.DAY_OF_MONTH);
			final DatePickerDialogCustom datepicker = new DatePickerDialogCustom(loader, d, yearValue, monthValue, dayValue);
			  datepicker.setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                        	call.sendPostponeResult(false);
                        }
                    });
			  datepicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					call.sendPostponeResult(false);
				}
			});
			  
			  if (year.length() > 0)
				  datepicker.setYearEnabled(false);
			  if (month.length() > 0)
				  datepicker.setMonthEnabled(false);
			  if (day.length() > 0)
				  datepicker.setDayEnabled(false);			  
			  datepicker.setTitle(title);
			  datepicker.show();
		}
		
		return super.apiCall(call);
		
	}
	
	public class customDialogOnClickListener implements DialogInterface.OnClickListener
	{
		AppDeckApiCall call;
		CharSequence[] items;
		
		customDialogOnClickListener(AppDeckApiCall call, CharSequence[] items)
		{
			this.call = call;
			this.items = items;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String result = (String) items[which];
//			Toast.makeText(loader, result, Toast.LENGTH_SHORT).show();
			call.setResult(result);
			call.sendPostponeResult(true);
		}
		
	}
	
	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if (pageWebView != null)
		{
			if (hidden)
				pageWebView.pause();
			else
				pageWebView.resume();
		}
	}
	
}
