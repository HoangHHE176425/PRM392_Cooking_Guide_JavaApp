package com.example.myapplication.entity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.entity.CookingStep;
import java.util.List;

public class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.StepViewHolder> {
    private List<CookingStep> steps;

    public CookingStepAdapter(List<CookingStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cooking_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        CookingStep step = steps.get(position);
        holder.bind(step);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    class StepViewHolder extends RecyclerView.ViewHolder {
        private TextView stepNumberTextView, stepTitleTextView, stepDescriptionTextView;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumberTextView = itemView.findViewById(R.id.stepNumberTextView);
            stepTitleTextView = itemView.findViewById(R.id.stepTitleTextView);
            stepDescriptionTextView = itemView.findViewById(R.id.stepDescriptionTextView);
        }

        public void bind(CookingStep step) {
            stepNumberTextView.setText(String.valueOf(step.getStepNumber()));
            stepTitleTextView.setText(step.getTitle());
            stepDescriptionTextView.setText(step.getDescription());
        }
    }
}
