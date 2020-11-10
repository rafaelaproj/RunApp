package rafaelacs.com.br.runapp.di

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import rafaelacs.com.br.runapp.R
import rafaelacs.com.br.runapp.other.Constants
import rafaelacs.com.br.runapp.ui.MainActivity

/*
 * ServiceModule provides dependencies for the TrackingService.
 */
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @SuppressLint("VisibleForTests")
    @ServiceScoped
    @Provides
    fun providerFusedLocationProviderClient(
            @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
            @ApplicationContext app: Context
    ): PendingIntent = PendingIntent.getActivity(
            app,
            0,
            Intent(app, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder (
            @ApplicationContext app: Context,
            pendingIntent: PendingIntent
    ): NotificationCompat.Builder =
            NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                    .setContentTitle("RouteMe")
                    .setContentText("00:00:00")
                    .setContentIntent(pendingIntent)

}