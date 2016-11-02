package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.utils.SearchHistory;

import java.util.List;

@Table(name = "Tag")
public class Tag extends SyncBaseModel implements SearchHistory.SearchableItem{

    // TODO [Jack][critical][#181] DO NOT USE THIS USE CONFIGURATION
    public static final int MINLENGTH = 2;
    public static final int MAXLENGTH = 30;

    // =============================================================================================
    // DATABASE

    @Column(name = "Name", notNull = true)
    @Expose
    public String name;

    @Column(name = "CountRef")
    @Expose(serialize = false, deserialize = true)
    public int count_ref;

    // =============================================================================================

    public Tag() {
    }

    public Tag(String name, int count_ref)  {
        this.name = name;
        this.count_ref = count_ref;
    }

    public Tag(String name) {
        this.count_ref = 0;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return name.equals(tag.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isShortEnough() {
        boolean bool =  getName().length()>=MINLENGTH;
        return  bool;
    }

    public boolean isLongEnough() {
        boolean bool =  getName().length()<= MAXLENGTH;
        return  bool;
    }

    @Override
    public boolean matchSearch(String term) {
        return this.name.startsWith(term);
    }

    public static String tagsToString(List<Tag> tags){
        if (tags == null || tags.size() == 0){
            return "";
        }
        String res = tags.get(0).getName();

        for (int i = 1; i < tags.size(); i++){
            res += "," + tags.get(i).name;
        }
        return res;
    }

    // =============================================================================================

    @Override
    public boolean isSync(SyncBaseModel o) {
        if (o == null) return false ;
        if (!(o instanceof Tag)) return false ;
        Tag tag = (Tag) o;
        return tag.count_ref == this.count_ref;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id='" + this.getId() + '\'' +
                "remoteId='" + this.remote_id + '\'' +
                "name='" + name + '\'' +
                ", count_ref=" + count_ref +
                '}';
    }

    public static From querySuggestTagForEvent(Event event) {
        return new Select()
                .from(Tag.class)
                .leftJoin(EventTag.class).on("Tag.Id = EventTag.Tag AND EventTag.Event = ?", event.getId())
                .orderBy("EventTag.CountRef DESC, Tag.CountRef DESC");
    }

    @Override
    public <T extends MyModel> T deepSave() throws CannotSaveModelException {
        return (T) this.mySave();
    }
}
