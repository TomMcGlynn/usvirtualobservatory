using System;
using System.Collections.Generic;

namespace UWSLib
{
    public class Args
    {
        public enum Names
        {
            jobs,
            phase,
            quote,
            executionduration,
            destruction,
            error,
            parameters,
            results,
            owner,

            INVALID_ARG = -1,
            NO_ARG = -2
        }

        public static Names findArg(string uwspath)
        {
            Names arg = Names.INVALID_ARG;
            string value = String.Empty;

            int slashindex = uwspath.IndexOf('/');
            int nextslashindex = uwspath.IndexOf('/', slashindex + 1);
            if (slashindex > -1)
            {
                if (nextslashindex > -1)
                {
                    int subarg = uwspath.IndexOf('/', nextslashindex + 1);
                    if (subarg == -1)
                        value = uwspath.Substring(nextslashindex + 1).ToLower();
                    else
                        value = uwspath.Substring(nextslashindex + 1, subarg - nextslashindex -1);
                }
                else
                    return Names.NO_ARG;
            }

            if (Enum.IsDefined(typeof(Names), value))
                arg = (Names)Enum.Parse(typeof(Names), value, true); //case insensitive

            return arg;
        }

        public static string findSubArg(string uwspath)
        {
            String value = string.Empty;
            int slashindex = uwspath.IndexOf('/');
            int nextslashindex = uwspath.IndexOf('/', slashindex + 1);
            if (slashindex > -1)
            {
                if (nextslashindex > -1)
                {
                    string arg = uwspath.Substring(nextslashindex + 1).ToLower();
                    int subargindex = arg.IndexOf('/');
                    if (subargindex > -1)
                    {
                        value = arg.Substring(subargindex + 1);
                    }
                }
                else
                    value = string.Empty;
            }


            return value;
        }

        public static long findJobNumber(string uwspath)
        {
            long jobnum = -1;
            int slashindex = uwspath.IndexOf('/');
            if (slashindex > -1)
            {
                try
                {
                    int endslashindex = uwspath.IndexOf('/', slashindex + 1);
                    if( slashindex < endslashindex && endslashindex != -1 )
                        jobnum = Convert.ToInt64(uwspath.Substring(slashindex + 1, endslashindex - slashindex - 1));
                    else
                        jobnum = Convert.ToInt64(uwspath.Substring(slashindex + 1));
                }
                catch (Exception)
                {
                    jobnum = 0;
                }
            }

            return jobnum;
        }
    }
}
