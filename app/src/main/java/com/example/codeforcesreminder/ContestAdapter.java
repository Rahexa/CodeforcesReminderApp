package com.example.codeforcesreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ContestAdapter extends RecyclerView.Adapter<ContestAdapter.ViewHolder> {

    private final List<Contest> contestList;
    private final OnContestClickListener clickListener;

    public interface OnContestClickListener {
        void onContestClick(Contest contest);
    }

    public ContestAdapter(List<Contest> contests, OnContestClickListener listener) {
        this.contestList = contests;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contest, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contest contest = contestList.get(position);
        holder.contestName.setText(contest.getName());

        long millisLeft = contest.getStartTime() - System.currentTimeMillis();

        if (millisLeft > 0) {
            holder.countdown.setText(formatDuration(millisLeft));
        } else {
            holder.countdown.setText("Started");
        }

        holder.itemView.setOnClickListener(v -> clickListener.onContestClick(contest));
    }

    private String formatDuration(long millis) {
        long hrs = TimeUnit.MILLISECONDS.toHours(millis);
        long mins = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long secs = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        return String.format(Locale.getDefault(), "Starts in: %02d hr %02d min %02d sec", hrs, mins, secs);
    }

    @Override
    public int getItemCount() {
        return contestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView contestName, countdown;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            contestName = itemView.findViewById(R.id.contestName);
            countdown = itemView.findViewById(R.id.contestCountdown);
        }
    }
}
