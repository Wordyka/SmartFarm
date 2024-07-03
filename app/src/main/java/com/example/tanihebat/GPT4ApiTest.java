package com.example.tanihebat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Arrays;
import java.util.Collections;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class GPT4ApiTest {

    // Retrofit client
    public static class RetrofitClient {
        public static Retrofit getClient(String baseUrl) {
            return new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }

    // API request model
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

    // API response model
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

    // API interface
    public interface GPT4Api {
        @Headers({
                "Content-Type: application/json",
                "Authorization: Bearer YOUR_API_KEY"
        })
        @POST("v1/chat/completions")
        Call<GPT4ChatResponse> generateChatResponse(@Body GPT4ChatRequest request);
    }

    // Method to call GPT-4 API
    private void callGPT4Api(String imageUri) {
        GPT4Api apiService = RetrofitClient.getClient("https://api.openai.com/").create(GPT4Api.class);

        // Messages for the chat request
        GPT4ChatRequest.Message systemMessage = new GPT4ChatRequest.Message("system", "You are a helpful assistant.");
        GPT4ChatRequest.Message userMessage = new GPT4ChatRequest.Message("user", "AI, tolong identifikasi masalah yang ada pada tanaman padi berdasarkan gambar yang diberikan. Gambar tersebut dapat menunjukkan discoloration (perubahan warna), spots on leaves (bintik-bintik pada daun), stunted growth (pertumbuhan terhambat), atau adanya hama yang terlihat pada tanaman. Mohon berikan analisis berdasarkan gambar yang disediakan.\n![image](" + imageUri + ")");

        // Create the chat request
        GPT4ChatRequest request = new GPT4ChatRequest("gpt-4o", Arrays.asList(systemMessage, userMessage), 300);

        // Make the API call
        Call<GPT4ChatResponse> call = apiService.generateChatResponse(request);
        call.enqueue(new Callback<GPT4ChatResponse>() {
            @Override
            public void onResponse(Call<GPT4ChatResponse> call, Response<GPT4ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GPT4ChatResponse gpt4Response = response.body();
                    if (gpt4Response.choices != null && !gpt4Response.choices.isEmpty()) {
                        GPT4ChatResponse.Choice choice = gpt4Response.choices.get(0);
                        if (choice.message != null && choice.message.content != null && !choice.message.content.isEmpty()) {
                            String problem = choice.message.content.trim();
                            problem = problem.replaceAll("\\*", "");
                            System.out.println("Problem: " + problem);
                        } else {
                            System.out.println("No content in the response message");
                        }
                    } else {
                        System.out.println("No choices in the response");
                    }
                } else {
                    System.out.println("API response not successful or body is null");
                }
            }

            @Override
            public void onFailure(Call<GPT4ChatResponse> call, Throwable t) {
                System.err.println("API call failed");
                t.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        GPT4ApiTest test = new GPT4ApiTest();
        String imageUri = "https://upload.wikimedia.org/wikipedia/commons/3/37/LPCC-743-Cranc_americ%C3%A0_en_arrossar.jpg";
        test.callGPT4Api(imageUri);
    }
}