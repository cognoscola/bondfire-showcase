package com.bondfire.app.android.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.bondfire.app.R;
import com.bondfire.app.android.activity.GamePlayServiceActivity;

/**
 * Created by alvaregd on 21/03/16.
 */
public class PurchaseListAdapter extends RecyclerView.Adapter<PurchaseListAdapter.PurchaseItemHolder>{

    private LayoutInflater _inflater;
    String[] _childItems = new String[]{"Full Version",};

    public PurchaseListAdapter(LayoutInflater inflater) {
        _inflater = inflater;
    }

    @Override
    public PurchaseItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = _inflater.inflate(R.layout.entry_purchase_item, parent, false);
        PurchaseItemHolder purchaseItemHolder = new PurchaseItemHolder(view);
        return purchaseItemHolder;
    }

    @Override
    public void onBindViewHolder(PurchaseItemHolder holder, final int position) {
        holder.purchaseItemTitle.setText(_childItems[position]);
        holder.rootLayout.setZooming(true);
        holder.rootLayout.setRippleDuration(150);
        holder.rootLayout.setZoomDuration(150);
        holder.rootLayout.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                ((GamePlayServiceActivity)_inflater.getContext()).onPurchaseClicked(GamePlayServiceActivity.SKU_PAID_VERSION);
                Toast.makeText(_inflater.getContext(),"Pressed "+ position , Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return _childItems.length;
    }

    public class PurchaseItemHolder extends RecyclerView.ViewHolder {
        TextView purchaseItemTitle;
        RippleView rootLayout;

        public PurchaseItemHolder(View itemView) {
            super(itemView);
            rootLayout = (RippleView) itemView.findViewById(R.id.ripple_store_item);
            purchaseItemTitle = (TextView) itemView.findViewById(R.id.tv_purchase_title);
        }
    }
}
