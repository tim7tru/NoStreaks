package com.example.snapchat_clone;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class sendFragment extends Fragment implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    ImageView captureButton;
    ImageView imageView;
    ImageView deleteImage;
    ImageView sendButton;

    // image count
    int imageCount = 0;

    // camera variables
    Camera.PictureCallback jpegCallback;
    final int CAMERA_REQUEST_CODE = 1;
    int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;

    // firebase storage
    StorageReference storageReference;

    //camera switch
    ImageView cameraSwitch;



    public sendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View sendView = inflater.inflate(R.layout.fragment_send, container, false);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // initializing widgets
        surfaceView = sendView.findViewById(R.id.surfaceView);
        captureButton = sendView.findViewById(R.id.captureButton);
        imageView = sendView.findViewById(R.id.snapView);
        deleteImage = sendView.findViewById(R.id.deleteImage);
        sendButton = sendView.findViewById(R.id.sendButton);
        cameraSwitch = sendView.findViewById(R.id.cameraSwitch);

        // Firebase Storage Reference
        storageReference = FirebaseStorage.getInstance().getReference();

        /*
        Function that runs when a picture is taken
         */
        jpegCallback = new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                // to construct the picture in the preview using a bitmap (imageView)
                if (data != null) {
                    // converting the picture taken into a bitmap
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);

                    // changing interface view
                    surfaceView.setVisibility(View.INVISIBLE);
                    captureButton.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    deleteImage.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    cameraSwitch.setVisibility(View.INVISIBLE);

                    // Rotate the Image
                    final Bitmap rotatedBitmap = rotate(bitmap);

                    // Set the image to the imageView
                    imageView.setImageBitmap(rotatedBitmap);

                    // To send the image to a user when the send button is tapped -> saves the images in firebase storage
                    sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // move to the SendUserListActivity to select which user you would like to send the image to

                            // creating a temporary file image to save memory -> bytearray is too large to be passed into the intent
                            String filePath = tempFileImage (getActivity(), rotatedBitmap, "photo");

                            // intent to the SendUserListActivity to choose who to send the image to
                            Intent intent = new Intent(getActivity(), SendUserListActivity.class);
                            intent.putExtra("data", filePath);
                            startActivity(intent);
                            bitmap.recycle();
                        }
                    });


                }
            }
        };

        /*
            To delete the picture taken and to go back to the previous screen to take another picture
         */
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButton.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                deleteImage.setVisibility(View.INVISIBLE);
                surfaceView.setVisibility(View.VISIBLE);
                captureButton.setVisibility(View.VISIBLE);
                cameraSwitch.setVisibility(View.VISIBLE);
            }
        });

        // To capture the image when the button is pressed and to show it in the preview
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        surfaceHolder = surfaceView.getHolder();

        // Setting the Camera Permissions
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // No permission for camera -> need to ask permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            // There was permission granted for the camera
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        /*
        Click listener to change the camera from back/front
         */
        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the current camera ID
                camera.stopPreview();

                // need to release camera first or app will crash
                camera.release();

                // swap the id of the camera to be used
                if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
                }

                camera = Camera.open(cameraID);

                Camera.Parameters parameters = camera.getParameters();

                //refresh rate
                parameters.setPreviewFrameRate(30);

                // focus
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                // To find the best image size for your camera
                Camera.Size bestSize = null;
                List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
                bestSize = sizeList.get(0);

                for (int i = 1; i < sizeList.size(); i++) {
                    if (sizeList.get(i).width * sizeList.get(i).height > bestSize.width * bestSize.height) {
                        bestSize = sizeList.get(i);
                    }
                }

                parameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(parameters);

                // set the display orientation
                setCameraDisplayOrientation(getActivity(), cameraID, camera);

                // setting the preview display
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                camera.startPreview();

            }
        });

        return sendView;
    }

    /*
    Creating a temporary file to store the byte array of image taken
    - this temporary file is what is sent in the intent to the SendUserListActivity
    - this is done because byte array is too large to be sent as an intent
     */
    private String tempFileImage(FragmentActivity activity, Bitmap rotatedBitmap, String photo) {
        File outputDirectory = activity.getCacheDir();
        File imageFile = new File(outputDirectory, photo + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG,100,os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
}

        return imageFile.getAbsolutePath();
                }

    /*
    Function to rotate the bitmap to the proper orientation depending on if the image was taken from the front or back camera
     */
    private Bitmap rotate(Bitmap bitmap) {
        // to get width and height of bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // setting the rotation if image was taken from the back camera
        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        // setting the rotation for the front camera
        if (cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Matrix frontMatrix = new Matrix();
            // preventing the mirror effect
            frontMatrix.preScale(-1.0f, 1.0f);
            // rotating the bitmap
            frontMatrix.postRotate(90);
            return Bitmap.createBitmap(bitmap, 0,0,width,height,frontMatrix,true);
        }

        // returns bitmap of image taken from back camera
        return Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
    }

    /*
    Function that takes the picture and saves it as a jpeg
     */
    private void captureImage() {
        // to take the picture
        camera.takePicture(null,null,jpegCallback);
    }

    /*
    Setting up the camera into the surface view when the surfaceView is created
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // To open the camera
        camera = Camera.open(cameraID);
        Camera.Parameters parameters = camera.getParameters();

        // set the orientation of the camera

        //refresh rate
        parameters.setPreviewFrameRate(30);

        // focus
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        // To find the best image size for your camera
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            if (sizeList.get(i).width * sizeList.get(i).height > bestSize.width * bestSize.height) {
                bestSize = sizeList.get(i);
            }
        }

        parameters.setPreviewSize(bestSize.width, bestSize.height);
        camera.setParameters(parameters);

        // setting up the display orientation depending if the image was taken from the back or front camera
        setCameraDisplayOrientation(getActivity(), cameraID, camera);

        // setting the preview display
        try {
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.startPreview();

    }

    /*
    Function to set the camera display orientation depending if it was the front or back camera
     */
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // There was permission granted for the camera
                    surfaceHolder.addCallback(this);
                    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                } else {
                    Toast.makeText(getContext(), "Please provide camera permission", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
