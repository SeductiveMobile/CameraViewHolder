package com.seductive.tools.cameraholder.sample.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

    private static final String PROVIDED_MESSAGE = "provided_message";
    private static final String PROVIDED_MODE = "provided_mode";

    public static ProgressDialogFragment newInstance(String title, String message) {
        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PROVIDED_MESSAGE, message);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isModal = getArguments().getBoolean(PROVIDED_MODE, false);
        setCancelable(!isModal);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getArguments().getString(PROVIDED_MESSAGE));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    public ProgressDialogFragment setModal(boolean value) {
        getArguments().putBoolean(PROVIDED_MODE, value);
        return this;
    }
}
