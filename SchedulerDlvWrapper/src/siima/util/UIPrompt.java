package siima.util;

import java.io.BufferedReader;
import java.io.IOException;

public class UIPrompt {
	
	public String getUserInput(String prompt, BufferedReader _input)
			throws IOException {

		System.out.println(prompt); // + " (or ENTER for none)");

		String line = _input.readLine();

		if (line.trim().equals(""))
			line = null;

		return line;
	}
	

}
