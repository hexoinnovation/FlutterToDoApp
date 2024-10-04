package com.example.fluttertodoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Task task);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(String taskId);
    }

    public TaskAdapter(List<Task> taskList, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.taskList = taskList;
        this.editClickListener = editListener;
        this.deleteClickListener = deleteListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitleView.setText(task.getTitle());
        holder.taskDescriptionView.setText(task.getDescription());

        holder.editButton.setOnClickListener(v -> editClickListener.onEditClick(task));
        holder.deleteButton.setOnClickListener(v -> deleteClickListener.onDeleteClick(task.getId())); // Pass the task ID to delete
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitleView;
        TextView taskDescriptionView;
        ImageButton editButton;
        ImageButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitleView = itemView.findViewById(R.id.taskTitleView);
            taskDescriptionView = itemView.findViewById(R.id.taskDescriptionView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
