package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailTabActivity extends BaseTabActivity {

    //TODO - it is out of sync here. the logic should be in TabModel.
    private final int coursewareTabIndex = 0;
    private final int courseInfoTabIndex = 1;

    public static String TAG = CourseDetailTabActivity.class.getCanonicalName();
    static final String TAB_ID = TAG + ".tabID";

    private View offlineBar;

    private int selectedTab = coursewareTabIndex;

    Bundle bundle;
    String activityTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coursedetail_tab);

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(TAB_ID);
        }

        setApplyPrevTransitionOnRestart(true);
        
        bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        offlineBar = findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            if(offlineBar!=null){
                offlineBar.setVisibility(View.VISIBLE);
            }
        }

        try{
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(Router.EXTRA_ENROLLMENT);

            if ( courseData == null ){
                //this is from notification
                String courseId = bundle.getString(Router.EXTRA_COURSE_ID);
                if (!TextUtils.isEmpty(courseId)){
                    SegmentFactory.getInstance().trackNotificationTapped(courseId);

                    Api api = new Api(this);
                    courseData = api.getCourseById(courseId);
                    if (courseData != null && courseData.getCourse() != null ) {
                        bundle.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
                        selectedTab = courseInfoTabIndex;
                    }
                }

            }
            //check courseData again, it may be fetched from local cache
            if ( courseData != null ) {
                activityTitle = courseData.getCourse().getName();
                try{
                    segIO.screenViewsTracking(courseData.getCourse().getName());
                }catch(Exception e){
                    logger.error(e);
                }
            } else {
                //it is a good idea to go to the my course page. as loading of my courses
                //take a while to load. that the only way to get anouncement link
                Router.getInstance().showMyCourses(this);
                finish();
            }

        }catch(Exception ex){
            logger.error(ex);
        }

    }

    @Override
    protected List<TabModel> tabsToAdd() {
        List<TabModel> tabs = new ArrayList<TabModel>();
        tabs.add(new TabModel(getString(R.string.tab_label_courseware),
                CourseChapterListFragment.class,
                bundle, getString(R.string.tab_chapter_list)));
        tabs.add(new TabModel(getString(R.string.tab_label_course_info),
                CourseCombinedInfoFragment.class,
                bundle, getString(R.string.tab_course_info)));

        return tabs;
    }

    @Override
    protected int getDefaultTab() {
        return selectedTab;
    }

    protected void onStart() {
        super.onStart();
        try{
            setTitle(activityTitle);
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOffline();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOnline();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAB_ID, tabHost.getCurrentTab());
    }
}
