package com.seductive.tools.cameraholder.sample.ui.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.seductive.tools.cameraholder.sample.R;

public class ProgressDialogFragment extends DialogFragment {

    public static final String TAG = ProgressDialogFragment.class.getSimpleName();

    private Bitmap mBitmap;

    public static ProgressDialogFragment newInstance(Bitmap jpgBitmap) {
        return new ProgressDialogFragment().setResultBitmap(jpgBitmap);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_view, null);
        if (mBitmap != null) {
            ((ImageView) dialogView.findViewById(R.id.capture_result_iv)).setImageBitmap(mBitmap);
            dialogView.findViewById(R.id.progress_tv).setVisibility(View.INVISIBLE);
        }

        return builder.setView(dialogView).create();
    }

    private ProgressDialogFragment setResultBitmap(Bitmap jpgBitmap) {
        this.mBitmap = jpgBitmap;
        return this;
    }
}
