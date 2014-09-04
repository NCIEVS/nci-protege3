package gov.nih.nci.protegex.workflow.wiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import edu.stanford.smi.protege.util.Log;
import gov.nih.nci.protegex.workflow.util.BaseXmlPullParser;

public class BiomedGTWiki {
    /** Wiki user name */
    protected static final String WIKI_USER = "Protege";
    
    /** Wiki password */
    protected static final String WIKI_PASSWORD = "postul8";
    
    /** Default WIKI URL */
    public static final String DEFAULT_WIKI_URL = "https://biomedgt.org";

    /** Log in portion of the URL */
    protected static final String USER_LOGIN = 
        "/index.php?title=Special:Userlogin";
    
    /** Required top level page begin processing */
    public static final String EXPORT_LIST = 
        "/index.php/Special:ExportList";

    /** Default namespace prefix. */
    public final static String DEFAULT_NS_PREFIX = "";
    
    /** Change status base portion of the url */
    private static final String CHANGE_STATUS = 
        "/index.php/Special:ChangeStatus/";
    
    /** Update portion of the URL */
    protected static final String ARTICLE_UPDATE = 
        "/index.php/Special:ArticleUpdate";
    
    /** The HttpClient */
    private HttpClient _httpClient = null;
    
    /**
     * wikiHost must be set for each run of any method in this class.
     * In keeping with HTTP it will be stateless.
     */
    protected String _wikiHost = DEFAULT_WIKI_URL;

    /** Logger */
    protected Logger _logger = Log.getLogger();
    
    public enum ProposalStatus {
        IN_PROGRESS("InProgress"),
        COMPLETED("Completed"),
        REJECTED("Rejected")
        ;

        // Name member variable and methods
        private String _name = null;
        public String getName() { return _name; }

        /**
         * Constructs this class.
         * @param name The name.
         */
        ProposalStatus(String name) {
            _name = name;
        }
        
        /**
         * Returns the column by the column name.
         * @param name The column name.
         * @return the column by the column name.
         */
        public static ProposalStatus find(String name) {
            for (ProposalStatus value : values())
                if (value.getName().equalsIgnoreCase(name))
                    return value;
            return null;
        }

        /**
         * Returns the list of statuses as strings.
         * @return the list of statuses as strings.
         */
        public static String[] getNames() {
            ArrayList<String> list = new ArrayList<String>();
            for (ProposalStatus value : values())
                list.add(value.getName());
            return list.toArray(new String[list.size()]);
        }
        
        /**
         * Returns the list of statuses.
         * @return the list of statuses.
         */
        public static ProposalStatus[] getList() {
            ArrayList<ProposalStatus> list = new ArrayList<ProposalStatus>();
            for (ProposalStatus value : values())
                list.add(value);
            return list.toArray(new ProposalStatus[list.size()]);
        }
    }

    public enum PackageStatus {
//      NEW("New"),
//      PENDING_RECEIPT("Pending Receipt"),
        IN_PROGRESS("InProgress"),
        COMPLETED("Completed"),
        COMPLETED_WITH_ISSUES("CompletedWithIssues"),
        ;

        // Name member variable and methods
        private String _name = null;
        public String getName() { return _name; }

        /**
         * Constructs this class.
         * @param name The name.
         */
        PackageStatus(String name) {
            _name = name;
        }
        
        /**
         * Returns the column by the column name.
         * @param name The column name.
         * @return the column by the column name.
         */
        public static PackageStatus find(String name) {
            for (PackageStatus value : values())
                if (value.getName().equalsIgnoreCase(name))
                    return value;
            return null;
        }

        /**
         * Returns the list of statuses as strings.
         * @return the list of statuses as strings.
         */
        public static String[] getNames() {
            ArrayList<String> list = new ArrayList<String>();
            for (PackageStatus value : values())
                list.add(value.getName());
            return list.toArray(new String[list.size()]);
        }
        
