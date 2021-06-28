package com.lawanyuk.home.report.model

import com.lawanyuk.auth.model.User
import com.lawanyuk.util.prettyClassString
import com.lawanyuk.util.prettyString
import java.text.SimpleDateFormat
import java.util.*

data class Report(
    val additionalInformation: String? = null,
    val category: Category? = null,
    val date: Date? = null,
    val description: String? = null,
    val id: String? = null,
    val location: String? = null,
    val photoUrlList: List<String>? = null,
    val user: User? = null
) {
    companion object {
        val dateFormatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    }

    override fun toString() = """
        |Report {
        |    additionalInformation: ${additionalInformation ?: "_"}
        |    category: ${category ?: "_"}
        |    date: ${date ?: "_"}
        |    description: ${description ?: "_"}
        |    id: ${id ?: "_"}
        |    location: ${location ?: "_"}
        |    photoUrlList: ${photoUrlList?.prettyString(2) ?: "_"}
        |    user: ${user ?: "_"}
        |}""".prettyClassString()

    enum class Category {
        PUDDLE, TRASH, OTHER
    }
}
