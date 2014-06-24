package com.mobideck.appdeck;

import com.mobideck.appdeck.R;
import com.crashlytics.android.Crashlytics;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.TransportProtocol;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnDragListener;
import android.webkit.WebView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mobideck.android.support.SlidingMenuFixed;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class Loader extends SherlockFragmentActivity {

	public final static String TAG = "LOADER";
	public final static String JSON_URL = "com.mobideck.appdeck.JSON_URL";
	
	public final static String POP_UP_URL = "com.mobideck.appdeck.POP_UP_URL";
	public final static String PAGE_URL = "com.mobideck.appdeck.URL";
	public final static String ROOT_PAGE_URL = "com.mobideck.appdeck.ROOT_URL";
	
	/* push */
	public final static String PUSH_URL = "com.mobideck.appdeck.PUSH_URL";
	public final static String PUSH_TITLE = "com.mobideck.appdeck.PUSH_TITLE";
	
	public String proxyHost;
	public int proxyPort;
	
	protected AppDeck appDeck;
	
	private WebView leftMenuWebView;
	private WebView rightMenuWebView;
	
	public SlidingMenuFixed slidingMenu;
	
	//private Loader self;
	
	//public int actionBarHeight;
	
	private PageMenuItem[] menuItems;
	
	//private List<AppDeckFragment> pages;
	
	//private jProxy jp;
	private HttpProxyServerBootstrap proxyServerBootstrap;
	
	@SuppressWarnings("unused")
	private GoogleCloudMessagingHelper gcmHelper;
	
	private int mProgress = 100;
	private int mTargetProgress = 0;
	
    Handler mHandler = new Handler();
    Runnable mProgressRunner = new Runnable() {
        @Override
        public void run() {
                    	
        	if (mProgress < mTargetProgress)
        		mProgress += 5;
        	
            //Normalize our progress along the progress bar's scale
            int progress = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * mProgress;
            //setSupportProgressBarIndeterminate(true);// ProgressBarIndeterminate
            setSupportProgress(progress);
            //setSupportSecondaryProgress((Window.PROGRESS_END - Window.PROGRESS_START) / 100 * 75);
            if (mProgress < 100) {
                mHandler.postDelayed(mProgressRunner, 100);
            }
        }
    };	
	
    
    
    
    protected void onCreatePass(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		//Debug.startMethodTracing("calc");
		Crashlytics.start(this);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		
		Intent intent = getIntent();
        String app_json_url = intent.getStringExtra(JSON_URL);
        appDeck = new AppDeck(getBaseContext(), app_json_url);
    	super.onCreate(savedInstanceState);

    	/*
    	APL.setup(getApplicationContext());

    	ProxyConfiguration currentProxy = null;
    	try {
    		URI uri = URI.create("http://www.google.com");
    		ProxyConfiguration proxyConf = APL.getCurrentProxyConfiguration(uri);
    		String result = ProxyUtils.getURI(uri, proxyConf.getProxy(), 10);    		
    		currentProxy = APL.getCurrentProxyConfiguration(appDeck.config.app_base_url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	*/
    	
    	this.proxyHost = "127.0.0.1";
    	
    	boolean isAvailable = false;
    	this.proxyPort = 8081; // default port
    	do
    	{
    		isAvailable = Utils.isPortAvailable(this.proxyPort);
    		if (isAvailable == false)
    			this.proxyPort = Utils.randInt(10000, 60000);	
    	}
    	while (isAvailable == false);
    	
    	Log.i(TAG, "filter registered at @"+this.proxyPort);
    	
    	CacheFiltersSource filtersSource = new CacheFiltersSource();
    	
    	proxyServerBootstrap = DefaultHttpProxyServer
                .bootstrap()
                .withPort(this.proxyPort)
                .withAllowLocalOnly(true)
                .withTransportProtocol(TransportProtocol.TCP)
                .withFiltersSource(filtersSource);    	
    	
/*    	if (currentProxy != null)
    		jp = new jProxy(8081, currentProxy.getProxyHost(), currentProxy.getProxyPort(), 20);
    	else
    		jp = new jProxy(8081, "", 0, 20);
		jp.setDebug(0, new LogPrintStream("JProxy", 1));		// or set the debug level to 2 for tons of output
		jp.start();*/
    	
    	proxyServerBootstrap.start();
    	
		try {
			WebkitProxy.setProxy("AppDeckApplication", this, this.proxyHost, this.proxyPort);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
        // change the way keyboard is hidden
    	//InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
    	//inputManager.toggleSoftInput (InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
    	
    	//this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_);
		setContentView(R.layout.loader);   	
		
        // Sliding Menu
        slidingMenu = new SlidingMenuFixed(this);
        if (appDeck.config.leftMenuUrl != null && appDeck.config.rightMenuUrl != null)
        	slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        else if (appDeck.config.leftMenuUrl != null)
        	slidingMenu.setMode(SlidingMenu.LEFT);
        else if (appDeck.config.rightMenuUrl != null)
        	slidingMenu.setMode(SlidingMenu.RIGHT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.slidingmenu_shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);      
        slidingMenu.setSecondaryShadowDrawable(R.drawable.slidingmenu_shadow_right);        
        
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW/* | SlidingMenu.SLIDING_CONTENT*/);
        //slidingMenu.setBehindWidth(150);
        
        //slidingMenu.setMenu(R.layout.slidingmenu);
        //slidingMenu.setBackgroundDrawable(appDeck.config.app_background_color.getDrawable());
        
        if (appDeck.config.leftMenuUrl != null) {
        	leftMenuWebView = new PageWebViewMenu(this, appDeck.config.leftMenuUrl.toString(), PageWebViewMenu.POSITION_LEFT);
        	if (appDeck.config.leftmenu_background_color != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        		leftMenuWebView.setBackground(appDeck.config.leftmenu_background_color.getDrawable());
        	slidingMenu.setMenu(leftMenuWebView);
        }
        if (appDeck.config.rightMenuUrl != null) {
        	rightMenuWebView = new PageWebViewMenu(this, appDeck.config.rightMenuUrl.toString(), PageWebViewMenu.POSITION_RIGHT);
        	if (appDeck.config.rightmenu_background_color != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        		rightMenuWebView.setBackground(appDeck.config.rightmenu_background_color.getDrawable());
        	
        	/*FrameLayout layout = new FrameLayout(this);
            FrameLayout.LayoutParams layoutparams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,Gravity.TOP|Gravity.RIGHT);             
        	layout.addView(rightMenuWebView);*/

            slidingMenu.setSecondaryMenu(rightMenuWebView);
        }
        
   		android.os.Process.setThreadPriority(-20);       
                
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
        	slidingMenu.setOnClosedListener(new OnClosedListener() {
				@Override
				public void onClosed() {
					Log.i("MENU", "onClosed");
					setEnableHardwareAcceleration(true);
				}
			});
	        slidingMenu.setOnOpenListener(new OnOpenListener() {				
				@Override
				public void onOpen() {
					Log.i("MENU", "onOpen");
					setEnableHardwareAcceleration(false);
				}
			});
	        slidingMenu.setOnOpenedListener(new OnOpenedListener() {				
				@Override
				public void onOpened() {
					Log.i("MENU", "onOpened");
					setEnableHardwareAcceleration(false);
				}
			});	        
	        slidingMenu.setOnDragListener(new OnDragListener() {
				@Override
				public boolean onDrag(View v, DragEvent event) {
					Log.i("MENU", "onDrag");
					if (event.getAction() == DragEvent.ACTION_DRAG_STARTED)
						setEnableHardwareAcceleration(false);
					return false;
				}
			});
        }

        // configure action bar
        appDeck.actionBarHeight = getActionBarHeight();
        
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // icon on the left of logo 
        getSupportActionBar().setDisplayShowHomeEnabled(true); // make icon + logo + title clickable       
        //getSupportActionBar().setDisplayUseLogoEnabled(true);
        
		if (appDeck.config.topbar_color != null)
			getSupportActionBar().setBackgroundDrawable(appDeck.config.topbar_color.getDrawable());        

		if (appDeck.config.title != null)
			getSupportActionBar().setTitle(appDeck.config.title);
		
		setSupportProgressBarVisibility(false);
		setSupportProgressBarIndeterminate(false);
		
		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener()
        {
            public void onBackStackChanged() 
            {                   
            	AppDeckFragment fragment = getCurrentAppDeckFragment();
                
                if (fragment != null)
                {
                	fragment.setIsMain(true);
                }          
            }
        });
		
		initUI();
		
		gcmHelper = new GoogleCloudMessagingHelper(getBaseContext());
		
		if (savedInstanceState == null)
		{
			loadRootPage(appDeck.config.bootstrapUrl.toString());
			//loadRootPage("http://testapp.appdeck.mobi/kitchensink_popover.php");
		}
		//MobclixFullScreenAdView adview = new MobclixFullScreenAdView(this);
		//adview.requestAndDisplayAd();		

    }
    
    boolean isForeground = true;
    @Override
    protected void onResume()
    {
    	super.onResume();
    	isForeground = true;
    }

    @Override
    protected void onPause()
    {
    	isForeground = false;
    	super.onPause();
    	if (appDeck.noCache)
    		Utils.killApp(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");    	
    	super.onSaveInstanceState(outState);
    	Log.i(TAG, "onSaveInstanceState");
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
      super.onRestoreInstanceState(savedInstanceState);
      Log.i(TAG, "onRestoreInstanceState");
    }
    
    @Override
    protected void onDestroy()
    {        
    	isForeground = false;
    	super.onDestroy();
    }    
    
    @SuppressWarnings("deprecation")
	public void initUI()
    {
    	// for smartphone
    	Display display = getWindowManager().getDefaultDisplay();
    	float width = (float)display.getWidth();
    	float height = (float)display.getHeight();
    	float screen_width = (width > height ? height : width);
    	float virtual_menu_width = appDeck.config.leftMenuWidth;
    	if (virtual_menu_width > 280)
    		virtual_menu_width = 280;
    	if (virtual_menu_width < 0)
    		virtual_menu_width = 0;
    	float menu_width = 0;
    	if (appDeck.isTablet)
    	{
    		float base_width = getResources().getDimension(R.dimen.slidingmenu_base_width);    		
    		menu_width = virtual_menu_width * base_width / 280;
    	} else {
    		menu_width = screen_width * virtual_menu_width / (appDeck.isTablet ? 768 : 320);
    	}
    	
    	//float density = getResources().getDisplayMetrics().density;
    	//float width = density *  menu_width;
    	Log.d("Loader", "virtual menu: " + appDeck.config.leftMenuWidth);
    	Log.d("Loader", "screen_width: " + screen_width);
    	Log.d("Loader", "menu width: " + menu_width);
    	
    	slidingMenu.setSideNavigationWidth((int)menu_width);
    	
    	//slidingMenu.setBehindWidth((int)menu_width);
    	
    	/*    	Configuration config = getResources().getConfiguration();
    	int size = config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

    	// set width of side navigation according to screen layout size

    	if (size == Configuration.SCREENLAYOUT_SIZE_SMALL)
    	{
    		slidingMenu.setBehindOffset(60);
    	}

    	if (size == Configuration.SCREENLAYOUT_SIZE_NORMAL)
    	{
    		slidingMenu.setBehindOffset(60);
    	}

    	if (size == Configuration.SCREENLAYOUT_SIZE_LARGE)
    	{
    		slidingMenu.setSideNavigationWidth(350);
    	}

    	if (size == Configuration.SCREENLAYOUT_SIZE_XLARGE)
    	{
    		slidingMenu.setSideNavigationWidth(350);
    	}*/
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }
    
    
/*    public PageSwipe getCurrentPageSwipe()
    {
    	if (pages != null && pages.size() > 0)
    		return pages.get(pages.size() - 1);
    	return null;
    }*/
    
    ArrayList<WeakReference<AppDeckFragment>> fragList = new ArrayList<WeakReference<AppDeckFragment>>();
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void onAttachFragment (Fragment fragment) {
    	
    	if (fragment != null)
    	{
    		String tag = fragment.getTag();
    		if (tag != null && tag.equalsIgnoreCase("AppDeckFragment"))
    		{
    			fragList.add(new WeakReference((AppDeckFragment)fragment));
    		}
    	}
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onDettachFragment (Fragment fragment) {
    	ArrayList<WeakReference<AppDeckFragment>> newlist = new ArrayList<WeakReference<AppDeckFragment>>();    	
        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
            if (f != fragment) {
            	newlist.add(new WeakReference((AppDeckFragment)f));
            }
        }    	
        fragList = newlist;
    }
    
    public ArrayList<AppDeckFragment> getActiveFragments() {
        ArrayList<AppDeckFragment> ret = new ArrayList<AppDeckFragment>();
        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
            if(f != null) {
                //if(f.isActive()) {
                    ret.add(f);
                //}
            }
        }
        return ret;
    }    

    public AppDeckFragment getPreviousAppDeckFragment(AppDeckFragment current)
    {
    	AppDeckFragment previous = null;

        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
        	if (f == current)
        		return previous;
        	previous = f;
        }

        return null;
    }
    
    
    public AppDeckFragment getCurrentAppDeckFragment()
    {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	return (AppDeckFragment)fragmentManager.findFragmentByTag("AppDeckFragment");
    }
    
    public AppDeckFragment getRootAppDeckFragment()
    {
    	WeakReference<AppDeckFragment> ref = fragList.get(0);
        return ref.get();
    }
        
    
    public void progressStart()
    {
    	setSupportProgressBarIndeterminate(true);    	
    	mProgress = 100;
    }
    
    public void progressSet(int percent)
    {
    	setSupportProgressBarIndeterminate(false);
        //Normalize our progress along the progress bar's scale
        int progress = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * percent;
        //setSupportProgressBarIndeterminate(true);// ProgressBarIndeterminate
        setSupportProgress(progress);    	
    	/*
		if (mProgress == 100)
		{
	    	mProgress = 0;
	        mProgressRunner.run();
		}
		mTargetProgress = percent;*/
    }
    
    public void progressStop()
    {
    	setSupportProgressBarIndeterminate(false);
    	
        int progress = (Window.PROGRESS_END - Window.PROGRESS_START);
        //setSupportProgressBarIndeterminate(true);// ProgressBarIndeterminate
        setSupportProgress(progress);    	
    	
    	/*
    	mTargetProgress = 100;
    	mProgressRunner.run();*/
    }

    /*
    public void setProgress(View origin, Boolean indeterminate, int percent)
    {
    	if (indeterminate)
    	{
    		mProgress = 0;
	        mProgressRunner.run();    		
    		mTargetProgress = 10;
    		//setSupportProgressBarIndeterminateVisibility(true);
    		//setSupportProgressBarVisibility(false);
    	} else {
    		//setSupportProgressBarIndeterminateVisibility(false);
    		//setSupportProgressBarVisibility(true);
    		if (mProgress == 100)
    		{
    	    	mProgress = 0;
    	        mProgressRunner.run();
    		}
    		mTargetProgress = 10 + (percent / 10) * 9;
    	}
    }*/
    
    //private int backStackIdentifier = -1;
    
    protected void prepareRootPage()
    {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); 
    	
    	// remove all current menu items
    	setMenuItems(new PageMenuItem[0]);
    	// make sure user see content
    	if (slidingMenu != null)
    		slidingMenu.showContent();
  	
    }
    
    public boolean loadSpecialURL(String absoluteURL)
    {
		if (absoluteURL.startsWith("tel:"))
		{
			try{
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(absoluteURL));
				startActivity(intent);
			}catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		
		if (absoluteURL.startsWith("mailto:")){
			Intent i = new Intent(Intent.ACTION_SEND);  
			i.setType("message/rfc822") ;
			i.putExtra(Intent.EXTRA_EMAIL, new String[]{absoluteURL.substring("mailto:".length())});  
			startActivity(Intent.createChooser(i, ""));
			return true;
		}      	
    	return false;
    }
    
	public int findUnusedId(/*ViewGroup container, */int fID) {
	    while( this.findViewById(android.R.id.content).findViewById(++fID) != null );
	    return fID;
	}    
    
    public void loadRootPage(String absoluteURL)
    {
    	fragList = new ArrayList<WeakReference<AppDeckFragment>>();
    	// if we don't have focus get it before load page
    	if (isForeground == false)
    	{
    		createIntent(ROOT_PAGE_URL, absoluteURL);
    		return;
    	}
    	prepareRootPage();
    	if (loadSpecialURL(absoluteURL))
    		return;
		AppDeckFragment fragment = initPageFragment(absoluteURL);
    	pushFragment(fragment);
    }
    
    public int loadPage(String absoluteURL)
    {
    	if (loadSpecialURL(absoluteURL))
    		return -1;

    	
    	if (isForeground == false)
    	{
    		createIntent(PAGE_URL, absoluteURL);
    		return -1;
    	}		
		//absoluteURL = "http://www.play3-live.com/__appli_android/smartphones/left_menu.php";
		AppDeckFragment fragment = initPageFragment(absoluteURL);
    	
    	/*if (fragment.screenConfiguration != null && fragment.screenConfiguration.isPopUp)
    	{
       		//createIntent(POP_UP_URL, fragment.currentPageUrl);
    		showPopUp(null, absoluteURL);
       		return -1;
    	}*/
		
    	return pushFragment(fragment);

    }
    
    public int replacePage(String absoluteURL)
    {
		AppDeckFragment fragment = initPageFragment(absoluteURL);
    	
		fragment.enablePushAnimation = false;
		
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	    	
    	AppDeckFragment oldFragment = (AppDeckFragment)fragmentManager.findFragmentByTag("AppDeckFragment");
    	if (oldFragment != null)
    	{
    		oldFragment.setIsMain(false);
    		fragmentTransaction.remove(oldFragment);
    		onDettachFragment(oldFragment);
    	}
    	
    	fragmentTransaction.add(R.id.loader_container, fragment, "AppDeckFragment");
    	
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	return fragmentTransaction.commitAllowingStateLoss();    	

    }    

    public AppDeckFragment initPageFragment(String absoluteURL)
    {
    	ScreenConfiguration config = appDeck.config.getConfiguration(absoluteURL);
    	
    	if (config != null && config.type != null && config.type.equalsIgnoreCase("browser"))
    	{
    		WebBrowser fragment = WebBrowser.newInstance(absoluteURL);
    		//pageSwipe.setRetainInstance(true);
    		fragment.screenConfiguration = config; 
    		return fragment;
    	}
    	
    	PageSwipe pageSwipe = PageSwipe.newInstance(absoluteURL);
    	pageSwipe.loader = this;
    	pageSwipe.setRetainInstance(true);
    	pageSwipe.screenConfiguration = appDeck.config.getConfiguration(absoluteURL);
    	return pageSwipe;
    }
    
    public int pushFragment(AppDeckFragment fragment)
    {    	
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	//fragmentTransaction.setTransitionStyle(1);
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	//fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
    	
    	//fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
    	
    	//fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    	//fragmentTransaction.setCustomAnimations(R.anim.exit, R.anim.enter);
    	
    	AppDeckFragment oldFragment = getCurrentAppDeckFragment();
    	if (oldFragment != null)
    	{
    		oldFragment.setIsMain(false);

    		//fragmentTransaction.hide(oldFragment);
    		//fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    		//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	}    	
    	
    	
    	fragmentTransaction.add(R.id.loader_container, fragment, "AppDeckFragment");
    	//fragmentTransaction.replace(R.id.loader_container, fragment, "AppDeckFragment");
    	//fragmentTransaction.addToBackStack("AppDeckFragment");

    	fragmentTransaction.addToBackStack("AppDeckFragment");
    	
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	//fragmentTransaction.setTransitionStyle()
    	
    	//fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);
    	//Animations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
    	    	
    	fragmentTransaction.commitAllowingStateLoss();
    	
    	return 0;
    }

    public boolean pushFragmentAnimation(AppDeckFragment fragment)
    {
    	AppDeckFragment current = getCurrentAppDeckFragment();
    	AppDeckFragment previous = getPreviousAppDeckFragment(current);

    	if (current == null || previous == null)
    		return false;
    	
    	if (fragment != current)
    		return false;
    	
    	AppDeckFragmentPushAnimation anim = new AppDeckFragmentPushAnimation(previous, current);
    	anim.start();
    	
    	
    	return true;
    }
    
    public boolean popFragment()
    {
    	AppDeckFragment current = getCurrentAppDeckFragment();
    	AppDeckFragment previous = getPreviousAppDeckFragment(current);
    	
    	if (current == null || previous == null)
    		return false;
    	
    	onDettachFragment(current);    	
    	
    	if (current.enablePopAnimation)
    	{
    		AppDeckFragmentPopAnimation anim = new AppDeckFragmentPopAnimation(current, previous);
    		anim.start();
    	}
    	return true;
    }

    public boolean popRootFragment()
    {
    	//AppDeckFragment root = getRootAppDeckFragment();

    	FragmentManager fragmentManager = getSupportFragmentManager();
    	
    	fragmentManager.popBackStack();
    	
    	//todo: a faire
    	//fragmentManager.popBackStack(root, FragmentManager.POP_BACK_STACK_INCLUSIVE); 
    	
    	/*prepareRootPage();
    	pushFragment(root);*/    	
    	return true;
    }    
    
    public void reload()
    {
        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
        	f.reload();
        }
        if (leftMenuWebView != null)
        	leftMenuWebView.reload();
        if (rightMenuWebView != null)
        	rightMenuWebView.reload();
    }
    
    public Boolean apiCall(AppDeckApiCall call)
	{		
		if (call.command.equalsIgnoreCase("share"))
		{
			Log.i("API", "**SHARE**");
					
			String shareTitle = call.param.path("title").textValue();
			String shareUrl = call.param.path("url").textValue();;
			String shareImageUrl = call.param.path("imageurl").textValue();;

			if (call.appDeckFragment != null)
				call.appDeckFragment.loader.share(shareTitle, shareUrl, shareImageUrl);
			
			return true;
		}		

		if (call.command.equalsIgnoreCase("preferencesget"))
		{
			Log.i("API", "**PREFERENCES GET**");
					
			String name = call.param.path("name").textValue();
			JsonNode defaultValue = call.param.path("value");

		    SharedPreferences prefs = getSharedPreferences(AppDeckApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		    
		    String key = "appdeck_preferences_json1_" + name;
		    String finalValueJson = prefs.getString(key, null);
		    
		    if (finalValueJson == null)
		    	call.setResult(defaultValue);
		    else
		    {
				try {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode json = mapper.readValue(finalValueJson, JsonNode.class);
					call.setResult(json);	
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		    /*
		    if (registrationId.isEmpty()) {
		        Log.i(TAG, "Registration not found.");
		        return "";
		    }
		    
		    if (registrationId.isEmpty()) {		    
		    editor.putInt(PROPERTY_APP_VERSION, appVersion);
			
			if (call.appDeckFragment != null)
				call.appDeckFragment.loader.share(shareTitle, shareUrl, shareImageUrl);*/
			
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("preferencesset"))
		{
			Log.i("API", "**PREFERENCES SET**");
					
			String name = call.param.path("name").textValue();
			String finalValue = "";//call.param.path("value").toString();

			try {
				ObjectMapper mapper = new ObjectMapper();
				finalValue = mapper.writeValueAsString(call.param.path("value"));
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
		    SharedPreferences prefs = getSharedPreferences(AppDeckApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		    SharedPreferences.Editor editor = prefs.edit();
		    String key = "appdeck_preferences_json1_" + name;
		    editor.putString(key, finalValue);
	        editor.commit();		    

		    call.setResult(finalValue);
		   
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("photobrowser"))
		{
			Log.i("API", "**PHOTO BROWSER**");
			// only show image browser if there are images
			JsonNode images = call.param.path("images");
			if (images.isArray() && images.size() > 0)
			{
				PhotoBrowser photoBrowser = PhotoBrowser.newInstance(call.param);
				photoBrowser.loader = this;
				photoBrowser.appDeck = appDeck;
				photoBrowser.currentPageUrl = "photo://browser";
				photoBrowser.screenConfiguration = ScreenConfiguration.defaultConfiguration();				
				pushFragment(photoBrowser);
			}
			
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("loadapp"))
		{
			Log.i("API", "**LOAD APP**");
			
			String jsonUrl = call.param.path("url").textValue();
			boolean clearCache = call.param.path("cache").booleanValue();
			
			// clear cache if asked
			if (clearCache)
				this.appDeck.cache.clear();
			
			// dowload json data, put it in cache, lauch app
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(jsonUrl, new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			    	appDeck.cache.storeInCache(this.getRequestURI().toString(), response);
			    	Intent i = new Intent(Loader.this, Loader.class);
			    	i.putExtra(JSON_URL, this.getRequestURI().toString());
			    	startActivity(i);			    	
			    }
			});
			
			return true;
		}	

		if (call.command.equalsIgnoreCase("reload"))
		{
			reload();
			return true;
		}
		
		if (call.command.equalsIgnoreCase("pageroot"))
		{
			Log.i("API", "**PAGE ROOT**");
			String absoluteURL = call.smartWebView.resolve(call.param.textValue());
			this.loadRootPage(absoluteURL);			
			return true;
		}

		if (call.command.equalsIgnoreCase("pagerootreload"))
		{
			Log.i("API", "**PAGE ROOT RELOAD**");
			String absoluteURL = call.smartWebView.resolve(call.param.textValue());
			this.loadRootPage(absoluteURL);
	        if (leftMenuWebView != null)
	        	leftMenuWebView.reload();
	        if (rightMenuWebView != null)
	        	rightMenuWebView.reload();
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("pagepush"))
		{
			Log.i("API", "**PAGE PUSH**");
			String absoluteURL = call.smartWebView.resolve(call.param.textValue());
			this.loadPage(absoluteURL);			
			return true;
		}
		
		if (call.command.equalsIgnoreCase("pagepop"))
		{
			Log.i("API", "**PAGE POP**");
			this.popFragment();			
			return true;
		}

		if (call.command.equalsIgnoreCase("pagepoproot"))
		{
			Log.i("API", "**PAGE POP ROOT**");
			popRootFragment();
			return true;
		}		

		if (call.command.equalsIgnoreCase("slidemenu"))
		{
			String command = call.param.path("command").textValue();
			String position = call.param.path("position").textValue();
			
			if (command.equalsIgnoreCase("open"))
			{
				if (position.equalsIgnoreCase("left"))
					this.slidingMenu.showMenu();
				if (position.equalsIgnoreCase("right"))
					this.slidingMenu.showSecondaryMenu();
				if (position.equalsIgnoreCase("main"))
					this.slidingMenu.showContent();
			} else {
				this.slidingMenu.showContent();
			}
		}	
		
		if (call.command.startsWith("is"))
		{
			Log.i("API", "** IS ["+call.command+"] **");
			
			boolean result = false;
			
			if (call.command.equalsIgnoreCase("istablet"))
				result = this.appDeck.isTablet;
			else if (call.command.equalsIgnoreCase("isphone"))
				result = !this.appDeck.isTablet;
			else if (call.command.equalsIgnoreCase("isios"))
				result = false;
			else if (call.command.equalsIgnoreCase("isandroid"))
				result = true;
			else if (call.command.equalsIgnoreCase("islandscape"))
				result = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
			else if (call.command.equalsIgnoreCase("isportrait"))
				result = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

			call.setResult(Boolean.valueOf(result));
			
			return true;
		}		
		
		Log.i("API ERROR", call.command);
		return false;
	}
	
    @Override
    public void onBackPressed() {
    	
    	// close menu ?
        if (slidingMenu != null && slidingMenu.isMenuShowing()) {
            slidingMenu.toggle();
            return;
        }

        // current fragment can go back ?
        AppDeckFragment currentFragment = getCurrentAppDeckFragment();
        if (currentFragment.canGoBack())
        {
        	currentFragment.goBack();
        	return;
        }
        
        // try to pop a fragment if possible
        if (popFragment())
        	return;

        // current fragment is home ?
        if (currentFragment == null || currentFragment.currentPageUrl == null || currentFragment.currentPageUrl.compareToIgnoreCase(appDeck.config.bootstrapUrl.toString()) != 0)
        {
//        	Debug.stopMethodTracing();
        	loadRootPage(appDeck.config.bootstrapUrl.toString());
        	return;
        }
        
/*        // page stacked ?
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1)
        {
        	fragmentManager.popBackStack();
        	return;
        }*/
        
        finish();      

    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU && this.slidingMenu != null) {
            this.slidingMenu.toggle();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
/*    
    public void setAsHome()
    {
    	Page parent = (Page)getParent();
    	if (parent != null)
    	{
    		parent.setAsHome();
    		parent.finish();
    	}
    }
        */
    public int getActionBarHeight()
    {
    	int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

/*        if (actionBarHeight == 0 && getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)){
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }*/
        
        if (actionBarHeight != 0)
        	return actionBarHeight;

       //OR as stated by @Marina.Eariel
       //TypedValue tv = new TypedValue();
       if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
          if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
       }/* else if(getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
       }*/
       return actionBarHeight;
    }

    
    public void setMenuItems(PageMenuItem[] menuItems)
    {
    	if (menuItems != null)
    		for (int i = 0; i < menuItems.length; i++) {
    			PageMenuItem item = menuItems[i];
    			item.cancel();
    		}
    	this.menuItems = menuItems;
    	supportInvalidateOptionsMenu();
//    	invalidateOptionsMenu();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    	if (menuItems == null)
    		return true;
    	
		for (int i = 0; i < menuItems.length; i++) {
			PageMenuItem item = menuItems[i];
			
			item.setMenuItem(menu.add("button"), getBaseContext());
			
			//item.setMenuItem(menu.add(0, i, 0, null));
		}
        return true;
    }


    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	int idx = item.getItemId();

    	if (idx == android.R.id.home)
    	{
    		if (slidingMenu != null)
    			slidingMenu.toggle();
    		return true;
    	}
    	
    	if (menuItems == null)
    		return false;    	

		for (int i = 0; i < menuItems.length; i++)
		{
			PageMenuItem pageMenuItem = menuItems[i];
			if (pageMenuItem.menuItem == item)
			{
				pageMenuItem.fire();
	    		return true;				
			}
		}
    	/*
    	if (idx >= 0 && idx <= menuItems.length)
    	{
    		PageMenuItem pageMenuItem = menuItems[idx];
    		AppDeckFragment fragment = getCurrentAppDeckFragment();
    		fragment.loadUrl(pageMenuItem.content);
    		return true;    	
    	}*/
    	
		return super.onOptionsItemSelected(item);
    }    

	@SuppressLint("InlinedApi")
	public void setEnableHardwareAcceleration(Boolean enabled)
	{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        	return;

        //enabled = true;
        
        int layerType = (enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE);
        
        slidingMenu.forceLayerType(layerType);
	}
	
	public void share(String title, String url, String imageURL)
	{
//		ShareActionProvider shareAction = null;	
//		shareAction = new ShareActionProvider(this);
		
		// add stats
		appDeck.ga.event("action", "share", (url != null && !url.isEmpty() ? url : title), 1);
		
		// create share intent
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

		sharingIntent.setType("text/plain");
		if (title != null && !title.isEmpty())
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
		if (url != null && !url.isEmpty() == false)
			sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
		
		// not an image ?
		if (imageURL == null || imageURL.isEmpty())
		{
			startActivity(Intent.createChooser(sharingIntent, "Share via"));
			return;
		}

		// image ?
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisc(true)
        .build();
        
        // Load image, decode it to Bitmap and return Bitmap to callback
        appDeck.imageLoader.loadImage(imageURL, options, new sharingImageLoadingListener(imageURL, sharingIntent));
	}
	
	private class sharingImageLoadingListener extends SimpleImageLoadingListener
	{
    	@SuppressWarnings("unused")
		String imageURL;
    	Intent sharingIntent;
    	
    	sharingImageLoadingListener(String imageURL, Intent sharingIntent)
    	{
    		this.imageURL = imageURL;
    		this.sharingIntent = sharingIntent;
    	}
    	
    	@Override
    	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
    		Log.e("image Sharing", failReason.toString());
    	};
    	
    	@Override
    	public void onLoadingComplete(String imageUri, View view,
    			Bitmap loadedImage) {
    		super.onLoadingComplete(imageUri, view, loadedImage);

    		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    		loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    		File f = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
    		try {
    		    f.createNewFile();
    		    FileOutputStream fo = new FileOutputStream(f);
    		    fo.write(bytes.toByteArray());
    		    fo.close();
    		} catch (IOException e) {                       
    		        e.printStackTrace();
    		}
    		sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath()  + "image.jpg"));
    		startActivity(Intent.createChooser(sharingIntent, "Share via"));    		
    	}
	}
	
	// cancel popup, popover, dialog from current page
	public void cancelSubViews()
	{
		/*if (appDeck.glPopup != null)
		{
			appDeck.glPopup.finish();
			appDeck.glPopup = null;
		}*/
		if (!isForeground)
			return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null)
        	return;
		Fragment popover = fragmentManager.findFragmentByTag("fragmentPopOver");
		if (popover != null)
		{
	    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    	fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);		    	
	    	fragmentTransaction.remove(popover);
	    	fragmentTransaction.commitAllowingStateLoss();			
		}
		Fragment popup = fragmentManager.findFragmentByTag("fragmentPopUp");
		if (popup != null)
		{
			getSupportActionBar().show();
	    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    	fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);		    	
	    	fragmentTransaction.remove(popup);
	    	fragmentTransaction.commitAllowingStateLoss();			
		}
	}
	
	public void showPopOver(AppDeckFragment origin, AppDeckApiCall call)
	{
		if (origin != null)
			origin.loader.cancelSubViews();
		
	    // rather create this as a instance variable of your class		
		PopOverFragment popover = new PopOverFragment(origin, call);
		popover.loader = this;
		//popover.setRetainInstance(true);
		//popover.screenConfiguration = appDeck.config.getConfiguration(popover.currentPageUrl);		
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		//ft.add(popover, "fragmentPopOver");
		ft.add(R.id.loader_container, popover, "fragmentPopOver");
		ft.commitAllowingStateLoss();	
		/*
		Dialog popUpDialog = new Dialog(getBaseContext(),
                android.R.style.Theme_Translucent_NoTitleBar);
		popUpDialog.setCanceledOnTouchOutside(true);
		popUpDialog.setContentView(popover.getView());*/
	}
	
	public void showPopUp(AppDeckFragment origin, String url)
	{
		if (origin != null)
			origin.loader.cancelSubViews();
		Intent intent = new Intent(this, PopUp.class);
    	//intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//    			|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	intent.putExtra(PopUp.POP_UP_URL, (origin != null ? origin.resolveURL(url) : url));
    	startActivity(intent);
    	//overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        Display display = ((android.view.WindowManager) 
                getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if ((display.getRotation() == Surface.ROTATION_0) || 
            (display.getRotation() == Surface.ROTATION_180)) {
        	//overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        	overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        } else if ((display.getRotation() == Surface.ROTATION_90) ||
                   (display.getRotation() == Surface.ROTATION_270)) {
        	//overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        	overridePendingTransition(R.anim.slide_in_left, android.R.anim.fade_out);
        	
        }
		//getActivity().getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
   		
	}
	
	protected void createIntent(String type, String absoluteURL)
	{
		cancelSubViews();
		Intent i = new Intent(this, Loader.class);
    	i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    	//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	i.putExtra(type, absoluteURL);
    	startActivity(i);
	}
	
    @Override
    protected void onNewIntent (Intent intent)
    {
    	super.onNewIntent(intent);
    	
    	isForeground = true;
    	Bundle extras = intent.getExtras();
    	if (extras == null)
    		return;

    	// loadUrl intent
    	String url = extras.getString(PAGE_URL);
    	if (url != null && !url.isEmpty())
    	{
    		loadPage(url);
    		return;
    	}

    	// root url
    	url = extras.getString(ROOT_PAGE_URL);
    	if (url != null && !url.isEmpty())
    	{
    		loadRootPage(url);
    		return;
    	}    	  

    	// Push Notification
    	url = extras.getString(PUSH_URL);
    	if (url != null && !url.isEmpty())
    	{
    		String title = extras.getString(PUSH_TITLE);
    		new PushDialog(url, title).show();
            return;
    	}
    	
    	// popup url
    	url = extras.getString(POP_UP_URL);
    	if (url != null && !url.isEmpty())
    	{
    		showPopUp(null, url);
    		return;
    	}
    	
    }	
	
    public class PushDialog
    {
    	String url;
    	String title;
    	
    	public PushDialog(String url, String title)
    	{
			this.url = url;
			this.title = title;
		}
    	
    	public void show()
    	{
            new AlertDialog.Builder(Loader.this)
            //.setTitle("javaScript dialog")
            .setMessage(title)
            .setPositiveButton(android.R.string.ok, 
                    new DialogInterface.OnClickListener() 
                    {
                        public void onClick(DialogInterface dialog, int which) 
                        {
                        	loadPage(url);
                        }
                    })
            .setNegativeButton(android.R.string.cancel, 
                    new DialogInterface.OnClickListener() 
                    {
                        public void onClick(DialogInterface dialog, int which) 
                        {
                            
                        }
                    })
            .create()
            .show();      		
    	}
    }
    
    boolean shouldRenderActionBar = true;
    public void toggleActionBar()
    {
 	   shouldRenderActionBar = !shouldRenderActionBar;
 	   
 	   if (shouldRenderActionBar)
 		   getSupportActionBar().show();
 	   else
 		   getSupportActionBar().hide();
    }    
    
}
