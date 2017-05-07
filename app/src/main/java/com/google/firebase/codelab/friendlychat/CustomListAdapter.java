package com.google.firebase.codelab.friendlychat;

import android.app.Activity;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<ImageInfo> {

    private Activity context;
    private int resource;
    private ImageInfo[] imageInfoList;

    public CustomListAdapter(Activity context, int resource, List<ImageInfo> objects) {
        super(context, resource, objects);

        this.context = context;
        this.resource = resource;
        this.imageInfoList = new ImageInfo[objects.size()];
        objects.toArray(this.imageInfoList);
    }

    public View getView(int position, View view, ViewGroup parent) {
        ImageInfo info = getItem(position);

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(this.resource, null, true);

        ImageView messengerImageView = (ImageView) rowView.findViewById(R.id.messengerImageView);
        TextView messageTextView = (TextView) rowView.findViewById(R.id.messageTextView);
        ImageView messageImageView = (ImageView) rowView.findViewById(R.id.messageImageView);
        TextView messengerTextView = (TextView) rowView.findViewById(R.id.messengerTextView);

        Glide.with(context).load(info.photoUrl).into(messengerImageView);
        Glide.with(messageImageView.getContext()).load(info.imageUrl).into(messageImageView);
        messageTextView.setText(info.text);
        messengerTextView.setText(info.name);
        return rowView;
    };
}