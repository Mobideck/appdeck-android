package com.mobideck.appdeck;

//import com.testflightapp.lib.TestFlight;

import android.app.Application;

public class AppDeckApplication extends Application {

	@Override
	public void onCreate()
	{
/*	     if (true) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	                 .detectDiskReads()
	                 .detectDiskWrites()
	                 .detectNetwork()   // or .detectAll() for all detectable problems
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 .detectLeakedClosableObjects()
	                 .penaltyLog()
	                 .penaltyDeath()
	                 .build());
	     }*/		
		super.onCreate();
        //Initialize TestFlight with your app token.
        //TestFlight.takeOff(this, "d1574e34-b01a-44b7-b44f-231bad62036d");
		
	}		
	
}
