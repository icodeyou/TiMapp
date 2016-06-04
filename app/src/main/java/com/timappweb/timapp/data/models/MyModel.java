package com.timappweb.timapp.data.models;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import static com.timappweb.timapp.data.models.annotations.ModelAssociation.Type.BELONGS_TO_MANY;

/**
 * Created by stephane on 5/10/2016.
 */
public class MyModel extends Model implements Observable{
    private static final String TAG = "MyModel";

    /**
     * Save belongs to many association.
     * @param data association to save
     * @param associationModel association model
     */
    public  <T extends MyModel> void saveBelongsToMany(Collection<T> data,
                                  Class<? extends MyModel> associationModel){
        MyModel savedModel = !this.hasLocalId() ? this.mySave() : this;
            //Log.e(TAG, "Cannot save association because this model is not saved yet: " + this);
            //return;
        try {
            for (MyModel model: data){
                Constructor<? extends MyModel> constructor = associationModel.getConstructor(savedModel.getClass(), model.getClass());
                MyModel instance = constructor.newInstance(savedModel, model);
                instance.deepSave();
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

    public void replaceAssociation(Collection<? extends MyModel> data,
                                Class<? extends MyModel> associationModel){
        this.deleteAssociation(associationModel);
        this.saveBelongsToMany(data, associationModel);
    }

    /**
     * Delete association data
     * @param associationModel
     */
    public void deleteAssociation(Class<? extends MyModel> associationModel){
        new Delete()
                .from(associationModel)
                .where(this.getClass().getSimpleName() + " = " + this.getId())
                .execute();
    }

    /**
     * Save model plus belongs to many associations
     * @return
     */
    public <T extends MyModel> T deepSave() {
        Log.v(TAG, "Saving model " + this.getClass().getCanonicalName());
        this._saveModelAssociations();
        return (T) this.mySave();
    }

    /**
     * Save model plus belongs to many associations
     * @return
     */
    public MyModel mySave() {
        this.save();

        if (!this.hasLocalId()){
            Log.e(TAG, "Cannot save to local model: " + this);
        }

        return this;
    }

    private void _saveModelAssociations(){
        Class<? extends MyModel> clazz = this.getClass();
        for (Field field: clazz.getFields()){
            try {
                if (field.isAnnotationPresent(ModelAssociation.class)){

                        ModelAssociation annotation = field.getAnnotation(ModelAssociation.class);
                        switch (annotation.type()){
                            case BELONGS_TO:
                                MyModel fieldValue = (MyModel) field.get(this);
                                if (fieldValue != null && !fieldValue.hasLocalId()){
                                    MyModel model = fieldValue.deepSave();
                                    field.set(this, model);
                                    Log.d(TAG, "Saving deep association for field '" + field.getName());
                                }
                                break;
                            case BELONGS_TO_MANY:
                                Collection<? extends MyModel> fieldValues = (Collection<? extends MyModel>) field.get(this);
                                if (fieldValues != null){
                                    if (annotation.saveStrategy() == ModelAssociation.SaveStrategy.REPLACE){
                                        this.deleteAssociation(annotation.joinModel());
                                    }
                                    this.saveBelongsToMany(fieldValues, annotation.joinModel());
                                    Log.d(TAG, "Saving deep association for field '" + field.getName() + ": size=" + fieldValues.size());
                                }
                                break;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasLocalId() {
        return this.getId() != null;
    }



    private transient PropertyChangeRegistry mCallbacks;


    // =============================================================================================
    // OBSERVABLE: see android.databinding.BaseObservable

    @Override
    public synchronized void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        if (mCallbacks == null) {
            mCallbacks = new PropertyChangeRegistry();
        }
        mCallbacks.add(callback);
    }

    @Override
    public synchronized void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        if (mCallbacks != null) {
            mCallbacks.remove(callback);
        }
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    public synchronized void notifyChange() {
        if (mCallbacks != null) {
            mCallbacks.notifyCallbacks(this, 0, null);
        }
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with {@link Bindable} to generate a field in
     * <code>BR</code> to be used as <code>fieldId</code>.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    public void notifyPropertyChanged(int fieldId) {
        if (mCallbacks != null) {
            mCallbacks.notifyCallbacks(this, fieldId, null);
        }
    }
}
