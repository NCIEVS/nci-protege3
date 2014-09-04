# $Id: README.txt 659 2009-02-04 17:31:30Z masmith $ 
#

Setup
----
1) Unzip the distribution into a permanent directory.  All contents will
   be placed in an explanation sub-directory.  For the remainder of this
   document, the explanation sub-directory of the unzipped distribution
   will be referred to as the installation directory.

2) In the installation directory modify start_explanation_server.sh
   to point to the correct location of the Protege install.  Relative
   paths from this location are used to find jar files for Protege,
   Protege-OWL and the MySQL JDBC connector.

   Edit the line in the file that begins protege_install= by changing
   the path after the '=' sign to the correct path


Starting The Server
---
1) Change directory into the installation directory

   # cd /usr/local/explanation

2) Run the startup script with the appropriate command line options

   # sh start_explanation_server.sh \
   #  --protege-standalone /tmp/my-project.pprj

   Note that a full list of options and the default values they take if
   omitted on the command line is available using the --help option.

   # sh start_explanation_server.sh --help

   With no arguments, the server will start, but no data will be loaded
   into the explanation server.

   The startup script produces two files that are used by the stop
   and restart scripts. server.pid and server-args.txt should not be
   manually edited or removed.

   The startup script will immediately return, but the server will
   produce output during startup.  When output prints "Server started..."
   the startup process has been completed and the explanation server
   is ready to accept connections.

Stopping The Server
---
1) Change directory into the installation directory

   # cd /usr/local/explanation

2) Run the stop script with the no command line options

   # sh stop_explanation_server.sh

   This script depends on the server.pid file created during startup for
   proper performance.


Restarting The Server
---
1) Change directory into the installation directory

   # cd /usr/local/explanation

2) Run the restart script with the no command line options

   # sh restart_explanation_server.sh

   This script depends on the server.pid and server-args.txt files
   created during startup for proper performance.

   This script can be used to reload the ontology if the database is
   modified.
