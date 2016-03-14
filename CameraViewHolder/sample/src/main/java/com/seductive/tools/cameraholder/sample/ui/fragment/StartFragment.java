package com.seductive.tools.cameraholder.sample.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seductive.tools.cameraholder.sample.R;
import com.seductive.tools.cameraholder.sample.ui.fragment.listeners.OnStartBtnClickListener;

public class StartFragment extends Fragment {

    public static Fragment createInstance() {
        return new StartFragment();
    }

    private OnStartBtnClickListener mClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_start, container, false);
        root.findViewById(R.id.start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onStartBtnClick();
                }
            }
        });
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStartBtnClickListener) {
            mClickListener = (OnStartBtnClickListener) context;
        }
    }

    public void hideLoadingDueToError() {
//        bindingObject.setLoadingResolutions(false);
        //TODO
    }
}
