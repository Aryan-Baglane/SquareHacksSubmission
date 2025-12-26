package com.example.indra.db


import com.example.indra.data.GraminProfile
import kotlinx.coroutines.tasks.await
import com.example.indra.data.UserProfile
import com.example.indra.data.Property
import com.example.indra.data.Report
import com.google.firebase.Firebase
import com.google.firebase.database.database

interface DatabaseApi {
    suspend fun getUserProfile(uid: String): UserProfile?
    suspend fun setUserProfile(profile: UserProfile)
    suspend fun getUserProperties(uid: String): List<Property>
    suspend fun addProperty(uid: String, property: Property)
    suspend fun updateProperty(uid: String, property: Property)
    suspend fun deleteProperty(uid: String, propertyId: String)
    
    // Report management functions
    suspend fun getUserReports(uid: String): List<Report>
    suspend fun addReport(uid: String, report: Report)
    suspend fun updateReport(uid: String, report: Report)
    suspend fun deleteReport(uid: String, reportId: String)
    suspend fun getReport(uid: String, reportId: String): Report?

    suspend fun getGraminProfile(uid: String): GraminProfile?
    suspend fun setGraminProfile(profile: GraminProfile)

}

private class FirebaseDatabaseApi : DatabaseApi {
    private val db = Firebase.database.reference

    override suspend fun getUserProfile(uid: String): UserProfile? {
        val snap = db.child("users").child(uid).get().await()
        return snap.getValue(UserProfile::class.java)
    }

    override suspend fun setUserProfile(profile: UserProfile) {
        db.child("users").child(profile.uid).setValue(profile).await()
    }

    override suspend fun getUserProperties(uid: String): List<Property> {
        val snap = db.child("users").child(uid).child("properties").get().await()
        val properties = mutableListOf<Property>()

        snap.children.forEach { child ->
            child.getValue(Property::class.java)?.let { property ->
                properties.add(property)
            }
        }
        return properties
    }

    override suspend fun addProperty(uid: String, property: Property) {
        db.child("users").child(uid).child("properties").child(property.id).setValue(property).await()
    }

    override suspend fun updateProperty(uid: String, property: Property) {
        db.child("users").child(uid).child("properties").child(property.id).setValue(property).await()
    }

    override suspend fun deleteProperty(uid: String, propertyId: String) {
        db.child("users").child(uid).child("properties").child(propertyId).removeValue().await()
    }

    // Report management implementations
    override suspend fun getUserReports(uid: String): List<Report> {
        val snap = db.child("users").child(uid).child("reports").get().await()
        val reports = mutableListOf<Report>()

        snap.children.forEach { child ->
            child.getValue(Report::class.java)?.let { report ->
                reports.add(report)
            }
        }
        return reports.sortedByDescending { it.timestamp }
    }

    override suspend fun addReport(uid: String, report: Report) {
        db.child("users").child(uid).child("reports").child(report.id).setValue(report).await()
    }

    override suspend fun updateReport(uid: String, report: Report) {
        db.child("users").child(uid).child("reports").child(report.id).setValue(report).await()
    }

    override suspend fun deleteReport(uid: String, reportId: String) {
        db.child("users").child(uid).child("reports").child(reportId).removeValue().await()
    }

    override suspend fun getReport(uid: String, reportId: String): Report? {
        val snap = db.child("users").child(uid).child("reports").child(reportId).get().await()
        return snap.getValue(Report::class.java)
    }

    override suspend fun getGraminProfile(uid: String): GraminProfile? {
        val snap = db.child("users").child(uid).child("graminProfile").get().await()
        return snap.getValue(GraminProfile::class.java)
    }

    override suspend fun setGraminProfile(profile: GraminProfile) {
        db.child("users")
            .child(profile.uid)
            .child("graminProfile")
            .setValue(profile)
            .await()
    }


}

object DatabaseProvider {
    fun database(): DatabaseApi = FirebaseDatabaseApi()
}

object DatabaseApiProvider {
    fun databaseApi(): DatabaseApi = FirebaseDatabaseApi()
}
