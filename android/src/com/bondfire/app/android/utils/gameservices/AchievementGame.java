package com.bondfire.app.android.utils.gameservices;


import java.util.ArrayList;

public class AchievementGame {

    public ArrayList<String> list;

    public void addId(String id) {

        if (list == null) {
            list = new ArrayList<>();
            list.add(id);
            return;
        }

        if(list.size() != 0){
            if(!list.contains(id)){
                list.add(id);
                return;
            }
        }
    }

    public String getNextId(){

       if(list.size() > 0){
           return list.remove(list.size()-1);
       }else{
           return  "-1";
       }
    }
}
