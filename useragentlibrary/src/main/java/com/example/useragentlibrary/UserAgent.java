package com.example.useragentlibrary;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Field;

public class UserAgent {
    private String userAgent;
    private Context ctx;
    private static final String TAG = "UserAgentInterceptor";

    public UserAgent(Context context){
        ctx = context;
    }

//        Request.Builder builder = chain.request().newBuilder();
//        userAgent = buildUserAgent(ctx);
//        builder.header(LibConstants.HEADER_PARAM, userAgent);

    public String buildUserAgent() {
        PackageManager packageManager = ctx.getPackageManager();
        String versionName = "";
        try {
            versionName = packageManager.getPackageInfo(ctx.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "nameNotfound";
        }

        String versionCode = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = String.valueOf(
                        packageManager.getPackageInfo(ctx.getPackageName()
                                , 0).getLongVersionCode()
                );
            } else {
                versionCode = String.valueOf(
                        packageManager.getPackageInfo(ctx.getPackageName()
                                , 0).versionCode
                );
            }
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = "versionCodeNotFound";
        }

        ApplicationInfo applicationInfo = ctx.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appName = ((stringId == 0) ?
                applicationInfo.nonLocalizedLabel.toString()
                : ctx.getString(stringId));

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String deviceType = Build.DEVICE;
        String version = String.valueOf(Build.VERSION.SDK_INT);
        String versionRelease = String.valueOf(Build.VERSION.RELEASE);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("android: ").append(versionRelease);

        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field: fields){
            String fieldName = field.getName();
            int fieldValue = -1;

            try{
                fieldValue = field.getInt(new Object());
            }catch (IllegalArgumentException e){
//                Logger.withTag(TAG).withCause(e);
            } catch (IllegalAccessException e){
//                Logger.withTag(TAG).withCause(e);
            } catch (NullPointerException e){
//                Logger.withTag(TAG).withCause(e);
            }

            if (fieldValue == Integer.parseInt(version)){
                stringBuilder.append(" : ").append(fieldName).append(" : ")
                        .append("SDK=").append(fieldValue);
            }
        }

        String installerName = packageManager.getInstallerPackageName(ctx.getPackageName()) ==
                null ?
                "StandAloneInstall" :
                packageManager.getInstallerPackageName(ctx.getPackageName());

        HeaderMap headerMap = new HeaderMap();
        Device device = new Device();
        Client client = new Client();
        Os os = new Os();

        //setup device
        device.setType(deviceType);
        device.setModel(model);
        device.setBrand(manufacturer);

        //setup client
        client.setVersion(String.format("%s(%s)", versionName, versionCode));
        client.setType(installerName);
        client.setEngineVersion("");
        client.setEngine("");
        client.setName(appName);

        //setup OS
        os.setVersion(String.format("SDK %s", version));
        os.setPlatform("Android");
        os.setName(stringBuilder.toString());

        //complete header string value
        headerMap.setDevice(device);
        headerMap.setOs(os);
        headerMap.setClient(client);
        headerMap.setBot(null);

//        Moshi moshi = new Moshi.Builder().build();
//        JsonAdapter<HeaderMap> adapter = moshi.adapter(HeaderMap.class);
//        final String header = adapter.toJson(headerMap);
//        Logger.withTag(TAG).log(header);
//        return header;

        String userAgentValue = String.format("%s / %s(%s); %s; " +
                        "(%s; %s; SDK %s; Android %s)",
                appName, versionName, versionCode, installerName, manufacturer, model, version
                , versionRelease);
//        Logger.withTag(TAG).log(userAgentValue);
//        return StringMapUtils.sanitiseStringVal(userAgentValue);
        return userAgentValue;
    }
}
