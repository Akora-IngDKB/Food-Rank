package org.akoraingdkb.foodorder

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.appcompat.widget.AppCompatRatingBar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class BottomSheetFragment : BottomSheetDialogFragment(){

    private lateinit var nameText: TextView
    private lateinit var priceText: TextView
    private lateinit var ratingBar: AppCompatRatingBar
    private lateinit var cartButton: Button

    var listener: OnAddToCartBtnClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.bottom_sheet, container, false)
        nameText = view.findViewById(R.id.product_name)
        priceText = view.findViewById(R.id.product_price)
        ratingBar = view.findViewById(R.id.product_rating)
        cartButton = view.findViewById(R.id.btn_add_to_cart)

        // Set on click listener for the "Add to Cart" button
        cartButton.setOnClickListener {
            ++FoodAdapter.count
            listener?.onAddToCartBtnClick()
            this.dismiss()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the current object (which has been clicked)
        val obj = MainActivity.currentFoodItem
        // Configure the cedi symbol
        val cedi = 0xA2
        val price = "GH" + Character.toString(cedi.toChar())
        // Feed the info into the UI
        nameText.text = obj.name
        priceText.text = "$price ${obj.price}"
        ratingBar.rating = obj.rating.toFloat()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the host activity implements the onclick listener
        listener = context as? OnAddToCartBtnClickListener
        if (listener == null) {
            throw ClassCastException("$context must implement OnArticleSelectedListener")
        }

    }

    // Set the theme (style resource) for the bottom sheet dialog
    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    // Container Activity must implement this interface
    interface OnAddToCartBtnClickListener {
        fun onAddToCartBtnClick()
    }

}// Required empty public constructor
