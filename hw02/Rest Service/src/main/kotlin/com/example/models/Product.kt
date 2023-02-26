package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(val id: Int, val name: String, val description: String)

@Serializable
data class ProductBody(val name: String, val description: String)