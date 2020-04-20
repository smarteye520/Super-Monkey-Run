package com.secrethq.ads;
import java.lang.ref.WeakReference;
import org.cocos2dx.lib.Cocos2dxActivity;


import android.R;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

// import com.applovin.*;
import com.applovin.adview.*;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.adview.AppLovinAdView;
import com.google.android.gms.ads.AdView;

public class PTAdAppLovinBridge {
    private static PTAdAppLovinBridge sInstance;
    private static final String TAG = "PTAdAppLovinBridge";
    private static Cocos2dxActivity activity;
    private static WeakReference<Cocos2dxActivity> s_activity;

    //private static AppLovinInterstitialAd mAppLovinInterstitial;
    private static AppLovinAdView mBanner;
    private static AppLovinSdk sdk;
    private static ViewGroup view;
    
    private static RelativeLayout.LayoutParams adViewParams;
      
    private static native void interstitialDidFail();
    private static native void bannerDidFail();
    
    private static boolean isBannerScheduledForShow = false;
    private static boolean isInterstitialScheduledForShow = false;
    
    static class InterstetialAppLovinListener implements AppLovinAdLoadListener,AppLovinAdDisplayListener {
    	protected String listenerType = "Interstetial ";
		
    	@Override
		public void adDisplayed(AppLovinAd arg0) {
			Log.v(TAG, listenerType + "adDisplayed");
		}

		@Override
		public void adHidden(AppLovinAd arg0) {
			Log.v(TAG, listenerType + "adHidden");
		}

		/**
	     * This method is called when a new ad has been received. This method is
	     * invoked on a background thread.
	     *
	     * @param ad
	     *            Newly received ad. Guaranteed not to be null.
	     */
		@Override
		public void adReceived(AppLovinAd ad) {
			Log.v(TAG, listenerType + "adReceived");
			// This HTML could be used to render an ad in any custom
	        // web view
	        // String adHTML = ad.getHtml();

	        // When the ad is clicked one should invoke this code to track
	        // the click:

	        // AppLovinSdk sdk = AppLovinSdk.getInstance( this );
	        // AppLovinAdService adService = sdk.getAdService();
	        // adService.trackAdClick( ad );
		}

		/**
	     * This method is called when an ad could not be retrieved from the server.
	     * This method is invoked on a background thread.
	     * <p>
	     * Common error codes are: </br> 
	     * <code>202</code> -- no ad is available</br>
	     * <code>5xx</code> -- internal server error</br>
	     * <code>negative number</code> -- internal errors </br>
	     *
	     * @param errorCode
	     *            An error code recieved from the server.
	     */
		@Override
		public void failedToReceiveAd(int arg0) {
			Log.v(TAG, listenerType + "failedToReceiveAd");
			failToReceiveHandler();
		}
		
		protected void failToReceiveHandler() {
			if ( !isInterstitialScheduledForShow )
				return;
			
			PTAdAppLovinBridge.interstitialDidFail();
			isInterstitialScheduledForShow = false;
		}
    };
    
    static class BannerAppLovinListener extends InterstetialAppLovinListener{
    	BannerAppLovinListener() {
    		listenerType = "Banner ";
    	}
		
    	@Override
		protected void failToReceiveHandler() {
    		if (!isBannerScheduledForShow)
    			return;
    		
			PTAdAppLovinBridge.bannerDidFail();
		}
    };
    
    private static boolean sessionStarted = false;
    private static InterstetialAppLovinListener interstetialListener = new InterstetialAppLovinListener();
    private static BannerAppLovinListener bannerListener = new BannerAppLovinListener();

    public static PTAdAppLovinBridge instance() {
        if (sInstance == null)
            sInstance = new PTAdAppLovinBridge();
        return sInstance;
    }

