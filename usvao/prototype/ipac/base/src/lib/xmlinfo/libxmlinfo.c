/*************************************************************************

   Copyright (c) 2014, California Institute of Technology, Pasadena,
   California, under cooperative agreement 0834235 between the California
   Institute of Technology and the National Science  Foundation/National
   Aeronautics and Space Administration.

   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   of this BSD 3-clause license are met:

   1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This software was developed by the Infrared Processing and Analysis
   Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
   funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
   501(c)(3) organization registered in the District of Columbia and a
   collaborative effort of the Association of Universities for Research
   in Astronomy (AURA) and the Associated Universities, Inc. (AUI).

*************************************************************************/



#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include <scew.h>

#include <xmlinfo.h>


static char * trim(char * s)
{
    char * t;

    if (s == NULL)
        return NULL;
    while ( isspace((int) ((unsigned char) *s)) )
        ++s;
    t = s + strlen(s);
    while (t > s)
    {
        if ( isspace((int) ((unsigned char) *(t - 1))) == 0 )
        {
            *t = '\0';
            break;
        }
        --t;
    }
    return s;
}


int xmlinfo_parse_element_string(char const *str);

scew_tree   *tree   = NULL;
scew_parser *parser = NULL;

struct xmltags
{
   char *tag;
   int   index;
   int   isattr;
}
element[128];

int  nelement;

char errorText[4096];

int xmlinfo_debug = 0;


/***********************************************/
/*                                             */
/* XMLINFO_INIT                                */
/*                                             */
/* Initialize library.  Start up parser and    */
/* process input file.                         */
/*                                             */
/***********************************************/

int xmlinfo_init(char const *filename)
{
   parser = scew_parser_create();

   scew_parser_ignore_whitespaces(parser, 1);

   if (!scew_parser_load_file(parser, filename))
   {
      scew_error code = scew_error_code();

      if (code == scew_error_expat)
      {
        enum XML_Error expat_code = scew_error_expat_code(parser);

        sprintf(errorText, "Expat error #%d (line %d, column %d): %s",
        expat_code,
            scew_error_expat_line  (parser),
            scew_error_expat_column(parser),
            scew_error_expat_string(expat_code));

	return 1;
      }

      sprintf(errorText, "[struct stat=\"ERROR\", msg=\"Unable to load file (error #%d: %s)\"]\n",
    code, scew_error_string(code));

      return 1;
   }

   tree = scew_parser_tree(parser);

   return 0;
}



/***********************************************/
/*                                             */
/* XMLINFO_CLOSE                               */
/*                                             */
/* Close down library and free memory          */
/*                                             */
/***********************************************/

void xmlinfo_close()
{
   scew_tree_free(tree);
   scew_parser_free(parser);
}



/***********************************************/
/*                                             */
/* XMLINFO_VALUE                               */
/*                                             */
/* Return the value of an XML element or of a  */
/* attribute.  This 'value' is the non-tag     */
/* character contents of the element           */
/*                                             */
/***********************************************/

char *xmlinfo_value(char const *str)
{
   int i, count, found = 0;

   scew_element   *root      = NULL;
   scew_element   *current   = NULL;
   scew_element   *child     = NULL;
   scew_attribute *attribute = NULL;

   XML_Char const *contents  = NULL;

   xmlinfo_parse_element_string(str);

   if(xmlinfo_debug)
   {
      for(i=0; i<nelement; ++i)
     printf("XMLINFO_DEBUG> %3d: [%s][%d][%d]\n",
        i, element[i].tag, element[i].index, element[i].isattr);
   }

   root = scew_tree_root(tree);

   if(xmlinfo_debug)
      printf("XMLINFO_DEBUG> root = [%s]\n",  scew_element_name(root));

   if(strcmp(element[0].tag, scew_element_name(root)) != 0)
   {
      sprintf(errorText, "Incorrect root element '%s' (should be '%s')",
     element[0].tag, scew_element_name(root));

      return (char *)NULL;
   }

   current = root;
   found   = 1;

   for(i=1; i<nelement; ++i)
   {
      child = NULL;
      found = 0;
      count = 0;

      if(element[i].isattr)
      {
     attribute = NULL;

     while ((attribute = scew_attribute_next(current, attribute)) != NULL)
     {
        if(strcmp(scew_attribute_name(attribute), element[i].tag) == 0)
           return (char *)scew_attribute_value(attribute);
     }

     break;
      }

      else
      {
     while ((child = scew_element_next(current, child)) != NULL)
     {
        if(strcmp(element[i].tag, scew_element_name(child)) == 0)
        {
           if(element[i].index == count)
           {
          if(xmlinfo_debug)
             printf("XMLINFO_DEBUG> found %s[%d]\n",
            element[i].tag, element[i].index);

          found = 1;
          break;
           }
           else
          ++count;
        }
     }

     if(!found)
     {
        sprintf(errorText, "Couldn't find subelement %s[%d]",
           element[i].tag, element[i].index);

        return (char *)NULL;
     }

     current = child;
      }
   }

   if(found && !element[i].isattr)
   {
      contents = scew_element_contents(current);

      if (contents == NULL)
      {
     strcpy(errorText, "This element has no text content");

     return (char *)NULL;
      }

      else
     return (char *)contents;
   }

   return(char *)NULL;
}



