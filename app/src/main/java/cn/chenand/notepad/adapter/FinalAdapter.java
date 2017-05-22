package cn.chenand.notepad.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by chen on 2017/1/8.
 */

public class FinalAdapter<T> extends RecyclerView.Adapter<FinalAdapter.FinalViewHolder> {
    private  Context mContext;
    private  List<T> mDatas;
    private int mLayoutId;

    public FinalAdapter(Context context, List<T> datas, int layoutid, OnRecycleViewListener onRecycleViewListener) {
        this.mOnRecycleViewListener = onRecycleViewListener;
        this.mDatas = datas;
        this.mContext = context;
        this.mLayoutId = layoutid;
    }

    @Override
    public int getItemCount() {
        return mDatas==null ? 0 :mDatas.size();
    }

    @Override
    public FinalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new FinalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FinalViewHolder holder, final int position) {
        bindData(holder, mDatas.get(position),position);
    }

    private void bindData(final FinalViewHolder holder, final T t, final int position) {
        if (mOnRecycleViewListener != null) {
            mOnRecycleViewListener.bindView(holder,t,position);
        }
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnRecycleViewListener != null) {
                    mOnRecycleViewListener.itemOnclick(position);
                }
            }
        });
        holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnRecycleViewListener != null) {
                    mOnRecycleViewListener.itemOnLongClick(position);
                }
                return true;
            }
        });
    }

    public static class FinalViewHolder extends RecyclerView.ViewHolder {
        private View mLayout;
        public FinalViewHolder(View itemView) {
            super(itemView);
            mLayout = itemView;
        }

        SparseArray<View> mSparseArray = new SparseArray<>();
        public View autoView(int rId) {
            View view = mSparseArray.get(rId);
            if (view == null) {
                view  = mLayout.findViewById(rId);
                mSparseArray.put(rId, view);
            }
            return view;
        }
    }

    public interface OnRecycleViewListener<T>{
        void bindView(FinalViewHolder finalAdapter, T t, int position);

        void itemOnclick(int position);

        void itemOnLongClick(int position);
    }
    private OnRecycleViewListener mOnRecycleViewListener;
}
