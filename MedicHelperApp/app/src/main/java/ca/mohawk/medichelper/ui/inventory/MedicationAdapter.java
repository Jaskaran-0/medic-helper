package ca.mohawk.medichelper.ui.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ca.mohawk.medichelper.R;
import data_models.Medication;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private final List<Medication> medicationList;
    private final OnMedicationActionListener actionListener;

    public MedicationAdapter(List<Medication> medications,  OnMedicationActionListener listener) {
        this.medicationList = medications;
        this.actionListener = listener;
    }

    public void updateMedications(List<Medication> medications) {
        medicationList.clear();
        medicationList.addAll(medications);

        // Notify RecyclerView of the data change, ensuring onBindViewHolder is triggered
        notifyDataSetChanged();

        Log.d("MedicationAdapter", "Medications updated: " + medications.size());
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.bind(medication);
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    // Package-private ViewHolder to allow RecyclerView access but encapsulate within package
    class MedicationViewHolder extends RecyclerView.ViewHolder {

        private final TextView medicationName;
        private final EditText inventoryCount;
        private final ImageView medicationImage;
        private final Button btnUpdate;
        private final Button btnDelete;

        private int originalInventory;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            medicationName = itemView.findViewById(R.id.tv_medication_name);
            inventoryCount = itemView.findViewById(R.id.et_inventory_count);
            medicationImage = itemView.findViewById(R.id.iv_medication_image);
            btnUpdate = itemView.findViewById(R.id.btn_update);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }


        public void bind(Medication medication) {
            medicationName.setText(medication.getName());
            originalInventory = medication.getInventory();
            inventoryCount.setText(String.valueOf(originalInventory));


            btnUpdate.setEnabled(false);

            String imageBase64 = medication.getImage();
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                Bitmap bitmap = base64ToBitmap(imageBase64);  // Convert base64 to Bitmap
                medicationImage.setImageBitmap(bitmap);
            } else {
                medicationImage.setImageResource(R.drawable.ic_menu_camera);  // Default image
            }

            // Add a TextWatcher to the EditText to enable the Update button only if the value changes
            inventoryCount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int newInventory = Integer.parseInt(s.toString());
                        if(newInventory < 0 || newInventory > 1000){
                            btnUpdate.setEnabled(false);
                            return;
                        }
                        // Enable the update button only if the value changes
                        btnUpdate.setEnabled(newInventory != originalInventory);
                    } catch (NumberFormatException e) {
                        btnUpdate.setEnabled(false);  // Disable if input is invalid
                    }
                }
            });

            // Handle update button click
            btnUpdate.setOnClickListener(v -> {
                int updatedInventory = Integer.parseInt(inventoryCount.getText().toString());
                actionListener.onUpdateClicked(medication.getMedicationId(), medication.getName(), updatedInventory);
            });

            // Handle delete button click
            btnDelete.setOnClickListener(v -> {
                actionListener.onDeleteClicked(medication.getMedicationId());
            });

            Log.d("MedicationAdapter", "Bound medication: " + medication.getName());
        }

        private Bitmap base64ToBitmap(String base64String) {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
    }

    public interface OnMedicationActionListener {
        void onUpdateClicked(int medicationId, String name, int newInventory);
        void onDeleteClicked(int medicationId);
    }
}
