package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.SubstanceDao
import com.example.data.model.Substance
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

class ChemistryRepository(
    private val context: Context,
    private val substanceDao: SubstanceDao
) {
    private val sharedPrefs = context.getSharedPreferences("chemistry_game_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    // Retrieve or generate player's unique UUID
    fun getPlayerUuid(): String {
        var uuid = sharedPrefs.getString("player_uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("player_uuid", uuid).apply()
        }
        return uuid
    }

    // Custom Supabase Credentials entered in settings UI
    fun saveCustomSupabaseConfig(url: String, key: String) {
        sharedPrefs.edit()
            .putString("supabase_custom_url", url)
            .putString("supabase_custom_key", key)
            .apply()
    }

    fun getSupabaseConfig(): Pair<String?, String?> {
        val customUrl = sharedPrefs.getString("supabase_custom_url", null)
        val customKey = sharedPrefs.getString("supabase_custom_key", null)
        
        if (!customUrl.isNullOrEmpty() && !customKey.isNullOrEmpty()) {
            return Pair(customUrl, customKey)
        }
        
        // Fallback to BuildConfig if configured and not placeholder
        val buildUrl = com.example.BuildConfig.SUPABASE_URL
        val buildKey = com.example.BuildConfig.SUPABASE_ANON_KEY
        val isValidBuild = buildUrl.isNotEmpty() && buildUrl != "YOUR_SUPABASE_URL" &&
                           buildKey.isNotEmpty() && buildKey != "YOUR_SUPABASE_ANON_KEY"
                           
        if (isValidBuild) {
            return Pair(buildUrl, buildKey)
        }
        return Pair(null, null)
    }

    val allSubstances: Flow<List<Substance>> = substanceDao.getAllSubstances()
    val discoveredSubstances: Flow<List<Substance>> = substanceDao.getDiscoveredSubstances()

    suspend fun getSubstance(formula: String): Substance? {
        return substanceDao.getSubstanceByFormula(formula)
    }

    // Local hardcoded recipes to satisfy "Tanpa ngeleg" for standard common ones
    private val localRecipes = mapOf(
        "H+O" to Substance("H2O", "Water", "Water is a polar inorganic compound that is at room temperature a tasteless and odorless liquid, essential for all known forms of life.", "Secondary Oxide", "#00E5FF", false),
        "Cl+Na" to Substance("NaCl", "Sodium Chloride", "Sodium chloride, commonly known as salt, is an ionic compound. It is essential for human life and sodium-based chemistry.", "Mineral Salt", "#ECEFF1", false),
        "C+O" to Substance("CO", "Carbon Monoxide", "Carbon monoxide is a toxic, flammable gas that is colorless, odorless, and tasteless, formed by combustion with limited oxygen.", "Toxic Gas", "#90A4AE", false),
        "CO+O" to Substance("CO2", "Carbon Dioxide", "Carbon dioxide is an acidic colorless gas with a density about 53% higher than that of dry air. Formed by complete combustion.", "Greenhouse Gas", "#B0BEC5", false),
        "H+N" to Substance("NH3", "Ammonia", "Ammonia is a inorganic compound of nitrogen and hydrogen. It is a colorless gas with a characteristic pungent smell.", "Alkaline Gas", "#80DEEA", false),
        "Cl+H" to Substance("HCl", "Hydrochloric Acid", "Hydrochloric acid is an inorganic, highly corrosive acid. It is clean and clear but possesses toxic respiratory hazards.", "Strong Acid", "#CCFF90", false),
        "Fe+O" to Substance("Fe2O3", "Iron Oxide", "Commonly known as rust. This reddish oxide is formed when iron reacts with oxygen in the presence of air moisture.", "Metallic Oxide", "#D84315", false),
        "Al+O" to Substance("Al2O3", "Aluminum Oxide", "A sturdy chemical compound of aluminum and oxygen. It is corrosion-resistant and used widely in industrial settings.", "Refractory Oxide", "#EEEEEE", false),
        "CO2+H2O" to Substance("H2CO3", "Carbonic Acid", "Carbonic acid is a chemical compound with the chemical formula H2CO3. Formed when carbon dioxide dissolved in pure water.", "Weak Acid", "#B2DFDB", false),
        "HCl+NH3" to Substance("NH4Cl", "Ammonium Chloride", "Ammonium chloride is an inorganic compound, a white crystalline salt that is highly soluble in water.", "Acidic Salt", "#E0F2F1", false),
        "O+S" to Substance("SO2", "Sulfur Dioxide", "Sulfur dioxide is a toxic gas with a pungent, irritating odour, released naturally by volcanic activity.", "Acid Gas", "#FFF59D", false),
        "O+SO2" to Substance("SO3", "Sulfur Trioxide", "Sulfur trioxide is a white crystalline compound that vaporizes at room temperature, releasing highly hazardous fumes.", "Acid Anhydride", "#FFE082", false),
        "H2O+SO3" to Substance("H2SO4", "Sulfuric Acid", "Sulfuric acid is a highly corrosive mineral acid. It is a viscous liquid solar in battery fluids and manufacture.", "Corrosive Acid", "#76FF03", false),
        "Cl+K" to Substance("KCl", "Potassium Chloride", "Potassium chloride is a metal halide salt. Excellent nutrient additive, medically used to treat low blood potassium.", "Essential Salt", "#F4FF81", false),
        "O+Cu" to Substance("CuO", "Copper Oxide", "Copper(II) oxide or cupric oxide is an inorganic compound. This black solid is a precursor to many copper salts.", "Transition Metal Oxide", "#37474F", false),
        "Cl+Zn" to Substance("ZnCl2", "Zinc Chloride", "Zinc chlorides are chemical compounds of zinc and chlorine. Extremely hygroscopic white solid used in soldering.", "Metal Halide", "#D7CCC8", false),
        "H2O+NaCl" to Substance("Saline", "Saline Solution", "A classic solution of table salt (sodium chloride) dissolved in sterile water, compatible with biology.", "Medical Fluid", "#E0F7FA", false),
        "H+S" to Substance("H2S", "Hydrogen Sulfide", "Hydrogen sulfide is a poisonous, corrosive, and flammable gas with the foul odor of rotten eggs.", "Hazardous Gas", "#E6EE9C", false),
        "Cl+Ca" to Substance("CaCl2", "Calcium Chloride", "Calcium chloride is an ionic inorganic salt. Deliquescent white powder used heavily for road de-icing and drying.", "Hygroscopic Salt", "#ECEFF1", false)
    )

    suspend fun seedDefaultSubstancesIfEmpty() = withContext(Dispatchers.IO) {
        if (substanceDao.getCount() > 0) return@withContext

        // Define our 8 standard starter elements (Pre-discovered)
        val starters = listOf(
            Substance("H", "Hydrogen", "Atomic Number 1. The lightest, most abundant chemical element in the universe. Extremely flammable gas.", "Non-metal Gas", "#33F0FF", true, 1, 1, 1, true, true),
            Substance("He", "Helium", "Atomic Number 2. Highly unreactive, colorless, and odorless monoatomic noble gas. Generates beautiful pink-purple discharge.", "Noble Gas", "#FF33F5", true, 2, 1, 18, true, true),
            Substance("C", "Carbon", "Atomic Number 6. Tetravalent nonmetallic solid. The backbone of organic chemistry and all terrestrial cell biology.", "Non-metal Solid", "#E0E0E0", true, 6, 2, 14, true, true),
            Substance("N", "Nitrogen", "Atomic Number 7. Diatomic gas making up 78% of Earth's atmosphere. Crucial in biological systems.", "Non-metal Gas", "#3374FF", true, 7, 2, 15, true, true),
            Substance("O", "Oxygen", "Atomic Number 8. Highly reactive nonmetallic oxidizing gas. Essential for human respiration and planetary water cycles.", "Non-metal Gas", "#33FF57", true, 8, 2, 16, true, true),
            Substance("Na", "Sodium", "Atomic Number 11. Soft, silvery-white, highly reactive alkali metal. Must be stored in oil to avoid fire.", "Alkali Metal", "#FFB833", true, 11, 3, 1, true, true),
            Substance("Cl", "Chlorine", "Atomic Number 17. Diatomic halogen gas. Greenish-yellow with a suffocating door, highly reactive bleach and disinfectant.", "Halogen Gas", "#ADFF2F", true, 17, 3, 17, true, true),
            Substance("Fe", "Iron", "Atomic Number 26. Solid transition metal, essential structural material of Earth's core and industrial constructs.", "Transition Metal", "#FF5733", true, 26, 4, 8, true, true)
        )

        // Define elements that are locked and visible on periodic table (Discovered = False)
        val lockedElements = listOf(
            Substance("Li", "Lithium", "Atomic Number 3. Relentlessly reactive alkali metal. Lightest solid metal, used in high-capacity rechargeable batteries.", "Alkali Metal", "#FF9E80", true, 3, 2, 1, false, false),
            Substance("Be", "Beryllium", "Atomic Number 4. Relatively rare alkaline earth metal, lightweight and strong, highly toxic carcinogenic dust.", "Alkaline Earth Metal", "#FF8A80", true, 4, 2, 2, false, false),
            Substance("B", "Boron", "Atomic Number 5. High-melting brown metalloid used in borosilicate glassware and semiconductor doping.", "Metalloid", "#FFD54F", true, 5, 2, 13, false, false),
            Substance("F", "Fluorine", "Atomic Number 9. Unmatched chemical reactivity, extremely toxic pale yellow halogen gas. Forges extremely stable plastics.", "Halogen Gas", "#81C784", true, 9, 2, 17, false, false),
            Substance("Ne", "Neon", "Atomic Number 10. Nobel gas glowing bright reddish-orange in discharge tubes. Used heavily in retro signs.", "Noble Gas", "#EA80FC", true, 10, 2, 18, false, false),
            Substance("Mg", "Magnesium", "Atomic Number 12. Lightweight, strong alkaline earth metal that burns with an intensely bright blinding white flare.", "Alkaline Earth Metal", "#A1887F", true, 12, 3, 2, false, false),
            Substance("Al", "Aluminum", "Atomic Number 13. Abundant, lightweight, nonmagnetic post-transition metal. Impervious to atmospheric corrosion.", "Post-transition Metal", "#90A4AE", true, 13, 3, 13, false, false),
            Substance("Si", "Silicon", "Atomic Number 14. Hard, crystalline metalloid playing as the absolute building block of modern digital computer chips.", "Metalloid", "#9E9D24", true, 14, 3, 14, false, false),
            Substance("P", "Phosphorus", "Atomic Number 15. Multivalent nonmetallic solid. Highly flammable white phosphorus or stable red matches.", "Non-metal Solid", "#FF8F00", true, 15, 3, 15, false, false),
            Substance("S", "Sulfur", "Atomic Number 16. Multi-allotropic light-yellow nonmetallic solid. Emits bad fumes and is core to volcanic chemistry.", "Non-metal Solid", "#FBC02D", true, 16, 3, 16, false, false),
            Substance("Ar", "Argon", "Atomic Number 18. Third-most abundant atmospheric gas. Plentiful noble gas serving inert atmospheres during welding.", "Noble Gas", "#CE93D8", true, 18, 3, 18, false, false),
            Substance("K", "Potassium", "Atomic Number 19. Silvery alkali metal that catches fire immediately in contact with any amount of water.", "Alkali Metal", "#D4E157", true, 19, 4, 1, false, false),
            Substance("Ca", "Calcium", "Atomic Number 20. Soft gray alkaline earth metal, essential building material of bones, chalk, and coral structures.", "Alkaline Earth Metal", "#81D4FA", true, 20, 4, 2, false, false),
            Substance("Cu", "Copper", "Atomic Number 29. Ductile transition metal with ultra-high electrical and thermal conductivity. Unique warm-colored metal.", "Transition Metal", "#FF7043", true, 29, 4, 11, false, false),
            Substance("Zn", "Zinc", "Atomic Number 30. Slightly brittle blue-gray transition metal used globally for steel galvanization rust-prevention.", "Transition Metal", "#A5D6A7", true, 30, 4, 12, false, false)
        )

        substanceDao.insertSubstances(starters)
        substanceDao.insertSubstances(lockedElements)
        
        Log.d("ChemistryRepository", "Seeded Database with starter and locked elements successfully.")
    }

    // Main reaction engine combining two substances
    suspend fun reactSubstances(sub1: Substance, sub2: Substance): ReactionResult = withContext(Dispatchers.IO) {
        val formula1 = sub1.formula
        val formula2 = sub2.formula

        // Sort alphabetically to be order-independent
        val sortedKeys = listOf(formula1, formula2).sorted()
        val recipeKey = "${sortedKeys[0]}+${sortedKeys[1]}"

        Log.d("ChemistryRepository", "Attempting combination: $recipeKey")

        // 1. First check Local static Recipes
        val localResult = localRecipes[recipeKey]
        if (localResult != null) {
            // Find in database to see if it already exists or update discovered
            val existing = substanceDao.getSubstanceByFormula(localResult.formula)
            if (existing != null) {
                if (!existing.discovered) {
                    substanceDao.markAsDiscovered(existing.formula)
                }
                return@withContext ReactionResult.Success(
                    existing.copy(discovered = true),
                    "Combination successful! Synthesized locally. Order-independent chemical bond."
                )
            } else {
                val newSub = localResult.copy(discovered = true)
                substanceDao.insertSubstance(newSub)
                return@withContext ReactionResult.Success(
                    newSub,
                    "Discovered a new compound! Added to your periodic catalog."
                )
            }
        }

        // 2. Fallback to Gemini 3.5-flash for true infinite creations!
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("ChemistryRepository", "Gemini API key is not configured.")
            return@withContext ReactionResult.Error(
                "Gemini AI key missing. Please enter your API key in the Secrets panel in Google AI Studio to unlock true infinity!"
            )
        }

        try {
            val prompt = """
                You are an expert chemical compound modeling engine.
                The player is playing an Infinite Chemistry game and wants to combine:
                Substance 1: "${sub1.name}" (Formula: ${sub1.formula}, Category: ${sub1.category})
                Substance 2: "${sub2.name}" (Formula: ${sub2.formula}, Category: ${sub2.category})

                Determine if these two substances can react together or merge to create a scientifically plausible substance, compound, solution, chemical aggregate, alloy or mineral.
                
                You must respond EXCLUSIVELY with a JSON object in this format (no conversational markdown preambles or postscripts):
                {
                  "success": true,
                  "result_name": "Official Common Name (e.g., Sodium Carbonate, Saline, Rust, Bronze)",
                  "result_formula": "Chemical formula of the result (e.g., Na2CO3, H2O, SnCu)",
                  "description": "A crisp, detailed explanation (1-2 sentences) of how these combined and what this compound is used for in modern chemistry.",
                  "category": "Chemical classification (e.g., Acid, Salt, Oxide, Alkali, Hydrocarbon, Gas, Alloy, Solution, Noble Gas, Metal)",
                  "color_hex": "An elegant hex color that chemically matches the substance (e.g., #00BCD4 for water/gases, #FF5722 for reddish compounds, #FFEB3B for yellow sulfur elements, etc.)"
                }

                Important Rules:
                - Do NOT make up complete nonsense. Use accurate chemistry rules (e.g., metal + acid -> salt, acid + base -> salt/water, alkali metal + halogen -> salt, inert noble gases like Helium do not react unless high energy ionization forming exotic molecular ions like HeH+).
                - Make it fun but plausible!
                - If they cannot combine at all or have absolutely no reaction under realistic or reasonable fantasy lab conditions, return:
                {
                  "success": false,
                  "description": "These materials do not react under current lab pressures or temperatures."
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                )
            )

            val apiResponse = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val rawText = apiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext ReactionResult.Error("Gemini returned an empty reply.")

            Log.d("ChemistryRepository", "Raw AI response: $rawText")
            val cleanJson = extractJson(rawText)
            
            val adapter = moshi.adapter(AiReactionResponse::class.java)
            val reactionResponse = adapter.fromJson(cleanJson)
                ?: return@withContext ReactionResult.Error("Failed to parse chemical reaction blueprint.")

            if (reactionResponse.success && !reactionResponse.result_name.isNullOrEmpty() && !reactionResponse.result_formula.isNullOrEmpty()) {
                val formula = reactionResponse.result_formula.trim()
                val name = reactionResponse.result_name.trim()
                val description = reactionResponse.description ?: "A synthesized chemical substance."
                val category = reactionResponse.category ?: "Unknown Compound"
                val colorHex = reactionResponse.color_hex ?: "#B0BEC5"

                // Check if it already exists as a locked element (e.g. they synthesized a native metal)
                val existing = substanceDao.getSubstanceByFormula(formula)
                if (existing != null) {
                    if (!existing.discovered) {
                        substanceDao.markAsDiscovered(existing.formula)
                    }
                    return@withContext ReactionResult.Success(
                        existing.copy(discovered = true),
                        "Combination successful! Synthesized via AI: ${existing.name}"
                    )
                }

                // Insert new compound
                val newSubstance = Substance(
                    formula = formula,
                    name = name,
                    description = description,
                    category = category,
                    colorHex = colorHex,
                    isElement = false,
                    discovered = true,
                    isStarter = false,
                    timestamp = System.currentTimeMillis()
                )
                substanceDao.insertSubstance(newSubstance)

                return@withContext ReactionResult.Success(
                    newSubstance,
                    "Infinite Synthesizer: Discovered a brand new compound!"
                )
            } else {
                val desc = reactionResponse.description ?: "No reaction detected. Energy dissipated safely."
                return@withContext ReactionResult.NoReaction(desc)
            }

        } catch (e: Exception) {
            Log.e("ChemistryRepository", "AI fusion crashed", e)
            return@withContext ReactionResult.Error("AI Synthesizer failed: ${e.localizedMessage ?: "Unknown collision"}")
        }
    }

    private fun extractJson(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json").substringBeforeLast("```")
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```").substringBeforeLast("```")
        }
        return clean.trim()
    }

    // Sync progress local -> Supabase clouds. "Zero Lag" guaranteed as this runs in IO thread completely asynchronously.
    suspend fun syncWithSupabase(): SupabaseSyncResult = withContext(Dispatchers.IO) {
        val (url, key) = getSupabaseConfig()
        if (url.isNullOrEmpty() || key.isNullOrEmpty()) {
            return@withContext SupabaseSyncResult.NotConfigured(
                "Cloud Sync is not configured. Go to Options and enter your Supabase credentials to sync progress!"
            )
        }

        try {
            val syncClient = SupabaseClientProvider.createService(url)
            val playerId = getPlayerUuid()

            // 1. Fetch discovered substances from DB snapshot using Flow.first()
            val discoveredLocal = substanceDao.getDiscoveredSubstances().first()
            val progressList = discoveredLocal.map {
                SupabaseProgress(
                    id = "${playerId}_${it.formula}",
                    player_id = playerId,
                    formula = it.formula,
                    name = it.name,
                    discovered_at = it.timestamp
                )
            }

            // 2. Upload/Upsert local progress to Supabase
            if (progressList.isNotEmpty()) {
                syncClient.upsertProgress(key, "Bearer $key", progressList = progressList)
            }

            // 3. Download cloud progress for this player
            val cloudProgress = syncClient.getProgress(key, "Bearer $key", eqPlayerId = "eq.$playerId")
            
            // 4. Merge cloud progress back to local database
            var pullCount = 0
            for (cloudItem in cloudProgress) {
                val localItem = substanceDao.getSubstanceByFormula(cloudItem.formula)
                if (localItem == null) {
                    // This is a dynamic compound discovered by the user previously and synced!
                    val pulledSub = Substance(
                        formula = cloudItem.formula,
                        name = cloudItem.name,
                        description = "Recovered from Supabase cloud synchronizer.",
                        category = "Recovered Compound",
                        colorHex = "#90A4AE",
                        isElement = false,
                        discovered = true,
                        timestamp = cloudItem.discovered_at
                    )
                    substanceDao.insertSubstance(pulledSub)
                    pullCount++
                } else if (!localItem.discovered) {
                    substanceDao.markAsDiscovered(localItem.formula, cloudItem.discovered_at)
                    pullCount++
                }
            }

            val syncMsg = if (pullCount > 0) {
                "Cloud Sync complete! Succeeded in uploading ${progressList.size} substances and recovered $pullCount from the cloud."
            } else {
                "Cloud Sync complete! All ${progressList.size} discovered substances are secure in Supabase."
            }
            return@withContext SupabaseSyncResult.Success(syncMsg)
        } catch (e: Exception) {
            Log.e("ChemistryRepository", "Supabase sync error", e)
            return@withContext SupabaseSyncResult.Failure(
                "Sync failed: ${e.localizedMessage ?: "Verify Internet & Table Schemas"}"
            )
        }
    }
}

// Data class representation for Moshi parsing
@JsonClass(generateAdapter = true)
data class AiReactionResponse(
    val success: Boolean,
    val result_name: String? = null,
    val result_formula: String? = null,
    val description: String? = null,
    val category: String? = null,
    val color_hex: String? = null
)

sealed interface ReactionResult {
    data class Success(val substance: Substance, val message: String) : ReactionResult
    data class NoReaction(val reason: String) : ReactionResult
    data class Error(val message: String) : ReactionResult
}

sealed interface SupabaseSyncResult {
    data class Success(val message: String) : SupabaseSyncResult
    data class NotConfigured(val prompt: String) : SupabaseSyncResult
    data class Failure(val error: String) : SupabaseSyncResult
}
