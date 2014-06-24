package com.mobideck.appdeck;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class GA {

	AppDeck appDeck;
	
	//String[] trackers; 

	GoogleAnalytics ga;
	
	List<Tracker> trackers;
	
	public static String globalTracker = "UA-39746493-1";

	GA(Context context)
	{
		appDeck = AppDeck.getInstance();
		trackers = new ArrayList<Tracker>();
		ga = GoogleAnalytics.getInstance(context);
		//trackers[0] = globalTracker;		
	}
	
	public void addTracker(String trackerID)
	{
		Tracker tracker = ga.getTracker(trackerID);
		
		tracker.set(Fields.customDimension(1), appDeck.packageName);
		if (appDeck.config.app_api_key != null)
			tracker.set(Fields.customDimension(2), appDeck.config.app_api_key);
		else
			tracker.set(Fields.customDimension(2), "none");
		
		trackers.add(tracker);
	}
	

	public void view(String url)
	{
		for (int i = 0; i < trackers.size(); i++) {
			Tracker tracker = trackers.get(i);

			tracker.send(MapBuilder
					  .createAppView()
					  .set(Fields.SCREEN_NAME, url)
					  .build()
					);
			
		}	
	}
	

	public void event(String category, String action, String label, long value)
	{
		for (int i = 0; i < trackers.size(); i++) {
			Tracker tracker = trackers.get(i);

			tracker.send(MapBuilder
			    .createEvent(category, action, label, value)
			    .build()
			);
			
		}	
	}
	
	
}
