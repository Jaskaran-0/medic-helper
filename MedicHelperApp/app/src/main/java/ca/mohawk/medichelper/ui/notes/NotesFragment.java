package ca.mohawk.medichelper.ui.notes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ca.mohawk.medichelper.R;
import java.util.List;

import ca.mohawk.medichelper.activities.MainActivity;
import data_models.Note;

public class NotesFragment extends Fragment {

    private NotesViewModel notesViewModel;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recycler_notes);
        progressBar = view.findViewById(R.id.progress_bar);

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.fab).setOnClickListener(v -> {
            Log.d("NotesFragment", "FAB clicked to add note");
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_add_notes);
        });

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);
        if (token == null) {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.logout(null);
            return view;
        }

        noteAdapter = new NoteAdapter(new NoteAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(Note note) {
                progressBar.setVisibility(View.VISIBLE);
                notesViewModel.deleteNote(token, note.getNoteId());
            }
        });
        recyclerView.setAdapter(noteAdapter);

        notesViewModel.getNotes().observe(getViewLifecycleOwner(), notes -> {
            if (notes == null) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }
            if (notes.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No notes found", Toast.LENGTH_SHORT).show();
                return;
            }
            noteAdapter.setNotes(notes);
            progressBar.setVisibility(View.GONE);
        });

        notesViewModel.getDeleteStatus().observe(getViewLifecycleOwner(), isDeleted -> {
            progressBar.setVisibility(View.GONE);
            if (isDeleted) {
                notesViewModel.fetchNotes(token);
            }
        });

        notesViewModel.fetchNotes(token);

        return view;
    }
}
