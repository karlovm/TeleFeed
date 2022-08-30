package ru.etysoft.telefeed.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import ru.etysoft.telefeed.activities.main.MainActivity;
import ru.etysoft.telefeed.activities.main.NewsAdapter;
import ru.etysoft.telefeed.bottomsheet.ShareBottomSheet;

public class NewsGetter {

    private static final HashMap<Long, NewsAdapter.Article> articles = new HashMap<>();

    private static final List<Long> processedMessages = new ArrayList<>();
    public static List<Long> processedAvatars = new ArrayList<>();
    public static HashMap<Integer, NewsAdapter.Article> photoWaitingArticles = new HashMap<>();
    public static HashMap<Long, TdApi.Chat> supergroups = new HashMap<>();
    public static HashMap<Long, Bitmap> supergroupsAvatars = new HashMap<>();


    public static Collection<NewsAdapter.Article> getArticles() {

        return articles.values();

    }


    public static NewsAdapter.Article findArticleForGroup(long mediaAlbum) {

        for (NewsAdapter.Article article : new ArrayList<>(articles.values())) {
            if (article.getMessage().mediaAlbumId == mediaAlbum) {
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


        if (processedMessages.contains(message.id)) return;
        processedMessages.add(message.id);
        //System.out.println("Processing message " + message);

        int views = 0;
        int comments = 0;

        if (message.interactionInfo != null) {
            views = message.interactionInfo.viewCount;
            if (message.interactionInfo.replyInfo != null) {
                comments = message.interactionInfo.replyInfo.replyCount;
            }
        }


        if (message.content instanceof TdApi.MessageText) {
            String text = (((TdApi.MessageText) message.content).text.text);



            NewsAdapter.Article article = new NewsAdapter.Article(new ArrayList<>(), text,
                    supergroups.get(message.chatId).title, message.date, "0", message, views, comments);
            article.setMessageId(message.id);
            article.setMessage(message);
            articles.put(message.id, article);


        } else if (message.content instanceof TdApi.MessagePhoto || message.content instanceof TdApi.MessageVideo) {

            String text;
            int fileId;
            int height, width ,type;
            if(message.content instanceof TdApi.MessageVideo)
            {
                text = ((TdApi.MessageVideo) message.content).caption.text;
                fileId = ((TdApi.MessageVideo) message.content).video.video.id;
                height = ((TdApi.MessageVideo) message.content).video.height;
                width = ((TdApi.MessageVideo) message.content).video.width;
                type = MediaInfo.TYPE_VIDEO;


            }
            else
            {
                type = MediaInfo.TYPE_IMAGE;
                text = ((TdApi.MessagePhoto) message.content).caption.text;
                fileId = ((TdApi.MessagePhoto) message.content).photo.sizes[
                        ((TdApi.MessagePhoto) message.content).photo.sizes.length - 1].photo.id;

                height = ((TdApi.MessagePhoto) message.content).photo.sizes[
                        ((TdApi.MessagePhoto) message.content).photo.sizes.length - 1].height;
               width = ((TdApi.MessagePhoto) message.content).photo.sizes[
                        ((TdApi.MessagePhoto) message.content).photo.sizes.length - 1].width;


            }


            TdApi.DownloadFile downloadFile = new TdApi.DownloadFile();
            downloadFile.fileId = fileId;
            downloadFile.priority = 32;
            downloadFile.synchronous = true;
            downloadFile.limit = 0;


            int finalViews = views;
            int finalComments = comments;

            //  Bitmap bitmap = getBitmap(((TdApi.File) object).local.path);


            ArrayList<MediaInfo> mediaDatas = new ArrayList<>();




            MediaInfo mediaInfo = new MediaInfo(fileId, height, width, type);
            mediaDatas.add(mediaInfo);
            NewsAdapter.Article article = new NewsAdapter.Article(mediaDatas, text,
                    supergroups.get(message.chatId).title, message.date, "0", message, finalViews, finalComments);
            article.setMessageId(message.id);
            article.setMessage(message);


            if (message.mediaAlbumId != 0) {
                NewsAdapter.Article articleAlbum = findArticleForGroup(message.mediaAlbumId);
                if (articleAlbum == null) {
                    articles.put(message.id, article);
                } else {
                    if (articleAlbum.getText().length() == 0) {
                        articleAlbum.setText(article.getText());
                        articleAlbum.setMessage(message);
                    }



                    articleAlbum.getMediaList().add(mediaInfo);

                    articles.remove(articleAlbum.getMessageId());
                    articles.put(articleAlbum.getMessageId(), articleAlbum);
                }
            } else {
                articles.put(message.id, article);
            }



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

                if (object instanceof TdApi.Chats) {
                    for (long chatId : ((TdApi.Chats) object).chatIds) {
                        Telegram.getClient().send(new TdApi.GetChat(chatId), new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.Object object) {

                                if (object instanceof TdApi.Chat) {


                                    TdApi.Chat chat = (TdApi.Chat) object;
                                    if (chat.type instanceof TdApi.ChatTypeSupergroup) {
                                        if (((TdApi.ChatTypeSupergroup) chat.type).isChannel) {


                                            TdApi.DownloadFile downloadAvatar = new TdApi.DownloadFile();
                                            if (chat.photo != null) {
                                                downloadAvatar.fileId = chat.photo.small.id;
                                                downloadAvatar.priority = 32;
                                                downloadAvatar.synchronous = true;
                                                downloadAvatar.limit = 0;
                                            }


                                            Telegram.getClient().send(downloadAvatar, new Client.ResultHandler() {
                                                @Override
                                                public void onResult(TdApi.Object object) {
                                                    if (object instanceof TdApi.File) {

                                                        Bitmap bitmap = ImageCropper.roundCrop(getBitmap(((TdApi.File) object).local.path));
                                                        supergroupsAvatars.put(chat.id, bitmap);
                                                    }
                                                }
                                            });

                                            supergroups.put(chat.id, chat);
                                            assert chat.lastMessage != null;
                                            System.out.println("Added channel " + chat.title);

                                            if(!MainActivity.ignoreChannels.contains(chatId))
                                            {
                                                updateArticlesForSupergroup(chat, chat.lastMessage.id);
                                                processMessageForArticle(chat.lastMessage);
                                            }

                                        }
                                    }
                                    else if(chat.type instanceof TdApi.ChatTypePrivate)
                                    {
                                        if(chat.id > 0 && !chat.isBlocked && chat.title.length() > 0) {
                                            if(!processedAvatars.contains(chat.id)) {
                                                MediaGetter.loadUserImage(((TdApi.ChatTypePrivate) chat.type).userId, new
                                                        MediaGetter.MediaResult() {
                                                            @Override
                                                            public int getType() {
                                                                return MediaInfo.TYPE_IMAGE;
                                                            }

                                                            @Override
                                                            public void onImageProcessed(Bitmap bitmap) {
                                                                ShareBottomSheet.avatars.put(chat,
                                                                        ImageCropper.roundCrop(bitmap));
                                                            }

                                                            @Override
                                                            public void onVideoProcessed(String path) {

                                                            }

                                                            @Override
                                                            public void onError() {
                                                                ShareBottomSheet.avatars.put(chat,
                                                                        null);
                                                            }
                                                        });
                                                processedAvatars.add(chat.id);
                                            }


                                        }
                                    }
                                }
                            }
                        });
                    }
                }

            }
        });

        if (!hasCallback) {
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
                                // article.setImagesList(bitmaps);
                                //articles.put(article.getMessageId(), article);
                            }

                        }


                    }
                }
            });

        }

    }

}
