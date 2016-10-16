/* N‰in Ei onnistu: kirjoittaa useampia rivej‰ ??? 
 * Mutta n‰in tehd‰‰n UpperCamelCase
 * */

package siima.util;

import java.io.*;
//import java.io.FileOutputStream;


public class Tiedostoon {

	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Tiedostoon kirja = new Tiedostoon();
		String str1="rivi2:tekstia tassa kahdella rivilla!";
		String str2="rivi1:" + "\n" + str1;
		String kameli=new String();
		//kameli.concat("h"); ei toimi
		String lause="aa bb cc";
		String[] sanat=lause.split("\\s");
		
		for (int i=0;i<sanat.length;i++){
		sanat[i]=sanat[i].substring(0, 1).toUpperCase()+sanat[i].substring(1);
		//kameli.concat(sanat[i]);
		kameli=kameli+sanat[i];
		System.out.println("\n" + sanat[i]);
		}
		
		File fl=new File("teksti.txt");
		
		try {
			FileOutputStream fout=new FileOutputStream(fl);
			BufferedOutputStream bfout=new BufferedOutputStream(fout);
			
			FileWriter fwr=new FileWriter(fl);
			fwr.write(str2);
			
			fwr.flush();
			fwr.close();
			
			/*
			fout.write(str2.getBytes());
			fout.write("\n".getBytes());
			fout.write(str2.getBytes());
			fout.flush();
			*/
			fout.close();
			
			StringWriter swr = new StringWriter();
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		System.out.println("hei" + str2);
		
		System.out.println("\n" + sanat[0]);
		System.out.println("\n" + kameli + "\n pituus:" + kameli.length());
		
	}

}
