package com.seition.cloud.pro.newcloud.home.mvp.ui.owner.exchange.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jess.arms.base.BaseBackFragment;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import com.seition.cloud.pro.newcloud.R;
import com.seition.cloud.pro.newcloud.home.di.component.DaggerUserComponent;
import com.seition.cloud.pro.newcloud.home.di.module.UserModule;
import com.seition.cloud.pro.newcloud.home.mvp.contract.UserContract;
import com.seition.cloud.pro.newcloud.home.mvp.presenter.UserExchangeListPresenter;
import com.seition.cloud.pro.newcloud.home.mvp.ui.public_adapter.UserExchangeRecordRecyclerAdapter;

import java.net.URLEncoder;

import javax.inject.Inject;

import butterknife.BindView;

import static com.jess.arms.utils.Preconditions.checkNotNull;


public class OwnerExchangeFragment extends BaseBackFragment<UserExchangeListPresenter> implements UserContract.View ,BaseQuickAdapter.OnItemClickListener{

    @BindView(R.id.recycle_view)
    RecyclerView recyclerView;
    @Inject
    UserExchangeRecordRecyclerAdapter adapter;

    public static OwnerExchangeFragment newInstance() {
        OwnerExchangeFragment fragment = new OwnerExchangeFragment();
        return fragment;
    }

    @Override
    public void setupFragmentComponent(AppComponent appComponent) {

        DaggerUserComponent //如找不到该类,请编译一下项目
                .builder()
                .appComponent(appComponent)
                .userModule(new UserModule(this))
                .build()
                .inject(this);
    }

    View view;
    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_exchange_record, container, false);
        return view;
    }



    @Override
    public void initData(Bundle savedInstanceState) {
        setTitle("我的兑换记录");


//        mStatusView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 3, GridLayoutManager.VERTICAL, false));
//        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
//        recyclerViewAdapter = new RecyclerViewAdapter(this, mDatas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));// 布局管理器
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setOnItemClickListener(this);


//        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
//            @Override
//            public void onLoadMoreRequested() {
//
//            }
//        });
        
/*        springView.setType(SpringView.Type.FOLLOW);
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }

            @Override
            public void onLoadmore() {

            }
        });
        springView.setHeader(new DefaultHeader(getActivity()));   //参数为：logo图片资源，是否显示文字*/
//        springView.callFresh();
//        springView.setFooter(new DefaultFooter(getActivity()));

        loadData();

    }

    private void loadData(){
        mPresenter.getExchangeRecord("exchange",false);
        try {
//            mPresenter.getTestData(M.getEncryptData(MApplication.getCodedLock(),M.getMapString("page",1)),false);
//            mPresenter.getTestData1(M.getEncryptData(MApplication.getCodedLock(),M.getMapString("page",1)),false);
            String uname  = URLEncoder.encode("123456", "utf-8");
            String upwd  = URLEncoder.encode("link123", "utf-8");
//            mPresenter.getTestData1(M.getEncryptData(MApplication.getCodedLock(),M.getMapString("uname",uname,"upwd",upwd)),false);

  /*          mPresenter.getTestData1(
                    M.getEncryptData(
                            MApplication.getCodedLock()
                            ,M.getMapString("uname","123456","upwd","link123"))
                    ,false);*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 此方法是让外部调用使fragment做一些操作的,比如说外部的activity想让fragment对象执行一些方法,
     * 建议在有多个需要让外界调用的方法时,统一传Message,通过what字段,来区分不同的方法,在setData
     * 方法中就可以switch做不同的操作,这样就可以用统一的入口方法做不同的事
     * <p>
     * 使用此方法时请注意调用时fragment的生命周期,如果调用此setData方法时onCreate还没执行
     * setData里却调用了presenter的方法时,是会报空的,因为dagger注入是在onCreated方法中执行的,然后才创建的presenter
     * 如果要做一些初始化操作,可以不必让外部调setData,在initData中初始化就可以了
     *
     * @param data
     */

    @Override
    public void setData(Object data) {

    }


    @Override
    public void showLoading() {

//        springView.onFinishFreshAndLoad();
    }

    @Override
    public void hideLoading() {
//        springView.onFinishFreshAndLoad();

    }

    @Override
    public void showMessage(@NonNull String message) {
        checkNotNull(message);
        ArmsUtils.snackbarText(message);
    }

    @Override
    public void launchActivity(@NonNull Intent intent) {
        checkNotNull(intent);
        ArmsUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {

    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

    }

    @Override
    public void showStateViewState(int state) {
        showMultiViewState(state);
    }

    @Override
    public void showSpingViewFooterEnable(boolean enabled) {

    }
}
