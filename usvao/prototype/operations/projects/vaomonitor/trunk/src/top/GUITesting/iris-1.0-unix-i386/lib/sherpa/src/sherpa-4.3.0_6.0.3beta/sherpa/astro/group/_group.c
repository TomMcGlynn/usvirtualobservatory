/*                                                                
**  Copyright (C) 2010  Smithsonian Astrophysical Observatory 
*/                                                                

/*                                                                          */
/*  This program is free software; you can redistribute it and/or modify    */
/*  it under the terms of the GNU General Public License as published by    */
/*  the Free Software Foundation; either version 2 of the License, or       */
/*  (at your option) any later version.                                     */
/*                                                                          */
/*  This program is distributed in the hope that it will be useful,         */
/*  but WITHOUT ANY WARRANTY; without even the implied warranty of          */
/*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           */
/*  GNU General Public License for more details.                            */
/*                                                                          */
/*  You should have received a copy of the GNU General Public License along */
/*  with this program; if not, write to the Free Software Foundation, Inc., */
/*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.             */
/*                                                                          */


/* H*****************************************************************
 *
 * FILE NAME:  pygrplib.c
 *
 * DEVELOPMENT: tools
 *
 * DESCRIPTION:  Provides a c-extension for the Python language to
 * the grplib library of functions
 *
 *
 *
 * REVISION HISTORY:
 *
 * Ref. No.         Date
   ----------       -----
   0.1              April2007 	File Created
H***************************************************************** */

#include "pygrplib.h"
#include "grplib.h"
#include "grp_priv.h"

/* * * * * * * * * * * * * * * * * * * * * * * *
 * Utilities for Python Interaction
 * * * * * * * * * * * * * * * * * * * * * * * */

/*
 * Set up the methods table
 */
