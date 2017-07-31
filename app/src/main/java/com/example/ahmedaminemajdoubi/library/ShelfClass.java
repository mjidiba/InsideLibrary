package com.example.ahmedaminemajdoubi.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amineabboudi on 7/29/17.
 */

public class ShelfClass extends  SubsamplingScaleImageView{
    private static final int LEFT=0;
    private static final int TOP=40;
    private static final int RIGHT=750;
    private static final int BOTTOM=136;
    private static final int OFFSET=176;
    private int[] shelfId = null;



    Paint paint = new Paint();


    public ShelfClass(Context context, AttributeSet attr)  {
        super(context, attr);
    }
    public ShelfClass(Context context) {
        this(context, null);
    }

    private void initialise() {
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
    }


    public void setShelves(int [] shelfId)
    {
        this.shelfId=shelfId.clone();
    }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady()) {
            return;
        }

        if(shelfId!=null)
        {   paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.ia_blue));
            for (int i=0;i<6;i++)
            {
                if(shelfId[i]!=0)
                {
                    PointF left_top = sourceToViewCoord(new PointF(LEFT,TOP+i*OFFSET));
                    PointF right_bottom = sourceToViewCoord(LEFT+RIGHT,BOTTOM+TOP+i*OFFSET);

                    canvas.drawRect(left_top.x,left_top.y,right_bottom.x,right_bottom.y,paint);
                    Log.e("Left", String.valueOf(left_top.x));
                    Log.e("Top", String.valueOf(left_top.y));
                    Log.e("Right", String.valueOf(right_bottom.x));
                    Log.e("Bottom", String.valueOf(right_bottom.y));
                    Log.e("i", String.valueOf(i));

                }
            }
        }




    }
}
