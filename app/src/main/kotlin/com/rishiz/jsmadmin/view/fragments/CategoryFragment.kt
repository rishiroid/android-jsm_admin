package com.rishiz.jsmadmin.view.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts


import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.rishiz.jsmadmin.R
import com.rishiz.jsmadmin.adapter.CategoryAdapter
import com.rishiz.jsmadmin.databinding.FragmentCategoryBinding
import com.rishiz.jsmadmin.model.CategoryModel
import java.util.EventListener
import java.util.UUID


class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private var imageUrl: Uri? = null
    private lateinit var dialog: Dialog
    private var launchGalleryActivity=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode==Activity.RESULT_OK) {
            imageUrl= it.data?.data
            binding.imageView.setImageURI(imageUrl)
        }
    }
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            imageUrl= uri
            binding.imageView.setImageURI(imageUrl)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryBinding.inflate(layoutInflater)
        binding.apply {
            imageView.setOnClickListener {
//                val intent=Intent("android.intent.action.GET_ACTION")
//                intent.type="image/*"
//                Log.d("CategoryFragment","in intent")
//                launchGalleryActivity.launch(intent)
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            btnUpload.setOnClickListener {
                validateData(binding.categoryName.text.toString())
            }
        }
        dialog=Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        return binding.root
    }

    private fun validateData(categoryName: String) {
        if(categoryName.isEmpty()){
            Toast.makeText(requireContext(),"Please provide category Name",Toast.LENGTH_SHORT).show()
        }else if(imageUrl==null){
            Toast.makeText(requireContext(),"Please select image",Toast.LENGTH_SHORT).show()
        }
        else{
            uploadImage(categoryName)
        }
    }

    private fun uploadImage(categoryName: String) {
        dialog.show()
        val fileName=UUID.randomUUID().toString()+".jpg"
        val refStorage=FirebaseStorage.getInstance().reference.child("category/$fileName")
        refStorage.putFile(imageUrl!!).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { image ->
                storeData(categoryName,image.toString())
            }
        }
            .addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong with storage",Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(categoryName: String, url: String) {
            val db=Firebase.firestore
            val data= hashMapOf<String,Any>(
                "Category" to categoryName,
                "Url"  to url
            )
           db.collection("categories").add(data)
               .addOnSuccessListener {
                   dialog.dismiss()
                   Toast.makeText(requireContext(),"Category add successfully",Toast.LENGTH_SHORT).show()
                   binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_image_24))
                   binding.categoryName.text=null
                   getData()
               }
               .addOnFailureListener{
                   dialog.dismiss()
                   Toast.makeText(requireContext(),"Something went wrong with storage",Toast.LENGTH_SHORT).show()
               }
    }

    private fun getData() {
        val list=ArrayList<CategoryModel>()
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
//                    val data=doc.toObject(CategoryModel::class.java)
//                    list.add(data!!)
                    list.add(doc.toObject(CategoryModel::class.java)!!)
                    Log.d("db","value"+list)
                }
                binding.recyclerView.adapter=CategoryAdapter(requireContext(),list)
            }

    }
}