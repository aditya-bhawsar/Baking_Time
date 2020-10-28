package com.aditya.bakingtime.util;

import android.net.Uri;

import com.aditya.bakingtime.model.Recipe;
import com.aditya.bakingtime.model.RecipeIngredient;
import com.aditya.bakingtime.model.RecipeStep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    public static List<Recipe> parseJsonMain(String s){
        List<Recipe> recipeList = new ArrayList<>();

        recipeList.clear();

        try {
            JSONArray mainJsonArr = new JSONArray(s);

            for(int i =0; i<mainJsonArr.length(); i++){

                JSONObject recipeJson = new JSONObject(mainJsonArr.get(i).toString());

                int id = recipeJson.getInt("id");
                String name = recipeJson.getString("name");
                String ingredients = recipeJson.getString("ingredients");
                String steps = recipeJson.getString("steps");
                int servings = recipeJson.getInt("servings");
                String image = recipeJson.getString("image");

                Recipe recipe = new Recipe(id,name,ingredients,steps,servings,image);

                recipeList.add(recipe);
            }
        }
        catch (JSONException e){e.printStackTrace();}
        return recipeList;
    }

    public static List<RecipeStep> parseJsonSteps(String s){
        List<RecipeStep> listRecipeStep = new ArrayList<>();

        listRecipeStep.clear();

        try {
            JSONArray mainJsonArr = new JSONArray(s);

            for(int i =0; i<mainJsonArr.length(); i++){

                JSONObject stepJson = new JSONObject(mainJsonArr.get(i).toString());

                int id = stepJson.getInt("id");
                String shortDescription = stepJson.getString("shortDescription");
                String description = stepJson.getString("description");
                String videoURL = stepJson.getString("videoURL");
                String thumbnailURL = stepJson.getString("thumbnailURL");

                RecipeStep recipeStep = new RecipeStep(id,shortDescription,description,videoURL,thumbnailURL);
                listRecipeStep.add(recipeStep);
            }

        } catch (JSONException e) { e.printStackTrace(); }
        return listRecipeStep;
    }

    public static List<RecipeIngredient> parseJsonIngredients(String s){
        List<RecipeIngredient> listRecipeIngredient = new ArrayList<>();

        listRecipeIngredient.clear();

        try {
            JSONArray mainJsonArr = new JSONArray(s);

            for(int i =0; i<mainJsonArr.length(); i++){
                JSONObject ingJson = new JSONObject(mainJsonArr.get(i).toString());

                int quantity = ingJson.getInt("quantity");
                String measure = ingJson.getString("measure");
                String ingredient = ingJson.getString("ingredient");

                RecipeIngredient recipeIngredient = new RecipeIngredient(quantity,measure,ingredient);
                listRecipeIngredient.add(recipeIngredient);

            }
        } catch (JSONException e) { e.printStackTrace(); }
        return listRecipeIngredient;
    }

    public static Uri videoUri(String s){
        return Uri.parse(s).buildUpon().build();
    }
}
