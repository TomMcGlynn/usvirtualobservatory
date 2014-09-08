using System;
using System.Runtime.Serialization;

namespace tapLib.Db.ParamQuery
{
    public class ParamQueryException : ApplicationException {
        public ParamQueryException() { }
        public ParamQueryException(String message) : base(message) { }
        public ParamQueryException(string message, Exception inner) : base(message, inner) { }
        protected ParamQueryException(SerializationInfo info,
                                      StreamingContext context)
            : base(info, context) { }

        public static ParamQueryException GenerateError(String format, params object[] args) {
            return new ParamQueryException(String.Format(format, args));
        }
    }
}