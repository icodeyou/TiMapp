package com.timappweb.timapp.rest.io.serializers;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.utils.PictureUtility;

import java.io.File;
import java.io.IOException;

import okhttp3.RequestBody;

/**
 * Created by Stephane on 23/09/2016.
 */
public class AddPictureMapper {

    private final File file;

    public AddPictureMapper(File file) throws CannotUploadPictureException {
        ApplicationRules rules = ConfigurationProvider.rules();

        try{
            this.file = PictureUtility.resize(file, rules.picture_max_width, rules.picture_max_height);
        } catch (IOException e) {
            throw new CannotUploadPictureException(R.string.cannot_read_this_picture);
        }
        if (this.file.length() > rules.picture_max_size){
            throw new CannotUploadPictureException(R.string.cannot_resize_picture);
        }
        else if (this.file.length() <= rules.picture_min_size){
            throw new CannotUploadPictureException(R.string.error_picture_too_small);
        }

    }

    public RequestBody build() {
        return getBuilder()
                .build();
    }

    public MultipartBuilder getBuilder(){
        return new MultipartBuilder()
                .add("photo", this.file);
    }

    public class CannotUploadPictureException extends Exception {
        private final int resId;

        public CannotUploadPictureException(int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return resId;
        }
    }
}
