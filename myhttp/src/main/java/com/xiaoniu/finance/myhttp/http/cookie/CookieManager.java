package com.xiaoniu.finance.myhttp.http.cookie;

import android.content.Context;
import android.net.ParseException;
import android.util.Log;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cookie管理器
 * <p>
 * 此处大部分代码均从 {@link android.webkit.CookieManager} 拷贝和修改而来
 * 自己管理自己登陆态的Cookie（目前功能中Cookie的保存和读取可以用最简单的SharePreferences代替）
 * <p>
 * WebView中的CookieManager由WebView自身管理，当请求中带有 Set-Cookie 时，会自动将此Cookie记录到webview.db中
 * 并且在请求的时候从此处取出做为其Cookie，其中 setCookie 方法会在原本的Cookie基础上无限追加（属于List<Cookie>）
 * CookieManager的set、get均是内存中的操作，不会实时的获取文件中的数据，通过CookieSyncManager可以将数据不同到db中
 * 但是CookieSyncManager的数据只在初始化时从db中获取，之后不会从db中取数据
 * 这样会导致在跨进程操作的时候,两个进程间的CookieManager中的数据不一致，由此出现Bug
 */
class CookieManager {

    private final static String TAG = CookieManager.class.getSimpleName();

    private static CookieManager sRef = new CookieManager();

    private static final String LOGTAG = "webkit";

    private static final String DOMAIN = "domain";

    private static final String PATH = "path";

    private static final String EXPIRES = "expires";

    private static final String SECURE = "secure";

    private static final String MAX_AGE = "max-age";

    private static final String HTTP_ONLY = "httponly";

    private static final String HTTPS = "https";

    private static final char PERIOD = '.';

    private static final char COMMA = ',';

    private static final char SEMICOLON = ';';

    private static final char EQUAL = '=';

    private static final char PATH_DELIM = '/';

    private static final char QUESTION_MARK = '?';

    private static final char WHITE_SPACE = ' ';

    private static final char QUOTATION = '\"';

    private static final int SECURE_LENGTH = SECURE.length();

    private static final int HTTP_ONLY_LENGTH = HTTP_ONLY.length();

    // RFC2109 defines 4k as maximum size of a cookie
    private static final int MAX_COOKIE_LENGTH = 4 * 1024;

    // RFC2109 defines 20 as max cookie count per domain. As we track with base
    // domain, we allow 50 per base domain
    private static final int MAX_COOKIE_COUNT_PER_BASE_DOMAIN = 50;

    // RFC2109 defines 300 as max count of domains. As we track with base
    // domain, we set 200 as max base domain count
    private static final int MAX_DOMAIN_COUNT = 200;

    // max cookie count to limit RAM cookie takes less than 100k, it is based on
    // average cookie entry size is less than 100 bytes
    private static final int MAX_RAM_COOKIES_COUNT = 1000;

    //  max domain count to limit RAM cookie takes less than 100k,
    private static final int MAX_RAM_DOMAIN_COUNT = 15;

    private Map<String, CopyOnWriteArrayList<Cookie>> mCookieMap = Collections
            .synchronizedMap(new LinkedHashMap<String, CopyOnWriteArrayList<Cookie>>(MAX_DOMAIN_COUNT, 0.75f, true));

    private boolean mAcceptCookie = true;

    private CookieController mCookieController;

    public static CookieManager getInstance() {
        return sRef;
    }

    public void addController(CookieController cookieController) {
        mCookieController = cookieController;
    }

    /**
     * This contains a list of 2nd-level domains that aren't allowed to have
     * wildcards when combined with country-codes. For example: [.co.uk].
     */
    private final static String[] BAD_COUNTRY_2LDS =
            {"ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
                    "lg", "ne", "net", "or", "org"};

    /**
     * Set cookie for a given url. The old cookie with same host/path/name will
     * be removed. The new cookie will be added if it is not expired or it does
     * not have expiration which implies it is session cookie.
     *
     * @param url The url which cookie is set for
     * @param value The value for set-cookie: in http response header
     */
    public void setCookie(String url, String value) {
        WebAddress uri;
        try {
            uri = new WebAddress(url);
        } catch (ParseException ex) {
            Log.e(LOGTAG, "Bad address: " + url);
            return;
        }

        setCookie(uri, value);
    }

