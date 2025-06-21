package com.example.codeforcesreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContestAdapter extends RecyclerView.Adapter<ContestAdapter.ContestViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Contest contest);
    }

    private List<Contest> contests;
    private OnItemClickListener listener;

    public ContestAdapter(List<Contest> contests, OnItemClickListener listener) {
        this.contests = contests;
        this.listener = listener;
    }

    @Override
    public ContestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contest_item, parent, false);
        return new ContestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContestViewHolder holder, int position) {
        Contest contest = contests.get(position);
        holder.bind(contest, listener);
    }

    @Override
    public int getItemCount() {
        return contests.size();
    }

    static class ContestViewHolder extends RecyclerView.ViewHolder {

        TextView contestNameTextView;
        TextView countdownTextView;

        public ContestViewHolder(View itemView) {
            super(itemView);
            contestNameTextView = itemView.findViewById(R.id.contestNameTextView);
            countdownTextView = itemView.findViewById(R.id.countdownTextView);
        }

        public void bind(Contest contest, OnItemClickListener listener) {
            contestNameTextView.setText(contest.getName());

            long diffMillis = contest.getStartTime() - System.currentTimeMillis();
            if (diffMillis > 0) {
                countdownTextView.setText(formatMillis(diffMillis));
            } else {
                countdownTextView.setText("Started");
            }

            itemView.setOnClickListener(v -> listener.onItemClick(contest));
        }

        private String formatMillis(long millis) {
            long seconds = millis / 1000;
            long hrs = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            long secs = seconds % 60;
            return String.format("Starts in: %02d:%02d:%02d", hrs, mins, secs);
        }
    }
}
