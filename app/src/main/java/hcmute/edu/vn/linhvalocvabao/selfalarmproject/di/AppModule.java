package hcmute.edu.vn.linhvalocvabao.selfalarmproject.di;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Api;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Service;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db.AppDatabase;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.repository.MusicRepository;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NetworkUtils;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.PreferenceManager;

/**
 * Dagger Hilt module for providing application-level dependencies
 * 
 * Last updated: 2025-03-10 09:30:23
 * @author lochuung
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Singleton
    @Provides
    public ZingMp3Service provideZingMp3Service() {
        return ZingMp3Service.Factory.getInstance();
    }

    @Singleton
    @Provides
    public ZingMp3Api provideZingMp3Api() {
        return new ZingMp3Api();
    }

    @Singleton
    @Provides
    public NetworkUtils provideNetworkUtils(@ApplicationContext Context context) {
        return new NetworkUtils(context);
    }

    @Singleton
    @Provides
    public PreferenceManager providePreferenceManager(@ApplicationContext Context context) {
        return new PreferenceManager(context);
    }

    @Singleton
    @Provides
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                "zingmp3_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    public MusicRepository provideMusicRepository(ZingMp3Api api, NetworkUtils networkUtils) {
        return new MusicRepository(api, networkUtils);
    }
}