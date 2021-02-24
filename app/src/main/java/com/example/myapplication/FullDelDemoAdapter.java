package com.example.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * 介绍：
 * 作者：zhangxutong
 * 邮箱：zhangxutong@imcoming.com
 * 时间： 2016/9/12.
 */

public class FullDelDemoAdapter extends RecyclerView.Adapter<FullDelDemoAdapter.FullDelDemoVH> {
    private Context mContext;
    private LayoutInflater mInfalter;
    private List<SwipeBean> mDatas;
    private OnItemClickListener mListener;

    public FullDelDemoAdapter(Context context, List<SwipeBean> mDatas) {
        mContext = context;
        mInfalter = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    public void setListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public FullDelDemoVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FullDelDemoVH(mInfalter.inflate(R.layout.item_cst_swipe, parent, false));
    }

    @Override
    public void onBindViewHolder(final FullDelDemoVH holder, final int position) {

        holder.content.setText(mDatas.get(position).name + (position % 2 == 0 ? "我右白虎" : "我左青龙"));

        //验证长按
        holder.content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, "longclig", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onLongClick() called with: v = [" + v + "]");
                return false;
            }
        });

        holder.btnUnRead.setVisibility(position % 3 == 0 ? View.GONE : View.VISIBLE);

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "delete", Toast.LENGTH_SHORT).show();
                SwipeMenuLayout.getViewCache().closeMenu();
            }
        });
        //注意事项，设置item点击，不能对整个holder.itemView设置咯，只能对第一个子View，即原来的content设置，这算是局限性吧。
        (holder.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "onClick:" + mDatas.get(holder.getAdapterPosition()).name, Toast.LENGTH_SHORT).show();
//                Log.d("TAG", "onClick() called with: v = [" + v + "]");
                if (mListener != null) {
                    mListener.onItemClick();
                }
            }
        });
        //置顶：
        holder.btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "top", Toast.LENGTH_SHORT).show();
                SwipeMenuLayout.getViewCache().closeMenu();
            }
        });
        holder.btnUnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "unread", Toast.LENGTH_SHORT).show();
                SwipeMenuLayout.getViewCache().closeMenu();
            }
        });
    }

    @Override
    public int getItemCount() {
        return null != mDatas ? mDatas.size() : 0;
    }

    class FullDelDemoVH extends RecyclerView.ViewHolder {
        TextView content;
        Button btnDelete;
        Button btnUnRead;
        Button btnTop;

        public FullDelDemoVH(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUnRead = itemView.findViewById(R.id.btnUnRead);
            btnTop = itemView.findViewById(R.id.btnTop);
        }
    }

    public interface OnItemClickListener {
        void  onItemClick();
    }
}

