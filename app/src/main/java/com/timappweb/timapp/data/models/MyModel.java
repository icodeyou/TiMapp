package com.timappweb.timapp.data.models;

import android.database.sqlite.SQLiteException;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.utils.Util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Created by stephane on 5/10/2016.
 */
public abstract class MyModel extends BaseModel implements Observable, Serializable{

    private static final String TAG = "MyModel";
    private transient PropertyChangeRegistry mCallbacks;

    /**
     * Save belongs to many association.
     *
     * @warning models must have corresponding constructors defined. For example:
     *      If class A (this) has and belongs to many class B
     *      AssociationModel C must have the following constructor
     *      ==> C(A a, B b);
     *
     *
     * @param data association to save
     * @param associationModel association model
     */
    public  <T extends MyModel> void saveBelongsToMany(Collection<T> data,
                                                       Class<? extends MyModel> associationModel) throws CannotSaveModelException {
        //MyModel savedModel = !this.hasLocalId() ? this.mySave() : this;
        for (MyModel model: data){
            this.saveBelongsToMany(model, associationModel);
        }
    }
    public  <T extends MyModel> void saveBelongsToMany(T data,
                                                       Class<? extends MyModel> associationModel) throws CannotSaveModelException {
        //MyModel savedModel = !this.hasLocalId() ? this.mySave() : this;
        try {
            Constructor<? extends MyModel> constructor = associationModel.getConstructor(this.getClass(), data.getClass());
            MyModel instance = constructor.newInstance(this, data);
            instance.deepSave();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            throw new CannotSaveModelException(this);
        }
        /*
        catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Util.appStateError(TAG, "No constructor for class: '" + associationModel.getCanonicalName() + "'");
            throw new CannotSaveModelException(savedModel);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (CannotSaveModelException e) {
            e.printStackTrace();
        }*/
    }

    /*
    public void replaceAssociation(Collection<? extends MyModel> data,
                                Class<? extends MyModel> associationModel) throws CannotSaveModelException {
        this.deleteAssociation(associationModel);
        this.saveBelongsToMany(data, associationModel);
    }*/

    /**
     * Delete association data
     * @param associationModel
     */
    public abstract void deleteAssociation(Class<? extends MyModel> associationModel, Property property);
    /**
     * Save model plus belongs to many associations
     * @return
     */
    public void deepSave() throws CannotSaveModelException {
        Log.v(TAG, "==> Saving model " + this.getClass().getCanonicalName());
        this._saveModelAssociations();
        this.mySave();
    }

    /**
     * Save model plus belongs to many associations
     * @return
     */
    public void mySave() throws CannotSaveModelException {
        try{
            this.save();
        }
        catch (SQLiteException ex){
            Log.e(TAG, "SQLiteException: " + ex);
            if (BuildConfig.DEBUG){
                ex.printStackTrace();
            }
            throw new CannotSaveModelException(this);
        }
    }

    private void _saveModelAssociations() throws CannotSaveModelException {
        Class<? extends MyModel> clazz = this.getClass();
        for (Field field: clazz.getFields()){
            try {
                if (field.isAnnotationPresent(ModelAssociation.class)){
                    ModelAssociation annotation = field.getAnnotation(ModelAssociation.class);
                    switch (annotation.type()){
                        case BELONGS_TO:
                            MyModel fieldValue = (MyModel) field.get(this);
                            if (fieldValue != null){
                                fieldValue.deepSave();
                                field.set(this, fieldValue);
                                Log.d(TAG, "    - Saving deep association for remoteField '" + field.getName());
                            }
                            break;
                        case BELONGS_TO_MANY:
                            Collection<? extends MyModel> fieldValues = (Collection<? extends MyModel>) field.get(this);
                            if (fieldValues != null){
                                if (annotation.saveStrategy() == ModelAssociation.SaveStrategy.REPLACE){
                                    this.deleteAssociation(annotation.joinModel(), (Property) annotation.targetTable().getField(annotation.remoteForeignKey()).get(null));
                                }
                                this.saveBelongsToMany(fieldValues, annotation.joinModel());
                                Log.d(TAG, "    - Saving deep association for remoteField '" + field.getName() + ": size=" + fieldValues.size());
                            }
                            break;
                    }
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, "IllegalAccessException: " + e.getMessage());
                if (BuildConfig.DEBUG){
                    e.printStackTrace();
                }
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "IllegalAccessException: " + e.getMessage());
                if (BuildConfig.DEBUG){
                    e.printStackTrace();
                }
                Util.appStateError(TAG, "Cancel because model are not properly parametrized");
            }
        }
    }

    public boolean hasLocalId() {
        return false;
    }

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
     * that changes should be marked with {@link Bindable} to generate a remoteField in
     * <code>BR</code> to be used as <code>fieldId</code>.
     *
     * @param fieldId The generated BR id for the Bindable remoteField.
     */
    public void notifyPropertyChanged(int fieldId) {
        if (mCallbacks != null) {
            mCallbacks.notifyCallbacks(this, fieldId, null);
        }
    }

    public void mySaveSafeCall() {
        try {
            this.mySave();
        } catch (CannotSaveModelException e) {
            Log.e(TAG, "Cannot save model: " + e.getMessage());
            if (BuildConfig.DEBUG){
                e.printStackTrace();
            }
        }
    }

    public void requireLocalId() throws CannotSaveModelException {
        if (!this.hasLocalId()){
           this.deepSave();
        }
    }

}
