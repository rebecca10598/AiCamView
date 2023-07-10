package com.example.translate_objecttext;

import static android.app.Activity.RESULT_OK;
import static com.example.translate_objecttext.Constant.BASE_URL;

import android.Manifest;
import android.app.Activity;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
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
import java.util.List;
import java.util.Locale;

//Summary - Fragment Java class that holds the UI of the OBJECT part in the app's main screen

public class ObjectFragment_crv extends Fragment implements ILoadImage, View.OnClickListener {

    //Cloud Connection
    private static final String CLOUD_VISION_API_KEY = Constant.KEY;
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 1;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = ObjectFragment_crv.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    //UI
    ImageView obj_frm_img;
    private Bitmap bitmap;
    String lng = null;

    private ProgressDialog progressDialog;

    LinearLayout bottomsheetLayout;
    BottomSheetBehavior bottomSheetBehavior;
    FloatingActionButton object_fbutton;

    Context context;

    Uri photoUri = null;
    Intent d = null;

    public ObjectFragment_crv() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_object_crv, container, false);
        obj_frm_img = view.findViewById(R.id.obj_frm_img);
        object_fbutton = view.findViewById(R.id.object_fbutton);

        bottomsheetLayout = (LinearLayout) view.findViewById(R.id.bottomsheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetLayout);

        detectedTxt = view.findViewById(R.id.detectedTxt);
        translatedTxt = view.findViewById(R.id.translatedTxt);
        detectSpeaker = view.findViewById(R.id.detectSpeaker);
        translateSpeaker = view.findViewById(R.id.translateSpeaker);

        detectedTxt.setMovementMethod(new ScrollingMovementMethod());
        translatedTxt.setMovementMethod(new ScrollingMovementMethod());

        detectSpeaker.setOnClickListener(this);
        translateSpeaker.setOnClickListener(this);
        object_fbutton.setOnClickListener(this);

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


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Detect object And translate detected object name and convert into Selected language
            ((ObjectTextCurved)getActivity()).setFragmentRefreshListener(new ObjectTextCurved.FragmentRefreshListener() {
                @Override
                public void onRefresh() {
                    if(lng != null){
                        if(!detectedTxt.getText().toString().equals("abc")){
                            getData(detectedTxt.getText().toString(), lng);
                        }
                    } else {
                        obj_frm_img.setImageDrawable(getResources().getDrawable(R.drawable.image_preview));
                    }
                }
            });
    }

    @Override
    public void onLoadImage(String lng) {
        this.lng = lng;
    }

    //Detect object And translate detected object name and convert into Selected language
    private void callCloudVision(final Bitmap bitmap) {
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(ObjectFragment_crv.this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.object_fbutton:
                 alertDialog();
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

    private void alertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_select_prompt)
                .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
        builder.create().show();
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(getActivity(), GALLERY_PERMISSIONS_REQUEST)) {
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
            photoUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", getCameraFile());
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
            d = data;
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
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

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
                obj_frm_img.setImageBitmap(bitmap);

            } catch (IOException e) {
                Toast.makeText(getContext(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
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
        private final WeakReference<ObjectFragment_crv> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(ObjectFragment_crv activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(progressDialog != null){
                if(progressDialog.isShowing()){
                    progressDialog.cancel();
                }
            }
            progressDialog = new ProgressDialog(getContext(), R.style.progressDialog);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG,  e.getContent());
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            ObjectFragment_crv activity = mActivityWeakReference.get();
            if (activity != null && !activity.isDetached()) {
                showBottomSheetDialog(result);
            }
        }
    }

    //Convert response into string
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%s", label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }
        return message.toString();
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
                labelDetection.setType("LABEL_DETECTION");
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

    //BottomSheet UI
    TextView detectedTxt;
    TextView translatedTxt;
    ImageView detectSpeaker;
    ImageView translateSpeaker;
    TextToSpeech textToSpeech;

    //BottomSheet Dialog For Deteceted and Translated Text
    private void showBottomSheetDialog(String result) {
        if(!result.equals("abc")){
            detectedTxt.setText(result);
            getData(result, lng);
            progressDialog.dismiss();
        }
    }

    //Get Translated Data From API
    public void getData(String text, String lng){
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
                                if(progressDialog != null){
                                    progressDialog.dismiss();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                    }
                }
            },error -> {
                progressDialog.dismiss();
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
            progressDialog.dismiss();
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
            progressDialog.dismiss();
        } else {
            progressDialog.dismiss();
        }

    }
}