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
    private static final int LEFT6=332;
    private static final int TOP6=323;
    private static final int RIGHT6=2177;
    private static final int BOTTOM6=813;
    private static final int OFFSET6=548;
    private static final int LEFT5=332;
    private static final int TOP5=323;
    private static final int RIGHT5=2177;
    private static final int BOTTOM5=813;
    private static final int OFFSET5=548;
    private int[] shelfId = null;
    private int color;
    private int bigId;



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

    public void setColor(int color)
    {
        this.color=color;
    }

    public void setBigId(int bigId)
    {
        this.bigId=bigId;
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
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(12);
            if(shelfId[6]==1)
            paint.setColor(getResources().getColor(R.color.dest_circle));
            else if(shelfId[6]==2)
                paint.setColor(getResources().getColor(R.color.bg_screen2));

            for (int i=0;i<6;i++)
            {
                if(shelfId[i]!=0)
                {   if(bigId==6)
                        {
                            PointF left_top = sourceToViewCoord(new PointF(LEFT6,TOP6+i*OFFSET6));
                            PointF right_bottom = sourceToViewCoord(RIGHT6,BOTTOM6+i*OFFSET6);
                            canvas.drawRect(left_top.x,left_top.y,right_bottom.x,right_bottom.y,paint);
                        }
                    else
                        {
                            PointF left_top = sourceToViewCoord(new PointF(LEFT6,TOP6+i*OFFSET6));
                            PointF right_bottom = sourceToViewCoord(RIGHT6,BOTTOM6+i*OFFSET6);
                            canvas.drawRect(left_top.x,left_top.y,right_bottom.x,right_bottom.y,paint);
                        }

                }
            }
        }




    }
}
