package com.example.nactik_chat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter {
    private int tabCount;

    public PagerAdapter(@NonNull FragmentActivity fragmentActivity, int tabCount) {
        super(fragmentActivity);
        this.tabCount = tabCount;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new chatFragment();
            case 1:
                return new statusFragment();
            case 2:
                return new callFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return tabCount;
    }
}