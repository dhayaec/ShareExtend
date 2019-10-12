package com.zt.shareextend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * Plugin method host for presenting a share sheet via Intent
 */
public class ShareExtendPlugin implements MethodChannel.MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {

    /// the authorities for FileProvider
    private static final int CODE_ASK_PERMISSION = 100;
    private static final String CHANNEL = "share_extend";

    private final Registrar mRegistrar;
    private String text;
    private String type;
    private ArrayList<String> filePaths;
    private String appId;

    public static void registerWith(Registrar registrar) {
        MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL);
        final ShareExtendPlugin instance = new ShareExtendPlugin(registrar);
        registrar.addRequestPermissionsResultListener(instance);
        channel.setMethodCallHandler(instance);
    }


    private ShareExtendPlugin(Registrar registrar) {
        this.mRegistrar = registrar;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        if (call.method.equals("share")) {
            if (!(call.arguments instanceof Map)) {
                throw new IllegalArgumentException("Map argument expected");
            }
            // Android does not support showing the share sheet at a particular point on screen.
            share((String) call.argument("text"), (String) call.argument("type"));
            result.success(null);
        } else if (call.method.equals("shareMultiple")) {
            if (!(call.arguments instanceof Map)) {
                throw new IllegalArgumentException("Map argument expected");
            }
            // Android does not support showing the share sheet at a particular point on screen.
            shareMultiple((ArrayList<String>) call.argument("filePaths"), (String) call.argument("appId"));
            result.success(null);
        } else {
            result.notImplemented();
        }
    }

    private void share(String text, String type) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Non-empty text expected");
        }
        this.text = text;
        this.type = type;

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        if ("text".equals(type)) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareIntent.setType("text/plain");
        } else {
            File f = new File(text);
            if (!f.exists()) {
                throw new IllegalArgumentException("file not exists");
            }

            if (ShareUtils.shouldRequestPermission(text)) {
                if (!checkPermisson()) {
                    requestPermission();
                    return;
                }
            }

            Uri uri = ShareUtils.getUriForFile(mRegistrar.activity(), f, type);

            if ("image".equals(type)) {
                shareIntent.setType("image/*");
            } else if ("video".equals(type)) {
                shareIntent.setType("video/*");
            } else {
                shareIntent.setType("application/*");
            }
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        Intent chooserIntent = Intent.createChooser(shareIntent, null /* dialog title optional */);
        if (mRegistrar.activity() != null) {
            mRegistrar.activity().startActivity(chooserIntent);
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mRegistrar.context().startActivity(chooserIntent);
        }
    }

    private void shareMultiple(ArrayList<String> filePaths, String appId) {
        if (filePaths.size() == 0) {
            throw new IllegalArgumentException("Non-empty filePaths expected");
        }
        this.filePaths = filePaths;
        this.appId = appId;

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Splitted videos.");
        intent.setType("video/*");
        if (!appId.isEmpty()) {
            intent.setPackage(appId);
        }


        ArrayList<Uri> files = new ArrayList<>();

        for (String path : filePaths /* List of the files you want to send */) {
            File file = new File(path);
            Uri uri = ShareUtils.getUriForFile(mRegistrar.activity(), file, "video/*");
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);


        Intent chooserIntent = Intent.createChooser(intent, "Set as WhatsApp status" /* dialog title optional */);

        if (mRegistrar.activity() != null) {
            mRegistrar.activity().startActivity(chooserIntent);
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mRegistrar.context().startActivity(chooserIntent);
        }
    }

    private boolean checkPermisson() {
        if (ContextCompat.checkSelfPermission(mRegistrar.context(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(mRegistrar.activity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_ASK_PERMISSION);
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] perms, int[] grantResults) {
        if (requestCode == CODE_ASK_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            share(text, type);
        }
        return false;
    }
}
