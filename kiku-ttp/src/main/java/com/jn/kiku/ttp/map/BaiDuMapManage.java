package com.jn.kiku.ttp.map;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.jn.kiku.ttp.common.ILogToastView;
import com.jn.kiku.ttp.map.callback.BDLocationListenerIml;
import com.jn.kiku.ttp.map.callback.LocationResultListener;
import com.jn.kiku.ttp.map.callback.RouteSearchResultListener;
import com.jn.kiku.ttp.map.overlayutil.BikingRouteOverlay;
import com.jn.kiku.ttp.map.overlayutil.DrivingRouteOverlay;
import com.jn.kiku.ttp.map.overlayutil.TransitRouteOverlay;
import com.jn.kiku.ttp.map.overlayutil.WalkingRouteOverlay;

import java.util.List;

/**
 * @version V1.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (百度地图管理类)
 * @create by: chenwei
 * @date 2017/3/13 13:27
 */
public class BaiDuMapManage implements ILogToastView {

    private static BaiDuMapManage instance = null;

    private LocationClient mLocationClient = null;//定位关键类
    private LocationClient mServiceLocationClient = null;//定位关键类
    private RoutePlanSearch pSearch = null;//搜索关键类

    private RouteSearchResultListener mRouteSearchResultListener = null;

    private BaiDuMapManage() {
    }

    public static synchronized BaiDuMapManage getInstance() {
        if (instance == null)
            instance = new BaiDuMapManage();
        return instance;
    }

    /**
     * 初始化定位相关类和参数
     *
     * @param context Context
     */
    private void initLoc(Context context, final LocationResultListener listener) {
        if (mLocationClient == null)
            mLocationClient = new LocationClient(context);
        final BDLocationListenerIml mBDLocationListener = new BDLocationListenerIml();
        mBDLocationListener.setLocationResultListener(new LocationResultListener() {
            @Override
            public void onSuccess(BDLocation location) {
                if (listener != null)
                    listener.onSuccess(location);
                if (mLocationClient != null)
                    mLocationClient.unRegisterLocationListener(mBDLocationListener);
            }

            @Override
            public void onFailure(BDLocation location) {
                if (listener != null)
                    listener.onFailure(location);
                if (mLocationClient != null)
                    mLocationClient.unRegisterLocationListener(mBDLocationListener);
            }
        });
        mLocationClient.registerLocationListener(mBDLocationListener);
        //定位参数
        LocationClientOption mOption = getLocationClientOption(0);
        mLocationClient.setLocOption(mOption);
    }

    /**
     * 初始化定位相关类和参数
     *
     * @param context
     * @param scanSpanTime
     * @param listener
     */
    private void initLoc(Context context, final int scanSpanTime, final LocationResultListener listener) {
        if (mServiceLocationClient == null)
            mServiceLocationClient = new LocationClient(context);
        final BDLocationListenerIml mBDLocationListener = new BDLocationListenerIml();
        mBDLocationListener.setLocationResultListener(listener);
        mServiceLocationClient.registerLocationListener(mBDLocationListener);
        //定位参数
        LocationClientOption mOption = getLocationClientOption(scanSpanTime);
        mServiceLocationClient.setLocOption(mOption);
    }

