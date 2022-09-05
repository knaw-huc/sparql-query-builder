package org.uu.nl.goldenagents.netmodels.angular;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.QueryException;
import org.apache.jena.util.FileUtils;
import org.springframework.lang.Nullable;
import org.uu.nl.goldenagents.exceptions.BadQueryException;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can encode various types of errors that could occur while parsing
 * a SPARQL query string.
 *
 * This encoded object can be used by any view to highlight incorrect parts of the query
 * and suggest changes
 */
public class QueryExceptionInfo implements FIPASendableObject {

    private static final String COMMON_PREFIXES_FILE_NAME = "configs/common_prefixes";

    private Exception originalError;

    private String queryID;
    private String encountered = "";
    private int line;
    private int column;
    private int nCharacters;
    private String helpfulMessage = "";

    private String[] expected = new String[0];

    /**
     * Create a new instance based on an exception thrown during the parsing of a query
     * @param e Thrown exception
     */
    public QueryExceptionInfo(Exception e, String queryID) {
        this.originalError = e;
        this.queryID = queryID;
        if(e instanceof QueryException) {
            fromQueryException();
        } else if(e instanceof BadQueryException) {
            fromBadQueryException();
        } else {
            Platform.getLogger().log(QueryExceptionInfo.class,
                    "Trying to create error info obejct from unknown error class: " + e.getClass().toString());
            this.helpfulMessage = e.getMessage();
        }
    }

    /**
     * By default, it is best to construct this object from an error message, but some issues are not
     * necessarily expressed by errors. This constructor can be used to just encode an error message
     * @param message       Error message, preferably human readable
     */
    public QueryExceptionInfo(String message, String queryID) {
        this.originalError = null;
        this.helpfulMessage = message;
        this.queryID = queryID;
    }

    /**
     * Encode this instance as a JSON object string
     * @return      String containing JSON encoded data of this instance
     */
    public String toString() {
        JsonObject object = new JsonObject();
        object.put("error", this.originalError == null ? this.helpfulMessage : this.originalError.getMessage());
        object.put("humanMessage", this.helpfulMessage);
        object.put("encountered", this.encountered);
        object.put("line", this.line);
        object.put("column", this.column);
        object.put("nCharacters", this.nCharacters);
        object.put("queryID", this.queryID);

        JsonArray expectedOptions = new JsonArray();
        for(String expected : this.expected) {
            expectedOptions.add(expected);
        }

        object.put("expected", expectedOptions);

        return object.toString();
    }

    /**
     * Populate this instance with information that can be
     * extracted from a QueryException as thrown by Jena
     */
    private void fromQueryException() {
        String message = this.originalError.getMessage();

        // For some reason, the column encoded by the QueryException is not the same as the one
        // given in the message
        getErrorPosition();

        // Parse message to something useful
        if(message.contains("Lexical error")) {
            // Lexical error at line 2, column 5. Encountered: " " (32), after : "b"
            parseLexicalError();
        } else if(message.contains("Encountered")) {
            // Encountered " "}" "} "" at line 3, column 1. Was expecting one of: <IRIref> ... <PNAME_NS> ... <PNAME_LN> ... <VAR1> ... <VAR2> ... "a" ... "(" ... "!" ... "^" ...
            parseEncountered();
        } else if (message.contains("Unresolved prefixed name")) {
            parseUnknownPrefix();
        } else {
            Platform.getLogger().log(QueryExceptionInfo.class,
                    "Did not recognise Jena error message. Using Jena default message as human message");
            this.helpfulMessage = this.originalError.getMessage();
        }
    }

