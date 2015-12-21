package com.timappweb.timapp.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;
import com.timappweb.timapp.entities.MapTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
public class ClustersRenderer implements ClusterRenderer<MapTag> {
    private static final boolean SHOULD_ANIMATE;
    private final GoogleMap mMap;
    private final IconGenerator mIconGenerator;
    private final ClusterManager<MapTag> mClusterManager;
    private final float mDensity;
    private static final int[] BUCKETS;
    private ShapeDrawable mColoredCircleBackground;
    private Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(new ConcurrentHashMap());
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray();
    private ClustersRenderer.MarkerCache<MapTag> mMarkerCache = new ClustersRenderer.MarkerCache();
    private static final int MIN_CLUSTER_SIZE = 4;
    private Set<? extends Cluster<MapTag>> mClusters;
    private Map<Marker, Cluster<MapTag>> mMarkerToCluster = new HashMap();
    private Map<Cluster<MapTag>, Marker> mClusterToMarker = new HashMap();
    private float mZoom;
    private final ClustersRenderer.ViewModifier mViewModifier = new ClustersRenderer.ViewModifier();
    private ClusterManager.OnClusterClickListener<MapTag> mClickListener;
    private ClusterManager.OnClusterInfoWindowClickListener<MapTag> mInfoWindowClickListener;
    private ClusterManager.OnClusterItemClickListener<MapTag> mItemClickListener;
    private ClusterManager.OnClusterItemInfoWindowClickListener<MapTag> mItemInfoWindowClickListener;
    private static final TimeInterpolator ANIMATION_INTERP;

    public ClustersRenderer(Context context, GoogleMap map, ClusterManager<MapTag> clusterManager) {
        this.mMap = map;
        this.mDensity = context.getResources().getDisplayMetrics().density;
        this.mIconGenerator = new IconGenerator(context);
        this.mIconGenerator.setContentView(this.makeSquareTextView(context));
        this.mIconGenerator.setTextAppearance(com.google.maps.android.R.style.ClusterIcon_TextAppearance);
        this.mIconGenerator.setBackground(this.makeClusterBackground());
        this.mClusterManager = clusterManager;
    }

