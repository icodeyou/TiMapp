package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by stephane on 5/10/2016.
 */
public class MyModel extends Model{
    private static final String TAG = "MyModel";

    /**
     * Save belongs to many association.
     * @param data association to save
     * @param associationModel association model
     */
    public void saveAssociation(List<? extends Model> data,
                                Class<? extends Model> associationModel){
        try {
            for (Model model: data){
                Constructor<? extends Model> constructor = associationModel.getConstructor(this.getClass(), model.getClass());
                Model instance = constructor.newInstance(this, model);
                instance.save();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void replaceAssociation(List<? extends Model> data,
                                Class<? extends Model> associationModel){
        this.deleteAssociation(associationModel);
        this.saveAssociation(data, associationModel);
    }

    /**
     * Delete association data
     * @param associationModel
     */
    public void deleteAssociation(Class<? extends Model> associationModel){
        new Delete()
                .from(associationModel)
                .where(this.getClass().getSimpleName() + " = " + this.getId())
                .execute();
    }

    /**
     * Save model plus belongs to many associations
     * @return
     */
    public Long deepSave() {
        Long id = this.save();
        if (id != null){
            this._saveAssociations();
        }
        return id;
    }

    private void _saveAssociations(){
        Class<? extends MyModel> clazz = this.getClass();
        for (Field field: clazz.getFields()){
            try {
                if (field.isAnnotationPresent(ModelAssociation.class)){
                    List<? extends Model> fieldValue = (List<? extends Model>) field.get(this);
                    if (fieldValue != null){
                        ModelAssociation annotation = field.getAnnotation(ModelAssociation.class);
                        if (annotation.saveStrategy() == ModelAssociation.SaveStrategy.REPLACE){
                            this.deleteAssociation(annotation.joinModel());
                        }
                        this.saveAssociation(fieldValue, annotation.joinModel());
                        Log.d(TAG, "Saving deep association for field '" + field.getName() + ": size=" + fieldValue.size());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
