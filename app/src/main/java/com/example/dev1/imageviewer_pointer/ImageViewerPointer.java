package com.example.dev1.imageviewer_pointer;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageViewerPointer extends android.support.v7.widget.AppCompatImageView {
    private PointF viewSize = new PointF(0,0);
    private PointF currSize = new PointF(0,0);
    private PointF origSize = new PointF(0,0);
    private PointF cursorPos = new PointF(0,0);

    private float minScale = 0.8f;
    private float maxScale = 4.0f;

    private PointF lastFocus = new PointF(0.0f,0.0f);
    private PointF lastCursor = new PointF(0.0f,0.0f);
    private static Matrix initMatrix;
    private Matrix matrix;

    private Paint mTextPaint;
    private int mTextColor = Color.argb(255,0,0,255);

    // EVENT HANDLER
    class GestureListener implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
//            Toast.makeText(getContext(),"onDown",Toast.LENGTH_SHORT).show();
            return true;
        }
        @Override
        public void onShowPress(MotionEvent motionEvent) {
//            Toast.makeText(getContext(),"onShowPress",Toast.LENGTH_SHORT).show();
        }
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
//            Toast.makeText(getContext(),"onSingleTapUp",Toast.LENGTH_SHORT).show();
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Toast.makeText(getContext(),"onScroll",Toast.LENGTH_SHORT).show();
            float[] f = new float[9];
            Matrix oldMatrix = getImageMatrix();
            Matrix transformMatrix = new Matrix();

            transformMatrix.postTranslate(-distanceX, -distanceY);
            oldMatrix.postConcat(transformMatrix);

            oldMatrix.getValues(f);
            final float ratioX2 = (viewSize.x/2f - f[Matrix.MTRANS_X]) / currSize.x ;
            final float ratioY2 = (viewSize.y/2f - f[Matrix.MTRANS_Y]) / currSize.y ;
            f[Matrix.MTRANS_X] = (ratioX2 > 1f)
                    ? (viewSize.x/2f - currSize.x)
                    : ((ratioX2 < 0f) ? viewSize.x/2f : f[Matrix.MTRANS_X]);
            f[Matrix.MTRANS_Y] = (ratioY2 > 1f)
                    ? (viewSize.y/2f - currSize.y)
                    : ((ratioY2 < 0f) ? viewSize.y/2f : f[Matrix.MTRANS_Y]);
            matrix.setValues(f);
            return true;
        }
        @Override
        public void onLongPress(MotionEvent motionEvent) {
//            Toast.makeText(getContext(),"onLongPress",Toast.LENGTH_SHORT).show();
        }
        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
