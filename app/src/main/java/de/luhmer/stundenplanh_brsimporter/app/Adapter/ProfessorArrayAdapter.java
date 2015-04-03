package de.luhmer.stundenplanh_brsimporter.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Helper.CircleTransform;
import de.luhmer.stundenplanh_brsimporter.app.Model.ProfessorEntry;
import de.luhmer.stundenplanh_brsimporter.app.R;

/**
 * Created by David on 05.07.2014.
 */
public class ProfessorArrayAdapter extends ArrayAdapter<ProfessorEntry> {
    private final String TAG = getClass().getCanonicalName();

    //Integer paintTvDate;
    //Integer paintTvDesc;

    LayoutInflater inflater;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public ProfessorArrayAdapter(Context context, int textViewResourceId, List<ProfessorEntry> items) {
        super(context, textViewResourceId, items);
        inflater = ((Activity) getContext()).getLayoutInflater();
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.prof_list_item, parent, false);
            viewHolder = new ViewHolderItem(convertView);
            convertView.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // object item based on the position
        ProfessorEntry entry = getItem(position);

        // assign values if the object is not null
        if (entry != null) {
            viewHolder.tvName.setText(entry.mName);
            viewHolder.tvRoom.setText(entry.mRoom);

            //Log.v(TAG, entry.mThumbUrl);

            if(!entry.mThumbUrl.isEmpty()) {
                Picasso.with(getContext()).load(entry.mThumbUrl).transform(new CircleTransform()).into(viewHolder.imgThumb);
            } else {
                Picasso.with(getContext()).load("https://cdn0.vox-cdn.com/images/verge/default-avatar.v9899025.gif").transform(new CircleTransform()).into(viewHolder.imgThumb);
            }
        }

        return convertView;
    }

    // our ViewHolder.
    // caches our TextView
    static class ViewHolderItem {
        @InjectView(R.id.tv_prof_name) TextView tvName;
        @InjectView(R.id.tv_prof_room) TextView tvRoom;
        @InjectView(R.id.img_thumb) ImageView imgThumb;

        public ViewHolderItem(View view) {
            ButterKnife.inject(this, view);
        }

    }
}