package com.cw.eventlog.widget;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cw.eventlog.R;
import com.cw.eventlog.mvp.model.EventModel;
import com.cw.eventlog.utils.DateFormatUtil;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Cw
 * @date 2017/7/10
 */
public class EventDialog extends AppCompatDialog implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_content)
    EditText tv_content;
    @BindView(R.id.bt_save)
    Button bt_save;

    private EventModel mInfo;
    private DatePickerDialog mDateDialog;
    private TimePickerDialog mTimeDialog;
    private String mDate;

    public EventDialog(Context context, EventModel data) {
        super(context, R.style.NoTitleDialog);
        this.mInfo = data;
        init();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String month = monthOfYear < 9 ? "0" + (monthOfYear + 1) : "" + (monthOfYear + 1);
        String day = dayOfMonth < 10 ? ("0" + dayOfMonth) : ("" + dayOfMonth);
        mDate = year + "-" + month + "-" + day;
        showTimeDialog();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String time = hourOfDay + ":" + minute;
        //yyyy-MM-dd HH:mm
        tv_time.setText(mDate + " " + time);
    }

    private void init() {
        setContentView(R.layout.widget_dialog_add_event);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
        if (mInfo != null) {
            tv_time.setText(DateFormatUtil.transTime(mInfo.getTime()));
            tv_content.setText(mInfo.getContent());
        }
    }

    private void showDateDialog() {
        if (mDateDialog == null) {
            Calendar now = Calendar.getInstance();
            mDateDialog = new DatePickerDialog(getContext()
                    , AlertDialog.THEME_DEVICE_DEFAULT_DARK, this
                    , now.get(Calendar.YEAR)
                    , now.get(Calendar.MONTH)
                    , now.get(Calendar.DAY_OF_MONTH));
        }
        mDateDialog.show();
    }

    private void showTimeDialog() {
        if (mTimeDialog == null) {
            mTimeDialog = new TimePickerDialog(getContext()
                    , AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, 0, 0, true);
        }
        mTimeDialog.show();
    }

    @OnClick(R.id.tv_time)
    public void onSelectTime() {
        showDateDialog();
    }

    @OnClick(R.id.bt_save)
    public void onSave() {
        String content = tv_content.getText().toString();
        String time = DateFormatUtil.parseTime(tv_time.getText().toString());
        if (TextUtils.isEmpty(time)) {
            return;
        }
        if (mInfo == null) {
            //添加模式
            EventModel eventModel = new EventModel();
            eventModel.setTime(time);
            eventModel.setContent(content);

            if (mOnUpdateListener != null) {
                mOnUpdateListener.onUpdate(true, eventModel);
            }
        } else {
            //更新模式
            EventModel eventModel = new EventModel();
            eventModel.setId(mInfo.getId());
            eventModel.setTime(time);
            eventModel.setContent(content);

            if (mOnUpdateListener != null) {
                mOnUpdateListener.onUpdate(false, eventModel);
            }
        }
        dismiss();
    }

    private OnUpdateListener mOnUpdateListener = null;

    public interface OnUpdateListener {
        void onUpdate(boolean isNew, EventModel eventModel);
    }

    public void setOnUpdateListener(OnUpdateListener listener) {
        this.mOnUpdateListener = listener;
    }
}