    /**
     * Set cookie for a given uri. The old cookie with same host/path/name will
     * be removed. The new cookie will be added if it is not expired or it does
     * not have expiration which implies it is session cookie.
     *
     * @param uri The uri which cookie is set for
     * @param value The value for set-cookie: in http response header
     * @hide - hide this because it takes in a parameter of type WebAddress, a system private class.
     */
    public synchronized void setCookie(WebAddress uri, String value) {
        if (value != null && value.length() > MAX_COOKIE_LENGTH) {
            return;
        }
        if (!mAcceptCookie || uri == null) {
            return;
        }

        Log.v(LOGTAG, "setCookie: uri: " + uri + " value: " + value);

        String[] hostAndPath = getHostAndPath(uri);
        if (hostAndPath == null) {
            return;
        }

        // For default path, when setting a cookie, the spec says:
        //Path:   Defaults to the path of the request URL that generated the
        // Set-Cookie response, up to, but not including, the
        // right-most /.
        if (hostAndPath[1].length() > 1) {
            int index = hostAndPath[1].lastIndexOf(PATH_DELIM);
            hostAndPath[1] = hostAndPath[1].substring(0,
                    index > 0 ? index : index + 1);
        }

        CopyOnWriteArrayList<Cookie> cookies = null;
        try {
            cookies = parseCookie(hostAndPath[0], hostAndPath[1], value);
        } catch (RuntimeException ex) {
            Log.e(LOGTAG, "parse cookie failed for: " + value);
        }

        if (cookies == null || cookies.size() == 0) {
            return;
        }

        String baseDomain = getBaseDomain(hostAndPath[0]);
        CopyOnWriteArrayList<Cookie> cookieList = mCookieMap.get(baseDomain);
        if (cookieList == null) {
            cookieList = new CopyOnWriteArrayList<Cookie>();
            mCookieMap.put(baseDomain, cookieList);
        }

        long now = System.currentTimeMillis();
        int size = cookies.size();
        for (int i = 0; i < size; i++) {
            Cookie cookie = cookies.get(i);

            boolean done = false;
            Iterator<Cookie> iter = cookieList.iterator();
            while (iter.hasNext()) {
                Cookie cookieEntry = iter.next();
                if (cookie.exactMatch(cookieEntry)) {
                    // expires == -1 means no expires defined. Otherwise
                    // negative means far future
                    if (cookie.expires < 0 || cookie.expires > now) {
                        // secure cookies can't be overwritten by non-HTTPS url
                        if (!cookieEntry.secure || HTTPS.equals(uri.getScheme())) {
                            cookieEntry.value = cookie.value;
                            cookieEntry.expires = cookie.expires;
                            cookieEntry.secure = cookie.secure;
                            cookieEntry.lastAcessTime = now;
                            cookieEntry.lastUpdateTime = now;
                            cookieEntry.mode = Cookie.MODE_REPLACED;
                        }
                    } else {
                        cookieEntry.lastUpdateTime = now;
                        cookieEntry.mode = Cookie.MODE_DELETED;
                    }
                    done = true;
                    break;
                }
            }

            // expires == -1 means no expires defined. Otherwise negative means
            // far future
            if (!done && (cookie.expires < 0 || cookie.expires > now)) {
                cookie.lastAcessTime = now;
                cookie.lastUpdateTime = now;
                cookie.mode = Cookie.MODE_NEW;
                if (cookieList.size() > MAX_COOKIE_COUNT_PER_BASE_DOMAIN) {
                    Cookie toDelete = new Cookie();
                    toDelete.lastAcessTime = now;
                    Iterator<Cookie> iter2 = cookieList.iterator();
                    while (iter2.hasNext()) {
                        Cookie cookieEntry2 = iter2.next();
                        if ((cookieEntry2.lastAcessTime < toDelete.lastAcessTime)
                                && cookieEntry2.mode != Cookie.MODE_DELETED) {
                            toDelete = cookieEntry2;
                        }
                    }
                    toDelete.mode = Cookie.MODE_DELETED;
                }
                cookieList.add(cookie);
            }
        }
    }

