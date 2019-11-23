package com.bondfire.app.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bondfire.app.R;
import com.bondfire.app.android.data.GameInformation;
import com.bondfire.app.android.data.GameInformationCollection;

public class GridViewAdapter extends BaseAdapter {

    private final static String Tag = GridViewAdapter.class.getName();
    private Context cxt;
    //private String[] labelArray;

    private GameInformationCollection gameInformationCollection;

    public GridViewAdapter(Context context, GameInformationCollection information) {

        this.cxt = context;
        this.gameInformationCollection = information;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return gameInformationCollection.getSize();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cxt
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;

        if (convertView == null) {

            gridView = new View(cxt);
            TextView textView;
            ImageView imageView;
            try{
                // get layout from mobile.xml
                gridView = inflater.inflate(R.layout.grid_item_entry, null);

                // set value into textview
                textView = (TextView) gridView
                        .findViewById(R.id.grid_item_label);
                textView.setText(gameInformationCollection.getTitle(position));

                // set image based on selected text
                 imageView = (ImageView) gridView
                        .findViewById(R.id.grid_item_image);

                imageView.setImageResource(gameInformationCollection.getIcon(position));
            }catch (ArrayIndexOutOfBoundsException e){
                Log.e(Tag, "Array Index out of bounds");
                imageView = (ImageView) gridView
                        .findViewById(R.id.grid_item_image);
                imageView.setImageResource(R.drawable.back_icon);
            }
            catch (IndexOutOfBoundsException e){
                imageView = (ImageView) gridView
                        .findViewById(R.id.grid_item_image);
                imageView.setImageResource(R.drawable.back_icon);
                Log.e(Tag, "Index out of bounds");
            }

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    public GameInformation getInformation(int position){

        GameInformation ret =  new GameInformation(gameInformationCollection.getTitle(position),
                gameInformationCollection.getLeaderboardId(position),
                gameInformationCollection.getIcon(position),
                gameInformationCollection.getgameId(position),
                gameInformationCollection.getMinPlayerCount(position),
                gameInformationCollection.getMaxPlayerCount(position)
                );

        ret.usesAdvertisementServices = gameInformationCollection.getUsesAdvertisementServices(position);
        ret.usesLeaderBoardServices = gameInformationCollection.getUsesLeaderBoardServices(position);
        ret.usesTurnBasedMultiplayerService = gameInformationCollection.getUsesTurnBasedMultiPlayerServices(position);
        ret.usesRealTimeMultiplayerServices = gameInformationCollection.getUsesRealTimeMultiplayerServices(position);
        ret.usesDayTimer= gameInformationCollection.getUsesDayTimer(position);

        return ret;
    }

}