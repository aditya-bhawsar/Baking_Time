package com.aditya.bakingtime.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aditya.bakingtime.R;
import com.aditya.bakingtime.model.Recipe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    List<Recipe> recipeList;
    ClickListener listener;

    public interface ClickListener{ void onClick(int pos);}

    public RecipeAdapter(List recipeList, ClickListener listener) {
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context ctx = viewGroup.getContext();
        View v= LayoutInflater.from(ctx).inflate(R.layout.recipe_item,viewGroup,false);
        return new RecipeAdapter.ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull RecipeAdapter.ViewHolder viewHolder, int i) { viewHolder.bind(i); }

    @Override
    public int getItemCount() {
        if(recipeList==null) {return 0;}
        return recipeList.size();
    }

    public void refresh(List<Recipe> recipeList){
        this.recipeList = recipeList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTv;
        ImageView recipeIv;
        CardView recipeCv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv  = itemView.findViewById(R.id.name_tv);
            recipeIv = itemView.findViewById(R.id.iv_recipe);
            recipeCv = itemView.findViewById(R.id.card_lay);
        }
        void bind(int pos){
            Recipe recipe = recipeList.get(pos);
            nameTv.setText(recipe.getName());
            if(recipe.getImage()!=null && !recipe.getImage().isEmpty()){Picasso.get().load(recipe.getImage()).error(R.drawable.ic_default).into(recipeIv);}
            recipeCv.setOnClickListener(this);
        }
        @Override public void onClick(View v) { listener.onClick(getAdapterPosition());}
    }
}