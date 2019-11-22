package com.bondfire.app.android.adapter;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bondfire.app.R;

/**
 * Created by alvaregd on 21/03/16.
 * Adapter that fills the Store section list
 */
public class StoreSectionAdapter extends RecyclerView.Adapter<StoreSectionAdapter.SectionHolder> {

    private final LayoutInflater inflater;
    private int selectedIndex = 0;

    String[] _items = new String[]{"General"};
    public StoreSectionAdapter(Context context, int openIndex) {

        inflater = LayoutInflater.from(context);
        this.selectedIndex = openIndex;
    }

    @Override
    public SectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.entry_store_section, parent, false);
        SectionHolder holder = new SectionHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final SectionHolder holder, int position) {
        holder.txtRootLine.setText(_items[position]);

        //see if a section should be open
        if (position == selectedIndex) {
            holder.itemsList.setVisibility(View.VISIBLE);
        }

        holder.itemsList.setLayoutManager(new LinearLayoutManager(inflater.getContext()
        , LinearLayoutManager.HORIZONTAL,false));
        holder.itemsList.setAdapter(new PurchaseListAdapter(inflater));
        holder.itemsList.setHasFixedSize(true);
        holder.itemsList.setNestedScrollingEnabled(false);

        holder.expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemsList.setVisibility(holder.itemsList.getVisibility() == View.VISIBLE ?
                        View.GONE: View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return _items.length;
    }


    class SectionHolder extends RecyclerView.ViewHolder {
        TextView txtRootLine;
        RecyclerView itemsList;
        LinearLayout expandButton;

        public SectionHolder(View itemView) {
            super(itemView);
            expandButton = (LinearLayout) itemView.findViewById(R.id.ll_section_button);
            txtRootLine = (TextView) itemView.findViewById(R.id.store_section_title);
            itemsList = (RecyclerView) itemView.findViewById(R.id.recycler_purchases_available);
        }
    }
}
