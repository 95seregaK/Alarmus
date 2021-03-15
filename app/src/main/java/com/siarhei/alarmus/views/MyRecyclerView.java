package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.siarhei.alarmus.R;

public class MyRecyclerView extends RecyclerView {
    private RecyclerItemClickListener recyclerItemClickListener;
    private OnItemSwipeListener onItemSwipeListener;
    public static int RIGHT = ItemTouchHelper.RIGHT;
    public static int LEFT = ItemTouchHelper.LEFT;
    public static int DOWN = ItemTouchHelper.DOWN;
    public static int UP = ItemTouchHelper.UP;

    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public MyRecyclerView(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(@NonNull Context context) {
        recyclerItemClickListener = new RecyclerItemClickListener(context, this, null);
        onItemSwipeListener = null;


    }

    public void setOnItemClickListener(@NonNull OnItemClickListener onItemClickListener) {
        recyclerItemClickListener.mListener = onItemClickListener;
        this.removeOnItemTouchListener(recyclerItemClickListener);
        this.addOnItemTouchListener(recyclerItemClickListener);
    }

    public void setOnItemSwipeListener(@NonNull OnItemSwipeListener listener) {
        this.onItemSwipeListener = listener;
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getAdapterPosition();
                onItemSwipeListener.onItemSwipe(viewHolder.itemView, position, swipeDir);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(this);
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView,
                                         OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (childView != null && mListener != null) {
                        View rowSwitch = childView.findViewById(R.id.row_switch);
                        if (e.getX() < rowSwitch.getLeft()) {
                            mListener.onLongItemClick(childView,
                                    recyclerView.getChildAdapterPosition(childView));
                        }
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());


            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                View rowSwitch = childView.findViewById(R.id.row_switch);
                if (rowSwitch != null && e.getX() < rowSwitch.getLeft()) {
                    //Toast.makeText(getContext(), childView.toString(), Toast.LENGTH_SHORT).show();
                    mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                    return true;
                }
            }
            //Toast.makeText(getContext(), "onItemClick()" + false, Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
            Toast.makeText(getContext(), "onInterceptTouchEvent()" + true, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public void onLongItemClick(View view, int position);
    }

    public interface OnItemSwipeListener {
        public void onItemSwipe(View view, int position, int dir);
    }
}