package com.example.pulsemindai

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsemindai.data.AppDatabase
import com.example.pulsemindai.data.EmergencyContact
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SOSActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)

        db = AppDatabase.getDatabase(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSOSLarge = findViewById<ConstraintLayout>(R.id.btnSOSLarge)
        val pulseView = findViewById<View>(R.id.pulseView)
        val btnAddContact = findViewById<TextView>(R.id.btnAddContact)
        val rvContacts = findViewById<RecyclerView>(R.id.rvContacts)

        btnBack.setOnClickListener { finish() }

        btnSOSLarge.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.pulse)
            pulseView.visibility = View.VISIBLE
            pulseView.startAnimation(animation)

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:911")
            startActivity(intent)
            Toast.makeText(this, "Initiating Emergency Call...", Toast.LENGTH_SHORT).show()
        }

        btnAddContact.setOnClickListener { showAddContactDialog() }

        // Setup RecyclerView
        adapter = ContactAdapter(
            onCall = { number ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$number")
                startActivity(intent)
            },
            onDelete = { contact ->
                lifecycleScope.launch {
                    db.emergencyContactDao().deleteContact(contact)
                    Toast.makeText(this@SOSActivity, "Contact deleted", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvContacts.layoutManager = LinearLayoutManager(this)
        rvContacts.adapter = adapter

        // Observe data
        lifecycleScope.launch {
            db.emergencyContactDao().getAllContacts().collect { contacts ->
                adapter.submitList(contacts)
            }
        }
    }

    private fun showAddContactDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_contact)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etName = dialog.findViewById<EditText>(R.id.etContactName)
        val etPhone = dialog.findViewById<EditText>(R.id.etContactPhone)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnAdd.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                lifecycleScope.launch {
                    db.emergencyContactDao().insertContact(EmergencyContact(name = name, phoneNumber = phone))
                    dialog.dismiss()
                    Toast.makeText(this@SOSActivity, "Contact added", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Simple Adapter within the same file for convenience in this setup
    class ContactAdapter(
        private val onCall: (String) -> Unit,
        private val onDelete: (EmergencyContact) -> Unit
    ) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

        private var contacts = listOf<EmergencyContact>()

        fun submitList(newList: List<EmergencyContact>) {
            contacts = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sos_contact, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contact = contacts[position]
            holder.tvName.text = contact.name
            holder.tvPhone.text = contact.phoneNumber
            holder.btnCall.setOnClickListener { onCall(contact.phoneNumber) }
            holder.btnDelete.setOnClickListener { onDelete(contact) }
        }

        override fun getItemCount() = contacts.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvContactName)
            val tvPhone: TextView = view.findViewById(R.id.tvContactNumber)
            val btnCall: ImageButton = view.findViewById(R.id.btnCallContact)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteContact)
        }
    }
}
