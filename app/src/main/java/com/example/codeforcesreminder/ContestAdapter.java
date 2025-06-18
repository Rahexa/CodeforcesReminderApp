package com.example.codeforcesreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ContestAdapter extends RecyclerView.Adapter<ContestAdapter.ViewHolder> {
    private List<Contest> contests;
    private OnContestClickListener clickListener;
    private OnScheduleReminderListener reminderListener;

    public interface OnContestClickListener {
        void onContestClick(Contest contest);
    }

    public interface OnScheduleReminderListener {
        void onScheduleReminder(Contest contest, long reminderTime);
    }

    public ContestAdapter(List<Contest> contests, OnContestClickListener clickListener, OnScheduleReminderListener reminderListener) {
        this.contests = contests;
        this.clickListener = clickListener;
        this.reminderListener = reminderListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contest contest = contests.get(position);
        holder.contestName.setText(contest.getName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.contestTime.setText(sdf.format(contest.getStartTime()));
        holder.itemView.setOnClickListener(v -> clickListener.onContestClick(contest));
        // Reminder buttons removed
    }

    @Override
    public int getItemCount() {
        return contests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView contestName, contestTime;

        ViewHolder(View itemView) {
            super(itemView);
            contestName = itemView.findViewById(R.id.contestName);
            contestTime = itemView.findViewById(R.id.contestTime);
        }
    }
}
