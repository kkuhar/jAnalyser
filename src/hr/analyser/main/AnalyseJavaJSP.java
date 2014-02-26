package hr.analyser.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;

public class AnalyseJavaJSP {
	private static final String path=".";
	private static String definition_file;
	


	public static void writeFunctionCallPos(String functionName,String funcDef,String current_path) throws FileNotFoundException{
		File file=new File(current_path);
		String linija=null;
		for(File currentFile: file.listFiles()){
		if(currentFile.isDirectory())	{
			writeFunctionCallPos(functionName, funcDef, currentFile.getAbsolutePath());
		}else{
		FileReader reader= new FileReader(currentFile);
		Scanner scanFile= new Scanner(reader);
		int i=1;
		//System.out.println(currentFile.getAbsolutePath());
		while(scanFile.hasNext()){
			linija=scanFile.nextLine();
			/*if(linija.startsWith(funcDef))
				pozivajuca_fukcija=linija.substring(8);*/
			if((!currentFile.getAbsolutePath().equals(definition_file))&&linija.contains(functionName.trim())&&(!currentFile.getAbsolutePath().contains(".txt"))){						
						System.out.println("               Poziva se u: "+currentFile.getName()+" na "+i +". liniji koda");
				}
			i++;
			}
		scanFile.close();
		}
		
		}
		
	}
	
	
	public static String getFunctionName(String functionName) throws FileNotFoundException{
		int first_pos=0;
		int final_pos=0;
		int found_flag=0;
		String functionNameClean;
			for(int i=functionName.length()-1;i>0;i--){
				if(functionName.charAt(i)=='(')
				{
					found_flag=1;
					final_pos=i;				
				}
				if(functionName.charAt(i)==' '&&found_flag==1)
				{
					first_pos=i;
				}
			}
			functionNameClean=functionName.substring(first_pos,final_pos);
		//	System.out.println("+++++++"+functionNameClean);
			return functionNameClean;
		
			//System.out.println(functionNameClean);
		}
	
	public static void writeFunctionDefinitionPos(File file, List<String> funcDef,int calls) throws FileNotFoundException{
		System.out.println("***************************************************\n"+file.getName()+"\n***************************************************");
		int i=1;
		String linija=null;
		String def="";
		String klasa="";
		FileReader reader= new FileReader(file);
		Scanner scanFile= new Scanner(reader);
		while(scanFile.hasNext()){
			linija=scanFile.nextLine();
		//	System.out.println(linija.trim());
			if(linija.trim().startsWith("class")||(linija.trim().startsWith("public")&&linija.contains("class")))
				{
					klasa=linija;			
					System.out.println(i+". "+klasa.trim());
				}
			
			for(int j=0;j<funcDef.size();j++)
			{
				def=funcDef.get(j);
			//	System.out.println(def);
				if(linija.trim().startsWith(def)&&(!linija.trim().contains("class")))
				{
					if(linija.trim().contains("(")&&(!linija.trim().contains("new"))){
						System.out.println(i+":       M: "+linija.trim());
					//	System.out.println(calls);
						if(calls==1){
							definition_file=file.getAbsolutePath();
							writeFunctionCallPos(	getFunctionName(linija.trim()),def,path);}
					}
					else
						System.out.println(i+":       A: "+linija.trim());
					
				}				
			}
			i++;
		}
		scanFile.close();
	}	
}
