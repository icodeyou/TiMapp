package com.timappweb.timapp.data.loader.paginate;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by Stephane on 21/10/2016.
 */
public class CursorPaginateDataLoader<DataType extends MyModel, RemoteType extends MyModel> {

    public enum LoadType {NEXT, PREV, UPDATE}

    private static final String TAG = "CursorPaginateDataLoad";
    private boolean hasInCache = false;
    public CacheInfo   cacheInfo;
    private LinkedList<PaginateFilter> filters;
    private From localBaseQuery;
    protected Callback<DataType> callback;
    protected HttpCallManager<ResponseWrapper<JsonObject>> currentCallManager;
    private Map<String, String> queryParams;
    private final Class<RemoteType> remoteClazz;
    private final CursorPaginateBackend service;
    private final Gson gson;
    private CacheCallback<DataType, RemoteType> cacheCallback;
    private From clearQuery;
    private boolean cacheEnabled = false;


    private CursorPaginateDataLoader(Class<RemoteType> remoteClazz) {
        this.service = RestClient.instance().createService(CursorPaginateBackend.class);
        this.gson = RestClient.instance().getGson();
        this.remoteClazz = remoteClazz;
        this.filters = new LinkedList<>();
    }


    public static <LT extends MyModel, RT extends MyModel> CursorPaginateDataLoader<LT, RT> create(String url, Class<RT> clazz){
        CursorPaginateDataLoader<LT, RT> loader = new CursorPaginateDataLoader(clazz);
        loader.cacheInfo = new CacheInfo();
        loader.cacheInfo.nextUrl = url;
        loader.cacheInfo.initialUrl = url;
        return loader;
    }


    public CursorPaginateDataLoader<DataType, RemoteType> setLocalQuery(From from){
        this.localBaseQuery = from;
        return this;
    }

    public CursorPaginateDataLoader<DataType, RemoteType> setClearQuery(From clearQuery) {
        this.clearQuery = clearQuery;
        return this;
    }

    public CursorPaginateDataLoader<DataType, RemoteType> addFilter(PaginateFilter filter){
        this.filters.add(filter);
        return this;
    }

    public CursorPaginateDataLoader<DataType, RemoteType> setCacheCallback(CacheCallback<DataType, RemoteType> cacheCallback) {
        this.cacheCallback = cacheCallback;
        return this;
    }

    public CursorPaginateDataLoader<DataType, RemoteType> setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    public CursorPaginateDataLoader addQueryParam(String key, String value) {
        if (this.queryParams == null) this.queryParams = new HashMap<>();
        this.queryParams.put(key, value);
        return this;
    }

    public void deleteCache(){
        if (this.cacheInfo.cacheId != null)
            new Delete().from(CacheInfo.class).where("CacheId = ?", this.cacheInfo.cacheId).execute();
        this.hasInCache = false;
        if (this.clearQuery != null) this.clearQuery.execute();
    }

    public void saveInCache() throws Exception {
        if (this.cacheInfo.cacheId == null){
            Log.d(TAG, "Cache is not activated here. Cannot save");
            return;
        }
        Log.d(TAG, "Saving in cache: " + this.cacheInfo);
        this.cacheInfo.mySave();
    }

    public CursorPaginateDataLoader<DataType, RemoteType> initCache(String cacheId, long expireDelay){
        this.cacheEnabled = true;
        Log.d(TAG, "Init localBaseQuery cache with id="+ cacheId);
        CacheInfo existingCacheInfo = new Select()
                .from(CacheInfo.class)
                .where("CacheId = ?", cacheId)
                .executeSingle();

        if (existingCacheInfo != null){
            if (existingCacheInfo.isValid()){
                this.cacheInfo = existingCacheInfo;
                this.hasInCache = true;
                return this;
            }
            else{
                Log.w(TAG, "Cache " + this.cacheInfo + " is outdated... Removing...");
                existingCacheInfo.delete();
            }
        }

        this.hasInCache = false;
        this.cacheInfo.cacheId = cacheId;
        this.cacheInfo.expireDate = (expireDelay > 0 ? System.currentTimeMillis() + expireDelay : 0);

        return this;
    }

    public CursorPaginateDataLoader<DataType, RemoteType> setLimit(int limit) {
        this.cacheInfo.limit = limit;
        return this;
    }
    public CursorPaginateDataLoader<DataType, RemoteType> setUpdateLimit(int limit) {
        this.cacheInfo.updateLimit = limit;
        return this;
    }

