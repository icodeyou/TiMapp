package com.timappweb.timapp.data.models;

import android.content.Context;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.BR;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.entities.MarkerValueInterface;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import org.ocpsoft.prettytime.PrettyTime;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

/**
 * @warning event cannot be serialize anymore as there is a recursive dependency:
 *  Event -> BELONGS TO -> Picture -> BELONGS TO -> Event
 */
@Table(name = "Event")
public class Event extends SyncBaseModel implements MarkerValueInterface, SyncHistory.HistoryItemInterface{

    private static final String TAG = "PlaceEntity" ;
    public static final String PROPERTY_PICTURE = "picture";
    public static final String PROPERTY_POINTS = "points";

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = Spot.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "spot_id")
    @Column(name = "Spot", onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("spot")
    @Expose
    public Spot             spot;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "user_id")
    @Column(name = "User", onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("user")
    @Expose(serialize = false, deserialize = true)
    public User             user;

    @Column(name = "Name")
    @Expose
    public String           name;

    @Column(name = "Description")
    @Expose
    public String           description;

    @Column(name = "Latitude")
    @Expose
    public double           latitude;

    @Column(name = "Longitude")
    @Expose
    public double           longitude;

    @ModelAssociation(joinModel = EventCategory.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "category_id")
    @Column(name = "Category", notNull = false, onDelete = Column.ForeignKeyAction.SET_NULL, onUpdate = Column.ForeignKeyAction.SET_NULL)
    @SerializedName("category")
    @Expose
    public EventCategory    event_category;

    @Column(name = "Points")
    @Expose(serialize = false, deserialize = true)
    public int              points;

    @Column(name = "CountHere")
    @Expose(serialize = false, deserialize = true)
    public Integer count_here;

    @Column(name = "CountComing")
    @Expose(serialize = false, deserialize = true)
    public Integer count_coming;

    @ModelAssociation(joinModel = Picture.class, type = ModelAssociation.Type.BELONGS_TO, remoteForeignKey = "picture_id")
    @Column(name = "Picture", notNull = false, onDelete = Column.ForeignKeyAction.SET_NULL, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("picture")
    @Expose
    public Picture    picture;


    // =============================================================================================
    // Fields

    @Expose(serialize = false, deserialize = true)
    public int              count_posts;

    @Expose(serialize = false, deserialize = false)
    public int              loaded_time = -1;

    @Expose(serialize = false, deserialize = true)
    public List<Tag>        tags;

    @Expose(serialize = false, deserialize = true)
    public ArrayList<EventPost> eventPosts;

    @Expose(serialize = false, deserialize = false)
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

    public Event(int id, double lat, double lng, String name) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.remote_id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.count_posts = 0;
        this.eventPosts = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Event(Location location, String name, EventCategory eventCategory, Spot spot, String description) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.setLocation(location);
        this.name = name;
        this.event_category = eventCategory;
        this.description = description;
        this.spot = spot;
    }

    public void addPost(EventPost eventPost){
        this.count_posts++;
        this.eventPosts.add(eventPost);
    }

    /**
     * Return true if this event is not saved on the server
     * @return
     */
    public boolean isNew(){
        return this.remote_id == null;
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

    public String getTime() {
        if (this.eventPosts != null && eventPosts.size() > 0){
            return eventPosts.get(eventPosts.size()-1).getPrettyTimeCreated();
        }
        return this.getPrettyTimeCreated();
    }

    /**
     * Get the created as a pretty time format
     *
     * @return
     */
    public String getPrettyTimeCreated() {
        return Util.secondsTimestampToPrettyTime(this.created);
    }

    @Override
    public String toString() {
        return "Event{" +
                "db_id=" + this.getId() +
                ", remote_id=" + remote_id +
                ", name='" + name + '\'' +
                ", location=(" + latitude + "," + longitude + ")" +
                ", created=" + created +
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

    public String displayPoints() {
        int points = this.points - (Util.getCurrentTimeSec() - this.loaded_time);
        return points > 0 ? String.valueOf(points) : "0";
    }

    public String displayCountComing() {
        return count_coming != null ? count_coming.toString() : "0";
    }
    public String displayCountHere() {
        return count_here != null ? count_here.toString() : "0";
    }


    public int getLevel(){
        return Event.computeLevel(this.getPoints());
    }

    public EventCategory getCategory() throws UnknownCategoryException {
        if (event_category == null){
            throw new UnknownCategoryException(-1);
        }
        return event_category;
    }

    @Override
    public int getMarkerId() {
        return this.remote_id;
    }

    public boolean hasDistanceFromUser(){
        this.updateDistanceFromUser();
        return distance != -1;
    }

    public double getDistanceFromUser() {
        this.updateDistanceFromUser();
        return distance;
    }
    public String getAddress(){
        if (hasSpot()){
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

    @Bindable
    public Spot getSpot() {
        return spot;
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
        return name.trim().length() >= ConfigurationProvider.rules().places_min_name_length;
    }

    private static int computeLevel(int points) {
        List<Integer> levels = ConfigurationProvider.rules().places_points_levels;
        int num = 0;
        for (int level: levels){
            if (level >= points){
                return num;
            }
            num++;
        }
        return num;
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

        if (remote_id != event.remote_id) return false;
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
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + remote_id;
        return result;
    }


    public From getPicturesQuery() {
        return new Select().from(Picture.class).where("Event = ?", this.getId()).orderBy("created DESC");
    }

    public List<Picture> getPictures() {
        return getPicturesQuery().execute();
    }

    public List<UserEvent> getUsers() {
        return new Select().from(UserEvent.class).where("Event = ?", this.getId()).execute();
    }

    public From getTagsQuery() {
        return new Select()
                .from(EventTag.class)
                .where("EventTag.Event = ?", this.getId())
                //.join(EventTag.class).on("Tag.Id = EventTag.Tag")
                .orderBy("EventTag.CountRef DESC");
    }
    public void deleteTags() {
        new Delete().from(EventTag.class).where("EventTag.Event = ?", this.getId()).execute();
    }

    public List<EventTag> getTags() {
        return getTagsQuery().execute();
    }

    public boolean hasDescription() {
        return description != null && description.length() > 0;
    }

    public boolean hasSpot() {
        return spot != null;
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
     * TODO remove when using category from the server
     * @return
     */
    public EventCategory getCategoryWithDefault() {
        try {
            return this.getCategory();
        } catch (UnknownCategoryException e) {
            return new EventCategory("else");
        }
    }

    public Event setSpot(Spot spot) {
        this.spot = spot;
        notifyPropertyChanged(BR.spot);
        return this;
    }

    public void setCategory(EventCategory category) {
        this.event_category = category;
    }

    public boolean isOver(){
        return this.getPoints() <= 0;
    }

    public void setLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public Drawable getBackgroundImage(Context context){
        return ContextCompat.getDrawable(context, R.drawable.photo_event_default);
    }

    public boolean hasAddress(){
        return getAddress() != null;
    }

    public boolean hasCategory() {
        return event_category != null;
    }

    public void setCategory(long categoryId) {
        this.event_category = ConfigurationProvider.getEventCategoryByRemoteId(categoryId);
    }

    public User getAuthor() {
        return user;
    }

    public void setAuthor(User user){
        this.user = user;
    }

    public void requestSync() {
        super.requestSync(MyApplication.getApplicationBaseContext(), DataSyncAdapter.SYNC_TYPE_EVENT);
    }

    /**
     * Get invitation sent user
     * @param currentUser
     */
    public List<EventsInvitation> getSentInvitationsByUser(User currentUser) {
        List<EventsInvitation> invitations = new Select()
                .from(EventsInvitation.class)
                .where("Event = ? AND UserSource = ?", this.getId(), currentUser.getId())
                .execute();
        return invitations;
    }

    public long getPointsLong() {
        return getPoints() * 1000;
    }

    public boolean hasPicture() {
        return picture != null;
    }

    public String getBackgroundUrl() {
        return picture.getThumbnailUrl(Picture.ThumbnailType.CARD);
    }

    @Override
    public Event deepSave() throws CannotSaveModelException {
        if (this.hasPicture()){
            // We need to save first the event as the picture require this association
            // If we don't do that there is an infinite loop as event save picture which save event ...
            Picture tmp = this.picture;
            this.picture = null;
            Event newModel = super.deepSave();
            newModel.picture = tmp;
            newModel.picture.event = newModel;
            newModel.picture.mySave();
            newModel.mySave();
            return newModel;
        }
        else {
            return super.deepSave();
        }
    }

    public Picture getPicture() {
        return picture;
    }


    public int getLevelBackground() {
        switch (this.getLevel()) {
            case 0:
                return R.drawable.b1;
            case 1:
                return R.drawable.b2;
            case 2:
                return R.drawable.b3;
            case 3:
                return R.drawable.b4;
            case 4:
            default:
                return R.drawable.b4;
        }
    }

    public From getPeopleQuery() {
        return new Select().from(UserEvent.class)
                .where("Event = ?", this.getId())
                .orderBy("UserEvent.Created DESC");
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

    public boolean isOwner(User currentUser) {
        return this.user != null && this.user.equals(currentUser);
    }

    public boolean hasLocation() {
        return false;
    }
}
