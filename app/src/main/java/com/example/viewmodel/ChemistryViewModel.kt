package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Substance
import com.example.data.repository.ChemistryRepository
import com.example.data.repository.ReactionResult
import com.example.data.repository.SupabaseSyncResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChemistryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ChemistryRepository(application, database.substanceDao())

    // UI States
    val allSubstances: StateFlow<List<Substance>> = repository.allSubstances
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val discoveredSubstances: StateFlow<List<Substance>> = repository.discoveredSubstances
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Chamber Slots
    var reactionSlot1 by mutableStateOf<Substance?>(null)
        private set
    var reactionSlot2 by mutableStateOf<Substance?>(null)
        private set

    // Animation & State
    var isReactionRunning by mutableStateOf(false)
        private set
    var reactionResult by mutableStateOf<ReactionResult?>(null)
        private set
    var isSyncing by mutableStateOf(false)
        private set
    var syncResultState by mutableStateOf<SupabaseSyncResult?>(null)
        private set

    // Settings Configuration Inputs
    var settingsUrlInput by mutableStateOf("")
    var settingsKeyInput by mutableStateOf("")

    // Category Filter for discovered inventory
    var selectedCategoryFilter by mutableStateOf("All")

    init {
        // Initialize database list and save existing Supabase details
        viewModelScope.launch {
            try {
                repository.seedDefaultSubstancesIfEmpty()
            } catch (e: Exception) {
                Log.e("ChemistryViewModel", "Error seeding default chemical substances: ${e.message}", e)
            }
            
            try {
                val (url, key) = repository.getSupabaseConfig()
                settingsUrlInput = url ?: ""
                settingsKeyInput = key ?: ""
            } catch (e: Exception) {
                Log.e("ChemistryViewModel", "Error reading Supabase key config: ${e.message}", e)
            }
        }
    }

    fun selectReactant(substance: Substance) {
        if (reactionSlot1 == null) {
            reactionSlot1 = substance
        } else if (reactionSlot2 == null && reactionSlot1?.formula != substance.formula) {
            // Cannot put same substance in both slots unless they want to double compile (some reactions support it, so slot1 can interact with slot2)
            // But we block putting EXACT same card if they try, unless we allow duplication.
            reactionSlot2 = substance
        } else if (reactionSlot2 == null) {
            reactionSlot2 = substance
        } else {
            // Both slots occupied, replace slot 1 and shift
            reactionSlot1 = reactionSlot2
            reactionSlot2 = substance
        }
        reactionResult = null // Reset previous result when shifting elements
    }

    fun clearChamber() {
        reactionSlot1 = null
        reactionSlot2 = null
        reactionResult = null
    }

    fun removeSlot1() {
        reactionSlot1 = null
        reactionResult = null
    }

    fun removeSlot2() {
        reactionSlot2 = null
        reactionResult = null
    }

    fun triggerReaction() {
        val reactant1 = reactionSlot1
        val reactant2 = reactionSlot2

        if (reactant1 == null || reactant2 == null) {
            reactionResult = ReactionResult.Error("Place 2 chemicals in the reaction chambers to synthesize!")
            return
        }

        viewModelScope.launch {
            try {
                isReactionRunning = true
                reactionResult = null
                
                // Keep the gorgeous neon sweeping fusion animation running for 1.2s to feed suspense
                val animationJob = launch { delay(1200) }
                
                // Concurrently run chemical reaction calculation on IO thread
                val result = repository.reactSubstances(reactant1, reactant2)
                
                // Wait for animation to finish
                animationJob.join()
                
                isReactionRunning = false
                reactionResult = result
                
                if (result is ReactionResult.Success) {
                    // Do not clear chambers so they can immediately see the ingredients, but they can click clear.
                    // Or they can run secondary reactions on the newly discovered thing!
                    Log.d("ChemistryViewModel", "Reaction Success: ${result.substance.name}")
                }
            } catch (e: Exception) {
                isReactionRunning = false
                reactionResult = ReactionResult.Error("Nuclear collapse: ${e.localizedMessage}")
            }
        }
    }

    fun syncProgress() {
        viewModelScope.launch {
            isSyncing = true
            syncResultState = null
            
            val result = repository.syncWithSupabase()
            syncResultState = result
            isSyncing = false
        }
    }

    fun saveConfigAndSync(url: String, key: String) {
        viewModelScope.launch {
            repository.saveCustomSupabaseConfig(url.trim(), key.trim())
            settingsUrlInput = url.trim()
            settingsKeyInput = key.trim()
            
            // Sync immediately as verification
            syncProgress()
        }
    }

    fun getPlayerUuid(): String {
        return repository.getPlayerUuid()
    }
}
