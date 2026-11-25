package com.kvlabs.demo.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * ARCHITECTURAL RATIONALE: MainDispatcherRule for JVM Unit Testing
 *
 * Why this approach?
 * 1. The Android Main Thread Problem in JVM Unit Tests:
 *    - In pure JVM local unit tests (executing on the host computer via JUnit 4), `Dispatchers.Main`
 *      is not initialized because Android's `Looper.getMainLooper()` is missing.
 *    - Any test launching a coroutine in `viewModelScope` will throw:
 *      `IllegalStateException: Module with the Main dispatcher had failed to initialize`.
 *
 * 2. JUnit TestWatcher Implementation:
 *    - This JUnit Rule intercepts test execution:
 *      - `starting()`: Replaces `Dispatchers.Main` with a `TestDispatcher` (defaulting to `UnconfinedTestDispatcher`).
 *      - `finished()`: Calls `Dispatchers.resetMain()` to leave the environment clean for subsequent test classes.
 *    - Result: Coroutines in `viewModelScope` run synchronously and deterministically on the test thread.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
