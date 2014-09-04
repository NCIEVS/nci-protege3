The NCI Protege Extensions are plugins to the stanford Protege application.  The build script creates a jar of the NCI Protege code and then puts all needed structures, config files, and supporting jars into a zip.  This zip is meant to be extracted into the root directory of the Protege install.  It will automatically create the needed folder in the Protege plugins directory.  It will also add a mysql driver to the jdbc driver library directory.

The extensions project depends on the Stanford developed Protege_core and Protege_OWL projects.  These projects can be found in CVS at:
Protege_core =  cbiocvs2.nci.nih.gov:/share/content/cvsroot/evs/Protege/Protege_core
Protege_OWL  =  cbiocvs2.nci.nih.gov:/share/content/cvsroot/evs/Protege/Protege_OWL

They are also available in Subversion from Stanford at
Protege_core =  http://smi-protege.stanford.edu/repos/protege/protege-core/branches/nci_june15/
Protege_OWL  =  http://smi-protege.stanford.edu/repos/protege/owl/branches/nci_june15/

Information about the NCI Protege Extensions project can be found at:
http://gforge.nci.nih.gov/projects/protegegui/

A Protege Extension Quick Start Guide to installing and running Protege can be found at
https://gforge.nci.nih.gov/docman/index.php?group_id=174&selected_doc_group_id=974&language_id=1