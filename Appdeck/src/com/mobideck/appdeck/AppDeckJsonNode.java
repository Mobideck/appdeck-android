package com.mobideck.appdeck;

import com.fasterxml.jackson.databind.JsonNode;

public class AppDeckJsonNode
{
	JsonNode root;
	
	AppDeckJsonNode(JsonNode root)
	{
		this.root = root;
	}
	
	AppDeckJsonNode get(String name)
	{
		JsonNode ret = root.path(name);
		if (ret.isMissingNode())
			return null;
		return new AppDeckJsonNode(ret);
	}
	
	AppDeckJsonNode get(int idx)
	{
		JsonNode ret = root.path(idx);
		if (ret.isMissingNode())
			return null;
		return new AppDeckJsonNode(ret);
	}
	

	AppDeckJsonNode path(String name)
	{
		JsonNode ret = root.path(name);
		if (AppDeck.getInstance().isTablet)
		{
			JsonNode alt = root.path(name+"_tablet");
			if (alt.isMissingNode() == false)
				ret = alt;
		}
		return new AppDeckJsonNode(ret);
	}
	
	AppDeckJsonNode path(int idx)
	{
		JsonNode ret = root.path(idx);
		return new AppDeckJsonNode(ret);
	}

	boolean isInt()
	{
		return root.isInt();
	}
	
	int intValue()
	{
		return root.intValue();
	}
	
	String textValue()
	{
		return root.textValue();
	}
	
	boolean booleanValue()
	{
		return root.booleanValue();
	}
	
	boolean isMissingNode()
	{
		return root.isMissingNode();
	}
			
	boolean isArray()
	{
		return root.isArray();
	}
	
	int size()
	{
		return root.size();
	}
}