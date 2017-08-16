package ConfigClasses;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Dylan Castanhinha on 3/30/2017.
 */

public class MyProfilePictureView extends ImageView {


    public MyProfilePictureView(Context context) {
        super(context);
    }

    public MyProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyProfilePictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Bitmap getRoundedBitmap(Bitmap bitmap) {
        if (bitmap == null){
            setBlankProfilePicture();
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public void setBlankProfilePicture(){
        int blankImageResource = com.facebook.R.drawable.com_facebook_profile_picture_blank_portrait;
        Bitmap blankprofilebmp = BitmapFactory.decodeResource(Resources.getSystem(), blankImageResource);
        getRoundedBitmap(blankprofilebmp);
    }

}
