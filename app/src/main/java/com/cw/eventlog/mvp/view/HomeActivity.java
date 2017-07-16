package com.cw.eventlog.mvp.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.cw.eventlog.R;
import com.cw.eventlog.base.BaseActivity;
import com.cw.eventlog.mvp.contract.HomeContract;
import com.cw.eventlog.mvp.model.EventModel;
import com.cw.eventlog.mvp.presenter.HomePresenter;
import com.cw.eventlog.mvp.view.adapter.EventAdapter;
import com.cw.eventlog.utils.permission.PermissionUtil;
import com.cw.eventlog.utils.permission.ResultCallBack;
import com.cw.eventlog.widget.EventDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity<HomePresenter> implements HomeContract.View {

    @BindView(R.id.tb_toolbar)
    Toolbar tb_toolbar;
    @BindView(R.id.rv_event_list)
    RecyclerView rv_event_list;
    @BindView(R.id.iv_no_data)
    ImageView iv_no_data;

    private EventAdapter mEventAdapter;

    @Override
    protected HomePresenter getPresenter() {
        return new HomePresenter(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_delete_all) {
            mPresenter.deleteAllEvent();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public void updateList(List<EventModel> list) {
        if (list == null || list.size() == 0) {
            iv_no_data.setVisibility(View.VISIBLE);
            rv_event_list.setVisibility(View.INVISIBLE);
        } else {
            iv_no_data.setVisibility(View.INVISIBLE);
            rv_event_list.setVisibility(View.VISIBLE);
            mEventAdapter.update(list);
        }
    }

    private void initView() {
        setTitle(null);
        setSupportActionBar(tb_toolbar);
        mEventAdapter = new EventAdapter();
        rv_event_list.setLayoutManager(new LinearLayoutManager(this));
        rv_event_list.addItemDecoration(new EventAdapter.DiaryDecoration(20, 10));
        rv_event_list.setAdapter(mEventAdapter);
        showPermissionDialog();
    }

    private void initListener() {
        mEventAdapter.setOnItemClickListener(new EventAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, EventModel data) {
                EventDialog eventDialog = new EventDialog(HomeActivity.this, data);
                eventDialog.show();
                eventDialog.setOnUpdateListener(new EventDialog.OnUpdateListener() {
                    @Override
                    public void onUpdate(boolean isNew, EventModel eventModel) {
                        if (!isNew) {
                            mPresenter.updateEvent(eventModel);
                        }
                    }
                });
            }

            @Override
            public void onItemLongClick(View view, final EventModel tag) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("是否删除？")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mPresenter.deleteEvent(tag.getId());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create().show();
            }
        });
    }

    @OnClick(R.id.fab_add)
    public void onViewClicked() {
        EventDialog eventDialog = new EventDialog(this, null);
        eventDialog.show();
        eventDialog.setOnUpdateListener(new EventDialog.OnUpdateListener() {
            @Override
            public void onUpdate(boolean isNew, EventModel eventModel) {
                if (isNew) {
                    mPresenter.insertEvent(eventModel);
                }
            }
        });
    }

    private void openAppSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(localIntent);
    }

    private void showPermissionDialog() {
        PermissionUtil.with(this)
                .add(Manifest.permission.WRITE_CALENDAR)
                .request(new ResultCallBack() {

                    @Override
                    public void onDenied(String... permissions) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setMessage("请授予日历权限，否则不能使用")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        openAppSettingIntent();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        finish();
                                    }
                                }).create().show();

                    }

                    @Override
                    public void onGrantedAll() {
                        mPresenter.refresh();
                    }

                    @Override
                    public void onGranted(String... permissions) {
                    }

                    @Override
                    public void onRationalShow(String... permissions) {
                    }
                });
    }

}
