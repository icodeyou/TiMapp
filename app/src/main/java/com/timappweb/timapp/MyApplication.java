package com.timappweb.timapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.data.LocalPersistenceManager;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application{

    private static final String TAG = "MyApplication";
    private static AlertDialog alertDialog = null;

    public static User getCurrentUser(){
        return RestClient.instance().getCurrentUser();
    }

    public static List<Category> mapNameToCategory = new LinkedList<>();

    @Override
    public void onCreate(){
        super.onCreate();
        LocalPersistenceManager.init(this);

        String endpoint = getResources().getString(R.string.ws_endpoint);
        RestClient.init(this, endpoint);

        initCategories();
    }

    public void initCategories(){
//        mapNameToCategory.put("music", new Category(1, "music", R.drawable.ic_category_music));
//        mapNameToCategory.put("bar", new Category(2, "bar", R.drawable.ic_category_bar));
//        mapNameToCategory.put("party", new Category(3, "party", R.drawable.ic_category_party));
//        mapNameToCategory.put("sport", new Category(4, "sport", R.drawable.ic_category_sport));
//        mapNameToCategory.put("show", new Category(5, "show", R.drawable.ic_category_show));
        mapNameToCategory.add(new Category(1, "music", R.drawable.ic_category_music));
        mapNameToCategory.add(new Category(2, "bar", R.drawable.ic_category_bar));
        mapNameToCategory.add(new Category(3, "party", R.drawable.ic_party));
        mapNameToCategory.add(new Category(4, "sport", R.drawable.ic_category_sport));
        mapNameToCategory.add(new Category(5,"strike", R.drawable.ic_category_strike));
        mapNameToCategory.add(new Category(6, "show", R.drawable.ic_show));
    }

    /**
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(){
        boolean isLoggedIn = LocalPersistenceManager.instance.pref.getBoolean(RestClient.IS_LOGIN, false);
        return isLoggedIn;
        // TODO
        //if (isLoggedIn){
        //    int lastLoggedIn = LocalPersistenceManager.instance.pref.getInt(RestClient.LAST_LOGGED_IN, 0);
        //    return lastLoggedIn < RestClient.LOGIN
        //}
        //return false;
    }


    /**
     * If user is logged in do nothing
     * If not redirect to login page
     * @param currentContext
     */
    public static boolean requireLoggedIn(Context currentContext) {
        if (!isLoggedIn()){
            redirectLogin(currentContext);
            return false;
        }
        return true;
    }

    public static void redirectLogin(Context currentContext){
        Intent intent = new Intent(currentContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentContext.startActivity(intent);
    }

    public static void logout() {
        if (isLoggedIn()){
            RestClient.instance().logoutUser();
        }
    }

    public static void startRequireLoggedInActivity(Context currentContext, Class<?> newActivity){

        if (MyApplication.requireLoggedIn(currentContext)){
            Intent intent = new Intent(currentContext, newActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            currentContext.startActivity(intent);
        }

    }

    static AlertDialog dialog = null;
    public static void showAlert(Context context, String message) {
        if (dialog == null){
            Log.d(TAG, "Creating new dialog");
            dialog = (new AlertDialog.Builder(context)).create();
        }
        if (!dialog.isShowing()){
            dialog.setMessage(message);
            dialog.show();
        }
    }
    public static void showAlert(Context context, int id){
        showAlert(context, context.getResources().getString(id));
    }
}
