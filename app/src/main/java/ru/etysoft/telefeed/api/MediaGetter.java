package ru.etysoft.telefeed.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

public class MediaGetter {

    private static boolean hasCallback = false;

    private static HashMap<Integer, MediaResult> mediaResults = new HashMap<>();

    public interface MediaResult
    {
        int getType();
        void onImageProcessed(Bitmap bitmap);
        void onVideoProcessed(String path);
        void onError();
    }

    public static void registerCallback()
    {
        if(hasCallback) return;
        hasCallback = true;
        Telegram.addUpdateCallBack(new Telegram.UpdateCallback() {
            @Override
            public void onUpdate(TdApi.Object object) {

                try
                {
                if (object.getConstructor() == TdApi.UpdateFile.CONSTRUCTOR) {

                    TdApi.UpdateFile updateFile = (TdApi.UpdateFile) object;
                    if (updateFile.file.local.isDownloadingCompleted) {


                        if (mediaResults.containsKey(updateFile.file.id) && !(updateFile.file.local.isDownloadingActive)) {
                            MediaResult mediaResult = mediaResults.get(updateFile.file.id);

                            assert mediaResult != null;
                            if (mediaResult.getType() == MediaInfo.TYPE_IMAGE) {
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                                Bitmap bitmap = BitmapFactory.decodeFile(updateFile.file.local.path, bmOptions);


                                mediaResult.onImageProcessed(bitmap);
                            } else {

                                System.out.println("Video processed by queue!");
                                mediaResult.onVideoProcessed(updateFile.file.local.path);
                            }
                            // article.setImagesList(bitmaps);
                            //articles.put(article.getMessageId(), article);
                        }

                    }
                }
                }
                catch(Exception e)
                    {
                        e.printStackTrace();
                    }



            }
        });
    }

    public static void getImage(int imageId, MediaResult mediaResult)
    {
        registerCallback();
        TdApi.DownloadFile downloadAvatar = new TdApi.DownloadFile();
        downloadAvatar.fileId = imageId;
        downloadAvatar.priority = 32;
        downloadAvatar.synchronous = false;
        downloadAvatar.limit = 0;





        Telegram.getClient().send(downloadAvatar, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.File) {
                    if(((TdApi.File) object).local.isDownloadingCompleted) {
                        if(mediaResult.getType() == MediaInfo.TYPE_IMAGE) {
                            Bitmap bitmap = getBitmap(((TdApi.File) object).local.path);
                            if (bitmap != null) {
                                mediaResult.onImageProcessed(bitmap);
                            } else {
                                mediaResult.onError();
                            }
                        }
                        else
                        {
                            mediaResult.onVideoProcessed(((TdApi.File) object).local.path);
                        }
                    }
                    else
                    {
                        mediaResults.put(imageId, mediaResult);
                    }

                }
                else
                {
                    mediaResult.onError();
                }
            }
        });
    }

    public static Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            File f = new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void loadUserImage(long userId, MediaResult mediaResult)
    {
        TdApi.GetUser getUser = new TdApi.GetUser();
        getUser.userId = userId;


        Telegram.getClient().send(getUser, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                try {
                    if (object instanceof TdApi.User) {
                        getImage(((TdApi.User) object).profilePhoto.small.id, mediaResult);
                    } else {
                        mediaResult.onError();
                    }
                } catch (Exception e)
                {
                    mediaResult.onError();
                }

            }
        });
    }

}
