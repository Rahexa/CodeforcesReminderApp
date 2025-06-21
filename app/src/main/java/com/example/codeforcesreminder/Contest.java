package com.example.codeforcesreminder;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ContestAdapter extends RecyclerView.Adapter<ContestAdapter.ViewHolder> {

    private List<Contest> contests;
    private OnContestClickListener listener;

    public interface OnContestClickListener {
        void onContestClick(Contest contest);
    }

    public ContestAdapter(List<Contest> contests, OnContestClickListener listener) {
        this.contests = contests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contest, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContestAdapter.ViewHolder holder, int position) {
        Contest contest = contests.get(position);
        holder.contestName.setText(contest.getName());
        holder.contestTime.setText(android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", contest.getStartTime()));

        long millisUntilStart = contest.getStartTime() - System.currentTimeMillis();
        if (millisUntilStart > 0) {
            holder.countdownTimer.setVisibility(View.VISIBLE);

            // Cancel any existing timer
            if (holder.countDownTimerInstance != null) {
                holder.countDownTimerInstance.cancel();
            }

            holder.countDownTimerInstance = new CountDownTimer(millisUntilStart, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long hrs = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    long mins = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                    long secs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                    String time = String.format("Starts in: %02d hr %02d min %02d sec", hrs, mins, secs);
                    holder.countdownTimer.setText(time);
                }

                @Override
                public void onFinish() {
                    holder.countdownTimer.setText("Contest started");
                }
            }.start();
        } else {
            holder.countdownTimer.setText("Contest started");
            holder.countdownTimer.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> listener.onContestClick(contest));
    }

    @Override
    public int getItemCount() {
        return contests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView contestName, contestTime, countdownTimer;
        CountDownTimer countDownTimerInstance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contestName = itemView.findViewById(R.id.contestName);
            contestTime = itemView.findViewById(R.id.contestTime);
            countdownTimer = itemView.findViewById(R.id.countdownTimer);
        }
    }
}
