package com.dooboolab.flutterinapppurchase;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterInappPurchasePlugin */
public class FlutterInappPurchasePlugin implements MethodCallHandler, FlutterPlugin {
  static AndroidInappPurchasePlugin androidPlugin;
  static AmazonInappPurchasePlugin amazonPlugin;
  Context context;
  private static MethodChannel channel;

  FlutterInappPurchasePlugin() {
    androidPlugin = new AndroidInappPurchasePlugin();
    amazonPlugin = new AmazonInappPurchasePlugin();
  }
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    context= binding.getApplicationContext();

    if(isPackageInstalled(binding.getApplicationContext(), "com.android.vending")) {
      channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_inapp");
      channel.setMethodCallHandler(new FlutterInappPurchasePlugin());
      androidPlugin.registerWith(channel,context);
    } else if(isPackageInstalled(binding.getApplicationContext(), "com.amazon.venezia")) {
      channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_inapp");
      channel.setMethodCallHandler(new FlutterInappPurchasePlugin());
      amazonPlugin.registerWith(channel,binding.getApplicationContext());
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

  }
  // Plugin registration.
  public static void registerWith(Registrar registrar) {


  }

  @Override
  public void onMethodCall(final MethodCall call, final Result result) {
    if(isPackageInstalled(context, "com.android.vending")) {
      androidPlugin.onMethodCall(call, result);
    } else if(isPackageInstalled(context, "com.amazon.venezia")) {
      amazonPlugin.onMethodCall(call, result);
    } else result.notImplemented();
  }

  public static final boolean isPackageInstalled(Context ctx, String packageName) {
    try {
      ctx.getPackageManager().getPackageInfo(packageName, 0);
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
    return true;
  }


}
