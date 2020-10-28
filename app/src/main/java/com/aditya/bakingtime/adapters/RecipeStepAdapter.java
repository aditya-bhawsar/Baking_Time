package com.aditya.bakingtime.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aditya.bakingtime.R;
import com.aditya.bakingtime.model.RecipeStep;

import java.util.List;

public class RecipeStepAdapter extends RecyclerView.Adapter<RecipeStepAdapter.ViewHolder> {

    List<RecipeStep> recipeStepList;
    ClickListener listener;
    private boolean mTwoPane;

    public interface ClickListener{
        void onClick(int pos);
    }

    public RecipeStepAdapter(List recipeList, ClickListener listener, boolean mTwoPane) {
        this.recipeStepList = recipeList;
        this.listener = listener;
        this.mTwoPane = mTwoPane;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context ctx = viewGroup.getContext();
        View v= LayoutInflater.from(ctx).inflate(R.layout.recipe_task_item,viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(i);
    }

    @Override
    public int getItemCount() { return recipeStepList.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView stepDescTv, stepNoTv;
        CardView stepCv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stepDescTv = itemView.findViewById(R.id.step_desc);
            stepNoTv = itemView.findViewById(R.id.step_no_tv);
            stepCv = itemView.findViewById(R.id.recipe_task_lay);
        }
        void bind(int pos) {
            RecipeStep recipeStep = recipeStepList.get(pos);
            stepCv.setOnClickListener(this);
            stepDescTv.setText(recipeStep.getShortDescription());
            stepNoTv.setText(String.valueOf(recipeStep.getId()));
        }

        @Override public void onClick(View v) {
            if(mTwoPane){}
            listener.onClick(getAdapterPosition());
        }
    }
}
