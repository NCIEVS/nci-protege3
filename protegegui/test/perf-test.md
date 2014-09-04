## A Simulator to stress Protege

This simple test harness is designed to test protege by exercising the
methods in OWLWrapper, the main data layer for NCI EditTab. The
script, run_sim.sh is built to run from the protege client install
directory, .eg.

    cd build/dist/Protege.Client-1.1.0

    ./run_sim.sh test1.config

It takes a single argument, the name of a config file that specifies
the test. The config will contain the number of clients to run along
with a spec for each client. For example, here's test1.config:

     no_clients=5
     client1.work=gov.nih.nci.protegex.test.CreateSubsUnderRoot
     client1.input=test1.in
     client1.no_exe=25
     client1.conn=127.0.0.1,Bob Dionne,bob,SmallBase3byName
     client2.work=gov.nih.nci.protegex.test.CreateSubsUnderRoot
     client2.input=test2.in
     client2.no_exe=25
     client2.conn=127.0.0.1,Bob Dionne,bob,SmallBase3byName
     client3.work=gov.nih.nci.protegex.test.CreateSubsUnderRoot
     client3.input=test3.in
     client3.no_exe=25
     client3.conn=127.0.0.1,Bob Dionne,bob,SmallBase3byName
     client4.work=gov.nih.nci.protegex.test.CreateSubsUnderRoot
     client4.input=test4.in
     client4.no_exe=25
     client4.conn=127.0.0.1,Bob Dionne,bob,SmallBase3byName
     client5.work=gov.nih.nci.protegex.test.CreateSubsUnderRoot
     client5.input=test5.in
     client5.no_exe=25
     client5.conn=127.0.0.1,Bob Dionne,bob,SmallBase3byName  

It specifies 5 clients, in this case they are all the same. Each
client is a java class that implements the following interface:

    public interface ClientWorker {

            public void init(OWLModel model, String fname);
            public void doWork(int iterate);
            public void cleanUp();

    }

The init method takes an OWLModel and an input file name. So
CreateSubsUnderRoot in it's init method opens the file testn.in and
uses the single name in there to create a root concept under
SyntaxRoot of SmallBase3byName. The method doWork uses the iterate
variable to create a sub class under that root. So for this simple
case CreateSubsUnderRoot creates a subclass under the given root. The
Simulator creates 5 threads, uses the connection info to call init for
each client, iterates however many time are specified, and lastly
calls cleanUp so that the client classes can do things like delete
classes. The Simulator only measures the total time spent in doWork,
ignoring init and cleanUp. Here's the results from running test1.config


    

     |---------------------+---------------+-----------------|
     | client              | no iterations | total time(sec) |
     |---------------------+---------------+-----------------|
     | CreateSubsUnderRoot |            25 |              50 |
     | CreateSubsUnderRoot |            25 |              55 |
     | CreateSubsUnderRoot |            25 |              57 |
     | CreateSubsUnderRoot |            25 |              52 |
     | CreateSubsUnderRoot |            25 |              62 |

Here's another run where each client is run 1000 times


    |---------------------+---------------+-----------------|
    | client              | no iterations | total time(sec) |
    |---------------------+---------------+-----------------|
    | CreateSubsUnderRoot |            25 |            2469 |
    | CreateSubsUnderRoot |            25 |            2509 |
    | CreateSubsUnderRoot |            25 |            2569 |
    | CreateSubsUnderRoot |            25 |            2588 |
    | CreateSubsUnderRoot |            25 |            2597 |