    public static void initBridge(Cocos2dxActivity activity){
        Log.v(TAG, "PTAdAppLovinBridge  -- INIT");

        AppLovinSdk.initializeSdk(activity);

        Log.v(TAG, "AppLovin SDK Version : " + PTAdAppLovinBridge.sdk.VERSION);
        
        PTAdAppLovinBridge.s_activity = new WeakReference<Cocos2dxActivity>(activity);
        PTAdAppLovinBridge.activity = activity;
        
        PTAdAppLovinBridge.s_activity.get().runOnUiThread( new Runnable() {
            public void run() {
//                FrameLayout frameLayout = (FrameLayout)PTAdAppLovinBridge.activity.findViewById(android.R.id.content);
//        
//                adViewParams = new RelativeLayout.LayoutParams(
//						RelativeLayout.LayoutParams.WRAP_CONTENT,
//						RelativeLayout.LayoutParams.WRAP_CONTENT);
//				adViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//				adViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//				
//				RelativeLayout layout = new RelativeLayout( PTAdAppLovinBridge.activity );
//				frameLayout.addView( layout );
//        
//                PTAdAppLovinBridge.view = layout;
            }
        });
    }

    
    public static void startSession( String sdkKey ){
        if(sdkKey != null && !PTAdAppLovinBridge.sessionStarted){
            Log.v(TAG, "Start Session: " + sdkKey);
           
            PTAdAppLovinBridge.sessionStarted = true;
        }
    }

    public static void showFullScreen() {
        Log.v(TAG, "showFullScreen");
        
        isInterstitialScheduledForShow = true;

        PTAdAppLovinBridge.s_activity.get().runOnUiThread( new Runnable() {
            public void run() {
                AppLovinSdk.getInstance(PTAdAppLovinBridge.activity).getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
                    @Override
            		public void adReceived(AppLovinAd ad) {
                    	if (!isInterstitialScheduledForShow)
                    		return;
                    	
            			Log.v(TAG, "Interstetial adReceived");
            			AppLovinInterstitialAd.show(PTAdAppLovinBridge.activity);
            			isInterstitialScheduledForShow = false;
            		}

            		@Override
            		public void failedToReceiveAd(int arg0) {
            			if (!isInterstitialScheduledForShow)
            				return;
            			
            			Log.v(TAG, "Interstetial failedToReceiveAd");
            			PTAdAppLovinBridge.interstitialDidFail();
            			isInterstitialScheduledForShow = false;
            		}
                });
		    }
		});
    }

    public static void showBannerAd(){
        Log.v(TAG, "showBannerAd");

        isBannerScheduledForShow = true;
        
        PTAdAppLovinBridge.s_activity.get().runOnUiThread( new Runnable() {
            public void run() {
//                if(PTAdAppLovinBridge.mBanner == null){
//                    Log.w(TAG, "BANNER VIEW IS EMPTY ---------------------------------------------------------------");
//                }else{
//                    PTAdAppLovinBridge.mBanner.setVisibility(View.VISIBLE);
//
//                    // adding banner to view only first time
//                    if ( PTAdAppLovinBridge.view.getChildCount() == 0 ) {
//                    	PTAdAppLovinBridge.mBanner.setAdLoadListener(bannerListener);
//                      PTAdAppLovinBridge.mBanner.setAdDisplayListener(bannerListener);
//                        
//	                    try {                       
//	                		PTAdAppLovinBridge.view.addView( PTAdAppLovinBridge.mBanner, PTAdAppLovinBridge.adViewParams );
//	                    } catch (Exception e) {
//	                        Log.v(TAG, "showBannerAd - Failed View Add Banner");
//	                    }
//                    }
//                    
//                    PTAdAppLovinBridge.mBanner.loadNextAd();
//                }
            }
        });
    }

    public static void hideBannerAd(){
        Log.v(TAG, "hideBannerAd");
        
        isBannerScheduledForShow = false;
        
        PTAdAppLovinBridge.s_activity.get().runOnUiThread( new Runnable() {
            public void run() {
//                if(PTAdAppLovinBridge.mBanner != null){
//                	PTAdAppLovinBridge.mBanner.setVisibility(View.INVISIBLE);
//                }
            }
        });
    }
}