    public void onAdd() {
        this.mClusterManager.getMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                return ClustersRenderer.this.mItemClickListener != null
                        && ClustersRenderer.this.mItemClickListener.onClusterItemClick(ClustersRenderer.this.mMarkerCache.get(marker));
            }
        });
        this.mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker marker) {
                if(ClustersRenderer.this.mItemInfoWindowClickListener != null) {
                    ClustersRenderer.this.mItemInfoWindowClickListener.onClusterItemInfoWindowClick(ClustersRenderer.this.mMarkerCache.get(marker));
                }

            }
        });
        this.mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                return ClustersRenderer.this.mClickListener != null && ClustersRenderer.this.mClickListener.onClusterClick(ClustersRenderer.this.mMarkerToCluster.get(marker));
            }
        });
        this.mClusterManager.getClusterMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker marker) {
                if (ClustersRenderer.this.mInfoWindowClickListener != null) {
                    ClustersRenderer.this.mInfoWindowClickListener.onClusterInfoWindowClick((Cluster) ClustersRenderer.this.mMarkerToCluster.get(marker));
                }

            }
        });
    }

    public void onRemove() {
        this.mClusterManager.getMarkerCollection().setOnMarkerClickListener((GoogleMap.OnMarkerClickListener)null);
        this.mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) null);
    }

    private LayerDrawable makeClusterBackground() {
        this.mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(23223232);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, this.mColoredCircleBackground});
        int strokeWidth = (int)(this.mDensity * 3.0F);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private SquareTextView makeSquareTextView(Context context) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -2);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(com.google.maps.android.R.id.text);
        int twelveDpi = (int)(12.0F * this.mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }

    private int getColor(int clusterSize) {
        float hueRange = 220.0F;
        float sizeRange = 300.0F;
        float size = Math.min((float)clusterSize, 300.0F);
        float hue = (300.0F - size) * (300.0F - size) / 90000.0F * 220.0F;
        return Color.HSVToColor(new float[]{hue, 1.0F, 0.6F});
    }

    protected String getClusterText(Cluster cluster) {
        int bucket = this.getBucket(cluster);
        MapTag mapTag = (MapTag) cluster.getItems().iterator().next();
        return mapTag.name + " ("+ mapTag.count_ref+")\n" + bucket;
    }

    protected int getBucket(Cluster<MapTag> cluster) {
        int size = cluster.getSize();
        if(size <= BUCKETS[0]) {
            return size;
        } else {
            for(int i = 0; i < BUCKETS.length - 1; ++i) {
                if(size < BUCKETS[i + 1]) {
                    return BUCKETS[i];
                }
            }

            return BUCKETS[BUCKETS.length - 1];
        }
    }

    protected boolean shouldRenderAsCluster(Cluster<MapTag> cluster) {
        return cluster.getSize() > 4;
    }

    public void onClustersChanged(Set<? extends Cluster<MapTag>> clusters) {
        this.mViewModifier.queue(clusters);
    }

    public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<MapTag> listener) {
        this.mClickListener = listener;
    }

    public void setOnClusterInfoWindowClickListener(ClusterManager.OnClusterInfoWindowClickListener<MapTag> listener) {
        this.mInfoWindowClickListener = listener;
    }

    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<MapTag> listener) {
        this.mItemClickListener = listener;
    }

    public void setOnClusterItemInfoWindowClickListener(ClusterManager.OnClusterItemInfoWindowClickListener<MapTag> listener) {
        this.mItemInfoWindowClickListener = listener;
    }

    private static double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private static Point findClosestCluster(List<Point> markers, Point point) {
        if(markers != null && !markers.isEmpty()) {
            double minDistSquared = 10000.0D;
            Point closest = null;
            Iterator i$ = markers.iterator();

            while(i$.hasNext()) {
                Point candidate = (Point)i$.next();
                double dist = distanceSquared(candidate, point);
                if(dist < minDistSquared) {
                    closest = candidate;
                    minDistSquared = dist;
                }
            }

            return closest;
        } else {
            return null;
        }
    }

    protected void onBeforeClusterItemRendered(MapTag item, MarkerOptions markerOptions) {
    }

    protected void onBeforeClusterRendered(Cluster<MapTag> cluster, MarkerOptions markerOptions) {
        int bucket = this.getBucket(cluster);
        BitmapDescriptor descriptor = (BitmapDescriptor)this.mIcons.get(bucket);
        if(descriptor == null) {
            this.mColoredCircleBackground.getPaint().setColor(this.getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(this.mIconGenerator.makeIcon(this.getClusterText(cluster)));
            this.mIcons.put(bucket, descriptor);
        }

        markerOptions.icon(descriptor);
    }

    protected void onClusterRendered(Cluster<MapTag> cluster, Marker marker) {
    }

    protected void onClusterItemRendered(MapTag clusterItem, Marker marker) {
    }

    public Marker getMarker(MapTag clusterItem) {
        return this.mMarkerCache.get(clusterItem);
    }

    public MapTag getClusterItem(Marker marker) {
        return this.mMarkerCache.get(marker);
    }

    public Marker getMarker(Cluster<MapTag> cluster) {
        return (Marker)this.mClusterToMarker.get(cluster);
    }

    public Cluster<MapTag> getCluster(Marker marker) {
        return (Cluster)this.mMarkerToCluster.get(marker);
    }

    static {
        SHOULD_ANIMATE = Build.VERSION.SDK_INT >= 11;
        BUCKETS = new int[]{10, 20, 50, 100, 200, 500, 1000};
        ANIMATION_INTERP = new DecelerateInterpolator();
    }

    @TargetApi(12)
    private class AnimationTask extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        private final ClustersRenderer.MarkerWithPosition markerWithPosition;
        private final Marker marker;
        private final LatLng from;
        private final LatLng to;
        private boolean mRemoveOnComplete;
        private MarkerManager mMarkerManager;

        private AnimationTask(ClustersRenderer.MarkerWithPosition markerWithPosition, LatLng from, LatLng to) {
            this.markerWithPosition = markerWithPosition;
            this.marker = markerWithPosition.marker;
            this.from = from;
            this.to = to;
        }

        public void perform() {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0F, 1.0F});
            valueAnimator.setInterpolator(ClustersRenderer.ANIMATION_INTERP);
            valueAnimator.addUpdateListener(this);
            valueAnimator.addListener(this);
            valueAnimator.start();
        }

        public void onAnimationEnd(Animator animation) {
            if(this.mRemoveOnComplete) {
                Cluster cluster = (Cluster)ClustersRenderer.this.mMarkerToCluster.get(this.marker);
                ClustersRenderer.this.mClusterToMarker.remove(cluster);
                ClustersRenderer.this.mMarkerCache.remove(this.marker);
                ClustersRenderer.this.mMarkerToCluster.remove(this.marker);
                this.mMarkerManager.remove(this.marker);
            }

            this.markerWithPosition.position = this.to;
        }

        public void removeOnAnimationComplete(MarkerManager markerManager) {
            this.mMarkerManager = markerManager;
            this.mRemoveOnComplete = true;
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float fraction = valueAnimator.getAnimatedFraction();
            double lat = (this.to.latitude - this.from.latitude) * (double)fraction + this.from.latitude;
            double lngDelta = this.to.longitude - this.from.longitude;
            if(Math.abs(lngDelta) > 180.0D) {
                lngDelta -= Math.signum(lngDelta) * 360.0D;
            }

            double lng = lngDelta * (double)fraction + this.from.longitude;
            LatLng position = new LatLng(lat, lng);
            this.marker.setPosition(position);
        }
    }

    private static class MarkerWithPosition {
        private final Marker marker;
        private LatLng position;

        private MarkerWithPosition(Marker marker) {
            this.marker = marker;
            this.position = marker.getPosition();
        }

        public boolean equals(Object other) {
            return other instanceof ClustersRenderer.MarkerWithPosition?this.marker.equals(((ClustersRenderer.MarkerWithPosition)other).marker):false;
        }

        public int hashCode() {
            return this.marker.hashCode();
        }
    }

    private class CreateMarkerTask {
        private final Cluster<MapTag> cluster;
        private final Set<MarkerWithPosition> newMarkers;
        private LatLng animateFrom = null;

        public CreateMarkerTask(Cluster<MapTag> var1, Set<ClustersRenderer.MarkerWithPosition> c, LatLng markersAdded) {
            this.cluster = (Cluster<MapTag>) var1;
            this.newMarkers = c;
            this.animateFrom = markersAdded;
        }

        private void perform(ClustersRenderer.MarkerModifier markerModifier) {
            if(!ClustersRenderer.this.shouldRenderAsCluster(this.cluster)) {
                Iterator markerOptions2 = this.cluster.getItems().iterator();

                while(markerOptions2.hasNext()) {
                    MapTag marker1 = (MapTag) markerOptions2.next();
                    Marker markerWithPosition2 = ClustersRenderer.this.mMarkerCache.get(marker1);
                    ClustersRenderer.MarkerWithPosition markerWithPosition1;
                    if(markerWithPosition2 == null) {
                        MarkerOptions markerOptions1 = new MarkerOptions();
                        if(this.animateFrom != null) {
                            markerOptions1.position(this.animateFrom);
                        } else {
                            markerOptions1.position(marker1.getPosition());
                        }

                        ClustersRenderer.this.onBeforeClusterItemRendered(marker1, markerOptions1);
                        markerWithPosition2 = ClustersRenderer.this.mClusterManager.getMarkerCollection().addMarker(markerOptions1);
                        markerWithPosition1 = new ClustersRenderer.MarkerWithPosition(markerWithPosition2);
                        ClustersRenderer.this.mMarkerCache.put(marker1, markerWithPosition2);
                        if(this.animateFrom != null) {
                            markerModifier.animate(markerWithPosition1, this.animateFrom, marker1.getPosition());
                        }
                    } else {
                        markerWithPosition1 = new ClustersRenderer.MarkerWithPosition(markerWithPosition2);
                    }

                    ClustersRenderer.this.onClusterItemRendered(marker1, markerWithPosition2);
                    this.newMarkers.add(markerWithPosition1);
                }

            } else {
                MarkerOptions markerOptions = (new MarkerOptions()).position(this.animateFrom == null?this.cluster.getPosition():this.animateFrom);
                ClustersRenderer.this.onBeforeClusterRendered(this.cluster, markerOptions);
                Marker marker = ClustersRenderer.this.mClusterManager.getClusterMarkerCollection().addMarker(markerOptions);
                ClustersRenderer.this.mMarkerToCluster.put(marker, this.cluster);
                ClustersRenderer.this.mClusterToMarker.put(this.cluster, marker);
                ClustersRenderer.MarkerWithPosition markerWithPosition = new ClustersRenderer.MarkerWithPosition(marker);
                if(this.animateFrom != null) {
                    markerModifier.animate(markerWithPosition, this.animateFrom, this.cluster.getPosition());
                }

                ClustersRenderer.this.onClusterRendered(this.cluster, marker);
                this.newMarkers.add(markerWithPosition);
            }
        }
    }

    private static class MarkerCache<T> {
        private Map<T, Marker> mCache;
        private Map<Marker, T> mCacheReverse;

        private MarkerCache() {
            this.mCache = new HashMap();
            this.mCacheReverse = new HashMap();
        }

        public Marker get(T item) {
            return (Marker)this.mCache.get(item);
        }

        public T get(Marker m) {
            return this.mCacheReverse.get(m);
        }

        public void put(T item, Marker m) {
            this.mCache.put(item, m);
            this.mCacheReverse.put(m, item);
        }

        public void remove(Marker m) {
            Object item = this.mCacheReverse.get(m);
            this.mCacheReverse.remove(m);
            this.mCache.remove(item);
        }
    }

    @SuppressLint({"HandlerLeak"})
    private class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;
        private final Lock lock;
        private final Condition busyCondition;
        private Queue<CreateMarkerTask> mCreateMarkerTasks;
        private Queue<ClustersRenderer.CreateMarkerTask> mOnScreenCreateMarkerTasks;
        private Queue<Marker> mRemoveMarkerTasks;
        private Queue<Marker> mOnScreenRemoveMarkerTasks;
        private Queue<ClustersRenderer.AnimationTask> mAnimationTasks;
        private boolean mListenerAdded;

        private MarkerModifier() {
            super(Looper.getMainLooper());
            this.lock = new ReentrantLock();
            this.busyCondition = this.lock.newCondition();
            this.mCreateMarkerTasks = new LinkedList();
            this.mOnScreenCreateMarkerTasks = new LinkedList();
            this.mRemoveMarkerTasks = new LinkedList();
            this.mOnScreenRemoveMarkerTasks = new LinkedList();
            this.mAnimationTasks = new LinkedList();
        }

        public void add(boolean priority, ClustersRenderer.CreateMarkerTask c) {
            this.lock.lock();
            this.sendEmptyMessage(0);
            if(priority) {
                this.mOnScreenCreateMarkerTasks.add(c);
            } else {
                this.mCreateMarkerTasks.add(c);
            }

            this.lock.unlock();
        }

        public void remove(boolean priority, Marker m) {
            this.lock.lock();
            this.sendEmptyMessage(0);
            if(priority) {
                this.mOnScreenRemoveMarkerTasks.add(m);
            } else {
                this.mRemoveMarkerTasks.add(m);
            }

            this.lock.unlock();
        }

        public void animate(ClustersRenderer.MarkerWithPosition marker, LatLng from, LatLng to) {
            this.lock.lock();
            this.mAnimationTasks.add(ClustersRenderer.this.new AnimationTask(marker, from, to));
            this.lock.unlock();
        }

        public void animateThenRemove(ClustersRenderer.MarkerWithPosition marker, LatLng from, LatLng to) {
            this.lock.lock();
            ClustersRenderer.AnimationTask animationTask = ClustersRenderer.this.new AnimationTask(marker, from, to);
            animationTask.removeOnAnimationComplete(ClustersRenderer.this.mClusterManager.getMarkerManager());
            this.mAnimationTasks.add(animationTask);
            this.lock.unlock();
        }

        public void handleMessage(Message msg) {
            if(!this.mListenerAdded) {
                Looper.myQueue().addIdleHandler(this);
                this.mListenerAdded = true;
            }

            this.removeMessages(0);
            this.lock.lock();

            try {
                int i = 0;

                while(true) {
                    if(i >= 10) {
                        if(!this.isBusy()) {
                            this.mListenerAdded = false;
                            Looper.myQueue().removeIdleHandler(this);
                            this.busyCondition.signalAll();
                        } else {
                            this.sendEmptyMessageDelayed(0, 10L);
                        }
                        break;
                    }

                    this.performNextTask();
                    ++i;
                }
            } finally {
                this.lock.unlock();
            }

        }

        private void performNextTask() {
            if(!this.mOnScreenRemoveMarkerTasks.isEmpty()) {
                this.removeMarker((Marker)this.mOnScreenRemoveMarkerTasks.poll());
            } else if(!this.mAnimationTasks.isEmpty()) {
                ((ClustersRenderer.AnimationTask)this.mAnimationTasks.poll()).perform();
            } else if(!this.mOnScreenCreateMarkerTasks.isEmpty()) {
                ((ClustersRenderer.CreateMarkerTask)this.mOnScreenCreateMarkerTasks.poll()).perform(this);
            } else if(!this.mCreateMarkerTasks.isEmpty()) {
                ((ClustersRenderer.CreateMarkerTask)this.mCreateMarkerTasks.poll()).perform(this);
            } else if(!this.mRemoveMarkerTasks.isEmpty()) {
                this.removeMarker((Marker)this.mRemoveMarkerTasks.poll());
            }

        }

        private void removeMarker(Marker m) {
            Cluster cluster = (Cluster)ClustersRenderer.this.mMarkerToCluster.get(m);
            ClustersRenderer.this.mClusterToMarker.remove(cluster);
            ClustersRenderer.this.mMarkerCache.remove(m);
            ClustersRenderer.this.mMarkerToCluster.remove(m);
            ClustersRenderer.this.mClusterManager.getMarkerManager().remove(m);
        }

        public boolean isBusy() {
            boolean var1;
            try {
                this.lock.lock();
                var1 = !this.mCreateMarkerTasks.isEmpty() || !this.mOnScreenCreateMarkerTasks.isEmpty() || !this.mOnScreenRemoveMarkerTasks.isEmpty() || !this.mRemoveMarkerTasks.isEmpty() || !this.mAnimationTasks.isEmpty();
            } finally {
                this.lock.unlock();
            }

            return var1;
        }

        public void waitUntilFree() {
            while(this.isBusy()) {
                this.sendEmptyMessage(0);
                this.lock.lock();

                try {
                    if(this.isBusy()) {
                        this.busyCondition.await();
                    }
                } catch (InterruptedException var5) {
                    throw new RuntimeException(var5);
                } finally {
                    this.lock.unlock();
                }
            }

        }

        public boolean queueIdle() {
            this.sendEmptyMessage(0);
            return true;
        }
    }

    private class RenderTask implements Runnable {
        final Set<? extends Cluster<MapTag>> clusters;
        private Runnable mCallback;
        private Projection mProjection;
        private SphericalMercatorProjection mSphericalMercatorProjection;
        private float mMapZoom;

        private RenderTask(Set<? extends Cluster<MapTag>> clusters) {
            this.clusters = clusters;
        }

        public void setCallback(Runnable callback) {
            this.mCallback = callback;
        }

        public void setProjection(Projection projection) {
            this.mProjection = projection;
        }

        public void setMapZoom(float zoom) {
            this.mMapZoom = zoom;
            this.mSphericalMercatorProjection = new SphericalMercatorProjection(256.0D * Math.pow(2.0D, (double)Math.min(zoom, ClustersRenderer.this.mZoom)));
        }

        @SuppressLint({"NewApi"})
        public void run() {
            if(this.clusters.equals(ClustersRenderer.this.mClusters)) {
                this.mCallback.run();
            } else {
                ClustersRenderer.MarkerModifier markerModifier = ClustersRenderer.this.new MarkerModifier();
                float zoom = this.mMapZoom;
                boolean zoomingIn = zoom > ClustersRenderer.this.mZoom;
                float zoomDelta = zoom - ClustersRenderer.this.mZoom;
                Set markersToRemove = ClustersRenderer.this.mMarkers;
                LatLngBounds visibleBounds = this.mProjection.getVisibleRegion().latLngBounds;
                ArrayList existingClustersOnScreen = null;
                if(ClustersRenderer.this.mClusters != null && ClustersRenderer.SHOULD_ANIMATE) {
                    existingClustersOnScreen = new ArrayList();
                    Iterator newMarkers = ClustersRenderer.this.mClusters.iterator();

                    while(newMarkers.hasNext()) {
                        Cluster newClustersOnScreen = (Cluster)newMarkers.next();
                        if(ClustersRenderer.this.shouldRenderAsCluster(newClustersOnScreen) && visibleBounds.contains(newClustersOnScreen.getPosition())) {
                            com.google.maps.android.projection.Point i$ = this.mSphericalMercatorProjection.toPoint(newClustersOnScreen.getPosition());
                            existingClustersOnScreen.add(i$);
                        }
                    }
                }

                Set newMarkers1 = Collections.newSetFromMap(new ConcurrentHashMap());
                Iterator newClustersOnScreen1 = this.clusters.iterator();

                while(true) {
                    com.google.maps.android.projection.Point onScreen;
                    while(newClustersOnScreen1.hasNext()) {
                        Cluster i$1 = (Cluster)newClustersOnScreen1.next();
                        boolean marker = visibleBounds.contains(i$1.getPosition());
                        if(zoomingIn && marker && ClustersRenderer.SHOULD_ANIMATE) {
                            onScreen = this.mSphericalMercatorProjection.toPoint(i$1.getPosition());
                            Point point = ClustersRenderer.findClosestCluster(existingClustersOnScreen, onScreen);
                            if(point != null) {
                                LatLng closest = this.mSphericalMercatorProjection.toLatLng(point);
                                markerModifier.add(true, ClustersRenderer.this.new CreateMarkerTask(i$1, newMarkers1, closest));
                            } else {
                                markerModifier.add(true, ClustersRenderer.this.new CreateMarkerTask(i$1, newMarkers1, (LatLng)null));
                            }
                        } else {
                            markerModifier.add(marker, ClustersRenderer.this.new CreateMarkerTask(i$1, newMarkers1, (LatLng)null));
                        }
                    }

                    markerModifier.waitUntilFree();
                    markersToRemove.removeAll(newMarkers1);
                    ArrayList newClustersOnScreen2 = null;
                    Iterator i$2;
                    if(ClustersRenderer.SHOULD_ANIMATE) {
                        newClustersOnScreen2 = new ArrayList();
                        i$2 = this.clusters.iterator();

                        while(i$2.hasNext()) {
                            Cluster marker1 = (Cluster)i$2.next();
                            if(ClustersRenderer.this.shouldRenderAsCluster(marker1) && visibleBounds.contains(marker1.getPosition())) {
                                onScreen = this.mSphericalMercatorProjection.toPoint(marker1.getPosition());
                                newClustersOnScreen2.add(onScreen);
                            }
                        }
                    }

                    i$2 = markersToRemove.iterator();

                    while(true) {
                        while(i$2.hasNext()) {
                            ClustersRenderer.MarkerWithPosition marker2 = (ClustersRenderer.MarkerWithPosition)i$2.next();
                            boolean onScreen1 = visibleBounds.contains(marker2.position);
                            if(!zoomingIn && zoomDelta > -3.0F && onScreen1 && ClustersRenderer.SHOULD_ANIMATE) {
                                com.google.maps.android.projection.Point point1 = this.mSphericalMercatorProjection.toPoint(marker2.position);
                                Point closest1 = ClustersRenderer.findClosestCluster(newClustersOnScreen2, point1);
                                if(closest1 != null) {
                                    LatLng animateTo = this.mSphericalMercatorProjection.toLatLng(closest1);
                                    markerModifier.animateThenRemove(marker2, marker2.position, animateTo);
                                } else {
                                    markerModifier.remove(true, marker2.marker);
                                }
                            } else {
                                markerModifier.remove(onScreen1, marker2.marker);
                            }
                        }

                        markerModifier.waitUntilFree();
                        ClustersRenderer.this.mMarkers = newMarkers1;
                        ClustersRenderer.this.mClusters = this.clusters;
                        ClustersRenderer.this.mZoom = zoom;
                        this.mCallback.run();
                        return;
                    }
                }
            }
        }
    }

    @SuppressLint({"HandlerLeak"})
    private class ViewModifier extends Handler {
        private static final int RUN_TASK = 0;
        private static final int TASK_FINISHED = 1;
        private boolean mViewModificationInProgress;
        private ClustersRenderer.RenderTask mNextClusters;

        private ViewModifier() {
            this.mViewModificationInProgress = false;
            this.mNextClusters = null;
        }

        public void handleMessage(Message msg) {
            if(msg.what == 1) {
                this.mViewModificationInProgress = false;
                if(this.mNextClusters != null) {
                    this.sendEmptyMessage(0);
                }

            } else {
                this.removeMessages(0);
                if(!this.mViewModificationInProgress) {
                    if(this.mNextClusters != null) {
                        ClustersRenderer.RenderTask renderTask;
                        synchronized(this) {
                            renderTask = this.mNextClusters;
                            this.mNextClusters = null;
                            this.mViewModificationInProgress = true;
                        }

                        renderTask.setCallback(new Runnable() {
                            public void run() {
                                ViewModifier.this.sendEmptyMessage(1);
                            }
                        });
                        renderTask.setProjection(ClustersRenderer.this.mMap.getProjection());
                        renderTask.setMapZoom(ClustersRenderer.this.mMap.getCameraPosition().zoom);
                        (new Thread(renderTask)).start();
                    }
                }
            }
        }

        public void queue(Set<? extends Cluster<MapTag>> clusters) {
            synchronized(this) {
                this.mNextClusters = ClustersRenderer.this.new RenderTask(clusters);
            }

            this.sendEmptyMessage(0);
        }
    }
}



