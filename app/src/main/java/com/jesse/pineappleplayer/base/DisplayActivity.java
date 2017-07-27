package com.jesse.pineappleplayer.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

/**
 * Created by zhaoliangtai on 2017/7/25.
 */

public class DisplayActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            hideSystemUI();
        }
        return super.dispatchTouchEvent(ev);
    }
}
