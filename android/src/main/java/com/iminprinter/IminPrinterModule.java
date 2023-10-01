package com.iminprinter;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.bridge.ReactContext;
import android.content.Context;
import com.imin.printer.PrinterHelper;

@ReactModule(name = IminPrinterModule.NAME)
public class IminPrinterModule extends ReactContextBaseJavaModule {
  Context mContext = null;
  public static final String NAME = "IminPrinter";

  public IminPrinterModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mContext = reactContext;
    PrinterHelper.getInstance().initPrinterService(mContext.getApplicationContext());
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  // initailize printer
  @ReactMethod
  public void initPrinter(Promise promise) {
    PrinterHelper.getInstance().initPrinterService(mContext.getApplicationContext());
    promise.resolve(true);
  }

  // get printer status
  @ReactMethod
  public void getPrinterStatus(Promise promise) {
    promise.resolve(PrinterHelper.getInstance().getPrinterStatus());
  }
  
  // print self test page
  @ReactMethod
  public void printSelfTestPage(Promise promise) {
    PrinterHelper.getInstance().printerSelfChecking(null);
    promise.resolve(true);
  }
}
