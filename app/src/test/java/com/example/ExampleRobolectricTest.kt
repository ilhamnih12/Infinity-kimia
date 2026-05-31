package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.ChemistryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Infinite Chemistry", appName)
  }

  @Test
  fun `test database and repository seeding`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val dao = database.substanceDao()
    val repo = ChemistryRepository(context, dao)
    
    // Seed database
    repo.seedDefaultSubstancesIfEmpty()
    
    // Get substances count
    val count = dao.getCount()
    assertTrue("Database seeding should populate elements", count > 0)
    
    // Check discovered substances
    val discovered = repo.discoveredSubstances.first()
    assertTrue("Should have discovered starter elements", discovered.isNotEmpty())
    
    val all = repo.allSubstances.first()
    assertEquals(count, all.size)
  }
}
