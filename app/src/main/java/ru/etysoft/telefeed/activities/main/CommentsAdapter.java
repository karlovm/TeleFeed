package ru.etysoft.telefeed.activities.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.etysoft.telefeed.R;

public class CommentsAdapter  extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    private LayoutInflater mInflater;
    private List<Comment> comments;
    private Activity activity;

    public CommentsAdapter(Activity context, List<Comment> data) {
        this.mInflater = LayoutInflater.from(context);
        this.comments = data;
        this.activity = context;
    }


    public static class Comment
    {
        private String text;
        private Bitmap profileImage;

        public String getText() {
            return text;
        }

        public Bitmap getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(Bitmap profileImage) {
            this.profileImage = profileImage;
        }

        public Comment(String text) {
            this.text = text;
        }
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.ViewHolder holder, int position) {
        holder.textView.setText(comments.get(position).text);
        if(comments.get(position).profileImage != null)
        {

            holder.avatar.setImageBitmap(comments.get(position).profileImage);
        }
        else
        {
            holder.avatar.setImageResource(R.drawable.ic_unknown);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        TextView textView;
        ImageView avatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.messageText);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }
}
