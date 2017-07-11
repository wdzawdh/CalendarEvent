package com.cw.eventlog.mvp.view.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cw.eventlog.R;
import com.cw.eventlog.mvp.model.EventModel;
import com.cw.eventlog.utils.DateFormatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cw
 * @date 2017/7/10
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventView> implements View.OnClickListener, View.OnLongClickListener {

    private List<EventModel> mList = new ArrayList<>();

    public void update(List<EventModel> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public EventView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_event_item, parent, false);
        view.setOnClickListener(this);
        view.setLongClickable(true);
        view.setOnLongClickListener(this);
        return new EventView(view);
    }

    @Override
    public void onBindViewHolder(EventView holder, int position) {
        EventModel eventModel = mList.get(position);
        if (eventModel.getTime() != null) {
            holder.tv_month.setText(DateFormatUtil.transMonth(eventModel.getTime()));
            holder.tv_day.setText(DateFormatUtil.transDay(eventModel.getTime()));
        }
        holder.tv_content.setText(eventModel.getContent());
        //将数据保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(eventModel);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (EventModel) v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemLongClick(v, (EventModel) v.getTag());
        }
        return true;
    }

    public static class DiaryDecoration extends RecyclerView.ItemDecoration {

        private int width;
        private int height;

        public DiaryDecoration(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = width;
            outRect.right = width;
            outRect.top = height;
            outRect.bottom = height;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = height * 2;
            }
        }
    }

    static class EventView extends RecyclerView.ViewHolder {

        TextView tv_month;
        TextView tv_day;
        TextView tv_content;

        EventView(View itemView) {
            super(itemView);
            tv_month = (TextView) itemView.findViewById(R.id.tv_month);
            tv_day = (TextView) itemView.findViewById(R.id.tv_day);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, EventModel data);

        void onItemLongClick(View view, EventModel tag);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
