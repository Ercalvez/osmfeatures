package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Data class associated with the Feature interface. Represents a non-localized feature. */
class BaseFeature implements Feature
{
	private final String id;
	private final Map<String,String> tags;
	private final List<GeometryType> geometry;
	private final String icon;
	private final String imageURL;
	private final List<String> names;
	private final List<String> terms;
	private final List<String> includeCountryCodes;
	private final List<String> excludeCountryCodes;
	private final boolean searchable;
	private final double matchScore;
	private final Map<String,String> addTags;
	private final Map<String,String> removeTags;

	private final List<String> canonicalNames;
	private final List<String> canonicalTerms;

	public BaseFeature(
			String id, Map<String, String> tags, List<GeometryType> geometry,
			String icon, String imageURL, List<String> names, List<String> terms,
			List<String> includeCountryCodes, List<String> excludeCountryCodes,
			boolean searchable, double matchScore,
			Map<String, String> addTags, Map<String, String> removeTags)
	{
		this.id = id;
		this.tags = tags;
		this.geometry = geometry;
		this.icon = icon;
		this.imageURL = imageURL;
		this.names = names;
		this.terms = terms;
		this.includeCountryCodes = includeCountryCodes;
		this.excludeCountryCodes = excludeCountryCodes;
		this.searchable = searchable;
		this.matchScore = matchScore;
		this.addTags = addTags;
		this.removeTags = removeTags;

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

	@Override public String getId() { return id; }
	@Override public Map<String, String> getTags() { return tags; }
	@Override public List<GeometryType> getGeometry() { return geometry; }
	@Override public String getName() { return names.get(0); }
	@Override public String getIcon() { return icon; }
	@Override public String getImageURL() { return imageURL; }
	@Override public List<String> getNames() { return names; }
	@Override public List<String> getTerms() { return terms; }
	@Override public List<String> getIncludeCountryCodes() { return includeCountryCodes; }
	@Override public List<String> getExcludeCountryCodes() { return excludeCountryCodes; }
	@Override public boolean isSearchable() { return searchable; }
	@Override public double getMatchScore() { return matchScore; }
	@Override public Map<String, String> getAddTags() { return addTags; }
	@Override public Map<String, String> getRemoveTags() { return removeTags; }
	@Override public List<String> getCanonicalNames() { return canonicalNames; }
	@Override public List<String> getCanonicalTerms() { return canonicalTerms; }
	@Override public Locale getLocale() { return null; }

	@Override public String toString() { return getId(); }
}
