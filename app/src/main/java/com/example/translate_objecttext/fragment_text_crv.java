package com.example.translate_objecttext;

import static android.app.Activity.RESULT_OK;
import static com.example.translate_objecttext.Constant.BASE_URL;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

//Summary - Class where language selection is described, in which we can get a response in the same language we selected
public class fragment_text_crv extends Fragment implements ILoadImage, View.OnClickListener {

    //Cloud Connection
    private static final String CLOUD_VISION_API_KEY = Constant.KEY;
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 1;

    //UI
    ImageView txt_frm_img;
    private Bitmap bitmap;
    String lng = null;
    FloatingActionButton fab;

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private static final String TAG = fragment_text_crv.class.getSimpleName();

    private ProgressDialog progressDialog2;

    LinearLayout bottomsheetLayout;
    BottomSheetBehavior bottomSheetBehavior;

    Context context;

    private boolean isGallery = false;
    private String imageURL = "NA";
    private String extension;
    String picPath, name, fPath;
    byte[] imageBytes;
    private static final int MAX_DIMENSION = 500;
    boolean isProfileClicked = false;
    String imageString = "";
    Intent data = null;
    Bitmap newbitmap = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public fragment_text_crv() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_crv, container, false);
        txt_frm_img = view.findViewById(R.id.txt_frm_img);

        bottomsheetLayout = (LinearLayout) view.findViewById(R.id.bottomsheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetLayout);

        detectedTxt = view.findViewById(R.id.detectedTxt);
        translatedTxt = view.findViewById(R.id.translatedTxt);
        detectSpeaker = view.findViewById(R.id.detectSpeaker);
        translateSpeaker = view.findViewById(R.id.translateSpeaker);
        fab = view.findViewById(R.id.text_fbutton);

        detectedTxt.setMovementMethod(new ScrollingMovementMethod());
        translatedTxt.setMovementMethod(new ScrollingMovementMethod());

        detectSpeaker.setOnClickListener(this);
        translateSpeaker.setOnClickListener(this);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomsheetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        bottomSheetBehavior.setDraggable(false);
        fab.setOnClickListener(this);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();



        //Detect Text And translate detected text
        ((ObjectTextCurved)getActivity()).setFragmentRefreshListener(new ObjectTextCurved.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                if(lng != null){
                    if(!detectedTxt.getText().toString().equals("abc")){
                        getData(detectedTxt.getText().toString(), lng);
                    }
                } else {
                    txt_frm_img.setImageDrawable(getResources().getDrawable(R.drawable.image_preview));
                }
            }
        });

    }

    @Override
    public void onLoadImage(String lng) {
        this.lng = lng;
    }

    //Detect Text And translate detected text
    private void callCloudVision(Bitmap bitmap) {
        try {
            AsyncTask<Object, Void, String> labelDetectionTask =
                    new LableDetectionTask(fragment_text_crv.this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_fbutton:
                onImageUploadClick();
                break;
            case R.id.detectSpeaker:
                if(!detectedTxt.getText().toString().equals(null)){
                    textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int i) {
                            if(i != TextToSpeech.ERROR){
                                textToSpeech.setLanguage(Locale.forLanguageTag("en"));
                                textToSpeech.speak(detectedTxt.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                            }
                        }
                    });
                }
                break;
            case R.id.translateSpeaker:
                if(!translatedTxt.getText().toString().equals(null)){
                    textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int i) {
                            if(i != TextToSpeech.ERROR){
                                textToSpeech.setLanguage(Locale.forLanguageTag(lng));
                                textToSpeech.speak(translatedTxt.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                            }
                        }
                    });
                }
                break;
        }
    }

    private void onImageUploadClick() {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Select Image")
                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGalleryChooser();
                    }
                }).setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startCamera();
            }
        });
        builder.create().show();
    }

    private void startGalleryChooser() {
        if (PermissionUtils.requestPermission(getActivity(), GALLERY_PERMISSIONS_REQUEST)) {
//            isGallery = true;
//            Intent intent = new Intent();
//            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
//                    GALLERY_IMAGE_REQUEST);

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                getActivity(),
                CAMERA_PERMISSIONS_REQUEST)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if(data.getData() != null){
                setImg(data.getData());
            }
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", getCameraFile());
            if(photoUri != null){
                setImg(photoUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void setImg(Uri uri) {
        if (uri != null) {
            try {
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri),
                                MAX_DIMENSION);


                newbitmap = bitmap;
                txt_frm_img.setImageBitmap(bitmap);
                callCloudVision(bitmap);
                Log.d("img", String.valueOf(newbitmap));
            } catch (IOException e) {
                Toast.makeText(getContext(), "Selecting image failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Error selecting an image  ", Toast.LENGTH_LONG).show();
        }
    }


    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) throws IOException{
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<fragment_text_crv> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(fragment_text_crv activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(progressDialog2 != null){
                if(progressDialog2.isShowing()){
                    progressDialog2.cancel();
                }
            }
            progressDialog2 = new ProgressDialog(getContext(), R.style.progressDialog);
            progressDialog2.setCancelable(false);
            progressDialog2.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog2.show();
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            fragment_text_crv activity = mActivityWeakReference.get();
            if (activity != null && !activity.isDetached()) {
                showBottomSheetDialog(result);
            }
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getActivity().getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);
                        String sig = PackageManagerUtils.getSignature(getActivity().getPackageManager(), packageName);
                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            Image base64EncodedImage = new Image();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("DOCUMENT_TEXT_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});



            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        annotateRequest.setDisableGZipContent(true);

        return annotateRequest;
    }

    //Convert response into string
    String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder();
        TextAnnotation fultexts = response.getResponses().get(0).getFullTextAnnotation();
        if (fultexts != null) {
            FullTextPojo fullTextPojo = new FullTextPojo(fultexts.getText());
            message.append(String.format(Locale.US, "%s", fullTextPojo.getFullText()));
            message.append("\n");
        } else {
            message.append("nothing");
        }
        return message.toString();
    }

    //BottomSheet UI
    TextView detectedTxt;
    TextView translatedTxt;
    ImageView detectSpeaker;
    ImageView translateSpeaker;
    TextToSpeech textToSpeech;

    //BottomSheet Dialog For Deteceted and Translated Text
    private void showBottomSheetDialog(String result) {
        detectedTxt.setText(result);
        getData(result, lng);
    }

    //Get Translated Data From API
    private void getData(String text, String lng){
        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String URL = BASE_URL;
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("q", text);
            jsonBody.put("target", lng);
            final String requestBody = jsonBody.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(Helper.checkConnection(response)){
                        Pair<String, String> pair = Helper.GetErrorMessage(response);
                        Helper.ShowAlertDialog(getContext(), pair.first, pair.second, false);
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject jsonObj = jsonObject.getJSONObject("data");
                            JSONArray jsonArray = jsonObj.getJSONArray("translations");
                            for(int i = 0 ; i < jsonArray.length(); i++){
                                JSONObject jObj = jsonArray.getJSONObject(i);
                                String translatedText = jObj.getString("translatedText");
                                translatedTxt.setText(translatedText);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                progressDialog2.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog2.dismiss();
                        }
                    }
                }
            },error -> {
                progressDialog2.dismiss();
                if(Helper.checkConnection(error.getMessage())){
                    Pair<String, String> pair = Helper.GetErrorMessage(error.getMessage());
                    Helper.ShowAlertDialog(getContext(), pair.first, pair.second, false);
                }else{
                    ShowConnErrorMsg(getContext(), "Unable To Connect", "Check your Internet Connection", "Ok");
                }
            }){

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

            };
            queue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog2.dismiss();
        }

    }

    //Get Any Error Message For Fetching Any Language
    public void ShowConnErrorMsg(Context context, String title, String message, String positive){
        android.app.AlertDialog.Builder aDialogBuilder = new android.app.AlertDialog.Builder(context);
        aDialogBuilder.setTitle(title);
        aDialogBuilder.setMessage(message);
        aDialogBuilder.setIcon(R.drawable.ic_error);
        aDialogBuilder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog alertDialog = aDialogBuilder.create();
        if(!alertDialog.isShowing()){
            alertDialog.show();
            progressDialog2.dismiss();
        } else {
            progressDialog2.dismiss();
        }
    }
}