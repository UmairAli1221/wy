package com.example.umairali.wyapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Umair Ali on 11/15/2017.
 */

class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Chats chat = new Chats();
                return chat;
            case 1:
                Groups groups=new Groups();
                return groups;
            case 2:
                Contacts contacts = new Contacts();
                return contacts;
            case 3:
                MapsActivity mapsActivity = new MapsActivity();
                return mapsActivity;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            case 3:
                return "Map";
            default:
                return null;
        }
    }
}