    public String getUrl(LoadType type){
        switch (type){
            case NEXT:
                return this.cacheInfo.nextUrl;
            case UPDATE:
                return this.cacheInfo.updateUrl;
            case PREV:
                return this.cacheInfo.prevUrl;
            default:
                return null;
        }
    }

    public boolean isFirstLoad() {
        return this.cacheInfo.lastUpdate == -1;
    }

    public Call<ResponseWrapper<JsonObject>> getCall(LoadType type) {
        if (this.queryParams != null){
            return this.service.get(this.getUrl(type), this.queryParams);
        }
        else{
            return this.service.get(this.getUrl(type));
        }
    }

    public void _load(final LoadType loadType){
        if (this.currentCallManager != null && !this.currentCallManager.isDone()){
            Log.i(TAG, "Already loading... Please wait...");
            if (loadType == LoadType.NEXT){
                if (callback != null) callback.onLoadEnd(null, loadType, false);
            }
            return ;
        }

        if (loadType == LoadType.NEXT && this.hasInCache){
            if (callback != null) callback.onLoadStart(loadType);
            this.localLoad();
        }
        else{
            if (this.getUrl(loadType) == null){
                Log.i(TAG, "There is no more data to load");
                if (loadType == LoadType.NEXT){
                    if (callback != null) callback.onLoadEnd(null, loadType, false);
                }
                return ;
            }
            if (callback != null) {
                callback.onLoadStart(loadType);
            }
            this.remoteLoad(loadType);
        }
    }

    private static String _buildWhereClause(List<Object> values, LinkedList<PaginateFilter> filters){
        PaginateFilter filter = filters.pollFirst();
        // Last field is uniq
        if (filters.size() == 0) {
            values.add(filter.value);
            return "("+filter.localField + filter.getSign() + " ?)";
        }
        else {
            values.add(filter.value);
            values.add(filter.value);
            return "("+filter.localField + filter.getSign() +" ? OR ("+filter.localField +" = ? AND " + _buildWhereClause(values, filters) + ") )";
        }
    }

    public void localLoad(){
        Log.i(TAG, "Loading from local db");
        From from = this.localBaseQuery;
        String orderBy = "";
        LinkedList<Object> args = new LinkedList<>();
        String where = _buildWhereClause(args, (LinkedList<PaginateFilter>) this.filters.clone());
        from.where(where, args);
        for (PaginateFilter filter: this.filters){
            orderBy += (orderBy.length() == 0 ? "" : ", ") + filter.orderBy();
        }
        from.orderBy(orderBy);
        Log.d(TAG, "Generated query: " + from.toSql());

        List<DataType> items = from.limit(this.cacheInfo.limit).execute();
        Log.i(TAG, "Loading " + items.size() + " item(s) localBaseQuery local db");

        if (items.size() == 0){
            Log.d(TAG, "There is no data in cache, trigger server load");
            this.cacheInfo.reset();
            this.hasInCache = false;
            this.remoteLoad(LoadType.NEXT);
            return;
        }
        else if (items.size() < this.cacheInfo.limit) {
            this.hasInCache = false;
            Log.d(TAG, "There is no more data in cache, next request against the server");
        }

        DataType lastItem = items.get(items.size() - 1);
        for (PaginateFilter filter: this.filters){
            filter.updateValue(lastItem);
        }
        LoadInfo<DataType> loadInfo = new LoadInfo<DataType>(items, this.cacheInfo.total, null);
        this.callback.onLoadEnd(loadInfo, LoadType.NEXT, false);
    }

    private void remoteLoad(final LoadType loadType) {
        Log.i(TAG, "Loading url: " + this.getUrl(loadType));
        this.currentCallManager = RestClient.buildCall(this.getCall(loadType))
                .onResponse(new HttpCallback<ResponseWrapper<JsonObject>>() {
                    @Override
                    public void successful(ResponseWrapper<JsonObject> feedback) throws Exception {
                        CursorPaginateDataLoader.this.cacheInfo.updateInfo(feedback, loadType);
                        Log.d(TAG, "Finish loading " + loadType + ", next url is now: " + CursorPaginateDataLoader.this.cacheInfo.nextUrl);
                        List<DataType> items = CursorPaginateDataLoader.this._parseItems(feedback.items);

                        boolean overwrite = false;
                        if (loadType == LoadType.UPDATE && items.size() == feedback.perPage){
                            Log.w(TAG, "Reaching limit for update (" + feedback.perPage + ")");
                            CursorPaginateDataLoader.this.hasInCache = false;
                            overwrite = true;
                        }
                        if (callback != null){
                            callback.onLoadEnd(new LoadInfo<DataType>(items, feedback.total, feedback.extra), loadType, overwrite);
                        }
                        try{
                            CursorPaginateDataLoader.this.saveInCache();
                        }
                        catch (Exception ex){
                            Log.e(TAG, "Cannot save in cache: " + ex.getMessage());
                        }
                    }
                })
                .onFinally(new HttpCallManager.FinallyCallback<ResponseWrapper<JsonObject>>() {
                    @Override
                    public void onFinally(Response<ResponseWrapper<JsonObject>> response, Throwable error) {
                        if (error != null || !response.isSuccessful()){
                            if (callback != null) callback.onLoadError(error, loadType);
                            //CursorPaginateDataLoader.this.nextUrl = null;
                            //CursorPaginateDataLoader.this.prevUrl = null;
                            //CursorPaginateDataLoader.this.updateUrl = null;
                        }
                    }
                })
                .perform();
    }

