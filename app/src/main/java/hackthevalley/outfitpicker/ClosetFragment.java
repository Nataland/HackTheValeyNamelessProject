package hackthevalley.outfitpicker;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by natalie on 2018-02-24.
 */
public class ClosetFragment extends Fragment {
    //recyclerview object
    private RecyclerView recyclerView;

    //adapter object
    private RecyclerView.Adapter adapter;

    //database reference
    private DatabaseReference mDatabase;

    //progress dialog
    private ProgressDialog progressDialog;

    //list to hold all the uploaded images
    private List<Upload> uploads;

    private FloatingActionButton takePhotoBtn;

    @BindView(R.id.imageCamera)
    ImageView imageCamera;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_closet, container, false);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        takePhotoBtn = view.findViewById(R.id.take_photo_fab);
        takePhotoBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_a_photo_white_24dp));
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FirebaseUploadActivity.class);
                startActivity(intent);
            }
        });
        progressDialog = new ProgressDialog(getContext());

        uploads = new ArrayList<>();

        //displaying progress dialog while fetching images
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

        //adding an event listener to fetch values
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                progressDialog.dismiss();

                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    uploads.add(upload);
                }

                Collections.sort(uploads, new Comparator<Upload>() {
                    @Override
                    public int compare(final Upload object1, final Upload object2) {
                        return object1.getName().compareTo(object2.getName());
                    }
                });

                //creating adapter
                adapter = new MyAdapter(getContext(), uploads);

                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });


        return view;

    }


    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;

    @OnClick(R.id.take_photo_fab)
    void onShowClick(){
        http();
//        takePhoto();
    }

    public void http() {
        OkHttpClient client = new OkHttpClient();
        System.out.println("Fany!!!");
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"url\":\"https://ufpro.com/media/uploads/public/product/-striker_xt_gen2_combat_pants_brown_grey_2.jpg\"}");
        Request request = new Request.Builder()
                .url("https://eastus.api.cognitive.microsoft.com/vision/v1.0/analyze?visualFeatures=Tags&details=Celebrities&language=en")
                .post(body).addHeader("Content-Type", "application/json")
                .addHeader("Ocp-Apim-Subscription-Key", "e7a2369b290c419fbc44a9c5eff066a9")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "d0ad4877-d999-4cbe-8987-c4b4b91994ca")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(response.toString());
                        Log.d("target", response.toString());
                    }
                });
            }

            @Override
            public void onFailure(final Call call, IOException e){
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Error Handle here
                    }
                });
            }
        });
    }
    static final int REQUEST_IMAGE_CAPTURE = 100;

    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
//        imageUri = Uri.fromFile(photo);
//        ClosetFragment.this.startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageCamera.setImageBitmap(imageBitmap);
//            encodeBitmapAndSaveToFirebaseAndSaveToFirebase(imageBitmap);
        }

//        if (requestCode == REQUEST_IMAGE_CAPTURE) {
//            if (resultCode == Activity.RESULT_OK) {
//                Uri selectedImage = imageUri;
//                Log.d("", data.toString());
//                Log.d("", "yoyoyo");
//                getActivity().getContentResolver().notifyChange(selectedImage, null);
//                ContentResolver cr = getActivity().getContentResolver();
//                Bitmap bitmap;
//
//                try {
//                    bitmap = android.provider.MediaStore.Images.Media
//                            .getBitmap(cr, selectedImage);
//
//                    imageCamera.setImageBitmap(bitmap);
//                    Toast.makeText(getActivity(), selectedImage.toString(),
//                            Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                    Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT)
//                            .show();
//                    Log.e("Camera", e.toString());
//                }
//            }
        }
    }

//    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference(Constants.FIREBASE_CHILD_RESTAURANTS)
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(mRestaurant.getPushId())
//                .child("imageUrl");
//        ref.setValue(imageEncoded);
//    }
//
//}
