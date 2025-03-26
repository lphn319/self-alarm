package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.fragments.CallsFragment;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.fragments.SMSFragment;

public class SMSCallsPagerAdapter extends FragmentStateAdapter {
    public SMSCallsPagerAdapter(@NonNull FragmentManager fragmentManager,
                                @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SMSFragment();
            case 1:
                return new CallsFragment();
            default:
                return new SMSFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
