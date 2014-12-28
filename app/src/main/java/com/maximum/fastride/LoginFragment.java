package com.maximum.fastride;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.widget.LoginButton;

import java.util.Arrays;

/**
 * Created by Oleg on 27-Dec-14.
 */
public class LoginFragment extends Fragment {

    public LoginFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_register,
                container, false);

//        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
//        //authButton.setFragment(this);
//        authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));

        return view;
    }

}
