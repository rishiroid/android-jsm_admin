package com.rishiz.jsmadmin.view.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.rishiz.jsmadmin.R
import com.rishiz.jsmadmin.adapter.AddProductImageAdapter
import com.rishiz.jsmadmin.databinding.FragmentAddProductBinding
import com.rishiz.jsmadmin.model.AddProductModel
import com.rishiz.jsmadmin.model.CategoryModel
import java.util.*
import kotlin.collections.ArrayList

class AddProductFragment : Fragment() {
    private lateinit var binding: FragmentAddProductBinding
    private lateinit var list:ArrayList<Uri>
    private lateinit var listImages:ArrayList<String>
    private lateinit var adapter: AddProductImageAdapter
    private lateinit var dialog: Dialog
    private  lateinit var categoryList:ArrayList<String>
    private var coverImage:Uri?=null
    private var coverImgUrl:String=""
    val pickMediaCover = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            coverImage = uri
            binding.productCoverImg.setImageURI(coverImage)
            binding.productCoverImg.visibility=VISIBLE
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
    val pickMediaProduct = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            list.add(uri)
            adapter.notifyDataSetChanged()
            binding.productCoverImg.setImageURI(coverImage)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentAddProductBinding.inflate(layoutInflater)
        list= ArrayList()
        listImages= ArrayList()
        dialog=Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        binding.selectCoverImg.setOnClickListener{
            pickMediaCover.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.productImgBtn.setOnClickListener{
            pickMediaProduct.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        adapter= AddProductImageAdapter(list)
        binding.productImgRecyclerView.adapter=adapter
        binding.addProductBtn.setOnClickListener {
            validateDate()
        }

        setProductCategory()
        return binding.root
    }

    private fun validateDate() {
        if(binding.productNameEdt.text.toString().isEmpty()){
            binding.productNameEdt.requestFocus()
            binding.productNameEdt.error="Empty"
        }else if(binding.productDescriptionEdt.text.toString().isEmpty()){
            binding.productDescriptionEdt.requestFocus()
            binding.productDescriptionEdt.error="Empty"
        }else if(binding.productMrpEdt.text.toString().isEmpty()){
            binding.productMrpEdt.requestFocus()
            binding.productMrpEdt.error="Empty"
        }else if(binding.productSpEdt.text.toString().isEmpty()){
            binding.productSpEdt.requestFocus()
            binding.productSpEdt.error="Empty"
        }else if(coverImage==null){
            Toast.makeText(requireContext(),"Please select cover image",Toast.LENGTH_SHORT).show()
        }else if(list.size<1){
            Toast.makeText(requireContext(),"Please select product image",Toast.LENGTH_SHORT).show()
        }else{
            uploadImage()
        }
    }

    private fun setProductCategory(){
        categoryList= ArrayList()
        Firebase.firestore.collection("categories").get()
            .addOnSuccessListener {
                categoryList.clear()
                for(doc in it.documents){
                    val data=doc.toObject(CategoryModel::class.java)
                    categoryList.add(data!!.cat!!)
                }
                categoryList.add(0,"Select Category")
                val arrayAdapter=ArrayAdapter(requireContext(), R.layout.dropdown_item_layout,categoryList)
                binding.productCategoryDropdown.adapter=arrayAdapter
            }
    }
    private fun uploadImage() {
        dialog.show()
        val fileName= UUID.randomUUID().toString()+".jpg"
        val refStorage= FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(coverImage!!).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { image ->
                coverImgUrl=image.toString()
                uploadProductImage()
            }
        }
            .addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong with storage",Toast.LENGTH_SHORT).show()
            }
    }
    private var i=0
    private fun uploadProductImage() {

        dialog.show()
        val fileName= UUID.randomUUID().toString()+".jpg"
        val refStorage= FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(list[i]).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { image ->
                listImages.add(image!!.toString())
               if(list.size==listImages.size){
                   storeDate()
               }else{
                   i+=1
                   uploadProductImage()
               }
            }
        }
            .addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong with storage",Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeDate() {
        val db=Firebase.firestore.collection("products")
        val key=db.document().id
        val data=AddProductModel(
            binding.productNameEdt.text.toString(),
            binding.productDescriptionEdt.text.toString(),
            coverImgUrl,
            categoryList[binding.productCategoryDropdown.selectedItemPosition],
            key,
            binding.productMrpEdt.text.toString(),
            binding.productSpEdt.text.toString(),
            listImages
        )
        db.document(key).set(data).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(requireContext(),"Product added",Toast.LENGTH_SHORT).show()
            binding.productNameEdt.text=null
        }
            .addOnCanceledListener {
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong",Toast.LENGTH_SHORT).show()
            }
    }
}