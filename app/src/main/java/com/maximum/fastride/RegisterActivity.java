package com.maximum.fastride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class RegisterActivity extends FragmentActivity {

    private static final String LOG_TAG = "fast_ride";

	private final String PENDING_ACTION_BUNDLE_KEY = "com.maximum.fastride:PendingAction";
    private final String fbProvider = "fb";

	private UiLifecycleHelper uiHelper;
	private LoginButton loginButton;

    private GraphUser fbUser;

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
	
	private PendingAction pendingAction = PendingAction.NONE;
	
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
		
        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        setContentView(R.layout.activity_register);
        loginButton = (LoginButton) findViewById(R.id.loginButton);
        
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if( user != null ) {
                    RegisterActivity.this.fbUser = user;

                    saveFBUser(user);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("regUser", "done");
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
                

            }
        });
        
        loginButton.setOnErrorListener(new LoginButton.OnErrorListener() {
			
			@Override
			public void onError(FacebookException error) {
				String msg = error.getMessage();
				msg.trim();
				
			}
		});
	}

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);

    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void saveFBUser(GraphUser fbUser) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("username", fbUser.getFirstName());
        editor.putString("registrationProvider", fbProvider);
        editor.putString("lastUsername", fbUser.getLastName());
        editor.putString("userid", fbUser.getId());
        editor.commit();
    }

    private void handlePendingAction() {
        pendingAction = PendingAction.NONE;
    }
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
//                new AlertDialog.Builder(RegisterActivity.this)
//                    .setTitle(R.string.cancelled)
//                    .setMessage(R.string.permission_not_granted)
//                    .setPositiveButton(R.string.ok, null)
//                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }

    }
	
}
