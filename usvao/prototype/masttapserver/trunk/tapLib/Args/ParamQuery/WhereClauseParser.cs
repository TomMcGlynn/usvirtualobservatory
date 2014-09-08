using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;

namespace tapLib.Args.ParamQuery {

    class WhereClauseParser {
        private readonly String _whereClause;
        private readonly String _text;
        private int _textPos;
        private readonly int _textLen;
        private readonly ConstraintGroupList _constraintGroupList;
        private char _ch;
        private Token _token;
        private readonly DiagArg _diagArg = DiagArg.OFF;

        public Token token { get { return _token; } }
        public TokenId tokenId { get { return _token.id; } }
        public String tokenText { get { return _token.text; } }
        public DiagArg diag { get { return _diagArg; } }
        public ConstraintGroupList constraints { get { return _constraintGroupList; } }

        public WhereClauseParser(String whereClause) : this(whereClause, new ConstraintGroupList(), DiagArg.DEFAULT) {}

        public WhereClauseParser(String whereClause, ConstraintGroupList constraintGroupList, DiagArg diagArg) {
            _whereClause = whereClause;
            _constraintGroupList = constraintGroupList;
            _diagArg = diagArg;
            _text = _whereClause;
            _textLen = _text.Length;
            _textPos = 0;
            SetTextPos(0);
        }

        public void parse()
        {
            do
            {
                _parseOneConstraint();
            } while (tokenId == TokenId.SemiColon);
        }

        private String _parseColumnName() {
            // Get the constraint name
            if (tokenId != TokenId.Identifier && tokenId != TokenId.StringLiteral) {
                throw ParseError(_textPos, Res.IdentifierExpected);
            }
            String result = tokenText;
            // Special handling for a StringLiteral -- transform to an identifier
            if (tokenId == TokenId.StringLiteral) {
                // Remove quotes and spaces
                Regex reg = new Regex(@"\s|['""]");
                result = reg.Replace(result, "");
            }
            return result;
        }

        void _parseOneConstraint() {
            NextToken();

            // This is here for the case when someone enters WHERE=x,22;
            // and nothing else
            if (_isConstraintEnd()) return;

            String fieldName = _parseColumnName();
            NextToken();

            // Jump over the ,
            if (tokenId != TokenId.Comma) {
                throw ParseError(_textPos, Res.CommaExpected, fieldName);
            }
            NextToken();

            // Now look for an identifier, string literal or real
            // Check for a possibly leading ! indicating negation and null

            Boolean isNegated = false;
            if (tokenId == TokenId.Exclamation) {
                isNegated = true;
                NextToken();
            }

            // Now check for special null identifier
            if (TokenIdentifierIs("null")) {
                _parseNullConstraint(fieldName, isNegated);
                NextToken();
                // For a null constraint, there is no more possible so return
                return;
            }

            switch (tokenId)
            {
                case TokenId.Identifier:
                case TokenId.StringLiteral:
                case TokenId.UnquotedLiteral:
                    _parseTextConstraint(fieldName, isNegated);
                    break;
                case TokenId.RealLiteral:
                case TokenId.IntegerLiteral:
                case TokenId.FloatLiteral:
                case TokenId.Slash:
                    _parseNumericConstraint(fieldName, isNegated);
                    break;
                case TokenId.SemiColon:
                case TokenId.End:
                    throw ParseError(Res.NoConstraint, fieldName);
                default:
                    throw ParseError(_textPos, Res.WhereSyntaxError, token.pos);
            }

        }

        static void _logConstraint(String fieldName, Boolean isNegated, FieldConstraintGroup.ConstraintType type, String constraint) {
            Console.WriteLine("Name: {0}, type: {1}, isNegated: {2}, Constraint: {3}", fieldName, type, isNegated, constraint);
        }

        void _parseNullConstraint(String fieldName, Boolean isNegated) {
            const FieldConstraintGroup.ConstraintType type =
                FieldConstraintGroup.ConstraintType.NULL_CONSTRAINT;            
            var newConstraintGroup = new FieldConstraintGroup(fieldName, isNegated, type,
                                                              new List<string> {"null"});
            _constraintGroupList.Add(newConstraintGroup);
            if (diag.isOn()) {
                _logConstraint(fieldName, isNegated, type, "null");
            }
        }

        // This removes the containing quotes in a StringLiteral since they are annoying further on
        private static String _removeAnyQuotes(Token t) {
            if (t.id == TokenId.StringLiteral) {
                return t.text.Substring(1, t.text.Length - 2);
            }
            return t.text;
        }