    /**
     * From the error message, extract what incorrect character sequence was encountered,
     * and what the parser had expected
     */
    private void parseEncountered() {
        if(this.originalError.getMessage().contains("Encountered \"<EOF>\"")) {
            this.encountered = "End of query";
            this.nCharacters = 0;
        } else {

            Pattern pattern = Pattern.compile("Encountered \" \"(.*)\" \".*\"\"", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(this.originalError.getMessage());

            if(matcher.find() && matcher.groupCount() == 1) {
                this.encountered = matcher.group(1);
                this.nCharacters = this.encountered.length();
            }
        }

        // Find all possible values at this location
        Pattern pattern = Pattern.compile("(?:(<\\w+>)|\"(.*)\") \\.\\.\\.\\s*", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(this.originalError.getMessage());

        ArrayList<String> expectedOnOf = new ArrayList<>();
        while(matcher.find()) {
            expectedOnOf.add(matcher.group(1) == null ? matcher.group(2) : matcher.group(1));
        }

        this.expected = expectedOnOf.toArray(this.expected);
    }

    /**
     * From the error message which encodes a lexical error, extract the specific character string
     * that causes the error
     */
    private void parseLexicalError() {
        Pattern pattern = Pattern.compile("Encountered: \"(.*)\" .* after : \"(.*)\"", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(this.originalError.getMessage());
        if(matcher.find() && matcher.groupCount() == 2) {
            String found = matcher.group(1);
            String after = matcher.group(2);
            this.nCharacters = found.length();
            this.encountered = found;
            this.helpfulMessage = String.format("Found \"%s\" after \"%s\" on line %d. This is invalid",
                    found, after, this.line);
        }
    }

    private void parseUnknownPrefix() {
        Pattern pattern = Pattern.compile("Unresolved prefixed name: ((.*?):.*)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(this.originalError.getMessage());

        if(matcher.find() && matcher.groupCount() == 2) {
            this.encountered = matcher.group(1);
            this.nCharacters = matcher.group(2).length();
            this.helpfulMessage = String.format("The prefix '%1$s' in %2$s was not recognized. Please find the URI for this " +
                            "prefix and add the following to the start of your query:\n" +
                            "PREFIX %1$s: <YOUR_%3$s_URI>\n" +
                            "(Make sure to replace YOUR_%3$s_URI with the URI for this prefix)",
                    matcher.group(2),
                    matcher.group(1),
                    matcher.group(2).toUpperCase()
            );

            String suggestedPrefixLine = getEntryForPrefix(matcher.group(2));
            if(suggestedPrefixLine != null) {
                this.helpfulMessage += String.format("\nPerhaps the prefix you are looking for is:\n%s", suggestedPrefixLine);
            }
        }
    }

    /**
     * Try to find the most likely SPARQL PREFIX line for the given prefix
     * @param prefix    Prefix for which import statement is needed
     * @return          Import statement if prefix is known
     */
    private @Nullable String getEntryForPrefix(String prefix) {
        String fullPrefixLine = null;
        String expectedPrefix = String.format("prefix %s:", prefix.toLowerCase());

        try(BufferedReader bufferedReader = FileUtils.openResourceFile(COMMON_PREFIXES_FILE_NAME)) {
            String prefixLine;
            while((prefixLine = bufferedReader.readLine()) != null) {
                if(prefixLine.toLowerCase().startsWith(expectedPrefix)) {
                    fullPrefixLine = prefixLine;
                    break;
                }
            }
        } catch(IOException ex) {
            Platform.getLogger().log(QueryExceptionInfo.class, Level.INFO, ex);
        }

        return fullPrefixLine;
    }

    /**
     * Populate this instance with information that can be
     * extracted from a BadQueryException as thrown by the GA agents
     */
    private void fromBadQueryException() {
        // TODO, but later when this becomes relevant. For now, just use custom error msg
        this.helpfulMessage = this.originalError.getMessage();
    }

    /**
     * Any parsing error that has information available about the line and column where the error
     * occured in the original query should encode that information on the exception itself. In the
     * rare cases that this is not the case, but line and column number are listed in the error message,
     * this function can extract those values
     */
    private void getErrorPosition() {
        Pattern pattern = Pattern.compile("line (\\d+), column (\\d+)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(this.originalError.getMessage());

        if(matcher.find() && matcher.groupCount() == 2) {
            this.line = Integer.parseInt(matcher.group(1));
            this.column = Integer.parseInt(matcher.group(2));
        }
    }



}
