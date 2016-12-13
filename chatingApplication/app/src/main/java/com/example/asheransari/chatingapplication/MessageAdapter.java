package com.example.asheransari.chatingapplication;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;

import java.util.List;

/**
 * Created by asher.ansari on 12/7/2016.
 */

public class MessageAdapter extends ArrayAdapter<FriendlyMessage>{

    public MessageAdapter(Context context, int resource, List<FriendlyMessage> list)
    {
        super(context,0,list);
    }

    @Override
    public View getView(int position, View converView,ViewGroup parent)
    {
        if (converView == null)
        {
            converView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.list_message,parent,false);
        }

        ImageView photoImageVIew = (ImageView) converView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView)converView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView)converView.findViewById(R.id.nameTextView);

        FriendlyMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;

        if (isPhoto)
        {
            messageTextView.setVisibility(View.GONE);
            photoImageVIew.setVisibility(View.VISIBLE);
            Glide.with(photoImageVIew.getContext()).load(message.getPhotoUrl()).into(photoImageVIew);
        }
        else{
            messageTextView.setVisibility(View.VISIBLE);
            photoImageVIew.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());

        return converView;
    }

}
