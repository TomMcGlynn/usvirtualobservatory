using System;
using tapLib.Args;

namespace tapLib.Db.ParamQuery {

    /// <summary>
    /// Interface for the SqlGenerator factory.  Each published table
    /// must have a registered generator to create its SQL.
    /// </summary>
    public interface ISqlGeneratorFactory {
        /// <summary>
        /// Factory returns an instance of ISqlGenerator that will construct
        /// SQL for the table based on the QueryArg and OneQuery.
        /// </summary>
        /// <param name="tableName">registered table name</param>
        /// <returns>an instance of ISqlGenerator or null if nothing is 
        /// registered for the table
        /// </returns>
        ISqlGenerator create(String tableName);

        /// <summary>
        /// Adds a generator for a tableName.  The full path of the
        /// class should be given (as returned by typeof.fullName
        /// Exceptions are thrown if the class doesn't exist or doesn't
        /// implement ISqlGenerator
        /// </summary>
        /// <param name="tableName">the name of the table</param>
        /// <param name="generatorFullName">the fullPath of the class to create</param>
        void publish(String tableName, String generatorFullName);
    }

    /// <summary>
    /// This interface must be implemented by any class that generates SQL for a table.
    /// The class should do the majority of creating SQL based upon the QueryArg in the 
    /// generateSQL method that will be called once.  The two ToSQL methods are called to
    /// return SQL based on whether or not the needed query has position arguments or not.    
    /// </summary>
    public interface ISqlGenerator {

        /// <summary>
        /// Return the tableName for this Generator. 
        /// </summary>
        String tableName { get; }

        /// <summary>
        /// Create the SQL template for the contents of the QueryArg.
        /// Classes that implement ISqlGenerator can throw the 
        /// ParamQueryException with problems.  Callers should catch this exception.
        /// </summary>
        /// <param name="queryArg">A valid QueryArg</param>
        /// <returns>true if the query was constructed with no problems</returns>
        Boolean generateSQL(TapQueryArgs queryArg);

        /// <summary>
        /// Return SQL based on the template using the position information in OneQuery.
        /// An implementation of this method can throw ParamQueryException
        /// </summary>
        /// <returns>A String of SQL that can be executed</returns>
        String ToSQL(TapPos pos, TapSizeArg size, TapRegionArg region, TapMTimeArg mtime);

        /// <summary>
        /// Return SQL based on the template for the case of no position.
        /// An implementation of this method can throw ParamQueryException
        /// </summary>
        /// <returns>A String of SQL that can be executed</returns>
        String ToSQL();
    }
}
