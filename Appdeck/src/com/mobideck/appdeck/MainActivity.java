package com.mobideck.appdeck;

import com.actionbarsherlock.app.SherlockActivity;
import com.mobideck.appdeck.R;

import android.content.Intent;
import android.os.Bundle;
//import android.view.Window;
//import android.view.WindowManager;

public class MainActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        */
        setContentView(R.layout.loading);
        
       // AppDeck.getInstance().onAppStart();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	finish();
    }    
    
    @Override
    protected void onNewIntent (Intent intent)
    {
    	
    }

    
}
