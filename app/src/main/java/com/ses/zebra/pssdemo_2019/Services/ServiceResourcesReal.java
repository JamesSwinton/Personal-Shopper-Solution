//---------------------------------------------------------------------------
//   Program         : Android SDK Demo
//   Filename:       : ServiceResourcesReal.java
//---------------------------------------------------------------------------
// (c) 2015-2018 San Luis Aviation, Inc.  All rights reserved.
//
// This program contains confidential proprietary information and should not
// be disclosed to anyone except on a strict need to know basis and only
// after obtaining written authorization for such disclosure from an
// authorized representative of San Luis Aviation, Inc.
//---------------------------------------------------------------------------
package com.ses.zebra.pssdemo_2019.Services;

import android.content.Context;

import com.ses.zebra.pssdemo_2019.R;
import com.slacorp.eptt.android.service.ServiceResources;

/**
 * Interface for providing resources used by the Service
 **/

public class ServiceResourcesReal implements ServiceResources {
   private Context ctx;

   @Override
   public void setContext(Context context) {
      ctx = context;
   }

   @Override
   public String getApplicationName(){
      if (ctx != null) {
         return ctx.getString(R.string.app_name);
      } else {
         return "Android SDK Demo";
      }
   }

   @Override
   public String getDebugSubject() {
      return getApplicationName() + " Debug: ";
   }

   @Override
   public String getUiContext() {
      return "Unknown";
   }
}
