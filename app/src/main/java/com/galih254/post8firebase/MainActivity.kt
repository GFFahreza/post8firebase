package com.galih254.post8firebase

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.galih254.post8firebase.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tasksRef: DatabaseReference // Ganti nama variabel biar tidak bingung
    private lateinit var adapter: TaskAdapter // Pastikan pakai TaskAdapter
    private val taskList = mutableListOf<Task>() // Gunakan List Task

    // ... companion object ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. PENTING: Pastikan Path ini sama dengan path saat Simpan Data
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks")

        setupAdapter()
        setupRecyclerView()

        binding.fabAddBooks.setOnClickListener {
            // Pastikan Dialog menulis ke referensi yang sama
            AddBookDialog(this, tasksRef) {
                // Callback
            }.show()
        }

        fetchData()
    }

    // ... setupAdapter dan setupRecyclerView sesuaikan dengan TaskAdapter ...

    private fun fetchData() {
        tasksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskList.clear()

                // Debugging: Cek apakah snapshot ada isinya
                if (!snapshot.exists()) {
                    Log.d(TAG, "Snapshot kosong! Cek path database.")
                    binding.emptyState.visibility = android.view.View.VISIBLE
                    return
                }

                for (child in snapshot.children) {
                    try {
                        // 2. PENTING: Gunakan class Task, bukan Book
                        val task = child.getValue(Task::class.java)

                        if (task != null) {
                            task.id = child.key // Simpan Key Firebase ke ID object
                            taskList.add(task)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Gagal parsing: ${e.message}")
                    }
                }

                // Update UI
                adapter.updateList(taskList) // Pastikan adapter punya fungsi ini

                // Toggle Empty State
                binding.emptyState.visibility =
                    if (taskList.isEmpty()) android.view.View.VISIBLE
                    else android.view.View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}