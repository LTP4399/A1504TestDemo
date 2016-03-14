package com.litangping.haibei.utils.fragment;

import android.os.Bundle;

/**
 * Created by LiTangping on 2016/3/5.
 */
public interface FragmentChangeListener {
    void fragmentChange(Class<? extends BaseFragment> fragment,Class<? extends BaseFragment> beforeFragment,Bundle data);
    void backFragment(Class<? extends BaseFragment> fragment);
}
