//---------------------------------------------------------------------------
//   Program         : Android SDK Demo
//   Filename:       : ServiceNotificationReal.java
//---------------------------------------------------------------------------
// (c) 2015-2018 San Luis Aviation, Inc.  All rights reserved.
//
// This program contains confidential proprietary information and should not
// be disclosed to anyone except on a strict need to know basis and only
// after obtaining written authorization for such disclosure from an
// authorized representative of San Luis Aviation, Inc.
//---------------------------------------------------------------------------
package com.ses.zebra.pssdemo_2019.Services;

import com.slacorp.eptt.android.service.CoreService;
import com.slacorp.eptt.android.service.ServiceNotification;
import com.slacorp.eptt.core.common.CallHistEntry;
import com.slacorp.eptt.core.common.MessageMetaData;

import java.util.List;

/**
 * Interface for receiving notifications from the core.
 **/

public class ServiceNotificationReal implements ServiceNotification {
   private CoreService core;

   @Override
   public void setCoreService(CoreService coreService) {
      core = coreService;
   }

   @Override
   public void statusUpdate(Status status, int i) {
   }

   @Override
   public void missedCallNotify(CallHistEntry callHistEntry) {
   }

   @Override
   public void messageNotify(int i, int i1, List<MessageMetaData> list, boolean b) {
   }

   @Override
   public void softwareUpdateNotify(int i, boolean b) {
   }

   @Override
   public void startPttCall(String s, int i, String s1) {
      // N/A
   }

   @Override
   public void updatePttCall(int i, String s) {
      // N/A
   }

   @Override
   public void endPttCall() {
      // N/A
   }
}
