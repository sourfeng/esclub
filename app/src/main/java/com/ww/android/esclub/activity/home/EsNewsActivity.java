package com.ww.android.esclub.activity.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;

import com.ww.android.esclub.R;
import com.ww.android.esclub.activity.base.BaseActivity;
import com.ww.android.esclub.adapter.TranslateTabAdapter;
import com.ww.android.esclub.bean.home.NewsItem;
import com.ww.android.esclub.config.Constant;
import com.ww.android.esclub.fragment.home.CommentFragment;
import com.ww.android.esclub.fragment.home.EsNewsFragment;
import com.ww.android.esclub.widget.TranslateTabBar;
import com.ww.mvp.model.VoidModel;
import com.ww.mvp.view.VoidView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by feng on 2017/6/7.
 * Es新闻页面
 */

public class EsNewsActivity extends BaseActivity<VoidView,VoidModel> {


    @BindView(R.id.translate_tab_home)
    TranslateTabBar translateTabBar;
    @BindView(R.id.viewpager_tab_home)
    ViewPager viewPager;

    private String contentUrl;
    private List<Fragment> fragments;
    private FragmentManager fragmentManager;
    private TranslateTabAdapter adapter;
    private NewsItem item;
    private String type;

    public static void start(Context context, NewsItem item,String type) {
        Intent intent = new Intent(context, EsNewsActivity.class);
        intent.putExtra("newsItem", item);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_esnews;
    }

    @Override
    public void onTitleLeft() {
        super.onTitleLeft();
        onBackPressed();
    }

    @Override
    public void onTitleRight() {
        super.onTitleRight();
        ShareActivity.start(this,item);
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        item = (NewsItem) getIntent().getSerializableExtra("newsItem");
        type = getIntent().getStringExtra("type");

        translateTabBar.setOnTabChangeListener(new TranslateTabBar.OnTabChangeListener() {
            @Override
            public void onChange(int index) {
                viewPager.setCurrentItem(index);
            }
        });
//
        fragmentManager = getSupportFragmentManager();
        addFragment();
        adapter = new TranslateTabAdapter(fragmentManager, fragments);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        viewPager.setOnPageChangeListener(pageChangeListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentEvent(String comment){
        if (!TextUtils.isEmpty(comment) &&TextUtils.equals(Constant.COMMENT_SUCCESS,comment)){
            translateTabBar.setCurrentIndex(1);
        }
    }

    private void addFragment(){
        if (fragments==null){
            fragments = new ArrayList<>();
        }
        fragments.add(createFragment(new EsNewsFragment(),item,type));
        fragments.add(createFragment(new CommentFragment(),item,type));
    }

    private Fragment createFragment(Fragment fragment,NewsItem item,String type){
        Bundle bundle = new Bundle();
        bundle.putSerializable("newsItem",item);
        bundle.putString("type",type);
        fragment.setArguments(bundle);
        return fragment;
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int
                positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            translateTabBar.setCurrentIndex(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
