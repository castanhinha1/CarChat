/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter.ViewControllers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.onesignal.OSPermissionState;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import ConfigClasses.OneSignalNotificationOpenedHandler;
import Models.FollowTable;
import Models.User;


public class WeightManagementMain extends Application {

  private static Context context;

  public static Context getContext() {
    return context;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    //Register Parse subclasses
    ParseUser.registerSubclass(User.class);
    ParseObject.registerSubclass(FollowTable.class);

    // Add your initialization code here
    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                    .applicationId("Qa4HAnmUrCnPYvrt7LND6hB3oXUTtYRVAfz1kjRl")
                    .clientKey("eYqFKXa6rEZ62Hsfqz3nLBTZWmTC1ZEW3IWsqGuI")
                    .server("https://parseapi.back4app.com/")
    .build()
    );

    // This is the installation part
    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
    installation.put("GCMSenderId", "521084357311");
    installation.saveInBackground();

    ParseFacebookUtils.initialize(getApplicationContext());

    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

    OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .setNotificationOpenedHandler(new OneSignalNotificationOpenedHandler())
            .init();

  }
}