    /**
     * Get cookie(s) for a given url so that it can be set to "cookie:" in http
     * request header.
     *
     * @param url The url needs cookie
     * @return The cookies in the format of NAME=VALUE [; NAME=VALUE]
     */
    public String getCookie(String url) {
        WebAddress uri;
        try {
            uri = new WebAddress(url);
        } catch (ParseException ex) {
            Log.e(LOGTAG, "Bad address: " + url);
            return null;
        }

        return getCookie(uri);
    }

    /**
     * Get cookie(s) for a given uri so that it can be set to "cookie:" in http
     * request header.
     *
     * @param uri The uri needs cookie
     * @return The cookies in the format of NAME=VALUE [; NAME=VALUE]
     * @hide - hide this because it has a parameter of type WebAddress, which is a system private class.
     */
    public synchronized String getCookie(WebAddress uri) {
        if (!mAcceptCookie || uri == null) {
            return null;
        }

        String[] hostAndPath = getHostAndPath(uri);
        if (hostAndPath == null) {
            return null;
        }

        String baseDomain = getBaseDomain(hostAndPath[0]);
        CopyOnWriteArrayList<Cookie> cookieList = mCookieMap.get(baseDomain);
        if (cookieList == null) {
            cookieList = new CopyOnWriteArrayList<Cookie>();
            mCookieMap.put(baseDomain, cookieList);
        }

        long now = System.currentTimeMillis();
        boolean secure = HTTPS.equals(uri.getScheme());
        Iterator<Cookie> iter = cookieList.iterator();

        SortedSet<Cookie> cookieSet = new TreeSet<Cookie>(COMPARATOR);
        while (iter.hasNext()) {
            Cookie cookie = iter.next();
            if (cookie.domainMatch(hostAndPath[0]) &&
                    cookie.pathMatch(hostAndPath[1])
                    // expires == -1 means no expires defined. Otherwise
                    // negative means far future
                    && (cookie.expires < 0 || cookie.expires > now)
                    && (!cookie.secure || secure)
                    && cookie.mode != Cookie.MODE_DELETED) {
                cookie.lastAcessTime = now;
                cookieSet.add(cookie);
            }
        }

        StringBuilder ret = new StringBuilder(256);
        Iterator<Cookie> setIter = cookieSet.iterator();
        while (setIter.hasNext()) {
            Cookie cookie = setIter.next();
            if (ret.length() > 0) {
                ret.append(SEMICOLON);
                // according to RC2109, SEMICOLON is official separator,
                // but when log in yahoo.com, it needs WHITE_SPACE too.
                ret.append(WHITE_SPACE);
            }

            ret.append(cookie.name);
            if (cookie.value != null) {
                ret.append(EQUAL);
                ret.append(cookie.value);
            }
        }

        if (ret.length() > 0) {
            Log.v(LOGTAG, "getCookie: uri: " + uri + " value: " + ret);

            return ret.toString();
        } else {
            Log.v(LOGTAG, "getCookie: uri: " + uri
                    + " But can't find cookie.");

            return null;
        }
    }

    static {
        Arrays.sort(BAD_COUNTRY_2LDS);
    }

    /**
     * Package level class to be accessed by cookie sync manager
     */
    static class Cookie {

        static final byte MODE_NEW = 0;

        static final byte MODE_NORMAL = 1;

        static final byte MODE_DELETED = 2;

        static final byte MODE_REPLACED = 3;

        String domain;

        String path;

        String name;

        String value;

        long expires;

        long lastAcessTime;

        long lastUpdateTime;

        boolean secure;

        byte mode;

        Cookie() {
        }

        Cookie(String defaultDomain, String defaultPath) {
            domain = defaultDomain;
            path = defaultPath;
            expires = -1;
        }

        boolean exactMatch(Cookie in) {
            // An exact match means that domain, path, and name are equal. If
            // both values are null, the cookies match. If both values are
            // non-null, the cookies match. If one value is null and the other
            // is non-null, the cookies do not match (i.e. "foo=;" and "foo;")
            boolean valuesMatch = !((value == null) ^ (in.value == null));
            return domain.equals(in.domain) && path.equals(in.path) &&
                    name.equals(in.name) && valuesMatch;
        }

