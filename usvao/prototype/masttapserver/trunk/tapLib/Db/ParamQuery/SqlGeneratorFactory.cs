using System;
using System.Collections.Generic;

namespace tapLib.Db.ParamQuery {
    /// <summary>
    /// This class is an implementation of the ISqlGeneratorFactory interface.
    /// It provides ISqlGenerator implementations for specific tables.
    /// The implementation classes must be registered before use.  This can be done
    /// programmatically or it could be done from the Configuration file.
    /// </summary>
    public class SqlGeneratorFactory : ISqlGeneratorFactory {
        private readonly IDictionary<String, Type> _map = new Dictionary<string, Type>();

        /// <summary>
        /// Given a table name, the factory returns an instance of the generator class
        /// </summary>
        /// <param name="tableName"></param>
        /// <returns>an implementation of ISqlGenerator.  Null is returned if there is no 
        /// published generator for a table.
        ///</returns>
        public ISqlGenerator create(string tableName)
        {
            Type generatorType;
            // tableNames are mapped in lower case
            String lowerCaseTableName = tableName.ToLower();
            lock (_map)
            {
                if (!_map.ContainsKey(lowerCaseTableName)) return null;

                generatorType = _map[lowerCaseTableName];
                if (generatorType == null) return null;
            }
            ISqlGenerator generator = (ISqlGenerator)Activator.CreateInstance(generatorType);
            return generator;
        }

        /// <summary>
        /// The full path for a class implementing the ISqlGenerator interface must be
        /// published for each published table.  The class is checked for existence and
        /// whether or not it implements ISqlGenerator when it is published.  Exceptions are
        /// thrown if it does not exist or does not implement the right interface.
        /// </summary>
        /// <param name="tableName"></param>
        /// <param name="generatorClassFullPath"></param>
        public void publish(string tableName, String generatorClassFullPath)
        {
            // First true is to throw an exception of not found
            // Second true is to ignore case on the class name
            Type generatorType;
            try {
                generatorType = Type.GetType(generatorClassFullPath, true, true);
            }
            catch (Exception) {
                Console.WriteLine("Class name cannot be located: " + generatorClassFullPath);
                // Rethrow
                throw;
            }

            Type[] interfaces = generatorType.GetInterfaces();
            bool found = false;
            foreach (Type type in interfaces)
            {
                if (type.FullName.Contains("ISqlGenerator")) found = true;
            }
            if (!found) throw new ArgumentException("generator for: " + tableName + ", does not implement ISqlGenerator");

            // For table purposes, take the whole thing to lower case, I'm not sure if this is a good 
            // idea or not
            string lowerCaseTableName = tableName.ToLower();
            lock (_map)
            {
                if (_map.ContainsKey(lowerCaseTableName))
                {
                    _map.Remove(lowerCaseTableName);
                }
                _map.Add(lowerCaseTableName, generatorType);
            }
        }
    }
}
