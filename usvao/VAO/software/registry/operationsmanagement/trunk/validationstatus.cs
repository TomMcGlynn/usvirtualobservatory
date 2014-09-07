using System;
using System.Collections.Generic;
using System.Collections;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Net;

namespace OperationsManagement
{
    public class validationStatus
    {
        private bool isValid = true;
        private ArrayList errors = new ArrayList();

        public validationStatus() { }
        public validationStatus(string error) { MarkInvalid(error); }

        public void MarkValid() { isValid = true; errors.Clear(); }
        public void MarkInvalid(string error) { isValid = false; errors.Add(error); }

        public bool IsValid { get { return isValid; } }
        public string[] GetErrors() { return (string[])errors.ToArray(typeof(string)); }

        public string GetConcatenatedErrors(string delimiter)
        {
            string resp = string.Empty;
            foreach (string error in errors)
            {
                resp += error + delimiter;
            }
            resp.Remove(resp.LastIndexOf(delimiter)); //laazy. fix.

            return resp;
        }

        public static validationStatus operator +(validationStatus c1, validationStatus c2)
        {
            validationStatus temp = new validationStatus();
            temp.isValid = c1.isValid && c2.isValid;
            temp.errors.AddRange(c1.errors);
            temp.errors.AddRange(c2.errors);

            return temp;
        }
    }
}