/**
 * Created by stephane on 8/30/2015.
 */
//public class ClustersRenderer extends ClustersRenderer<MapTag> {
//
//
//    private static final int[] BUCKETS;
//
//    static {
//        BUCKETS = new int[]{10, 20, 50, 100, 200, 500, 1000};
//    }
//
//    private static final String TAG =  "ClustersRenderer";
//    private final Context context;
//    private final IconGenerator mClusterIconGenerator;
//
//    public ClustersRenderer(Context context, GoogleMap map, ClusterManager<MapTag> clusterManager) {
//        super(context, map, clusterManager);
//
//        this.context = context;
//        mClusterIconGenerator = new IconGenerator(context);
//    }
//
//    @Override
//    protected void onBeforeClusterItemRendered(MapTag spotsTag, MarkerOptions markerOptions) {
//        Log.d(TAG, "Rendering cluster item");
//
//        IconGenerator item = new IconGenerator(context);
//        Bitmap iconBitmap = item.makeIcon(spotsTag.name);
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).anchor(0.3f, 0.5f);
//
//        //mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).anchor(0.5f, 0.6f));
//    /*
//        mImageView.setImageResource(person.profilePhoto);
//        Bitmap icon = mIconGenerator.makeIcon();
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).anchor(0.5f, 0.6f);
//        */
//    }
//
//    protected String getClusterText(Cluster cluster) {
//        int bucket = this.getBucket(cluster);
//        MapTag spotsTag = (MapTag) cluster.getItems().iterator().next();
//        String number = bucket < BUCKETS[0] ? String.valueOf(bucket) : bucket + "+";
//        return spotsTag.name + "  " + number;
//    }
//
//    /**
//     * https://github.com/googlemaps/android-maps-utils/blob/master/demo/src/com/google/maps/android/utils/demo/CustomMarkerClusteringDemoActivity.java
//     * Called before the marker for a Cluster is added to the map.
//     * The default implementation draws a circle with a rough count of the number of items.
//     */
//    @Override
//    protected void onBeforeClusterRendered(Cluster cluster, MarkerOptions markerOptions) {
//        int bucket = this.getBucket(cluster);
//        BitmapDescriptor descriptor = (BitmapDescriptor)this.mIcons.get(bucket);
//        if(descriptor == null) {
//            super.mColoredCircleBackground.getPaint().setColor(this.getColor(bucket));
//            descriptor = BitmapDescriptorFactory.fromBitmap(mClusterIconGenerator.makeIcon(this.getClusterText(bucket)));
//            this.mIcons.put(bucket, descriptor);
//        }
//
//        markerOptions.icon(descriptor);
//
//        super.onBeforeClusterRendered(cluster, markerOptions);
//        // Draw multiple people.
//        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
//
//
//        Bitmap icon = mClusterIconGenerator.makeIcon(this.getClusterText(cluster));
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
//    }
//
//    @Override
//    protected boolean shouldRenderAsCluster(Cluster cluster) {
//        // Always render clusters.
//        return cluster.getSize() > 1;
//    }
//
//}
