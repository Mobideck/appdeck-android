package com.mobideck.appdeck;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.littleshoot.proxy.HttpFilters;

import android.util.Log;

public class CacheFilters implements HttpFilters {

	public final String TAG = "CacheFilters";
	
	protected AppDeck appDeck;
	
	protected String absoluteURL;
	
	protected boolean isFirstRequest = false;
	protected boolean shouldInjectAppDeckJS = false;
	protected boolean forceCache = false;
	protected boolean isInCache = false;
	protected boolean shouldStoreInCache = false;
	protected int shouldStoreInCacheTTL = 0;
	
	//private final Pattern head = Pattern.compile("\\<HEAD\\>", Pattern.CASE_INSENSITIVE);
	//private final List<String> insertInHead;	
	
    protected final HttpRequest originalRequest;
    protected final ChannelHandlerContext ctx;
    
    protected HttpMethod originalMethod;
    
    
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";    

    public CacheFilters(HttpRequest originalRequest,
            ChannelHandlerContext ctx) {
        this.originalRequest = originalRequest;
        this.ctx = ctx;
        this.appDeck = AppDeck.getInstance();
    }

    public CacheFilters(HttpRequest originalRequest) {
        this(originalRequest, null);
    }

    private HttpResponse setCache(HttpResponse response, String cacheName, int maxage)
    {
    	Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.SECOND, maxage);
        Date expire = cal.getTime();    	
        
		response.headers().set("Pragma", "public");
		response.headers().set("Cache-Control", "public, maxage="+maxage);
		
		response.headers().set("Date", DateUtils.formatDate(now));
		response.headers().set("Last-modified", DateUtils.formatDate(now));
		response.headers().set("ETag", "\""+ cacheName + "-appdeck-"+ Utils.randInt(0, 2000000) + "\"");
		response.headers().set("Expires", DateUtils.formatDate(expire));
		
