package com.galih254.post8firebase

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.galih254.post8firebase.databinding.ItemTaskBinding // Pastikan menggunakan ItemTaskBinding (bukan Book)

class TaskAdapter(
    // 1. Ubah List Book menjadi List Task
    private val tasks: MutableList<Task>,
    // 2. Ubah Callback agar mengirim object Task
    private val onDelete: (Task, Int) -> Unit,
    private val onEdit: (Task, Int) -> Unit,
    private val onToggleDone: (Task, Int, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    fun updateList(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    // Ubah nama ViewHolder
    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task, position: Int) {
            // 3. Masukkan data dari Task ke Tampilan
            binding.tvTaskTitle.text = task.title

            // Cek jika deskripsi kosong, sembunyikan TextView-nya
            if (task.description.isNullOrEmpty()) {
                binding.tvTaskDescription.visibility = View.GONE
            } else {
                binding.tvTaskDescription.visibility = View.VISIBLE
                binding.tvTaskDescription.text = task.description
            }

            // Cek tanggal
            binding.tvTaskDate.text = task.date ?: "-"

            val isDone = task.isDone

            // 4. Logika Visual (Selesai / Belum)
            if (isDone) {
                // Jika selesai: Transparan & Dicoret
                binding.root.alpha = 0.5f
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                // Jika belum: Jelas & Tidak Dicoret
                binding.root.alpha = 1f
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // 5. Checkbox Listener
            // Penting: Set listener ke null dulu sebelum setChecked agar tidak loop/bug saat scroll
            binding.cbTaskComplete.setOnCheckedChangeListener(null)
            binding.cbTaskComplete.isChecked = isDone

            binding.cbTaskComplete.setOnCheckedChangeListener { _, isChecked ->
                onToggleDone(task, position, isChecked)
            }

            // 6. Tombol Hapus
            binding.btnDeleteTask.setOnClickListener {
                onDelete(task, position)
            }

            // 7. Klik pada item untuk Edit
            binding.root.setOnClickListener {
                onEdit(task, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Inflate layout item_task.xml
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], position)
    }

    override fun getItemCount() = tasks.size
}