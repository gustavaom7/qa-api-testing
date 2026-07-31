package com.gustavaom7.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model for Wikipedia Search API response
 * Represents the JSON structure returned by search endpoint
 */
public class SearchResponse {

    @SerializedName("batchcomplete")
    private boolean batchComplete;

    @SerializedName("query")
    private Query query;

    public static class Query {
        @SerializedName("search")
        private List<SearchResult> search;

        public List<SearchResult> getSearch() {
            return search;
        }

        public void setSearch(List<SearchResult> search) {
            this.search = search;
        }
    }

    public static class SearchResult {
        @SerializedName("ns")
        private int namespace;

        @SerializedName("title")
        private String title;

        @SerializedName("pageid")
        private int pageId;

        @SerializedName("size")
        private int size;

        @SerializedName("wordcount")
        private int wordCount;

        @SerializedName("snippet")
        private String snippet;

        @SerializedName("timestamp")
        private String timestamp;

        public String getTitle() {
            return title;
        }

        public String getSnippet() {
            return snippet;
        }

        public int getPageId() {
            return pageId;
        }

        public int getSize() {
            return size;
        }

        public int getWordCount() {
            return wordCount;
        }
    }

    public boolean isBatchComplete() {
        return batchComplete;
    }

    public Query getQuery() {
        return query;
    }

    public List<SearchResult> getSearchResults() {
        return query != null ? query.getSearch() : null;
    }
}
