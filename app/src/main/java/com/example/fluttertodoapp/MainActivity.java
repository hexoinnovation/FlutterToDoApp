package com.example.fluttertodoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnEditClickListener {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private EditText taskTitle;
    private EditText taskDescription;
    private Button addTaskButton;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private List<Task> filteredTaskList; // List for filtered tasks



    private boolean isEditing = false; // Track if editing is in progress
    private String currentTaskId; // Store the current task ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        taskTitle = findViewById(R.id.taskTitle);
        taskDescription = findViewById(R.id.taskDescription);
        addTaskButton = findViewById(R.id.addTaskButton);
        recyclerView = findViewById(R.id.recyclerView);
        SearchView searchView = findViewById(R.id.searchView); // Initialize SearchView

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        filteredTaskList = new ArrayList<>(); // Initialize filtered task list
        adapter = new TaskAdapter(taskList, this, this::deleteTaskFromFirestore); // Pass delete method
        recyclerView.setAdapter(adapter);

        // Load tasks from Firestore
        loadTasksFromFirestore();

        // Add task to Firestore or update task on button click
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = taskTitle.getText().toString();
                String description = taskDescription.getText().toString();

                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEditing) {
                    updateTaskInFirestore(currentTaskId, title, description); // Call update method
                    isEditing = false; // Reset editing state
                } else {
                    addTaskToFirestore();
                }
            }
        });
        // Set up SearchView query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTasks(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTasks(newText);
                return false;
            }
        });
    }



    private void loadTasksFromFirestore() {
        CollectionReference tasksCollection = db.collection("tasks");
        tasksCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                taskList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getId(); // Get the document ID
                    String title = document.getString("title");
                    String description = document.getString("description");
                    taskList.add(new Task(id, title, description)); // Include ID here
                }
                filteredTaskList.clear(); // Clear filtered list
                filteredTaskList.addAll(taskList); // Add all tasks to filtered list initially
                adapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });
    }
    private void searchTasks(String query) {
        filteredTaskList.clear(); // Clear the filtered list

        if (query.isEmpty()) {
            // If the search query is empty, display all tasks
            filteredTaskList.addAll(taskList);
        } else {
            // Search through the task titles
            for (Task task : taskList) {
                if (task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredTaskList.add(task);
                }
            }
        }

        // Notify the adapter that the data has changed
        if (filteredTaskList.isEmpty()) {
            Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged(); // Update the RecyclerView
    }
    private void addTaskToFirestore() {
        String title = taskTitle.getText().toString();
        String description = taskDescription.getText().toString();

        Map<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("description", description);

        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                    taskList.add(new Task(documentReference.getId(), title, description)); // Include the generated ID
                    filteredTaskList.add(new Task(documentReference.getId(), title, description)); // Also add to filtered list
                    adapter.notifyDataSetChanged();
                    taskTitle.setText("");
                    taskDescription.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error adding task", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding document", e);
                });
    }

    @Override
    public void onEditClick(Task task) {
        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());

        // Set the editing state and store the task ID
        isEditing = true;
        currentTaskId = task.getId(); // Get the task ID to edit
    }

    private void updateTaskInFirestore(String taskId, String newTitle, String newDescription) {
        DocumentReference taskRef = db.collection("tasks").document(taskId);
        Map<String, Object> updatedTask = new HashMap<>();
        updatedTask.put("title", newTitle);
        updatedTask.put("description", newDescription);

        taskRef.update(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                    loadTasksFromFirestore(); // Refresh the task list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error updating task", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error updating document", e);
                });
    }

    private void deleteTaskFromFirestore(String taskId) {
        DocumentReference taskRef = db.collection("tasks").document(taskId);
        taskRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                    loadTasksFromFirestore(); // Refresh the task list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting document", e);
                });
    }
}
