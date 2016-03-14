package com.litangping.haibei.utils.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiTangping on 2016/3/5.
 */
public class FragmentFactory {

    private Map<Class<? extends BaseFragment>, BaseFragment> fragmentMap;

    private static FragmentFactory fragmentFactoryInstance = null;
    private FragmentChangeListener listener;

    private FragmentFactory(FragmentChangeListener listener) {
        this.listener = listener;
        fragmentMap = new HashMap<>();
    }

    public synchronized static FragmentFactory getInstance(FragmentChangeListener listener) {
        if (fragmentFactoryInstance == null) {
            fragmentFactoryInstance = new FragmentFactory(listener);
        }
        return fragmentFactoryInstance;
    }

    /**
     * 获取一个BaseFragment的实例
     * @param fragment
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public BaseFragment getFragment(Class<? extends BaseFragment> fragment) {
        BaseFragment baseFragment;
        if (!fragmentMap.containsKey(fragment)) {
            try {
                baseFragment = fragment.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("在Fragment工厂中产生"+fragment.getSimpleName()+"实例时出错");
            }
        }else{
            baseFragment = fragmentMap.get(fragment);
            if(baseFragment == null){
                fragmentMap.remove(fragment);
                baseFragment = getFragment(fragment);
            }else{
                return baseFragment;
            }
        }
        if(baseFragment == null){
            throw new RuntimeException("在Fragment工厂中产生"+fragment.getSimpleName()+"实例时出错");
        }
        baseFragment.setListener(listener);
        fragmentMap.put(fragment,baseFragment);
        return baseFragment;
    }

}
