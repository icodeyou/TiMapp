package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created by stephane on 6/8/2016.
 *
 * NOT DONE.
 */
public class AutoMapper {

    /*
    public static <T extends SyncBaseModel> JsonObject toJson(T obj, String[] fields){
        List<Field> reflectionFields = ReflectionHelper.getFieldsFromNames(fields);
    }*/

    public static <T extends SyncBaseModel> JsonObject toJson(T obj){
        JsonObject jsonObject = new JsonObject();
        try {
            for (Field field: ReflectionHelper.getAllFields(obj.getClass())){
                if (field.isAnnotationPresent(Expose.class)){
                    Expose exposeAnnotation = field.getAnnotation(Expose.class);
                    if (exposeAnnotation.serialize()){
                        if (field.isAnnotationPresent(ModelAssociation.class)){
                            ModelAssociation associationAnnotation = field.getAnnotation(ModelAssociation.class);
                            if (associationAnnotation.type() == ModelAssociation.Type.BELONGS_TO){
                                SyncBaseModel associationModel = (SyncBaseModel) field.get(obj);
                                if (associationModel != null){
                                    jsonObject.addProperty(associationAnnotation.remoteForeignKey(), associationModel.getRemoteId());
                                }
                            }
                            associationAnnotation.remoteForeignKey();
                        }
                        else {
                            //jsonObject.addProperty(field.getName(), field.get(obj));
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}