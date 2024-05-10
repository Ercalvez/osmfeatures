package de.westnordost.osmfeatures

import okio.use

/** Non-localized feature collection sourcing from (NSI) iD presets defined in JSON.
 *
 * The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 * there is a presets.json which includes all the features. Additionally, it is possible to place
 * more files like e.g. presets-DE.json, presets-US-NY.json into the directory which will be loaded
 * lazily on demand  */
internal class IDBrandPresetsFeatureCollection(
    private val fileAccess: FileAccessAdapter
) : PerCountryFeatureCollection {
    // countryCode -> featureId -> Feature
    private val featuresByIdByCountryCode: MutableMap<String?, LinkedHashMap<String, Feature>> = LinkedHashMap(320)

    init {
        getOrLoadPerCountryFeatures(null)
    }

    override fun getAll(countryCodes: List<String?>): Collection<Feature> {
        val result = HashMap<String, Feature>()
        for (cc in countryCodes) {
            result.putAll(getOrLoadPerCountryFeatures(cc))
        }
        return result.values
    }

    override fun get(id: String, countryCodes: List<String?>): Feature? {
        for (countryCode in countryCodes) {
            val result = getOrLoadPerCountryFeatures(countryCode)[id]
            if (result != null) return result
        }
        return null
    }

    private fun getOrLoadPerCountryFeatures(countryCode: String?): LinkedHashMap<String, Feature> {
        return featuresByIdByCountryCode.synchronizedGetOrCreate(countryCode) {
            loadFeatures(countryCode).associateByTo(LinkedHashMap()) { it.id }
        }
    }

    private fun loadFeatures(countryCode: String?): List<BaseFeature> {
        val filename = getPresetsFileName(countryCode)
        if (!fileAccess.exists(filename)) return emptyList()
        return fileAccess.open(filename).use { source ->
            IDPresetsJsonParser(true).parse(source)
        }
    }

    companion object {
        private fun getPresetsFileName(countryCode: String?): String =
            if (countryCode == null) "presets.json" else "presets-$countryCode.json"
    }
}