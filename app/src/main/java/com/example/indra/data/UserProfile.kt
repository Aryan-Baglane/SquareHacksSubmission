package com.example.indra.data

/**
 * Updated UserProfile to match the Firebase Realtime Database structure.
 * This prevents "No setter/field" warnings in logs.
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val name: String = "",
    val onboardingCompleted: Boolean = false,
    val numDwellers: Int = 0,
    val roofAreaSqm: Double = 0.0,
    val openSpaceSqm: Double = 0.0,
    val roofType: String = "Concrete",

    // Add these fields to match the Firebase keys found in your logs
    val reports: Map<String, Report>? = null,
    val graminProfile: GraminProfile? = null,
    val properties: Map<String, Property>? = null
) {
    // Primary default constructor for Firebase
    constructor() : this(uid = "")
}