    /**
     * 获取定位参数
     *
     * @param scanSpanTime 设置发起定位请求的间隔，需要大于等于1000ms才是有效的
     * @return LocationClientOption
     */
    private LocationClientOption getLocationClientOption(int scanSpanTime) {
        LocationClientOption mOption = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系
        mOption.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setScanSpan(scanSpanTime);
        //可选，设置是否需要地址信息，默认不需要
        mOption.setIsNeedAddress(true);
        //可选，默认false,设置是否使用gps
        mOption.setOpenGps(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        mOption.setLocationNotify(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        mOption.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mOption.setIsNeedLocationPoiList(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        mOption.setIgnoreKillProcess(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        mOption.SetIgnoreCacheException(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mOption.setEnableSimulateGps(false);
        return mOption;
    }

    /**
     * 开始定位
     *
     * @param context  Context
     * @param listener 定位结果
     */
    public void startLoc(Context context, @NonNull LocationResultListener listener) {
        initLoc(context, listener);
        startLoc();
    }

    /**
     * 开始定位
     *
     * @param context      Context
     * @param scanSpanTime 定位间隔时间点，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
     * @param listener     定位结果
     */
    public void startLoc(Context context, int scanSpanTime, @NonNull LocationResultListener listener) {
        initLoc(context, scanSpanTime, listener);
        startServiceLoc();
    }

    /**
     * 开始定位
     */
    private void startLoc() {
        if (mLocationClient != null)
            mLocationClient.start();
    }

    /**
     * 停止定位
     */
    public void stopLoc() {
        if (mLocationClient != null)
            mLocationClient.stop();
    }

    /**
     * 开始定位
     */
    private void startServiceLoc() {
        if (mServiceLocationClient != null)
            mServiceLocationClient.start();
    }

    /**
     * 停止定位
     */
    public void stopServiceLoc() {
        if (mServiceLocationClient != null)
            mServiceLocationClient.stop();
    }

    /**
     * 设置地图类型
     *
     * @param baiduMap
     */
    public void setMapType(BaiduMap baiduMap) {
        //普通地图
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启交通图
        baiduMap.setTrafficEnabled(true);
    }

    /**
     * 更新定位图层
     *
     * @param baiduMap
     * @param location
     */
    public void updateLocationMapStatus(@NonNull BaiduMap baiduMap, @NonNull BDLocation location) {
        LatLng locLng = new LatLng(location.getLatitude(), location.getLongitude());
        MyLocationData myLocationData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        baiduMap.setMyLocationData(myLocationData);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(locLng));
    }

    /**
     * 更新地图状态
     *
     * @param baiduMap BaiduMap
     * @param points   地图点集合
     */
    public void animateMapStatus(@NonNull BaiduMap baiduMap, List<LatLng> points) {
        if (null == points || points.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        MapStatusUpdate msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build());
        baiduMap.animateMapStatus(msUpdate);
    }

    /**
     * 更新地图状态
     *
     * @param baiduMap BaiduMap
     * @param point    地图点
     * @param zoom     级别
     */
    public void animateMapStatus(@NonNull BaiduMap baiduMap, LatLng point, float zoom) {
        MapStatus.Builder builder = new MapStatus.Builder();
        MapStatus mapStatus = builder.target(point).zoom(zoom).build();
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    /**
     * 刷新地图
     *
     * @param baiduMap BaiduMap
     */
    public void refresh(@NonNull BaiduMap baiduMap) {
        LatLng mapCenter = baiduMap.getMapStatus().target;
        float mapZoom = baiduMap.getMapStatus().zoom - 1.0f;
        animateMapStatus(baiduMap, mapCenter, mapZoom);
    }

    /**
     * 获取两个位置之间的距离
     *
     * @param from 开始位置
     * @param to   结束位置
     * @return 距离(单位 : m)
     */
    public double getDistance(@NonNull LatLng from, @NonNull LatLng to) {
        return DistanceUtil.getDistance(from, to);
    }

    /**
     * 添加点标记
     *
     * @param baiduMap      BaiduMap
     * @param latLng        标记的点
     * @param imgResourceId 对应点显示的图标
     */
    public void addMarker(BaiduMap baiduMap, LatLng latLng, @DrawableRes int imgResourceId) {
        if (latLng != null) {
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(imgResourceId);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(latLng)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            baiduMap.addOverlay(option);
        }

    }

    /**
     * 绘制折线
     *
     * @param baiduMap BaiduMap
     * @param latLngs  点集合
     */
    public void addOverlay(BaiduMap baiduMap, List<LatLng> latLngs) {
        if (latLngs != null && latLngs.size() >= 2) {
            OverlayOptions overlayOptions = new PolylineOptions()
                    .width(20)
                    .color(0xAA13C768)
                    .points(latLngs);
            Polyline polyline = (Polyline) baiduMap.addOverlay(overlayOptions);
            polyline.setZIndex(3);
        }
    }

    /**
     * 初始化路线搜索
     *
     * @param listener
     */
    private void initRouteSearch(RouteSearchResultListener listener) {
        mRouteSearchResultListener = listener;
        if (pSearch == null)
            pSearch = RoutePlanSearch.newInstance();
        pSearch.setOnGetRoutePlanResultListener(new MyOnGetRoutePlanResultListener());
    }

    /**
     * 驾车路线搜索
     *
     * @param currentCity 当前城市名
     * @param from        开始位置
     * @param to          结束位置
     * @param policy      策略(默认：最少时间)
     */
    public void drivingSearch(@NonNull String currentCity, @NonNull LatLng from, @NonNull LatLng to, @Nullable DrivingRoutePlanOption.DrivingPolicy policy, RouteSearchResultListener listener) {
        try {
            initRouteSearch(listener);
            PlanNode fromNode = PlanNode.withLocation(from);
            PlanNode toNode = PlanNode.withLocation(to);
            DrivingRoutePlanOption.DrivingPolicy drivingPolicy;
            if (policy == null)
                drivingPolicy = DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST;
            else
                drivingPolicy = policy;
            if (pSearch != null)
                pSearch.drivingSearch(new DrivingRoutePlanOption().currentCity(currentCity).from(fromNode).to(toNode).policy(drivingPolicy));
        } catch (Exception e) {
            e.printStackTrace();
            if (mRouteSearchResultListener != null)
                mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.DRIVING);
        }
    }

    /**
     * 公交路线搜索
     *
     * @param currentCity 当前城市名
     * @param from        开始位置
     * @param to          结束位置
     * @param policy      策略(默认：时间优先)
     */
    public void transitSearch(@NonNull String currentCity, @NonNull LatLng from, @NonNull LatLng to, @Nullable TransitRoutePlanOption.TransitPolicy policy, RouteSearchResultListener listener) {
        try {
            initRouteSearch(listener);
            PlanNode fromNode = PlanNode.withLocation(from);
            PlanNode toNode = PlanNode.withLocation(to);
            TransitRoutePlanOption.TransitPolicy transitPolicy;
            if (policy == null)
                transitPolicy = TransitRoutePlanOption.TransitPolicy.EBUS_TIME_FIRST;
            else
                transitPolicy = policy;
            if (pSearch != null)
                pSearch.transitSearch(new TransitRoutePlanOption().city(currentCity).from(fromNode).to(toNode).policy(transitPolicy));
        } catch (Exception e) {
            e.printStackTrace();
            if (mRouteSearchResultListener != null)
                mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.TRANSIT);
        }
    }

    /**
     * 骑行路线搜索
     *
     * @param from
     * @param to
     */
    public void bikingSearch(@NonNull LatLng from, @NonNull LatLng to, RouteSearchResultListener listener) {
        try {
            initRouteSearch(listener);
            PlanNode fromNode = PlanNode.withLocation(from);
            PlanNode toNode = PlanNode.withLocation(to);
            if (pSearch != null)
                pSearch.bikingSearch(new BikingRoutePlanOption().from(fromNode).to(toNode));
        } catch (Exception e) {
            e.printStackTrace();
            if (mRouteSearchResultListener != null)
                mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.BIKING);
        }

    }

    /**
     * 步行路线搜索
     *
     * @param from
     * @param to
     */
    public void walkingSearch(@NonNull LatLng from, @NonNull LatLng to, RouteSearchResultListener listener) {
        try {
            initRouteSearch(listener);
            PlanNode fromNode = PlanNode.withLocation(from);
            PlanNode toNode = PlanNode.withLocation(to);
            if (pSearch != null)
                pSearch.walkingSearch(new WalkingRoutePlanOption().from(fromNode).to(toNode));
        } catch (Exception e) {
            e.printStackTrace();
            if (mRouteSearchResultListener != null)
                mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.WALKING);
        }
    }

    public void updateDrivingSearchMapStatus(BaiduMap baiduMap, List<DrivingRouteLine> wrLines) {
        baiduMap.clear();
        //创建驾车路线规划线路覆盖物
        DrivingRouteOverlay overlay = new DrivingRouteOverlay(baiduMap);
        //设置驾车路线规划数据
        overlay.setData(wrLines.get(0));
        //将驾车路线规划覆盖物添加到地图中
        overlay.addToMap();
        overlay.zoomToSpan();
    }

    public void updateTransitSearchMapStatus(BaiduMap baiduMap, List<TransitRouteLine> wrLines) {
        baiduMap.clear();
        //创建公交路线规划线路覆盖物
        TransitRouteOverlay overlay = new TransitRouteOverlay(baiduMap);
        //设置公交路线规划数据
        overlay.setData(wrLines.get(0));
        //将公交路线规划覆盖物添加到地图中
        overlay.addToMap();
        overlay.zoomToSpan();
    }

    public void updateBikingSearchMapStatus(BaiduMap baiduMap, List<BikingRouteLine> wrLines) {
        baiduMap.clear();
        //创建骑行路线规划线路覆盖物
        BikingRouteOverlay overlay = new BikingRouteOverlay(baiduMap);
        //设置骑行路线规划数据
        overlay.setData(wrLines.get(0));
        //将骑行路线规划覆盖物添加到地图中
        overlay.addToMap();
        overlay.zoomToSpan();
    }

    public void updateWalkingSearchMapStatus(BaiduMap baiduMap, List<WalkingRouteLine> wrLines) {
        baiduMap.clear();
        //创建步行路线规划线路覆盖物
        WalkingRouteOverlay overlay = new WalkingRouteOverlay(baiduMap);
        //设置步行路线规划数据
        overlay.setData(wrLines.get(0));
        //将步行路线规划覆盖物添加到地图中
        overlay.addToMap();
        overlay.zoomToSpan();
    }

    private class MyOnGetRoutePlanResultListener implements OnGetRoutePlanResultListener {

        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                //未找到结果
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.WALKING);
                logE("步行路线获取失败");
                return;
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                //walkingRouteResult.getSuggestAddrInfo()
                logE("步行起终点或途经点地址有岐义");
                return;
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                //获取步行线路规划结果
                List<WalkingRouteLine> wrLines = walkingRouteResult.getRouteLines();
                if (null == wrLines) return;
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onWalkingSuccess(wrLines);
                logI("步行路线获取成功");
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                //未找到结果
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.TRANSIT);
                logE("公交路线获取失败");
                return;
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                //transitRouteResult.getSuggestAddrInfo()
                logE("公交起终点或途经点地址有岐义");
                return;
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                //获取公交换乘路径规划结果
                List<TransitRouteLine> wrLines = transitRouteResult.getRouteLines();
                if (null == wrLines) return;
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onTransitSuccess(wrLines);
                logI("公交路线获取成功");
            }
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                //未找到结果
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.DRIVING);
                logE("驾车路线获取失败");
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                //drivingRouteResult.getSuggestAddrInfo()
                logE("驾车起终点或途经点地址有岐义");
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                //获取驾车线路规划结果
                List<DrivingRouteLine> wrLines = drivingRouteResult.getRouteLines();
                if (null == wrLines) return;
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onDrivingSuccess(wrLines);
                logI("驾车路线获取成功");
            }
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                //未找到结果
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onFailure(RouteSearchResultListener.Type.BIKING);
                logE("骑行路线获取失败");
                return;
            }
            if (bikingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                //bikingRouteResult.getSuggestAddrInfo()
                logE("骑行起终点或途经点地址有岐义");
                return;
            }
            if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                List<BikingRouteLine> wrLines = bikingRouteResult.getRouteLines();
                if (null == wrLines) return;
                if (mRouteSearchResultListener != null)
                    mRouteSearchResultListener.onBikingSuccess(wrLines);
                logI("骑行路线获取成功");
            }
        }
    }

    public void onResume(@NonNull MapView mapView) {
        mapView.onResume();
    }

    public void onPause(@NonNull MapView mapView) {
        mapView.onPause();
    }

    public void onDestroy(@Nullable MapView mapView) {
        if (mapView != null)
            mapView.onDestroy();
    }

    public void onDestroy() {
        if (mLocationClient != null)
            mLocationClient = null;
        if (pSearch != null) {
            pSearch.destroy();
            pSearch = null;
        }
    }

    public void onDestroyService() {
        if (mServiceLocationClient != null)
            mServiceLocationClient = null;
        if (pSearch != null) {
            pSearch.destroy();
            pSearch = null;
        }
    }

    @Override
    public void logI(String message) {
        Log.i(getClass().getSimpleName(), String.format(messageFormat, getClass().getSimpleName(), message));
    }

    @Override
    public void logE(String message) {
        Log.e(getClass().getSimpleName(), String.format(messageFormat, getClass().getSimpleName(), message));
    }

    @Override
    public void showToast(String message) {

    }

    @Override
    public void showToast(String message, int duration) {

    }
}