    public synchronized void loadNext(){
        this._load(LoadType.NEXT);
    }

    public void loadPrev(){
        this._load(LoadType.PREV);
    }

    public void update(){
        if (this.isFirstLoad()){
            this.loadNext();
        }
        else{
            this._load(LoadType.UPDATE);
        }
    }


    public boolean hasMoreData() {
        return this.cacheInfo.nextUrl != null || this.hasInCache;
    }

    public void setCallback(Callback<DataType> callback) {
        this.callback = callback;
    }

    private List<DataType> _parseItems(List<JsonObject> items) {
        List<DataType> result = new ArrayList<>(items.size());

        if (this.cacheEnabled) {
            ActiveAndroid.beginTransaction();
        }

        for (JsonObject item: items){
            RemoteType remoteModel = this.gson.fromJson(item, remoteClazz);
            DataType model;
            if (cacheCallback != null){
                model = this.cacheCallback.beforeSaveModel(remoteModel);
            }
            else{
                model = (DataType) remoteModel;
            }
            if (this.cacheEnabled){
                Log.d(TAG, "Saving in local db: " + model);
                try {
                    model = model.deepSave();

                } catch (CannotSaveModelException e) {
                    Log.e(TAG, "Cannot save model: " + e.getMessage());
                }
            }
            result.add(model);
        }

        if (this.cacheEnabled) {
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
        }
        return result;
    }


    public CursorPaginateDataLoader<DataType, RemoteType> enableCache(boolean b) {
        this.cacheEnabled = b;
        return this;
    }

    /**
     *
     * @param <T>
     */
    public interface Callback<T>{

        void onLoadEnd(LoadInfo<T> data, LoadType type, boolean overwrite);

        void onLoadError(Throwable error, LoadType type);

        void onLoadStart(LoadType type);
    }

    public static class LoadInfo<T>{

        public List<T> items;
        public JsonObject extra;
        public int total;

        public LoadInfo(List<T> items, int total, JsonObject extra) {
            this.items = items;
            this.extra = extra;
            this.total = total;
        }

    }

    public static class ResponseWrapper<T>{
        @Expose
        public List<T> items;

        @Expose
        public JsonObject paginate;

        @Expose
        public long time;

        @Expose
        public int perPage;

        @Expose
        int total = -1;

        @Expose
        public JsonObject extra;

        public String getNextUrl(){
            try {
                return this.paginate.get("next").getAsString();
            }
            catch (Exception ex){
                Log.e(TAG, "Cannot get next url in server response: " + ex.getMessage());
                return null;
            }
        }

        public String getUpdateUrl() {
            try {
                return this.paginate.get("update").getAsString();
            }
            catch (Exception ex){
                Log.e(TAG, "Cannot get update url in server response: " + ex.getMessage());
                return null;
            }
        }

        public String getPrevUrl() {
            try {
                return this.paginate.get("prev").getAsString();
            }
            catch (Exception ex){
                Log.e(TAG, "Cannot get prev url in server response: " + ex.getMessage());
                return null;
            }
        }
    }

    public interface CursorPaginateBackend{

        @GET
        Call<ResponseWrapper<JsonObject>> get(@Url String url);

        @GET
        Call<ResponseWrapper<JsonObject>> get(@Url String url, @QueryMap Map<String, String> data);

        @GET
        Call<ResponseWrapper<JsonObject>> update(@Url String url, @QueryMap Map<String, String> data);

        @GET
        Call<ResponseWrapper<JsonObject>> update(@Url String url);
    }


    /**
     *
     * @param <T>
     */
    public interface CacheEngine<T>{

        void init(String id);

        List<T> load();

    }

    @Table(name = "CursorPaginateCacheInfo")
    public static class CacheInfo extends MyModel {

