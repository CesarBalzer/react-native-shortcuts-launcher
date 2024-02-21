package com.shortcutslauncher;

import android.R.drawable;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import android.graphics.drawable.Icon;
import android.app.Activity;
import android.os.Build;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Locale.Category;
import java.util.HashMap;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import static android.text.TextUtils.isEmpty;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;


public class ShortcutsLauncherModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private static final String TAG = "SHORT_CUTS_PLUGIN";
  private final String SHORTCUT_NOT_EXIST = "SHORTCUT_NOT_EXIST";
  private final String DEFAULT_ACTIVITY = "MainActivity";
  private final String ID_KEY = "id";
  private final String SHORT_LABEL_KEY = "shortLabel";
  private final String LONG_LABEL_KEY = "longLabel";
  private final String ICON_FOLDER_KEY = "iconFolderName";
  private final String ICON_NAME_KEY = "iconName";
  private final String ICON_PHONE_KEY = "phoneNumber";
  private final String ACTIVITY_NAME_KEY = "activityName";
  private static final int PERMISSIONS_REQUEST_ACCESS_CALL = 101;

  public ShortcutsLauncherModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "ShortcutsLauncher";
  }

  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  @ReactMethod
  public void handleShortcut(Callback successCallback) {
    if (Build.VERSION.SDK_INT < 25) {
      successCallback.invoke();
      return;
    }

    Activity currentActivity = this.reactContext.getCurrentActivity();
    String shortCutId = currentActivity.getIntent().getStringExtra("shortcutId");

    if (shortCutId == null) {
      successCallback.invoke();
    } else {
      successCallback.invoke(shortCutId);
    }
  }

  @ReactMethod
  public void clearShortcutIntent() {
    Activity currentActivity = this.reactContext.getCurrentActivity();

    Intent intent = currentActivity.getIntent();
    intent.removeExtra("shortcutId");

    currentActivity.setIntent(intent);
  }

  @ReactMethod
  public void removeShortcut(String id) {
    Log.i(TAG, "REMOVE SHORTCUT" + id);
    if (Build.VERSION.SDK_INT < 25)
      return;

    ShortcutManager shortcutManager = (ShortcutManager) getShortCutManager();
    shortcutManager.removeDynamicShortcuts(Arrays.asList(id));
  }

  @ReactMethod
  public void removeAllShortcuts() {
    if (Build.VERSION.SDK_INT < 25)
      return;

    ShortcutManager shortcutManager = (ShortcutManager) getShortCutManager();
    shortcutManager.removeAllDynamicShortcuts();
  }

  @ReactMethod
  public void exists(String id, Promise promise) {
    if (Build.VERSION.SDK_INT < 25)
      return;

    if (isShortcutExist(id)) {
      promise.resolve(null);
    } else {
      promise.reject(SHORTCUT_NOT_EXIST, "Not found this app shortcut");
    }
  }

  @ReactMethod
  public void addShortcutToScreen(ReadableMap shortcutDetails, Promise promise)
      throws PackageManager.NameNotFoundException {

    Log.i(TAG, "ADD SHORTCUT TO SCREEN");
    if (Build.VERSION.SDK_INT < 25)
      return;
    if (isShortcutExist(shortcutDetails.getString(ID_KEY))) {
      Log.i(TAG, "O ATALHO JA EXISTE");
      return;
    }

    Boolean checkPermission = checkPermissionToCall();
    if (!checkPermission) {
      Log.i(TAG, "NOT PERMISSION TO CALL");
      return;
    }

    ShortcutManager shortcutManager = (ShortcutManager) getShortCutManager();
    Activity currentActivity = this.reactContext.getCurrentActivity();
    Context currentContext = currentActivity.getApplicationContext();
    String activityPackage = currentActivity.getPackageName();

    PackageManager pm = currentActivity.getPackageManager();

    IconCompat icon;
    ApplicationInfo applicationInfo = pm.getApplicationInfo(
        activityPackage,
        PackageManager.GET_META_DATA);
    System.out.println(applicationInfo);
    Log.i(TAG, String.format("--------APPLICATIONINFO.", applicationInfo));
    icon = IconCompat.createWithResource(this.reactContext, applicationInfo.icon);

    int iconId = currentContext.getResources().getIdentifier(shortcutDetails.getString(ICON_NAME_KEY),
        shortcutDetails.getString(ICON_FOLDER_KEY), activityPackage);

    if (ShortcutManagerCompat.isRequestPinShortcutSupported(this.reactContext)) {
      String url = "tel:" + shortcutDetails.getString(ICON_PHONE_KEY);
      Log.v(TAG, "SUPORTED PIN SHORTCUT " + url);

      Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse(url));
      intentCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // Intent intentCall = new Intent(Intent.ACTION_VIEW,
      // Uri.parse("https://google.com"));

      // intentCall.addCategory(Intent.CALL);

      ShortcutInfoCompat builder = new ShortcutInfoCompat.Builder(this.reactContext, ID_KEY)
          .setActivity(intentCall.getComponent())
          .setShortLabel(shortcutDetails.getString(SHORT_LABEL_KEY))
          .setLongLabel(shortcutDetails.getString(LONG_LABEL_KEY))
          .setIcon(IconCompat.createWithResource(currentActivity.getApplicationContext(),
              iconId))
          .setIntent(intentCall)
          // .setIntent(new Intent(Intent.ACTION_CALL, Uri.parse("https://google.com")))
          .build();

      Log.v(TAG, "ADDED PIN SHORTCUT ");

      try {
        ShortcutManagerCompat.requestPinShortcut(this.reactContext, builder, null);
        promise.resolve("BAGUA");
      } catch (Exception e) {
        promise.reject(SHORTCUT_NOT_EXIST, "NOT FOUND SHORTCUT");
      }
    }
  }

  @ReactMethod
  public Boolean checkPermissionToCall() {
    Activity currentActivity = this.reactContext.getCurrentActivity();
    if (ContextCompat.checkSelfPermission(getReactApplicationContext(),
        android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
      return true;
    } else {
      ActivityCompat.requestPermissions(getCurrentActivity(),
          new String[] { android.Manifest.permission.CALL_PHONE },
          PERMISSIONS_REQUEST_ACCESS_CALL);
      return false;
    }
  }

  @ReactMethod
  public void addDetailToShortcut(ReadableMap shortcutDetails) {
    Log.v(TAG, "ADD DETAIL TO SHORTCUT ");
    if (Build.VERSION.SDK_INT < 25)
      return;
    if (isShortcutExist(shortcutDetails.getString(ID_KEY)))
      return;

    ShortcutInfo shortcut = initShortcut(shortcutDetails);

    ShortcutManager shortcutManager = getShortCutManager();
    shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut));
  }

  @ReactMethod
  public void updateShortcut(ReadableMap shortcutDetail) {
    Log.v(TAG, "UPDATE SHORTCUT " + shortcutDetail);
    if (Build.VERSION.SDK_INT < 25)
      return;

    if (isShortcutExist(shortcutDetail.getString(ID_KEY))) {
      String activityName = DEFAULT_ACTIVITY;
      if (shortcutDetail.getString(ACTIVITY_NAME_KEY) != null) {
        activityName = shortcutDetail.getString(ACTIVITY_NAME_KEY);
      }

      ShortcutInfo shortcut = (ShortcutInfo) initShortcut(shortcutDetail);

      ShortcutManager shortcutManager = (ShortcutManager) getShortCutManager();
      shortcutManager.updateShortcuts(Arrays.asList(shortcut));
    } else {
      return;
    }
  }

  @Nullable
  private ShortcutInfo initShortcut(ReadableMap shortcutDetail) {
    if (Build.VERSION.SDK_INT < 25)
      return null;

    String activityName = DEFAULT_ACTIVITY;
    try {
      activityName = shortcutDetail.getString(ACTIVITY_NAME_KEY);
    } catch (Exception e) {

    }
    Activity currentActivity = this.reactContext.getCurrentActivity();
    Intent intent = new Intent(currentActivity.getApplicationContext(), currentActivity.getClass());
    intent.putExtra("shortcutId", shortcutDetail.getString(ID_KEY));
    intent.setAction(Intent.ACTION_VIEW);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

    Context currentContext = currentActivity.getApplicationContext();
    int iconId = currentContext.getResources().getIdentifier(shortcutDetail.getString(ICON_NAME_KEY),
        shortcutDetail.getString(ICON_FOLDER_KEY), currentContext.getPackageName());

    ShortcutInfo shortcut = new ShortcutInfo.Builder(currentActivity, shortcutDetail.getString(ID_KEY))
        .setShortLabel(shortcutDetail.getString(SHORT_LABEL_KEY))
        .setLongLabel(shortcutDetail.getString(LONG_LABEL_KEY))
        .setIcon(Icon.createWithResource(currentActivity.getApplicationContext(), iconId))
        .setIntent(intent)
        .build();
    return shortcut;
  }

  private boolean isShortcutExist(String id) {
    Log.v(TAG, "IS SHORTCUT EXIST " + id);
    if (Build.VERSION.SDK_INT < 25)
      return false;

    ShortcutManager shortcutManager = (ShortcutManager) getShortCutManager();
    List<ShortcutInfo> shortcutInfoList = shortcutManager.getDynamicShortcuts();
    for (ShortcutInfo shortcutInfo : shortcutInfoList) {
      if (shortcutInfo.getId().equals(id)) {
        Log.v(TAG, "IS SHORTCUT EXIST " + shortcutInfo.getId());
        return true;
      }
    }
    Log.v(TAG, "IS SHORTCUT NOT EXIST ");
    return false;
  }

  @Nullable
  private ShortcutManager getShortCutManager() {
    if (Build.VERSION.SDK_INT < 25)
      return null;

    Activity currentActivity = this.reactContext.getCurrentActivity();
    ShortcutManager shortcutManager = (ShortcutManager) currentActivity.getSystemService(Context.SHORTCUT_SERVICE);

    return shortcutManager;
  }

  @ReactMethod

  private Intent parseIntent(JSONObject jsonIntent) throws JSONException {
    String url = "tel:" + jsonIntent.getString("phone");
    // String action = jsonIntent.getString("action");
    // Log.v(TAG, "ACTION " + action);
    // Intent intent = new Intent(Intent.ACTION_CALL);
    // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // return intent;
    // Log.v(TAG, "SUPORTED PIN SHORTCUT " + url);
    Intent intent = new Intent();
    Activity currentActivity = this.reactContext.getCurrentActivity();
    String activityClass = jsonIntent.optString("activityClass", currentActivity.getClass().getName());
    String activityPackage = jsonIntent.optString("activityPackage", currentActivity.getPackageName());
    Log.v(TAG, "ACTIVITY CLASS " + activityClass);
    Log.v(TAG, "ACTIVITY PACKAGE " + activityPackage);

    // intent.setClassName(Intent.ACTION_CALL, Uri.parse(url));

    String action = jsonIntent.getString("action");
    // Log.i(TAG, "ACTION: " + action);
    // if (action.indexOf('.') < 0) {
    // action = activityPackage + '.' + action;
    // }
    Log.i(TAG, "Creating new intent with action: " + action);
    intent.setAction(action);
    intent.setData(Uri.parse(url));

    int flags = jsonIntent.optInt("flags", Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.setFlags(flags);
    Log.i(TAG, "Added flags to intent " + flags);
    // Toast.makeText(this.reactContext, "Contato: " + url + "
    // selecionado.",Toast.LENGTH_LONG).show();
    // return intent;
    JSONArray jsonCategories = jsonIntent.optJSONArray("categories");
    if (jsonCategories != null) {
      int count = jsonCategories.length();
      for (int i = 0; i < count; ++i) {
        String category = jsonCategories.getString(i);
        if (category.indexOf('.') < 0) {
          category = activityPackage + '.' + category;
        }
        intent.addCategory(category);
        Log.i(TAG, "Added categories intent: " + category);
      }
    }

    // String data = jsonIntent.optString("data");
    // if (data.length() > 0) {
    // intent.setData(Uri.parse(data));
    // Log.i(TAG, "Added data intent: " + data);
    // }

    JSONObject extras = jsonIntent.optJSONObject("extras");
    if (extras != null) {
      Iterator<String> keys = extras.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        Object value = extras.get(key);
        if (value != null) {
          if (key.indexOf('.') < 0) {
            key = activityPackage + "." + key;
          }
          if (value instanceof Boolean) {
            intent.putExtra(key, (Boolean) value);
          } else if (value instanceof Integer) {
            intent.putExtra(key, (Integer) value);
          } else if (value instanceof Long) {
            intent.putExtra(key, (Long) value);
          } else if (value instanceof Float) {
            intent.putExtra(key, (Float) value);
          } else if (value instanceof Double) {
            intent.putExtra(key, (Double) value);
          } else {
            intent.putExtra(key, value.toString());
          }
        }
      }
    }
    return intent;
  }

  @ReactMethod
  private ShortcutInfoCompat buildPinnedShortcut(JSONObject jsonShortcut)
      throws PackageManager.NameNotFoundException, JSONException {
    if (jsonShortcut == null) {
      throw new InvalidParameterException("Parameters must include a valid shorcut.");
    }

    Activity currentActivity = this.reactContext.getCurrentActivity();
    Context context = currentActivity.getApplicationContext();
    String shortcutId = jsonShortcut.getString("id");
    if (shortcutId.length() == 0) {
      throw new InvalidParameterException("A value for 'id' is required");
    }

    ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(context, shortcutId);

    String shortLabel = jsonShortcut.getString("shortLabel");
    String longLabel = jsonShortcut.getString("longLabel");
    if (shortLabel.length() == 0 && longLabel.length() == 0) {
      throw new InvalidParameterException(
          "A value for either 'shortLabel' or 'longLabel' is required");
    }

    if (shortLabel.length() == 0) {
      shortLabel = longLabel;
    }

    if (longLabel.length() == 0) {
      longLabel = shortLabel;
    }

    IconCompat icon;
    String iconBitmap = jsonShortcut.getString(ICON_NAME_KEY);
    String iconFolder = jsonShortcut.getString(ICON_FOLDER_KEY);
    int iconId = context.getResources().getIdentifier(iconBitmap, iconFolder, context.getPackageName());

    // Icon icon;
    // String iconBitmap = jsonShortcut.optString("iconBitmap");
    // String iconFromResource = jsonShortcut.optString("iconFromResource");

    // String activityPackage = this.cordova.getActivity().getPackageName();

    // if (iconBitmap.length() > 0) {
    // icon = Icon.createWithBitmap(decodeBase64Bitmap(iconBitmap));
    // }

    // if (iconFromResource.length() > 0) {
    // Resources activityRes = this.cordova.getActivity().getResources();
    // int iconId = activityRes.getIdentifier(
    // iconFromResource,
    // "drawable",
    // activityPackage);
    // icon = Icon.createWithResource(context, iconId);
    // } else {
    // PackageManager pm = context.getPackageManager();
    // ApplicationInfo applicationInfo = pm.getApplicationInfo(
    // activityPackage,
    // PackageManager.GET_META_DATA);
    // icon = Icon.createWithResource(activityPackage, applicationInfo.icon);
    // }

    JSONObject jsonIntent = jsonShortcut.optJSONObject("intent");
    if (jsonIntent == null) {
      jsonIntent = new JSONObject();
    }

    Intent intent = parseIntent(jsonIntent);

    return builder
        .setActivity(intent.getComponent())
        .setShortLabel(shortLabel)
        .setLongLabel(longLabel)
        // .setIcon(IconCompat.createWithResource(context, android.R.drawable.padrao))
        .setIcon(IconCompat.createWithResource(context, iconId))
        .setIntent(intent)
        .build();
  }

  @ReactMethod
  public boolean addPinnedShortcut(String args, Promise promise) throws PackageManager.NameNotFoundException {
    try {

      Log.v(TAG, "----ADD PINNED SHORTCUT-----");
      JSONObject obj = new JSONObject(args);
      String shortLabel = obj.getString("longLabel");
      // Log.v(TAG, shortLabel);

      ShortcutInfoCompat shortcut = buildPinnedShortcut(obj);
      Activity currentActivity = this.reactContext.getCurrentActivity();
      Context context = currentActivity.getApplicationContext();
      Log.v(TAG, "----ADD PINNED SHORTCUT RESOLVE-----");
      promise.resolve("OK");
      return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null);
    } catch (Exception e) {
      Log.v(TAG, "----ADD PINNED SHORTCUT REJECT-----" + e);
      promise.reject("ERROR CREATE SHORTCUT");
      return false;
    }
  }

  // @ReactMethod
  // public void getDrawableImageNames(Promise promise) {
  // Activity currentActivity = getCurrentActivity();
  // Context context = currentActivity.getApplicationContext();
  // Field[] drawableFields = R.drawable.class.getDeclaredFields();
  // List<String> drawableImageNames = new ArrayList<>();
  // for (Field field : drawableFields) {
  // try {
  // // if (field.getName().startsWith("ic_")) {
  // int resId = field.getInt(R.drawable.class);
  // String resName = context.getResources().getResourceEntryName(resId);
  // drawableImageNames.add(resName);
  // // }
  // } catch (IllegalAccessException e) {
  // e.printStackTrace();
  // }
  // }
  // Log.v(TAG, "Drawable images: " + drawableImageNames.toString());
  // promise.resolve(drawableImageNames);
  // }

  @ReactMethod
  public void getDrawableImageNames(Promise promise) throws IOException {
    Activity currentActivity = this.reactContext.getCurrentActivity();
    Resources resources = currentActivity.getResources();
    AssetManager assetManager = resources.getAssets();

    String[] imageNames = resources.getAssets().list("drawable");
    List<String> imagePaths = new ArrayList<>();

    for (String imageName : imageNames) {
      int resId = resources.getIdentifier(imageName, "drawable", currentActivity.getPackageName());
      if (resId != 0) {
        imagePaths.add("drawable://" + resId);
      }
    }

    promise.resolve(imagePaths);
  }

  // @ReactMethod
  // public List<String> getDrawableImageNames() throws IOException {
  // Activity currentActivity = this.reactContext.getCurrentActivity();
  // Context context = currentActivity.getApplicationContext();
  // List<String> imageNames = new ArrayList<>();
  // String[] drawableImages =
  // context.getResources().getAssets().list("drawable");
  // // Log.v(TAG, "----FOR-----" + Arrays.toString(drawableImages));
  // for (String imageName : drawableImages) {
  // imageNames.add(imageName);
  // }
  // Log.v(TAG, "----DRAWABLE IMAGES-----" + Arrays.toString(drawableImages));
  // return Arrays.asList(drawableImages);
  // }

  private JSONObject convertReadableMapToJson(ReadableMap readableMap) throws Exception {
    HashMap<String, Object> map = readableMap.toHashMap();
    JSONObject jsonObject = new JSONObject(map);
    return jsonObject;
  }

}
