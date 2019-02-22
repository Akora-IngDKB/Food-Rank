package org.akoraingdkb.foodorder

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FoodItem (
        var name: String,
        var price: String,
        var rating: Int,
        var imageUrl: String)