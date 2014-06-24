package com.mobideck.appdeck;

import java.io.IOException;

import android.webkit.JsPromptResult;
import android.webkit.WebView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AppDeckApiCall {

	public WebView webview;
	public SmartWebView smartWebView;
	public String command;
	public String eventID;
	public String inputJSON;
	public JsonNode input;
	public JsonNode param;
	public String resultJSON;
	//@property (strong, nonatomic) id result;
	public Boolean success;
	public Boolean callBackSend;
	public JsPromptResult result;
	public AppDeckFragment appDeckFragment;
	
	protected boolean postponeResult = false;
	
	protected boolean resultSent = false;
	
	public AppDeckApiCall(String command, String inputJSON, JsPromptResult result)
	{
		this.command = command;
		this.inputJSON = inputJSON;
		this.result = result;
		input = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			input = mapper.readValue(inputJSON, JsonNode.class);
			eventID = input.path("eventid").textValue();
			param = input.path("param");
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
	
	public void sendCallBackWithError(String error)
	{
		
	}
	
	public void sendCallbackWithResult(String result)
	{
		
	}
	
	public void setResultJSON(String json)
	{
		resultJSON = json;
	}
	
	public void setResult(Object res)
	{
		try {
			ObjectMapper mapper = new ObjectMapper();
			resultJSON = mapper.writeValueAsString(res);
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
	
	public void postponeResult()
	{
		postponeResult = true;
	}
	
	public void sendPostponeResult(Boolean r)
	{
		postponeResult = false;
		sendResult(r);
	}
	
	public void sendResult(Boolean r)
	{
		if (postponeResult)
			return;
		if (resultSent)
			return;
		resultSent = true;
		String rs = (r == true ? "1" : "0");
		if (resultJSON == null)
			resultJSON = "null";
		String ret = "{\"success\": \""+rs+"\", \"result\": ["+resultJSON+"]}";
		result.confirm(ret);
	}	
	
	protected void finalize() throws Throwable
	{
		super.finalize();

		if (resultSent == false)
			result.cancel();
		
	}
	
	
}
