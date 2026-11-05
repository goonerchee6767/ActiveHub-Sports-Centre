/**
 * Storable - interface for any object that can be saved to a text file
 * as ONE line in a simple pipe-delimited ("|") format.
 *
 * Demonstrates ABSTRACTION: DataStore only needs to know that an object
 * can convert itself into a file line - not how each class does it.
 */
public interface Storable {

    /** Converts the object into a single pipe-delimited line for the text file. */
    String toFileLine();
}
