package com.example.tanihebat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class LoadingScreen2Activity extends AppCompatActivity {

    private ImageView ivImage;
    private static final String TAG = "LoadingScreen2Activity";
    private static final String BASE_URL = "https://api.openai.com/";
    private static final String API_KEY = "YOUR_API_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen2);

        ivImage = findViewById(R.id.ivImage);

        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("imageUri");
        Uri imageUri = Uri.parse(imageUriString);
        ivImage.setImageURI(imageUri);

        // Upload image to Firebase Storage
        uploadImageToFirebase(imageUri);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images/" + imageUri.getLastPathSegment());

        UploadTask uploadTask = imagesRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        String imageUrl = downloadUri.toString();
                        Log.d(TAG, "Image URL: " + imageUrl);

                        // Call GPT-4 API with the image URL
                        callGPT4Api(imageUrl);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload image to Firebase", exception);
            }
        });
    }

    private void callGPT4Api(String imageUrl) {
        GPT4Api apiService = getClient(BASE_URL).create(GPT4Api.class);

        // First API call to identify the problem
        GPT4ChatRequest.Message systemMessage = new GPT4ChatRequest.Message("system", "You are a helpful assistant.");
        GPT4ChatRequest.Message userMessage = new GPT4ChatRequest.Message("user", "AI, tolong identifikasi masalah yang ada pada tanaman padi berdasarkan gambar yang diberikan. Gambar tersebut dapat menunjukkan discoloration (perubahan warna), spots on leaves (bintik-bintik pada daun), stunted growth (pertumbuhan terhambat), atau adanya hama yang terlihat pada tanaman. Mohon berikan analisis berdasarkan gambar yang disediakan\n![image](" + imageUrl + ")");
        GPT4ChatRequest request = new GPT4ChatRequest("gpt-4o", Arrays.asList(systemMessage, userMessage), 300);

        Call<GPT4ChatResponse> call = apiService.generateChatResponse(request);
        call.enqueue(new Callback<GPT4ChatResponse>() {
            @Override
            public void onResponse(Call<GPT4ChatResponse> call, Response<GPT4ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String problem = response.body().choices.get(0).message.content.trim();
                    Log.d(TAG, "Problem: " + problem);

                    String problem2 = problem.replaceAll("\\*", "");

                    // Second API call to provide solution
                    GPT4ChatRequest solutionRequest = new GPT4ChatRequest("gpt-4o", Arrays.asList(systemMessage, new GPT4ChatRequest.Message("user", "AI, tolong identifikasi solusi atas masalah yang ada pada tanaman padi berdasarkan gambar yang diberikan. Gambar tersebut dapat menunjukkan discoloration (perubahan warna), spots on leaves (bintik-bintik pada daun), stunted growth (pertumbuhan terhambat), atau adanya hama yang terlihat pada tanaman. Mohon berikan analisis berdasarkan gambar yang disediakan\n![image](" + imageUrl + ")")), 300);
                    Call<GPT4ChatResponse> solutionCall = apiService.generateChatResponse(solutionRequest);
                    solutionCall.enqueue(new Callback<GPT4ChatResponse>() {
                        @Override
                        public void onResponse(Call<GPT4ChatResponse> call, Response<GPT4ChatResponse> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                                String solution = response.body().choices.get(0).message.content.trim();
                                Log.d(TAG, "Solution: " + solution);

                                String solution2 = solution.replaceAll("\\*", "");

                                // Third API call to determine category with maximum 3 words response
                                GPT4ChatRequest categoryRequest = new GPT4ChatRequest("gpt-4o", Arrays.asList(systemMessage, new GPT4ChatRequest.Message("user", "AI, tolong tentukan kategori tanaman padi dari gambar ini. Jawaban harus maksimal 3 kata dalam kapital\n![image](" + imageUrl + ")")), 100);
                                Call<GPT4ChatResponse> categoryCall = apiService.generateChatResponse(categoryRequest);
                                categoryCall.enqueue(new Callback<GPT4ChatResponse>() {
                                    @Override
                                    public void onResponse(Call<GPT4ChatResponse> call, Response<GPT4ChatResponse> response) {
                                        if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                                            String category = response.body().choices.get(0).message.content.trim();
                                            Log.d(TAG, "Category: " + category);

                                            // Start ResultActivity to show the results
                                            Intent resultIntent = new Intent(LoadingScreen2Activity.this, ResultActivity.class);
                                            resultIntent.putExtra("imageUri", imageUrl);
                                            resultIntent.putExtra("problem", problem2);
                                            resultIntent.putExtra("solution", solution2);
                                            resultIntent.putExtra("category", category);
                                            startActivity(resultIntent);
                                        } else {
                                            handleUnsuccessfulResponse(response, "Category");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<GPT4ChatResponse> call, Throwable t) {
                                        handleFailure(t, "Category");
                                    }
                                });
                            } else {
                                handleUnsuccessfulResponse(response, "Solution");
                            }
                        }

                        @Override
                        public void onFailure(Call<GPT4ChatResponse> call, Throwable t) {
                            handleFailure(t, "Solution");
                        }
                    });
                } else {
                    handleUnsuccessfulResponse(response, "Problem");
                }
            }

            @Override
            public void onFailure(Call<GPT4ChatResponse> call, Throwable t) {
                handleFailure(t, "Problem");
            }
        });
    }

    private void handleUnsuccessfulResponse(Response<?> response, String stage) {
        Log.d(TAG, "API response not successful or body is null at " + stage);
        Log.d(TAG, "Response code: " + response.code());
        if (response.errorBody() != null) {
            try {
                Log.d(TAG, "Error body: " + response.errorBody().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFailure(Throwable t, String stage) {
        Log.d(TAG, "API call failed at " + stage);
        t.printStackTrace();
    }

    public interface GPT4Api {
        @Headers({
                "Content-Type: application/json",
                "Authorization: Bearer " + API_KEY
        })
        @POST("v1/chat/completions")
        Call<GPT4ChatResponse> generateChatResponse(@Body GPT4ChatRequest request);
    }

    public static class GPT4ChatRequest {
        public String model;
        public List<Message> messages;
        public int max_tokens;

        public GPT4ChatRequest(String model, List<Message> messages, int max_tokens) {
            this.model = model;
            this.messages = messages;
            this.max_tokens = max_tokens;
        }

        public static class Message {
            public String role;
            public String content;

            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }
        }
    }

    public static class GPT4ChatResponse {
        public List<Choice> choices;

        public static class Choice {
            public Message message;

            public static class Message {
                public String role;
                public String content;
            }
        }
    }

    public static Retrofit getClient(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}