//            Toast.makeText(getContext(),"onFling",Toast.LENGTH_SHORT).show();
            return false;
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
//            Toast.makeText(getContext(),"onSingleTapConfirmed",Toast.LENGTH_SHORT).show();
            final Matrix beginMatrix = getImageMatrix();
            final Matrix transformMatrix = new Matrix();
            float[] f = new float[9];
            matrix.getValues(f);
            PointF newPos = new PointF(
                    (motionEvent.getX() - 0.5f * viewSize.x),
                    (motionEvent.getY() - 0.5f * viewSize.y));
            transformMatrix.postTranslate(-newPos.x, -newPos.y);
            matrix.postConcat(transformMatrix);
            matrix.getValues(f);
            final float ratioX2 = (viewSize.x / 2f - f[Matrix.MTRANS_X]) / currSize.x;
            final float ratioY2 = (viewSize.y / 2f - f[Matrix.MTRANS_Y]) / currSize.y;
            if(ratioX2 <= 1f && ratioX2 >= 0f && ratioY2 <= 1f && ratioY2 >= 0f)
            {
                ObjectAnimator objectAnimator = ObjectAnimator.ofObject(ImageViewerPointer.this, MatrixEvaluator.ANIMATED_TRANSFORM_PROPERTY, new MatrixEvaluator(), beginMatrix, matrix);
                objectAnimator.setDuration(1000);
                objectAnimator.start();
            } else {
                beginMatrix.getValues(f);
                matrix.setValues(f);
            }
            return true ;
        }
        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            float f[] = new float[9];
            initMatrix.getValues(f);
            matrix.setValues(f);
            return true;
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
//            Toast.makeText(getContext(),"onDoubleTapEvent",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private GestureDetector mGestureDetector = new GestureDetector(ImageViewerPointer.this.getContext(), new GestureListener());

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector){
            float[] f = new float[9];
            Matrix oldMatrix = getImageMatrix();
            Matrix transformMatrix = new Matrix();

            final PointF currFocus = new PointF(detector.getFocusX(),detector.getFocusY());
            // Zoom focus is where the fingers are centered,
            transformMatrix.postTranslate(-1f*currFocus.x,-1f*currFocus.y);
            transformMatrix.postScale(detector.getScaleFactor(),detector.getScaleFactor());
            // Adding focus shift to allow for scrolling with two pointers down
            transformMatrix.postTranslate(  currFocus.x + (currFocus.x - lastFocus.x),
                                            currFocus.y + (currFocus.y - lastFocus.y));
            oldMatrix.postConcat(transformMatrix);

            oldMatrix.getValues(f);
            if( f[Matrix.MSCALE_X] < maxScale && f[Matrix.MSCALE_X] > minScale) {
                matrix.postConcat(transformMatrix);
            }
            lastFocus = currFocus;
            f = new float[9];
            matrix.getValues(f);
            final float ratioX2 = (viewSize.x/2f - f[Matrix.MTRANS_X]) / currSize.x ;
            final float ratioY2 = (viewSize.y/2f - f[Matrix.MTRANS_Y]) / currSize.y ;
            f[Matrix.MTRANS_X] = (ratioX2 > 1f)
                                ? (viewSize.x/2f - currSize.x)
                                : ((ratioX2 < 0f) ? viewSize.x/2f : f[Matrix.MTRANS_X]);
            f[Matrix.MTRANS_Y] = (ratioY2 > 1f)
                                ? (viewSize.y/2f - currSize.y)
                                : ((ratioY2 < 0f) ? viewSize.y/2f : f[Matrix.MTRANS_Y]);
            matrix.setValues(f);
            return true;
        }
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastFocus = new PointF(detector.getFocusX(),detector.getFocusY());
            return super.onScaleBegin(detector);
        }
//        @Override
//        public void onScaleEnd(ScaleGestureDetector detector) {
//            super.onScaleEnd(detector);
//        }
    }
    private ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(ImageViewerPointer.this.getContext(), new ScaleListener());

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        final int action = event.getAction();

        switch(action) {
            case MotionEvent.ACTION_DOWN : {
                break;
            }
            case MotionEvent.ACTION_MOVE : {
                mTextColor = Color.argb(255, 255, 0, 0);
                break;
            }
            case MotionEvent.ACTION_UP : {
                mTextColor = Color.argb(255, 0, 0, 0);
                break;
            }
            default :
                break;
        }
        mTextPaint.setColor(mTextColor);
        setImageMatrix(matrix);
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.save();
        // translate, rotate and scale transform here
//        canvas.translate(transXY.x, transXY.y);
//        canvas.scale(scaleZ,scaleZ);
        // draw here
        updateDrawableData();
//
//        float[] f = new float[9];
//        matrix.getValues(f);
//        canvas.drawText(matrix.toString(), 100,100,mTextPaint);
//        canvas.drawText(initMatrix.toString(), 100,150,mTextPaint);
////        canvas.drawText(minScale + " " + f[Matrix.MSCALE_X] + " " + f[Matrix.MSCALE_Y]  + " " + maxScale, 100,200,mTextPaint);
//        canvas.drawText(viewSize.toString() + " " + origSize.toString() + " " + currSize.toString(),100,250,mTextPaint);
//        canvas.drawText(lastCursor.toString() + "",100,300,mTextPaint);
//
        canvas.drawText(cursorPos.toString() + "",viewSize.x/2f,viewSize.y/2f, mTextPaint);