        /**
         * Returns the list of statuses.
         * @return the list of statuses.
         */
        public static PackageStatus[] getList() {
            ArrayList<PackageStatus> list = new ArrayList<PackageStatus>();
            for (PackageStatus value : values())
                list.add(value);
            return list.toArray(new PackageStatus[list.size()]);
        }
    }
    
    /**
     * Instantiates this class.
     * @param wikiHost The url for the wiki to talk to.
     */
    public BiomedGTWiki(String wikiHost) {
        _wikiHost = BaseXmlPullParser.stripSlashfromURL(wikiHost);
        debug("wikiHost: " + _wikiHost);
        _httpClient = new HttpClient();
    }

    /**
     * Displays debug messages.
     * @param text The text message.
     */
    protected void debug(String text) {
        _logger.info(text);
    }
    
    /**
     * Debugs the post method.
     * @param url The URL.
     * @param post The postMethod.
     * @param code The status code.
     * @throws IOException
     */
    private void debug(String url, PostMethod post, int code) throws IOException {
        String response = post.getResponseBodyAsString();

        debug("----------------------------------------" + 
            "----------------------------------------");
        debug("URL: " + url);
        debug("Response status code: " + code);
        
        String skipMsgs[] = new String[] { "<title>Login successful" };
        for (String msg : skipMsgs) {
            if (response.contains(msg)) {
                debug("Suppress message containing: " + msg);
                return;
            }
        }
        
        debug("Response body: ");
        debug(response);
    }

    /**
     * Posts URL to wiki.
     * @param login if true, logins in as a Protege user.
     * @param url The URL.
     * @return The wiki response.
     * @throws Exception
     */
    public String postToWiki(boolean login, String url) throws Exception {
        if (login)
            login(url);
        
        PostMethod post = new PostMethod(url);
        try {
            int code = _httpClient.executeMethod(post);
            debug(url, post, code);
        } finally {
            post.releaseConnection();
        }
        String response = post.getResponseBodyAsString();
        return response;
    }
    
    /**
     * Returns the host URL from the url.
     * @param url The URL.
     * @return the host URL from the url.
     */
    public static String getHostUrl(String url) {
        int i = url.indexOf("/index.php");
        if (i < 0)
            return "";
        String value = url.substring(0, i);
        return value;
    }
    
    /**
     * Returns the last token from the url ("/" delimited).
     * @param url The URL.
     * @return the last token from the url.
     */
    protected static String getLastToken(String url) {
        int i = url.lastIndexOf("/");
        if (i < 0)
            return "";
        String value = url.substring(i+1);
        return value;
    }
    
    /**
     * Returns the PostMethod associated with the login.
     * @param userid The user id.
     * @param passord The password.
     * @return the PostMethod associated with the login.
     */
    private PostMethod newLoginPost(String userid, String passord) {
        PostMethod post = new PostMethod(_wikiHost + USER_LOGIN);

        NameValuePair action = new NameValuePair("wpLoginattempt", "Log in");
        NameValuePair id = new NameValuePair("wpName", userid);
        NameValuePair pw = new NameValuePair("wpPassword", passord);
        post.setRequestBody(new NameValuePair[] { action, id, pw });
        return post;
    }
    
    /**
     * Logins into the Wiki.
     * @param url the URL.
     * @return The wiki response.
     * @throws Exception
     */
    private String login(String url) throws Exception {
        PostMethod post = newLoginPost(WIKI_USER, WIKI_PASSWORD);
        try {
            int code = _httpClient.executeMethod(post);
            debug("[login] " + url, post, code);
        } finally {
            post.releaseConnection();
        }
        String response = post.getResponseBodyAsString(); 
        return response;
    }
    