        boolean domainMatch(String urlHost) {
            if (domain.startsWith(".")) {
                if (urlHost.endsWith(domain.substring(1))) {
                    int len = domain.length();
                    int urlLen = urlHost.length();
                    if (urlLen > len - 1) {
                        // make sure bar.com doesn't match .ar.com
                        return urlHost.charAt(urlLen - len) == PERIOD;
                    }
                    return true;
                }
                return false;
            } else {
                // exact match if domain is not leading w/ dot
                return urlHost.equals(domain);
            }
        }

        boolean pathMatch(String urlPath) {
            if (urlPath.startsWith(path)) {
                int len = path.length();
                if (len == 0) {
                    Log.w(LOGTAG, "Empty cookie path");
                    return false;
                }
                int urlLen = urlPath.length();
                if (path.charAt(len - 1) != PATH_DELIM && urlLen > len) {
                    // make sure /wee doesn't match /we
                    return urlPath.charAt(len) == PATH_DELIM;
                }
                return true;
            }
            return false;
        }

        public String toString() {
            return "domain: " + domain + "; path: " + path + "; name: " + name
                    + "; value: " + value;
        }
    }

    /**
     * Extract the host and path out of a uri
     *
     * @param uri The given WebAddress
     * @return The host and path in the format of String[], String[0] is host which has at least two periods, String[1] is path which always ended with "/"
     */
    private String[] getHostAndPath(WebAddress uri) {
        if (uri.getHost() != null && uri.getPath() != null) {

            /*
             * The domain (i.e. host) portion of the cookie is supposed to be
             * case-insensitive. We will consistently return the domain in lower
             * case, which allows us to do the more efficient equals comparison
             * instead of equalIgnoreCase.
             *
             * See: http://www.ieft.org/rfc/rfc2965.txt (Section 3.3.3)
             */
            String[] ret = new String[2];
            ret[0] = uri.getHost().toLowerCase();
            ret[1] = uri.getPath();

            int index = ret[0].indexOf(PERIOD);
            if (index == -1) {
                if (uri.getScheme().equalsIgnoreCase("file")) {
                    // There is a potential bug where a local file path matches
                    // another file in the local web server directory. Still
                    // "localhost" is the best pseudo domain name.
                    ret[0] = "localhost";
                }
            } else if (index == ret[0].lastIndexOf(PERIOD)) {
                // cookie host must have at least two periods
                ret[0] = PERIOD + ret[0];
            }

            if (ret[1].charAt(0) != PATH_DELIM) {
                return null;
            }

            /*
             * find cookie path, e.g. for http://www.google.com, the path is "/"
             * for http://www.google.com/lab/, the path is "/lab"
             * for http://www.google.com/lab/foo, the path is "/lab/foo"
             * for http://www.google.com/lab?hl=en, the path is "/lab"
             * for http://www.google.com/lab.asp?hl=en, the path is "/lab.asp"
             * Note: the path from URI has at least one "/"
             * See:
             * http://www.unix.com.ua/rfc/rfc2109.html
             */
            index = ret[1].indexOf(QUESTION_MARK);
            if (index != -1) {
                ret[1] = ret[1].substring(0, index);
            }

            return ret;
        } else {
            return null;
        }
    }

    /**
     * Get the base domain for a give host. E.g. mail.google.com will return
     * google.com
     *
     * @param host The give host
     * @return the base domain
     */
    private String getBaseDomain(String host) {
        int startIndex = 0;
        int nextIndex = host.indexOf(PERIOD);
        int lastIndex = host.lastIndexOf(PERIOD);
        while (nextIndex < lastIndex) {
            startIndex = nextIndex + 1;
            nextIndex = host.indexOf(PERIOD, startIndex);
        }
        if (startIndex > 0) {
            return host.substring(startIndex);
        } else {
            return host;
        }
    }

