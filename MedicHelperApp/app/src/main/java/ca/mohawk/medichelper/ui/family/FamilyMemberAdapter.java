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
import data_models.FamilyMember;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.FamilyMemberViewHolder> {

    private final List<FamilyMember> familyMembers;
    private final OnFamilyMemberActionListener listener;

    public interface OnFamilyMemberActionListener {
        void onGoToAccount(FamilyMember familyMember);
        void onRemove(FamilyMember familyMember);
    }

    public FamilyMemberAdapter(List<FamilyMember> familyMembers, OnFamilyMemberActionListener listener) {
        this.familyMembers = familyMembers != null ? familyMembers : new ArrayList<>();
        this.listener = listener;
    }

    public void setFamilyMembers(List<FamilyMember> familyMembers) {
        this.familyMembers.clear();
        this.familyMembers.addAll(familyMembers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FamilyMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family_member, parent, false);
        return new FamilyMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyMemberViewHolder holder, int position) {
        FamilyMember familyMember = familyMembers.get(position);

        holder.tvName.setText(familyMember.getFullName());
        holder.tvEmail.setText(familyMember.getEmail());
        holder.tvAddedOn.setText(familyMember.getAddedOn());

        holder.btnGoToAccount.setOnClickListener(v -> listener.onGoToAccount(familyMember));
        holder.btnRemove.setOnClickListener(v -> listener.onRemove(familyMember));
    }

    @Override
    public int getItemCount() {
        return familyMembers != null ? familyMembers.size() : 0;
    }

    public static class FamilyMemberViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvAddedOn;
        private final Button btnGoToAccount;
        private final Button btnRemove;

        public FamilyMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvAddedOn = itemView.findViewById(R.id.tv_added_on);
            btnGoToAccount = itemView.findViewById(R.id.btn_go_to_account);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
