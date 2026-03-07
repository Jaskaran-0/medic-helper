package ca.mohawk.medichelper.ui.family;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.mohawk.medichelper.R;
import data_models.PendingRequest;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.PendingRequestViewHolder> {

    private final List<PendingRequest> pendingRequests;
    private final OnPendingRequestActionListener listener;

    public interface OnPendingRequestActionListener {
        void onApprove(PendingRequest request);
        void onReject(PendingRequest request);
    }

    public PendingRequestAdapter(List<PendingRequest> pendingRequests, OnPendingRequestActionListener listener) {
        this.pendingRequests = pendingRequests != null ? pendingRequests : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_request, parent, false);
        return new PendingRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingRequestViewHolder holder, int position) {
        PendingRequest request = pendingRequests.get(position);

        holder.tvEmail.setText(request.getRequestingUser().getEmail());
        holder.btnApprove.setOnClickListener(v -> listener.onApprove(request));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request));
    }

    @Override
    public int getItemCount() {
        return pendingRequests != null ? pendingRequests.size() : 0;
    }

    public void updatePendingRequests(List<PendingRequest> newRequests) {
        this.pendingRequests.clear();
        this.pendingRequests.addAll(newRequests);
        notifyDataSetChanged();
    }

    static class PendingRequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvEmail;
        Button btnApprove, btnReject;

        public PendingRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tv_email);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
