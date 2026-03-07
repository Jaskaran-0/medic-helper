package ca.mohawk.medichelper.ui.notes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ca.mohawk.medichelper.R;
import data_models.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Note note);
    }

    public NoteAdapter(OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvNoteContent.setText(note.getNoteContent());

        // Convert the createdAt date to EST and format
        String createdAtFormatted = formatToEST(note.getCreatedAt());
        holder.tvCreatedAt.setText(createdAtFormatted);

        if (note.getImage() != null) {
            byte[] decodedString = Base64.decode(note.getImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.ivImage.setImageBitmap(decodedByte);
            holder.ivImage.setVisibility(View.VISIBLE);
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(note));
    }

    // Method to convert the date string to EST and format it
    private String formatToEST(String dateString) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString);
        ZonedDateTime estDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("America/New_York"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm dd/MM/yyyy");
        return estDateTime.format(formatter);
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvNoteContent, tvCreatedAt;
        ImageView ivImage;
        Button btnDelete;

        NoteViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvNoteContent = itemView.findViewById(R.id.tv_note_content);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            ivImage = itemView.findViewById(R.id.iv_image);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
