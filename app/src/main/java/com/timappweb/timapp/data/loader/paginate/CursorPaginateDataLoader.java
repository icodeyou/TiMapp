package com.timappweb.timapp.data.loader.paginate;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.MyModel;
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
    private Where<DataType> localBaseQuery;
    protected Callback<DataType> callback;
    protected HttpCallManager<ResponseWrapper<JsonObject>> currentCallManager;
    private Map<String, String> queryParams;
    private final Class<RemoteType> remoteClazz;
    private final CursorPaginateBackend service;
    private final Gson gson;
    private CacheCallback<DataType, RemoteType> cacheCallback;
    private Where<DataType> clearQuery;
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


    public CursorPaginateDataLoader<DataType, RemoteType> setLocalQuery(Where<DataType> where){
        this.localBaseQuery = where;
        return this;
    }

    public CursorPaginateDataLoader<DataType, RemoteType> setClearQuery(Where<DataType> clearQuery) {
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
        Log.d(TAG, "Deleting cache with id: " + this.cacheInfo.cacheId);
        if (this.cacheInfo.cacheId != null)
            SQLite.delete(CacheInfo.class).where(CacheInfo_Table.cacheId.eq(this.cacheInfo.cacheId)).execute();
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
        CacheInfo existingCacheInfo = SQLite.select()
                .from(CacheInfo.class)
                .where(CacheInfo_Table.cacheId.eq(cacheId))
                .querySingle();

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

    private static ConditionGroup _buildWhereClause(Where query, LinkedList<PaginateFilter> filters){
        PaginateFilter filter = filters.pollFirst();
        filter.orderBy(query);

        if (filter.value == null){
            return filters.size() > 0 ? _buildWhereClause(query, filters) : null;
        }
        // Last field is uniq
        ConditionGroup conditionGroup = ConditionGroup.clause();

        if (filters.size() == 0) {
            conditionGroup.and(filter.strictCondition()); // FIELD STRICT_SIGN VALUE
        }
        else {
            ConditionGroup subClause = _buildWhereClause(query, filters);
            conditionGroup
                        .and(filter.strictCondition())                  // FIELD STRICT_SIGN VALUE
                        .or(ConditionGroup.clause()
                                .and(filter.equalsCondition())                  // FIELD EQUALS VALUE
                                .and(subClause));        // AND SUBQUERY)
        }
        return conditionGroup;
    }

    public void localLoad(){
        Log.i(TAG, "Loading from local db");
        Where<DataType> query = this.localBaseQuery;
        ConditionGroup condition = _buildWhereClause(query, (LinkedList<PaginateFilter>) this.filters.clone());
        if (condition != null){
            query.and(condition);
        }

        Log.d(TAG, "Generated query: " + query.getQuery());

        List<DataType> items = query.limit(this.cacheInfo.limit).queryList();
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

    private List<DataType> _parseItems(final List<JsonObject> items) {
        final List<DataType> result = new ArrayList<>(items.size());
        FlowManager.getDatabase(AppDatabase.class).executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                for (JsonObject item: items){
                    RemoteType remoteModel = CursorPaginateDataLoader.this.gson.fromJson(item, remoteClazz);
                    DataType model;
                    if (cacheCallback != null){
                        model = CursorPaginateDataLoader.this.cacheCallback.beforeSaveModel(remoteModel);
                    }
                    else{
                        model = (DataType) remoteModel;
                    }
                    if (CursorPaginateDataLoader.this.cacheEnabled){
                        Log.d(TAG, "Saving in local db: " + model);
                        try {
                            model.deepSave();
                        } catch (CannotSaveModelException e) {
                            Log.e(TAG, "Cannot save model: " + e.getMessage());
                            // TODO [critical] remove cache otherwise there will have missing value
                        }
                    }
                    result.add(model);
                }
            }
        });
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

    public interface FilterValueTransformer<T extends MyModel>{
        Object transform(T model);
    }
    public interface CacheCallback<LocalType extends MyModel, RemoteType extends MyModel>{
        LocalType beforeSaveModel(RemoteType model);
    }
}

