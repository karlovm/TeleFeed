package ru.etysoft.telefeed.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ru.etysoft.telefeed.activities.main.NewsAdapter;

public class NewsGetter {

    private static final HashMap<Long, NewsAdapter.Article> articles = new HashMap<>();

    public static List<Long> processedMessages = new ArrayList<>();
    public static HashMap<Integer, NewsAdapter.Article> photoWaitingArticles = new HashMap<>();
    public static HashMap<Long, TdApi.Chat> supergroups = new HashMap<>();
    public static HashMap<Long, Bitmap> supergroupsAvatars = new HashMap<>();


    public static Collection<NewsAdapter.Article> getArticles() throws CloneNotSupportedException {

        return articles.values();

    }




    public static NewsAdapter.Article findArticleForGroup(long mediaAlbum)
    {

        for (NewsAdapter.Article article : new ArrayList<>(articles.values())) {
            if(article.getMessage().mediaAlbumId == mediaAlbum)
            {
                return article;
            }
        }
        return null;
    }

    public static List<NewsAdapter.Article> get(int offset) {
        return null;
    }

    public static void updateArticlesForSupergroup(TdApi.Chat chat, long lastMessageId) {


        Telegram.getClient().send(new TdApi.GetChatHistory(chat.id, lastMessageId, 0, 100, false), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {

                if (object instanceof TdApi.Messages) {
                    for (TdApi.Message message : ((TdApi.Messages) object).messages) {
                        processMessageForArticle(message);
                    }
                }
            }
        });

    }

    public static void processMessageForArticle(TdApi.Message message) {

        if(processedMessages.contains(message.id)) return;

        //System.out.println("Processing message " + message);

        int views = 0;
        int comments = 0;

        if(message.interactionInfo != null) {
            views = message.interactionInfo.viewCount;
            if (message.interactionInfo.replyInfo != null)
            {
                comments = message.interactionInfo.replyInfo.replyCount;
            }
        }




        if (message.content instanceof TdApi.MessageText) {
            String text = (((TdApi.MessageText) message.content).text.text);

            NewsAdapter.Article article = new NewsAdapter.Article(new ArrayList<>(), text,
                    supergroups.get(message.chatId).title, message.date, "0", message, views, comments);
               articles.put(message.id, article);
        } else if (message.content instanceof TdApi.MessagePhoto) {


            String text = ((TdApi.MessagePhoto) message.content).caption.text;

            TdApi.DownloadFile downloadFile = new TdApi.DownloadFile();
            downloadFile.fileId = ((TdApi.MessagePhoto) message.content).photo.sizes[
                    ((TdApi.MessagePhoto) message.content).photo.sizes.length - 1].photo.id;
            downloadFile.priority = 32;
            downloadFile.synchronous = true;
            downloadFile.limit = 0;




            int finalViews = views;
            int finalComments = comments;
            Telegram.getClient().send(downloadFile, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (object instanceof TdApi.File) {
                        List<Bitmap> bitmaps = new ArrayList<>();
                        Bitmap bitmap = getBitmap(((TdApi.File) object).local.path);
                        bitmaps.add(bitmap);
                        NewsAdapter.Article article = new NewsAdapter.Article(bitmaps, text,
                                supergroups.get(message.chatId).title, message.date, "0", message, finalViews, finalComments);
                        article.setMessageId(message.id);
                        article.setMessage(message);


                        if(message.mediaAlbumId != 0)
                        {
                            NewsAdapter.Article articleAlbum = findArticleForGroup(message.mediaAlbumId);
                            if(articleAlbum == null)
                            {
                                articles.put(message.id, article);
                            }
                            else
                            {
                                if(articleAlbum.getText().length() == 0)
                                {
                                    articleAlbum.setText(article.getText());
                                }

                                articleAlbum.getImagesList().add(bitmap);

                                articles.remove(articleAlbum.getMessageId());
                                articles.put(articleAlbum.getMessageId(), articleAlbum);
                            }
                        }
                        else
                        {
                            articles.put(message.id, article);
                        }



                        processedMessages.add(message.id);
                    }
                }
            });


        }

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


    private static boolean hasCallback = false;
    public static void processChannels() {
        Telegram.getClient().send(new TdApi.GetChats(new TdApi.ChatListMain(), 100), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if(object instanceof TdApi.Chats)
                {
                    for(long chatId : ((TdApi.Chats) object).chatIds)
                    {
                        Telegram.getClient().send(new TdApi.GetChat(chatId), new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.Object object) {
                                System.out.println("jopa " + object);
                               if(object instanceof TdApi.Chat)
                               {

                                   TdApi.Chat chat = (TdApi.Chat) object;
                                   TdApi.DownloadFile downloadAvatar = new TdApi.DownloadFile();
                                   if(chat.photo != null)
                                   {
                                       downloadAvatar.fileId = chat.photo.small.id;
                                       downloadAvatar.priority = 32;
                                       downloadAvatar.synchronous = true;
                                       downloadAvatar.limit = 0;
                                   }


                                   Telegram.getClient().send(downloadAvatar, new Client.ResultHandler() {
                                       @Override
                                       public void onResult(TdApi.Object object) {
                                           if (object instanceof TdApi.File) {

                                               Bitmap bitmap = ImageCropper.getCroppedBitmap(getBitmap(((TdApi.File) object).local.path));
                                               supergroupsAvatars.put(chat.id, bitmap);
                                           }
                                       }
                                   });

                                   if (chat.type instanceof TdApi.ChatTypeSupergroup) {
                                       if (((TdApi.ChatTypeSupergroup) chat.type).isChannel) {
                                           supergroups.put(chat.id, chat);
                                           assert chat.lastMessage != null;
                                           System.out.println("Added channel " + chat.title);
                                           updateArticlesForSupergroup(chat, chat.lastMessage.id);
                                           processMessageForArticle(chat.lastMessage);
                                       }
                                   }
                               }
                            }
                        });
                    }
                }

            }
        });

        if(!hasCallback)
        {
            hasCallback = true;
            Telegram.addUpdateCallBack(new Telegram.UpdateCallback() {
                @Override
                public void onUpdate(TdApi.Object object) {

                     if (object.getConstructor() == TdApi.UpdateFile.CONSTRUCTOR) {

                        TdApi.UpdateFile updateNewChat = (TdApi.UpdateFile) object;
                        if (updateNewChat.file.local.isDownloadingCompleted) {

                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeFile(updateNewChat.file.local.path, bmOptions);

                            if (photoWaitingArticles.containsKey(updateNewChat.file.id)) {
                                NewsAdapter.Article article = photoWaitingArticles.get(updateNewChat.file.id);
                                List<Bitmap> bitmaps = new ArrayList<>();
                                bitmaps.add(bitmap);
                                article.setImagesList(bitmaps);
                                //articles.put(article.getMessageId(), article);
                            }

                        }


                    }
                }
            });

        }

    }

}
