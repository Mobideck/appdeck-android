package com.mobideck.appdeck;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

public class FragmentAnimation {

	AppDeckFragment from;
	AppDeckFragment to;
	
	public FragmentAnimation(AppDeckFragment from, AppDeckFragment to)
	{
		this.from = from;
		this.to = to;
	}
	
	public void onFromFragmentShow()
	{
		View fromView = from.getView();
		//View toView = to.getView();
		
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(
					com.nineoldandroids.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(
					com.nineoldandroids.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(com.nineoldandroids.animation.Animator animation)
			{
				
				
			}
			
			@Override
			public void onAnimationCancel(
					com.nineoldandroids.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(
            //ObjectAnimator.ofFloat(view, "rotationX", 0, 360),
            //ObjectAnimator.ofFloat(view, "rotationY", 0, 180),
            //ObjectAnimator.ofFloat(view, "rotation", 0, -90),
            ObjectAnimator.ofFloat(fromView, "translationX", 320, 0),
            //ObjectAnimator.ofFloat(view, "translationY", 0, 90),
            ObjectAnimator.ofFloat(fromView, "scaleX", 1.2f, 1.0f),
            ObjectAnimator.ofFloat(fromView, "scaleY", 1.2f, 1.0f),
            ObjectAnimator.ofFloat(fromView, "alpha", 0.0f, 1.0f)
        );
        set.setDuration(300).start();
	}
	
	public void onFromFragmentHide()
	{
		View fromView = from.getView();
		//View toView = to.getView();
		
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(
					com.nineoldandroids.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(
					com.nineoldandroids.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(com.nineoldandroids.animation.Animator animation)
			{
				
				
			}
			
			@Override
			public void onAnimationCancel(
					com.nineoldandroids.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(
            //ObjectAnimator.ofFloat(view, "rotationX", 0, 360),
            //ObjectAnimator.ofFloat(view, "rotationY", 0, 180),
            //ObjectAnimator.ofFloat(view, "rotation", 0, -90),
            ObjectAnimator.ofFloat(fromView, "translationX", 320, 0),
            //ObjectAnimator.ofFloat(view, "translationY", 0, 90),
            ObjectAnimator.ofFloat(fromView, "scaleX", 1.2f, 1.0f),
            ObjectAnimator.ofFloat(fromView, "scaleY", 1.2f, 1.0f),
            ObjectAnimator.ofFloat(fromView, "alpha", 0.0f, 1.0f)
        );
        set.setDuration(300).start();
	}	
	
}
