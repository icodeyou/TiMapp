package com.timappweb.timapp.data.models;

import android.location.Location;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.entities.MarkerValueInterface;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "Place")
public class Place extends SyncBaseModel implements Serializable, MarkerValueInterface {

    private static final String TAG = "PlaceEntity" ;

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Spot", onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("spot")
    @Expose
    public Spot             spot;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
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

    @Column(name = "Created")
    @Expose
    public int              created;

    @Column(name = "Latitude")
    @Expose
    public double           latitude;

    @Column(name = "Longitude")
    @Expose
    public double           longitude;

    @Column(name = "CategoryId")
    @Expose
    public int              category_id;

    @Column(name = "Points")
    @Expose(serialize = false, deserialize = true)
    public int              points;

    // =============================================================================================
    // Fields

    @Expose(serialize = false, deserialize = true)
    public int              count_posts;

    @Expose(serialize = false, deserialize = false)
    public int              loaded_time = -1;

    @Expose(serialize = false, deserialize = true)
    public List<Tag>        tags;

    @Expose(serialize = false, deserialize = true)
    public ArrayList<Post>  posts;

    @Expose
    public Integer          spot_id  = null;

    public double           distance = -1;
    // =============================================================================================

    public Place(){
        this.loaded_time = Util.getCurrentTimeSec();
    }

    public Place(int id, double lat, double lng, String name) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.remote_id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.count_posts = 0;
        this.posts = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Place(Location lastLocation, String name, EventCategory eventCategory, Spot spot, String description) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.latitude = lastLocation.getLatitude();
        this.longitude = lastLocation.getLongitude();
        this.name = name;
        this.category_id = eventCategory.remote_id;
        this.description = description;
        if (spot != null){
            this.spot = spot;
            this.spot_id = spot.remote_id;
        }
    }

    public void addPost(Post post){
        this.count_posts++;
        this.posts.add(post);
    }

    /**
     * Return true if this place is not saved on the server
     * @return
     */
    public boolean isNew(){
        return this.remote_id == null;
    }

    public int countPosts(){
        return this.count_posts;
    }

    // Dummy data
    private static int dummyIndice = 0;

    public static Place createDummy(){
        Place place = new Place(1, dummyIndice, dummyIndice, "Test");
        place.tags.add(Tag.createDummy());
        place.addPost(Post.createDummy());
        place.addPost(Post.createDummy());
        place.addPost(Post.createDummy());
        place.addPost(Post.createDummy());
        dummyIndice++;
        return place;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(this.latitude, this.longitude);
    }


    public String getTime() {
        if (this.posts != null && posts.size() > 0){
            return posts.get(posts.size()-1).getPrettyTimeCreated();
        }
        return this.getPrettyTimeCreated();
    }

    /**
     * Get the created as a pretty time format
     *
     * @return
     */
    public String getPrettyTimeCreated() {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(((long) this.created) * 1000));
    }

    @Override
    public String toString() {
        return "Place{" +
                "db_id=" + this.getId() +
                ", remote_id=" + remote_id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", created=" + created +
                ", longitude=" + longitude +
                ", category_id=" + category_id +
                ", spot=" + (spot != null ? spot : "No spot") +
                '}';
    }

    public int countUsers() {
        return this.count_posts;
    }

    /**
     * @param latitude
     * @param longitude
     * @return true if the user can post in the place
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

    public int getPoints() {
        int points = this.points - (Util.getCurrentTimeSec() - this.loaded_time);
        return points > 0 ? points : 0;
    }

    public int getCategoryId() {
        return category_id;
    }

    public int getLevel(){
        return Place.computeLevel(this.getPoints());
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

    public EventCategory getCategory() throws UnknownCategoryException {
        return MyApplication.getCategoryById(this.category_id);
    }


    @Override
    public int getMarkerId() {
        return this.remote_id;
    }

    // =============================================================================================

    @Override
    public boolean isSync(SyncBaseModel o) {
        if (o == null) return false;
        Place place = (Place) o;

        if (spot_id != place.spot_id) return false;
        if (created != place.created) return false;
        if (Double.compare(place.latitude, latitude) != 0) return false;
        if (Double.compare(place.longitude, longitude) != 0) return false;
        if (count_posts != place.count_posts) return false;
        if (category_id != place.category_id) return false;
        if (points != place.points) return false;
        if (!name.equals(place.name)) return false;
        return !(description != null ? !description.equals(place.description) : place.description != null);
    }



    // =============================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Place place = (Place) o;

        if (remote_id != place.remote_id) return false;
        if (spot_id != place.spot_id) return false;
        if (created != place.created) return false;
        if (Double.compare(place.latitude, latitude) != 0) return false;
        if (Double.compare(place.longitude, longitude) != 0) return false;
        if (count_posts != place.count_posts) return false;
        if (category_id != place.category_id) return false;
        if (points != place.points) return false;
        if (spot != null ? !spot.equals(place.spot) : place.spot != null) return false;
        if (!name.equals(place.name)) return false;
        return !(description != null ? !description.equals(place.description) : place.description != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + remote_id;
        return result;
    }


    public From getPicturesQuery() {
        return new Select().from(Picture.class).where("Place = ?", this.getId()).orderBy("created DESC");
    }

    public List<Picture> getPictures() {
        return getPicturesQuery().execute();
    }

    public List<? extends SyncBaseModel> getUsers() {
        return new Select().from(UserPlace.class).where("Place = ?", this.getId()).execute();
    }

    public From getTagsQuery() {
        return new Select()
                .from(Tag.class)
                .where("PlaceTag.Place = ?", this.getId())
                .join(PlaceTag.class).on("Tag.Id = PlaceTag.Tag")
                .orderBy("PlaceTag.CountRef DESC");
    }

    public List<Tag> getTags() {
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

    public double getDistanceFromUser() {
        if (distance != -1){
            return distance;
        }
        this.updateDistanceFromUser();
        return distance;
    }

    public void updateDistanceFromUser() {
        Location location = LocationManager.getLastLocation();
        double distance =  DistanceHelper.distFrom(location.getLatitude(), location.getLongitude(),
                this.latitude, this.longitude);
        this.distance = Math.round(distance);
    }
}