        void _parseTextConstraint(String fieldName, Boolean isNegated) {
            const FieldConstraintGroup.ConstraintType type = FieldConstraintGroup.ConstraintType.TEXT_CONSTRAINT;
            // String is of the form x,'blah',blah,'blah'
            var constraintGroupList = new List<String>();
            String firstConstraint = _removeAnyQuotes(token);
            constraintGroupList.Add(firstConstraint);
            if (diag.isOn()) {
                _logConstraint(fieldName, isNegated, type, firstConstraint);
            }
            NextToken();
            while(!_isConstraintEnd()) {
                if (tokenId == TokenId.Comma)
                {
                    NextToken();
                }
                String constraint = _removeAnyQuotes(token);
                constraintGroupList.Add(constraint);
                if (diag.isOn()) {
                    _logConstraint(fieldName, isNegated, type, constraint);
                }
                NextToken();
            }

            var newConstraintGroup = new FieldConstraintGroup(fieldName, isNegated, type, constraintGroupList);
            _constraintGroupList.Add(newConstraintGroup);
        }
        
        void _getSlashConstraint(StringBuilder sb, Boolean hasLowValue, String fieldName) {
            sb.Append(tokenText);
            NextToken();
            switch(tokenId)
            {
                case TokenId.IntegerLiteral:
                case TokenId.RealLiteral:
                case TokenId.FloatLiteral:
                    sb.Append(tokenText);
                    NextToken();
                    break;
                case TokenId.Exclamation:
                    throw ParseError(Res.NoExclamationExpected, fieldName);
                case TokenId.End:
                case TokenId.Comma:
                case TokenId.SemiColon:
                    if (!hasLowValue) {
                        throw ParseError(Res.ExpectedNumeric, fieldName);
                    }
                    break;
                default:
                    throw ParseError(Res.ExpectedNumeric, fieldName);
            }
        }

        String _getOneNumericConstraint(String fieldName) {
            StringBuilder sb = new StringBuilder();
            switch (tokenId)
            {
                case TokenId.IntegerLiteral:
                case TokenId.RealLiteral:
                case TokenId.FloatLiteral:
                    sb.Append(tokenText);
                    NextToken();
                    switch(tokenId)
                    {
                        case TokenId.Slash:
                            // value,3/?  - has low value
                            _getSlashConstraint(sb, true, fieldName);
                            break;
                        case TokenId.End:
                        case TokenId.Comma:
                        case TokenId.SemiColon:
                            break;
                        default:
                            throw ParseError(Res.CommaOrSemiColonExpected, fieldName);
                    }
                    break;
                case TokenId.Slash:
                    // value /? -- no low value
                    _getSlashConstraint(sb, false, fieldName);
                    break;
                default:
                    throw ParseError(Res.ExpectedNumeric, fieldName);
            }
            return sb.ToString();
        }

        void _parseNumericConstraint(String fieldName, Boolean isNegated) {
            const FieldConstraintGroup.ConstraintType type = FieldConstraintGroup.ConstraintType.NUMERIC_CONSTRAINT;
            // String is of the form x,23,23/,/23,23/35
            var constraintGroupList = new List<String>();
            String firstConstraint = _getOneNumericConstraint(fieldName);
            constraintGroupList.Add(firstConstraint);
            if (diag.isOn()) {
                _logConstraint(fieldName, isNegated, type, firstConstraint);
            }
            // left at comma by getOneNumericConstraint
            while (tokenId == TokenId.Comma) {
                // Eat the comma
                NextToken();

                String constraint = _getOneNumericConstraint(fieldName);
                constraintGroupList.Add(constraint);
                if (diag.isOn()) {
                    _logConstraint(fieldName, isNegated, type, constraint);
                }
            }

            var newConstraintGroup = new FieldConstraintGroup(fieldName, isNegated, type, constraintGroupList);
            _constraintGroupList.Add(newConstraintGroup);
        }

        internal struct Token {
            public TokenId id;
            public string text;
            public int pos;
        }

        internal enum TokenId {
            Unknown,
            End,
            Identifier,
            StringLiteral,
            UnquotedLiteral,
            IntegerLiteral,
            RealLiteral,
            FloatLiteral,
            Exclamation,
            Asterisk,
            Comma,
            Dot,
            Slash,
            SemiColon
        }

        public sealed class ParseException : Exception {
            readonly int position;

            public ParseException(string message, int position)
                : base(message) {
                this.position = position;
            }

            public int Position {
                get { return position; }
            }

            public override string ToString() {
                return string.Format(Res.ParseExceptionFormat, Message, position);
            }
        }

        void SetTextPos(int pos) {
            _textPos = pos;
            _ch = _textPos < _textLen ? _text[_textPos] : '\0';
        }

        void NextChar() {
            if (_textPos < _textLen) _textPos++;
            _ch = _textPos < _textLen ? _text[_textPos] : '\0';
        }

