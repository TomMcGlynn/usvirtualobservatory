namespace tapLib.Db.ParamQuery {
    class DefaultSqlQueryGenerator : AbstractSqlQueryGenerator {
        private const string DEFAULT_TABLE_NAME = "Default Table Name";

        public override string tableName {
            get { return DEFAULT_TABLE_NAME; }
        }
    }
}
