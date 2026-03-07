package ca.mohawk.medichelper.ui.notes;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.Note;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNoteViewModel extends AndroidViewModel {

    private static final String TAG = "AddNoteViewModel";
    private final ApiService apiService;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isNoteAdded = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> selectedImage = new MutableLiveData<>();
    private String encodedImage;

    public AddNoteViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<Boolean> isNoteAdded() {
        return isNoteAdded;
    }

    public LiveData<Bitmap> getSelectedImage() {
        return selectedImage;
    }

    // Method to set and encode image
    public void setImage(Bitmap bitmap) {
        selectedImage.setValue(bitmap);
        encodedImage = encodeImage(bitmap);
    }

    public void resetNoteAdded() {
        isNoteAdded.setValue(null);
    }

    // Method to convert Bitmap to Base64 String
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }



    // Method to add note
    public void addNote(String token, String title, String content, String encodedImage) {
        isLoading.setValue(true);

        Note note = new Note(title, content, encodedImage);

        apiService.addNote("Bearer " + token, note).enqueue(new Callback<Note>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    isNoteAdded.setValue(true);
                    Log.d(TAG, "Note added successfully: " + response.body());
                    Toast.makeText(getApplication(), "Note added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    isNoteAdded.setValue(false);
                    Log.e(TAG, "Failed to add note. Response code: " + response.code());
                    Toast.makeText(getApplication(), "Failed to add note", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                isLoading.setValue(false);
                isNoteAdded.setValue(false);
                Log.e(TAG, "Error adding note: ", t);
                Toast.makeText(getApplication(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
