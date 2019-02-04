package com.example.snapchat_clone;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    // firebase storage
    StorageReference storageReference;

    // firebase storage file path
    String FIREBASE_IMAGE_STORAGE = "photos/users/";

    // firebase database

    public sendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View sendView = inflater.inflate(R.layout.fragment_send, container, false);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        surfaceView = sendView.findViewById(R.id.surfaceView);
        captureButton = sendView.findViewById(R.id.captureButton);
        imageView = sendView.findViewById(R.id.imageView);
        deleteImage = sendView.findViewById(R.id.deleteImage);
        sendButton = sendView.findViewById(R.id.sendButton);

        // Firebase Storage Reference
        storageReference = FirebaseStorage.getInstance().getReference();

        jpegCallback = new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                // to construct the picture in the preview using a bitmap (imageView)
                if (data != null) {
                    // converting the picture taken into a bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);

                    // changing interface view
                    surfaceView.setVisibility(View.INVISIBLE);
                    captureButton.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    deleteImage.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);

                    // Rotate the Image
                    Bitmap rotatedBitmap = rotate(bitmap);

                    // Set the image to the imageView
                    imageView.setImageBitmap(rotatedBitmap);

                    // To send the image to a user -> saves the images in firebase storage
                    sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // move to the SendUserListActivity to select which user you would like to send the image to
                            Intent intent = new Intent(getActivity(), SendUserListActivity.class);
                            intent.putExtra("data", data);
                            startActivity(intent);
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

        // count the number of images already located in the users database
//        mPhotos.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot ds : dataSnapshot
//                        .child("").getChildren()) {
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        return sendView;
    }

    private Bitmap rotate(Bitmap bitmap) {
        // to get width and height of bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        return Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
    }

    private void captureImage() {
        // to take the picture
        camera.takePicture(null,null,jpegCallback);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // To open the camera
        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();

        // set the orientation of the camera
        camera.setDisplayOrientation(90);

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

        // setting the preview display
        try {
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.startPreview();

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
