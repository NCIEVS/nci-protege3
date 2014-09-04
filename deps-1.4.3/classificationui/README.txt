# $Id: README.txt 123 2007-04-26 17:57:42Z masmith $ 
#

Setup
----
1) Unzip the distribution into Protege plugins directory. The zip file
   will create a directory named 
        com.clarkparsia.protege.explanation
   under the <Protege>/plugins directory with a set of jar files. If 
   you have extracted the contents to a different directory you can 
   simply copy this directory to the plugins directory in your
   Protege installation.

2) When a project is opened in Protege client, click 'Configure" and
   make sure the check box next to "Explanation Tab" is selected. A
   tab titled "Explanations" should be visible next to other Protege
   tabs.

3) Click "Explanations->Preferences" menu option and enter the URL
   and port number of the explanation server. For example, if the
   explanation server is running on the machine server.example.org
   with the default port number then enter:
       http://server.example.org:18080/explain/
   into the text box.  Please note, you must be sure to include the
   trailing slash after explain, otherwise the tab will be unable
   to find the server.

Using the Explanation Tab
---
   When the Explanation Tab is active select a class from the class 
   hierarchy. The sub and super classes of the selected class will 
   be shown in the lists next to the class hierarchy. When a subclass
   or superclass is selected in one of the lists, the client will 
   connect to the explanation server and retrieve the explanations
   for the selected subsclass relation. The axioms in the explanation
   will be shown inside the text box titled "Explanation".

   Note that, the sub and super classes shown by default are asserted
   sub and super classes. The explanations for asserted relations are
   generally very straightforward and only contain the asserted axiom.
   If the ontology is classified under Protege client using the
   "OWL->Classify taxonomy" menu option then you can click the tab
   titled "Inferred" under the explanation tab and you will see the
   inferred sub and super classes in the lists. Selecting one of these
   classes will return the explanation for the inferred relation.
