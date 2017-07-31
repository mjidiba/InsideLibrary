package com.example.ahmedaminemajdoubi.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.Map;

/**
 * Created by amineabboudi on 7/18/17.
 */

public class BlueDotClass extends SubsamplingScaleImageView {
    Paint paint = new Paint();
    private float radius = 1.0f;
    private PointF dotCenter = null;
    private  PointF destCenter1=null;
    private PointF destCenter2=null;
    private PointF left_top;
    private PointF right_bottom;
    private int id1=0,id2=0;
    private float accuracy;
    private float bearing;


    public void setAccuracy(float accuracy)
    {
        this.accuracy = accuracy;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setDotCenter(PointF dotCenter) {
        this.dotCenter = dotCenter;
    }

    public void setBearing(float bearing) {
        this.bearing = (float) ((bearing)*(Math.PI/180));

    }

    public void setDestCenter(PointF destCenter1, PointF destCenter2) {
        this.destCenter1=destCenter1;
        this.destCenter2=destCenter2;
    }

    public void setId1(int id1)
    {this.id1=id1;}

    public void setId2(int id2)
    {this.id2=id2;}


    public BlueDotClass(Context context) {
        this(context, null);
    }

    public BlueDotClass(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    private void initialise() {
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady()) {
            return;
        }
        float pix= MapActivity.mFloorPlan.getMetersToPixels();


        if (dotCenter != null) {
            PointF vPoint = sourceToViewCoord(dotCenter);
            float scaledRadius = getScale() * radius;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(getResources().getColor(R.color.title));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7);
            canvas.drawCircle(vPoint.x, vPoint.y, getScale()*accuracy, paint);
            paint.setColor(getResources().getColor(R.color.ia_blue));
            paint.setAlpha(100);
            canvas.drawCircle(vPoint.x, vPoint.y, getScale()*accuracy, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.white));
            canvas.drawCircle(vPoint.x, vPoint.y, 1.2f*scaledRadius, paint);
            paint.setAntiAlias(true);
            paint.setColor(getResources().getColor(R.color.title));
            canvas.drawCircle(vPoint.x, vPoint.y, scaledRadius, paint);
            paint.setStrokeWidth(10);
            paint.setColor(getResources().getColor(R.color.title));
            double startx = scaledRadius*Math.cos(bearing) + vPoint.x;
            double starty = scaledRadius*Math.sin(bearing) + vPoint.y;
            double endx = 2*scaledRadius*Math.cos(bearing) + vPoint.x;
            double endy = 2*scaledRadius*Math.sin(bearing) + vPoint.y;
            //canvas.drawLine((float) startx, (float) starty, (float) endx, (float) endy, paint);
            canvas.drawLine((float) endx, (float) endy,(float) (endx -((endx-startx)*Math.cos(Math.toRadians(55)) - (endy-starty)*Math.sin(Math.toRadians(55)))*1.5) ,(float) (endy -((endy-starty)*Math.cos(Math.toRadians(55)) + (endx-startx)*Math.sin(Math.toRadians(55)))*1.5),paint);
            canvas.drawLine((float) endx, (float) endy,(float) (endx -((endx-startx)*Math.cos(Math.toRadians(55)) + (endy-starty)*Math.sin(Math.toRadians(55)))*1.5) ,(float) (endy -((endy-starty)*Math.cos(Math.toRadians(55)) - (endx-startx)*Math.sin(Math.toRadians(55)))*1.5),paint);

        }
        if(MapActivity.mFloorPlan!=null) {
            if (MapActivity.mFloorPlan.getFloorLevel()==1) {
                paint.setAntiAlias(true);

                for (int i = 0; i < 6; i++) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(getResources().getColor(R.color.maron));
                    left_top = new PointF((MapActivity.X1 - 1.11f - 2.23f * i - ((i>=3) ? 1:0 ) *3.6f) * pix, (MapActivity.Y1 - 0.4f) * pix);
                    right_bottom = new PointF((MapActivity.X1 + 1.11f - 2.23f * i - ((i>=3) ? 1:0 )*3.6f) * pix, (MapActivity.Y1 + 0.4f) * pix);
                    left_top.set(MapActivity.setPos(left_top));
                    right_bottom.set(MapActivity.setPos(right_bottom));
                    left_top.set(sourceToViewCoord(left_top));
                    right_bottom.set(sourceToViewCoord(right_bottom));
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(getResources().getColor(R.color.title));
                    paint.setStrokeWidth(5);
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                }


                for (int i = 0; i < 9; i++) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(getResources().getColor(R.color.maron));
                    left_top.set(new PointF((MapActivity.X2 - 1f + 2f * i) * pix, (MapActivity.Y2 - 0.4f) * pix));
                    right_bottom.set(new PointF((MapActivity.X2 + 1f + 2f * i) * pix, (MapActivity.Y2 + 0.4f) * pix));
                    left_top.set(MapActivity.setPos(left_top));
                    right_bottom.set(MapActivity.setPos(right_bottom));
                    left_top.set(sourceToViewCoord(left_top));
                    right_bottom.set(sourceToViewCoord(right_bottom));
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(getResources().getColor(R.color.title));
                    paint.setStrokeWidth(5);
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                }
            }
            else if (MapActivity.mFloorPlan.getFloorLevel()==2)
            {
                for (int i = 0; i < 9; i++)
                {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(getResources().getColor(R.color.maron));
                    left_top = new PointF((MapActivity.X3 - 1.11f + 2.23f * i) * pix, (MapActivity.Y3 - 0.4f) * pix);
                    right_bottom=new PointF((MapActivity.X3 + 1.11f + 2.23f * i) * pix, (MapActivity.Y3 + 0.4f) * pix);
                    left_top.set(MapActivity.setPos(left_top));
                    right_bottom.set(MapActivity.setPos(right_bottom));
                    left_top.set(sourceToViewCoord(left_top));
                    right_bottom.set(sourceToViewCoord(right_bottom));
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(getResources().getColor(R.color.title));
                    paint.setStrokeWidth(5);
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                }
                for (int i = 0; i < 6; i++)
                {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(getResources().getColor(R.color.maron));
                    left_top = new PointF((MapActivity.X4 - 1.11f - 2.23f * i - ((i>=3) ? 1:0 ) *6.83f) * pix, (MapActivity.Y4 - 0.4f) * pix);
                    right_bottom = new PointF((MapActivity.X4 + 1.11f - 2.23f * i - ((i>=3) ? 1:0 )*6.83f) * pix, (MapActivity.Y4 + 0.4f) * pix);
                    left_top.set(MapActivity.setPos(left_top));
                    right_bottom.set(MapActivity.setPos(right_bottom));
                    left_top.set(sourceToViewCoord(left_top));
                    right_bottom.set(sourceToViewCoord(right_bottom));
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(getResources().getColor(R.color.title));
                    paint.setStrokeWidth(5);
                    canvas.drawRect(left_top.x, left_top.y, right_bottom.x, right_bottom.y, paint);
                }
            }
        }

        if (destCenter1 != null) {
            PointF vPoint = destCenter1;
            if(id1==1)
            {       Log.e("The Id of", String.valueOf(id1));
                    left_top=new PointF(vPoint.x - 1.11f*pix,vPoint.y-0.4f*pix);
                    right_bottom=new PointF(vPoint.x + 1.11f*pix,vPoint.y+0.4f*pix);
            }
            else
                {
                    left_top=new PointF(vPoint.x - 1f*pix,vPoint.y-0.4f*pix);
                    right_bottom=new PointF(vPoint.x + 1f*pix,vPoint.y+0.4f*pix);
                }
            left_top=sourceToViewCoord(left_top);
            right_bottom=sourceToViewCoord(right_bottom);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.bg_screen2));
            canvas.drawRect(left_top.x,left_top.y,right_bottom.x,right_bottom.y,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getResources().getColor(R.color.title));
            paint.setStrokeWidth(5);
            canvas.drawRect(left_top.x,left_top.y,right_bottom.x,right_bottom.y,paint);

        }

        if (destCenter2 != null) {
            PointF vPoint = destCenter2;
            if(id2==1)
            {
                left_top=new PointF(vPoint.x - 1.11f*pix,vPoint.y-0.4f*pix);
                right_bottom=new PointF(vPoint.x + 1.11f*pix,vPoint.y+0.4f*pix);
            }
            else
            {
                left_top=new PointF(vPoint.x - 1f*pix,vPoint.y-0.4f*pix);
                right_bottom=new PointF(vPoint.x + 1f*pix,vPoint.y+0.4f*pix);
            }
            left_top=sourceToViewCoord(left_top);
            right_bottom=sourceToViewCoord(right_bottom);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.dest_circle));
            canvas.drawRect(left_top.x,left_top.y,right_bottom.x,right_bottom.y,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getResources().getColor(R.color.title));
            paint.setStrokeWidth(5);
            canvas.drawRect(left_top.x,left_top.y,right_bottom.x,right_bottom.y,paint);

        }



    }


}
