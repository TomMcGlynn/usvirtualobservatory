Tasking Interface
--------------------------------

The ``voclient`` python package provides a special framework for
executing what are called *tasks*.  The motivation behind the tasking
framework is three-fold.  Perhaps most important is it provides a
means for importing existing (legacy) software tools into the
framework, making them executable from python.  In particular, a
number of existing VOClient tools currently available from the
command-line are also made available to Python through this framework.
Second of all, the framework has built-in support for asynchronous
execution; that is, one can start a task and potentially let it run
for a long time while the launching thread can go onto other activity.
This allows a task to take on complex and involved data processing
(and the associated information book-keeping), such as downloading
many datasets from multiple archives.  A task may manage long-lived
state--possibly across multiple calls to a task; an example would be
managing a local data cache used to collect datasets from archive.
Finally, because a task may run for a long time and do lots of complex
things, the information acquired as a result may be complex as well; a
task can return lots of arbitrary data as part of its results that
need not be constrained by a particular object model.

Tasks are imported as a collection referred to as *task packages*.
The ``VAOPackage`` task package (described below) ships with the
VOClient product; however, external task packages can be loaded
dynamically at runtime as well.  In particular, the tasking framework
provides an API for wrapping existing code to create new task
packages.  

The *task* is the heart of the framework.  A task is a computational
component which can be executed as a process in either the local
operating system or in a remote system (e.g. a cluster).  Tasks may be
written in any language so long as the defined tasking interface is
observed.  

Importing Tasks
+++++++++++++++++++++++++++

The tasks are imported by importing their task package.  Usually this
is done via a simple python ``import`` command on the module that
defines the task package.  For example, to import the ``VAOPackage``
task package, one can::

   import voclient.vaopackage as vaop

Consquently, the ``vosesame`` task would then be available as
``vaop.vosesame`` as with all the other tasks in the package.
Alternatively, one can just import a single task::

   from voclient.vaopackage import vosesame

In this example, ``vosesame`` is actually a *task function*, the
function that can be used to execute that task (see next section).  

It is worth noting that it is possible to import task packages that
are not available as modules on the Python ``system.path``.  Packages
located in arbitrary directories can be imported via the specialized 
task package import functions that are part of the
[``voclient.tasking``] module.  

.. note::
   Give details of the import functions here.

Executing a Task
+++++++++++++++++++++++++++

Users typically execute a task via a *task function*.  These are
called with the following form::

   result = taskfcn(*[posarg[,...]]*, *[kwarg[,...]]*, *[taskctl[,...]])

Here *posarg* is a positional argument, *kwarg* is a *key=value*-type
argument, and *taskctl* is a task control argument, such as
``async=True``.   The result is an ``OPset`` instance, an object with
dictionary semantics that represents an *output parameter set*.  The
positional and keyword arguments accepted are task-specific.  Common 
task control parameters include:

   =========   =======    =======================================
   Parameter   Type       Description
   =========   =======    =======================================
   async       boolean	  if True, execute asynchronously
   ---------   -------    ---------------------------------------
   execute     boolean    if True, execute immediately by default
   =========   =======    =======================================

The returned output parameter set (i.e. the ``OPset`` object) has four
key features:
  * it behaves like a python dictionary,
  * it can contain arbitrary, task-specific dictionary keys to hold
    the output information, 
  * it also contains a few object attributes that describe the overall 
    execution status,
  * the contents are filled asynchronously.

For example, one might poll the status of a task execution by
monitoring the ``exitstatus`` attribute::

  if not result.exitstatus:
     # not finished yet
     ...
  elsif result.exitstatus == result.ERROR:
     # complain
     print >> sys.stderr, "task error:", result.exitmsg
     result.close()
  elsif result.exitstatus == result.SUCCESS:
     # handle the results
     print result['ra'], result['dec']
     result.close()

With some tasks, it may be possible to process some of the results
before execution has finished.  

.. note::
   Show an example using a VAOPackage task.

Task Internals
+++++++++++++++++++++++++++

.. note::
   Show API for taking greater control over task execution.


.. _vao-package:

The VAO Task Package
+++++++++++++++++++++++++++

The VAO task package provides the a number of tasks that for doing
such things as 
  * resolving object names into positions,
  * manipulate VOTables, 
  * download data from archives, 
  * interact with other desktop tools

A summary of VAO tasks are as follows:

    ===========  =====================================================
    Task         Description
    ===========  =====================================================
    voregistry   VO Resource discovery
    -----------  -----------------------------------------------------
    vodata       General query and access to VO data
    -----------  -----------------------------------------------------
    vocatalog    Query VO Catalog services
    -----------  -----------------------------------------------------
    voimage      Query VO image services
    -----------  -----------------------------------------------------
    vospectrum   Query VO spectrum services
    -----------  -----------------------------------------------------
    voatlas      Multi-wavelength all-sky images
    -----------  -----------------------------------------------------
    voobslog     Query public observation logs
    -----------  -----------------------------------------------------
    vosloanspec  SDSS spectra data interface
    -----------  -----------------------------------------------------
    votcnv       Convert to/from VOTable format
    -----------  -----------------------------------------------------
    votget       Download data access references in a VOTable
    -----------  -----------------------------------------------------
    votinfo      Print information about a VOTable
    -----------  -----------------------------------------------------
    votpos       Extract positional information from a VOTable
    -----------  -----------------------------------------------------
    votselect    Select rows by expression
    -----------  -----------------------------------------------------
    votsort      Sort a VOTable by a column value
    -----------  -----------------------------------------------------
    votstat      Compute statistics for numeric columns in a VOTable
    -----------  -----------------------------------------------------
    votcat       Concatenate VOTable into single multi-resource table
    -----------  -----------------------------------------------------
    votjoin      Perform an inner-join between two VOTables
    -----------  -----------------------------------------------------
    votsplit     Split a multi-resource VOTable
    -----------  -----------------------------------------------------
    vosamp       SAMP utility command (sessions, messages, etc)
    -----------  -----------------------------------------------------
    voiminfo     Compute image footprints
    -----------  -----------------------------------------------------
    vosesame     Resolve object name to positions
    -----------  -----------------------------------------------------
    voskybot     List known moving objects in a field
    -----------  -----------------------------------------------------
    voxmatch     Cross-compare local table and VO data
    -----------  -----------------------------------------------------
    vosput       Put files to a VOSpace
    -----------  -----------------------------------------------------
    vosget       Get files from a VOSpace
    -----------  -----------------------------------------------------
    vosmove      Move files/nodes between VOSpaces
    -----------  -----------------------------------------------------
    voslist      List files/nodes in a VOSpace
    -----------  -----------------------------------------------------
    vosdelete    Delete files/nodes in a VOSpace
    ===========  =====================================================


Creating New Tasks
+++++++++++++++++++++++++++

Autobinding of Tasks to Python Functions
****************************************

.. note::

    Add a summary of the high level tasking interface here (V4 is
    described separately in a text file).  Binding of tasks to
    functions, examining packages and their tasks, standard modes of
    execution, use of the output pset.


