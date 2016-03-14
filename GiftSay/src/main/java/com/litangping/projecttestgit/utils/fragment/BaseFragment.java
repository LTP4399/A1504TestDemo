package com.litangping.haibei.utils.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by LiTangping on 2016/3/5.
 */
public abstract class BaseFragment extends Fragment{
    public static final int REQUEST_TYPE_ONCE = 0x1;

    private FragmentChangeListener listener;
    private Bundle bundle;
    private Class<? extends BaseFragment> beforeFragment;
    private boolean isReShow = false;
    public void setListener(FragmentChangeListener listener) {
        this.listener = listener;
    }

    public void requestFragment(Class<? extends BaseFragment> fragment,Class<? extends BaseFragment> beforeFragment,Bundle bundle){
        listener.fragmentChange(fragment,beforeFragment, bundle);
    }

    public void onDataResponse(Bundle data){
        if (data==null){
            return;
        }
        this.setBundle(data);
        if(isReShow) {
            onRefrashData(data);
        }
        isReShow = true;
    }

    /**
     * 此方法可以获取到被请求启动时传入的Bundle，与initData(Bundle bundle)中的参数相同。
     * @return
     */
    protected final Bundle getBundle() {
        return bundle;
    }

    private void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = createRootView(inflater, container, savedInstanceState);
        initView(rootView);
        initData(bundle);
        return rootView;
    }

    /**
     * 设置单次回退界面
     * @param beforeFragment
     */
    public void setBeforeFragment(Class<? extends BaseFragment> beforeFragment){
        if(beforeFragment !=null && this.beforeFragment !=null){
            throw new IllegalStateException("该"+this.getClass().getSimpleName()+"（id="+this.getId()+"）尚在回退栈中，未被释放！");
        }else{
//            Toast.makeText(getActivity(), ""+beforeFragment+"==============="+this.beforeFragment, Toast.LENGTH_SHORT).show();
            this.beforeFragment = beforeFragment;
        }
    }

    /**
     * 获取单次返回的界面
     * @return
     */
    private Class<? extends BaseFragment> getBeforeFragment(){
        return beforeFragment;
    }

    /**
     * 在此处初始化数据，此过程执行在initView(View rootView)之后。
     * @param bundle 由其他Fragment（需继承BaseFragment）要启动该Fragment时传入，中途会经过Fragment工厂
     *               的监听器，可能会对其进行读取或修改。
     */
    protected abstract void initData(Bundle bundle);

    /**
     * 在此处初始化数据，此过程执行在createRootView之后。
     * @param rootView 根视图，可以使用rootView.findViewById方法查找其子控件
     */
    protected abstract void initView(View rootView);

    /**
     * 在此处初始化默认视图并返回 ，此方法与onCreateView相同。
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    protected abstract View createRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected void onRefrashData(Bundle data){

    }

    /**
     * 返回true则拦截返回按钮的事件
     * 返回false则处理完返回按钮事件后传递给activity；
     * @return
     */
    public boolean backPressed(){
        if (beforeFragment == null){
            return false;
        }
        listener.backFragment(beforeFragment);
        beforeFragment=null;
        return true;
    }
}
