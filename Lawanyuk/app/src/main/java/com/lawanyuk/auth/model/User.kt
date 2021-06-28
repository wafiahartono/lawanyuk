package com.lawanyuk.auth.model

import com.lawanyuk.util.prettyClassString

data class User(
    val emailAddress: String? = null,
    val fullName: String? = null,
    val homeAddress: String? = null,
    val id: String? = null,
    val nik: String? = null,
    val password: String? = null,
    val phoneNumber: String? = null,
    val position: String? = null,
    val profilePictureUrl: String? = null
) {
    override fun toString() = """
        |User {
        |    emailAddress: ${emailAddress ?: "_"}
        |    fullName: ${fullName ?: "_"}
        |    homeAddress: ${homeAddress ?: "_"}
        |    id: ${id ?: "_"}
        |    nik: ${nik ?: "_"}
        |    password: ${password ?: "_"}
        |    phoneNumber: ${phoneNumber ?: "_"}
        |    password: ${password ?: "_"}
        |    profilePictureUrl: ${profilePictureUrl ?: "_"}
        |}""".prettyClassString()
}