/***********************************************/
/*                                             */
/* XMLINFO_COUNT                               */
/*                                             */
/* Return the number of elements of the given  */
/* name (i.e. if there are four 'abc.def'      */
/* tags, returns 4).                           */
/*                                             */
/***********************************************/

int xmlinfo_count(char const *str)
{
   int i, found, count = 0;

   scew_element   *root      = NULL;
   scew_element   *current   = NULL;
   scew_element   *child     = NULL;
   scew_attribute *attribute = NULL;

   xmlinfo_parse_element_string(str);

   if(xmlinfo_debug)
   {
      for(i=0; i<nelement; ++i)
     printf("XMLINFO_DEBUG> %3d: [%s][%d][%d]\n",
        i, element[i].tag, element[i].index, element[i].isattr);
   }

   root = scew_tree_root(tree);

   if(xmlinfo_debug)
      printf("XMLINFO_DEBUG> root = [%s]\n",  scew_element_name(root));

   if(strcmp(element[0].tag, scew_element_name(root)) != 0)
   {
      sprintf(errorText, "Incorrect root element '%s' (should be '%s')\n",
     element[0].tag, scew_element_name(root));

      return -1;
   }

   current = root;

   for(i=1; i<nelement; ++i)
   {
      found = 0;

      if(element[i].isattr)
      {
     attribute = NULL;

     while ((attribute = scew_attribute_next(current, attribute)) != NULL)
     {
        if(strcmp(scew_attribute_name(attribute), element[i].tag) == 0)
           return 1;
     }

     return 0;
      }

      else
      {
     count = 0;
     child = NULL;

     while ((child = scew_element_next(current, child)) != NULL)
     {
        if(strcmp(element[i].tag, scew_element_name(child)) == 0)
        {
           if(i == nelement-1)
           {
          found = 1;

          ++count;
           }

           else
           {
          if(element[i].index == count)
          {
             if(xmlinfo_debug)
            printf("XMLINFO_DEBUG> found %s[%d]\n",
               element[i].tag, element[i].index);

             found = 1;
             break;
          }
          else
             ++count;
           }
        }
     }

     if(!found)
     {
        sprintf(errorText,
           "[struct stat=\"ERROR\", msg=\"Couldn't find subelement %s[%d]\"]\n",
           element[i].tag, element[i].index);

        return -1;
     }

     current = child;
      }
   }

   return count;
}



/***********************************************/
/*                                             */
/* XMLINFO_PARSE_ELEMENT_STRING                */
/*                                             */
/* Used by other routines to turn a string of  */
/* the form abc.def[4].ghi(jkl)' into an array */
/* of subelements, indices, etc.               */
/*                                             */
/***********************************************/

int xmlinfo_parse_element_string(char const *str)
{
   char *ptr, *pindex;
   static char *pCopy = (char *)NULL;
   
   if(pCopy)
   {
      pCopy = realloc(pCopy, strlen(str) + 1);
      strcpy(pCopy, str);
   }
   else
   {
      pCopy = strdup(str);
   }

   /* initialize the element tags to zero */
   memset (element, 0, 128 * sizeof(struct xmltags));

   nelement = 0;
   ptr = trim(pCopy);

   while(1)
   {
      element[nelement].tag = ptr;

      while((   (*ptr >= 'a' && *ptr <= 'z')
         || (*ptr >= 'A' && *ptr <= 'Z')
         || (*ptr >= '0' && *ptr <= '9')
         || (*ptr == '_')
         || (*ptr == ':')) && *ptr != '\0')
     ++ptr;

      if(*ptr == '.' || *ptr == '\0')
      {
     element[nelement].index  = 0;
     element[nelement].isattr = 0;
     ++nelement;

     if(*ptr == '\0')
        break;

     *ptr = '\0';
     ++ptr;
      }

      else if(*ptr == '[')
      {
     *ptr = '\0';
     element[nelement].index  = 0;
     element[nelement].isattr = 0;

     ++ptr;

     pindex = ptr;

     while(*ptr != ']' && *ptr != '\0')
        ++ptr;

     *ptr = '\0';

     element[nelement].index = atoi(pindex);

     ++nelement;
     ++ptr;

     if(*ptr == '.')
        ++ptr;

     else if(*ptr == '(')
     {
        ++ptr;

        element[nelement].tag    = ptr;
        element[nelement].index  = 0;
        element[nelement].isattr = 1;

        while(*ptr != ')' && *ptr != '\0')
           ++ptr;

        *ptr = '\0';

        ++nelement;
        break;
     }

     else if(*ptr == '\0')
        break;
      }

      else if(*ptr == '(')
      {
     *ptr = '\0';
     element[nelement].index  = 0;
     element[nelement].isattr = 0;

     ++ptr;
     ++nelement;
     element[nelement].tag    = ptr;
     element[nelement].index  = 0;
     element[nelement].isattr = 1;

     while(*ptr != ')' && *ptr != '\0')
        ++ptr;

     *ptr = '\0';

     ++nelement;
     break;
      }

      else
      {
     return 1;
     break;
      }
   }

   return 0;
}



/***********************************************/
/*                                             */
/* XMLINFO_ERROR_TEXT                          */
/*                                             */
/* Returns the latest error message text       */
/*                                             */
/***********************************************/

char *xmlinfo_error()
{
   return errorText;
}
