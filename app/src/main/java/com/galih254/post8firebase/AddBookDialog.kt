package com.galih254.post8firebase

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.galih254.post8firebase.databinding.DialogTaskBinding // Gunakan Binding yang Benar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Class ini menangani Logika Dialog Tambah/Edit
class AddBookDialog(
    private val context: Context,
    private val tasksRef: DatabaseReference, // Ubah nama variabel jadi tasksRef biar tidak bingung
    private val onSaved: (() -> Unit)? = null
) {

    companion object {
        private const val TAG = "TaskDialog"
    }

    // Ubah parameter dari Book menjadi Task
    fun show(existing: Task? = null, nodeKey: String? = null) {
        Log.d(TAG, "📝 Dialog opened - Mode: ${if (existing == null) "ADD" else "EDIT"}")

        // 1. Gunakan Layout Dialog yang benar (dialog_task.xml)
        val binding = DialogTaskBinding.inflate(LayoutInflater.from(context))

        // Variabel untuk menyimpan tanggal yang dipilih
        var selectedDateString: String? = null

        // 2. Isi Data Jika Mode Edit (Pre-fill)
        existing?.let {
            Log.d(TAG, "Editing task: $it")
            binding.etTitle.setText(it.title)
            binding.etDescription.setText(it.description)

            // Set tanggal jika ada
            if (!it.date.isNullOrEmpty()) {
                selectedDateString = it.date
                binding.tvDateResult.text = it.date
            }
        }

        // 3. Logika Date Picker (Kalender)
        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dp = DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                    // Format tanggal
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    selectedDateString = dateFormat.format(selectedCalendar.time)

                    // Tampilkan ke TextView
                    binding.tvDateResult.text = selectedDateString
                    Log.d(TAG, "Date selected: $selectedDateString")
                },
                year, month, day
            )
            // dp.datePicker.minDate = System.currentTimeMillis() // Opsional: Batasi tanggal min hari ini
            dp.show()
        }

        // 4. Bangun Alert Dialog
        val dialogBuilder = MaterialAlertDialogBuilder(context)
            .setTitle(if (existing == null) "Tugas Baru" else "Edit Tugas")
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton("Simpan", null) // Set null dulu agar validasi bisa berjalan manual
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialogBuilder.show()

        // 5. Override Tombol Simpan (Agar bisa validasi input sebelum tutup dialog)
        dialogBuilder.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()

            // Validasi: Judul Wajib Diisi
            if (title.isEmpty()) {
                binding.etTitle.error = "Judul tugas wajib diisi!"
                return@setOnClickListener
            }

            // Validasi: Tanggal Wajib Diisi (Opsional, jika mau wajib hapus komentar ini)
            /*
            if (selectedDateString == null) {
                Toast.makeText(context, "Mohon pilih tanggal!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            */

            if (nodeKey == null) {
                // === TAMBAH DATA BARU ===
                Log.d(TAG, "➕ Adding new task...")
                val node = tasksRef.push() // Buat ID baru
                val generatedKey = node.key

                val newTask = Task(
                    id = generatedKey,
                    title = title,
                    description = desc,
                    date = selectedDateString,
                    isDone = false
                )

                node.setValue(newTask)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Task added successfully!")
                        Toast.makeText(context, "Tugas berhasil disimpan", Toast.LENGTH_SHORT).show()
                        onSaved?.invoke()
                        dialogBuilder.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed: ${e.message}")
                        Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            } else {
                // === UPDATE DATA LAMA ===
                Log.d(TAG, "✏️ Updating task key: $nodeKey")

                // Update hanya field yang berubah
                val updatedData = mapOf(
                    "title" to title,
                    "description" to desc,
                    "date" to selectedDateString
                    // isDone tidak diubah di sini
                )

                tasksRef.child(nodeKey).updateChildren(updatedData)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Task updated!")
                        Toast.makeText(context, "Tugas diperbarui", Toast.LENGTH_SHORT).show()
                        onSaved?.invoke()
                        dialogBuilder.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed update: ${e.message}")
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}