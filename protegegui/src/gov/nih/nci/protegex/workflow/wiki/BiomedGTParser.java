package gov.nih.nci.protegex.workflow.wiki;

import java.util.ArrayList;

public class BiomedGTParser extends BiomedGTWiki {
    /**
     * If true, clears the packages from the wiki once the user imported them.
     */
    private boolean isClearPackagesFromWiki = true;
 
    /**
     * Instantiates this class.
     * @param wikiHost The url for the wiki to talk to.
     */
    public BiomedGTParser(String wikiHost) {
        super(wikiHost);
    }
    
    /**
     * Returns the list of package URLs.
     * @param topPage The top page when parsing.  When topPage is null,
     *   then this method uses the DEFAULT_EXPORT_LIST as its top page.
     * @return the list of package URLs.
     */
    private ArrayList<String> getPackageURLs(String topPage) throws Exception {
        if (topPage == null)
            topPage = EXPORT_LIST;
        String content = postToWiki(false, _wikiHost + topPage);
        BiomedGTPackageParser parser = new BiomedGTPackageParser();
        ArrayList<String> list = parser.processXml(content);
        return list;
    }
    
    /**
     * Returns the list of proposals.
     * @param importPackageURLs The list of packages URLs.
     * @return the list of proposals.
     */
    private ArrayList<Proposal> getProposals(ArrayList<String> importPackageURLs)
            throws Exception {
        ArrayList<Proposal> proposals = new ArrayList<Proposal>();
        for (String pkgURL : importPackageURLs) {
            String pkgContent = postToWiki(false, pkgURL);
            ArrayList<BiomedGTProposalParser.ProposalInfo> proposalURLs =
                new BiomedGTProposalParser().processXml(pkgContent);
            for (BiomedGTProposalParser.ProposalInfo propInfo : proposalURLs) {
                String propURL = propInfo.getUrl();
                String propContent = propURL;
                boolean isStructured = propInfo.isStructured();
                if (isStructured) { //(propURL.contains("Proposal:")
                    propContent = postToWiki(false, propURL);
                    propContent = removeRawTag(propContent);
                }
                proposals.add(new Proposal(pkgURL, propURL, propContent,
                    isStructured));
            }
        }
        return proposals;
    }
    
    /**
     * Marks the package and all its proposals as "In Progress".
     * @param pkgUrls The list of package URLs.
     * @param proposals The list of individual proposals.
     * @throws Exception
     */
    private void markPackageAsInProgress(ArrayList<String> pkgUrls,
        ArrayList<Proposal> proposals) throws Exception {
        if (! isClearPackagesFromWiki)
            return;
        
        for (Proposal proposal : proposals) 
            if (proposal.isStructured()) {
                String propUrl = proposal.getProposalURL();
                updateProposalStatus(propUrl, ProposalStatus.IN_PROGRESS);
            }
        for (String pkgUrl : pkgUrls)
            updatePackageStatus(pkgUrl, PackageStatus.IN_PROGRESS);
    }
    
    /**
     * Imports the proposals and their corresponding data from the wiki.
     * @param topPage The top page when parsing.  When topPage is null,
     *   then this method uses the DEFAULT_EXPORT_LIST as its top page.
     * @param nsPrefix The namespace prefix.
     * @return the list of proposals.
     */
    public ArrayList<Proposal> importPackets(String topPage, String nsPrefix) 
        throws Exception {
        ArrayList<String> pkgUrls = getPackageURLs(topPage);
        ArrayList<Proposal> proposals = getProposals(pkgUrls);
        verifyImport(proposals, nsPrefix);
        markPackageAsInProgress(pkgUrls, proposals);
        return proposals;
    }

    /**
     * Removed raw tag.  This is only used for debugging purposes.
     * @param xmlPacket The XML string.
     * @return the string with raw tag removed.
     */
    public static String removeRawTag(String xmlPacket) {
        int i = xmlPacket.indexOf("<raw");
        if (i >= 0) {
            xmlPacket = xmlPacket.substring(0, i);
            xmlPacket += "</rdf:RDF>";
        }
        return xmlPacket;
    }
    
    /**
     * Returns the concept name.
     * @param packageUrl The package URL.
     * @param packet The XML packet.
     * @return the concept name.
     */
    public String isAbout(String packageUrl, String packet) {
        String pkg = getLastToken(packageUrl);
        if (pkg != null && pkg.length() > 0) {
            pkg = skipAfterPhrase(pkg, "WorkFlow:");
            pkg = pkg + ": ";
        } else {
            pkg = "";
        }
        
        if (! isStructured(packet))
            return pkg + packet;

        String name = getConceptPreferredName(packet);
        if (name != null && name.length() > 0)
            return pkg + name;
        
        name = getConceptName(packet);
        if (name != null && name.length() > 0)
            return pkg + name;
        
        return pkg;
    }
    
