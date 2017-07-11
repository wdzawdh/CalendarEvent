package com.cw.eventlog.mvp.presenter;

import com.cw.eventlog.base.BasePresenter;
import com.cw.eventlog.mvp.contract.HomeContract;
import com.cw.eventlog.mvp.model.EventModel;
import com.cw.eventlog.mvp.view.HomeActivity;
import com.cw.eventlog.widget.CalendarEvent;

/**
 * @author Cw
 * @date 2017/7/10
 */
public class HomePresenter extends BasePresenter<HomeActivity> implements HomeContract.Presenter {

    public HomePresenter(HomeActivity view) {
        super(view, view);
    }

    @Override
    public void insertEvent(EventModel eventModel) {
        CalendarEvent.insertEvent(eventModel);
        mView.updateList(CalendarEvent.queryEvents());
    }

    @Override
    public void updateEvent(EventModel eventModel) {
        CalendarEvent.updateEvent(eventModel);
        mView.updateList(CalendarEvent.queryEvents());
    }

    @Override
    public void deleteEvent(String id) {
        CalendarEvent.deleteEvent(id);
        mView.updateList(CalendarEvent.queryEvents());
    }

    @Override
    public void deleteAllEvent() {
        CalendarEvent.deleteAllEvent();
        mView.updateList(CalendarEvent.queryEvents());
    }

    @Override
    public void refresh() {
        mView.updateList(CalendarEvent.queryEvents());
    }
}
