package ca.mohawk.medichelper.ui.notes;

import android.util.Log;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Collections;
import java.util.List;
import data_models.Note;
import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotesViewModel extends ViewModel {

    private final MutableLiveData<List<Note>> notes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteStatus = new MutableLiveData<>();
    private final ApiService apiService;

    public NotesViewModel() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Note>> getNotes() {
        return notes;
    }

    public LiveData<Boolean> getDeleteStatus() {
        return deleteStatus;
    }

    public void fetchNotes(String token) {
        notes.setValue(null); // Set to null to trigger loading state in UI
        apiService.getNotes("Bearer " + token).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notes.setValue(response.body());
                } else {
                    Log.e("NotesViewModel", "Failed to fetch notes");
                    notes.setValue(Collections.emptyList()); // Indicate empty result
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                Log.e("NotesViewModel", "Error fetching notes", t);
                notes.setValue(Collections.emptyList()); // Handle failure
            }
        });
    }


    public void deleteNote(String token, int noteId) {
        apiService.deleteNote("Bearer " + token, noteId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    deleteStatus.setValue(true);
                } else {
                    Log.e("NotesViewModel", "Failed to delete note");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NotesViewModel", "Error deleting note", t);
            }
        });
    }

}