    /**
     * Returns the concept name from the text. 
     * @param text The text string.
     * @return the concept name from the text.
     */
    private String getConceptName(String text) {
        int end = text.indexOf("</owl-class-name>");
        if (end < 0)
            return "";
        String value = text.substring(0, end);
        int start = value.lastIndexOf('>');
        value = value.substring(start+1);
        return value;
    }
    
    /**
     * Returns the preferred name.
     * @param text The text string.
     * @return the preferred name.
     */
    private String getConceptPreferredName(String text) {
        text = skipAfterPhrase(text, "P108");
        text = skipAfterPhrase(text, "</property-code>");
        text = skipAfterPhrase(text, ">");
        text = removeFromPhrase(text, "</property-value>");
        if (text != null)
            text = text.trim();
        return text;
    }
    
    /**
     * Finds the first occurrence of a specific phrase and returns the
     * text right after it.
     * @param text The text string.
     * @param phrase The phrase.
     * @return the text right after the phrase.
     */
    private String skipAfterPhrase(String text, String phrase) {
        if (text == null || text.length() <= 0)
            return "";
        int i = text.indexOf(phrase);
        if (i < 0)
            return "";
        text = text.substring(i + phrase.length());
        return text;
    }
    
    /**
     * Removes all the text from the first occurrence of the phrase.
     * @param text The text string.
     * @param phrase The phrase.
     * @return the modified text.
     */
    private String removeFromPhrase(String text, String phrase) {
        if (text == null || text.length() <= 0)
            return "";
        int i = text.indexOf(phrase);
        text = text.substring(0, i);
        return text;
    }

    /**
     * Returns true if XML packet is structured.
     * @param packet The XML packet.
     * @return true if XML packet is structured.
     */
    public boolean isStructured(String packet) {
        return packet.startsWith("<?xml") && 
            packet.indexOf("</rdf:RDF>") > 0;
    }
    
    /**
     * This class contains the data the parser parsed out.
     * @author David Yee
     */
    public class Proposal {
        private String _packageURL = "";
        private String _proposalURL = "";
        private String _content = "";
        private boolean _isStructured = false;
        
        public Proposal(String packageURL, String proposalURL, String content,
            boolean isStructured) {
            _packageURL = packageURL;
            _proposalURL = proposalURL;
            _content = content;
            _isStructured = isStructured;
        }
        
        public String getPackageURL() { return _packageURL; }
        public String getProposalURL() { return _proposalURL; }
        public String getContent() { return _content; }
        public boolean isStructured() { return _isStructured; }

    }

    /**
     * Verifies if the import is valid and checks to see if each proposal
     * is within the specified namespace. 
     * @param proposals The list of proposals.
     * @param nsPrefix The namespace prefix.
     * @throws Exception
     */
    private void verifyImport(ArrayList<Proposal> proposals, String nsPrefix) 
        throws Exception {
        for (BiomedGTParser.Proposal proposal : proposals) {
            boolean isStructured = proposal.isStructured();
            if (! isStructured)
                return;
            
            String pkgURL = proposal.getPackageURL();
            String content = proposal.getContent();
            if (content.length() <= 0) {
                throw new Exception(
                    "Import aborted because the following package contains" +
                    " a proposal with an empty content:\n" +
                    "    * " + pkgURL + "\n" +
                    "See proposal:\n" + 
                    "    * " + proposal.getProposalURL());
            }
            String prefix = getXmlProperty(content, "in-scheme");
            if (! prefix.equals(nsPrefix))
                throw new Exception(
                    "Import aborted because of conflicting namespaces.\n\n" + 
                    "One or more proposals from the following package:\n" +
                    "    * " + pkgURL + "\n" + 
                    "belongs to the " + prefix +  " namespace instead of " + 
                    nsPrefix + ".");
        }
    }
    
    /**
     * Returns the value of the xml property.
     * @param xmlPacket The xml packet.
     * @param xmlProperty The xml property name.
     * @return Returns the value of the xml property.
     */
    public static String getXmlProperty(String xmlPacket, String xmlProperty) {
        String s = xmlPacket;
        int i = s.indexOf("</" + xmlProperty + ">");
        if (i < 0)
            return "";
        s = s.substring(0, i);
        i = s.lastIndexOf(">");
        if (i < 0)
            return "";
        s = s.substring(i+1, s.length());
        return s;
    }
}