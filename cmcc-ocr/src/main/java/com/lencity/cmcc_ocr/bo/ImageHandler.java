package com.lencity.cmcc_ocr.bo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;


public class ImageHandler {

	/* Pre-process photo for memory optimization and set to ImageView */
	public Bitmap setPicFromPath(ImageView mImageView, String mCurrentPhotoPath) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		// scale pic
		bmOptions.inSampleSize = 8;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(bitmap);
		mImageView.setVisibility(View.VISIBLE);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmap;
	}

	/* Pre-process photo for memory optimization and set to ImageView */
	public Bitmap setPicFromByteArr(ImageView identifyResultImageView, byte[] imgByteArr) {
		/* Decode the JPEG file into a Bitmap */
		//Bitmap bitmap = BitmapFactory.decodeByteArray(imgByteArr, 0, imgByteArr.length, bmOptions);
		Bitmap bitmap = BitmapFactory.decodeByteArray(imgByteArr, 0, imgByteArr.length);

		/* Associate the Bitmap to the ImageView */
		identifyResultImageView.setImageBitmap(bitmap);
		//mVideoUri = null;
		identifyResultImageView.setVisibility(View.VISIBLE);
		//mVideoView.setVisibility(View.INVISIBLE);

		return bitmap;
	}

	/* Recycle Bitmap for ImageView */
	public void recycleBitmap(ImageView mImageView, Bitmap bitmap) {
		mImageView.setImageBitmap(null);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	/* Recycle Bitmap */
	public void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	/* Bitmap -> byte[] */
	public byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/* byte[] -> Bitmap */
	public Bitmap bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/* byte[] -> Bitmap, using BitmapFactory.Options */
	public Bitmap bytes2Bimap(byte[] b, BitmapFactory.Options bmOptions) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length, bmOptions);
		} else {
			return null;
		}
	}

	public byte[] scaleImageByteArrByN(byte[] imageByteArr, int n) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = n;

		Bitmap bitmap = bytes2Bimap(imageByteArr, bmOptions);
		imageByteArr = bitmap2Bytes(bitmap);

		recycleBitmap(bitmap);

		return imageByteArr;
	}

	/*public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
		int rotate = 0;
		try {
			context.getContentResolver().notifyChange(imageUri, null);
			File imageFile = new File(imagePath);

			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;
			}

			Log.i("RotateImage", "Exif orientation: " + orientation);
			Log.i("RotateImage", "Rotate value: " + rotate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotate;
	}*/

	/**
	 * 旋转Bitmap
	 * @param b
	 * @param rotateDegree
	 * @return
	 */
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
		Matrix matrix = new Matrix();
		matrix.postRotate((float) rotateDegree);
		Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
		return rotaBitmap;
	}


}