    /**
     * parseCookie() parses the cookieString which is a comma-separated list of
     * one or more cookies in the format of "NAME=VALUE; expires=DATE;
     * path=PATH; domain=DOMAIN_NAME; secure httponly" to a list of Cookies.
     * Here is a sample: IGDND=1, IGPC=ET=UB8TSNwtDmQ:AF=0; expires=Sun,
     * 17-Jan-2038 19:14:07 GMT; path=/ig; domain=.google.com, =,
     * PREF=ID=408909b1b304593d:TM=1156459854:LM=1156459854:S=V-vCAU6Sh-gobCfO;
     * expires=Sun, 17-Jan-2038 19:14:07 GMT; path=/; domain=.google.com which
     * contains 3 cookies IGDND, IGPC, PREF and an empty cookie
     *
     * @param host The default host
     * @param path The default path
     * @param cookieString The string coming from "Set-Cookie:"
     * @return A list of Cookies
     */
    private CopyOnWriteArrayList<Cookie> parseCookie(String host, String path,
            String cookieString) {
        CopyOnWriteArrayList<Cookie> ret = new CopyOnWriteArrayList<Cookie>();

        int index = 0;
        int length = cookieString.length();
        while (true) {
            Cookie cookie = null;

            // done
            if (index < 0 || index >= length) {
                break;
            }

            // skip white space
            if (cookieString.charAt(index) == WHITE_SPACE) {
                index++;
                continue;
            }

            /*
             * get NAME=VALUE; pair. detecting the end of a pair is tricky, it
             * can be the end of a string, like "foo=bluh", it can be semicolon
             * like "foo=bluh;path=/"; or it can be enclosed by \", like
             * "foo=\"bluh bluh\";path=/"
             *
             * Note: in the case of "foo=bluh, bar=bluh;path=/", we interpret
             * it as one cookie instead of two cookies.
             */
            int semicolonIndex = cookieString.indexOf(SEMICOLON, index);
            int equalIndex = cookieString.indexOf(EQUAL, index);
            cookie = new Cookie(host, path);

            // Cookies like "testcookie; path=/;" are valid and used
            // (lovefilm.se).
            // Look for 2 cases:
            // 1. "foo" or "foo;" where equalIndex is -1
            // 2. "foo; path=..." where the first semicolon is before an equal
            //    and a semicolon exists.
            if ((semicolonIndex != -1 && (semicolonIndex < equalIndex)) ||
                    equalIndex == -1) {
                // Fix up the index in case we have a string like "testcookie"
                if (semicolonIndex == -1) {
                    semicolonIndex = length;
                }
                cookie.name = cookieString.substring(index, semicolonIndex);
                cookie.value = null;
            } else {
                cookie.name = cookieString.substring(index, equalIndex);
                // Make sure we do not throw an exception if the cookie is like
                // "foo="
                if ((equalIndex < length - 1) &&
                        (cookieString.charAt(equalIndex + 1) == QUOTATION)) {
                    index = cookieString.indexOf(QUOTATION, equalIndex + 2);
                    if (index == -1) {
                        // bad format, force return
                        break;
                    }
                }
                // Get the semicolon index again in case it was contained within
                // the quotations.
                semicolonIndex = cookieString.indexOf(SEMICOLON, index);
                if (semicolonIndex == -1) {
                    semicolonIndex = length;
                }
                if (semicolonIndex - equalIndex > MAX_COOKIE_LENGTH) {
                    // cookie is too big, trim it
                    cookie.value = cookieString.substring(equalIndex + 1,
                            equalIndex + 1 + MAX_COOKIE_LENGTH);
                } else if (equalIndex + 1 == semicolonIndex
                        || semicolonIndex < equalIndex) {
                    // this is an unusual case like "foo=;" or "foo="
                    cookie.value = "";
                } else {
                    cookie.value = cookieString.substring(equalIndex + 1,
                            semicolonIndex);
                }
            }
            // get attributes
            index = semicolonIndex;
            while (true) {
                // done
                if (index < 0 || index >= length) {
                    break;
                }

                // skip white space and semicolon
                if (cookieString.charAt(index) == WHITE_SPACE
                        || cookieString.charAt(index) == SEMICOLON) {
                    index++;
                    continue;
                }

                // comma means next cookie
                if (cookieString.charAt(index) == COMMA) {
                    index++;
                    break;
                }

                // "secure" is a known attribute doesn't use "=";
                // while sites like live.com uses "secure="
                if (length - index >= SECURE_LENGTH
                        && cookieString.substring(index, index + SECURE_LENGTH).
                        equalsIgnoreCase(SECURE)) {
                    index += SECURE_LENGTH;
                    cookie.secure = true;
                    if (index == length) {
                        break;
                    }
                    if (cookieString.charAt(index) == EQUAL) {
                        index++;
                    }
                    continue;
                }

                // "httponly" is a known attribute doesn't use "=";
                // while sites like live.com uses "httponly="
                if (length - index >= HTTP_ONLY_LENGTH
                        && cookieString.substring(index,
                        index + HTTP_ONLY_LENGTH).
                        equalsIgnoreCase(HTTP_ONLY)) {
                    index += HTTP_ONLY_LENGTH;
                    if (index == length) {
                        break;
                    }
                    if (cookieString.charAt(index) == EQUAL) {
                        index++;
                    }
                    // FIXME: currently only parse the attribute
                    continue;
                }
                equalIndex = cookieString.indexOf(EQUAL, index);
                if (equalIndex > 0) {
                    String name = cookieString.substring(index, equalIndex).toLowerCase();
                    int valueIndex = equalIndex + 1;
                    while (valueIndex < length && cookieString.charAt(valueIndex) == WHITE_SPACE) {
                        valueIndex++;
                    }

                    if (name.equals(EXPIRES)) {
                        int comaIndex = cookieString.indexOf(COMMA, equalIndex);

                        // skip ',' in (Wdy, DD-Mon-YYYY HH:MM:SS GMT) or
                        // (Weekday, DD-Mon-YY HH:MM:SS GMT) if it applies.
                        // "Wednesday" is the longest Weekday which has length 9
                        if ((comaIndex != -1) &&
                                (comaIndex - valueIndex <= 10)) {
                            index = comaIndex + 1;
                        }
                    }
                    semicolonIndex = cookieString.indexOf(SEMICOLON, index);
                    int commaIndex = cookieString.indexOf(COMMA, index);
                    if (semicolonIndex == -1 && commaIndex == -1) {
                        index = length;
                    } else if (semicolonIndex == -1) {
                        index = commaIndex;
                    } else if (commaIndex == -1) {
                        index = semicolonIndex;
                    } else {
                        index = Math.min(semicolonIndex, commaIndex);
                    }
                    String value = cookieString.substring(valueIndex, index);

                    // Strip quotes if they exist
                    if (value.length() > 2 && value.charAt(0) == QUOTATION) {
                        int endQuote = value.indexOf(QUOTATION, 1);
                        if (endQuote > 0) {
                            value = value.substring(1, endQuote);
                        }
                    }
                    if (name.equals(EXPIRES)) {
                        try {
                            cookie.expires = HttpDateTime.parse(value);
                        } catch (IllegalArgumentException ex) {
                            Log.e(LOGTAG,
                                    "illegal format for expires: " + value);
                        }
                    } else if (name.equals(MAX_AGE)) {
                        try {
                            cookie.expires = System.currentTimeMillis() + 1000
                                    * Long.parseLong(value);
                        } catch (NumberFormatException ex) {
                            Log.e(LOGTAG,
                                    "illegal format for max-age: " + value);
                        }
                    } else if (name.equals(PATH)) {
                        // only allow non-empty path value
                        if (value.length() > 0) {
                            cookie.path = value;
                        }
                    } else if (name.equals(DOMAIN)) {
                        int lastPeriod = value.lastIndexOf(PERIOD);
                        if (lastPeriod == 0) {
                            // disallow cookies set for TLDs like [.com]
                            cookie.domain = null;
                            continue;
                        }
                        try {
                            Integer.parseInt(value.substring(lastPeriod + 1));
                            // no wildcard for ip address match
                            if (!value.equals(host)) {
                                // no cross-site cookie
                                cookie.domain = null;
                            }
                            continue;
                        } catch (NumberFormatException ex) {
                            // ignore the exception, value is a host name
                        }
                        value = value.toLowerCase();
                        if (value.charAt(0) != PERIOD) {
                            // pre-pended dot to make it as a domain cookie
                            value = PERIOD + value;
                            lastPeriod++;
                        }
                        if (host.endsWith(value.substring(1))) {
                            int len = value.length();
                            int hostLen = host.length();
                            if (hostLen > (len - 1)
                                    && host.charAt(hostLen - len) != PERIOD) {
                                // make sure the bar.com doesn't match .ar.com
                                cookie.domain = null;
                                continue;
                            }
                            // disallow cookies set on ccTLDs like [.co.uk]
                            if ((len == lastPeriod + 3)
                                    && (len >= 6 && len <= 8)) {
                                String s = value.substring(1, lastPeriod);
                                if (Arrays.binarySearch(BAD_COUNTRY_2LDS, s) >= 0) {
                                    cookie.domain = null;
                                    continue;
                                }
                            }
                            cookie.domain = value;
                        } else {
                            // no cross-site or more specific sub-domain cookie
                            cookie.domain = null;
                        }
                    }
                } else {
                    // bad format, force return
                    index = length;
                }
            }
            if (cookie != null && cookie.domain != null) {
                ret.add(cookie);
            }
        }
        return ret;
    }

