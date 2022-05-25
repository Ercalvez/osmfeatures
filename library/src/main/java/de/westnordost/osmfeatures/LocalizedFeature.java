package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Data class associated with the Feature interface. Represents a localized feature.
 *
 *  I.e. the name and terms are specified in the given locale. */
class LocalizedFeature implements Feature {

    private final BaseFeature p;
    private final List<String> names;
    private final List<String> terms;
    private final List<String> canonicalNames;
    private final List<String> canonicalTerms;
    private final Locale locale;

    public LocalizedFeature(BaseFeature p, Locale locale, List<String> names, List<String> terms)
    {
        this.p = p;
        this.names = names;
        this.terms = terms;
        this.locale = locale;

        List<String> canonicalNames = new ArrayList<>(names.size());
        for (String name : names)
        {
            canonicalNames.add(StringUtils.canonicalize(name));
        }
        this.canonicalNames = Collections.unmodifiableList(canonicalNames);

        List<String> canonicalTerms = new ArrayList<>(terms.size());
        for (String term : terms)
        {
            canonicalTerms.add(StringUtils.canonicalize(term));
        }
        this.canonicalTerms = Collections.unmodifiableList(canonicalTerms);
    }

    @Override public String getId() { return p.getId(); }
    @Override public Map<String, String> getTags() { return p.getTags(); }
    @Override public List<GeometryType> getGeometry() { return p.getGeometry(); }
    @Override public String getName() { return names.get(0); }
    @Override public String getIcon() { return p.getIcon(); }
    @Override public String getImageURL() { return p.getImageURL(); }
    @Override public List<String> getNames() { return names; }
    @Override public List<String> getTerms() { return terms; }
    @Override public List<String> getIncludeCountryCodes() { return p.getIncludeCountryCodes(); }
    @Override public List<String> getExcludeCountryCodes() { return p.getExcludeCountryCodes(); }
    @Override public boolean isSearchable() { return p.isSearchable(); }
    @Override public double getMatchScore() { return p.getMatchScore(); }
    @Override public Map<String, String> getAddTags() { return p.getAddTags(); }
    @Override public Map<String, String> getRemoveTags() { return p.getRemoveTags(); }
    @Override public List<String> getCanonicalNames() { return canonicalNames; }
    @Override public List<String> getCanonicalTerms() { return canonicalTerms; }
    @Override public Locale getLocale() { return locale; }

    @Override public String toString() { return getId(); }
}
