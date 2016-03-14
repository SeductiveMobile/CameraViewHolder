package com.seductive.tools.cameraholder.sample.ui.listeners;

import android.widget.SeekBar;

public abstract class CustomOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

    @Override
    public abstract void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) ;

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
