package com.mobideck.appdeck;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mobideck.appdeck.R;
import com.fasterxml.jackson.databind.JsonNode;

public class ScreenConfiguration {

	public String title;
	public String logo;
	public String type;
	
	public Boolean isPopUp;
	public Boolean enableShare;
	
	public Pattern urlRegexp[];
	
	public int ttl;
	
	
	public static ScreenConfiguration defaultConfiguration()
	{
		ScreenConfiguration config = new ScreenConfiguration();
		config.ttl = 600;
		return config;
	}
	
	private ScreenConfiguration()
	{
		
	}
	
	public PageMenuItem[] getDefaultPageMenuItems(URI baseUrl, AppDeckFragment fragment)
	{
		PageMenuItem refresh = new PageMenuItem(fragment.loader.getResources().getString(R.string.refresh), "!refresh", "button", "appdeckapi:refresh", baseUrl, fragment);
		
		PageMenuItem items[] = new PageMenuItem[1];
		items[0] = refresh;
		
		return items;
	}	
	
	private String readString(AppDeckJsonNode node, String name)
	{
		AppDeckJsonNode value = node.path(name);
		if (value.isMissingNode())
			return null;
		String text = value.textValue();
		if (text.equalsIgnoreCase("") == true)
			return null;
		return text;
	}
	
	public ScreenConfiguration(AppDeckJsonNode node, URI baseUrl)
	{
		title = readString(node, "title");
		logo =  readString(node, "logo");
		if (logo != null)
			logo = baseUrl.resolve(logo).toString(); 
		type = readString(node, "type");
		isPopUp = node.path("popup").booleanValue();
		enableShare = node.path("enable_share").booleanValue();
		ttl = 600;		
		if (node.path("ttl").isInt())
			ttl = node.path("ttl").intValue();
		
		AppDeckJsonNode urlsNode = node.path("urls"); 
		if (urlsNode.isArray())
		{
			urlRegexp = new Pattern[urlsNode.size()];
			for (int i = 0; i < urlsNode.size(); i++) {
				String regexp = urlsNode.path(i).textValue();
				Pattern p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
				urlRegexp[i] = p;
			}
		}		
	}
	
	public Boolean match(String absoluteURL)
	{
		if (urlRegexp == null)
			return false;
		for (int i = 0; i < urlRegexp.length; i++) {
			Pattern regexp = urlRegexp[i];
			Matcher m = regexp.matcher(absoluteURL);
			if (m.find())
				return true;
		}		
		return false;
	}
	
	public Boolean isRelated(String absoluteURL)
	{
		return match(absoluteURL);
	}
}
