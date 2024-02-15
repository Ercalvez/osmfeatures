package de.westnordost.osmfeatures

import osmfeatures.FileAccessAdapter
import osmfeatures.Locale
import osmfeatures.LocalizedFeatureCollection

/** Localized feature collection sourcing from iD presets defined in JSON.
 *
 * The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 * there is a presets.json which includes all the features. The translations are expected to be
 * located in the same directory named like e.g. de.json, pt-BR.json etc.  */
class IDLocalizedFeatureCollection(private val fileAccess: FileAccessAdapter) :
    LocalizedFeatureCollection {
    private val featuresById: LinkedHashMap<String, BaseFeature>
    private val localizedFeaturesList: MutableMap<Locale?, List<LocalizedFeature>> = HashMap()
    private val localizedFeatures: MutableMap<List<Locale?>?, LinkedHashMap<String, Feature>> = HashMap()

    init {
        val features = loadFeatures()
        featuresById = LinkedHashMap(features.size)
        for (feature in features) {
            featuresById[feature.id] = feature
        }
    }

    private fun loadFeatures(): List<BaseFeature> {
        try {
            val source = fileAccess.open(FEATURES_FILE)
            return IDPresetsJsonParser().parse(source)
        }
        catch (e: Exception)
        {
            throw RuntimeException(e)
        }
    }
    private fun getOrLoadLocalizedFeatures(locales: List<Locale?>): LinkedHashMap<String, Feature>? {
        return CollectionUtils.synchronizedGetOrCreate(localizedFeatures, locales, ::loadLocalizedFeatures)
    }

    private fun loadLocalizedFeatures(locales: List<Locale?>): LinkedHashMap<String, Feature> {
        val result = LinkedHashMap<String, Feature>(featuresById.size)
        val it = locales.listIterator(locales.size)
        while (it.hasPrevious()) {
            val locale = it.previous()
            if (locale != null) {
                for (localeComponent in getLocaleComponents(locale)) {
                    getOrLoadLocalizedFeaturesList(localeComponent)?.let { it1 -> putAllFeatures(result, it1) }
                }
            } else {
                putAllFeatures(result, featuresById.values)
            }
        }
        return result
    }

    private fun getOrLoadLocalizedFeaturesList(locale: Locale): List<LocalizedFeature>? {
        return CollectionUtils.synchronizedGetOrCreate(
            localizedFeaturesList, locale
        ) { locale: Locale? ->
            loadLocalizedFeaturesList(
                locale
            )
        }
    }

    private fun loadLocalizedFeaturesList(locale: Locale?): List<LocalizedFeature> {
        val filename = getLocalizationFilename(locale)
        if (!fileAccess.exists(filename)) return emptyList()

        fileAccess.open(filename).use { source ->
            return IDPresetsTranslationJsonParser().parse(source, locale, featuresById.toMap())
        }
    }

    override fun getAll(locales: List<Locale?>): Collection<Feature> {
        return getOrLoadLocalizedFeatures(locales)?.values ?: emptyList()
    }

    override operator fun get(id: String, locales: List<Locale?>): Feature? {
        return getOrLoadLocalizedFeatures(locales)?.get(id)
    }

    companion object {
        private const val FEATURES_FILE = "presets.json"
        private fun getLocalizationFilename(locale: Locale?): String {
            /* we only want language+country+script of the locale, not anything else. So we construct
		   it anew here */
            return Locale.Builder()
                .setLanguage(locale?.language ?: "")
                .setRegion(locale?.country ?: "")
                .setScript(locale?.script ?: "")
                .build()
                .languageTag + ".json"
        }

        private fun getLocaleComponents(locale: Locale?): List<Locale> {
            val lang = locale?.language ?: ""
            val country = locale?.country ?: ""
            val script = locale?.script ?: ""
            val result: MutableList<Locale> = ArrayList(4)
            result.add(Locale(lang))
            if (country.isNotEmpty()) result.add(Locale.Builder().setLanguage(lang).setRegion(country).build())
            if (script.isNotEmpty()) result.add(Locale.Builder().setLanguage(lang).setScript(script).build())
            if (country.isNotEmpty() && script.isNotEmpty()) result.add(
                Locale.Builder().setLanguage(lang).setRegion(country).setScript(script).build()
            )
            return result
        }

        private fun putAllFeatures(map: MutableMap<String, Feature>, features: Iterable<Feature>) {
            for (feature in features) {
                map[feature.id] = feature
            }
        }
    }
}
