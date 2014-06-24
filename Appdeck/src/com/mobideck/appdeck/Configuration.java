package com.mobideck.appdeck;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;

public class Configuration {

	public static String TAG = "Configuration";
	
	//int imageToLoad;
	
	class AppDeckColor
	{
		public int color1;
		public int color2;
		
		public Drawable getDrawable()
		{
			 GradientDrawable gd = new GradientDrawable(
			            GradientDrawable.Orientation.TOP_BOTTOM,
			            new int[] {color1, color2});
			    gd.setCornerRadius(0f);
			    return gd;
		}
	}
		
	public Configuration()
	{
		//imageToLoad = 0;
    }

	public void readConfiguration(String app_json_url)
	{
		// init URI object, it will be used to resolve all URLs
		try {
			json_url = new URI(app_json_url);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		// first try to load JSon from application embed ressources
		InputStream jsonStream = null;
		//if (AppDeck.getInstance().noCache == false)
		jsonStream = AppDeck.getInstance().cache.getEmbedResourceStream(app_json_url);
		if (jsonStream == null)
			jsonStream = AppDeck.getInstance().cache.getCachedResourceStream(app_json_url);
		if (jsonStream != null)
		{
			JsonNode node = null;
			try {
				ObjectMapper mapper = new ObjectMapper(AppDeck.getInstance().jsonFactory);
				node = mapper.readValue(jsonStream, JsonNode.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Crashlytics.log("JSon parse exception "+e.getMessage());
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Crashlytics.log("JSon mapping exception "+e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Crashlytics.log("JSon IO exception "+e.getMessage());
			}
	    	readConfiguration(node);
	    	return;
		}
		Crashlytics.log("JSon not in embed ressources");
		Log.e(TAG, "JSon not in embed ressources");
		Utils.killApp(true);
		/*
		// if not available, we download it		
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url.toString(), new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
				JsonNode node = null;
				try {
					ObjectMapper mapper = new ObjectMapper(AppDeck.getInstance().jsonFactory);
					node = mapper.readValue(response, JsonNode.class);
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
		    	readConfiguration(node);
		    	return;
		    }
		});			*/	
	}
	
	private void readConfiguration(JsonNode node)
	{
		if (node == null)
		{
			Crashlytics.log("JSon null node");
			return;
		}
		AppDeckJsonNode root = new AppDeckJsonNode(node);
		// load configuration from json
		
		app_version = root.path("version").intValue();
		app_api_key = root.path("api_key").textValue();
		
		Log.d("Configuration", "Version: " + app_version + " API Key: "+ app_api_key);		
		Crashlytics.log("JSon Version: " + app_version + " API Key: "+ app_api_key);
		
		enable_debug = root.path("enable_debug").booleanValue();
		
		try {
			String base_url = root.path("base_url").textValue();
			app_base_url = new URI(base_url);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		if (app_base_url == null)
			app_base_url = json_url;
		
//		bootstrapUrl = readURI(root.path("bootstrap"), "base_url", null);
		
		// bootstrap
		bootstrapUrl = readURI(root.path("bootstrap"), "url", "/");
		/*try {
			bootstrapUrl = url.resolve(root.path("bootstrap").path("url").textValue()); 
		} catch (Exception e) {
			bootstrapUrl = url.resolve("/");
		}*/

		// left menu
		leftMenuUrl = readURI(root.path("leftmenu"), "url", null);
		
		leftMenuWidth = root.path("leftmenu").path("width").intValue();
		if (leftMenuWidth == 0)
			leftMenuWidth = 320;
/*
		try {		
			leftMenuUrl = url.resolve(root.path("leftmenu").path("url").textValue());
			leftMenuWidth = root.path("leftmenu").path("width").intValue();
			if (leftMenuWidth == 0)
				leftMenuWidth = 320;
		} catch (Exception e) {
			leftMenuUrl = null;
		}*/
		
		// right menu
		rightMenuUrl = readURI(root.path("rightmenu"), "url", null);
		rightMenuWidth = root.path("rightmenu").path("width").intValue();
		if (leftMenuWidth == 0)
			leftMenuWidth = 320;
		/*
		try {
			rightMenuUrl = url.resolve(root.path("rightmenu").path("url").textValue());
			rightMenuWidth = root.path("rightmenu").path("width").intValue();
			if (rightMenuWidth == 0)
				rightMenuWidth = 320;		
		} catch (Exception e) {
			rightMenuUrl = null;
		}*/
		
		title = root.path("title").textValue();
		
		// colors
		app_color = readColor(root, "app_color");
		app_background_color = readColor(root, "app_background_color");
		leftmenu_background_color = readColor(root, "leftmenu_background_color");
		rightmenu_background_color = readColor(root, "rightmenu_background_color");
		
		control_color = readColor(root, "control_color");
		button_color = readColor(root, "button_color");
		
		topbar_color = readColor(root, "app_topbar_color");
		
		// cache
		AppDeckJsonNode cacheNode = root.path("cache"); 
		if (cacheNode.isArray())
		{
			cache = new Pattern[cacheNode.size()];
			for (int i = 0; i < cacheNode.size(); i++) {
				String regexp = cacheNode.path(i).textValue();
				Pattern p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
				cache[i] = p;
			}
		}
		
		// CDN
		
		cdn_enabled = root.path("cdn_enabled").booleanValue();
		cdn_host = root.path("cdn_host").textValue();
		cdn_path = root.path("cdn_path").textValue();
		if (app_api_key != null)
		{
			if (cdn_host == null)
				cdn_host = String.format("%s.appdeckcdn.com", app_api_key);
			if (cdn_path == null)
				cdn_path = "";
		}
		if (cdn_host == null || cdn_host.equalsIgnoreCase(""))
			cdn_enabled = false;
		
		// screen Configuration
		AppDeckJsonNode screenNode = root.path("screens"); 
		if (screenNode.isArray())
		{
			screenConfigurations = new ScreenConfiguration[screenNode.size()];
			for (int i = 0; i < screenNode.size(); i++) {
				ScreenConfiguration config = new ScreenConfiguration(screenNode.path(i), app_base_url);
				screenConfigurations[i] = config;
			}
		}

		// prefetch url
		prefetch_url = readURI(root, "prefetch_url", String.format("http://%s.appdeckcdn.com/%s.7z", app_api_key, app_api_key));
		prefetch_ttl = root.path("prefetch_ttl").intValue();
		if (prefetch_ttl == 0)
			prefetch_ttl = 600;
		
		ga = root.path("ga").textValue();
		
		push_register_url = readURI(root, "push_register_url", "http://push.appdeck.mobi/register");

		push_google_cloud_messaging_sender_id = root.path("push_google_cloud_messaging_sender_id").textValue();
		
		embed_url = readURI(root, "embed_url", null);
		embed_runtime_url = readURI(root, "embed_runtime_url", null);
		
		enable_mobilize = root.path("enable_mobilize").booleanValue();

		icon_theme = "light";
		String icon_theme_suffix = "";
		if (root.path("icon_theme").textValue().equalsIgnoreCase("dark"))
		{
			icon_theme = "dark";
			icon_theme_suffix = "_dark";
		}
			
		
		logoUrl = readURI(root, "logo", null);
		

		icon_action = readURI(root, "icon_action", "http://appdata.static.appdeck.mobi/res/android/icons/action"+icon_theme_suffix+".png");
		icon_ok = readURI(root, "icon_ok", "http://appdata.static.appdeck.mobi/res/android/icons/ok"+icon_theme_suffix+".png");
		icon_cancel = readURI(root, "icon_cancel", "http://appdata.static.appdeck.mobi/res/android/icons/cancel"+icon_theme_suffix+".png");
		icon_close = readURI(root, "icon_close", "http://appdata.static.appdeck.mobi/res/android/icons/close"+icon_theme_suffix+".png");
		icon_config = readURI(root, "icon_config", "http://appdata.static.appdeck.mobi/res/android/icons/config"+icon_theme_suffix+".png");
		icon_info = readURI(root, "icon_info", "http://appdata.static.appdeck.mobi/res/android/icons/info"+icon_theme_suffix+".png");
		icon_menu = readURI(root, "icon_menu", "http://appdata.static.appdeck.mobi/res/android/icons/menu"+icon_theme_suffix+".png");
		icon_next = readURI(root, "icon_next", "http://appdata.static.appdeck.mobi/res/android/icons/next"+icon_theme_suffix+".png");
		icon_previous = readURI(root, "icon_previous", "http://appdata.static.appdeck.mobi/res/android/icons/previous"+icon_theme_suffix+".png");
		icon_refresh = readURI(root, "icon_refresh", "http://appdata.static.appdeck.mobi/res/android/icons/refresh"+icon_theme_suffix+".png");
		icon_search = readURI(root, "icon_search", "http://appdata.static.appdeck.mobi/res/android/icons/search"+icon_theme_suffix+".png");
		icon_up = readURI(root, "icon_up", "http://appdata.static.appdeck.mobi/res/android/icons/up"+icon_theme_suffix+".png");
		icon_down = readURI(root, "icon_down", "http://appdata.static.appdeck.mobi/res/android/icons/down"+icon_theme_suffix+".png");
		icon_user = readURI(root, "icon_user", "http://appdata.static.appdeck.mobi/res/android/icons/user"+icon_theme_suffix+".png");

		image_loader = readURI(root, "image_loader", "http://appdata.static.appdeck.mobi/res/android/images/loader"+icon_theme_suffix+".png");
		image_pull_arrow = readURI(root, "image_pull_arrow", "http://appdata.static.appdeck.mobi/res/android/images/pull_arrow"+icon_theme_suffix+".png");

		
		/*
		imageToLoad = 12;
		logo = readImage(root, "logo", null);
		icon_menu = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/menu.png");
		image_loader = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/images/loader.png");
		image_pull_arrow = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/images/pull_arrow.png");
		icon_action = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/action.png");
		icon_cancel = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/action.png");
		icon_close = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/close.png");
		icon_next = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/next.png");
		icon_previous = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/previous.png");
		icon_up = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/up.png");
		icon_down = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/down.png");
		icon_refresh = readImage(root, "icon_action", "http://appdata.static.appdeck.mobi/res/icons/refresh.png");
		*/
		image_network_error_url = readURI(root, "image_network_error", "http://appdata.static.appdeck.mobi/res/android/images/network_error.png");
		image_network_error_background_color = readColor(root, "image_network_error_background_color");
		
		Crashlytics.log("Read JSON configuration");
		//if (imageToLoad == 0)
		//AppDeck.getInstance().configurationReady();
		
	}
    
	
	public ScreenConfiguration getConfiguration(String absoluteURL)
	{
		if (absoluteURL != null && screenConfigurations != null)
		{
			for (int i = 0; i < screenConfigurations.length; i++) {
				ScreenConfiguration screenConfiguration = screenConfigurations[i];
				
				if (screenConfiguration.match(absoluteURL))
					return screenConfiguration;
			}
		}				
		return ScreenConfiguration.defaultConfiguration();
	}
	
	/*protected void finishLoadImage()
	{
		imageToLoad--;
		if (imageToLoad == 0)
			AppDeck.getInstance().configurationReady();
	}*/
	
	/*protected ImageView readImage(JsonNode root, String name, String defaultValue)
	{
		String imageURL = defaultValue;
		
		// get image URL
		JsonNode value = root.path(name);
		if (value.isMissingNode() == false)
		{
			imageURL = url.resolve(value.textValue()).toString();
		}
		
		if (imageURL == null)
		{
			imageToLoad--;
			return null;
		}
		
		// download image
		// https://github.com/nostra13/Android-Universal-Image-Loader
		ImageView imageView = new ImageView(AppDeck.getInstance().context);
		
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder().build();
		
		ImageLoader.getInstance().displayImage(imageURL, imageView, displayOptions, new ImageLoadingListener() {
		    @Override
		    public void onLoadingStarted(String imageUri, View view) {
		        Log.d("image", "onLoadingStarted");
		    }
		    @Override
		    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		    	Log.d("image", "onLoadingFailed");
		    	finishLoadImage();
		    }
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		    	Log.d("image", "onLoadingComplete");
		    	finishLoadImage();
		    }
		    @Override
		    public void onLoadingCancelled(String imageUri, View view) {
		    	Log.d("image", "onLoadingCancelled");
		    	finishLoadImage();
		    }

		});
		
		return imageView;
	}*/
	
	int parseColor(String colorTxt)
	{
		// try android parser
		try {
			return Color.parseColor(colorTxt);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// try by appending #
		try {
			return Color.parseColor("#"+colorTxt);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// error case
		return Color.TRANSPARENT;
		
	}
	
	protected AppDeckColor readColor(AppDeckJsonNode root, String name)
	{
		
		AppDeckJsonNode value = root.path(name);
		
		if (value.isMissingNode())
			return null;
				
		if (value.isArray() && value.size() == 2)
		{
			AppDeckColor color = new AppDeckColor();
			color.color1 = parseColor(value.path(0).textValue());
			color.color2 = parseColor(value.path(1).textValue());
			return color;
		}		
		if (value.isArray() == false)
		{
			AppDeckColor color = new AppDeckColor();
			color.color1 = color.color2 = parseColor(value.textValue());
			return color;
		}

		return null;
	}
	
	protected URI readURI(AppDeckJsonNode root, String name, String defaultValue)
	{
		AppDeckJsonNode node = root.path(name); 
		if (node.isMissingNode() == false)			
			return app_base_url.resolve(node.textValue());
		if (defaultValue == null)
			return null;
		try {
			return new URI(defaultValue);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// store app conf
	public URI json_url;

	public int app_version;

	public String app_api_key;

	public Boolean	enable_debug;

	public URI app_base_url;
	public URI app_conf_url;
	public URI push_register_url;
	
	public String push_google_cloud_messaging_sender_id;

	public URI bootstrapUrl;
	public URI leftMenuUrl;
	public int leftMenuWidth;
	//public int leftMenuReveal;
	public URI rightMenuUrl;
	public int rightMenuWidth;
	//public int rightMenuReveal;

	public AppDeckColor app_color;
	public AppDeckColor app_background_color;
	public AppDeckColor leftmenu_background_color;
	public AppDeckColor rightmenu_background_color;

	public AppDeckColor control_color;
	public AppDeckColor button_color;

	public String title;

	//public String logoUrl;
	public URI logoUrl;

	public Pattern cache[];


	//@property (strong, nonatomic) UIView *statusBarInfo;

	public AppDeckColor topbar_color;

	public URI prefetch_url;
	public int prefetch_ttl;

	// images and icons

	public Boolean         cdn_enabled;
	public String cdn_host;
	public String cdn_path;

	public String icon_theme;
	
	public URI icon_action;
	public URI icon_ok;
	public URI icon_cancel;
	public URI icon_close;
	public URI icon_config;
	public URI icon_info;	
	public URI icon_menu;
	public URI icon_next;
	public URI icon_previous;
	public URI icon_refresh;
	public URI icon_search;	
	public URI icon_up;
	public URI icon_down;
	public URI icon_user;
	public URI image_loader;
	public URI image_pull_arrow;

	public URI image_network_error_url;
	public AppDeckColor image_network_error_background_color;

	public ScreenConfiguration screenConfigurations[];

	public String mobiclickApplicationId;
	public String mobiclickAdMobSub;

	public String ga;
	public URI embed_url;
	public URI embed_runtime_url;

	public Boolean enable_mobilize;		
}
