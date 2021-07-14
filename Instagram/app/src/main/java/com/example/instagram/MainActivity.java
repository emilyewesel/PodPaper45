package com.example.instagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

/*
 * Main Activity class that loads {@link MainFragment}.
 */

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private EditText mCaption; //mCaption
    private Button takePicture;
    private ImageView image;
    private Button submit;
    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCaption = findViewById(R.id.caption);
        takePicture = findViewById(R.id.takePic);
        image = findViewById(R.id.image);
        submit = findViewById(R.id.submit);



        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        ParseObject firstObject = new  ParseObject("FirstClass");
        firstObject.put("message","Hey ! First message from android. Parse is now connected");
        firstObject.saveInBackground(e -> {
            if (e != null){
                Log.e("MainActivity", e.getLocalizedMessage());
            }else{
                Log.d("MainActivity","Object saved.");
            }
        });
        queryPosts();

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String description = mCaption.getText().toString();
                if(description.isEmpty()){
                    Toast.makeText(MainActivity.this, "enter a caption ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoFile == null || image.getDrawable() == null){
                    Toast.makeText(MainActivity.this, "there is no image data!", Toast.LENGTH_SHORT).show();
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, currentUser, photoFile);

            }
        });
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(MainActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                //Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, SOME_WIDTH);
                image.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getPhotoFileUri(String photoFileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + photoFileName);

    }

    private void savePost(String description, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        //Toast.makeText(MainActivity.this, "Trying to save post!!!", Toast.LENGTH_SHORT).show();
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null){
                    Log.e("ERORR!!! " + e," Toast.LENGTH_SHORT");
                    Toast.makeText(MainActivity.this, "ERORR!!! " + e, Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "save was successful");
                mCaption.setText("");
                image.setImageResource(0);
            }
        });
    }

    public void queryPosts(){
        Log.e(TAG, "calling queryPosts");
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        //query.include(Post.KEY_USER);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {

                if (e != null){
                    Log.e("wrong!!", "wrong!!", e);
                    return;
                }
                Log.e(TAG, "there are " + posts.size() + "posts");
                for (Post post: posts){
                    ParseUser temp = post.getUser();
                    try {
                        temp.fetchIfNeeded();
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                    Log.e(TAG, "the post issss"+  post.getDescription() + "username " + temp.getUsername());
                }
            }
        });
    }
    public void handleLogOut(View view){
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
    }

    public void handleViewFeed(View view){
        Intent intent = new Intent(MainActivity.this, FeedActivity.class);
        startActivity(intent);

    }

}

