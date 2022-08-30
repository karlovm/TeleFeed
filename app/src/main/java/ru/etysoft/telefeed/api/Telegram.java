package ru.etysoft.telefeed.api;

import android.content.Context;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ru.etysoft.telefeed.CacheUtils;

public class Telegram {
    private static Client client;
    private static Context context;
    private static List<AuthorizationStateCallback> authorizationStateCallbackList = new ArrayList<>();
    private static List<UpdateCallback> updateCallbacks = new ArrayList<>();

    public interface UpdateCallback
    {
        void onUpdate(TdApi.Object object);

    }

    public static void addUpdateCallBack(UpdateCallback updateCallback)
    {
        if(updateCallbacks.contains(updateCallback)) return;
        updateCallbacks.add(updateCallback);
    }

    public static void removeUpdateCallBack(UpdateCallback updateCallback)
    {
        updateCallbacks.remove(updateCallback);
    }

    public static void reset()
    {
        client.close();

        client = null;
    }

    public static void initialize(Context context) {
        if (client == null) {
            Telegram.context = context;

            System.out.println("Initializing Telegram Client...");

            try {
                client = Client.create(new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {

                        try {
                            for (UpdateCallback updateCallback : updateCallbacks) {
                                updateCallback.onUpdate(object);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        switch (object.getConstructor()) {
                            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                                onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                                break;
                        }
                    }
                }, new Client.ExceptionHandler() {
                    @Override
                    public void onException(Throwable e) {
                        e.printStackTrace();
                    }
                }, new Client.ExceptionHandler() {
                    @Override
                    public void onException(Throwable e) {
                        e.printStackTrace();
                    }
                });

                System.out.println("jopa...");
                Client.setLogVerbosityLevel(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addAuthorizationStateCallback(AuthorizationStateCallback authorizationStateCallback) {
        if (!authorizationStateCallbackList.contains(authorizationStateCallback)) {
            authorizationStateCallbackList.add(authorizationStateCallback);
        }
    }

    public static void removeAuthorizationStateCallback(AuthorizationStateCallback authorizationStateCallback) {
        authorizationStateCallbackList.remove(authorizationStateCallback);
    }

    public static Client getClient() {
        return client;
    }

    private static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {


        int apiId = 11796157;
        String apiHash = "17774933ac44aa8fa44a859cced004a4";

        if(CacheUtils.getInstance().hasKey("apiId", context))
        {
            try {
                apiId = Integer.parseInt(CacheUtils.getInstance().getString("apiId", context));
                apiHash = CacheUtils.getInstance().getString("apiHash", context);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        System.out.println("Api id " + apiHash);

        switch (authorizationState.getConstructor()) {

            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:

                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = context.getFilesDir().getAbsolutePath() + "/";
                parameters.filesDirectory = context.getExternalFilesDir(null).getAbsolutePath() + "/";
                parameters.useMessageDatabase = false;
                parameters.useSecretChats = false;
                parameters.apiId = apiId;
                parameters.apiHash = apiHash;
                parameters.systemLanguageCode = "ru";
                parameters.deviceModel = "Android";
                parameters.useFileDatabase = true;
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                for (AuthorizationStateCallback authorizationStateCallback : authorizationStateCallbackList) {
                    authorizationStateCallback.onAuthorizationReady();
                }
                break;
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                for (AuthorizationStateCallback authorizationStateCallback : authorizationStateCallbackList) {
                    authorizationStateCallback.onWaitRegistration();
                }
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                for (AuthorizationStateCallback authorizationStateCallback : authorizationStateCallbackList) {
                    authorizationStateCallback.onWaitPassword();
                }
                break;
            }
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                for (AuthorizationStateCallback authorizationStateCallback : authorizationStateCallbackList) {
                    authorizationStateCallback.onWaitPhoneNumber();
                }
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                for (AuthorizationStateCallback authorizationStateCallback : authorizationStateCallbackList) {
                    authorizationStateCallback.onWaitCode();
                }
                break;
            }
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                    client = Client.create(new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {

                            switch (object.getConstructor()) {
                                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                                    break;
                            }
                        }
                    }, null, null); // recreate client after previous has closed
                break;

        }

    }

    public static class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("Receive an error:" + object);

                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    System.err.println("Receive wrong response from TDLib:" + object);
            }
        }
    }

    public static void log(String s)
    {
        System.out.println(s);
    }

    public interface AuthorizationStateCallback {
        void onWaitPhoneNumber();

        void onWaitCode();

        void onWaitRegistration();

        void onWaitPassword();

        void onAuthorizationReady();
    }
}
