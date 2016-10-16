/* FileUtil.java
 * 
 * For writing the ontology into owl-file.
 * 
 * from: http://www.javadb.com/write-lines-of-text-to-file-using-a-printwriter
 */

package siima.util;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtil {
	public void writeLinesToFile(String filename, String[] linesToWrite,
			boolean appendToFile) {

		PrintWriter pw = null;

		try {

			if (appendToFile) {

				// If the file already exists, start writing at the end of it.
				pw = new PrintWriter(new FileWriter(filename, true));

			} else {

				pw = new PrintWriter(new FileWriter(filename));
				// this is equal to:
				// pw = new PrintWriter(new FileWriter(filename, false));

			}

			for (int i = 0; i < linesToWrite.length; i++) {
				
				if(linesToWrite[i]!=null){
				pw.println(linesToWrite[i]);
				}
			}
			pw.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			// Close the PrintWriter
			if (pw != null)
				pw.close();

		}

	}

	public static void main(String[] args) {
		FileUtil util = new FileUtil();
		util.writeLinesToFile("myfile.txt", new String[] { "Line 1", "Line 2",
				"Line 3" }, true);
	}

}