static PyMethodDef groupMethods[] =
  {
    /* Each entry in the groupMethods array is a PyMethodDef structure containing
     * 1) the Python name,
     * 2) the C-function that implements the function,
     * 3) flags indicating whether or not keywords are accepted for this function,
     * and 4) The docstring for the function.
      */
    {"grpAdaptive", (PyCFunction)grpAdaptive,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpAdaptive(countsArray, minCounts [,maxLength, tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpAdaptiveSnr", (PyCFunction)grpAdaptiveSnr,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpAdaptiveSnr(countsArray, snr [,maxLength, tabStops, errorCol]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpBin", (PyCFunction)grpBin,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpBin(dataArray, binLowArray, binHighArray [,tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpBinFile", (PyCFunction)grpBinFile,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpBinFile(dataArray, fDataArray, fGroupingCol, fQualCol [,tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpBinWidth", (PyCFunction)grpBinWidth,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpBinWidth(numChans, binWidth [, tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpGetChansPerGroup", (PyCFunction)grpGetChansPerGroup,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  chanspergrp = grpGetChansPerGroup(groupCol) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpGetGroupSum", (PyCFunction)grpGetGroupSum,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  grpdata = grpGetGroupSum(dataArray, groupCol) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpGetGroupNum", (PyCFunction)grpGetGroupNum,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  grpnum = grpGetGroupNum(groupCol) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpMaxSlope", (PyCFunction)grpMaxSlope,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpMaxSlope(dataArray, binArray, slope [,maxLength, tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpMinSlope", (PyCFunction)grpMinSlope,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpMinSlope(dataArray, binArray, slope [,maxLength, tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpNumBins", (PyCFunction)grpNumBins,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpNumBins(numChans, numBins [, tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpNumCounts", (PyCFunction)grpNumCounts,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpNumCounts(countsArray, numCounts [,maxLength,tabStops]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {"grpSnr", (PyCFunction)grpSnr,
     METH_VARARGS | METH_KEYWORDS,
     "Example:  (grouping, quality) = grpSnr(countsArray, snr [,maxLength, tabStops,errorCol]) \n\
     The keywords for the function are the parameters shown in 'Example.'"},
    {NULL, NULL, 0, NULL}     /* Sentinel - marks the end of this structure */
  };/*end...groupMethods */

/*
 * Initialize the module
 * */
void init_group()
{
  (void) Py_InitModule("_group", groupMethods);
  import_array();  /* Must be present for NumPy.  Called first after above line. */
}

/* * * * * * * * * * * * * * * * * * * * * * * *
 * Function Definitions
 * * * * * * * * * * * * * * * * * * * * * * * */

/*
 * This function returns the grouping and quality arrays that represent the
 * input data (countsArray) after it has been adaptively grouped so that
 * each group contains at least numCounts counts.
 */
static PyObject *grpAdaptive(PyObject *self,	/*i: Used by Python */
                             PyObject *args,	/*i: Python tuple of the arguments */
                             PyObject *kwds   /*i: Python tuple of keywords */)
{
  double maxLength = 0;   /* number of elements that can be combined into a group */
  double minCounts = 0;   /* how many counts to contain in each group */
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  int numChans;           /* number of channels in groupCol and qualCol */
  int numTabs      = 0;  	/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *c_countsArray = NULL;  /* countsArray in c-style array */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_countsArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops    = NULL;  /*  The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping       = NULL;  /* The result obtained from grplib.c */
  PyArrayObject *quality        = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] =
    {"countsArray", "minCounts", "maxLength", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!d|dO!", kwlist,
                                   &PyArray_Type, &py_countsArray, &minCounts, /* mandatory args */
                                   &maxLength, &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if (py_countsArray == NULL)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_countsArray->descr->type_num, NPY_DOUBLE))
		{
			py_countsArray = (PyArrayObject *)PyArray_Cast(py_countsArray, NPY_DOUBLE);
			c_countsArray  = DDATA(py_countsArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "countsArray is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */
  if (minCounts <= 0)
  {
    PyErr_SetString(PyExc_ValueError, "minCounts values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  numChans = py_countsArray->dimensions[0];  /* the number of channels is the size of the py_countsArray */
  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "tabStops and countsArray have differing length.");
    return NULL;
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol  == NULL) || (groupCol == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  /* Function called from grplib.c */
  isError = grp_do_adaptive(c_countsArray, numChans, minCounts, groupCol,
                            qualCol, c_tabStops, maxLength, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);/* free the allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpAdaptive*/

/*
 * This function returns the grouping and quality arrays that represent the
 * input data (countsArray) after it has been adaptively grouped so that the
 * signal to noise of each group is at least equal to the snr parameter. The
 * errorCol array gives the error for each element of the original array: if
 * it is not supplied then the error is taken to be the square root of the
 * element value.
 */
static PyObject *grpAdaptiveSnr(PyObject *self,	/*i: Used by Python */
                                PyObject *args,	/*i: Python tuple of the arguments */
                                PyObject *kwds  /*i: Python tuple of keywords */)
{
  double snr       = 0;  	/* signal to noise parameter */
  double maxLength = 0;   /* number of elements that can be combined into a group */
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  int numChans;           /* number of channels in groupCol and qualCol */
  int numTabs      = 0; 	/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *c_countsArray = NULL;  /* countsArray in c-style array */
  double *c_errorCol    = NULL;  /* errorCol in c-style array */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  short useErrCols		  = 0;		 /* value indicating if a errorCol argument was passed to the function */
  int numErrs						= 0;		 /* number of errors in errorCol */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_countsArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_errorCol    = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops    = NULL;  /* The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping       = NULL;  /* The result obtained from grp_do_snr in grplib.c */
  PyArrayObject *quality        = NULL;  /* The result obtained from grp_do_snr in grplib.c */

  static char *kwlist[] =
    {"countsArray", "snr", "maxLength", "tabStops", "errorCol", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!d|dO!O!", kwlist,
                                   &PyArray_Type, &py_countsArray, &snr, /* mandatory args */
                                   &maxLength, &PyArray_Type, &py_tabStops, &PyArray_Type, &py_errorCol /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if (py_countsArray == NULL)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_countsArray->descr->type_num, NPY_DOUBLE))
		{
			py_countsArray = (PyArrayObject *)PyArray_Cast(py_countsArray, NPY_DOUBLE);
			c_countsArray  = DDATA(py_countsArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "countsArray is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */
  if (snr <= 0)
  {
    PyErr_SetString(PyExc_ValueError, "Scalar values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  if (py_errorCol != NULL)
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_errorCol->descr->type_num, NPY_DOUBLE))
		{
			py_errorCol = (PyArrayObject *)PyArray_Cast(py_errorCol, NPY_DOUBLE);
			c_errorCol  = DDATA(py_errorCol);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "errorCol is an incompatible type.");
	    return NULL;
		}/*end else... */
    useErrCols = 1; /* set value to true since we have a errorCol array */
    numErrs = py_errorCol->dimensions[0];  /* the number of tabs is the size of the py_errorCol */
  }/*end if... */

  numChans = py_countsArray->dimensions[0];  /* the number of channels is the size of the py_countsArray */
  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "The tabStops and countsArray have differing sizes.");
    return NULL;
  }/*end if... */
  if (useErrCols && (numErrs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "The errorCol and countsArray have differing sizes.");
    return NULL;
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol == NULL) || (groupCol == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!useErrCols)
  {
    c_errorCol = (double *) calloc(numChans, sizeof(double));
    if (c_errorCol == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  ii = 0;
  while (!useErrCols && (ii < numChans))
  {
    c_errorCol[ii] = 1.0;  /*fill errorCol with 1's */
    ii++;
  }/*end while... */

  /* Function called from grplib.c */
  isError = grp_do_adaptive_snr(c_countsArray, numChans, snr, groupCol,
                                qualCol, c_tabStops, c_errorCol, useErrCols, maxLength, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);/* free the allocated memory */
  free(qualCol);
  if (!useErrCols)
  {
    free(c_errorCol);
  }/*end if... */
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpAdaptiveSnr*/

/*
 * This function returns the grouping and quality arrays for a set of groups
 * defined by the low (binLowArray) and high (binHighArray) boundaries when
 * applied to the axis values of the data (axisArray).
 */
static PyObject *grpBin(PyObject *self,	/*i: Used by Python */
                        PyObject *args,	/*i: Python tuple of the arguments */
                        PyObject *kwds  /*i: Python tuple of keywords */)
{
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  int numChans;           /* number of channels in groupCol and qualCol */
  int numBins      = -1;	/* number of bins provided by binlow and binhigh */
  int numTabs      = 0; 	/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  int colRealFlag  = 0; 	/* value determining if dataArray values are ints or reals */
  double *c_dataArray    = NULL;  /* dataArray in c-style array */
  double *c_binLowArray  = NULL;  /* binLowArray in c-style array */
  double *c_binHighArray = NULL;  /* binHighArray in c-style array */
  double *groupData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData       = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol        = NULL;  /* the GROUPING column */
  short *qualCol         = NULL;  /* the QUALITY column */
  short *c_tabStops      = NULL;  /* elements that should be ignored */
  int isTabStops		     = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_dataArray    = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_binLowArray  = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_binHighArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops     = NULL;  /*  The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping        = NULL;  /* The result obtained from grplib.c */
  PyArrayObject *quality         = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] =
    {"dataArray", "binLowArray", "binHighArray", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!O!O!|O!", kwlist,
                                   &PyArray_Type, &py_dataArray, &PyArray_Type, &py_binLowArray, /* mandatory args */
                                   &PyArray_Type, &py_binHighArray, /* mandatory args */
                                   &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if ((py_dataArray == NULL) || (py_binLowArray == NULL) || (py_binHighArray == NULL))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_dataArray->descr->type_num, NPY_DOUBLE)
				&& (PyArray_CanCastSafely(py_binLowArray->descr->type_num, NPY_DOUBLE))
				&& (PyArray_CanCastSafely(py_binHighArray->descr->type_num, NPY_DOUBLE)))
		{
			/* determine if dataArray values are ints or reals before casting*/
		  /* NOTE: int value for NPY_FLOAT  = 11
		   * 			 int value for NPY_DOUBLE = 12
		   * 			 int value for NPY_LONG   = 7 (which corresponds to c-type INT) */
		  if (((py_dataArray->descr->type_num) == NPY_FLOAT) || ((py_dataArray->descr->type_num) == NPY_DOUBLE))
		  {
		    colRealFlag = 1;
		  }/*end if... */

			py_dataArray = (PyArrayObject *)PyArray_Cast(py_dataArray, NPY_DOUBLE);
			c_dataArray  = DDATA(py_dataArray);
			py_binLowArray = (PyArrayObject *)PyArray_Cast(py_binLowArray, NPY_DOUBLE);
			c_binLowArray  = DDATA(py_binLowArray);
			py_binHighArray = (PyArrayObject *)PyArray_Cast(py_binHighArray, NPY_DOUBLE);
			c_binHighArray  = DDATA(py_binHighArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "Array is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  numChans = py_dataArray->dimensions[0];
  numBins  = py_binLowArray->dimensions[0]; /* the number of channels is the size of the py_binLowArray */
  /* check to see if binlow has same size as binhigh */
  if (py_binLowArray->dimensions[0] != py_binHighArray->dimensions[0])
  {
    PyErr_SetString(PyExc_ValueError, "binLowArray and binHighArray have differing length.");
    return NULL;
  }/*end if... */
  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "tabStops and countsArray have differing length.");
    return NULL;
  }/*end if... */

  /* allocate memory arrays */
  groupCol       = (short *) calloc(numChans, sizeof(short));
  qualCol        = (short *) calloc(numChans, sizeof(short));
  if ((qualCol    == NULL) || (groupCol   == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  /* Function called from grplib.c */
  isError = grp_do_bin(c_dataArray, numChans, c_binLowArray, c_binHighArray,
                       numBins, groupCol, qualCol, c_tabStops, NULL, 0, colRealFlag);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);    /* free the allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpBin*/

/*
 * This function allows you to calculate the grouping information needed to
 * group the input data (the axisArray array) to match the grouping of another
 * dataset
 */
static PyObject *grpBinFile(PyObject *self,	/*i: Used by Python */
                            PyObject *args,	/*i: Python tuple of the arguments */
                            PyObject *kwds  /*i: Python tuple of keywords */)
{
  int ii;                     /* loop variable */
  int isError;                /* captures return value from grplib.c */
  int numChans;               /* number of channels in groupCol and qualCol */
  int fNumChans;							/* number of channels in the file data */
  int numTabs           = 0; 	/* number of tabs in tabStop */
  npy_intp dims[1];                /* the dimensions of the arrays */
  npy_intp typenum;                /* the typenum */
  int colRealFlag       = 0; 	/* value determining if dataArray values are ints or reals */
  double *c_dataArray   = NULL;  /* dataArray in c-style array */
  double *c_fDataArray  = NULL;  /* fDataArray in c-style array */
  short *c_fGroupCol    = NULL;	 /* short int column of grouping data argument */
  short *c_fQualCol     = NULL;  /* short int column of quality data argument */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_dataArray  = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_fDataArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_fGroupCol  = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_fQualCol   = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops   = NULL;  /*  The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping      = NULL;  /* The result obtained from grplib.c */
  PyArrayObject *quality       = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] =
    {"dataArray", "fDataArray", "fGroupCol", "fQualCol", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!O!O!O!|O!", kwlist,
                                   &PyArray_Type, &py_dataArray, &PyArray_Type, &py_fDataArray, /* mandatory args */
                                   &PyArray_Type, &py_fGroupCol, &PyArray_Type, &py_fQualCol, /* mandatory args */
                                   &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if ((py_dataArray == NULL) || (py_fDataArray == NULL))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if((PyArray_CanCastSafely(py_dataArray->descr->type_num, NPY_DOUBLE))
				&& (PyArray_CanCastSafely(py_fDataArray->descr->type_num, NPY_DOUBLE)))
		{
			/* determine if dataArray values are ints or reals before casting */
		  /* NOTE: int value for NPY_FLOAT  = 11
		   * 			 int value for NPY_DOUBLE = 12
		   * 			 int value for NPY_LONG   = 7 (which corresponds to c-type INT) */
		  if (((py_dataArray->descr->type_num) == NPY_FLOAT) || ((py_dataArray->descr->type_num) == NPY_DOUBLE))
		  {
		    colRealFlag = 1;
		  }/*end if... */

			py_dataArray = (PyArrayObject *)PyArray_Cast(py_dataArray, NPY_DOUBLE);
			c_dataArray  = DDATA(py_dataArray);
			py_fDataArray = (PyArrayObject *)PyArray_Cast(py_fDataArray, NPY_DOUBLE);
			c_fDataArray  = DDATA(py_fDataArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "Array is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */

  if((py_fGroupCol == NULL) || (py_fQualCol == NULL))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		if((py_fGroupCol->descr->type_num >= 17) || (py_fQualCol->descr->type_num >= 17))
  	{/*types 17 and above include strings and other non-numerical values */
  		PyErr_SetString(PyExc_TypeError, "Col is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_fGroupCol = (PyArrayObject *)PyArray_Cast(py_fGroupCol, NPY_SHORT);
		c_fGroupCol  = SDATA(py_fGroupCol);
		py_fQualCol  = (PyArrayObject *)PyArray_Cast(py_fQualCol, NPY_SHORT);
		c_fQualCol   = SDATA(py_fQualCol);
  }/*end else... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  numChans   = py_dataArray->dimensions[0];
  fNumChans  = py_fDataArray->dimensions[0]; /* the number of channels is the size of the py_fDataArray */
  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "tabStops and countsArray have differing length.");
    return NULL;
  }/*end if... */

  /* allocate memory arrays */
  groupCol    = (short *) calloc(numChans, sizeof(short));
  qualCol     = (short *) calloc(numChans, sizeof(short));
  if ((qualCol    == NULL) || (groupCol   == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  /* call the function from grplib.c */
  isError = grp_do_bin_file(c_dataArray, numChans, groupCol, qualCol,
                            c_tabStops, c_fDataArray, fNumChans, c_fGroupCol,
                            c_fQualCol, colRealFlag, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);  /* free the allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpBinFile*/

/*
 * This function returns the grouping and quality arrays that represent an
 * array of numChans elements in which the groups are each grpWidth elements
 * wide.
 */
static PyObject *grpBinWidth(PyObject *self,	/*i: Used by Python */
                             PyObject *args,	/*i: Python tuple of the arguments */
                             PyObject *kwds  /*i: Python tuple of keywords */)
{
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  long numChans = 0;      /* number of channels in groupCol and qualCol */
  long binWidth = 0;			/* number of bins */
  int numTabs   = 0;			/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_tabStops = NULL;  /*  The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping    = NULL;  /* The result obtained from grplib.c */
  PyArrayObject *quality     = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] = {"numChans", "binWidth", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "ll|O!", kwlist,
                                   &numChans, &binWidth, /* mandatory args */
                                   &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if ((numChans <= 0) || (binWidth <= 0))
  {
    PyErr_SetString(PyExc_ValueError, "Scalar values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol    == NULL) || (groupCol   == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "the numTabs and numChans have different sizes.");
    return NULL;
  }/*end if... */

  /* Function called from grplib.c */
  isError =  grp_do_bin_width(numChans, binWidth, groupCol, qualCol, c_tabStops, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);  /*free allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpBinWidth*/

/*
 * This function returnes the number of channels (i.e. elements) in each
 * group. The return value is an array whose length equals that of the input
 * data (the dataArray argument) and each element within a group contains the
 * same value.
 */
static PyObject *grpGetChansPerGroup(PyObject *self,	/*i: Used by Python */
                                     PyObject *args,	/*i: Python tuple of the arguments */
                                     PyObject *kwds  /*i: Python tuple of keywords */)
{
  int ii;                  /* loop variable */
  int isError;             /* captures return value from grplib.c */
  long numChans       = 0; /* number of channels in groupCol and qualCol */
  npy_intp dims[1];             /* the dimensions of the arrays */
  npy_intp typenum;             /* the typenum */
  long *chansPerGrpCol   = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *groupData 	   = NULL;  /* value used to store values while creating output */
  short *c_groupCol      = NULL;  /* short int of c_groupCol */

  PyArrayObject *py_groupCol = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *chansPerGrp = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] = {"groupCol", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!", kwlist, &PyArray_Type, &py_groupCol))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if (py_groupCol == NULL)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
  	if(py_groupCol->descr->type_num >= 17)/*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "groupCol is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_groupCol = (PyArrayObject *)PyArray_Cast(py_groupCol, NPY_SHORT);
		c_groupCol  = SDATA(py_groupCol);
  }/*end else... */

  numChans = py_groupCol->dimensions[0];

  /* allocate memory for arrays */
  chansPerGrpCol = (long *) calloc(numChans, sizeof(long));
  if (chansPerGrpCol == NULL)
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */

  /* Function called from grplib.c */
  isError = set_chans_per_grp(c_groupCol, chansPerGrpCol, numChans);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  chansPerGrp = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if (NULL == chansPerGrp)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */

  groupData = DDATA(chansPerGrp);

  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = chansPerGrpCol[ii]; /*grab the data from groupCol and place in grouping data */
  }/*end for... */

  free(chansPerGrpCol);  /*free allocated memory */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("O", PyArray_Return(chansPerGrp));
}/*end...grpGetChansPerGroup*/

/*
 * This function applies the grouping information from the grouping parameter
 * to the dataArray parameter. The return value is an array whose length
 * equals that of the input data (the dataArray argument) and each element
 * within a group contains the same value.
 */
static PyObject *grpGetGroupSum(PyObject *self,	/*i: Used by Python */
                                PyObject *args,	/*i: Python tuple of the arguments */
                                PyObject *kwds  /*i: Python tuple of keywords */)
{
  int ii;                  /* loop variable */
  int isError;             /* captures return value from grplib.c */
  long numChans       = 0; /* number of channels in groupCol and qualCol */
  npy_intp dims[1];             /* the dimensions of the arrays */
  npy_intp typenum;             /* the typenum */
  double *grpDataCol  = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *groupData 	= NULL;  /* value used to store values while creating output */
  short *c_groupCol   = NULL;  /* short int of c_groupCol */
  double *c_dataArray = NULL;  /*  The data array as a c-style array */

  PyArrayObject *py_dataArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_groupCol  = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *grpData      = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] = {"dataArray", "groupCol", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!O!", kwlist,
                                   &PyArray_Type, &py_dataArray, &PyArray_Type, &py_groupCol))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if ((py_dataArray == NULL) || (py_groupCol == NULL))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_dataArray->descr->type_num, NPY_DOUBLE))
		{
			py_dataArray = (PyArrayObject *)PyArray_Cast(py_dataArray, NPY_DOUBLE);
			c_dataArray  = DDATA(py_dataArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "dataArray is an incompatible type.");
	    return NULL;
		}/*end else... */

		if(py_groupCol->descr->type_num >= 17)/*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "groupCol is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_groupCol = (PyArrayObject *)PyArray_Cast(py_groupCol, NPY_SHORT);
		c_groupCol  = SDATA(py_groupCol);
  }/*end else... */

  numChans = py_dataArray->dimensions[0];

  /* allocate memory for arrays */
  grpDataCol = (double *) calloc(numChans, sizeof(double));
  if (grpDataCol == NULL)
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */

  /* Function called from grplib.c */
  isError = set_grp_data(c_dataArray, c_groupCol, grpDataCol, numChans);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grpData = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if (NULL == grpData)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */

  groupData = DDATA(grpData);

  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = grpDataCol[ii]; /*grab the data from groupCol and place in grouping data */
  }/*end for... */

  free(grpDataCol);/*free allocated memory */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("O", PyArray_Return(grpData));
}/*end...grpGetGroupSum*/

/*
 * his function calculates which group each element in the input array
 * belongs to, where the groups are numbered from 1. The return value is
 * an array whose length equals that of the input data (the grouping argument)
 * and each element within a group contains the same value.
 */
static PyObject *grpGetGroupNum(PyObject *self,	/*i: Used by Python */
                                PyObject *args,	/*i: Python tuple of the arguments */
                                PyObject *kwds  /*i: Python tuple of keywords */)
{
  int ii;                  /* loop variable */
  int isError;             /* captures return value from grplib.c */
  long numChans       = 0; /* number of channels in groupCol and qualCol */
  npy_intp dims[1];             /* the dimensions of the arrays */
  npy_intp typenum;             /* the typenum */
  long *grpNumCol        = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *groupData 	   = NULL;  /* value used to store values while creating output */
  short *c_groupCol      = NULL;  /* short int of c_groupCol */

  PyArrayObject *py_groupCol = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *grpNum = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] = {"groupCol", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!", kwlist, &PyArray_Type, &py_groupCol))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if (py_groupCol == NULL)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
  	if((py_groupCol->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "groupCol is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_groupCol = (PyArrayObject *)PyArray_Cast(py_groupCol, NPY_SHORT);
		c_groupCol  = SDATA(py_groupCol);
  }/*end else... */

  numChans = py_groupCol->dimensions[0];

  /* allocate memory for arrays */
  grpNumCol = (long *) calloc(numChans, sizeof(long));
  if (grpNumCol == NULL)
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */

  /* Function called from grplib.c */
  isError = set_grp_num(c_groupCol, grpNumCol, numChans);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grpNum = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if (NULL == grpNum)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */

  groupData = DDATA(grpNum);

  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = grpNumCol[ii]; /*grab the data from groupCol and place in grouping data */
  }/*end for... */

  free(grpNumCol);  /*free allocated memory */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("O", PyArray_Return(grpNum));
}/*end...grpGetGroupNum*/

/*
 * In this routine, groups are created when the absolute value of the slope
 * of the input data (the axisArray and binArray arguments) is less than
 * the threshold value (the slope argument).
 */
static PyObject *grpMaxSlope(PyObject *self,	/*i: Used by Python */
                             PyObject *args,	/*i: Python tuple of the arguments */
                             PyObject *kwds  /*i: Python tuple of keywords */)
{
  double maxLength = 0;   /* number of elements that can be combined into a group */
  double slope     = 0;   /* the value of the slope to limit the grouping */
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  int numChans;           /* number of channels in groupCol and qualCol */
  int numBins;						/* number of bins in the binArray */
  int numTabs = 0;				/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *c_dataArray   = NULL;  /* dataArray in c-style array */
  double *c_binArray    = NULL;  /* binArray in c-style array */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_dataArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_binArray  = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops  = NULL;  /*  The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping     = NULL;  /* The result obtained from grplib.c */
  PyArrayObject *quality      = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] =
    {"dataArray", "binArray", "slope", "maxLength", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!O!d|dO!", kwlist,
                                   &PyArray_Type, &py_dataArray, &PyArray_Type, &py_binArray, &slope, /* mandatory args */
                                   &maxLength, &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if ((py_dataArray == NULL) || (py_binArray == NULL))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_dataArray->descr->type_num, NPY_DOUBLE)
				&& (PyArray_CanCastSafely(py_binArray->descr->type_num, NPY_DOUBLE)))
		{
			py_dataArray = (PyArrayObject *)PyArray_Cast(py_dataArray, NPY_DOUBLE);
			c_dataArray  = DDATA(py_dataArray);
			py_binArray  = (PyArrayObject *)PyArray_Cast(py_binArray, NPY_DOUBLE);
			c_binArray   = DDATA(py_binArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "Array is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */
  if (slope <= 0)
  {
    PyErr_SetString(PyExc_ValueError, "Scalar values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  numChans = py_dataArray->dimensions[0];  /* the number of channels is the size of the py_dataArray */
  numBins  = py_binArray->dimensions[0];  /* the number of bins is the size of the py_binArray */

  if (numBins != numChans)
  {
    PyErr_SetString(PyExc_ValueError, "The binArray and dataArray have differing length.");
    return NULL;
  }/*end if... */

  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "tabStops and dataArray have differing length.");
    return NULL;
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol    == NULL) || (groupCol   == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  isError =  grp_do_max_slope(c_dataArray, c_binArray, numChans,
                              slope, groupCol, qualCol, c_tabStops, maxLength, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);  /*free allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpMaxSlope*/

/*
 * In this routine, groups are created when the absolute value of the slope of
 * the input data (the axisArray and binArray arguments) is more than the
 * threshold value (the slope argument).
 */
static PyObject *grpMinSlope(PyObject *self,	/*i: Used by Python */
                             PyObject *args,	/*i: Python tuple of the arguments */
                             PyObject *kwds  /*i: Python tuple of keywords */)
{
  double maxLength = 0;   /* number of elements that can be combined into a group */
  double slope     = 0;   /* the value of the slope to limit the grouping */
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  int numChans;           /* number of channels in groupCol and qualCol */
  int numBins;						/* number of bins in the binArray */
  int numTabs = 0;				/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *c_dataArray   = NULL;  /* dataArray in c-style array */
  double *c_binArray    = NULL;  /* binArray in c-style array */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_dataArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_binArray  = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops  = NULL;  /*  The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping     = NULL;  /* The result obtained from grplib.c */
  PyArrayObject *quality      = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] =
    {"dataArray", "binArray", "slope", "maxLength", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!O!d|dO!", kwlist,
                                   &PyArray_Type, &py_dataArray, &PyArray_Type, &py_binArray, &slope, /* mandatory args */
                                   &maxLength, &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if ((py_dataArray == NULL) || (py_binArray == NULL))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_dataArray->descr->type_num, NPY_DOUBLE)
				&& (PyArray_CanCastSafely(py_binArray->descr->type_num, NPY_DOUBLE)))
		{
			py_dataArray = (PyArrayObject *)PyArray_Cast(py_dataArray, NPY_DOUBLE);
			c_dataArray  = DDATA(py_dataArray);
			py_binArray  = (PyArrayObject *)PyArray_Cast(py_binArray, NPY_DOUBLE);
			c_binArray   = DDATA(py_binArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "Array is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */
  if (slope <= 0)
  {
    PyErr_SetString(PyExc_ValueError, "Scalar values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  numChans = py_dataArray->dimensions[0];  /* the number of channels is the size of the py_dataArray */
  numBins  = py_binArray->dimensions[0];  /* the number of bins is the size of the py_binArray */

  if (numBins != numChans)
  {
    PyErr_SetString(PyExc_ValueError, "The binArray and dataArray have differing length.");
    return NULL;
  }/*end if... */

  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "tabStops and dataArray have differing length.");
    return NULL;
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol    == NULL) || (groupCol   == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  isError =  grp_do_min_slope(c_dataArray, c_binArray, numChans,
                              slope, groupCol, qualCol, c_tabStops, maxLength, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);  /*free allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpMinSlope*/

/*
 * This function returns the grouping and quality arrays that represent an
 * array of numChans elements grouped into numGroups groups.
 */
static PyObject *grpNumBins(PyObject *self,	/*i: Used by Python */
                            PyObject *args,	/*i: Python tuple of the arguments */
                            PyObject *kwds  /*i: Python tuple of keywords */)
{
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  long numChans = 0;      /* number of channels in groupCol and qualCol */
  long numBins  = 0;			/* number of bins */
  int numTabs   = 0;  		/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_tabStops = NULL;  /*  The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping    = NULL;  /* The result obtained from grplib.c */
  PyArrayObject *quality     = NULL;  /* The result obtained from grplib.c */

  static char *kwlist[] =
    {"numChans", "numBins", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "ll|O!", kwlist,
                                   &numChans, &numBins, /* mandatory args */
                                   &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if ((numChans <= 0) || (numBins <= 0))
  {
    PyErr_SetString(PyExc_ValueError, "Scalar values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol    == NULL) || (groupCol   == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "tabStops and numChans have differing sizes.");
    return NULL;
  }/*end if... */

  /* Function called from grplib.c */
  isError =  grp_do_num_bins(numChans, numBins, groupCol, qualCol, c_tabStops, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);   /*free allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpNumBins*/


/*
 * This function returns the grouping and quality arrays that represent
 * the input data (countsArray) after it has been grouped so that each
 * group contains at least numCounts counts. The optional parameters
 * maxLength and tabStops represent the maximum number of elements that
 * can be combined and an array representing those elements that should
 * be ignored respectively.
 */
static PyObject *grpNumCounts(PyObject *self,	/*i: Used by Python */
                              PyObject *args,	/*i: Python tuple of the arguments */
                              PyObject *kwds  /*i: Python tuple of keywords */)
{
  double maxLength = 0;   /* number of elements that can be combined into a group */
  double numCounts = 0;   /* how many counts to contain in each group */
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grp_do_num_counts in grplib.c */
  int numChans;           /* number of channels in groupCol and qualCol */
  int numTabs      = 0;		/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *c_countsArray = NULL;  /* countsArray in c-style array */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_countsArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops    = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *grouping       = NULL;  /* The result obtained from grp_do_num_counts in grplib.c */
  PyArrayObject *quality        = NULL;  /* The result obtained from grp_do_num_counts in grplib.c */

  static char *kwlist[] =
    {"countsArray", "numCounts", "maxLength", "tabStops", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!d|dO!", kwlist,
                                   &PyArray_Type, &py_countsArray, &numCounts, /* mandatory args */
                                   &maxLength, &PyArray_Type, &py_tabStops     /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if (py_countsArray == NULL)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_countsArray->descr->type_num, NPY_DOUBLE))
		{
			py_countsArray = (PyArrayObject *)PyArray_Cast(py_countsArray, NPY_DOUBLE);
			c_countsArray  = DDATA(py_countsArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "countsArray is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */

  if (numCounts <= 0)
  {
    PyErr_SetString(PyExc_ValueError, "Scalar values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  numChans = py_countsArray->dimensions[0];  /* the number of channels is the size of the py_countsArray */
  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "tabStops and countsArray have differing length.");
    return NULL;
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol    == NULL) || (groupCol   == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  /* Function called from grplib.c */
  isError = grp_do_num_counts(c_countsArray, numChans, numCounts, groupCol,
                              qualCol, c_tabStops, maxLength, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol);    /* free the allocated memory */
  free(qualCol);
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpNumCounts*/


/*
 * This function returns the grouping and quality arrays that represent
 * the input data (countsArray) after it has been grouped so that the signal
 * to noise of each group is at least equal to the snr parameter. The
 * optional parameters maxLength and tabStops represent the maximum number
 * of elements that can be combined into a group and an array representing
 * those elements that should be ignored respectively. The errorCol array
 * gives the error for each element of the original array: if it is not
 * supplied then the error is taken to be the square root of the element value.
 */
static PyObject *grpSnr(PyObject *self,	/*i: Used by Python */
                        PyObject *args,	/*i: Python tuple of the arguments */
                        PyObject *kwds  /*i: Python tuple of keywords */)
{
  double snr       = 0;  	/* signal to noise parameter */
  double maxLength = 0;   /* number of elements that can be combined into a group */
  int ii;                 /* loop variable */
  int isError;            /* captures return value from grplib.c */
  int numChans;           /* number of channels in groupCol and qualCol */
  int numTabs      = 0; 	/* number of tabs in tabStop */
  npy_intp dims[1];            /* the dimensions of the arrays */
  npy_intp typenum;            /* the typenum */
  double *c_countsArray = NULL;  /* countsArray in c-style array */
  double *c_errorCol    = NULL;  /* errorCol in c-style array */
  double *groupData     = NULL;  /* used to store values when converting from c-style array to numpy array */
  double *qualData      = NULL;  /* used to store values when converting from c-style array to numpy array */
  short *groupCol       = NULL;  /* the GROUPING column */
  short *qualCol        = NULL;  /* the QUALITY column */
  short *c_tabStops     = NULL;  /* elements that should be ignored */
  short useErrCols		  = 0;		 /* value indicating if a errorCol argument was passed to the function */
  int numErrs						= 0;		 /* number of errors in errorCol */
  int isTabStops		    = 0;     /* a tabStop argument is passed in */

  PyArrayObject *py_countsArray = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_errorCol    = NULL;  /* Python Array Object that will be converted to a c-style array for processing */
  PyArrayObject *py_tabStops    = NULL;  /* The Python Object that will be turn into a numpy array Object */
  PyArrayObject *grouping       = NULL;  /* The result obtained from grp_do_snr in grplib.c */
  PyArrayObject *quality        = NULL;  /* The result obtained from grp_do_snr in grplib.c */

  static char *kwlist[] =
    {"countsArray", "snr", "maxLength", "tabStops", "errorCol", NULL};

  if (!PyArg_ParseTupleAndKeywords(args, kwds, "O!d|dO!O!", kwlist,
                                   &PyArray_Type, &py_countsArray, &snr, /* mandatory args */
                                   &maxLength, &PyArray_Type, &py_tabStops, &PyArray_Type, &py_errorCol /* optional keyword args*/))
  {
    PyErr_SetString(PyExc_Exception, "Could not parse arguments.");
    return NULL;
  }/*end if... */
  if (py_countsArray == NULL)
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  else
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_countsArray->descr->type_num, NPY_DOUBLE))
		{
			py_countsArray = (PyArrayObject *)PyArray_Cast(py_countsArray, NPY_DOUBLE);
			c_countsArray  = DDATA(py_countsArray);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "countsArray is an incompatible type.");
	    return NULL;
		}/*end else... */
  }/*end else... */
  if (snr <= 0)
  {
    PyErr_SetString(PyExc_ValueError, "Scalar values must be > zero.");
    return NULL;
  }/*end if... */

  if (py_tabStops != NULL)/* if a tabStop array is present */
  {
  	if((py_tabStops->descr->type_num) >= 17) /*types 17 and above include strings and other non-numerical values */
  	{
  		PyErr_SetString(PyExc_TypeError, "tabStops is an incompatible type.");
	    return NULL;
  	}/*end if... */
		py_tabStops = (PyArrayObject *)PyArray_Cast(py_tabStops, NPY_SHORT);
		c_tabStops  = SDATA(py_tabStops);
    numTabs     = py_tabStops->dimensions[0]; /* the number of tabs is the size of the py_tabStops */
    isTabStops  = 1; /* set value to true since we have a tabStop array */
  }/*end if... */

  if (py_errorCol != NULL)
  {
		/* Make sure the arrays are of correct type */
		if(PyArray_CanCastSafely(py_errorCol->descr->type_num, NPY_DOUBLE))
		{
			py_errorCol = (PyArrayObject *)PyArray_Cast(py_errorCol, NPY_DOUBLE);
			c_errorCol  = DDATA(py_errorCol);
		}/*end if... */
		else
		{
			PyErr_SetString(PyExc_TypeError, "errorCol is an incompatible type.");
	    return NULL;
		}/*end else... */
    useErrCols = 1; /* set value to true since we have a errorCol array */
    numErrs = py_errorCol->dimensions[0];  /* the number of tabs is the size of the py_errorCol */
  }/*end if... */

  numChans = py_countsArray->dimensions[0];  /* the number of channels is the size of the py_countsArray */
  if (isTabStops && (numTabs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "The tabStops and countsArray have differing sizes.");
    return NULL;
  }/*end if... */
  if (useErrCols && (numErrs != numChans))
  {
    PyErr_SetString(PyExc_ValueError, "The errorCol and countsArray have differing sizes.");
    return NULL;
  }/*end if... */

  /* allocate memory for arrays */
  groupCol      = (short *) calloc(numChans, sizeof(short));
  qualCol       = (short *) calloc(numChans, sizeof(short));
  if ((qualCol == NULL) || (groupCol == NULL))
  {
    PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
    return NULL;
  }/*end if... */
  if (!useErrCols)
  {
    c_errorCol = (double *) calloc(numChans, sizeof(double));
    if (c_errorCol == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */
  if (!isTabStops)
  {
    c_tabStops = (short *) calloc(numChans, sizeof(short));
    if (c_tabStops == NULL)
    {
      PyErr_SetString(PyExc_MemoryError, "Could not allocate memory.");
      return NULL;
    }/*end if... */
  }/*end if... */

  ii = 0;
  while (!useErrCols && (ii < numChans))
  {
    c_errorCol[ii] = 1.0;  /*fill errorCol with 1's */
    ii++;
  }/*end while... */

  /* Function called from grplib.c */
  isError = grp_do_snr(c_countsArray, numChans, snr, groupCol,
                       qualCol, c_tabStops, c_errorCol, useErrCols, maxLength, NULL);

  dims[0] = numChans;
  typenum = NPY_DOUBLE;

  /* create the output arrays from the data returned in groupCol and qualCol */
  grouping = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  quality  = (PyArrayObject *)PyArray_SimpleNew(1, dims, typenum);
  if ((NULL == grouping) || (NULL == quality))
  {
    PyErr_SetString(PyExc_Exception, "Could not create array object.");
    return NULL;
  }/*end if... */
  groupData = DDATA(grouping);
  qualData  = DDATA(quality);
  for (ii = 0; ii < numChans; ii++)
  {
    groupData[ii] = groupCol[ii]; /*grab the data from groupCol and place in grouping data */
    qualData[ii] = qualCol[ii];  /*grab the data from qualCol and place in quality data */
  }/*end for... */

  free(groupCol); /* free the allocated memory */
  free(qualCol);
  if (!useErrCols)
  {
    free(c_errorCol);
  }/*end if... */
  if (!isTabStops)
  {
    free(c_tabStops);
  }/*end if... */

  /* Return grouping and quality NumPy arrays */
  return Py_BuildValue("OO", PyArray_Return(grouping), PyArray_Return(quality));
}/*end...grpSnr*/
