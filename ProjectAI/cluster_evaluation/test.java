package cluster_evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import io.*;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String,Integer> tes = new HashMap<String,Integer>();
		try{
			System.out.println("Dalam"+tes.put("halo",tes.get("halo")+4));
		} catch(Exception e){
			tes.put("halo", 4);
		}
		System.out.println("Keluar"+tes.get("halo"));
		
		String[] ss = FileLoadingUtils.listDirectoriesDirectory("../MeasuredLabel");
		ArrayList<String> sss= FileLoadingUtils.listFilesDirectory("../MeasuredLabel/Topic1");
		System.out.println(ss.length);
		System.out.println(sss.toString());
		for (String s:sss){
			System.out.println(s);
		}
		
	}

}
