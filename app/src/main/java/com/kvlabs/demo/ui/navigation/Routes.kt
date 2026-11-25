package com.kvlabs.demo.ui.navigation

import kotlinx.serialization.Serializable

/**
 * ARCHITECTURAL RATIONALE: Type-Safe Navigation (Navigation Compose 2.8+)
 *
 * FREQUENT INTERVIEW QUESTION:
 * "Why are we using `@Serializable` instead of `@Parcelize / Parcelable` since Parcelable
 * is Android-specific and Serialization is typically known as Java-specific?"
 *
 * ANSWER & DESIGN DISTINCTIONS:
 *
 * 1. Crucial Distinction: `kotlinx.serialization` != `java.io.Serializable`
 *    - Java's `java.io.Serializable`: Uses slow runtime reflection, generates excessive garbage collection
 *      allocations, and was historically discouraged in Android in favor of `android.os.Parcelable`.
 *    - Kotlin's `kotlinx.serialization.Serializable`: This is a Kotlin compiler plugin (`org.jetbrains.kotlin.plugin.serialization`).
 *      It generates serializers at COMPILE TIME with zero reflection. Its runtime performance is just as fast
 *      as Parcelable.
 *
 * 2. Navigation is Fundamentally URL & Deep Link Based:
 *    - Android Jetpack Navigation maps every destination to a URI schema under the hood (e.g. `route/{id}` or `route?query=val`).
 *    - `kotlinx.serialization` can serialize and deserialize Kotlin properties directly to/from human-readable
 *      URL path segments, query parameters, and bundle arguments.
 *    - `android.os.Parcelable` CANNOT do this: Parcelable is a proprietary binary format tailored specifically
 *      for Android Linux Binder IPC shared memory. It has no mechanism to build or parse string-based web URLs
 *      or deep link schemas.
 *
 * 3. Compile-Time Route & Argument Safety:
 *    - Legacy Compose Navigation used fragile formatted strings (e.g. `"detail/{id}"`), where a typo or missing
 *      argument triggered runtime crashes.
 *    - With `@Serializable` data classes/objects, destinations and arguments are validated at compile time.
 *      Navigation Compose retrieves typed arguments directly via `toRoute<AddEditAssignmentRoute>()`.
 *
 * 4. Multiplatform / Separation from Android OS:
 *    - `Parcelable` is coupled to `android.os.*`. Using `kotlinx.serialization` keeps navigation route contracts
 *      as pure Kotlin, making them 100% compatible with Compose Multiplatform (Android, Desktop, iOS, Web).
 */

sealed interface ScreenRoute

@Serializable
data object DashboardRoute : ScreenRoute

@Serializable
data class AddEditAssignmentRoute(
    val assignmentId: Long? = null
) : ScreenRoute

@Serializable
data class AssignmentDetailRoute(
    val assignmentId: Long
) : ScreenRoute
