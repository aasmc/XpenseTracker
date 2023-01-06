package ru.aasmc.xpensemanager.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Rule
import ru.aasmc.xpensemanager.utils.MainDispatcherRule

abstract class DatabaseTest {
    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule by lazy { HiltAndroidRule(this) }

    @get:Rule(order = 1)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

}