//        canvas.restore();
    }

    /**
     * onMeasure is called to determine the size requirements for this view and all of its children.
     * Here, we scale the drawable to match it with the width of this view.
     * @param w horizontal space requirements as imposed by the parent. The requirements are
     *          encoded with {@link android.view.View.MeasureSpec}
     * @param h vertical space requirements as imposed by the parent. The requirements are encoded
     *          with {@link android.view.View.MeasureSpec}
     */
    @Override
    protected void onMeasure(int w, int h){
        super.onMeasure(w,h);
        matrix.reset();
        initMatrix = getImageMatrix();

        viewSize.x = this.getMeasuredWidth();                                                       // Get this ImageViewPointer size
        viewSize.y = this.getMeasuredHeight();

        final Drawable d = getDrawable();                                                           // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        origSize.x = d.getIntrinsicWidth();                                                         // and so, it original Size in the screen
        origSize.y = d.getIntrinsicHeight();

        float scale = viewSize.x / origSize.x;                                                // We resize the drawable to match with the width of current ImageViewerPointer
        matrix.setScale(scale,scale);
        initMatrix.setScale(scale,scale);
        setImageMatrix(initMatrix);
        updateDrawableData();

        if(cursorPos.y > 0.5){
            float f[] = new float[9];
            initMatrix.getValues(f);
            f[Matrix.MTRANS_Y] = (viewSize.y - currSize.y*scale) * 0.5f;
            initMatrix.setValues(f);
            matrix.setValues(f);
            setImageMatrix(initMatrix);
            updateDrawableData();
        }

        minScale = scale - scale * 0.2f;
        maxScale = scale * 4f;
    }

    /**
     * updateDrawableData allow to update the current size of the drawable and the cursor position
     * in the drawable, converted in a ratio value between 0 and 1
     */
    private void updateDrawableData(){
        final float f[] = new float[9];
        getImageMatrix().getValues(f);

        currSize = new PointF(
                origSize.x * f[Matrix.MSCALE_X],
                origSize.y * f[Matrix.MSCALE_Y]);

        cursorPos = new PointF(
                (viewSize.x/2f - f[Matrix.MTRANS_X]) / currSize.x,
                (viewSize.y/2f - f[Matrix.MTRANS_Y]) / currSize.y);
    }


    // CONSTRUCTOR ---------------------------------------------------------------------------------
    public ImageViewerPointer(Context context) {
        super(context);
        init(context);
    }
    public ImageViewerPointer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public ImageViewerPointer(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context){
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(20f);

        matrix = getImageMatrix();
        updateDrawableData();
        invalidate();
    }
}

class MatrixEvaluator implements TypeEvaluator<Matrix> {

    private static final String TAG = MatrixEvaluator.class.getSimpleName();

    public static TypeEvaluator<Matrix> NULL_MATRIX_EVALUATOR = new TypeEvaluator<Matrix>() {
        @Override
        public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
            return null;
        }
    };

    /**
     * This property is passed to ObjectAnimator when we are animating image matrix of ImageView
     */
    public static final Property<ImageView, Matrix> ANIMATED_TRANSFORM_PROPERTY = new Property<ImageView, Matrix>(Matrix.class,
            "animatedTransform") {

        /**
         * This is copy-paste form ImageView#animateTransform - method is invisible in sdk
         */
        @Override
        public void set(ImageView imageView, Matrix matrix) {
            Drawable drawable = imageView.getDrawable();
            if (drawable == null) {
                return;
            }
            if (matrix == null) {
                drawable.setBounds(0, 0, imageView.getWidth(), imageView.getHeight());
            } else {

                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                Matrix drawMatrix = imageView.getImageMatrix();
                if (drawMatrix == null) {
                    drawMatrix = new Matrix();
                    imageView.setImageMatrix(drawMatrix);
                }
                imageView.setImageMatrix(matrix);
            }
            imageView.invalidate();
        }

        @Override
        public Matrix get(ImageView object) {
            return null;
        }
    };

    float[] mTempStartValues = new float[9];

    float[] mTempEndValues = new float[9];

    Matrix mTempMatrix = new Matrix();

    @Override
    public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
        startValue.getValues(mTempStartValues);
        endValue.getValues(mTempEndValues);
        for (int i = 0; i < 9; i++) {
            float diff = mTempEndValues[i] - mTempStartValues[i];
            mTempEndValues[i] = mTempStartValues[i] + (fraction * diff);
        }
        mTempMatrix.setValues(mTempEndValues);

        return mTempMatrix;
    }
}
