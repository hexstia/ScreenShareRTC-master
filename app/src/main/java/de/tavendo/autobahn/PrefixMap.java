//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import java.util.HashMap;

public class PrefixMap {
    private final HashMap<String, String> mPrefixes = new HashMap();
    private final HashMap<String, String> mUris = new HashMap();

    public PrefixMap() {
    }

    public void set(String prefix, String uri) {
        this.mPrefixes.put(prefix, uri);
        this.mUris.put(uri, prefix);
    }

    public String get(String prefix) {
        return (String)this.mPrefixes.get(prefix);
    }

    public String remove(String prefix) {
        if (this.mPrefixes.containsKey(prefix)) {
            String uri = (String)this.mPrefixes.get(prefix);
            this.mPrefixes.remove(prefix);
            this.mUris.remove(uri);
            return uri;
        } else {
            return null;
        }
    }

    public void clear() {
        this.mPrefixes.clear();
        this.mUris.clear();
    }

    public String resolve(String curie) {
        int i = curie.indexOf(58);
        if (i > 0) {
            String prefix = curie.substring(0, i);
            if (this.mPrefixes.containsKey(prefix)) {
                return (String)this.mPrefixes.get(prefix) + curie.substring(i + 1);
            }
        }

        return null;
    }

    public String resolveOrPass(String curieOrUri) {
        String u = this.resolve(curieOrUri);
        return u != null ? u : curieOrUri;
    }

    public String shrink(String uri) {
        for(int i = uri.length(); i > 0; --i) {
            String u = uri.substring(0, i);
            String p = (String)this.mUris.get(u);
            if (p != null) {
                return p + ':' + uri.substring(i);
            }
        }

        return uri;
    }
}
