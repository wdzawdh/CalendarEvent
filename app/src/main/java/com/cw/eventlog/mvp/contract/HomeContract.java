package com.cw.eventlog.mvp.contract;


import com.cw.eventlog.base.IPresenter;
import com.cw.eventlog.base.IView;
import com.cw.eventlog.mvp.model.EventModel;

import java.util.List;

/**
 * @author Cw
 * @date 2017/4/22
 */
public interface HomeContract {

    interface View extends IView {
        void updateList(List<EventModel> list);
    }

    interface Presenter extends IPresenter {
        void insertEvent(EventModel eventModel);
        void updateEvent(EventModel eventModel);
        void deleteEvent(String id);
        void deleteAllEvent();
        void refresh();
    }
}
