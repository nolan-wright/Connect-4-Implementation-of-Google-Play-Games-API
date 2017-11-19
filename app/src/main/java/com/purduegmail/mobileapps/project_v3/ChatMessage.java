package com.purduegmail.mobileapps.project_v3;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nolan Wright on 11/18/2017.
 */

public class ChatMessage implements Parcelable {

    private static final int MESSAGE_WAS_SENT = 0;
    private static final int MESSAGE_WAS_RECEIVED = 1;

    // implementation of Parcelable
    public int describeContents() {
        return hashCode();
    }
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(message);
        if (wasSent) {
            parcel.writeInt(MESSAGE_WAS_SENT);
        }
        else {
            parcel.writeInt(MESSAGE_WAS_RECEIVED);
        }
    }
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }};

    private String message;
    public String getMessage() {
        return message;
    }

    private boolean wasSent; // whether the message was sent or received
    public Boolean wasSent() {
        return wasSent;
    }

    // constructors
    public ChatMessage(String msg, boolean messageWasSentRatherThanReceived) {
        message = msg;
        wasSent = messageWasSentRatherThanReceived;
    }
    public ChatMessage(Parcel in) {
        message = in.readString();
        int wasSentFlag = in.readInt();
        if (wasSentFlag == MESSAGE_WAS_SENT) {
            wasSent = true;
        }
        else {
            wasSent = false;
        }
    }

}