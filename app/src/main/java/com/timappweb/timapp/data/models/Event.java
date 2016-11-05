package com.timappweb.timapp.data.models;

import android.content.Context;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.Table;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.entities.MarkerValueInterface;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @warning event cannot be serialize anymore as there is a recursive dependency:
 *  Event -> BELONGS TO -> Picture -> BELONGS TO -> Event
 */
@Table(database = AppDatabase.class)
public class Event extends SyncBaseModel implements MarkerValueInterface, SyncHistory.HistoryItemInterface{

    public enum VisiblityStatus {OVER, INACTIVE, ACTIVE, PLANNED};

    private static final String TAG = "PlaceEntity" ;
    public static final String PROPERTY_PICTURE = "picture";
    public static final String PROPERTY_POINTS = "points";
    private static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_INACTIVITY_THRESHOLD =  "inactivity_threshold";


    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = Spot.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "spot_id")
    @ForeignKey(tableClass = Spot.class,
            saveForeignKeyModel = true,
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE)
    @SerializedName("spot")
    @Expose
    public Spot             spot;

    @ForeignKey(tableClass = User.class,
            saveForeignKeyModel = true,
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE)
    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "user_id")
    @SerializedName("user")
    @Expose(serialize = true, deserialize = true)
    public User             user;

    @Column
    @Expose
    public String           name;

    @Column
    @Expose
    public String           description;

    @Column
    @Expose
    public double           latitude;

    @Column
    @Expose
    public double           longitude;

    @Column
    @Expose
    public int              start_date;

    @Column
    @Expose
    public int              end_date;

    @Column
    @Expose
    public int              inactivity_threshold;

    @Column
    @Expose
    public int              last_activity;

    @ModelAssociation(joinModel = EventCategory.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "category_id")
    @ForeignKey(tableClass = EventCategory.class,
            saveForeignKeyModel = false,
            stubbedRelationship = false,
            onDelete = ForeignKeyAction.SET_NULL,
            onUpdate = ForeignKeyAction.CASCADE)
    @SerializedName("category")
    @Expose
    public EventCategory    event_category;

    @Column
    @Expose(serialize = true, deserialize = true)
    public int              points;

    @Column
    @Expose(serialize = true, deserialize = true)
    public Integer count_here;

    @Column
    @Expose(serialize = true, deserialize = true)
    public Integer count_coming;

    @ModelAssociation(joinModel = Picture.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "picture_id")
    @ForeignKey(tableClass = Picture.class,
            saveForeignKeyModel = true,
            onDelete = ForeignKeyAction.SET_NULL,
            onUpdate = ForeignKeyAction.CASCADE)
    @SerializedName("picture")
    @Expose
    public Picture    picture;


    // =============================================================================================
    // Fields

    @Expose(serialize = true, deserialize = true)
    public int              count_posts;

    @Expose(serialize = true, deserialize = true)
    public int              loaded_time = -1;

    @Expose(serialize = true, deserialize = true)
    public List<Tag>        tags;

    @Expose(serialize = true, deserialize = true)
    public double           distance = -1;

    // =============================================================================================

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Picture backgroundPicture;

    public void addPropertyChangeListener(String field, PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(field, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    // =============================================================================================

    public Event(){
        this.loaded_time = Util.getCurrentTimeSec();
    }

    /**
     * Return true if this event is not saved on the server
     * @return
     */
    public boolean isNew(){
        return this.id == null;
    }

    public int countPosts(){
        return this.count_posts;
    }

    // =============================================================================================

    @Override
    public LatLng getPosition() {
        return new LatLng(this.latitude, this.longitude);
    }

    public long getTimestampPoints() {
        return points;
    }

    /**
     * Get the created as a pretty time format
     *
     * @return
     */
    public String getPrettyTimeBegin() {
        Log.d("Debug pretty time", "Start date Event : " + start_date);
        return Util.secondsTimestampToPrettyTime(this.start_date);
    }

    @Override
    public String toString() {
        return "Event{" +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", visibility='" + getVisibilityStatus() + '\'' +
                ", date='from " + new Date(start_date *1000).toString() + " to "  + (end_date > 0 ? new Date(end_date*1000).toString() : '?') + '\'' +
                ", location=(" + latitude + "," + longitude + ")" +
                ", created=" + new Date(created*1000).toString()  +
                ", category=" + (event_category != null ? event_category.getName() : "NONE") +
                ", spot=" + (spot != null ? spot.getName() : "No spot") +
                ", author=" + (user != null ? user.username : "No author") +
                ", picture=" + (picture != null ? picture.getUrl() : "No picture") +
                '}';
    }

    public int getPoints() {
        int points = this.points - (Util.getCurrentTimeSec() - this.loaded_time);
        return points > 0 ? points : 0;
    }

    public boolean hasName(){
        return this.name != null && this.name.length() > 0;
    }

    public EventCategory getCategory() {
        return event_category;
    }

    @Override
    public int getMarkerId() {
        return (int)(long)this.id;
    } // TODO [critical]

    public boolean hasDistanceFromUser(){
        this.updateDistanceFromUser();
        return distance != -1;
    }

    public double getDistanceFromUser() {
        this.updateDistanceFromUser();
        return distance;
    }
    public String getAddress(){
        if (spot != null){
            return spot.getAddress();
        }
        else{
            return null;
        }
    }

    public MarkerOptions getMarkerOption() {
        return new MarkerOptions().position(getPosition());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // =============================================================================================

    /**
     * @param latitude
     * @param longitude
     * @return true if the user can eventPost in the event
     */
    public boolean isUserAround(double latitude, double longitude) {
        return DistanceHelper.distFrom(latitude, longitude, this.latitude, this.longitude)
                < ConfigurationProvider.rules().place_max_reachable;
    }

    public boolean isUserAround() {
        Location lastLocation = LocationManager.getLastLocation();
        if (lastLocation == null) return false;
        return this.isUserAround(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= ConfigurationProvider.rules().places_min_name_length;
    }

    private static ApplicationRules.LevelPointInfo computeLevel(int points) {
        List<ApplicationRules.LevelPointInfo> levels = ConfigurationProvider.rules().places_points_levels;
        if (levels != null) {
            for (ApplicationRules.LevelPointInfo level : levels) {
                if (points <= level.to) {
                    return level;
                }
            }
            return levels.get(levels.size()-1);
        }
        return null;
    }

    // =============================================================================================
    @Override
    public boolean isSync(SyncBaseModel o) {
        if (o == null) return false;
        Event event = (Event) o;

        if (created != event.created) return false;
        if (Double.compare(event.latitude, latitude) != 0) return false;
        if (Double.compare(event.longitude, longitude) != 0) return false;
        if (count_posts != event.count_posts) return false;
        if (points != event.points) return false;
        if (!name.equals(event.name)) return false;
        return !(description != null ? !description.equals(event.description) : event.description != null);
    }



    // =============================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Event event = (Event) o;

        if (id != event.id) return false;
        if (created != event.created) return false;
        if (Double.compare(event.latitude, latitude) != 0) return false;
        if (Double.compare(event.longitude, longitude) != 0) return false;
        if (count_posts != event.count_posts) return false;
        if (points != event.points) return false;
        if (spot != null ? !spot.equals(event.spot) : event.spot != null) return false;
        if (!name.equals(event.name)) return false;
        return !(description != null ? !description.equals(event.description) : event.description != null);

    }

    @Override
    public int getSyncType() {
        return DataSyncAdapter.SYNC_TYPE_EVENT;
    }

    public boolean hasDescription() {
        return description != null && description.length() > 0;
    }

    public boolean hasTags() {
        return this.tags != null && tags.size() > 0;
    }

    public void updateDistanceFromUser() {
        Location location = LocationManager.getLastLocation();
        if (location == null)
            return;
        double distance =  DistanceHelper.distFrom(location.getLatitude(), location.getLongitude(),
                this.latitude, this.longitude);
        this.distance = Math.round(distance);
    }

    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public EventCategory getCategoryWithDefault() {
        if (this.event_category != null){
            return this.getCategory();
        } else {
            return new EventCategory("else"); // TODO use constante
        }
    }

    public Event setSpot(Spot spot) {
        this.spot = spot;
        //notifyPropertyChanged(BR.spot);
        return this;
    }

    public void setCategory(EventCategory category) {
        this.event_category = category;
    }

    /**
     * Return true if this event is over.
     * @return
     */
    public boolean isOver(){
        return getVisibilityStatus() == VisiblityStatus.OVER;
    }

    /**
     *
     * @return
     */
    public boolean hasVisibilityStatus(VisiblityStatus status){
        return getVisibilityStatus() == status;
    }

    public boolean hasBegin(){
        return this.start_date <= Util.getCurrentTimeSec();
    }

    public VisiblityStatus getVisibilityStatus(){
        if (!this.hasBegin()){
            return VisiblityStatus.PLANNED;
        }
        else if (this.end_date != 0){
            return this.end_date < Util.getCurrentTimeSec() ? VisiblityStatus.OVER : VisiblityStatus.ACTIVE;
        }
        else {
            return Util.isOlderThan(this.inactivity_threshold, ConfigurationProvider.rules().place_max_inactivity_threshold)
                    ? VisiblityStatus.OVER
                    : (this.inactivity_threshold <= Util.getCurrentTimeSec())
                        ? VisiblityStatus.INACTIVE
                        : VisiblityStatus.ACTIVE;
        }
    }

    public boolean hasFinishedDate() {
        return end_date != 0;
    }
    public int getEndDate() {
        return end_date;
    }

    public boolean isInactive() {
        return getVisibilityStatus() == VisiblityStatus.INACTIVE;
    }

    public int getInactivityDurationSeconds(){
        return Util.getCurrentTimeSec() - this.last_activity ;
    }

    public String getPrettyInactivityDuration() {
        int inactivityDuration = getInactivityDurationSeconds() ;
        return Util.secondsDurationToPrettyTime(inactivityDuration);
    }

    public void setLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        pcs.firePropertyChange(PROPERTY_LOCATION, null, location);
    }

    public Drawable getBackgroundImage(Context context){
        return ContextCompat.getDrawable(context, R.drawable.photo_event_default);
    }

    public boolean hasAddress(){
        return getAddress() != null;
    }

    public void setCategory(long categoryId) {
        this.event_category = ConfigurationProvider.getEventCategoryByRemoteId(categoryId);
    }

    public void setAuthor(User user){
        this.user = user;
    }

    public String getBackgroundUrl() {
        return picture.getThumbnailUrl(Picture.ThumbnailType.CARD);
    }

    @Override
    public void deepSave() throws CannotSaveModelException {
        if (this.user != null) this.user.deepSave();
        if (this.spot != null) this.spot.deepSave();
        if (this.picture != null){
            this.picture.event = this;
            this.picture.mySave();
        }
        else{
            this.mySave();
        }
    }

    /*
    public Event deepSaveTransaction() throws CannotSaveModelException {
        ActiveAndroid.beginTransaction();
        Event event = this.deepSave();
        ActiveAndroid.setTransactionSuccessful();
        ActiveAndroid.endTransaction();
        return event;
    }*/

    public Picture getPicture() {
        return picture;
    }

    public String getLevelColorHex(Context context) {
        ApplicationRules.LevelPointInfo level = Event.computeLevel(
                getVisibilityStatus() == VisiblityStatus.INACTIVE
                    ? 0
                    : this.getPoints()
        );

        if (level != null){
            return "#" + level.icon_color;
        }
        else {
            Log.e(TAG, "The event has a wrong level !");
            return "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimary));
        }
    }

    public void setPoints(int points) {
        this.points = points;
        this.loaded_time = Util.getCurrentTimeSec();
        this.pcs.firePropertyChange(PROPERTY_POINTS, -1, points); // TODO set correct value for old value
    }

    public void setBackgroundPicture(Picture backgroundPicture) {
        this.picture = backgroundPicture;
        this.pcs.firePropertyChange(PROPERTY_PICTURE, null, backgroundPicture); // TODO set correct value for old value (warning: it does not trigger anything if old and new are the same)
    }

    public void setInactivityThreshold(int value) {
        this.inactivity_threshold = value;
        this.pcs.firePropertyChange(PROPERTY_INACTIVITY_THRESHOLD, -1, value); // TODO set correct value for old value
    }

    public boolean isOwner(User currentUser) {
        return this.user != null && this.user.id == currentUser.id;
    }


    // =============================================================================================


    public void savePicture() {
        if (this.picture != null){
            if (this.picture.event == null){
                this.picture.event = this;
            }
            this.picture.mySaveSafeCall();
        }
        this.mySaveSafeCall();
    }

}
