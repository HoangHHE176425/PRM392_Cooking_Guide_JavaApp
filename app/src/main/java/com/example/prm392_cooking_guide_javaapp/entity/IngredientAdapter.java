package com.example.prm392_cooking_guide_javaapp.entity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_cooking_guide_javaapp.R;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
    private List<DishIngredient> ingredients;

    public IngredientAdapter(List<DishIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishIngredient ingredient = ingredients.get(position);
        holder.tvIngredientName.setText(ingredient.getIngredientName());
        holder.tvQuantity.setText(ingredient.getQuantity());
        holder.tvCalories.setText(ingredient.getCaloriesPerUnit() + " cal");
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public void updateData(List<DishIngredient> newIngredients) {
        this.ingredients = newIngredients;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIngredientName;
        TextView tvQuantity;
        TextView tvCalories;

        ViewHolder(View itemView) {
            super(itemView);
            tvIngredientName = itemView.findViewById(R.id.tv_ingredient_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvCalories = itemView.findViewById(R.id.tv_calories);
        }
    }
}
