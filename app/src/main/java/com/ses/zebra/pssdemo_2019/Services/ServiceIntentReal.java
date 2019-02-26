//---------------------------------------------------------------------------
//   Program         : Android SDK Demo
//   Filename:       : ServiceIntentReal.java
//---------------------------------------------------------------------------
// (c) 2015-2018 San Luis Aviation, Inc.  All rights reserved.
//
// This program contains confidential proprietary information and should not
// be disclosed to anyone except on a strict need to know basis and only
// after obtaining written authorization for such disclosure from an
// authorized representative of San Luis Aviation, Inc.
//---------------------------------------------------------------------------
package com.ses.zebra.pssdemo_2019.Services;

import android.content.Intent;
import android.os.Bundle;

import com.slacorp.eptt.android.service.CoreService;
import com.slacorp.eptt.android.service.ServiceIntent;

/**
 * Interface for building Intents requested by the core.
 **/

public class ServiceIntentReal implements ServiceIntent {

   private CoreService core;

   @Override
   public void setCoreService(CoreService coreService)
   {
      core = coreService;
   }

   @Override
   public Intent getComposeMessageIntent(Bundle bundle) {
      return null;
   }
}
