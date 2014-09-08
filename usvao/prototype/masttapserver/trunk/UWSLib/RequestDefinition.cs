using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace UWSLib
{
    class RequestDefinition
    {
        private Args.Names argName;
        public Args.Names ArgName { get { return argName; } }

        private long jobNumber;
        public long JobNumber { get { return jobNumber; } }

        private string subArg;
        public string SubArg { get { return subArg; } }

        private System.Collections.Specialized.NameValueCollection inputParams;
        public System.Collections.Specialized.NameValueCollection InputParams { get { return inputParams; } }

        public RequestDefinition()
        {
            argName = Args.Names.INVALID_ARG;
            jobNumber = -1;
            inputParams = null;
            subArg = string.Empty;
        }

        public static RequestDefinition parseRequest(System.Collections.Specialized.NameValueCollection input, string restPath)
        {
            RequestDefinition def = new RequestDefinition();
            def.inputParams = input;

            try
            {
                if (restPath == "async/jobs") //special job-number-less case.
                {
                    def.argName = Args.Names.jobs;
                }
                else
                {
                    def.jobNumber = UWSLib.Args.findJobNumber(restPath);
                    Args.Names arg = Args.findArg(restPath);
                    if (arg != UWSLib.Args.Names.INVALID_ARG)
                    {
                        def.argName = arg;
                        def.subArg = UWSLib.Args.findSubArg(restPath);
                    }
                }
            }
            catch (Exception) { def = new RequestDefinition(); }

            return def;
        }
    }
}
