package com.andonova.singit.adapters;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andonova.singit.databinding.SongItemBinding;
import com.andonova.singit.models.SongItem;

import java.io.IOException;
import java.util.List;


public class SongsRecyclerAdapter extends RecyclerView.Adapter<SongsRecyclerAdapter.ViewHolder> {

    List<SongItem> songsList;
    private final Context activity;

    public SongsRecyclerAdapter(List<SongItem> songsList, Context activity) {
        this.songsList = songsList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(SongItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.songName.setText(songsList.get(position).getSongName());
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public Context getContext() {
        return activity;
    }

    public SongItem getSongItem(int position) {
        return songsList.get(position);
    }

    public void updateList(List<SongItem> songList) {
        this.songsList = songList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        SongItem item = songsList.get(position);
        //TODO: delete the item (song) from the Firebase Storage
        songsList.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SongItemBinding binding;

        public ViewHolder(SongItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(view -> {
                SongItem songItem = getSongItem(getAdapterPosition());
                String songName = songItem.getSongName();
                Uri AudioUri = songItem.getSongHTTPurl();

                // TODO: open music player with the song:
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build());
                try {
                    mediaPlayer.setDataSource(activity, AudioUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // TODO: On long click promp a dialog for deleting the song
        }
    }
}