    	return response;
    }
    
    
    private HttpResponse forceCache(HttpResponse response)
    {
    	int maxage = Integer.MAX_VALUE;
    	/*String cacheName = "force";
    	Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.SECOND, maxage);
        Date expire = cal.getTime();*/    	
        
//		response.headers().set("Pragma", "public");
		response.headers().set("Cache-Control", "public, maxage="+maxage);
		
		
		/*
		response.headers().set("Date", DateUtils.formatDate(now));
		response.headers().set("Last-modified", DateUtils.formatDate(now));
		response.headers().set("ETag", "\""+ cacheName + "-appdeck-"+ Utils.randInt(0, 2000000) + "\"");
		response.headers().set("Expires", DateUtils.formatDate(expire));    	
    	*/
		return response;
		
		//return setCache(response, "force", Integer.MAX_VALUE);
    }
    
    private HttpResponse storeInCache(HttpResponse response, int maxage)
    {
    	return setCache(response, "store", maxage);
    }    
    
    private HttpResponse backupInCache(HttpResponse response)
    {
    	return setCache(response, "backup", Integer.MAX_VALUE);    	
    }     
    
    @Override
    public HttpResponse requestPre(HttpObject httpObject) {
    	
    	if (httpObject instanceof DefaultHttpRequest)
    	{
    		DefaultHttpRequest request = (DefaultHttpRequest)httpObject;
    		
    		originalMethod = request.getMethod();
    		
    		//Log.i(TAG, request.toString());    		
    		
    		absoluteURL = request.getUri();
    		if (absoluteURL.startsWith("http://") == false && absoluteURL.startsWith("https://") == false)
    		{
	    		if (absoluteURL.endsWith(":443"))
	    			absoluteURL = "https://" + absoluteURL;
	    		else
	    			absoluteURL = "http://" + absoluteURL;
    		}
    		
    		// sub request always have Referer header
    		isFirstRequest = request.headers().get("Referer") == null;
    		
    		// force cache appdeck ETAG ?
    		String etag = request.headers().get("If-None-Match");
    		if (etag != null && etag.startsWith("\"force-appdeck-"))
    		{
    			Log.i(TAG, "NOT MODIFIED: "+absoluteURL);
	    		DefaultFullHttpResponse response = new DefaultFullHttpResponse(
	                    HttpVersion.HTTP_1_1,
	                    HttpResponseStatus.NOT_MODIFIED);
	    		return response;
    		}
    		
    		// embed file ?
    		InputStream embedStream = appDeck.cache.getEmbedResourceStream(absoluteURL);
    		if (embedStream != null)
    		{
    			//ChunkedStream chunkedStream = new ChunkedStream(embedStream); 
    			//ByteBuf buf = Unpooled.copiedBuffer("ToTo", Charsets.UTF_8);
    			
	    		try {
	    			byte[] data = IOUtils.toByteArray(embedStream);
	    			ByteBuf buf = Unpooled.wrappedBuffer(data);
	    			
		    		DefaultFullHttpResponse response = new DefaultFullHttpResponse(
		                    HttpVersion.HTTP_1_1,
		                    HttpResponseStatus.OK,
		                    buf);
	    			
	    			if (absoluteURL.endsWith(".css"))
	    				response.headers().set("Content-Type", "text/css");
	    			else if (absoluteURL.endsWith(".js"))
	    				response.headers().set("Content-Type", "application/x-javascript; charset=utf-8");
	    			else if (absoluteURL.endsWith(".png"))
	    				response.headers().set("Content-Type", "image/png");
	    			else if (absoluteURL.endsWith(".jpg") || absoluteURL.endsWith(".jpeg"))
	    				response.headers().set("Content-Type", "image/jpg");
	    			else if (absoluteURL.endsWith(".gif"))
	    				response.headers().set("Content-Type", "image/gif");
	    			else if (absoluteURL.endsWith(".htm") || absoluteURL.endsWith(".html") || absoluteURL.endsWith(".php"))
	    				response.headers().set("Content-Type", "text/html");
	    			else if (isFirstRequest)
	    			{
	    				//Log.i(TAG, "EMBED: "+absoluteURL+" Guess CT: text/html");
	    				response.headers().set("Content-Type", "text/html");
	    			}
	    			else
	    			{
	    				//Log.i(TAG, "EMBED: "+absoluteURL+" Guess CT: application/octet-stream");
	    				response.headers().set("Content-Type", "application/octet-stream");
	    				//Log.i(TAG, "EMBED: "+absoluteURL+" Guess CT: text/plain");
	    				//response.headers().set("Content-Type", "text/plain");
	    			}
	    				
	    			
	    			response.headers().set("Content-Length", data.length);	    			
	    			
	    			//response.headers().set("Embed", "true");	    				    			
	    			
	    			response.headers().set("Vary", "Accept-Encoding");
	    			response.headers().set("Server", "AppDeck-Embed-Files/1.0");
	    			response.headers().set("X-Cache", "HIT");
	    			response.headers().set("Accept-Ranges", "bytes");
	    							
	    			forceCache(response);
	    			
	    			response.headers().set("Via", "1.1.10.0.2.15");
	    			
	    			Log.i(TAG, "EMBED: "+absoluteURL+" Size:"+(data.length/1024)+"Kb");
		    		
		    		return response;
	    			
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		
    		if (etag != null && etag.startsWith("\"backup-appdeck-"))
    		{
    			Log.i(TAG, "IN LOCAL BACKUP CACHE: "+absoluteURL);
    			isInCache = true;
    		}
    		if (etag != null && etag.startsWith("\"store-appdeck-"))
    		{
    			Log.i(TAG, "IN LOCAL STORE CACHE: "+absoluteURL);
    			isInCache = true;
    			String cacheControl = request.headers().get("Cache-Control");
    			Log.i(TAG, "Header Cache-Control: "+cacheControl);
    			if (cacheControl.contains("max-age=0") == false)
    			{
	    			String expireString = request.headers().get("If-Modified-Since");
	    			Log.i(TAG, "Header If-Modified-Since: "+expireString);
	    			if (expireString != null)
	    			{
						try {
							Date expire = DateUtils.parseDate(expireString);
		    				if (expire.before(new Date()))
		    				{
		    	    			Log.i(TAG, "LOCAL STORE CACHE STILL VALID NOT MODIFIED: "+absoluteURL);
		    		    		DefaultFullHttpResponse response = new DefaultFullHttpResponse(
		    		                    HttpVersion.HTTP_1_1,
		    		                    HttpResponseStatus.NOT_MODIFIED);
		    		    		return response;    					
		    				}						
						} catch (DateParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
    			}
    		}    		
    		
    		// should be cached ?
    		if (appDeck.cache.shouldCache(absoluteURL))
    		{
    			forceCache = true;
    			Log.i(TAG, " CACHE MISS " + absoluteURL);
    		}
    		
			// this is a screen page ?
			if (isFirstRequest)
			{
				ScreenConfiguration screenConfiguration = appDeck.config.getConfiguration(absoluteURL);
				if (screenConfiguration != null && screenConfiguration.ttl > 0)
				{
					Log.i(TAG, "> ASK TO STORE IN CACHE FOR " + screenConfiguration.ttl + " sec: "+absoluteURL);
					shouldStoreInCache = true;
					shouldStoreInCacheTTL = screenConfiguration.ttl;
					return null;
				}
			}    		
    		
   			Log.i(TAG, " DOWNLOAD " + absoluteURL);

    	}
    	
        return null;
    }

    @Override
    public HttpResponse requestPost(HttpObject httpObject) {
    	Log.i(TAG, "requestPost < " + absoluteURL);

        return null;
    }

/*    
    private boolean isHtmlResponse(FullHttpResponse httpObject) {
		String content = httpObject.headers().get("Content-Type");
		if (content == null) {
			Log.i(TAG, "No content type specified");
			return false;
		} else {
			return content.contains("html");
		}
	}    
    
	private Charset getCharSet(String s) {
		int index = s.toLowerCase().indexOf("charset=");
		if (index > 0) {
			String charset = s.substring(index + 8);
			try {
				return Charset.forName(charset);
			} catch (UnsupportedCharsetException e) {
				Log.w(TAG, "Unsupported charset " + charset + ". Using UTF-8");

			}
		}
		return Charsets.UTF_8;
	}
    	
	private FullHttpResponse intercept(FullHttpResponse httpObject) {
		Charset charset = getCharSet(httpObject.headers().get("Content-Type"));
		ByteBuf content = httpObject.content();
		//Preconditions.checkState(content.isReadable(), "Content not readable");
		String replacement = "TEST OK";//head.matcher(content.toString(charset)).replaceFirst("<HEAD> \n" + replacements());
		content.clear();
		content.writeBytes(replacement.getBytes(Charsets.UTF_8));
		HttpHeaders.setContentLength(httpObject, content.readableBytes());
		return httpObject;
	}    */
    
    @Override
    public HttpObject responsePre(HttpObject httpObject) {
    	
    	Log.i(TAG, "responsePre < " + absoluteURL);
    	
		if (httpObject instanceof HttpResponse)
		{
			HttpResponse response = (HttpResponse)httpObject;
			
			if (forceCache)
			{
				Log.i(TAG, "< CACHE MISS SEND: " + absoluteURL);
				return forceCache(response);
			}
			
			HttpResponseStatus status = response.getStatus(); 
			if (!status.equals(HttpResponseStatus.OK) && isInCache)
			{
    			Log.i(TAG, "< DOWNLOAD FAILED ["+originalMethod.toString()+"] ["+status.toString()+"] - SEND NOT MODIFIED: "+absoluteURL);
	    		response = new DefaultFullHttpResponse(
	                    HttpVersion.HTTP_1_1,
	                    HttpResponseStatus.NOT_MODIFIED);
	    		return response;				
			}

			if (shouldStoreInCache)
			{
				Log.i(TAG, "< SHOULD STORE IN CACHE FOR "+shouldStoreInCacheTTL+" sec : "+absoluteURL);
				return storeInCache(response, shouldStoreInCacheTTL);
			}
			
			// we only store in cache GET and OK responses
			if (status == HttpResponseStatus.OK && originalMethod == HttpMethod.GET)
			{
				Log.i(TAG, "< BACKUP IN CACHE: "+absoluteURL);
				return backupInCache(response);
			}

			// failed ?
			if (status == HttpResponseStatus.BAD_GATEWAY || status == HttpResponseStatus.BAD_REQUEST || status == HttpResponseStatus.FORBIDDEN ||
					status == HttpResponseStatus.GATEWAY_TIMEOUT || status == HttpResponseStatus.INTERNAL_SERVER_ERROR ||
					status == HttpResponseStatus.NOT_FOUND)
			{
				Log.i(TAG, "< FAILED: ["+status.toString()+"] "+absoluteURL);
				response = new DefaultFullHttpResponse(
						HttpVersion.HTTP_1_1,
						HttpResponseStatus.OK);
				return response;
			}
			
			Log.i(TAG, "< PASSTROUGHT: ["+originalMethod.toString()+"] ["+status.toString()+"] "+absoluteURL);
			return response;
		}
		else if (httpObject instanceof DefaultHttpContent)
		{
			//Log.i(TAG, "< DefaultHttpContent " + absoluteURL);
			//DefaultHttpContent content = (DefaultHttpContent)httpObject;
			//return content;
            return httpObject;		
		}
		else if (httpObject instanceof DefaultHttpResponse)
		{
			Log.i(TAG, "< DefaultHttpResponse " + absoluteURL);
			DefaultHttpResponse response = (DefaultHttpResponse)httpObject;
			
			return response;
            			
		}		
		else if (httpObject instanceof HttpResponse)
		{
			Log.i(TAG, "< HttpResponse " + absoluteURL);
			HttpResponse response = (HttpResponse)httpObject;
			return response;
		}
		else if (httpObject instanceof DefaultLastHttpContent)
		{
			Log.i(TAG, "< DefaultLastHttpContent " + absoluteURL);
			DefaultLastHttpContent response = (DefaultLastHttpContent)httpObject;
			return response;
		}
		
    	
		Log.i(TAG, "< ??? " + absoluteURL);
		
    	return httpObject;
    }

    @Override
    public HttpObject responsePost(HttpObject httpObject) {
    	
    	Log.i(TAG, "responsePost < " + absoluteURL);
    	
    	/*
header("Pragma: public");
header("Cache-Control: maxage=".$expires);
header('Expires: ' . gmdate('D, d M Y H:i:s', time()+$expires) . ' GMT');    	 * 
    	 */

    	
    	
/*    	DefaultHttpResponse response = (DefaultHttpResponse)httpObject;
    	HttpHeaders headers = response.headers();
    	
    	
    	headers.add("Pragma", "public");
    	headers.add("Cache-Control", "maxage=2592000");*/
    	
    	
        return httpObject;
    }
	
}
