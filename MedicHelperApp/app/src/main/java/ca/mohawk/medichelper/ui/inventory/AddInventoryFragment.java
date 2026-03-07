package ca.mohawk.medichelper.ui.inventory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.IOException;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;

/**
 * Fragment for adding medication inventory.
 */
public class AddInventoryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private InventoryViewModel inventoryViewModel;
    private AddInventoryViewModel addInventoryViewModel;
    private EditText etMedicationName, etInventoryCount;
    private ImageView ivMedicationImage;
    private Button btnAddMedication, btnSelectImage;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_inventory, container, false);

        Log.d("AddInventoryFragment", "Fragment created");

        // Initialize UI components
        etMedicationName = view.findViewById(R.id.et_medication_name);
        etInventoryCount = view.findViewById(R.id.et_medication_inventory);
        ivMedicationImage = view.findViewById(R.id.iv_medication_image);
        btnAddMedication = view.findViewById(R.id.btn_add_medication);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize ViewModel
        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        addInventoryViewModel = new ViewModelProvider(this).get(AddInventoryViewModel.class);

        // Handle Select Image button click
        btnSelectImage.setOnClickListener(v -> selectImage());

        // Handle Add Medication button click
        btnAddMedication.setOnClickListener(v -> {
            String medicationName = etMedicationName.getText().toString();
            String inventoryCount = etInventoryCount.getText().toString();

            // Fetch token from shared preferences
            getContext();
            String token = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                    .getString("jwt_token", null);

            if (token != null) {
                addInventoryViewModel.addMedication(token, medicationName, inventoryCount);
            } else {
                Toast.makeText(requireContext(), "Token not found. Please log in.", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).logout(null);
                }
            }
        });

        // Set up observers
        setupObservers();

        return view;
    }

    // Method to open image gallery for selecting a photo
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");  // Only show images
        intent.setAction(Intent.ACTION_GET_CONTENT);  // Open file picker
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // Handle the result of image selection from the gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();  // Get the selected image URI

            try {
                // Convert the image URI into a Bitmap and set it in the ViewModel
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                addInventoryViewModel.setImage(bitmap, imageUri);
                ivMedicationImage.setImageBitmap(bitmap);  // Display the selected image in the ImageView
                Log.d("AddInventoryFragment", "Image selected successfully");
            } catch (IOException e) {
                Log.e("AddInventoryFragment", "Error loading image", e);
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("AddInventoryFragment", "Image selection failed or canceled");
        }
    }

    // Set up LiveData observers to update the UI based on ViewModel data
    private void setupObservers() {
        addInventoryViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    progressBar.setVisibility(View.VISIBLE);
                    btnAddMedication.setEnabled(false);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnAddMedication.setEnabled(true);
                }
            }
        });

        addInventoryViewModel.getMedicationAdded().observe(getViewLifecycleOwner(), isAdded -> {
            if(isAdded == null) return;
            if (Boolean.TRUE.equals(isAdded)) {
                Log.d("AddInventoryFragment", "Medication added successfully");

                // Notify InventoryViewModel about the new medication
                addInventoryViewModel.getAddedMedication().observe(getViewLifecycleOwner(), newMedication -> {
                    inventoryViewModel.addMedicationToList(newMedication);
                });

                // Navigate back to InventoryFragment
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.popBackStack();
            } else {
                Toast.makeText(requireContext(), "Failed to add medication", Toast.LENGTH_SHORT).show();
            }
        });

        addInventoryViewModel.getSelectedImage().observe(getViewLifecycleOwner(), bitmap -> {
            if (bitmap != null) {
                ivMedicationImage.setImageBitmap(bitmap);  // Update the ImageView with the selected image
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("AddInventoryFragment", "Fragment destroyed");
    }
}
