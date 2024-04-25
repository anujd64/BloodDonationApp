package com.example.blooddonationapp.ui.home.posts

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot

class PaginationHelper {
    companion object {
        private const val PAGE_SIZE = 100 // Number of items to fetch per page
    }

    private var lastDocument: String? = null
    private var isLoading = false

    fun isLoading(): Boolean {
        return isLoading
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
    }

    fun getNextQuery(currentQuery: Query): Query {
        return if (lastDocument == null) {
            currentQuery.limit(PAGE_SIZE.toLong())
        } else {
            Log.d(
                "PaginationHelper",
                "Fetching next page starting after document with ID: $lastDocument"

            )
            currentQuery.startAfter(lastDocument)
                .limit(PAGE_SIZE.toLong())
        }
    }

    fun updateLastDocument(document: String) {
        this.lastDocument = document
    }
}
