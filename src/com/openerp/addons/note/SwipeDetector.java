package com.openerp.addons.note;

import com.openerp.R;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class SwipeDetector implements OnTouchListener {

    Button deleteRow;

    public static enum Action {
	LR, // Left to Right
	RL, // Right to Left
	TB, // Top to bottom
	BT, // Bottom to Top
	None, // when no action was detected
	Click
    }

    private static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    private Action mSwipeDetected = Action.None;

    public boolean swipeDetected() {
	return mSwipeDetected != Action.None;
    }

    public Action getAction() {
	return mSwipeDetected;
    }

    public boolean onTouch(View v, MotionEvent event) {

	switch (event.getAction()) {
	case MotionEvent.ACTION_DOWN: {
	    downX = event.getX();
	    downY = event.getY();
	    mSwipeDetected = Action.None;

	    System.out.println("Click On List" + v.getX());
	    return false; // allow other events like Click to be processed
	}

	case MotionEvent.ACTION_UP: {
	    upX = event.getX();
	    upY = event.getY();

	    float deltaX = downX - upX;
	    float deltaY = downY - upY;

	    // horizontal swipe detection
	    if (Math.abs(deltaX) > MIN_DISTANCE) {
		// left or right
		if (deltaX < 0) {

		    mSwipeDetected = Action.LR;
		    System.out.println("LR");
		    return false;
		}
		if (deltaX > 0) {

		    mSwipeDetected = Action.RL;
		    System.out.println("RL");
		    /*
		     * Note note = new Note(); note.handleSwipe();
		     */

		    deleteRow = (Button) v
			    .findViewById(R.id.btnNoteListItemDelete);
		    deleteRow.setVisibility(0);
		    deleteRow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			    // TODO Auto-generated method stub
			    System.out.println("DELETING THIS ROW");
			    deleteRow.setVisibility(View.GONE);
			}
		    });
		    return true;
		}
	    }

	    mSwipeDetected = Action.Click;
	    return false;
	}
	}
	return false;
    }
}