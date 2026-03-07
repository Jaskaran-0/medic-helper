package ca.mohawk.medichelper.ui.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import ca.mohawk.medichelper.R;

public class AddNoteFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText etTitle, etNoteContent;
    private Button btnAddNote, btnSelectImage;
    private ImageView ivSelectedImage;
    private ProgressBar progressBar;
    private AddNoteViewModel addNoteViewModel;
    private String encodedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_note, container, false);

        etTitle = view.findViewById(R.id.et_title);
        etNoteContent = view.findViewById(R.id.et_note_content);
        btnAddNote = view.findViewById(R.id.btn_add_note);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        progressBar = view.findViewById(R.id.progress_bar);

        addNoteViewModel = new ViewModelProvider(requireActivity()).get(AddNoteViewModel.class);

        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnAddNote.setOnClickListener(v -> addNote());

        setupObservers();
        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");  // Only show images
        intent.setAction(Intent.ACTION_GET_CONTENT);  // Open file picker
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ivSelectedImage.setImageBitmap(bitmap);
                ivSelectedImage.setVisibility(View.VISIBLE);
                encodeImage(bitmap);
            } catch (IOException e) {
                Log.e("AddNoteFragment", "Error loading image", e);
            }
        }
    }

    private void encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    private void addNote() {
        String title = etTitle.getText().toString();
        String content = etNoteContent.getText().toString();
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAddNote.setEnabled(false);
        addNoteViewModel.addNote(token, title, content, encodedImage);
    }

    private void setupObservers() {
        addNoteViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnAddNote.setEnabled(!isLoading); // Enable/disable button based on loading state
        });

        addNoteViewModel.isNoteAdded().observe(getViewLifecycleOwner(), isAdded -> {
            if (isAdded != null && isAdded) {
                Toast.makeText(getContext(), "Note added successfully", Toast.LENGTH_SHORT).show();

                // Reset input fields for a new note
                etTitle.setText("");
                etNoteContent.setText("");
                ivSelectedImage.setVisibility(View.GONE);

                // Keep the user on the same page
                progressBar.setVisibility(View.GONE);
                btnAddNote.setEnabled(true);

                // Navigate back to NotesFragment and refresh notes
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.popBackStack();

                // Reset the note added status
                addNoteViewModel.resetNoteAdded();
            } else if (isAdded != null) {
                Toast.makeText(getContext(), "Failed to add note", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnAddNote.setEnabled(true);
            }
        });
    }
}
