import java.io.FileNotFoundException;
import java.io.FileReader;

public class HIRCompiler {
	public static void main(String[] args) {
		String inName = "";

        if (args.length == 2) {
            inName = args[0];
        } else {
            System.err.println("usage: Compile <input file> <output file>");
            System.exit(-1);
        }

        FileReader inFile = null;
        try {
            inFile = new FileReader(inName);
        } catch (FileNotFoundException ex) {
            System.err.println("File " + inName + " not found.");
            System.exit(-1);
        }

        parser P = new parser(new Yylex(inFile));

        Program program = null;

        try {
            program = (Program) P.parse().value; // do the parse
        } catch (Exception ex) {
            System.err.println("Exception occured during parse: " + ex);
            System.exit(-1);
        }

        if (Errors.fatalError) {
            System.err.println("Confused by earlier errors: aborting");
            System.exit(0);
        }
        
        program.compile();
	}
}