    private static final CookieComparator COMPARATOR = new CookieComparator();

    private static final class CookieComparator implements Comparator<Cookie> {

        public int compare(Cookie cookie1, Cookie cookie2) {
            // According to RFC 2109, multiple cookies are ordered in a way such
            // that those with more specific Path attributes precede those with
            // less specific. Ordering with respect to other attributes (e.g.,
            // Domain) is unspecified.
            // As Set is not modified if the two objects are same, we do want to
            // assign different value for each cookie.
            int diff = cookie2.path.length() - cookie1.path.length();
            if (diff != 0) {
                return diff;
            }

            diff = cookie2.domain.length() - cookie1.domain.length();
            if (diff != 0) {
                return diff;
            }

            // If cookie2 has a null value, it should come later in
            // the list.
            if (cookie2.value == null) {
                // If both cookies have null values, fall back to using the name
                // difference.
                if (cookie1.value != null) {
                    return -1;
                }
            } else if (cookie1.value == null) {
                // Now we know that cookie2 does not have a null value, if
                // cookie1 has a null value, place it later in the list.
                return 1;
            }

            // Fallback to comparing the name to ensure consistent order.
            return cookie1.name.compareTo(cookie2.name);
        }
    }

    //--------------------------------------------------------------------------------------------------

    /**
     * 保存cookie
     */
    public void saveCookieFile(Context context) {
        List<SaveCookies> list = new CopyOnWriteArrayList<>();
        Iterator<Entry<String, CopyOnWriteArrayList<Cookie>>> it = mCookieMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CopyOnWriteArrayList<Cookie>> entry = it.next();
            SaveCookies cookies = new SaveCookies();
            cookies.key = entry.getKey();
            cookies.cookies = entry.getValue();
            list.add(cookies);
        }
        if (mCookieController != null) {
            mCookieController.saveCookies(context, list);
        }
    }


    /**
     * 读取cookie
     */
    public void readCookieFile(Context context) {
        mCookieMap.clear();
        List<SaveCookies> list = null;
        if (mCookieController != null) {
            list = mCookieController.loadCookies(context);
        }
        if (list == null || list.isEmpty()) {
            Log.e(TAG, "readCookieFile parse Json failed");
            return;
        }
        for (SaveCookies cookies : list) {
            mCookieMap.put(cookies.key, cookies.cookies);
        }
        Log.v(TAG, "readCookieFile readSize: " + mCookieMap.size());
    }

    /**
     * 移除cookie
     */
    public void removeCookie(String url) {
        WebAddress uri;
        try {
            uri = new WebAddress(url);
        } catch (ParseException ex) {
            Log.e(LOGTAG, "Bad address: " + url);
            return;
        }
        String[] hostAndPath = getHostAndPath(uri);
        if (hostAndPath == null) {
            return;
        }
        String baseDomain = getBaseDomain(hostAndPath[0]);
        mCookieMap.remove(baseDomain);
    }

    /**
     * 清空所有cookie
     */
    public void removeAllCookie() {
        mCookieMap.clear();
    }

}