        internal void NextToken() {
            while (Char.IsWhiteSpace(_ch)) NextChar();
            TokenId t;
            int tokenPos = _textPos;
            switch (_ch) {
                case '!':
                    NextChar();
                    t = TokenId.Exclamation;
                    break;
                case ',':
                    NextChar();
                    t = TokenId.Comma;
                    break;
                case ';':
                    NextChar();
                    t = TokenId.SemiColon;
                    break;
                case '.':
                    NextChar();
                    t = TokenId.Dot;
                    break;
                case '/':
                    NextChar();
                    t = TokenId.Slash;
                    break;
                case '"':
                case '\'':
                    char quote = _ch;
                    do {
                        NextChar();
                        while (_textPos < _textLen && _ch != quote) NextChar();
                        if (_textPos == _textLen)
                            throw ParseError(_textPos, Res.UnterminatedStringLiteral);
                        NextChar();
                    } while (_ch == quote);
                    t = TokenId.StringLiteral;
                    break;
                default:
                    if (Char.IsLetter(_ch) || _ch == '_') {
                        t = TokenId.Identifier;
                        do {
                            NextChar();
                            // This is to allow embedded *
                            // The set of chars allowed in unquoted strings is
                            // less than quoted literals.  Not sure what to allow
                            if (_ch == '*') t = TokenId.UnquotedLiteral;
                        } while (Char.IsLetterOrDigit(_ch) || _ch == '*' || _ch == '_');
                        break;
                    }
                    if (Char.IsDigit(_ch)) {
                        t = TokenId.IntegerLiteral;
                        do {
                            NextChar();
                        } while (Char.IsDigit(_ch));
                        if (_ch == '.') {
                            t = TokenId.RealLiteral;
                            NextChar();
                            ValidateDigit();
                            do {
                                NextChar();
                            } while (Char.IsDigit(_ch));
                        } //exponential notation - E or e following digit.
                        if (_ch == 'E' || _ch == 'e'){ 
                            t = TokenId.FloatLiteral;
                            NextChar();
                            if (_ch == '-') { //can be negative
                                NextChar();
                            }
                            while (Char.IsDigit(_ch)) {
                                NextChar();
                            } 
                        }
                        break;
                    }
                    // Adding * here to allow unquoted wildcards and to keep the
                    // determination of an identifier a little bit cleaner, yech
                    if (_ch == '*') {
                        t = TokenId.UnquotedLiteral;
                        NextChar();
                        while (_textPos < _textLen && _ch != ',' && _ch != ';') NextChar();
                        break;
                    }

                    if (_textPos == _textLen) {
                        t = TokenId.End;
                        break;
                    }
                    throw ParseError(_textPos, Res.InvalidCharacter, _ch);
            }
            _token.id = t;
            _token.text = _text.Substring(tokenPos, _textPos - tokenPos);
            _token.pos = tokenPos;
        }

        internal bool TokenIdentifierIs(String id) {
            return _token.id == TokenId.Identifier && String.Equals(id, _token.text, StringComparison.OrdinalIgnoreCase);
        }

        internal String GetIdentifier() {
            ValidateToken(TokenId.Identifier, Res.IdentifierExpected);
            string id = _token.text;
            if (id.Length > 1 && id[0] == '@') id = id.Substring(1);
            return id;
        }

        void ValidateDigit() {
            if (!Char.IsDigit(_ch)) throw ParseError(_textPos, Res.DigitExpected);
        }

        void ValidateToken(TokenId t, string errorMessage) {
            if (_token.id != t) throw ParseError(errorMessage);
        }

        Boolean _isConstraintEnd() {
            return tokenId == TokenId.End || tokenId == TokenId.SemiColon;
        }

        internal void ValidateToken(TokenId t) {
            if (_token.id != t) throw ParseError(Res.SyntaxError);
        }

        Exception ParseError(string format, params object[] args) {
            return ParseError(_token.pos, format, args);
        }

        static Exception ParseError(int pos, string format, params object[] args) {
            return new ParseException(string.Format(System.Globalization.CultureInfo.CurrentCulture, format, args), pos);
        }

        static internal class Res {
            public const string UnterminatedStringLiteral = "Unterminated string literal";
            public const string InvalidCharacter = "Syntax error '{0}'";
            public const string DigitExpected = "Digit expected";
            public const string SyntaxError = "Syntax error";
            public const string WhereSyntaxError = "Where clause syntax error at location: {0}";
            public const String ExpectedNumeric =
                "A numeric value was expected following the / in the WHERE clause for: {0}";
            public const string ParseExceptionFormat = "{0} (at index {1})";
            public const string CommaOrSemiColonExpected = "',' or ';' expected in WHERE clause for: {0}";
            public const string IdentifierExpected = "Identifier expected";
            public const String CommaExpected = "A comma must follow the identifier: \"{0}\" in the WHERE clause";
            public const String NoExclamationExpected = "A negation (!) should only precede the first constraint for dentifier: \"{0}\" in the WHERE clause";
            public const String NoConstraint = "Empty constraint string for column: \"{0}\" in the WHERE caluse";
        }
    }
}
