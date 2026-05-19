package com.fongmi.android.tv.event;

import com.fongmi.android.tv.BuildConfig;

public final class ActionEvent {

    public static final String STOP = BuildConfig.APPLICATION_ID.concat(".stop");
    public static final String PREV = BuildConfig.APPLICATION_ID.concat(".prev");
    public static final String NEXT = BuildConfig.APPLICATION_ID.concat(".next");
    public static final String PLAY = BuildConfig.APPLICATION_ID.concat(".play");
    public static final String PAUSE = BuildConfig.APPLICATION_ID.concat(".pause");
    public static final String AUDIO = BuildConfig.APPLICATION_ID.concat(".audio");
    public static final String REPEAT = BuildConfig.APPLICATION_ID.concat(".repeat");
    public static final String REPLAY = BuildConfig.APPLICATION_ID.concat(".replay");

    public static String STOP = BuildConfig.APPLICATION_ID.concat(".stop");
    public static String PREV = BuildConfig.APPLICATION_ID.concat(".prev");
    public static String NEXT = BuildConfig.APPLICATION_ID.concat(".next");
    public static String PLAY = BuildConfig.APPLICATION_ID.concat(".play");
    public static String PAUSE = BuildConfig.APPLICATION_ID.concat(".pause");
    public static String UPDATE = BuildConfig.APPLICATION_ID.concat(".update");

    private final String action;

    public static void send(String action) {
        EventBus.getDefault().post(new ActionEvent(action));
    }

    public static void update() {
        send(UPDATE);
    }

    public static void next() {
        send(NEXT);
    }

    public static void prev() {
        send(PREV);
    }

    public static void pause() {
        send(PAUSE);
    }

    public ActionEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public boolean isUpdate() {
        return UPDATE.equals(getAction());
    }
}