    /**
     * Updates a specific article page based on the wiki text.
     * @param url The proposal url.
     * @param xmlPacket contains the updated wiki text.
     * @return The wiki response.
     * @throws Exception
     */
    public String updateArticle(String url, String xmlPacket) throws Exception {
        //Notes: <hostUrl>/index.php/Special:ArticleUpdate
        
        if (xmlPacket == null || xmlPacket.length() <= 0)
            return "";
        login(url);
        
        String hostUrl = _wikiHost.replace("https", "http");
        String postUrl = hostUrl + ARTICLE_UPDATE;
        PostMethod post = new PostMethod(postUrl);
        try {
            post.setRequestEntity(new StringRequestEntity(
                xmlPacket, "application/xml", "UTF-8"));
            int code = _httpClient.executeMethod(post);
            debug(postUrl, post, code);
        } finally {
            post.releaseConnection();
        }
        String response = post.getResponseBodyAsString();
        if (! response.contains("<status>Success</status>"))
            throw new Exception("Failed updating article: " + postUrl
                + "\n" + response);
        return response;
    }
    
    /**
     * Updates package status.
     * @param url The package URL from Wiki.
     * @param status The new package status.
     * @return The wiki response.
     * @throws Exception
     */
    public String updatePackageStatus(String url, PackageStatus status)
        throws Exception {
        //Note: Title = Wiki name of Package document.
        //      <hostUrl>/index.php/Special:ChangeStatus/<title>?status=<status>
        String hostUrl = getHostUrl(url).replace("https", "http");
        String pkgName = getLastToken(url);
        String pkgStatus = "?status=" + status.getName();
        
        String postUrl = hostUrl + CHANGE_STATUS + pkgName + pkgStatus;
        String response = postToWiki(true, postUrl);
        if (! response.contains("<status>Success</status>"))
            throw new Exception("Failed updating package status: " + postUrl
                + "\n" + response);
        return response;
    }

    /**
     * Updates proposal status.
     * @param url The proposal URL from Wiki.
     * @param status The new proprosal status.
     * @return The wiki response.
     * @throws Exception
     */
    public String updateProposalStatus(String url, ProposalStatus status)
        throws Exception {
        //Note: Title = Wiki name of Proposal document.
        //      <hostUrl>/index.php/Special:ChangeStatus/<title>?status=<status>
        String hostUrl = getHostUrl(url).replace("https", "http");
        String propName = getLastToken(url);
        String propStatus = "?status=" + status.getName();
        
        String postUrl = hostUrl + CHANGE_STATUS + propName + propStatus;
        String response = postToWiki(true, postUrl);
        if (! response.contains("<status>Success</status>"))
            throw new Exception("Failed updating proposal status: " + postUrl 
                + "\n" + response);
        return response;
    }

    /**
     * Updates proposal status.
     * @param url The package URL from Wiki.
     * @param xmlPacket The XML containing the modeler's notes.
     * @return The wiki response.
     * @throws Exception
     */
    public String updateModelerNotes(String url, String xmlPacket)
        throws Exception {
        //Notes: Title = Wiki name of Package document. 
        //       <hostUrl>/index.php/Special:ChangeStatus/<title>?editorNotes
        if (xmlPacket == null || xmlPacket.length() <= 0)
            return "";
        login(url);
        
        String hostUrl = _wikiHost.replace("https", "http");
        String pkgName = getLastToken(url);
        String pkgNotes = "?editorNotes=true";
        String postUrl = hostUrl + CHANGE_STATUS + pkgName + pkgNotes;
        PostMethod post = new PostMethod(postUrl);
        try {
            post.setRequestEntity(new StringRequestEntity(
                xmlPacket, "application/xml", "UTF-8"));
            int code = _httpClient.executeMethod(post);
            debug(postUrl, post, code);
        } finally {
            post.releaseConnection();
        }
        String response = post.getResponseBodyAsString();
        if (! response.contains("<status>Success</status>"))
            throw new Exception("Failed updating article: " + postUrl
                + "\n" + response);
        return response;
    }
    
    /**
     * Checks to see if the namespace prefix is not blank.  If so, returns the
     *   default namespace prefix.  In addition, converts all the characters
     *   to uppercase.
     * @param nsPrefix The namespace prefix.
     * @return The namespace prefix.
     */
    public static String adjustNSPrefix(String nsPrefix) {
        if (nsPrefix.length() <= 0)
            nsPrefix = BiomedGTParser.DEFAULT_NS_PREFIX;
        nsPrefix = nsPrefix.toUpperCase();
        return nsPrefix;
    }
}
