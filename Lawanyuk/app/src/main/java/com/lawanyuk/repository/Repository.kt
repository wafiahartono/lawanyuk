package com.lawanyuk.repository

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.lawanyuk.auth.model.User
import com.lawanyuk.home.report.model.Report
import com.lawanyuk.util.logd
import com.lawanyuk.util.prettyString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object Repository {
    enum class AuthState {
        ANONYMOUS, AUTHENTICATED, UNAUTHENTICATED
    }

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        Firebase.auth.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        Firebase.auth.removeAuthStateListener(listener)
    }

    fun getAuthState(): AuthState {
        val firebaseUser = Firebase.auth.currentUser
        return when {
            firebaseUser == null -> AuthState.UNAUTHENTICATED
            firebaseUser.isAnonymous -> AuthState.ANONYMOUS
            else -> AuthState.AUTHENTICATED
        }
    }

    suspend fun isUserFullyRegistered(): Boolean {
        return Firebase.firestore
            .collection("users")
            .document(Firebase.auth.currentUser?.uid ?: throw IllegalStateException())
            .get().await()
            .exists()
    }

    suspend fun signUp(user: User, googleAccount: Boolean) {
        logd("user : $user, parsed : ${Uri.parse(user.profilePictureUrl)}")
        if (!googleAccount)
            Firebase.auth.createUserWithEmailAndPassword(user.emailAddress!!, user.password!!)
                .await()
        val firebaseUser = Firebase.auth.currentUser ?: throw IllegalStateException()
        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(user.profilePictureUrl))
                .build()
        ).await()
        Firebase.firestore
            .collection("users")
            .document(firebaseUser.uid)
            .set(
                hashMapOf(
                    "full_name" to user.fullName,
                    "home_address" to user.homeAddress,
                    "nik" to user.nik,
                    "phone_number" to user.phoneNumber,
                    "position" to user.position,
                    "profile_picture_url" to user.profilePictureUrl
                )
            )
            .await()
    }

    suspend fun signInWithGoogle(token: String) {
        Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
    }

    fun getUserGoogleAccountData(): Map<String, String>? {
        val firebaseUser = Firebase.auth.currentUser ?: throw IllegalStateException()
        val userInfo = firebaseUser.providerData.find {
            it?.providerId == GoogleAuthProvider.PROVIDER_ID
        }
        return if (userInfo == null) null
        else mapOf(
            "emailAddress" to userInfo.email!!,
            "displayName" to userInfo.displayName!!
        )
    }

    suspend fun signInAnonymously() {
        Firebase.auth.signInAnonymously().await()
    }

    suspend fun signIn(user: User) {
        Firebase.auth.signInWithEmailAndPassword(user.emailAddress!!, user.password!!).await()
    }

    fun signOut(googleSignInClient: GoogleSignInClient?) {
        Firebase.auth.signOut()
        googleSignInClient?.signOut()
    }

    suspend fun getUser(): User {
        val firebaseUser = Firebase.auth.currentUser ?: throw IllegalStateException()
        val snapshot = Firebase.firestore
            .collection("users")
            .document(firebaseUser.uid)
            .get().await()
        return User(
            emailAddress = firebaseUser.email,
            fullName = snapshot.getString("full_name"),
            homeAddress = snapshot.getString("home_address"),
            id = firebaseUser.uid,
            nik = snapshot.getString("nik"),
            phoneNumber = snapshot.getString("phone"),
            position = snapshot.getString("position"),
            profilePictureUrl = firebaseUser.photoUrl?.toString()
        )
    }

    suspend fun createReport(report: Report, photoFileNameList: List<String>) {
        logd("createReport: report: \n$report\n\nphotoNameList: ${photoFileNameList.prettyString()}\n")
        val ref = Firebase.firestore.collection("reports").document()
        ref.set(
            hashMapOf(
                "additional_information" to report.additionalInformation,
                "category" to report.category,
                "date" to Timestamp(report.date!!),
                "description" to report.description,
                "location" to report.location,
                "user" to hashMapOf(
                    "id" to Firebase.auth.currentUser!!.uid
                )
            )
        ).await()
        withContext(Dispatchers.IO) {
            report.photoUrlList!!.mapIndexed { i, s ->
                async {
                    getStorageReference()
                        .child("reports/${ref.id}/photos")
                        .child(photoFileNameList[i])
                        .putFile(Uri.parse(s))
                }
            }
        }.awaitAll()
    }

    suspend fun getReportList(): List<Report> {
        return Firebase.firestore
            .collection("reports")
            .get().await()
            .documents.map { snapshot ->
                Report(
                    additionalInformation = snapshot.getString("additional_information"),
                    category = Report.Category.valueOf(snapshot.getString("category")!!),
                    date = snapshot.getDate("date"),
                    description = snapshot.getString("description"),
                    location = snapshot.getString("location"),
                    photoUrlList = (snapshot.get("photo_urls") as List<*>).map { url -> url.toString() },
                    user = (snapshot.get("user") as Map<*, *>).entries.associate { entry ->
                        Pair(entry.key.toString(), entry.value.toString())
                    }.let { map ->
                        User(
                            fullName = map["full_name"],
                            id = map["id"]
                        )
                    }
                )
            }
    }

    fun getStorageReference() = Firebase.storage.reference
}
