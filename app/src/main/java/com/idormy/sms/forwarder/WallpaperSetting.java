package com.idormy.sms.forwarder;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class WallpaperSetting extends WallpaperService {

    /* renamed from: c  reason: collision with root package name */
    public static boolean f5186c;

    /* renamed from: d  reason: collision with root package name */
    public static String wallpaperPath;

    /* renamed from: e  reason: collision with root package name */
    public static boolean f5188e;

    /* renamed from: f  reason: collision with root package name */
    public static Runnable f5189f;

    /* renamed from: g  reason: collision with root package name */
    public static Runnable f5190g;

    /* renamed from: h  reason: collision with root package name */
    public static Runnable f5191h;
    public boolean a = false;

    /* renamed from: b  reason: collision with root package name */
    public Bitmap f5192b;

    public class a extends Engine {

        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            try {
                if (!isPreview()) {
                    Runnable runnable = WallpaperSetting.f5189f;
                    if (runnable != null) {
                        runnable.run();
                        return;
                    }
                    return;
                }
                Runnable runnable2 = WallpaperSetting.f5191h;
                if (runnable2 != null) {
                    runnable2.run();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        public void onDestroy() {
            if (WallpaperSetting.f5190g != null && isPreview()) {
                WallpaperSetting.f5190g.run();
                WallpaperSetting.f5191h = null;
            }
            super.onDestroy();
        }

        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {

            super.onSurfaceCreated(surfaceHolder);
            Canvas canvas = surfaceHolder.lockCanvas();
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStrokeWidth(10);
            p.setColor(Color.BLUE);
            canvas.drawLine(0, 0, 100, 100, p);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

        public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (!isPreview()) {
                WallpaperSetting.f5186c = false;
            }
            super.onSurfaceDestroyed(surfaceHolder);
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void onCreate() {
        super.onCreate();
    }

    public Engine onCreateEngine() {
        String str = wallpaperPath;
        return new a();
    }

    public void onDestroy() {
        super.onDestroy();
        f5186c = false;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        return super.onStartCommand(intent, i, i2);
    }
}