        public      int         limit = 10;
        public      int         updateLimit = 20;

        /**
         * Sync group key
         */
        @Column(name = "CacheId", notNull = true, unique = true)
        public String cacheId;

        @Column(name = "LastUpdate")
        protected   long        lastUpdate = -1;

        @Column(name = "UpdateUrl")
        protected   String      updateUrl;

        @Column(name = "NextUrl")
        protected   String      nextUrl;

        @Column(name = "InitialUrl")
        protected   String      initialUrl;

        @Column(name = "PrevUrl")
        protected   String      prevUrl;

        @Column(name = "ExpireDate")
        protected   long      expireDate = 0;

        @Column(name = "Total")
        protected   int      total = -1;

        public CacheInfo() {}

        public boolean isValid(){
            return expireDate == 0 || (System.currentTimeMillis() < expireDate);
        }

        public void updateInfo(ResponseWrapper<JsonObject> feedback, LoadType loadType) {
            if (loadType == LoadType.NEXT) this.nextUrl = feedback.getNextUrl();
            if (loadType == LoadType.PREV) this.prevUrl = feedback.getPrevUrl();
            this.updateUrl = feedback.getUpdateUrl();
            if (loadType == LoadType.UPDATE || this.lastUpdate == -1) this.lastUpdate = feedback.time;
            this.updateUrl = feedback.getUpdateUrl();
            this.total = feedback.total;
        }

        @Override
        public String toString() {
            return "CacheInfo{" +
                    "limit=" + limit +
                    ", cacheId='" + cacheId + '\'' +
                    ", lastUpdate=" + lastUpdate +
                    ", updateUrl='" + updateUrl + '\'' +
                    ", nextUrl='" + nextUrl + '\'' +
                    ", initialUrl='" + initialUrl + '\'' +
                    ", prevUrl='" + prevUrl + '\'' +
                    '}';
        }

        public void reset() {
            nextUrl = this.initialUrl;
            lastUpdate = -1;
        }
    }


    public static class PaginateFilter{

        public static final String GTE = ">=";
        public static final String LTE = "<=";
        public static final String LT = "<";
        public static final String GT = ">";

        public static final String ASC = "asc";
        public static final String DESC = "desc";
        private static FilterValueTransformer<SyncBaseModel> syncIdTransformer;
        private static FilterValueTransformer<SyncBaseModel> createdIdTransformer;

        public final String order;
        public final String localField;
        public final String remoteField;
        public final FilterValueTransformer transformer;
        public Object value;

        public PaginateFilter(String localField, String order, FilterValueTransformer transformer) {
            this(localField, localField, order, transformer);
        }

        public PaginateFilter(String localField, String remoteField, String order, FilterValueTransformer<? extends MyModel> transformer) {
            this.order = order;
            this.localField = localField;
            this.remoteField = remoteField;
            this.transformer = transformer;
        }

        public String toServerParams(){
            String res = this.remoteField + ":" + this.order;
            if (value != null){
                res += ":" + value;
            }
            return res;
        }

        public String getSign(){
            return this.order == DESC ? LT : GT;
        }

        public String orderBy(){
            return this.localField + " " + this.order;
        }

        public void updateValue(MyModel model) {
            this.value = this.transformer.transform(model);
        }

        public static PaginateFilter createSyncIdFilter() {
            return new CursorPaginateDataLoader.PaginateFilter("SyncId", "id", CursorPaginateDataLoader.PaginateFilter.DESC, getSyncIdTransformer());
        }

        public static CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel> getSyncIdTransformer(){
            if (syncIdTransformer == null){
                syncIdTransformer = new CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel>(){
                    @Override
                    public Object transform(SyncBaseModel model) {
                        return model.getRemoteId();
                    }
                };
            }
            return syncIdTransformer;
        }

        public static PaginateFilter createCreatedFilter() {
            return new CursorPaginateDataLoader.PaginateFilter("created", CursorPaginateDataLoader.PaginateFilter.DESC, getCreatedTransformer());
        }

        public static FilterValueTransformer<SyncBaseModel> getCreatedTransformer() {
            if (createdIdTransformer == null){
                createdIdTransformer = new CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel>() {
                    @Override
                    public Object transform(SyncBaseModel model) {
                        return model.created;
                    }
                };
            }
            return createdIdTransformer;
        }
    }

    public interface FilterValueTransformer<T extends MyModel>{
        Object transform(T model);
    }
    public interface CacheCallback<LocalType extends MyModel, RemoteType extends MyModel>{
        LocalType beforeSaveModel(RemoteType model);
    }
}

